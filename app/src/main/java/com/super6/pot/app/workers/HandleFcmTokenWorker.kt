package com.super6.pot.app.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.super6.pot.api.app.ApiService
import com.super6.pot.api.app.AppClient
import com.super6.pot.ui.managers.UserSharedPreferencesManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject


@HiltWorker
class HandleFcmTokenWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object{

        private val TAG="SUPER6_APP_FCM"
    }

    override suspend fun doWork(): Result {

        val fcmToken = inputData.getString("fcm_token") ?: return Result.failure()

        return   try {
            // Call the API to update the FCM token
            val response = AppClient.instance.create(ApiService::class.java).updateFcmToken(fcmToken)

            if (response.isSuccessful) {
                val body=response.body()
                if (body!=null && body.isSuccessful){
                    // Clear the token from local storage after successful update
                    UserSharedPreferencesManager.removeFcmToken()
                    UserSharedPreferencesManager.fcmTokenStatus = true
                    Log.d(TAG, "Token successfully sent to server")
                    Result.success()  // Indicate that the work was successful
                }else{
                    Result.retry()  // Indicate that the work was successful
                }
            } else {
                Log.w(TAG, "Server responded with error: ${response.code()}")

                if(response.code()==403){
                    Result.failure()
                }else{
                    Result.retry()  // Indicate that the work should be retried
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending token to server", e)
            Result.retry()  // Retry in case of failure (e.g., network issue)
        }
    }
}

