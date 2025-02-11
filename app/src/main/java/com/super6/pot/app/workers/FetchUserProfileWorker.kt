package com.super6.pot.app.workers

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
import com.super6.pot.App
import com.super6.pot.api.models.service.FeedUserProfileInfo
import com.super6.pot.app.database.models.chat.ChatMessageStatus
import com.super6.pot.app.database.models.chat.ChatMessageType
import com.super6.pot.app.database.models.chat.Message
import com.super6.pot.app.database.daos.chat.MessageDao
import com.super6.pot.app.database.daos.chat.MessageMediaMetaDataDao
import com.super6.pot.app.database.models.chat.MessageMediaMetadata
import com.super6.pot.app.database.models.chat.ChatUser
import com.super6.pot.api.auth.managers.socket.SocketManager
import com.super6.pot.api.auth.managers.socket.SocketConnectionException
import com.super6.pot.app.database.daos.chat.ChatUserDao
import com.super6.pot.app.notifications.NotificationIdManager
import com.super6.pot.app.workers.download.downloadMediaAndCache
import com.super6.pot.database.services.buildAndShowChatNotification
import com.super6.pot.components.utils.LogUtils.TAG
import com.super6.pot.compose.ui.auth.repos.DecryptionFileStatus
import com.super6.pot.compose.ui.auth.repos.DecryptionStatus
import com.super6.pot.compose.ui.auth.repos.decryptFile
import com.super6.pot.compose.ui.auth.repos.decryptMessage
import com.super6.pot.compose.ui.chat.IsolatedChatActivity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.socket.client.Ack
import io.socket.client.Socket
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
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
    private val messageMediaMetadataDao: MessageMediaMetaDataDao,
    val socketManager: SocketManager
    ) : CoroutineWorker(context, workerParams) {


    private lateinit var socket: Socket // Use the appropriate type for your socket


    override suspend fun doWork(): Result {




        val userId = inputData.getLong("user_id", -1)
        val filePath = inputData.getString("data_path")
        /*
                val messageId = inputData.getLong("message_id", -1)
        */
        val timestamp = inputData.getLong("timestamp", -1)

        if (filePath == null) {
            Log.e(TAG, "Missing required input file")
            return Result.failure()  // Return failure if required data is missing
        }


        val cacheFile = File(filePath)
        val cachedData = try {

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



        val data = JSONObject(cachedData)
        val senderId = data.getLong("sender")
        val encryptedMessage = data.getString("message")
        val messageId = data.getLong("message_id")
        val replyId = data.getLong("reply_id")
        val category = data.getString("category")

        val queryString = "sender=$senderId&message_id=$messageId&recipient_id=$userId"


        return try {
            socket = awaitConnectToSocket(socketManager, !App.isAppInForeground, true, queryString)

            if (socket.connected()) {

                try {
                   val profile = withTimeout(300000) {
                        suspendCancellableCoroutine { continuation ->
                            socket.emit("chat:getChatUserProfileInfo", JSONObject().apply {
                                put("user_id", userId)
                                put("recipient_id", senderId)
                            }, Ack { args ->

                                if(isStopped){
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

                                        continuation.resume(profile, null)
                                    } else {
                                        val errorMessage = args.getOrNull(1)?.toString() ?: "Unknown error"
                                        continuation.resumeWithException(Exception("Error: $errorMessage"))
                                    }
                                } catch (e: Exception) {
                                    continuation.resumeWithException(e)
                                }
                            })

                            // Handle cancellation
                            continuation.invokeOnCancellation {
                                socket.off("chat:getChatUserProfileInfo")
                            }
                        }
                    }

                    var publicKeyRecipientId: Long = -1
                    var publicKey: String? = null
                    var keyVersion: Long = -1
                    if (socket.connected()) {

                        val completeDeferred = CompletableDeferred<Unit>()

                        socket.emit("chat:queryPublicKey", JSONObject().apply {
                            put("user_id", userId)
                            put("recipient_id", senderId)
                        }, Ack { args ->
                            // Handle the acknowledgment from the server
                            if (args.isNotEmpty()) {
                                //val message = args[0] as String
                                val queryKeyResponse = args[1] as JSONObject

                                publicKeyRecipientId = queryKeyResponse.getLong("recipient_id")
                                publicKey = queryKeyResponse.getString("publicKey")
                                keyVersion = queryKeyResponse.getLong("keyVersion")
                                completeDeferred.complete(Unit)

                            } else {
                                completeDeferred.completeExceptionally(NotFoundException("Public key not found"))
                            }
                        })

                        // Wait for the acknowledgment or timeout
                        try {
                            withTimeout(100000) {
                                if (!completeDeferred.isCompleted) {
                                    completeDeferred.await()
                                }
                            }

                            completeDeferred.getCompletionExceptionOrNull()?.let {
                                throw it
                            }


                        } catch (e: Exception) {
                            e.printStackTrace()

                            Log.e(TAG, "Acknowledgment not received within 100000 milliseconds")
                            return Result.retry()
                            // Handle timeout logic here (e.g., retry, show an error message, etc.)
                        }




                        if (publicKeyRecipientId == -1L || publicKey == null || keyVersion == -1L) {
                            return Result.retry()
                        }


                        val chatUserProfilePicUrl = profile.profilePicUrl96By96?.let {
                            cacheProfilePic(applicationContext, profile.profilePicUrl96By96, "profile_pic_url_96x96_${senderId}.jpg")
                        }

                        val chatUser = chatUserDao.getChatUserByRecipientId(senderId) ?: run {

                            // Insert into the database
                            chatUserDao.insertChatUser(
                                ChatUser(
                                    userId = userId,
                                    recipientId = senderId,
                                    timestamp = System.currentTimeMillis(),
                                    userProfile = profile.copy(isOnline = false, profilePicUrl96By96 = chatUserProfilePicUrl),
                                    publicKeyBase64 = publicKey,
                                    keyVersion = keyVersion
                                )
                            )

                            chatUserDao.getChatUserByRecipientId(senderId)
                        }

                        if (chatUser == null) {
                            return Result.failure()
                        }

                        val context = applicationContext




                        if (category.contains("image") || category.contains("video") ||
                            category.contains("gif")) {

                            val fileMetadata = JSONObject(data.getString("file_metadata"))
                            val originalFileName = fileMetadata.getString("original_file_name")
                            val downloadUrl = fileMetadata.getString("download_url")
                            val thumbDownloadUrl = fileMetadata.getString("thumb_download_url")
                            val fileSize = fileMetadata.getLong("file_size")
                            val contentType = fileMetadata.getString("content_type")
                            val extension = fileMetadata.getString("extension")
                            val width = fileMetadata.getInt("width")
                            val height = fileMetadata.getInt("height")
                            val totalDuration = fileMetadata.getLong("total_duration")

                            val cachedFile = downloadMediaAndCache(
                                context,
                                thumbDownloadUrl,
                                originalFileName,
                                extension
                            )



                            if(socket.connected()){


                                try {

                                    var mediaFile : File? =null

                                    withTimeout(5000) { // Set timeout to 5 seconds
                                        suspendCancellableCoroutine<Unit> { continuation ->
                                            socket.emit("chat:mediaStatus", JSONObject().apply {
                                                put("status", "MEDIA_DOWNLOADED")
                                                put("download_url", thumbDownloadUrl)
                                                put("sender", senderId)
                                                put("recipient_id", userId)
                                                put("message_id", messageId) // Add the inserted message ID to JSON
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
                                                        status = ChatMessageStatus.FAILED_TO_DISPLAY_REASON_DECRYPTION_FAILED,
                                                        type = if (contentType.startsWith("image/")) {
                                                            if(extension==".gif"){
                                                                ChatMessageType.GIF
                                                            }else{
                                                                ChatMessageType.IMAGE
                                                            }
                                                        } else {
                                                            ChatMessageType.VIDEO
                                                        },
                                                    ),
                                                    messageMediaMetadataDao,
                                                    MessageMediaMetadata(
                                                        messageId = -1,
                                                        fileDownloadUrl = downloadUrl,
                                                        thumbData = null,
                                                        fileThumbPath = null,
                                                        fileSize = fileSize,
                                                        fileMimeType = contentType,
                                                        fileExtension = extension,
                                                        originalFileName = originalFileName,
                                                        width = width,
                                                        height = height,
                                                        totalDuration = totalDuration
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
                                                        status = ChatMessageStatus.FAILED_TO_DISPLAY_REASON_UNKNOWN,
                                                        type = if (contentType.startsWith("image/")) {
                                                            if(extension==".gif"){
                                                                ChatMessageType.GIF
                                                            }else{
                                                                ChatMessageType.IMAGE
                                                            }
                                                        } else {
                                                            ChatMessageType.VIDEO
                                                        }
                                                    ),
                                                    messageMediaMetadataDao,
                                                    MessageMediaMetadata(
                                                        messageId = -1,
                                                        fileDownloadUrl = downloadUrl,
                                                        fileThumbPath = null,
                                                        thumbData = null,
                                                        fileSize = fileSize,
                                                        fileMimeType = contentType,
                                                        fileExtension = extension,
                                                        originalFileName = originalFileName,
                                                        width = width,
                                                        height = height,
                                                        totalDuration = totalDuration
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
                                                            if(extension==".gif"){
                                                                ChatMessageType.GIF
                                                            }else{
                                                                ChatMessageType.IMAGE
                                                            }
                                                        } else {
                                                            ChatMessageType.VIDEO
                                                        }
                                                    ),
                                                    messageMediaMetadataDao,
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

                                    } ?: also {
                                        return Result.failure()
                                    }



                                } catch (t: TimeoutCancellationException) {

                                    return Result.retry()
                                } catch (e: Exception) {
                                    // Handle any unexpected errors
                                    return Result.failure()
                                }

                            }else{
                                throw SocketConnectionException("Socket connect error")
                            }




                        }
                        else if (category.contains("audio")) {

                            val fileMetadata = JSONObject(data.getString("file_metadata"))
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
                                    timestamp = timestamp,
                                    senderMessageId = messageId,
                                    replyId = replyId,
                                    status = ChatMessageStatus.SENDING,
                                    type = ChatMessageType.AUDIO
                                ),
                                messageMediaMetadataDao,
                                MessageMediaMetadata(
                                    messageId = -1,
                                    fileSize = fileSize,
                                    fileMimeType = contentType,
                                    fileExtension = extension,
                                    originalFileName = originalFileName,
                                    totalDuration = totalDuration,
                                    fileDownloadUrl = downloadUrl
                                )
                            )


                        }
                        else if (category.contains("file")) {

                            val fileMetadata = JSONObject(data.getString("file_metadata"))
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
                                    timestamp = timestamp,
                                    senderMessageId = messageId,
                                    replyId = replyId,
                                    status = ChatMessageStatus.SENDING,
                                    type = ChatMessageType.FILE,

                                    ),
                                messageMediaMetadataDao,

                                MessageMediaMetadata(
                                    messageId = -1,
                                    fileDownloadUrl = downloadUrl,
                                    fileSize = fileSize,
                                    fileMimeType = contentType,
                                    fileExtension = extension,
                                    originalFileName = originalFileName,
                                )
                            )
                        }
                        else if (category.contains("others")) {

                            val fileMetadata = data.getJSONObject("file_metadata")
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
                                    timestamp = timestamp,
                                    senderMessageId = messageId,
                                    replyId = replyId,
                                    status = ChatMessageStatus.SENDING,
                                    type = ChatMessageType.FILE,

                                    ),
                                messageMediaMetadataDao,
                                MessageMediaMetadata(
                                    messageId = -1,
                                    fileSize = fileSize,
                                    fileDownloadUrl = downloadUrl,
                                    fileMimeType = contentType,
                                    fileExtension = extension,
                                    originalFileName = originalFileName,
                                )
                            )


                        }
                        else {
                            when (val decryptionStatus = decryptMessage(encryptedMessage)) {
                                is DecryptionStatus.DecryptionFailed -> {
                                    messageDao.insertMessage(
                                        Message(
                                            chatId = chatUser.chatId,
                                            senderId = senderId,
                                            recipientId = userId,
                                            content = "",
                                            timestamp = timestamp,
                                            senderMessageId = messageId,
                                            replyId = replyId,
                                            status = ChatMessageStatus.FAILED_TO_DISPLAY_REASON_DECRYPTION_FAILED
                                        )
                                    )
                                }

                                is DecryptionStatus.UnknownError -> {
                                    messageDao.insertMessage(
                                        Message(
                                            chatId = chatUser.chatId,
                                            senderId = senderId,
                                            recipientId = userId,
                                            content = "",
                                            timestamp = timestamp,
                                            senderMessageId = messageId,
                                            replyId = replyId,
                                            status = ChatMessageStatus.FAILED_TO_DISPLAY_REASON_UNKNOWN
                                        )
                                    )
                                }

                                is DecryptionStatus.Success -> {
                                    messageDao.insertMessage(
                                        Message(
                                            chatId = chatUser.chatId,
                                            senderId = senderId,
                                            recipientId = userId,
                                            content = decryptionStatus.decryptedMessage,
                                            timestamp = timestamp,
                                            senderMessageId = messageId,
                                            replyId = replyId,
                                            status = ChatMessageStatus.SENDING
                                        )
                                    )
                                }
                            }
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
                                Log.e(TAG, "Text message acknowledgment received by client ${messageId}")
                            })

                            // Wait for the acknowledgment or timeout
                          try {
                                withTimeout(timeoutDuration) {
                                    // Await the acknowledgment response
                                    acknowledgmentReceived.await()
                                }
                            } catch (e: TimeoutCancellationException) {
                              e.printStackTrace()

                              Log.e(TAG, "Acknowledgment not received within $timeoutDuration milliseconds")
                                return Result.retry()
                                // Handle timeout logic here (e.g., retry, show an error message, etc.)
                            }

                        } else {
                            throw SocketConnectionException("Socket connect error")
                        }


                        cleanCacheFile(cacheFile)

                        Result.success()


                    } else {
                        throw SocketConnectionException("Socket is not connected")
                    }

                }catch (e:Exception){
                    e.printStackTrace()
                    Result.retry()
                }

            }else{
                throw SocketConnectionException("Socket is not connected")
            }


        } catch (e: SocketConnectionException) {
            // If the socket connection error occurs, retry the work
            Log.e(TAG, "Socket connection failed. Retrying...")
            finalizeSocket()
            Result.retry() // Request retry in case of socket connection failure
        } catch (e: Exception) {

            cleanCacheFile(cacheFile)
            finalizeSocket()
            e.printStackTrace()
            Log.e(TAG, "Error processing message: ${e.message}")
            Result.failure()
        }

    }


    private suspend fun cacheProfilePic(context: Context, imageUrl: String, fileName: String): String? {
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

            } catch (e: Exception) {
                return null
            }
        } else {

            return null
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
    private fun cleanCacheFile(cacheFile: File){

        if(cacheFile.exists()){
            cacheFile.delete()
        }
    }

}