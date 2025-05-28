package com.lts360.app.workers.chat

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lts360.App
import com.lts360.api.auth.managers.socket.SocketConnectionException
import com.lts360.api.auth.managers.socket.SocketManager
import com.lts360.app.database.daos.chat.ChatUserDao
import com.lts360.app.database.daos.chat.MessageDao
import com.lts360.app.database.daos.chat.MessageMediaMetaDataDao
import com.lts360.app.database.daos.notification.NotificationDao
import com.lts360.app.database.models.chat.ChatMessageStatus
import com.lts360.app.database.models.chat.ChatMessageType
import com.lts360.app.database.models.chat.Message
import com.lts360.app.database.models.chat.MessageMediaMetadata
import com.lts360.app.notifications.buildAndShowChatNotification
import com.lts360.app.workers.chat.download.downloadMediaAndCache
import com.lts360.app.workers.chat.utils.awaitConnectToSocket
import com.lts360.app.workers.chat.utils.cacheThumbnailToAppSpecificFolder
import com.lts360.app.workers.helpers.ChatMessageHandlerWorkerHelper
import com.lts360.components.utils.errorLogger
import com.lts360.compose.ui.auth.repos.DecryptionFileStatus
import com.lts360.compose.ui.auth.repos.DecryptionStatus
import com.lts360.compose.ui.auth.repos.decryptFile
import com.lts360.compose.ui.auth.repos.decryptMessage
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.socket.client.Ack
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import kotlin.coroutines.resumeWithException

