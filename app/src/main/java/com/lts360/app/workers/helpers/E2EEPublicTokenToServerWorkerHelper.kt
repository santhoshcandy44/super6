package com.lts360.app.workers.helpers

import android.app.Application
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.lts360.app.workers.e2ee.HandleE2EEPublicKey
import java.util.concurrent.TimeUnit

object E2EEPublicTokenToServerWorkerHelper {


    private const val SEND_E2EE_PUBLIC_KEY_WORKER = "send_e2ee_public_token"

    fun enqueueSendE2EEPublicKey(application: Application) {

        WorkManager.getInstance(application.applicationContext)
            .enqueueUniqueWork(
                SEND_E2EE_PUBLIC_KEY_WORKER,
                ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequestBuilder<HandleE2EEPublicKey>()
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


    fun cancelSendE2EEPublicTokenToServerUniqueWork(application: Application) {
        WorkManager.getInstance(application).cancelUniqueWork(SEND_E2EE_PUBLIC_KEY_WORKER)
    }

    fun forceEnqueueSendE2EEPublicTokenToServerUniqueWork(application: Application) {
        cancelSendE2EEPublicTokenToServerUniqueWork(application)
        enqueueSendE2EEPublicKey(application)
    }
}