package com.super6.pot.app.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.super6.pot.App
import com.super6.pot.api.common.CommonClient
import com.super6.pot.app.database.models.chat.ChatMessageStatus
import com.super6.pot.app.database.models.chat.ChatMessageType
import com.super6.pot.app.database.daos.chat.MessageDao
import com.super6.pot.app.database.daos.chat.MessageMediaMetaDataDao
import com.super6.pot.app.database.models.chat.MessageMediaMetadata
import com.super6.pot.app.database.daos.profile.UserProfileDao
import com.super6.pot.ui.auth.repos.DecryptionFileStatus
import com.super6.pot.ui.auth.repos.decryptFile
import com.super6.pot.api.auth.managers.socket.SocketManager
import com.super6.pot.api.auth.managers.socket.SocketConnectionException
import com.super6.pot.api.auth.services.CommonService
import com.super6.pot.app.database.daos.chat.ChatUserDao
import com.super6.pot.app.database.daos.notification.NotificationDao
import com.super6.pot.app.database.models.chat.Message
import com.super6.pot.utils.LogUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.socket.client.Ack
import io.socket.client.Socket
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import javax.inject.Inject


@HiltWorker
class VisualMediaChatMessageProcessor @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted workerParams: WorkerParameters,
    val notificationDao:NotificationDao,
    val chatUserDao:ChatUserDao,
    val messageDao:MessageDao,
    val fileMetaDataDao: MessageMediaMetaDataDao,
    val userProfileDao: UserProfileDao,
    val socketManager: SocketManager

) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "SUPER6_APP_IMAGE_MESSAGE_PROCESSOR"
    }

    private lateinit var socket: Socket // Use the appropriate type for your socket

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
            // Assuming socket connection and message processing can be done inside a coroutine
            socket = awaitConnectToSocket(socketManager, !App.isAppInForeground)

            val responseBody = CommonClient.rawInstance.create(CommonService::class.java)
                .downloadImage(thumbDownloadUrl)

            // Get the cache directory where the file should be stored
            val dir = context.getExternalFilesDir(null)

            val fileCategoryAndTypeByExtension = getFolderTypeByExtension(fileExtension)

            val directory = File(dir, fileCategoryAndTypeByExtension.first)

            if (!directory.exists()) {
                directory.mkdirs()  // Create the directory if it doesn't exist
            }

            // Create the file in the desired directory
            val destinationFile = File(directory, "$originalFileName.enc")

            var readLength: Int

            val buffer = ByteArray(4 * 1024)  // 4 KB buffer size
            val bis = BufferedInputStream(responseBody.byteStream(), 4 * 1024)

            var totalBytesDownloaded: Long = 0

            // Write directly to disk in chunks
            val fileOutputStream = withContext(Dispatchers.IO) {
                FileOutputStream(destinationFile, false)
            }
            val fileChannel = fileOutputStream.channel
            val bufferByteBuffer = ByteBuffer.allocate(4 * 1024)



            withContext(Dispatchers.IO) {
                while (bis.read(buffer).also { readLength = it } != -1) {

                    if (!isActive) {
                        cancel() // Stop the download if the job is cancelled
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


            // Handle decryption after download
            val mediaFile = cacheThumbnailToAppSpecificFolder(
                context,
                originalFileName,
                if (contentType.startsWith("video/")) ".jpg" else fileExtension,
                fileExtension)

            when (val decryptionFileStatus = decryptFile(destinationFile, mediaFile)) {
                is DecryptionFileStatus.DecryptionFailed -> {
                    destinationFile.delete()
                    mediaFile.delete()

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
                                if(fileExtension==".gif"){
                                    ChatMessageType.GIF
                                }else{
                                    ChatMessageType.IMAGE
                                }

                            } else {
                                ChatMessageType.VIDEO
                            }),

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
                    mediaFile.delete()

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
                                if(fileExtension==".gif"){
                                    ChatMessageType.GIF
                                }else{
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
                                if(fileExtension==".gif"){
                                    ChatMessageType.GIF
                                }else{
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



            if (socket.connected()) {

                val timeoutDuration = 20000L // 30 seconds timeout in milliseconds

                // Create the acknowledgment promise
                val acknowledgmentReceived = CompletableDeferred<Unit>()

                // Emit the message
                socket.emit("chat:acknowledgment", JSONObject().apply {
                    put("status", "delivered")
                    put("sender", senderId)
                    put("recipient_id", recipientId)
                    put("message_id", senderMessageId) // Add the inserted message ID to JSON
                }, Ack {
                    // This callback will be triggered when the acknowledgment is received
                    acknowledgmentReceived.complete(Unit)
                    Log.e(LogUtils.TAG, "Others media message acknowledgment received by client")
                })

                // Wait for the acknowledgment or timeout
                try {
                    withTimeout(timeoutDuration) {
                        // Await the acknowledgment response
                        if(!acknowledgmentReceived.isCompleted){
                            acknowledgmentReceived.await()
                        }
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


            finalizeSocket()
            Result.success()


        } catch (e: SocketConnectionException) {
            Log.e(TAG, "Socket connection failed: Retrying...")
            finalizeSocket()
            Result.retry()  // Retry the task in case of a socket connection failure
        } catch (e: Exception) {
            e.printStackTrace()
            finalizeSocket()
            Log.e(TAG, "Error processing message: ${e.message}")
            Result.failure()  // Return failure for other types of exceptions
        }


    }


    fun finalizeSocket() {
        if (socketManager.isBackgroundSocket) {
            socketManager.destroySocket()
            if (App.isAppInForeground) {
                socketManager.getSocket()
            }
        }
    }


}

