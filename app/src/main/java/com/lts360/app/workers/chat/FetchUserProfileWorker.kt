package com.lts360.app.workers.chat

import android.content.Context
import android.content.res.Resources.NotFoundException
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.toBitmap
import com.google.gson.Gson
import com.lts360.App
import com.lts360.api.auth.managers.socket.SocketConnectionException
import com.lts360.api.auth.managers.socket.SocketManager
import com.lts360.api.models.service.FeedUserProfileInfo
import com.lts360.app.database.daos.chat.ChatUserDao
import com.lts360.app.database.daos.chat.MessageDao
import com.lts360.app.database.daos.chat.MessageMediaMetaDataDao
import com.lts360.app.database.models.chat.ChatMessageStatus
import com.lts360.app.database.models.chat.ChatMessageType
import com.lts360.app.database.models.chat.ChatUser
import com.lts360.app.database.models.chat.Message
import com.lts360.app.database.models.chat.MessageMediaMetadata
import com.lts360.app.workers.chat.download.downloadMediaAndCache
import com.lts360.app.workers.chat.utils.awaitConnectToSocket
import com.lts360.app.workers.chat.utils.cacheThumbnailToAppSpecificFolder
import com.lts360.components.utils.LogUtils.TAG
import com.lts360.compose.ui.auth.repos.DecryptionFileStatus
import com.lts360.compose.ui.auth.repos.DecryptionStatus
import com.lts360.compose.ui.auth.repos.decryptFile
import com.lts360.compose.ui.auth.repos.decryptMessage
import com.lts360.app.notifications.buildAndShowChatNotification
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
import java.io.FileOutputStream
import kotlin.coroutines.resumeWithException


