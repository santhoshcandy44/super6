package com.lts360.app.workers.helpers

import android.app.Application
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.lts360.app.workers.upload.MediaUploadWorker
import com.lts360.app.workers.upload.models.FileUploadInfo
import java.util.concurrent.TimeUnit

object MediaUploadWorkerHelper {

    private val MEDIA_UPLOAD_WORKER = "media_upload"


    fun doMediaUpload(application: Application, fileUploadInfo: FileUploadInfo) {
        // Process all media uploads with a single function call
        when {fileUploadInfo.mimeType.startsWith("video/") ||
                fileUploadInfo.mimeType.startsWith("audio/") ||
                fileUploadInfo.mimeType.startsWith("application/") ||
                fileUploadInfo.mimeType.startsWith("text/")
            -> {
            // Call a common upload function for all media types
            uploadMedia(application, fileUploadInfo)
        }
            else -> {
                throw IllegalArgumentException("Unsupported media type: ${fileUploadInfo.mimeType}")
            }
        }
    }


    private fun uploadMedia(application: Application, fileUploadInfo: FileUploadInfo) {
        WorkManager.getInstance(application.applicationContext).enqueueUniqueWork(
            "${MEDIA_UPLOAD_WORKER}_${fileUploadInfo.chatId}_${fileUploadInfo.messageId}",
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<MediaUploadWorker>()
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setInputData(
                    workDataOf(
                    "messageId" to fileUploadInfo.messageId,
                    "senderId" to fileUploadInfo.senderId,
                    "recipientId" to fileUploadInfo.recipientId,
                    "replyId" to fileUploadInfo.replyId,
                    "content" to fileUploadInfo.content,
                    "category" to fileUploadInfo.category,
                    "fileName" to fileUploadInfo.fileName,
                    "extension" to fileUploadInfo.extension,
                    "mimeType" to fileUploadInfo.mimeType,
                    "fileLength" to fileUploadInfo.mediaLength,
                    "fileAbsPath" to fileUploadInfo.mediaAbsPath,
                    "totalDuration"  to fileUploadInfo.totalDuration
                )
                )
                .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.SECONDS)
                .build()
        )
    }


    fun cancelMediaUploadWorker(application: Application, chatId: Int, messageId: Long) {
        val workManager = WorkManager.getInstance(application)
        workManager.cancelUniqueWork("${MEDIA_UPLOAD_WORKER}_${chatId}_${messageId}")

    }

}