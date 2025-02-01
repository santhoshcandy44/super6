package com.super6.pot.ui.main.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.super6.pot.api.app.AppClient
import com.super6.pot.api.common.errors.ErrorResponse
import com.super6.pot.api.common.responses.ResponseReply
import com.super6.pot.app.database.daos.profile.UserProfileDao
import com.super6.pot.ui.managers.UserSharedPreferencesManager
import com.super6.pot.api.Utils.Result
import com.super6.pot.api.Utils.mapExceptionToError
import com.super6.pot.api.app.AccountSettingsService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException


@HiltViewModel
class ForgotPasswordProtectedViewModel @Inject constructor(
    userProfileDao: UserProfileDao,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {


    val userId = UserSharedPreferencesManager.userId


    // MutableStateFlow to hold the email value
    private val _email = MutableStateFlow<String>("")
    val email  = _email.asStateFlow()



    private val _loadingState = MutableStateFlow(false)
    val loadingState: StateFlow<Boolean> = _loadingState.asStateFlow()


    private var errorMessage: String = ""


    init {

        viewModelScope.launch {
            withContext(Dispatchers.IO){
                _email.value = userProfileDao.getProfile(userId)?.email ?: ""
            }

            launch {
                userProfileDao.getProfileFlow(userId).collectLatest {
                    it?.let {
                        _email.value = it.email
                    }
                }
            }
        }
    }


    fun onValidateEmailForgotPasswordProtected(
        userId:Long,
        email: String,
        onSuccess: (String,String) -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch {

            try {

                _loadingState.value = true
                when (val result =
                    validateEmailForgotPasswordProtected(userId,email)) { // Call the network function
                    is Result.Success -> {
                        val data = result.data
                        onSuccess(email,data.message)
                        // Handle success
                        // Proceed to next step or navigate to OTP screen
                    }

                    is Result.Error -> {
                        if (result.error is CancellationException) {
                            return@launch
                        }
                        errorMessage = mapExceptionToError(result.error).errorMessage
                        onError(errorMessage)
                        // Handle the error and update the UI accordingly
                    }
                }


            }catch (t:Throwable){
                errorMessage = "Something Went Wrong"
                onError(errorMessage)
                t.printStackTrace()
                _loadingState.value = false // Reset loading state

            }finally {
                _loadingState.value = false // Reset loading state
            }
        }
    }


    private suspend fun validateEmailForgotPasswordProtected(
        userId:Long,
        email: String,
    ): Result<ResponseReply> {

        return try {
            val response = AppClient.instance.create(AccountSettingsService::class.java)
                .validateEmailForgotPassword(userId,email)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.isSuccessful) {
                    Result.Success(body)
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

