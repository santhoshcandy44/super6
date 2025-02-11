package com.super6.pot.compose.ui.profile.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.super6.pot.api.Utils.Result
import com.super6.pot.api.Utils.mapExceptionToError
import com.super6.pot.api.app.AppClient
import com.super6.pot.api.app.ProfileSettingsService
import com.super6.pot.api.common.errors.ErrorResponse
import com.super6.pot.api.common.responses.ResponseReply
import com.super6.pot.app.database.daos.profile.UserProfileDao
import com.super6.pot.app.database.models.profile.UserProfile
import com.super6.pot.compose.ui.managers.UserSharedPreferencesManager
import com.super6.pot.compose.ui.utils.FormatterUtils.isValidEmail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException


@HiltViewModel
class EditEmailViewModel @Inject constructor(
    private val userProfileDao: UserProfileDao) : ViewModel() {

    // Retrieve the argument from the navigation
    val userId: Long = UserSharedPreferencesManager.userId

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading


    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile

    private val _emailError = MutableStateFlow<String?>(null)
    val emailError: StateFlow<String?> = _emailError.asStateFlow()


    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email


    private var errorMessage: String = ""

    init {
        viewModelScope.launch(Dispatchers.IO){
            userProfileDao.getEmailFlow(userId).filterNotNull().collect {
                _email.value = it
            }
        }
    }

    // Separate email validation function
    fun validateEmail(email: String): Boolean {
        return when {
            email.isEmpty() -> {
                _emailError.value = "Email can't be empty"
                false
            }

            !isValidEmail(email) -> {
                _emailError.value = "Invalid email address"
                false
            }

            else -> {
                _emailError.value = null
                true
            }
        }
    }


    fun onEmailChanged(value: String) {
        _email.value = value
        clearEmailError()
    }

    private fun clearEmailError() {
        _emailError.value = null
    }



    fun onChangeEmailValidate(
        userId: Long,
        email: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
    ) {

        viewModelScope.launch {

            try {
                _isLoading.value = true
                when (val result =
                    changeEmailValidate(userId, email)) { // Call the network function
                    is Result.Success -> {

                        onSuccess(result.data.message)
                        // Handle success
                        // Proceed to next step or navigate to OTP screen
                    }

                    is Result.Error -> {
                        if(result.error is CancellationException){
                            return@launch
                        }
                        errorMessage = mapExceptionToError(result.error).errorMessage
                        onError(errorMessage)
                        // Handle the error and update the UI accordingly
                    }

                }
            } catch (t: Throwable) {
                t.printStackTrace()
                errorMessage = "Something Went Wrong"
                onError(errorMessage)
            } finally {

                _isLoading.value = false // Reset loading state

            }
        }


    }


    private suspend fun changeEmailValidate(
        userId: Long,
        email: String,
    ): Result<ResponseReply> {

        return try {
            val response = AppClient.instance.create(ProfileSettingsService::class.java)
                .changeEmailValidate(userId, email)

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

