package com.super6.pot.app.workers.helpers

import android.app.Application
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.super6.pot.app.workers.ChatMessageProcessor
import com.super6.pot.app.workers.FetchUserProfileWorker
import com.super6.pot.app.workers.VisualMediaChatMessageProcessor
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit



object ChatMessageHandlerWorkerHelper {

    private val FETCH_USER_PROFILE = "fetch_user_profile"

    private val CHAT_MESSAGE_PROCESSOR = "chat_message_processor"
    private val VISUAL_MEDIA_MESSAGE_PROCESSOR_WORKER = "visual_media_message_processor"


    fun enqueueFetchUserProfileWork(
        application: Application,
        userId: Long,
        senderId: Long,
        messageId: Long,
        data: String) {

        val timestamp = System.currentTimeMillis()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val context = application.applicationContext // Get application context

        val cacheFile = File(context.cacheDir, "${userId}_${senderId}_${messageId}_data.txt")
        cacheFile.writeText(data)  // Write your large data to the file

        // You can now pass the file path to the worker
        val filePath = cacheFile.absolutePath


        val fetchProfileWork = OneTimeWorkRequestBuilder<FetchUserProfileWorker>()
            .setInputData(
                Data.Builder()
                    .putLong("user_id", userId)
                    .putString("data_path", filePath)
                    .putLong("timestamp", timestamp)
                    .build())
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(application.applicationContext)
            .enqueueUniqueWork(
                "${FETCH_USER_PROFILE}_${senderId}",
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                fetchProfileWork
            )


    }


    fun enqueueChatMessageProcessor(
        application: Application,
        userId: Long,
        messageId: Long,
        title: String,
        data: String,
        type: String,
    ) {
        // Create input data to pass the FCM token to the worker

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()


        val context = application.applicationContext // Get application context

        val cacheFile = File(context.cacheDir, "${messageId}_data.txt")
        cacheFile.writeText(data)  // Write your large data to the file

       // You can now pass the file path to the worker
        val filePath = cacheFile.absolutePath


        val inputData = Data.Builder()
            .putLong("user_id", userId)
            .putString("title", title)
            .putString("data_path", filePath)
            .putString("type", type)
            .build()


        // Create a work request to send the FCM token
        val chatMessageWorkRequest = OneTimeWorkRequestBuilder<ChatMessageProcessor>()
            .setInputData(inputData)
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,   // Retry with a fixed backoff
                10,
                TimeUnit.SECONDS
            ).build()


        WorkManager.getInstance(application.applicationContext)
            .enqueueUniqueWork(
                CHAT_MESSAGE_PROCESSOR,          // Unique work name
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                chatMessageWorkRequest
            )
    }


    fun visualMediaChatMessageProcessor(
        application: Application,
        data: JSONObject,
        chatId: Int,
        chatRecipientId: Long,
        senderId: Long,
        replyId:Long,
        senderMessageId: Long,
        content:String
    ) {

        // Create input data to pass the FCM token to the worker
        val fileMetadata = JSONObject(data.getString("file_metadata"))

        val originalFileName = fileMetadata.optString("original_file_name")
        val contentType = fileMetadata.optString("content_type")
        val fileSize = fileMetadata.optLong("file_size")
        val fileExtension = fileMetadata.optString("extension")
        val width = fileMetadata.optInt("width")
        val height = fileMetadata.optInt("height")
        val totalDuration = fileMetadata.optLong("total_duration",-1)

        val downloadUrl = fileMetadata.optString("download_url")
        val thumbDownloadUrl = fileMetadata.optString("thumb_download_url")

        // Create a work request to send the FCM token
        val doFileUploadWorkRequest = OneTimeWorkRequestBuilder<VisualMediaChatMessageProcessor>()
            .setConstraints( Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build())
            .setInputData(
                workDataOf(
                    "senderId" to senderId,
                    "chatRecipientId" to chatRecipientId,
                    "senderMessageId" to senderMessageId,
                    "replyId" to replyId,
                    "chatId" to chatId,
                    "contentType" to contentType,
                    "content" to content,
                    "originalFileName" to originalFileName,
                    "extension" to fileExtension,
                    "fileSize" to fileSize,
                    "width" to width,
                    "height" to height,
                    "totalDuration" to totalDuration,
                    "downloadUrl" to downloadUrl,
                    "thumbDownloadUrl" to thumbDownloadUrl,
                )
            )
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,   // Retry with a fixed backoff
                10,
                TimeUnit.SECONDS
            ).build()



        WorkManager.getInstance(application.applicationContext).enqueueUniqueWork(
            "${VISUAL_MEDIA_MESSAGE_PROCESSOR_WORKER}_${chatId}_${chatRecipientId}_${senderMessageId}",          // Unique work name
            ExistingWorkPolicy.REPLACE, // Replace existing work with the same name
            doFileUploadWorkRequest
        )

    }




}
