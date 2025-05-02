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

    private const val SEND_FCM_TOKEN_WORKER = "send_fcm_token"

    fun enqueueSendFCMTokenToServerWork(application: Application, fcmToken: String) {

        WorkManager.getInstance(application.applicationContext)
            .enqueueUniqueWork(
                SEND_FCM_TOKEN_WORKER,
                ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequestBuilder<HandleFcmTokenWorker>()
                    .setInputData(
                        Data.Builder()
                            .putString("fcm_token", fcmToken)
                            .build()

                    )
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build()
                    )
                    .setBackoffCriteria(
                        BackoffPolicy.LINEAR,
                        10,
                        TimeUnit.SECONDS
                    ).build()
            )

    }


    fun cancelSendFCMTokenToServerUniqueWork(application: Application) {
        WorkManager.getInstance(application).cancelUniqueWork(SEND_FCM_TOKEN_WORKER)
    }

    fun forceEnqueueSendFCMTokenToServerUniqueWork(application: Application, fcmToken: String) {
        WorkManager.getInstance(application).cancelUniqueWork(SEND_FCM_TOKEN_WORKER)
        enqueueSendFCMTokenToServerWork(application, fcmToken)
    }

}