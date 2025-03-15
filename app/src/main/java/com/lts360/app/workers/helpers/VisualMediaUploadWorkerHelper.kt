package com.lts360.app.workers.helpers

import android.app.Application
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.lts360.app.workers.chat.upload.VisualMediaUploadWorker
import com.lts360.app.workers.chat.upload.models.FileUploadInfo
import java.util.concurrent.TimeUnit


object VisualMediaUploadWorkerHelper {

    private val VISUAL_MEDIA_UPLOAD_WORKER = "visual_media_upload"

    fun doVisualMediaUpload(application: Application, fileUploadInfo: FileUploadInfo) {
        if (fileUploadInfo.mimeType.startsWith("image/") ||
            fileUploadInfo.mimeType.startsWith("video/") || fileUploadInfo.mimeType.startsWith("*/*")) {
            uploadVisualMedia(application, fileUploadInfo)
        } else {
            throw IllegalArgumentException("The file is not an image or video.")
        }
    }


    private fun uploadVisualMedia(application: Application, fileUploadInfo: FileUploadInfo) {
        WorkManager.getInstance(application.applicationContext).enqueueUniqueWork("${VISUAL_MEDIA_UPLOAD_WORKER}_${fileUploadInfo.chatId}_${fileUploadInfo.messageId}",
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<VisualMediaUploadWorker>()
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setInputData(
                    workDataOf(
                    "fileName" to fileUploadInfo.fileName,
                    "extension" to fileUploadInfo.extension,
                    "category" to fileUploadInfo.category,
                    "mimeType" to fileUploadInfo.mimeType,
                    "fileLength" to fileUploadInfo.mediaLength,
                    "thumbnailCachePath" to fileUploadInfo.thumbnailCachedPath,
                    "fileAbsPath" to fileUploadInfo.mediaAbsPath,
                    "senderId" to fileUploadInfo.senderId,
                    "recipientId" to fileUploadInfo.recipientId,
                    "messageId" to fileUploadInfo.messageId,
                    "replyId" to fileUploadInfo.replyId,
                    "content" to fileUploadInfo.content,
                    "width" to fileUploadInfo.width,
                    "height" to fileUploadInfo.height,
                    "totalDuration" to fileUploadInfo.totalDuration
                ))
                .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.SECONDS)
                .build()
        )
    }

    fun cancelVisualMediaUploadWorker(application: Application, chatId: Int, messageId: Long) {
        WorkManager.getInstance(application)
            .cancelUniqueWork("${VISUAL_MEDIA_UPLOAD_WORKER}_${chatId}_${messageId}")
    }
}