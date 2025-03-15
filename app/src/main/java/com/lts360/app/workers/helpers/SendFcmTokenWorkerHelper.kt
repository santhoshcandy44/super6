package com.lts360.app.workers.helpers

import android.app.Application
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.lts360.app.workers.fcm.HandleFcmTokenWorker
import java.util.concurrent.TimeUnit


object SendFcmTokenWorkerHelper {

    private val SEND_FCM_TOKEN_WORKER = "send_fcm_token"

    fun enqueueSendFCMTokenToServerWork(application: Application, fcmToken: String) {
        // Create input data to pass the FCM token to the worker

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val inputData = Data.Builder()
            .putString("fcm_token", fcmToken)
            .build()

        // Create a work request to send the FCM token
        val sendTokenWorkRequest = OneTimeWorkRequestBuilder<HandleFcmTokenWorker>()
            .setInputData(inputData)
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,   // Retry with a fixed backoff
                10,
                TimeUnit.SECONDS
            ).build()

        // Enqueue the work


        WorkManager.getInstance(application.applicationContext)
            .enqueueUniqueWork(
                SEND_FCM_TOKEN_WORKER,          // Unique work name
                ExistingWorkPolicy.REPLACE, // Replace existing work with the same name
                sendTokenWorkRequest
            )

    }


    fun cancelSendFCMTokenToServerUniqueWork(application: Application) {
        WorkManager.getInstance(application).cancelUniqueWork(SEND_FCM_TOKEN_WORKER)
    }

}