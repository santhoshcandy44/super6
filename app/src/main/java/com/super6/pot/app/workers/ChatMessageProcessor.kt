package com.super6.pot.app.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.super6.pot.App
import com.super6.pot.app.database.models.chat.ChatMessageStatus
import com.super6.pot.app.database.daos.chat.MessageDao
import com.super6.pot.app.database.daos.chat.MessageMediaMetaDataDao
import com.super6.pot.app.database.models.chat.MessageMediaMetadata
import com.super6.pot.app.database.daos.notification.NotificationDao
import com.super6.pot.app.database.daos.profile.UserProfileDao
import com.super6.pot.ui.auth.repos.DecryptionFileStatus
import com.super6.pot.ui.auth.repos.DecryptionStatus
import com.super6.pot.ui.auth.repos.decryptFile
import com.super6.pot.ui.auth.repos.decryptMessage
import com.super6.pot.api.auth.managers.socket.SocketManager
import com.super6.pot.app.notifications.NotificationIdManager
import com.super6.pot.api.auth.managers.socket.SocketConnectionException
import com.super6.pot.app.database.daos.chat.ChatUserDao
import com.super6.pot.app.database.models.chat.ChatMessageType
import com.super6.pot.app.database.models.chat.Message
import com.super6.pot.app.workers.download.downloadMediaAndCache
import com.super6.pot.app.workers.helpers.ChatMessageHandlerWorkerHelper
import com.super6.pot.database.services.buildAndShowChatNotification
import com.super6.pot.utils.LogUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.socket.client.Ack
import io.socket.client.Socket
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import java.io.File
import javax.inject.Inject


@HiltWorker
class ChatMessageProcessor @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {

    @Inject
    lateinit var notificationDao: NotificationDao

    @Inject
    lateinit var chatUserDao: ChatUserDao

    @Inject
    lateinit var messageDao: MessageDao

    @Inject
    lateinit var fileMetadataDao: MessageMediaMetaDataDao

    @Inject
    lateinit var userProfileDao: UserProfileDao

    @Inject
    lateinit var socketManager: SocketManager

    private lateinit var socket: Socket // Use the appropriate type for your socket

    companion object {

        private const val TAG = "SUPER6_APP_CHAT_MESSAGE_PROCESSOR"
    }


    override suspend fun doWork(): Result {

        val userId = inputData.getLong("user_id", -1)
        val title = inputData.getString("title")
        val filePath = inputData.getString("data_path")
        val type = inputData.getString("type")
        // Ensure required fields are not null
        if (userId == -1L || title == null || type == null || filePath == null) {
            Log.e(TAG, "Missing required input data")
            return Result.failure()  // Return failure if required data is missing
        }

        val cacheFile = File(filePath)

        // Read large data from file in cacheDir
        val data = try {
            if (cacheFile.exists()) {
                val dataFromFile = cacheFile.readText()  // Read the large data from the file

                dataFromFile
            } else {
                Log.e(TAG, "Cache file not found at: $filePath")
                return Result.failure()  // If file doesn't exist, return failure
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading data from file: ${e.message}")
            return Result.failure()  // Return failure if there's an issue reading the file
        }

        // Proceed with message processing after reading the data
        return try {

            val result = when (val result = processMessage(title, data, type, userId)) {
                Result.success() -> {
                    cleanCacheFile(cacheFile)
                    result
                }

                Result.failure() -> {
                    cleanCacheFile(cacheFile)
                    result
                }

                else -> result
            }

            finalizeSocket()


            return result


        } catch (e: SocketConnectionException) {
            Log.e(TAG, "Socket connection failed. Retrying...")
            finalizeSocket()
            Result.retry()  // Retry the task in case of a socket connection failure
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "Error processing message: ${e.message}")
            cleanCacheFile(cacheFile)
            finalizeSocket()
            Result.failure()  // Return failure for other types of exceptions
        }
    }


    private suspend fun processMessage(
        title: String?,
        data: String?,
        type: String?,
        userId: Long,
    ): Result {


        return if (title != null && data != null && type != null) {
            when (type) {
                "chat_message" -> {
                    handleChatMessage(data, userId)
                }

                else -> Result.failure()
            }
        } else {
            Result.failure()
        }

    }


