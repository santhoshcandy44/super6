package com.super6.pot.api.auth.managers

import com.google.gson.Gson
import com.super6.pot.api.auth.AuthClient
import com.super6.pot.api.auth.models.TokenResponse
import com.super6.pot.api.auth.services.AuthService
import com.super6.pot.api.common.responses.ResponseReply
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.Response

object RefreshTokenManager{

    private val mutex = Mutex()

    suspend fun onRefreshAccessToken(
        tokenManager: TokenManager,
        authToken: String,
        criticalListener: CriticalListener,
        retryListener: RetryListener,
    ) {

        mutex.withLock {

            val newToken = tokenManager.getAccessToken()
            // Check if the token has already been refreshed by another thread
            if (authToken == newToken) {

                val refreshToken = tokenManager.getRefreshToken()
                // Call refresh token API to get a new access token

                try {
                    val refreshTokenResponse = refreshAccessToken(refreshToken)
                    if (refreshTokenResponse.isSuccessful) {
                        val body = refreshTokenResponse.body()

                        if (body != null && body.isSuccessful) {
                            val tokenData = Gson().fromJson(body.data, TokenResponse::class.java)

                            val newAccessToken = tokenData.accessToken
                            val newRefreshToken = tokenData.refreshToken

                            tokenManager.saveAccessToken(newAccessToken)
                            tokenManager.saveRefreshToken(newRefreshToken)

                            criticalListener.onSuccess(newAccessToken)

                        } else {
                            criticalListener.onError()
                        }

                    } else {

                        val responseCode = refreshTokenResponse.code()
                        if(responseCode==401 || responseCode==403){
                            criticalListener.onError()
                        }else{
                            criticalListener.onFailed(responseCode)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    criticalListener.onError(e)
                }
            } else {
                retryListener.onRetry(newToken)
            }
        }
    }

    private suspend fun refreshAccessToken(refreshToken: String): Response<ResponseReply> {
        return AuthClient.instance.create(AuthService::class.java).refreshToken("Bearer $refreshToken")
    }

}
