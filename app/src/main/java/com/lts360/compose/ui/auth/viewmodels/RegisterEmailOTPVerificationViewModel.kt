package com.lts360.compose.ui.auth.viewmodels

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.google.gson.Gson
import com.lts360.App
import com.lts360.api.auth.AuthClient
import com.lts360.api.auth.models.LogInResponse
import com.lts360.api.common.errors.ErrorResponse
import com.lts360.api.common.responses.ResponseReply
import com.lts360.api.Utils.Result
import com.lts360.api.Utils.mapExceptionToError
import com.lts360.api.auth.services.AuthService
import com.lts360.compose.ui.auth.AccountType
import com.lts360.compose.ui.auth.navhost.AuthScreen
import com.lts360.compose.ui.auth.repos.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterEmailOTPVerificationViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle,
) : EmailOTPVerificationViewModel(authRepository) {

    private val args = savedStateHandle.toRoute<AuthScreen.RegisterEmailOtpVerification>()


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
                )) { // Call the network function
                    is Result.Success -> {

                        val data = Gson().fromJson(result.data.data, LogInResponse::class.java)
                        (context.applicationContext as App).setIsInvalidSession(false)

                        authRepository.updateProfileIfNeeded(data.userDetails)
                        authRepository.saveUserId(data.userId)
                        authRepository.saveEmailSignInInfo(data.accessToken, data.refreshToken)
                        onSuccess()
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