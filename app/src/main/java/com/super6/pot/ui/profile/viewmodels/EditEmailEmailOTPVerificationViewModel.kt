package com.super6.pot.ui.profile.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.super6.pot.api.app.AppClient
import com.super6.pot.api.common.errors.ErrorResponse
import com.super6.pot.api.common.responses.ResponseReply
import com.super6.pot.ui.auth.repos.AuthRepository
import com.super6.pot.ui.auth.viewmodels.EmailOTPVerificationViewModel
import com.super6.pot.ui.main.navhosts.routes.EditEmailOtpVerification
import com.super6.pot.ui.managers.UserSharedPreferencesManager
import com.super6.pot.api.Utils.Result
import com.super6.pot.api.Utils.mapExceptionToError
import com.super6.pot.api.app.ProfileSettingsService
import com.super6.pot.app.database.daos.profile.UserProfileDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditEmailEmailOTPVerificationViewModel @Inject constructor(
    private val userProfileDao: UserProfileDao,
    savedStateHandle: SavedStateHandle,
    authRepository: AuthRepository,
) : EmailOTPVerificationViewModel(authRepository){


    private val args = savedStateHandle.toRoute<EditEmailOtpVerification>()
    val userId: Long = UserSharedPreferencesManager.userId

    val email: String = args.email


    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading


    private var verificationEmailErrorMessage = ""


    fun onResendOtp(userId: Long,email: String,onSuccess: () -> Unit,onError: (String) -> Unit){
        super.onEditEmailReSendEmailVerificationOTPValidUser(
            userId,
            email,{onSuccess()}){onError(it)}
    }


    fun onVerifyEmailEditEmail(
        userId: Long,
        email: String,
        otp: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
    ) {


        viewModelScope.launch {



            try {
                _loading.value = true
                when (val result = verifyEmailEditEmail(userId,email,otp)) { // Call the network function
                    is Result.Success -> {
                        val data=Gson().fromJson(result.data.data,JsonObject::class.java)

                        val accessToken= data.get("access_token").asString
                        val refreshToken= data.get("refresh_token").asString

                        val updatedEmail = data.get("email").asString
                        val updatedAt = data.get("updated_at").asString

                        authRepository.saveEmailSignInInfo(accessToken, refreshToken)
                        userProfileDao.updateEmail(userId, updatedEmail, updatedAt)

                        onSuccess(result.data.message)
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
                t.printStackTrace()
                verificationEmailErrorMessage = "Something Went Wrong"
                onError(verificationEmailErrorMessage)
            }finally {
                _loading.value = false
            }


        }


    }


    private suspend fun verifyEmailEditEmail(
        userId: Long,
        email: String,
        otp: String,
    ): Result<ResponseReply> {

        return try {
            val response =  AppClient.instance.create(ProfileSettingsService::class.java).editEmailEmailVerification(
                    userId,
                    email,
                    otp )

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
                val errorMessage= try {
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