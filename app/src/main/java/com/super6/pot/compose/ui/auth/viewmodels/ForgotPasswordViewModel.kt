package com.super6.pot.compose.ui.auth.viewmodels

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.super6.pot.api.auth.AuthClient
import com.super6.pot.api.common.errors.ErrorResponse
import com.super6.pot.api.common.responses.ResponseReply
import com.super6.pot.components.utils.LogUtils.TAG
import com.super6.pot.api.Utils.Result
import com.super6.pot.api.Utils.mapExceptionToError
import com.super6.pot.api.auth.services.AuthService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor() : ViewModel() {


    private val _loadingState = MutableStateFlow(false)
    val loadingState: StateFlow<Boolean> = _loadingState.asStateFlow()

    private val _emailError = MutableStateFlow<String?>(null)
    val emailError: StateFlow<String?> = _emailError.asStateFlow()


    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email


    private var errorMessage: String = ""


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


    fun onValidateEmailForgotPassword(
        email: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {

            try {
                _loadingState.value = true
                when (val result = validateEmailForgotPassword(email)) { // Call the network function
                    is Result.Success -> {
                        onSuccess(email)
                        // Handle success
                        // Proceed to next step or navigate to OTP screen
                    }
                    is Result.Error -> {
                        errorMessage = mapExceptionToError(result.error).errorMessage
                        onError(errorMessage)
                        // Handle the error and update the UI accordingly
                    }
                }
            }catch (t:Throwable){
                t.printStackTrace()
            }finally {
                _loadingState.value = false // Reset loading state
            }

        }


    }


    private suspend fun validateEmailForgotPassword(
        email: String
    ): Result<ResponseReply> {

       return try {
            val response=AuthClient.instance.create(AuthService::class.java).
            validateEmailForgotPassword(email)

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
                val errorMessage= try {
                    Gson().fromJson(errorBody, ErrorResponse::class.java).message
                } catch (e: Exception) {
                    "An unknown error occurred"
                }
                Result.Error(Exception(errorMessage))
            }

        }catch (t:Throwable){
         Result.Error(t)
        }

    }



    private fun isValidEmail(target: CharSequence?): Boolean {
        return target != null && Patterns.EMAIL_ADDRESS.matcher(target).matches()
    }

}
