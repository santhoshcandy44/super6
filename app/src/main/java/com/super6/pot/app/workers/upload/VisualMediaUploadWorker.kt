package com.super6.pot.app.workers.upload

import android.app.NotificationManager
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.media3.common.util.UnstableApi
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.super6.pot.App
import com.super6.pot.api.auth.managers.socket.SocketConnectionException
import com.super6.pot.api.auth.managers.socket.SocketManager
import com.super6.pot.app.database.models.chat.ChatMessageStatus
import com.super6.pot.app.database.models.chat.MessageProcessingData
import com.super6.pot.app.workers.awaitConnectToSocket
import com.super6.pot.ui.chat.repos.ChatUserRepository
import com.super6.pot.ui.chat.repos.UploadWorkerUtilRepository
import com.super6.pot.ui.chat.viewmodels.FileUploadState
import com.super6.pot.ui.chat.viewmodels.serializeFileUploadState
import com.super6.pot.ui.managers.UserSharedPreferencesManager
import com.super6.pot.utils.LogUtils.TAG
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
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
import java.io.FileInputStream
import java.io.IOException
import kotlin.coroutines.resumeWithException

@HiltWorker
class VisualMediaUploadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val chatUserRepository: ChatUserRepository,
    private val uploadWorkerUtilRepository: UploadWorkerUtilRepository,
    val socketManager: SocketManager
) : CoroutineWorker(context, workerParams) {


    companion object {
        private const val CHUNK_SIZE = 1024 * 1024
        private const val THUMBNAIL_CHUNK_SIZE = 1024 * 1024
    }


    // This will hold the notification ID (for notification updates)
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


    private lateinit var socket: Socket // Use the appropriate type for your socket


    private val notificationId = id.hashCode()

    // Generate unique fileId using sender, recipient, message, and timestamp
    private var fileId: String? = null
    private var chunkIndex: Long = -1L
    private var thumbnailChunkIndex: Long = -1L

    @androidx.annotation.OptIn(UnstableApi::class)
    override suspend fun doWork(): Result {


        // Retrieve input data
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

        var thumbCachePath = uploadWorkerUtilRepository.getFileThumbPathByMessageId(messageId)

        // Try to get cached file path from the database
        var cachedFilePath = uploadWorkerUtilRepository.getFileCachePathByMessageId(messageId)




        return try {
            socket = awaitConnectToSocket(socketManager, !App.isAppInForeground)


            try {
                if (publicKey.isEmpty() || keyVersion == -1L) {

                    return withTimeout<Result>(30_000) { // Timeout set to 30 seconds
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

                                            // Perform database updates within the coroutine scope
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
                                    continuation.resumeWithException(SocketConnectionException("Query E2EE key: Socket is not connected"))
                                }


                            } else {

                                continuation.resume(Result.success()) { cause, _, _ -> } // If publicKey or keyVersion is already valid, return success
                            }

                            // Handle cancellation
                            continuation.invokeOnCancellation {
                                socket.off("chat:queryPublicKey")
                            }
                        }
                    }
                }
            } catch (e: TimeoutCancellationException) {
                e.printStackTrace()
                // Handle timeout
                return Result.retry()
            } catch (e: Exception) {
                e.printStackTrace()
                throw e
            }


            val thumbnailData = uploadWorkerUtilRepository.getFileThumbDataByMessageId(messageId)
                ?: return Result.failure()


            val inputStream =
                applicationContext.contentResolver.openInputStream(Uri.parse(fileAbsPath))
                    ?: return Result.failure()


            updateUploadState(messageId, FileUploadState.Started)


            val encryptedFile = try {

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
                        it

                    } else {
                        it
                    }

                }

            } catch (e: Exception) {

                deleteCachedFilePath(messageId, cachedFilePath)
                updateUploadState(
                    messageId,
                    FileUploadState.Retry(e.message ?: "Exception: Caused by file encryption")
                )
                e.printStackTrace()
                return Result.failure()
            }


            val encryptedThumbnailBytes = try {
                uploadWorkerUtilRepository.getEncryptedFileBytes(
                    applicationContext,
                    messageId,
                    ByteArrayInputStream(File(thumbnailData).readBytes()),
                    publicKey,
                    thumbCachePath,
                    ".jpg",
                    "${fileName.substringBeforeLast(".")}_thumbnail",
                    "thumbnail"
                ).let {
                    if (thumbCachePath == null) {
                        thumbCachePath = it.absolutePath
                        it.readBytes()

                    } else {
                        it.readBytes()
                    }
                }

            } catch (e: Exception) {
                deleteThumbCachePath(messageId, thumbCachePath)
                updateUploadState(
                    messageId,
                    FileUploadState.Retry(e.message ?: "Error: Caused by thumbnail encryption")
                )
                e.printStackTrace()
                return Result.failure()
            }


            createUploadNotificationChannel(notificationManager)
            updateNotification(0)


            return sendVisualMedia(
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
                thumbCachePath,
                cachedFilePath,
                encryptedFile,
                encryptedThumbnailBytes
            )


        } catch (e: InvalidKeyVersionException) {
            updateUploadState(messageId, FileUploadState.Retry("SocketAuth: Invalid key version"))
            notificationManager.cancel(notificationId)

            deleteCachedFilesAndUpdateMessage(messageId, cachedFilePath, thumbCachePath)

            uploadWorkerUtilRepository.updateLastChunkIndexByMessageId(messageId, -1)
            uploadWorkerUtilRepository.updateLastSentByteOffsetByMessageId(messageId, -1)
            uploadWorkerUtilRepository.updateLastSentThumbnailChunkIndexByMessageId(messageId, -1)
            uploadWorkerUtilRepository.updateLastSentThumbnailByteOffsetByMessageId(messageId, -1)

            Result.retry()
        } catch (e: UserNotActiveException) {
            uploadWorkerUtilRepository.updateMessageStatus(messageId, ChatMessageStatus.FAILED)
            updateUploadState(messageId, FileUploadState.Failed)
            notificationManager.cancel(notificationId)
            deleteCachedFilesAndUpdateMessage(messageId, cachedFilePath, thumbCachePath)
            uploadWorkerUtilRepository.updateLastChunkIndexByMessageId(messageId, -1)
            uploadWorkerUtilRepository.updateLastSentByteOffsetByMessageId(messageId, -1)
            uploadWorkerUtilRepository.updateLastSentThumbnailChunkIndexByMessageId(messageId, -1)
            uploadWorkerUtilRepository.updateLastSentThumbnailByteOffsetByMessageId(messageId, -1)
            Result.success()
        } catch (e: Exception) {

            when (e) {
                is SocketConnectionException, is  UploadFailedException
                -> {
                    fileId?.let {
                        socket.off("chat:fileTransferCompleted-${it}")
                        socket.off("chat:thumbnailTransferCompleted-${it}")
                    }

                    if (chunkIndex != -1L) {
                        socket.off("chat:mediaChunkAck-${fileId}-$chunkIndex")
                    }

                    if (thumbnailChunkIndex != -1L) {
                        socket.off("chat:mediaThumbnailChunkAck-${fileId}-${thumbnailChunkIndex}")
                    }

                    updateUploadState(messageId, FileUploadState.Retry("${e.message}"))
                    notificationManager.cancel(notificationId)
                    return Result.retry()
                }

                else -> {
                    runBlocking {
                        updateUploadState(
                            messageId,
                            FileUploadState.Retry(e.message ?: "Caused by exception")
                        )
                        notificationManager.cancel(notificationId)  // Dismiss the notification by using the notificationId
                    }
                    return Result.failure()
                }
            }


        } finally {
            finalizeSocket()
        }
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun sendVisualMedia(
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
        thumbCachePath: String?,
        cachedFilePath: String?,
        encryptedFile: File,
        thumbnailByte: ByteArray,
    ): Result {

        var isMessageSent = false
        val encryptedFileLength = encryptedFile.length()
        val totalLengthEncryptedFileAndThumbnail =
            encryptedFile.length() + thumbnailByte.size.toLong()

        val buffer = ByteArray(CHUNK_SIZE)
        var bytesRead: Int


        var lastSentChunkIndex: Long
        var byteOffset: Long
        // Store the file ID associated with the message for resuming transfer
        withContext(Dispatchers.IO) {

            val messageProcessingMediaData =
                uploadWorkerUtilRepository.getMessageProcessingDataByMessageId(messageId)
            val currentFileId = messageProcessingMediaData?.fileId
            fileId = currentFileId
                ?: "${senderId}_${recipientId}_${messageId}_${System.currentTimeMillis()}"
            // Update or create fileId if it's new

            val updatedData = messageProcessingMediaData?.copy(
                fileId = fileId // Update the fileId
            ) ?: MessageProcessingData(
                messageId = messageId,
                fileId = fileId // New entry with the provided fileId
            )

            uploadWorkerUtilRepository.insertOrUpdateMessageProcessingData(updatedData)

            byteOffset = withContext(Dispatchers.IO) {
                uploadWorkerUtilRepository.getLastSentByteOffset(messageId)
            }
            lastSentChunkIndex = (byteOffset + CHUNK_SIZE - 1) / CHUNK_SIZE // Round up total chunks


            /*
                                withContext(Dispatchers.IO) { uploadWorkerUtilRepository.getLastSentChunkIndex(messageId) }
            */

        }


        try {
            val totalChunks =
                (encryptedFileLength.toInt() + CHUNK_SIZE - 1) / CHUNK_SIZE // Round up total chunks

            // Start file transfer metadata
            val startTransferData = JSONObject().apply {
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
                put("file_size", encryptedFileLength)
                put("extension", extension)
                put("content_type", mimeType)
                put("width", width)
                put("height", height)
                put("total_duration", totalDuration)
                put("timestamp", System.currentTimeMillis())
            }


            // Create a CompletableDeferred to wait for the transfer completion event
            val allChunksAckReceived = CompletableDeferred<Unit>()
            val onMessageSentCompleted = CompletableDeferred<Unit>()

            // Register the completion listener
            socket.once("chat:fileTransferCompleted-${fileId}") { args ->

                if (isStopped || onMessageSentCompleted.isCompleted) {
                    return@once
                }

                val result = args[0] as JSONObject
                val status = result.optString("status")

                val originalFileId = fileId

                if (originalFileId == null) {
                    onMessageSentCompleted.completeExceptionally(Exception("File id can not be null"))
                    return@once
                }

                val sendThumbnailAction: suspend () -> Unit = {

                    sendMediaThumbnail(
                        onMessageSentCompleted,
                        startTransferData,
                        totalLengthEncryptedFileAndThumbnail,
                        senderId,
                        recipientId,
                        messageId,
                        replyId,
                        content,
                        mimeType,
                        category,
                        originalFileId,
                        fileName,
                        extension,
                        width,
                        height,
                        totalDuration,
                        thumbCachePath,
                        cachedFilePath,
                        thumbnailByte,
                        keyVersion,
                        {
                            onMessageSentCompleted.complete(Unit)
                        }, {
                            onMessageSentCompleted.completeExceptionally(
                                SocketConnectionException(it)
                            )
                        }, {
                            isMessageSent = true
                        })
                }

                CoroutineScope(Dispatchers.IO).launch {

                    if (status == "FILE_TRANSFER_COMPLETED") {

                        if (!allChunksAckReceived.isCompleted) {
                            allChunksAckReceived.await()
                        }
                    }

                    if (status == "FILE_TRANSFER_COMPLETED" || status == "ALL_CHUNKS_RECEIVED_FILE_TRANSFER_COMPLETED") {
                        CoroutineScope(Dispatchers.IO).launch {
                            sendThumbnailAction()
                        }
                    }
                }


            }

            // A function to handle chunk acknowledgment and progress
            fun onChunkAcknowledged(chunkIndex: Int) {
                if (chunkIndex == totalChunks - 1) {
                    CoroutineScope(Dispatchers.IO).launch {
                        uploadWorkerUtilRepository.updateLastChunkIndexByMessageId(
                            messageId,
                            -1
                        )
//                                        updateLastSentByteOffsetByMessageId(messageId, -1)
                        // Complete the deferred task once the last chunk is acknowledged
                        allChunksAckReceived.complete(Unit)
                    }
                }
            }


            // Emit the event to start file transfer
            socket.emit("chat:startFileTransfer", startTransferData, Ack {


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

                        onMessageSentCompleted.completeExceptionally(
                            InvalidKeyVersionException(
                                "Invalid key version"
                            )
                        )


                    } else if (status != null && status == "USER_NOT_ACTIVE_ERROR") {
                        onMessageSentCompleted.completeExceptionally(UserNotActiveException("User not active"))
                    } else {

                        // Initialize file transfer from the last sent chunk (resume)
                        FileInputStream(encryptedFile).use { fis ->

                            chunkIndex = lastSentChunkIndex

                            var lastSentByteOffset = byteOffset



                            fis.skip(lastSentByteOffset)


                            // Start sending chunks
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


                                        // Wait for the chunk acknowledgment asynchronously
                                        socket.once("chat:mediaChunkAck-${fileId}-$chunkIndex") { args ->

                                            if (isStopped || onMessageSentCompleted.isCompleted) {
                                                return@once
                                            }


                                            val data = args[0] as? JSONObject
                                            data?.let {
                                                CoroutineScope(Dispatchers.IO).launch {

                                                    val updatedChunkIndex = it.getInt("chunkIndex")
                                                    val updatedSize = it.getLong("updatedSize")

                                                    val progress =
                                                        ((updatedSize.toFloat() / totalLengthEncryptedFileAndThumbnail) * 100).toInt()
                                                    updateNotification(progress)

                                                    updateUploadState(
                                                        messageId,
                                                        FileUploadState.InProgress(progress)
                                                    )
                                                    uploadWorkerUtilRepository.updateLastChunkIndexByMessageId(
                                                        messageId,
                                                        updatedChunkIndex
                                                    )

                                                    lastSentByteOffset = updatedSize // Increment by the size of the chunk received

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
                                        }, Ack {

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


                                        // Suspend the current loop until chunk acknowledgment is received
                                        chunkAcknowledged.await()
                                        chunkIndex++

                                    } else {
                                        onMessageSentCompleted.completeExceptionally(
                                            SocketConnectionException("Thumbnail Transfer: Socket is not connected")
                                        )
                                    }
                                }
                            }

                            // Complete the transfer once the last chunk acknowledgment is received

                        }
                    }


                }

            })


            // Wait for the final chunk acknowledgment
            while (!onMessageSentCompleted.isCompleted && !isMessageSent) {
                if (!socket.connected()) {
                    onMessageSentCompleted.completeExceptionally(SocketConnectionException("Waiting visual media transfer: Socket is not connected"))
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
            return Result.success()

        } catch (e: SocketConnectionException) {
            // Handle socket errors
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


    private suspend fun sendMediaThumbnail(
        isJobCompletableDeferred: CompletableDeferred<Unit>,
        originalFileTransferData: JSONObject,
        totalLengthEncryptedFileAndThumbnail: Long,
        senderId: Long,
        recipientId: Long,
        messageId: Long,
        replyId: Long,
        content: String,
        contentType: String,
        category: String,
        fileId: String,
        fileName: String,
        extension: String,
        width: Int,
        height: Int,
        totalDuration: Long,
        thumbCachePath: String?,
        cachedFilePath: String?,
        thumbnail: ByteArray,
        keyVersion: Long,
        onMessageSentCompleted: () -> Unit,
        onMessageSentFailed: (String) -> Unit,
        onMessageSent: () -> Unit,
    ) {


        // Start sending the chunks from the last sent chunk index
        val buffer = ByteArray(THUMBNAIL_CHUNK_SIZE) // Buffer to read chunks into


        val totalThumbnailChunks =
            (thumbnail.size + THUMBNAIL_CHUNK_SIZE - 1) / THUMBNAIL_CHUNK_SIZE // Calculate total chunks

        val byteOffset = withContext(Dispatchers.IO) {
            uploadWorkerUtilRepository.getLastSentThumbnailByteOffsetByMessageId(messageId)
        }
        // Load the last successfully sent chunk index from a persistent storage or variable (e.g., SharedPreferences, file)
        val lastSentThumbnailChunkIndex: Long =
            (byteOffset + THUMBNAIL_CHUNK_SIZE - 1) / THUMBNAIL_CHUNK_SIZE // Round up total chunks

        /*   withContext(Dispatchers.IO) {
               lastSentThumbnailChunkIndex =  uploadWorkerUtilRepository.getLastSentThumbnailChunkIndexByMessageId(messageId)
           } //
        */


        val startThumbnailTransferData = JSONObject().apply {
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
            put("total_chunks", totalThumbnailChunks)
            put("byte_offset", byteOffset)
            put("file_size", thumbnail.size)
            put("extension", extension)
            put("content_type", contentType)
            put("width", width)
            put("height", height)
            put("total_duration", totalDuration)
            put("timestamp", System.currentTimeMillis())
        }


        val allThumbnailChunksSentCompletedDeferred = CompletableDeferred<Unit>()
        val onMessageSentCompletedDeferred = CompletableDeferred<Unit>()


        // Register the completion listener
        socket.once("chat:thumbnailTransferCompleted-${fileId}") { args ->

            if (isStopped || isJobCompletableDeferred.isCompleted) {
                return@once
            }


            // Wait until the final chunk acknowledgment is complete
            CoroutineScope(Dispatchers.IO).launch {


                val result = args[0] as? JSONObject?

                val status = result?.optString("status")


                if (status == "THUMBNAIL_TRANSFER_COMPLETED") {
                    if (!allThumbnailChunksSentCompletedDeferred.isCompleted) {
                        allThumbnailChunksSentCompletedDeferred.await()
                    }
                }

                if (status == "THUMBNAIL_TRANSFER_COMPLETED"
                    || status == "ALL_CHUNKS_RECEIVED_THUMBNAIL_TRANSFER_COMPLETED"
                ) {
                    updateUploadState(messageId, FileUploadState.Completed)
                    updateNotification(100, "Upload complete")
                    notificationManager.cancel(notificationId)

                    delay(1000)

                    if (socket.connected()) {

                        socket.emit("chat:sendVisualMedia", originalFileTransferData)

                        onMessageSent()

                        uploadWorkerUtilRepository.updateLastSentThumbnailChunkIndexByMessageId(
                            messageId,
                            -1
                        )
//                        updateLastSentThumbnailByteOffsetByMessageId(messageId, -1)
                        deleteCachedFilesAndUpdateMessage(
                            messageId,
                            cachedFilePath,
                            thumbCachePath
                        )
                        onMessageSentCompletedDeferred.complete(Unit)


                    } else {
                        onMessageSentFailed("Thumbnail transfer failed: Socket is not connected")
                    }
                }


            }
        }

        // Function to handle chunk acknowledgment and progress
        fun onChunkAcknowledged(chunkIndex: Int) {

            if (chunkIndex == totalThumbnailChunks - 1) {
                CoroutineScope(Dispatchers.IO).launch {
                    allThumbnailChunksSentCompletedDeferred.complete(Unit)
                }
                // Complete the deferred task once the last chunk is acknowledged
            }
        }

        // Emit the event to start file transfer
        socket.emit("chat:startThumbnailTransfer", startThumbnailTransferData, Ack {


            CoroutineScope(Dispatchers.IO).launch {

                if (isStopped || onMessageSentCompletedDeferred.isCompleted) {
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

                    onMessageSentFailed("Invalid key version")


                } else if (status != null && status == "USER_NOT_ACTIVE_ERROR") {
                    onMessageSentFailed("User not active")
                } else {

                    ByteArrayInputStream(thumbnail).use { fis ->
                        thumbnailChunkIndex = lastSentThumbnailChunkIndex
                        var lastSentByteOffset = byteOffset

                        var bytesRead: Int


                        fis.skip(lastSentByteOffset)

                        if (thumbnail.size.minus(byteOffset) == 0L) {
                            updateUploadState(messageId, FileUploadState.Completed)
                            allThumbnailChunksSentCompletedDeferred.complete(Unit)
                            onMessageSentCompletedDeferred.complete(Unit)
                            notificationManager.cancel(notificationId)
                        }

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

                            if (thumbnailChunkIndex >= lastSentThumbnailChunkIndex) {
                                if (socket.connected()) {

                                    val chunkAcknowledged = CompletableDeferred<Unit>()


                                    socket.emit("chat:sendThumbnailChunk", JSONObject().apply {

                                        put("sender_id", senderId)
                                        put("recipient_id", recipientId)
                                        put("message_id", messageId)
                                        put("reply_id", replyId)
                                        put("message", content)
                                        put("type", if (replyId == -1L) "normal" else "reply")
                                        put("category", category)
                                        put("content_type", contentType)
                                        put("file_id", fileId)
                                        put("file_name", fileName)
                                        put("extension", extension)
                                        put("t_width", width)
                                        put("t_height", height)
                                        put("total_duration", totalDuration)

                                        put("total_chunks", totalThumbnailChunks)

                                        put("chunk_index", thumbnailChunkIndex)
                                        put("byte_offset", lastSentByteOffset)
                                        put("data", chunk)
                                        put("chunk_size", THUMBNAIL_CHUNK_SIZE)

                                        put("key_version", keyVersion)

                                    }, Ack {
                                        val failedReason = it[0] as String

                                        val sendFileChunkResponse = it[1] as JSONObject
                                        val sendFileChunkResponseStatus =
                                            sendFileChunkResponse.getString("status")

                                        if (sendFileChunkResponseStatus == "MEDIA_TRANSFER_NOT_FOUND") {
                                            onMessageSentFailed(failedReason)
                                            if (!chunkAcknowledged.isCompleted) {
                                                chunkAcknowledged.complete(Unit)
                                            }
                                        }
                                    })



                                    // Wait for acknowledgment of the chunk
                                    socket.once("chat:mediaThumbnailChunkAck-${fileId}-${thumbnailChunkIndex}") { args ->

                                        if (isStopped || isJobCompletableDeferred.isCompleted) {
                                            return@once
                                        }

                                        val data = args[0] as? JSONObject

                                        data?.let {

                                            val updatedChunkIndex: Int = data.getInt("chunkIndex")
                                            val updatedSize: Long = data.getLong("updatedSize")

                                            // Update chunk index and the last sent chunk
                                            CoroutineScope(Dispatchers.IO).launch {

                                                val progress =
                                                    ((updatedSize.toFloat() / totalLengthEncryptedFileAndThumbnail) * 100).toInt()


                                                updateUploadState(
                                                    messageId,
                                                    FileUploadState.InProgress(progress)
                                                )
                                                uploadWorkerUtilRepository.updateLastSentThumbnailChunkIndexByMessageId(
                                                    messageId,
                                                    updatedChunkIndex
                                                )
                                                lastSentByteOffset = updatedSize
                                                uploadWorkerUtilRepository.updateLastSentThumbnailByteOffsetByMessageId(
                                                    messageId,
                                                    lastSentByteOffset
                                                )
                                                // Acknowledge the chunk
                                                onChunkAcknowledged(updatedChunkIndex)
                                                chunkAcknowledged.complete(Unit)
                                            }


                                        }

                                    }

                                    chunkAcknowledged.await()
                                    thumbnailChunkIndex++ // Move to the next chunk

                                } else {
                                    onMessageSentFailed("Thumbnail transfer failed: Socket is not connected")
                                }
                            }

                        }


                    }

                }


            }

        })



        if (!onMessageSentCompletedDeferred.isCompleted) {
            onMessageSentCompletedDeferred.await()
        }

        onMessageSentCompleted()

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


    private suspend fun updateUploadState(messageId: Long, state: FileUploadState) {
        try {
            if (!isStopped) {
                withContext(Dispatchers.IO) {
                    setProgress(
                        workDataOf(
                            "messageId" to messageId,
                            "state" to serializeFileUploadState(state)
                        )
                    )
                }
            }
        } catch (_: IllegalStateException) {
        }
    }


    private suspend fun deleteCachedFilesAndUpdateMessage(
        messageId: Long,
        cachedFilePath: String?,
        thumbCachePath: String?
    ) {

        deleteCachedFilePath(messageId, cachedFilePath)
        deleteThumbCachePath(messageId, thumbCachePath)

    }


    private suspend fun deleteCachedFilePath(messageId: Long, cachedFilePath: String?) {
        cachedFilePath?.let {
            File(it).apply {
                if (exists()) {
                    delete()
                }
            }
            uploadWorkerUtilRepository.updateMessageFileCachePath(messageId, null)
        }

    }

    private suspend fun deleteThumbCachePath(messageId: Long, thumbCachePath: String?) {
        thumbCachePath?.let {
            File(it).apply {
                if (exists()) {
                    delete()
                }
            }
            uploadWorkerUtilRepository.updateMessageFileThumbPath(messageId, null)
        }

    }

    private fun finalizeSocket() {
        if (socketManager.isBackgroundSocket) {
            socketManager.destroySocket()
            if (App.isAppInForeground) {
                socketManager.getSocket()
            }
        }
    }
}

