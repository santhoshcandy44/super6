package com.lts360.compose.ui.auth.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.lts360.App
import com.lts360.api.auth.AuthClient
import com.lts360.api.auth.models.LogInResponse
import com.lts360.api.common.errors.ErrorResponse
import com.lts360.api.common.responses.ResponseReply
import com.lts360.api.utils.Result
import com.lts360.api.utils.mapExceptionToError
import com.lts360.api.auth.services.AuthService
import com.lts360.components.utils.LogUtils.TAG
import com.lts360.compose.ui.auth.AccountType
import com.lts360.compose.ui.auth.navhost.AuthScreen
import com.lts360.compose.ui.auth.repos.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.InjectedParam

@KoinViewModel
class RegisterEmailOTPVerificationViewModel(
    val applicationContext: Context,
    authRepository: AuthRepository,
    @InjectedParam val args : AuthScreen.RegisterEmailOtpVerification
) : EmailOTPVerificationViewModel(authRepository) {

    val accountType: AccountType = args.accountType
    val firstName: String = args.firstName
    val lastName: String = args.lastName
    val email: String = args.email
    val password: String = args.password


    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading


    private var verificationEmailErrorMessage = ""


    fun onResendOtp(email: String, onSuccess: () -> Unit, onError: (String) -> Unit) {

        super.onRegisterReSendEmailVerificationOTP(email,{
            onSuccess() }) { onError(it) }

    }

    fun onVerifyEmail(
        otp: String,
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        accountType: AccountType,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {


        viewModelScope.launch {
            _loading.value = true
            try {

                when (val result = verifyEmail(
                    otp = otp,
                    firstName = firstName,
                    lastName = lastName,
                    email = email,
                    password = password,
                    accountType = accountType
                )) {
                    is Result.Success -> {

                        val data = Gson().fromJson(result.data.data, LogInResponse::class.java)
                        (applicationContext.applicationContext as App).setIsInvalidSession(false)
                        withContext(Dispatchers.IO) {
                            Log.e(TAG,"${data.boards}")
                            authRepository.boardDao.clearAndInsertSelectedBoards(data.boards)
                            authRepository.updateProfileIfNeeded(data.userDetails)
                        }
                        authRepository.saveUserId(data.userId)
                        authRepository.saveEmailSignInInfo(data.accessToken, data.refreshToken)
                        onSuccess()
                    }

                    is Result.Error -> {
                        verificationEmailErrorMessage =
                            mapExceptionToError(result.error).errorMessage
                        onError(verificationEmailErrorMessage)
                    }

                }
            } catch (t: Throwable) {
                t.printStackTrace()
                verificationEmailErrorMessage = "Something Went Wrong"
                onError(verificationEmailErrorMessage)

            } finally {
                _loading.value = false

            }


        }


    }


    private suspend fun verifyEmail(
        otp: String,
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        accountType: AccountType,
    ): Result<ResponseReply> {

        return try {
            val response = AuthClient.instance.create(AuthService::class.java).verifyEmail(
                otp = otp,
                firstName = firstName,
                lastName = lastName,
                email = email,
                password = password,
                accountType = accountType.name
            )

            if (response.isSuccessful) {
                val verificationResponse = response.body()

                if (verificationResponse != null) {

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