package com.super6.pot.app.workers

import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.super6.pot.api.app.ApiService
import com.super6.pot.api.app.AppClient
import com.super6.pot.api.auth.models.PublicKeyRequest
import com.super6.pot.ui.auth.repos.generateRSAKeyPair
import com.super6.pot.ui.managers.UserSharedPreferencesManager
import java.security.PublicKey
import java.util.Base64

class HandleE2EEPublicKey(appContext: android.content.Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    companion object {
        private val TAG = "SUPER6_APP_E2EEPublicKey"
    }


    override suspend fun doWork(): Result {

//
//        when (val result = getE2EEPublicKey(-1)) {
//            is com.super6.pot.utils.Result.Success -> {
//
//
//                val data = Gson().fromJson<JSONObject>(result.data.data, JSONObject::class.java)
//
//                val publicKey = data.getString("public_key")
//                val keyVersion = data.getString("key_version")
//
//
//                publicKey?.let {
//
//                    if(keyVersion!=null){
//
//                    }else{
//                        sendPublicKeyToServer()
//                    }
//                } ?: run {
//                    sendPublicKeyToServer()
//                }
//
//
//            }
//
//            is com.super6.pot.utils.Result.Error -> {
//
//            }
//        }

        // Retrieve the public key
        val keyPair = generateRSAKeyPair() // Or load the existing key pair from Keystore
        val publicKey: PublicKey = keyPair.public

        // Convert the public key to a Base64 string for sending it to the server
        val encodedPublicKey = Base64.getEncoder().encodeToString(publicKey.encoded)

        // Send the public key to the server (for example, via an API call)
        return sendPublicKeyToServer(encodedPublicKey, System.currentTimeMillis())
    }

/*
    private suspend fun getE2EEPublicKey(userId: Long): com.super6.pot.utils.Result<ResponseReply> {

        return try {
            // Call the API to update the FCM token
            val response = AppClient.instance.create(ApiService::class.java)
                .getE2EEPublicToken(userId)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.isSuccessful) {
                    com.super6.pot.utils.Result.Success(body)

                } else {
                    val errorMessage = "Failed, try again later..."
                    com.super6.pot.utils.Result.Error(Exception(errorMessage))
                }


            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = try {
                    Gson().fromJson(errorBody, ErrorResponse::class.java).message
                } catch (e: Exception) {
                    "An unknown error occurred"
                }
                if (response.code() == 403) {
                    com.super6.pot.utils.Result.Error(ForceFailureException("Forced to cancel task"))

                } else {
                    com.super6.pot.utils.Result.Error(Exception(errorMessage))
                }


            }
        } catch (e: Exception) {
            com.super6.pot.utils.Result.Error(e)

        }
    }
*/


    private suspend fun sendPublicKeyToServer(publicKey: String, keyVersion: Long): Result {

        return try {
            // Call the API to update the FCM token
            val response = AppClient.instance.create(ApiService::class.java)
                .updateE2EEPublicToken(PublicKeyRequest(publicKey,keyVersion))

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.isSuccessful) {
                    // Clear the token from local storage after successful update
                    UserSharedPreferencesManager.removeE2EEPublicToken()
                    UserSharedPreferencesManager.removeE2EEKeyVersion()

                    UserSharedPreferencesManager.E2EEPublicTokenStatus = true
                    UserSharedPreferencesManager.E2EELatestKeyVersion = keyVersion

                    val latestKeyVersion=
                    Log.d(TAG, "E2EE public Key successfully sent to server")
                    Result.success()  // Indicate that the work was successful
                } else {
                    Result.retry()  // Indicate that the work was successful
                }
            } else {
                Log.w(TAG, "Server responded with error: ${response.code()}")
                if (response.code() == 403) {
                    Result.failure()
                } else {
                    Result.retry()  // Indicate that the work should be retried
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending E2EE public token to server", e)
            Result.retry()  // Retry in case of failure (e.g., network issue)
        }
    }
}