@HiltWorker
class FetchUserProfileWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val chatUserDao: ChatUserDao,
    private val messageDao: MessageDao,
    private val fileMetadataDao: MessageMediaMetaDataDao,
    val socketManager: SocketManager
) : CoroutineWorker(context, workerParams) {


    private lateinit var socket: Socket


    override suspend fun doWork(): Result {

        val userId = inputData.getLong("user_id", -1)
        val data = inputData.getString("data")
        val type = inputData.getString("type")

        if (userId == -1L || type == null || data == null) {
            Log.e(TAG, "Missing required input data")
            return Result.failure()
        }


        val fcmData = JSONObject(data)
        val fcmSenderId = fcmData.getLong("sender_id")
        val messageId = fcmData.getLong("message_id")

        val queryString = "sender_id=$fcmSenderId&message_id=$messageId&recipient_id=$userId"


        return try {

            socket = awaitConnectToSocket(socketManager, !App.isAppInForeground, true, queryString)

            if (socket.connected()) {

                try {
                    val profile = withTimeout(300000) {
                        suspendCancellableCoroutine<FeedUserProfileInfo> { continuation ->
                            socket.emit("chat:getChatUserProfileInfo", JSONObject().apply {
                                put("user_id", userId)
                                put("recipient_id", fcmSenderId)
                            }, Ack { args ->

                                if (isStopped) {
                                    return@Ack
                                }

                                try {
                                    val status = args[0] as String?

                                    if (status == "SUCCESS") {
                                        val profileInfo = args[1] as JSONObject
                                        val profile = Gson().fromJson(
                                            profileInfo.toString(),
                                            FeedUserProfileInfo::class.java
                                        ) as FeedUserProfileInfo

                                        continuation.resume(profile, { _, _, _ -> })
                                    } else {
                                        val errorMessage =
                                            args.getOrNull(1)?.toString() ?: "Unknown error"
                                        continuation.resumeWithException(Exception("Error: $errorMessage"))
                                    }
                                } catch (e: Exception) {
                                    continuation.resumeWithException(e)
                                }
                            })

                            continuation.invokeOnCancellation {
                                socket.off("chat:getChatUserProfileInfo")
                            }
                        }
                    }


                    var publicKeyRecipientId: Long = -1
                    var publicKey: String? = null
                    var keyVersion: Long = -1

                    if (socket.connected()) {


                        try {
                            withTimeout(100_000) {
                                suspendCancellableCoroutine<Unit> { cont ->
                                    socket.emit("chat:queryPublicKey", JSONObject().apply {
                                        put("user_id", userId)
                                        put("recipient_id", fcmSenderId)
                                    }, Ack { args ->
                                        if (args.isNotEmpty()) {
                                            try {
                                                val queryKeyResponse = args[1] as JSONObject

                                                publicKeyRecipientId =
                                                    queryKeyResponse.getLong("recipient_id")
                                                publicKey = queryKeyResponse.getString("publicKey")
                                                keyVersion = queryKeyResponse.getLong("keyVersion")

                                                if (cont.isActive) cont.resume(
                                                    Unit,
                                                    { cause, _, _ -> })
                                            } catch (e: Exception) {
                                                if (cont.isActive) cont.resumeWithException(e)
                                            }
                                        } else {
                                            if (cont.isActive) cont.resumeWithException(
                                                NotFoundException("Public key not found")
                                            )
                                        }
                                    })
                                }
                            }

                        } catch (e: Exception) {
                            e.printStackTrace()
                            Log.e(TAG, "Acknowledgment not received within 100000 milliseconds")
                            return Result.retry()
                        }


                        if (publicKeyRecipientId == -1L || publicKey == null || keyVersion == -1L) {
                            return Result.retry()
                        }


                        val chatUserProfilePicUrl = profile.profilePicUrl96By96?.let {
                            cacheProfilePic(
                                applicationContext,
                                profile.profilePicUrl96By96,
                                "profile_pic_url_96x96_${fcmSenderId}.jpg"
                            )
                        }

                        val chatUser = chatUserDao.getChatUserByRecipientId(fcmSenderId) ?: run {

                            chatUserDao.insertChatUser(
                                ChatUser(
                                    userId = userId,
                                    recipientId = fcmSenderId,
                                    userProfile = profile.copy(
                                        isOnline = false,
                                        profilePicUrl96By96 = chatUserProfilePicUrl
                                    ),
                                    publicKeyBase64 = publicKey,
                                    keyVersion = keyVersion,
                                    timestamp = System.currentTimeMillis()
                                )
                            )

                            chatUserDao.getChatUserByRecipientId(fcmSenderId)
                        }

                        if (chatUser == null) {
                            return Result.failure()
                        }

                        val context = applicationContext


                        val messagesJsonArray = suspendCancellableCoroutine { cont ->

                            socket.once("chat:offlineMessages", Emitter.Listener { args ->
                                if (args.isNotEmpty()) {
                                    try {
                                        val data = args[0] as JSONObject
                                        val messagesJsonArray =
                                            data.getJSONArray("offline_messages")

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
                                        suspendCancellableCoroutine<Unit> { cont ->


                                            socket.emit(
                                                "chat:offlineMessageAcknowledgment",
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

                            if (category.contains("image") || category.contains("video") || category.contains(
                                    "gif"
                                )
                            ) {

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

                                        withTimeout(5000) {
                                            suspendCancellableCoroutine<Unit> { continuation ->
                                                socket.emit("chat:mediaStatus", JSONObject().apply {
                                                    put("status", "MEDIA_DOWNLOADED")
                                                    put("download_url", thumbDownloadUrl)
                                                    put("sender_id", senderId)
                                                    put("recipient_id", userId)
                                                    put(
                                                        "message_id",
                                                        messageId
                                                    )
                                                }, Ack {

                                                    mediaFile = cacheThumbnailToAppSpecificFolder(
                                                        context,
                                                        originalFileName,
                                                        if (contentType.startsWith("video/")) ".jpg" else extension,
                                                        extension
                                                    )

                                                    continuation.resume(Unit) { cause, value, context -> }
                                                })
                                            }
                                        }


                                        mediaFile?.let {
                                            when (val decryptionFileStatus =
                                                decryptFile(cachedFile, it)) {

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

                                                    val outputFile =
                                                        decryptionFileStatus.decryptedFile

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


                                    } catch (_: TimeoutCancellationException) {

                                        return Result.retry()
                                    } catch (_: Exception) {
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


                        val (chatUserProfile, lastUnreadMessages, unreadMessageCount) = withContext(Dispatchers.IO) {
                            val user = chatUser.userProfile
                            val messages = messageDao.getLastSixUnreadMessage(fcmSenderId, chatUser.chatId).reversed()
                            val count = messageDao.countAllUnreadMessages()
                            Triple(user, messages, count)
                        }

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

                        Result.success()


                    } else {
                        throw SocketConnectionException("Socket is not connected")
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    Result.retry()
                }

            } else {
                Log.e(TAG, "Socket is not connected")
                throw SocketConnectionException("Socket is not connected")
            }


        } catch (_: SocketConnectionException) {
            Log.e(TAG, "Socket connection failed. Retrying...")
            finalizeSocket()
            Result.retry()
        } catch (e: Exception) {
            finalizeSocket()
            e.printStackTrace()
            Log.e(TAG, "Error processing message: ${e.message}")
            Result.failure()
        }

    }


    private suspend fun cacheProfilePic(
        context: Context,
        imageUrl: String,
        fileName: String
    ): String? {
        val imageLoader = ImageLoader(context)
        val request = ImageRequest.Builder(context)
            .data(imageUrl)
            .build()

        val parent = File(context.filesDir, "/chats/Profile Pictures")

        if (!parent.exists()) {
            parent.mkdirs()
        }


        val result = (imageLoader.execute(request) as? SuccessResult)?.image
        if (result != null) {
            val file = File(parent, fileName)
            try {
                withContext(Dispatchers.IO) {
                    FileOutputStream(file).use { outputStream ->
                        val bitmap = result.toBitmap()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                        outputStream.flush()
                    }
                }


                return Uri.fromFile(file).toString()

            } catch (_: Exception) {
                return null
            }
        } else {

            return null
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