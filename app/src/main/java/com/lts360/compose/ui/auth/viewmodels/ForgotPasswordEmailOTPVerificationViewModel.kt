package com.lts360.compose.ui.auth.viewmodels

import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.lts360.api.app.AppClient
import com.lts360.api.auth.AuthClient
import com.lts360.api.common.errors.ErrorResponse
import com.lts360.api.common.responses.ResponseReply
import com.lts360.api.utils.Result
import com.lts360.api.utils.mapExceptionToError
import com.lts360.api.app.AccountSettingsService
import com.lts360.api.auth.services.AuthService
import com.lts360.compose.ui.auth.navhost.AuthScreen
import com.lts360.compose.ui.auth.repos.AuthRepository
import com.lts360.compose.ui.managers.UserSharedPreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.InjectedParam


@KoinViewModel
class ForgotPasswordEmailOTPVerificationViewModel(
    authRepository: AuthRepository,
    @InjectedParam val args:AuthScreen.ForgotPasswordEmailOtpVerification,
) : EmailOTPVerificationViewModel(authRepository) {

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
                } catch (_: Exception) {
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
                } catch (_: Exception) {
                    "An unknown error occurred"
                }
                Result.Error(Exception(errorMessage))

            }
        } catch (t: Throwable) {
            Result.Error(t)


        }

    }


}