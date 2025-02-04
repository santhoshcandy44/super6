package com.super6.pot.ui.auth.viewmodels

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.super6.pot.api.app.AppClient
import com.super6.pot.api.auth.AuthClient
import com.super6.pot.api.common.errors.ErrorResponse
import com.super6.pot.api.common.responses.ResponseReply
import com.super6.pot.ui.auth.repos.AuthRepository
import com.super6.pot.ui.auth.navhost.AuthScreen
import com.super6.pot.ui.managers.UserSharedPreferencesManager
import com.super6.pot.utils.LogUtils.TAG
import com.super6.pot.api.Utils.Result
import com.super6.pot.api.Utils.mapExceptionToError
import com.super6.pot.api.app.AccountSettingsService
import com.super6.pot.api.auth.services.AuthService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordEmailOTPVerificationViewModel @Inject constructor(
    authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle,
) : EmailOTPVerificationViewModel(authRepository) {


    val args = savedStateHandle.toRoute<AuthScreen.ForgotPasswordEmailOtpVerification>()
    val email: String = args.email

    val userId = UserSharedPreferencesManager.userId

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading


    private var verificationEmailErrorMessage = ""


    fun onResendOtp(email: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        super.onForgotPasswordReSendEmailOtpVerification(email, { onSuccess() }) { onError(it) }
    }


    fun onProtectedResendOtp(
        userId: Long,
        email: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        super.onProtectedForgotPasswordReSendEmailOtpVerificationValidUser(
            userId,
            email,
            { onSuccess() }) { onError(it) }
    }


    fun onVerifyEmailForgotPassword(
        otp: String,
        onSuccess: (String, String) -> Unit,
        onError: (String) -> Unit,
    ) {

        viewModelScope.launch {

            try {
                _loading.value = true
                when (val result = verifyEmailForgotPassword(otp)) { // Call the network function
                    is Result.Success -> {
                        val data = Gson().fromJson(result.data.data, JsonObject::class.java)
                        onSuccess(data.get("email").asString, data.get("access_token").asString)
                        // Handle success
                        // Proceed to next step or navigate to OTP screen
                    }

                    is Result.Error -> {
                        verificationEmailErrorMessage =
                            mapExceptionToError(result.error).errorMessage
                        onError(verificationEmailErrorMessage)
                        // Handle the error and update the UI accordingly
                    }
                }
            } catch (t: Throwable) {
                t.printStackTrace()
            } finally {
                _loading.value = false
            }
        }
    }


    private suspend fun verifyEmailForgotPassword(otp: String): Result<ResponseReply> {

        return try {
            val response =
                AuthClient.instance.create(AuthService::class.java).verifyEmailForgotPassword(
                    email = email,
                    otp = otp
                )

            if (response.isSuccessful) {
                val verificationResponse = response.body()

                if (verificationResponse != null && verificationResponse.isSuccessful) {

                    Result.Success(verificationResponse)


                } else {
                    val errorMessage = "Failed, try again later..."
                    Result.Error(Exception(errorMessage))

                }
            } else {


                val errorBody = response.errorBody()?.string()
                val errorMessage = try {
                    Gson().fromJson(errorBody, ErrorResponse::class.java).message
                } catch (e: Exception) {
                    "An unknown error occurred"
                }
                Result.Error(Exception(errorMessage))

            }
        } catch (t: Throwable) {
            Result.Error(t)


        }

    }


    fun onProtectedVerifyEmailForgotPassword(
        userId: Long,
        otp: String,
        onSuccess: (String, String) -> Unit,
        onError: (String) -> Unit,
    ) {


        viewModelScope.launch {

            try {
                _loading.value = true
                when (val result =  protectedVerifyEmailForgotPassword(userId,otp)) { // Call the network function
                    is Result.Success -> {
                        val data = Gson().fromJson(result.data.data, JsonObject::class.java)
                        onSuccess(data.get("email").asString, data.get("access_token").asString)
                        // Handle success
                        // Proceed to next step or navigate to OTP screen
                    }

                    is Result.Error -> {
                        verificationEmailErrorMessage = mapExceptionToError(result.error).errorMessage
                        onError(verificationEmailErrorMessage)
                        // Handle the error and update the UI accordingly
                    }
                }
            }catch (t:Throwable){
                verificationEmailErrorMessage = "Something Went Wrong"
                onError(verificationEmailErrorMessage)
                t.printStackTrace()
            }finally {
                _loading.value = false

            }


        }
    }


    private suspend fun protectedVerifyEmailForgotPassword(
        userId: Long,
        otp: String,
    ): Result<ResponseReply> {

        return try {
            val response =
                AppClient.instance.create(AccountSettingsService::class.java).verifyEmailForgotPassword(
                    userId,
                    email = email,
                    otp = otp
                )

            if (response.isSuccessful) {
                val verificationResponse = response.body()

                if (verificationResponse != null && verificationResponse.isSuccessful) {

                    Result.Success(verificationResponse)


                } else {
                    val errorMessage = "Failed, try again later..."
                    Result.Error(Exception(errorMessage))

                }
            } else {


                val errorBody = response.errorBody()?.string()
                val errorMessage = try {
                    Gson().fromJson(errorBody, ErrorResponse::class.java).message
                } catch (e: Exception) {
                    "An unknown error occurred"
                }
                Result.Error(Exception(errorMessage))

            }
        } catch (t: Throwable) {
            Result.Error(t)


        }

    }


}