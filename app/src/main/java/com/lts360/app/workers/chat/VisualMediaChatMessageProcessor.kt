package com.lts360.app.workers.chat

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lts360.App
import com.lts360.api.app.AppClient
import com.lts360.app.database.models.chat.ChatMessageStatus
import com.lts360.app.database.models.chat.ChatMessageType
import com.lts360.app.database.daos.chat.MessageDao
import com.lts360.app.database.daos.chat.MessageMediaMetaDataDao
import com.lts360.app.database.models.chat.MessageMediaMetadata
import com.lts360.app.database.daos.profile.UserProfileDao

import com.lts360.api.auth.managers.socket.SocketManager
import com.lts360.api.auth.managers.socket.SocketConnectionException
import com.lts360.api.auth.services.CommonService
import com.lts360.app.database.daos.chat.ChatUserDao
import com.lts360.app.database.daos.notification.NotificationDao
import com.lts360.app.database.models.chat.Message
import com.lts360.app.notifications.buildAndShowChatNotification
import com.lts360.app.workers.chat.utils.awaitConnectToSocket
import com.lts360.app.workers.chat.utils.cacheThumbnailToAppSpecificFolder
import com.lts360.app.workers.chat.utils.getFolderTypeByExtension
import com.lts360.components.utils.LogUtils
import com.lts360.components.utils.errorLogger
import com.lts360.compose.ui.auth.repos.DecryptionFileStatus
import com.lts360.compose.ui.auth.repos.decryptFile
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.socket.client.Ack
import io.socket.client.Socket
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer


