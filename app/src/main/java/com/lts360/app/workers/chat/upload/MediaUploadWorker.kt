package com.lts360.app.workers.chat.upload

import android.app.NotificationManager
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.lts360.App
import com.lts360.api.auth.managers.socket.SocketConnectionException
import com.lts360.api.auth.managers.socket.SocketManager
import com.lts360.app.database.models.chat.ChatMessageStatus
import com.lts360.app.database.models.chat.MessageProcessingData
import com.lts360.app.workers.chat.utils.awaitConnectToSocket
import com.lts360.compose.ui.chat.repos.ChatUserRepository
import com.lts360.compose.ui.chat.repos.UploadWorkerUtilRepository
import com.lts360.compose.ui.chat.viewmodels.FileUploadState
import com.lts360.compose.ui.chat.viewmodels.serializeFileUploadState
import com.lts360.compose.ui.managers.UserSharedPreferencesManager
import io.socket.client.Ack
import io.socket.client.Socket
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import kotlin.coroutines.resumeWithException
import androidx.core.net.toUri
import org.koin.android.annotation.KoinWorker

@KoinWorker
class MediaUploadWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val chatUserRepository: ChatUserRepository,
    private val uploadWorkerUtilRepository: UploadWorkerUtilRepository,
    val socketManager: SocketManager
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val CHUNK_SIZE = 1024 * 1024
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


    private lateinit var socket: Socket



    private val notificationId = id.hashCode()

    private var fileId: String? = null
    private var chunkIndex: Long = -1L

    override suspend fun doWork(): Result {


        val fileName = inputData.getString("fileName") ?: return Result.failure()
        val extension = inputData.getString("extension") ?: return Result.failure()
        val category = inputData.getString("category") ?: return Result.failure()
        val mimeType = inputData.getString("mimeType") ?: return Result.failure()
        val fileAbsPath = inputData.getString("fileAbsPath") ?: return Result.failure()

        val senderId = inputData.getLong("senderId", -1)
        val recipientId = inputData.getLong("recipientId", -1)
        val messageId = inputData.getLong("messageId", -1)
        val replyId = inputData.getLong("replyId", -1)
        val content = inputData.getString("content") ?: ""

        val width = inputData.getInt("width", -1)
        val height = inputData.getInt("height", -1)
        val totalDuration = inputData.getLong("totalDuration", -1)

        val e2eeKey =
            chatUserRepository.getPublicKeyWithVersionByRecipientId(recipientId).firstOrNull()
        val publicKey = e2eeKey?.publicKey ?: ""
        val keyVersion = e2eeKey?.keyVersion ?: -1L


        val userId = UserSharedPreferencesManager.userId

        if (userId == -1L) {
            return Result.failure()
        }

        var cachedFilePath = uploadWorkerUtilRepository.getFileCachePathByMessageId(messageId)

        return try {
            socket = awaitConnectToSocket(socketManager, !App.isAppInForeground)

            try {
                if (publicKey.isEmpty() || keyVersion == -1L) {

                    return withTimeout<Result>(30_000) {
                        suspendCancellableCoroutine { continuation ->
                            if (publicKey.isEmpty() || keyVersion == -1L) {

                                if (socket.connected()) {
                                    socket.emit("chat:queryPublicKey", JSONObject().apply {
                                        put("user_id", userId)
                                        put("recipient_id", recipientId)
                                    }, Ack { args ->

                                        if (isStopped) {
                                            return@Ack
                                        }

                                        if (args.isNotEmpty()) {
                                            val response = args[1] as JSONObject
                                            val responseRecipientId =
                                                response.getLong("recipient_id")
                                            val responsePublicKey = response.getString("publicKey")
                                            val responseKeyVersion = response.getLong("keyVersion")

                                            CoroutineScope(Dispatchers.IO).launch {
                                                chatUserRepository.updatePublicKeyByRecipientId(
                                                    responseRecipientId,
                                                    responsePublicKey,
                                                    responseKeyVersion
                                                )
                                                chatUserRepository.updateMessage(
                                                    messageId,
                                                    ChatMessageStatus.QUEUED
                                                )

                                                continuation.resume(Result.retry()) { cause, _, _ -> }// Resume the operation after the update
                                            }
                                        } else {
                                            CoroutineScope(Dispatchers.IO).launch {
                                                chatUserRepository.updateMessage(
                                                    messageId,
                                                    ChatMessageStatus.FAILED
                                                )
                                                continuation.resume(Result.failure()) { cause, _, _ -> } // Fail the operation if no response
                                            }
                                        }
                                    })
                                } else {
                                    continuation.resumeWithException(SocketConnectionException("Query key: Socket is not connected"))
                                }


                            } else {
                                continuation.resume(Result.success()) { cause, _, _ -> }
                            }

                            continuation.invokeOnCancellation {
                                socket.off("chat:queryPublicKey")
                            }
                        }
                    }
                }
            } catch (e: TimeoutCancellationException) {
                e.printStackTrace()
                return Result.retry()
            } catch (e: Exception) {
                e.printStackTrace()
                throw e
            }

            val inputStream =
                applicationContext.contentResolver.openInputStream(fileAbsPath.toUri())
                    ?: return Result.failure()

            updateUploadState(messageId, FileUploadState.Started)

            val encryptedBytes = try {
                withContext(Dispatchers.IO) {
                    uploadWorkerUtilRepository.getEncryptedFileBytes(
                        applicationContext,
                        messageId,
                        inputStream,
                        publicKey,
                        cachedFilePath,
                        extension,
                        fileName.substringBeforeLast("."),
                        "file"
                    )
                }.let {
                    if (cachedFilePath == null) {
                        cachedFilePath = it.absolutePath
                        it.readBytes()

                    } else {
                        it.readBytes()
                    }
                }
            } catch (e: Exception) {
                updateUploadState(
                    messageId,
                    FileUploadState.Retry(e.message ?: "Error: Caused by file encryption")
                )
                deleteCachedFileAndUpdateMessage(messageId, cachedFilePath)
                e.printStackTrace()
                return Result.failure()
            }

            createUploadNotificationChannel(notificationManager)

            updateNotification(0)

            sendMedia(
                senderId,
                recipientId,
                messageId,
                replyId,
                content,
                category,
                keyVersion,
                fileName,
                extension,
                mimeType,
                width,
                height,
                totalDuration,
                cachedFilePath,
                encryptedBytes,
            )

            return Result.success()
        } catch (e: SocketConnectionException) {

            fileId?.let {
                socket.off("chat:fileTransferCompleted-${it}")
            }

            if (chunkIndex != -1L) {
                socket.off("chat:mediaChunkAck-${fileId}-$chunkIndex")
            }

            updateUploadState(
                messageId,
                FileUploadState.Retry(e.message ?: "Caused by socket is not connected")
            )
            notificationManager.cancel(notificationId)

            Result.retry()
        } catch (_: UserNotActiveException) {
            uploadWorkerUtilRepository.updateMessageStatus(messageId, ChatMessageStatus.FAILED)
            updateUploadState(messageId, FileUploadState.Failed)
            notificationManager.cancel(notificationId)

            deleteCachedFileAndUpdateMessage(messageId, cachedFilePath)

            uploadWorkerUtilRepository.updateLastChunkIndexByMessageId(messageId, -1)
            uploadWorkerUtilRepository.updateLastSentByteOffsetByMessageId(messageId, -1)
            Result.success()
        } catch (_: InvalidKeyVersionException) {
            updateUploadState(messageId, FileUploadState.Retry("SocketAuth: Invalid key version"))
            notificationManager.cancel(notificationId)

            deleteCachedFileAndUpdateMessage(messageId, cachedFilePath)

            uploadWorkerUtilRepository.updateLastChunkIndexByMessageId(messageId, -1)
            uploadWorkerUtilRepository.updateLastSentByteOffsetByMessageId(messageId, -1)

            Result.retry()
        } catch (e: Exception) {
            runBlocking {
                updateUploadState(
                    messageId,
                    FileUploadState.Retry(e.message ?: "Error: Caused by exception")
                )
                notificationManager.cancel(notificationId)
            }
            return Result.failure()
        } finally {
            finalizeSocket()
        }
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun sendMedia(
        senderId: Long,
        recipientId: Long,
        messageId: Long,
        replyId: Long,
        content: String,
        category: String,
        keyVersion: Long,
        fileName: String,
        extension: String,
        mimeType: String,
        width: Int,
        height: Int,
        totalDuration: Long,
        cachedFilePath: String?,
        byteArray: ByteArray
    ) {
        val buffer = ByteArray(CHUNK_SIZE)

        var bytesRead: Int
        var isMessageSent = false

        var lastSentChunkIndex: Long
        var byteOffset: Long

        withContext(Dispatchers.IO) {

            val messageProcessingMediaData =
                uploadWorkerUtilRepository.getMessageProcessingDataByMessageId(messageId)

            val currentFileId = uploadWorkerUtilRepository.getFileIdByMessageId(messageId)
            fileId = currentFileId
                ?: "${senderId}_${recipientId}_${messageId}_${System.currentTimeMillis()}"

            val updatedData = messageProcessingMediaData?.copy(
                fileId = fileId
            ) ?: MessageProcessingData(
                messageId = messageId,
                fileId = fileId
            )

            uploadWorkerUtilRepository.insertOrUpdateMessageProcessingData(updatedData)
            byteOffset = uploadWorkerUtilRepository.getLastSentByteOffset(messageId)
            lastSentChunkIndex =
                (byteOffset + CHUNK_SIZE - 1) / CHUNK_SIZE

        }


        try {
            val totalChunks =
                (byteArray.size + CHUNK_SIZE - 1) / CHUNK_SIZE


            val transferData = JSONObject().apply {
                put("sender_id", senderId)
                put("recipient_id", recipientId)
                put("message_id", messageId)
                put("reply_id", replyId)
                put("type", if (replyId == -1L) "normal" else "reply")
                put("category", category)
                put("message", content)
                put("key_version", keyVersion)
                put("file_id", fileId)
                put("file_name", fileName)
                put("total_chunks", totalChunks)
                put("byte_offset", byteOffset)
                put("file_size", byteArray.size)
                put("extension", extension)
                put("content_type", mimeType)
                put("width", width)
                put("height", height)
                put("total_duration", totalDuration)
                put("timestamp", System.currentTimeMillis())
            }

            val onMessageSentCompleted = CompletableDeferred<Unit>()
            val allChunksAckReceived = CompletableDeferred<Unit>()

            fun onChunkAcknowledged(chunkIndex: Int) {
                if (chunkIndex == totalChunks - 1) {
                    CoroutineScope(Dispatchers.IO).launch {
                        allChunksAckReceived.complete(Unit)
                    }
                }
            }


            socket.once("chat:fileTransferCompleted-${fileId}") { args ->

                if (isStopped || onMessageSentCompleted.isCompleted) {
                    return@once
                }

                CoroutineScope(Dispatchers.IO).launch {

                    val result = args[0] as JSONObject
                    val status = result.optString("status")

                    if (status == "FILE_TRANSFER_COMPLETED") {

                        if (!allChunksAckReceived.isCompleted) {
                            allChunksAckReceived.await()
                        }
                    }


                    if (status == "FILE_TRANSFER_COMPLETED" || status == "ALL_CHUNKS_RECEIVED_FILE_TRANSFER_COMPLETED") {
                        updateUploadState(messageId, FileUploadState.Completed)
                        updateNotification(100, "Upload complete")
                        notificationManager.cancel(notificationId)
                        delay(1000)
                        if (socket.connected()) {

                            socket.emit("chat:sendMedia", transferData)

                            isMessageSent = true

                            uploadWorkerUtilRepository.updateLastChunkIndexByMessageId(
                                messageId,
                                -1
                            )
                            deleteCachedFileAndUpdateMessage(messageId, cachedFilePath)
                            onMessageSentCompleted.complete(Unit)


                        } else {
                            onMessageSentCompleted.completeExceptionally(SocketConnectionException("Error: Sending media caused by ocket is not connected"))
                        }

                    }
                }

            }


            socket.emit("chat:startFileTransfer", transferData, Ack {

                CoroutineScope(Dispatchers.IO).launch {

                    if (isStopped || onMessageSentCompleted.isCompleted) {
                        return@launch
                    }

                    val fileTransferData = if (it.isNotEmpty()) it[0] as? JSONObject else null

                    val status = fileTransferData?.optString("status")

                    if (status != null && status == "KEY_ERROR") {

                        val keyErrorRecipientId = fileTransferData.getLong("recipient_id")
                        val publicKey = fileTransferData.getString("publicKey")
                        val newKeyVersion = fileTransferData.getLong("keyVersion")

                        chatUserRepository.updatePublicKeyByRecipientId(
                            keyErrorRecipientId,
                            publicKey,
                            newKeyVersion
                        )

                        onMessageSentCompleted.completeExceptionally(InvalidKeyVersionException("Invalid key version"))


                    }
                    else if (status != null && status == "USER_NOT_ACTIVE_ERROR") {
                        onMessageSentCompleted.completeExceptionally(UserNotActiveException("User not active"))
                    }else if(status!=null && status == "ERROR"){
                        onMessageSentCompleted.completeExceptionally(UserNotActiveException("Unknown error occurred"))
                    }
                    else {


                        ByteArrayInputStream(byteArray).use { fis ->
                            chunkIndex = lastSentChunkIndex

                            var lastSentByteOffset = byteOffset
                            fis.skip(lastSentByteOffset)

                            while (fis.read(buffer).also { bytesRead = it } != -1) {

                                val chunk = ByteArray(bytesRead).apply {
                                    System.arraycopy(
                                        buffer,
                                        0,
                                        this,
                                        0,
                                        bytesRead
                                    )
                                }

                                if (chunkIndex >= lastSentChunkIndex) {

                                    if (socket.connected()) {

                                        val chunkAcknowledged = CompletableDeferred<Unit>()

                                        socket.once("chat:mediaChunkAck-${fileId}-$chunkIndex") { args ->

                                            if (isStopped || onMessageSentCompleted.isCompleted) {
                                                return@once
                                            }

                                            val mediaChunkAckData = args[0] as? JSONObject

                                            mediaChunkAckData?.let {
                                                CoroutineScope(Dispatchers.IO).launch {

                                                    val updatedChunkIndex = it.getInt("chunkIndex")
                                                    val updatedSize = it.getLong("updatedSize")

                                                    val progress = (updatedSize.toFloat() / byteArray.size.toFloat() * 100).toInt()
                                                    updateNotification(progress)

                                                    updateUploadState(
                                                        messageId,
                                                        FileUploadState.InProgress(progress)
                                                    )

                                                    uploadWorkerUtilRepository.updateLastChunkIndexByMessageId(
                                                        messageId,
                                                        updatedChunkIndex
                                                    )

                                                    lastSentByteOffset = updatedSize

                                                    uploadWorkerUtilRepository.updateLastSentByteOffsetByMessageId(
                                                        messageId,
                                                        lastSentByteOffset
                                                    )
                                                    onChunkAcknowledged(updatedChunkIndex)
                                                    chunkAcknowledged.complete(Unit)
                                                }
                                            }
                                        }


                                        socket.emit("chat:sendFileChunk", JSONObject().apply {
                                            put("file_id", fileId)
                                            put("chunk_index", chunkIndex)
                                            put("byte_offset", lastSentByteOffset)
                                            put("data", chunk)
                                        }, Ack { args ->
                                            val failedReason = it[0] as String

                                            val sendFileChunkResponse = it[1] as JSONObject
                                            val sendFileChunkResponseStatus =
                                                sendFileChunkResponse.getString("status")

                                            if (sendFileChunkResponseStatus == "MEDIA_TRANSFER_NOT_FOUND") {
                                                onMessageSentCompleted.completeExceptionally(
                                                    UploadFailedException(failedReason)
                                                )
                                                if (!chunkAcknowledged.isCompleted) {
                                                    chunkAcknowledged.complete(Unit)
                                                }
                                            }
                                        })

                                        chunkAcknowledged.await()
                                        chunkIndex++

                                    } else {
                                        onMessageSentCompleted.completeExceptionally(
                                            SocketConnectionException("File upload: Socket is not connected")
                                        )
                                    }
                                }
                            }


                        }
                    }


                }

            })


            while (!onMessageSentCompleted.isCompleted && !isMessageSent) {

                if (!socket.connected()) {
                    onMessageSentCompleted.completeExceptionally(SocketConnectionException("Waiting transfer: Socket is not connected"))
                }

                delay(1000)
            }


            if (!onMessageSentCompleted.isCompleted) {
                onMessageSentCompleted.await()
            }

            val exception = onMessageSentCompleted.getCompletionExceptionOrNull()
            if (exception != null) {
                throw exception
            }

        } catch (e: SocketConnectionException) {
            throw e
        } catch (e: InvalidKeyVersionException) {
            e.printStackTrace()
            throw e
        } catch (e: IOException) {
            e.printStackTrace()
            socket.emit("chat:fileTransferError-${fileId}", JSONObject().apply {
                put("file_id", fileId)
                put("status", "error")
                put("message", "File transfer failed: ${e.message}")
            })
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }


    private suspend fun deleteCachedFileAndUpdateMessage(messageId: Long, cachedFilePath: String?) {

        cachedFilePath?.let {
            File(it).apply {
                if (exists()) {
                    delete()
                }
            }
            uploadWorkerUtilRepository.updateMessageFileCachePath(messageId, null)
        }
    }

    private suspend fun updateUploadState(messageId: Long, state: FileUploadState) {
        withContext(Dispatchers.IO) {
            if (!isStopped) {
                try {
                    setProgress(
                        workDataOf(
                            "messageId" to messageId,
                            "state" to serializeFileUploadState(state)
                        )
                    )
                } catch (_: IllegalStateException) {
                    if (state == FileUploadState.Completed) {
                        throw Exception("Complted staet updating rpogress 1")
                    }

                }
            } else {
                if (state == FileUploadState.Completed) {
                    throw Exception("Complted staet updating rpogress")
                }
            }
        }
    }


    private fun updateNotification(progress: Int, message: String = "Uploading") {


        sendUploadNotification(
            applicationContext,
            notificationManager,
            notificationId,
            progress,
            message
        )
    }


    private suspend fun finalizeSocket() {
        if (socketManager.isBackgroundSocket) {
            socketManager.destroySocket()
            if (App.isAppInForeground) {
                socketManager.initSocket()
            }
        }
    }

}

