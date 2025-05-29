package com.lts360.app.workers.helpers

import android.app.Application
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.lts360.app.workers.chat.FetchUserProfileWorker
import com.lts360.app.workers.chat.OfflineChatMessagesProcessor
import com.lts360.app.workers.chat.VisualMediaChatMessageProcessor
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit



object ChatMessageHandlerWorkerHelper {


    private const val FETCH_USER_PROFILE = "fetch_user_profile"

    private const val OFFLINE_MESSAGES_FETCH_PROCESSOR = "offline_messages_processor"

    private const val VISUAL_MEDIA_MESSAGE_PROCESSOR_WORKER = "visual_media_message_processor"


    fun enqueueFetchUserProfileWork(
        application: Application,
        userId: Long,
        senderId: Long,
        type: String,
        data: String) {


        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()


        val fetchProfileWork = OneTimeWorkRequestBuilder<FetchUserProfileWorker>()
            .setInputData(
                Data.Builder()
                    .putLong("user_id", userId)
                    .putString("data", data)
                    .putString("type", type)
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


    fun enqueueFetchChatMessages(
        application: Application,
        userId: Long,
        type: String,
        data: String,
    ) {

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val inputData = Data.Builder()
            .putLong("user_id", userId)
            .putString("data", data)
            .putString("type", type)
            .build()


        val chatMessageWorkRequest = OneTimeWorkRequestBuilder<OfflineChatMessagesProcessor>()
            .setInputData(inputData)
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                10,
                TimeUnit.SECONDS
            ).build()


        WorkManager.getInstance(application.applicationContext)
            .enqueueUniqueWork(
                OFFLINE_MESSAGES_FETCH_PROCESSOR,
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
                BackoffPolicy.LINEAR,
                10,
                TimeUnit.SECONDS
            ).build()

        WorkManager.getInstance(application.applicationContext).enqueueUniqueWork(
            "${VISUAL_MEDIA_MESSAGE_PROCESSOR_WORKER}_${chatId}_${chatRecipientId}_${senderMessageId}",          // Unique work name
            ExistingWorkPolicy.REPLACE,
            doFileUploadWorkRequest
        )

    }




}