@HiltWorker
class OfflineChatMessagesProcessor @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted workerParams: WorkerParameters,
    val socketManager: SocketManager,
    val notificationDao: NotificationDao,
    val chatUserDao: ChatUserDao,
    val messageDao: MessageDao,
    val fileMetadataDao: MessageMediaMetaDataDao
) : CoroutineWorker(context, workerParams) {


    private lateinit var socket: Socket

    companion object {

        private const val TAG = "SUPER6_APP_OFFLINE_MESSAGES_PROCESSOR"
    }

    override suspend fun doWork(): Result {

        val userId = inputData.getLong("user_id", -1)
        val data = inputData.getString("data")
        val type = inputData.getString("type")

        if (userId == -1L || type == null || data == null) {
            Log.e(TAG, "Missing required input data")
            return Result.failure()
        }

        return try {

            val result = when (val result = processMessage(data, type, userId)) {
                Result.success() -> {
                    result
                }
                Result.failure() -> {
                    result
                }
                else -> result
            }

            finalizeSocket()
            return result
        } catch (_: SocketConnectionException) {
            Log.e(TAG, "Socket connection failed. Retrying...")
            finalizeSocket()
            Result.retry()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "Error processing message: ${e.message}")
            finalizeSocket()
            Result.failure()
        }
    }


    private suspend fun processMessage(
        data: String?,
        type: String?,
        userId: Long,
    ): Result {
        return when {
            data != null && type == "new_chat_message" -> handleChatMessage(data, userId)
            else -> Result.failure()
        }
    }

    private suspend fun handleChatMessage(data: String, userId: Long): Result {

        val fcmPayloadData = JSONObject(data)
        errorLogger(fcmPayloadData.toString())
        val fcmSenderId = fcmPayloadData.getLong("sender_id")
        val fcmMessageId = fcmPayloadData.getLong("message_id")

        val chatUser = chatUserDao.getChatUserByRecipientId(fcmSenderId)

        if (chatUser != null) {

            socket = awaitConnectToSocket(socketManager, !App.isAppInForeground)

            val messagesJsonArray = suspendCancellableCoroutine { cont ->

                socket.once("chat:offlineMessages", Emitter.Listener { args ->
                    if (args.isNotEmpty()) {
                        try {
                            val data = args[0] as JSONObject
                            val messagesJsonArray = data.getJSONArray("offline_messages")

                            cont.resume(messagesJsonArray, { cause, _, _ -> })
                        } catch (e: Exception) {
                            cont.resumeWithException(e)
                        }
                    } else {
                        cont.resume(JSONArray(), { cause, _, _ -> })
                    }
                })

                socket.emit("chat:requestOfflineMessages", JSONObject().apply {
                    put("userId", userId)
                    put("senderId", fcmSenderId)
                })
            }

            val messagesList = List(messagesJsonArray.length()) { i ->
                messagesJsonArray.getJSONObject(i)
            }


            messagesList.forEach { messageData ->


                val senderId = messageData.getLong("sender_id")
                val messageId = messageData.getLong("message_id")

                if (socket.connected()) {

                    try {
                           withTimeout(20_000L) {
                            suspendCancellableCoroutine<Unit>{ cont ->


                                socket.emit("chat:offlineMessageAcknowledgment",
                                    JSONObject().apply {
                                        put("status", "delivered")
                                        put("sender_id", senderId)
                                        put("recipient_id", userId)
                                        put("message_id", messageId)
                                    },
                                    Ack {
                                        Log.e(TAG, "Acknowledgment received by client")
                                        cont.resume(Unit, { cause, _, _ -> })
                                    }
                                )
                            }
                        }
                    } catch (_: TimeoutCancellationException) {
                        Log.e(TAG, "Acknowledgment not received in 20000 ms")
                        return Result.failure()
                    }
                } else {
                    throw SocketConnectionException("Socket connect error")
                }

                val encryptedMessage = messageData.getString("message_body")
                val replyId = messageData.getLong("reply_id")
                val category = messageData.getString("category")

                if (category.contains("image") || category.contains("video") || category.contains("gif")) {

                    val fileMetadata = messageData.getJSONObject("file_metadata")
                    val originalFileName = fileMetadata.getString("original_file_name")
                    val downloadUrl = fileMetadata.getString("download_url")
                    val thumbDownloadUrl = fileMetadata.getString("thumb_download_url")
                    val fileSize = fileMetadata.getLong("file_size")
                    val contentType = fileMetadata.getString("content_type")
                    val extension = fileMetadata.getString("extension")
                    val width = fileMetadata.getInt("width")
                    val height = fileMetadata.getInt("height")
                    val totalDuration = fileMetadata.getLong("total_duration")

                    val cachedFile =
                        downloadMediaAndCache(
                            context,
                            thumbDownloadUrl,
                            originalFileName,
                            extension
                        )

                    if (socket.connected()) {


                        try {

                            var mediaFile: File? = null

                            withTimeout(5000) { // Set timeout to 5 seconds
                                suspendCancellableCoroutine<Unit> { continuation ->
                                    socket.emit("chat:mediaStatus", JSONObject().apply {
                                        put("status", "MEDIA_DOWNLOADED")
                                        put("download_url", thumbDownloadUrl)
                                        put("sender", senderId)
                                        put("recipient_id", userId)
                                        put(
                                            "message_id",
                                            messageId
                                        ) // Add the inserted message ID to JSON
                                    }, Ack {

                                        // Handle decryption after download
                                        mediaFile = cacheThumbnailToAppSpecificFolder(
                                            context,
                                            originalFileName,
                                            if (contentType.startsWith("video/")) ".jpg" else extension,
                                            extension
                                        )

                                        continuation.resume(Unit) { cause, value, context ->
                                            // Acknowledgment received, log it
                                        }
                                    })
                                }
                            }


                            mediaFile?.let {
                                when (val decryptionFileStatus = decryptFile(cachedFile, it)) {

                                    is DecryptionFileStatus.DecryptionFailed -> {
                                        cachedFile.delete()
                                        it.delete()

                                        messageDao.insertMessageAndMetadata(
                                            Message(
                                                chatId = chatUser.chatId,
                                                senderId = senderId,
                                                recipientId = userId,
                                                content = encryptedMessage,
                                                timestamp = System.currentTimeMillis(),
                                                senderMessageId = messageId,
                                                replyId = replyId,
                                                status = ChatMessageStatus.SENDING,
                                                type = if (contentType.startsWith("image/")) {
                                                    ChatMessageType.IMAGE
                                                } else {
                                                    ChatMessageType.VIDEO
                                                }
                                            ),
                                            fileMetadataDao,
                                            MessageMediaMetadata(
                                                messageId = -1,
                                                fileSize = fileSize,
                                                fileMimeType = contentType,
                                                fileExtension = extension,
                                                originalFileName = originalFileName,
                                                width = width,
                                                height = height,
                                                totalDuration = totalDuration,
                                                fileDownloadUrl = downloadUrl,
                                                fileThumbPath = null,
                                                thumbData = null,
                                            )
                                        )
                                    }

                                    is DecryptionFileStatus.UnknownError -> {
                                        cachedFile.delete()
                                        it.delete()

                                        messageDao.insertMessageAndMetadata(
                                            Message(
                                                chatId = chatUser.chatId,
                                                senderId = senderId,
                                                recipientId = userId,
                                                content = encryptedMessage,
                                                timestamp = System.currentTimeMillis(),
                                                senderMessageId = messageId,
                                                replyId = replyId,
                                                status = ChatMessageStatus.SENDING,
                                                type = if (contentType.startsWith("image/")) {
                                                    ChatMessageType.IMAGE
                                                } else {
                                                    ChatMessageType.VIDEO
                                                },

                                                ),
                                            fileMetadataDao,
                                            MessageMediaMetadata(
                                                messageId = -1,
                                                fileSize = fileSize,
                                                fileMimeType = contentType,
                                                fileExtension = extension,
                                                originalFileName = originalFileName,
                                                width = width,
                                                height = height,
                                                totalDuration = totalDuration,
                                                fileDownloadUrl = downloadUrl,
                                                fileThumbPath = null,
                                                thumbData = null,
                                            )
                                        )
                                    }

                                    is DecryptionFileStatus.Success -> {

                                        cachedFile.delete()

                                        val outputFile = decryptionFileStatus.decryptedFile

                                        messageDao.insertMessageAndMetadata(
                                            Message(
                                                chatId = chatUser.chatId,
                                                senderId = senderId,
                                                recipientId = userId,
                                                content = encryptedMessage,
                                                timestamp = System.currentTimeMillis(),
                                                senderMessageId = messageId,
                                                replyId = replyId,
                                                status = ChatMessageStatus.SENDING,
                                                type = if (contentType.startsWith("image/")) {
                                                    ChatMessageType.IMAGE
                                                } else {
                                                    ChatMessageType.VIDEO
                                                }
                                            ),
                                            fileMetadataDao,
                                            MessageMediaMetadata(
                                                messageId = -1,
                                                fileSize = fileSize,
                                                fileMimeType = contentType,
                                                fileExtension = extension,
                                                originalFileName = originalFileName,
                                                width = width,
                                                height = height,
                                                totalDuration = totalDuration,
                                                fileDownloadUrl = downloadUrl,
                                                fileThumbPath = outputFile.absolutePath,
                                                thumbData = outputFile.absolutePath,
                                            )
                                        )

                                    }
                                }
                                Result.failure()
                            } ?: also {
                                return Result.failure()
                            }


                        } catch (t: TimeoutCancellationException) {

                            return Result.retry()
                        } catch (e: Exception) {
                            // Handle any unexpected errors
                            return Result.failure()
                        }

                    } else {
                        throw SocketConnectionException("Socket connect error")
                    }


                } else if (category.contains("audio")) {

                    val fileMetadata = messageData.getJSONObject("file_meta_data")
                    val originalFileName = fileMetadata.getString("original_file_name")
                    val downloadUrl = fileMetadata.getString("download_url")
                    val fileSize = fileMetadata.getLong("file_size")
                    val contentType = fileMetadata.getString("content_type")
                    val extension = fileMetadata.getString("extension")
                    val totalDuration = fileMetadata.getLong("total_duration")


                    messageDao.insertMessageAndMetadata(
                        Message(
                            chatId = chatUser.chatId,
                            senderId = senderId,
                            recipientId = userId,
                            content = encryptedMessage,
                            timestamp = System.currentTimeMillis(),
                            senderMessageId = messageId,
                            replyId = replyId,
                            status = ChatMessageStatus.SENDING,
                            type = ChatMessageType.AUDIO,
                        ),
                        fileMetadataDao,
                        MessageMediaMetadata(
                            messageId = -1,
                            fileSize = fileSize,
                            fileMimeType = contentType,
                            fileExtension = extension,
                            originalFileName = originalFileName,
                            totalDuration = totalDuration,
                            fileDownloadUrl = downloadUrl,
                        )
                    )


                } else if (category.contains("file")) {


                    val fileMetadata = messageData.getJSONObject("file_meta_data")
                    val originalFileName = fileMetadata.getString("original_file_name")
                    val downloadUrl = fileMetadata.getString("download_url")
                    val fileSize = fileMetadata.getLong("file_size")
                    val contentType = fileMetadata.getString("content_type")
                    val extension = fileMetadata.getString("extension")

                    messageDao.insertMessageAndMetadata(
                        Message(
                            chatId = chatUser.chatId,
                            senderId = senderId,
                            recipientId = userId,
                            content = encryptedMessage,
                            timestamp = System.currentTimeMillis(),
                            senderMessageId = messageId,
                            replyId = replyId,
                            status = ChatMessageStatus.SENDING,
                            type = ChatMessageType.FILE,
                        ),
                        fileMetadataDao,
                        MessageMediaMetadata(
                            messageId = -1,
                            fileSize = fileSize,
                            fileMimeType = contentType,
                            fileExtension = extension,
                            originalFileName = originalFileName,
                            fileDownloadUrl = downloadUrl
                        )
                    )


                } else if (category.contains("others")) {

                    val fileMetadata = messageData.getJSONObject("file_meta_data")
                    val originalFileName = fileMetadata.getString("original_file_name")
                    val downloadUrl = fileMetadata.getString("download_url")
                    val fileSize = fileMetadata.getLong("file_size")
                    val contentType = fileMetadata.getString("content_type")
                    val extension = fileMetadata.getString("extension")

                    messageDao.insertMessageAndMetadata(
                        Message(
                            chatId = chatUser.chatId,
                            senderId = senderId,
                            recipientId = userId,
                            content = encryptedMessage,
                            timestamp = System.currentTimeMillis(),
                            senderMessageId = messageId,
                            replyId = replyId,
                            status = ChatMessageStatus.SENDING,
                            type = ChatMessageType.FILE
                        ),
                        fileMetadataDao,
                        MessageMediaMetadata(
                            messageId = -1,
                            fileSize = fileSize,
                            fileMimeType = contentType,
                            fileExtension = extension,
                            originalFileName = originalFileName,
                            fileDownloadUrl = downloadUrl
                        )
                    )

                } else {
                    when (val decryptionStatus = decryptMessage(encryptedMessage)) {

                        is DecryptionStatus.DecryptionFailed -> {
                            withContext(Dispatchers.IO) {
                                messageDao.insertMessage(
                                    Message(
                                        chatId = chatUser.chatId,
                                        senderId = senderId,
                                        recipientId = userId,
                                        content = "",
                                        timestamp = System.currentTimeMillis(),
                                        senderMessageId = messageId,
                                        replyId = replyId,
                                        status = ChatMessageStatus.FAILED_TO_DISPLAY_REASON_DECRYPTION_FAILED
                                    )
                                )
                            }
                        }

                        is DecryptionStatus.UnknownError -> {
                            withContext(Dispatchers.IO) {
                                messageDao.insertMessage(
                                    Message(
                                        chatId = chatUser.chatId,
                                        senderId = senderId,
                                        recipientId = userId,
                                        content = "",
                                        timestamp = System.currentTimeMillis(),
                                        senderMessageId = messageId,
                                        replyId = replyId,
                                        status = ChatMessageStatus.FAILED_TO_DISPLAY_REASON_UNKNOWN
                                    )
                                )
                            }
                        }

                        is DecryptionStatus.Success -> {
                            errorLogger(decryptionStatus.decryptedMessage)
                            withContext(Dispatchers.IO) {
                                messageDao.insertMessage(
                                    Message(
                                        chatId = chatUser.chatId,
                                        senderId = senderId,
                                        recipientId = userId,
                                        content = decryptionStatus.decryptedMessage,
                                        timestamp = System.currentTimeMillis(),
                                        senderMessageId = messageId,
                                        replyId = replyId,
                                        status = ChatMessageStatus.SENDING
                                    )
                                )
                            }
                        }
                    }
                }

            }

            val lastUnreadMessages = withContext(Dispatchers.IO) {
                messageDao.getLastSixUnreadMessage(fcmSenderId, chatUser.chatId).reversed()
            }

            val unreadMessageCount = withContext(Dispatchers.IO) {
                messageDao.countAllUnreadMessages()
            }

            val chatUserProfile = chatUser.userProfile


            if (!App.isAppInForeground) {
                buildAndShowChatNotification(
                    context,
                    fcmSenderId,
                    "${chatUserProfile.firstName} ${chatUserProfile.lastName ?: ""}",
                    lastUnreadMessages,
                    chatUserProfile.profilePicUrl,
                    unreadMessageCount
                )
            }

            return Result.success()

        } else {
            ChatMessageHandlerWorkerHelper.enqueueFetchUserProfileWork(
                context.applicationContext as App,
                userId,
                fcmSenderId,
                fcmMessageId,
                data
            )
        }

        return Result.success()
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