    private suspend fun handleChatMessage(data: String, userId: Long): Result {

        val messageData = JSONObject(data)
        val senderId = messageData.getLong("sender")
        val encryptedMessage = messageData.getString("message")
        val messageId = messageData.getLong("message_id")
        val replyId = messageData.getLong("reply_id")
        val category = messageData.getString("category")


        val chatUser = chatUserDao.getChatUserByRecipientId(senderId)


        if (chatUser != null) {

            // Assuming socket connection and message processing can be done inside a coroutine
            socket = awaitConnectToSocket(socketManager, !App.isAppInForeground)



            if (category.contains("image") || category.contains("video") || category.contains("gif")){

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

                val cachedFile = downloadMediaAndCache(context, thumbDownloadUrl, originalFileName, extension)
                // Handle decryption after download
                val mediaFile = cacheThumbnailToAppSpecificFolder(
                    context,
                    originalFileName,
                    if (contentType.startsWith("video/")) ".jpg" else extension,
                    extension
                )

                when (val decryptionFileStatus = decryptFile(cachedFile, mediaFile)) {

                    is DecryptionFileStatus.DecryptionFailed -> {
                        cachedFile.delete()

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

            }

            else if (category.contains("audio")) {


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



            }

            else if (category.contains("file")) {



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



            }
            else if (category.contains("others")) {



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

            }
            else {

                val decryptionStatus = decryptMessage(encryptedMessage)

                when (decryptionStatus) {
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

            if (socket.connected()) {

                val timeoutDuration = 20000L // 30 seconds timeout in milliseconds

                // Create the acknowledgment promise
                val acknowledgmentReceived = CompletableDeferred<Unit>()

                // Emit the message
                socket.emit("chat:acknowledgment", JSONObject().apply {
                    put("status", "delivered")
                    put("sender", senderId)
                    put("recipient_id", userId)
                    put("message_id", messageId) // Add the inserted message ID to JSON
                }, Ack {
                    // This callback will be triggered when the acknowledgment is received
                    acknowledgmentReceived.complete(Unit)
                    Log.e(LogUtils.TAG, "Others media message acknowledgment received by client")
                })

                // Wait for the acknowledgment or timeout
                try {
                    withTimeout(timeoutDuration) {
                        // Await the acknowledgment response
                        acknowledgmentReceived.await()
                    }
                } catch (e: TimeoutCancellationException) {
                    Log.e(
                        LogUtils.TAG,
                        "Acknowledgment not received within $timeoutDuration milliseconds"
                    )
                    return Result.retry()
                    // Handle timeout logic here (e.g., retry, show an error message, etc.)
                }
            } else {
                throw SocketConnectionException("Socket connect error")
            }


            val lastUnreadMessages = withContext(Dispatchers.IO) {
                messageDao.getLastSixUnreadMessage(senderId, chatUser.chatId).reversed()
            }
            val unreadMessageCount = withContext(Dispatchers.IO) {
                messageDao.countAllUnreadMessages()
            }

            val chatUserProfile = chatUser.userProfile


            if (!App.isAppInForeground) {
                buildAndShowChatNotification(
                    context,
                    NotificationIdManager.getNotificationId(senderId), // Reusing notification ID
                    senderId,
                    "${chatUserProfile.firstName} ${chatUserProfile.lastName ?: ""}",
                    lastUnreadMessages,
                    chatUserProfile.profilePicUrl,
                    unreadMessageCount
                )
            }


            return Result.success()

        } else {


            // Enqueue the work request
            ChatMessageHandlerWorkerHelper.enqueueFetchUserProfileWork(
                context.applicationContext as App,
                userId,
                senderId,
                messageId,
                data
            )


        }


        return Result.success()
    }

    private fun finalizeSocket() {
        if (socketManager.isBackgroundSocket) {
            socketManager.destroySocket()
            if (App.isAppInForeground) {
                socketManager.getSocket()
            }
        }
    }

    private fun cleanCacheFile(cacheFile: File){

        if(cacheFile.exists()){
            cacheFile.delete()
        }
    }
}