@HiltWorker
class VisualMediaChatMessageProcessor @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted workerParams: WorkerParameters,
    val notificationDao: NotificationDao,
    val chatUserDao: ChatUserDao,
    val messageDao: MessageDao,
    val fileMetaDataDao: MessageMediaMetaDataDao,
    val userProfileDao: UserProfileDao,
    val socketManager: SocketManager

) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "SUPER6_APP_IMAGE_MESSAGE_PROCESSOR"
    }

    private lateinit var socket: Socket

    override suspend fun doWork(): Result {

        val chatId = inputData.getInt("chatId", -1)
        val senderId = inputData.getLong("senderId", -1)
        val recipientId = inputData.getLong("chatRecipientId", -1)
        val replyId = inputData.getLong("replyId", -1)
        val senderMessageId = inputData.getLong("senderMessageId", -1)

        val content = inputData.getString("content")

        val downloadUrl = inputData.getString("downloadUrl")
        val thumbDownloadUrl = inputData.getString("thumbDownloadUrl")

        val originalFileName = inputData.getString("originalFileName")
        val fileSize = inputData.getLong("fileSize", -1)
        val contentType = inputData.getString("contentType")
        val fileExtension = inputData.getString("extension")
        val width = inputData.getInt("width", -1)
        val height = inputData.getInt("height", -1)
        val totalDuration = inputData.getLong("totalDuration", -1L)

        if (downloadUrl == null || contentType == null || fileExtension == null ||
            originalFileName == null || thumbDownloadUrl == null || (contentType.startsWith("video/") && (totalDuration == -1L))
        ) {
            return Result.failure()
        }

        return try {

            socket = awaitConnectToSocket(socketManager, !App.isAppInForeground)

            val responseBody = AppClient.mediaDownloadInstance.create(CommonService::class.java)
                .downloadMedia(thumbDownloadUrl)

            val dir = context.getExternalFilesDir(null)

            val fileCategoryAndTypeByExtension = getFolderTypeByExtension(fileExtension)

            val directory = File(dir, fileCategoryAndTypeByExtension.first)

            if (!directory.exists()) {
                directory.mkdirs()
            }

            val destinationFile = File(directory, "$originalFileName.enc")

            var readLength: Int

            val buffer = ByteArray(4 * 1024)
            val bis = BufferedInputStream(responseBody.byteStream(), 4 * 1024)

            var totalBytesDownloaded: Long = 0

            val fileOutputStream = withContext(Dispatchers.IO) {
                FileOutputStream(destinationFile, false)
            }
            val fileChannel = fileOutputStream.channel
            val bufferByteBuffer = ByteBuffer.allocate(4 * 1024)

            withContext(Dispatchers.IO) {
                while (bis.read(buffer).also { readLength = it } != -1) {

                    if (!isActive) {
                        cancel()
                        return@withContext
                    }

                    totalBytesDownloaded += readLength.toLong()
                    bufferByteBuffer.clear()
                    bufferByteBuffer.put(buffer, 0, readLength)
                    bufferByteBuffer.flip()
                    fileChannel.write(bufferByteBuffer)

                }

                bis.close()
                fileOutputStream.close()
            }

            var mediaFile: File? = null

            withTimeout(5000) {
                suspendCancellableCoroutine<Unit> { continuation ->
                    socket.emit("chat:mediaStatus", JSONObject().apply {
                        put("status", "MEDIA_DOWNLOADED")
                        put("download_url", thumbDownloadUrl)
                        put("sender_id", senderId)
                        put("recipient_id", recipientId)
                        put("message_id", senderMessageId)
                    }, Ack {

                        mediaFile = cacheThumbnailToAppSpecificFolder(
                            context,
                            originalFileName,
                            if (contentType.startsWith("video/")) ".jpg" else fileExtension,
                            fileExtension
                        )

                        continuation.resume(Unit) { cause, value, context ->
                        }
                    })
                }
            }


            mediaFile?.let {
                when (val decryptionFileStatus = decryptFile(destinationFile, it)) {
                    is DecryptionFileStatus.DecryptionFailed -> {
                        destinationFile.delete()
                        it.delete()

                        messageDao.insertMessageAndMetadata(
                            Message(
                                chatId = chatId,
                                senderId = senderId,
                                recipientId = recipientId,
                                content = content ?: "",
                                timestamp = System.currentTimeMillis(),
                                senderMessageId = senderMessageId,
                                replyId = replyId,
                                status = ChatMessageStatus.FAILED_TO_DISPLAY_REASON_DECRYPTION_FAILED,
                                type =

                                    if (contentType.startsWith("image/")) {
                                        if (fileExtension == ".gif") {
                                            ChatMessageType.GIF
                                        } else {
                                            ChatMessageType.IMAGE
                                        }

                                    } else {
                                        ChatMessageType.VIDEO
                                    }
                            ),

                            fileMetaDataDao,
                            MessageMediaMetadata(
                                messageId = -1,
                                fileDownloadUrl = downloadUrl,
                                fileThumbPath = null,
                                thumbData = null,
                                fileSize = fileSize,
                                fileMimeType = contentType,
                                fileExtension = fileExtension,
                                originalFileName = originalFileName,
                                width = width,
                                height = height,
                                totalDuration = totalDuration
                            )
                        )

                    }


                    is DecryptionFileStatus.UnknownError -> {
                        destinationFile.delete()
                        it.delete()

                        messageDao.insertMessageAndMetadata(
                            Message(
                                chatId = chatId,
                                senderId = senderId,
                                recipientId = recipientId,
                                content = content ?: "",
                                timestamp = System.currentTimeMillis(),
                                senderMessageId = senderMessageId,
                                replyId = replyId,
                                status = ChatMessageStatus.FAILED_TO_DISPLAY_REASON_UNKNOWN,
                                type = if (contentType.startsWith("image/")) {
                                    if (fileExtension == ".gif") {
                                        ChatMessageType.GIF
                                    } else {
                                        ChatMessageType.IMAGE
                                    }
                                } else {
                                    ChatMessageType.VIDEO
                                },

                                ),
                            fileMetaDataDao,
                            MessageMediaMetadata(
                                messageId = -1,
                                fileDownloadUrl = downloadUrl,
                                fileThumbPath = null,
                                thumbData = null,
                                fileSize = fileSize,
                                fileMimeType = contentType,
                                fileExtension = fileExtension,
                                originalFileName = originalFileName,
                                width = width,
                                height = height,
                                totalDuration = totalDuration
                            )
                        )
                    }


                    is DecryptionFileStatus.Success -> {

                        destinationFile.delete()

                        val outputFile = decryptionFileStatus.decryptedFile

                        messageDao.insertMessageAndMetadata(
                            Message(
                                chatId = chatId,
                                senderId = senderId,
                                recipientId = recipientId,
                                content = content ?: "",
                                timestamp = System.currentTimeMillis(),
                                senderMessageId = senderMessageId,
                                replyId = replyId,
                                status = ChatMessageStatus.SENDING,
                                type = if (contentType.startsWith("image/")) {
                                    if (fileExtension == ".gif") {
                                        ChatMessageType.GIF
                                    } else {
                                        ChatMessageType.IMAGE
                                    }
                                } else {
                                    ChatMessageType.VIDEO
                                }

                            ),

                            fileMetaDataDao,
                            MessageMediaMetadata(
                                messageId = -1,
                                fileDownloadUrl = downloadUrl,
                                fileThumbPath = outputFile.absolutePath,
                                thumbData = outputFile.absolutePath,
                                fileSize = fileSize,
                                fileMimeType = contentType,
                                fileExtension = fileExtension,
                                originalFileName = originalFileName,
                                width = width,
                                height = height,
                                totalDuration = totalDuration
                            )
                        )


                    }
                }
            } ?: also {
                return Result.failure()
            }



            if (socket.connected()) {

                val timeoutDuration = 20000L

                val acknowledgmentReceived = CompletableDeferred<Unit>()

                socket.emit("chat:acknowledgment", JSONObject().apply {
                    put("status", "delivered")
                    put("sender_id", senderId)
                    put("recipient_id", recipientId)
                    put("message_id", senderMessageId)
                }, Ack {
                    acknowledgmentReceived.complete(Unit)
                    Log.e(LogUtils.TAG, "Others media message acknowledgment received by client")
                })

                try {
                    withTimeout(timeoutDuration) {
                        if (!acknowledgmentReceived.isCompleted) {
                            acknowledgmentReceived.await()
                        }
                    }
                } catch (_: TimeoutCancellationException) {
                    Log.e(
                        LogUtils.TAG,
                        "Acknowledgment not received within $timeoutDuration milliseconds"
                    )
                    return Result.retry()
                }
            } else {
                throw SocketConnectionException("Socket connect error")
            }

            val (chatUser, lastUnreadMessages, unreadMessageCount) = withContext(Dispatchers.IO) {
                val user = chatUserDao.getChatUserByRecipientId(senderId)
                val messages = messageDao.getLastSixUnreadMessage(senderId, chatId).reversed()
                val count = messageDao.countAllUnreadMessages()
                Triple(user, messages, count)
            }

            chatUser?.userProfile
                ?.let {
                    if (!App.isAppInForeground) {
                        buildAndShowChatNotification(
                            context,
                            senderId,
                            "${it.firstName} ${it.lastName ?: ""}",
                            lastUnreadMessages,
                            it.profilePicUrl,
                            unreadMessageCount
                        )
                    }
                }

            finalizeSocket()
            Result.success()
        } catch (_: SocketConnectionException) {
            Log.e(TAG, "Socket connection failed: Retrying...")
            finalizeSocket()
            Result.retry()
        } catch (e: Exception) {
            e.printStackTrace()
            finalizeSocket()
            Log.e(TAG, "Error processing message: ${e.message}")
            Result.failure()
        }


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

