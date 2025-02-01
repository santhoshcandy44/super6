package com.super6.pot.app.workers.helpers

import android.app.Application
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.super6.pot.app.workers.HandleE2EEPublicKey
import java.util.concurrent.TimeUnit

object E2EEPublicTokenToServerWorkerHelper {

    private val SEND_E2EE_PUBLIC_KEY_WORKER = "send_e2ee_public_token"

    fun enqueueSendE2EEPublicKey(application: Application) {
        // Create input data to pass the FCM token to the worker

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // Create a work request to send the FCM token
        val sendTokenWorkRequest = OneTimeWorkRequestBuilder<HandleE2EEPublicKey>()
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,   // Retry with a fixed backoff
                10,
                TimeUnit.SECONDS
            ).build()

        // Enqueue the work


        WorkManager.getInstance(application.applicationContext)
            .enqueueUniqueWork(
                SEND_E2EE_PUBLIC_KEY_WORKER,          // Unique work name
                ExistingWorkPolicy.REPLACE, // Replace existing work with the same name
                sendTokenWorkRequest
            )

    }


    fun cancelSendE2EEPublicTokenToServerUniqueWork(application: Application) {
        WorkManager.getInstance(application).cancelUniqueWork(SEND_E2EE_PUBLIC_KEY_WORKER)
    }
}