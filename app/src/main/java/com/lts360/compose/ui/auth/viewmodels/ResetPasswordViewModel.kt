package com.lts360.compose.ui.auth.viewmodels


import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.google.gson.Gson
import com.lts360.api.Utils.Result
import com.lts360.api.Utils.mapExceptionToError
import com.lts360.api.app.AccountSettingsService
import com.lts360.api.app.AppClient
import com.lts360.api.auth.AuthClient
import com.lts360.api.auth.services.AuthService
import com.lts360.api.common.errors.ErrorResponse
import com.lts360.api.common.responses.ResponseReply
import com.lts360.compose.ui.auth.navhost.AuthScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResetPasswordViewModel @Inject constructor(savedStateHandle: SavedStateHandle): ViewModel() {


    private val args=savedStateHandle.toRoute<AuthScreen.ResetPassword>()

    val accessToken: String = args.accessToken
    val email: String = args.email


    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword

    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError: StateFlow<String?> get() = _passwordError

    private val _confirmPasswordError = MutableStateFlow<String?>(null)
    val confirmPasswordError: StateFlow<String?> get() = _confirmPasswordError


    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading



    private var errorMessage: String = ""



    fun validatePasswords():Boolean{

        var isValid = true

        // Validate Password
        if (_password.value.isEmpty()) {
            _passwordError.value = "Password is required"
            isValid = false
        } else if (_password.value.length < 8) {
            _passwordError.value = "Password must be at least 8 characters"
            isValid = false
        } else {
            _passwordError.value = null // Clear error if valid
        }

        // Validate Confirm Password
        if (_confirmPassword.value.isEmpty()) {
            _confirmPasswordError.value = "Confirm password is required"
            isValid = false
        } else if (_password.value != _confirmPassword.value) {
            _confirmPasswordError.value = "Passwords do not match"
            isValid = false
        } else {
            _confirmPasswordError.value = null // Clear error if valid
        }

        return isValid
    }

    fun onPasswordChanged(value: String) {
        _password.value = value
        clearError("password")
    }

    fun onConfirmPasswordChanged(value: String) {
        _confirmPassword.value = value
        clearError("confirmPassword")
    }


    private fun clearError(field: String) {
        when (field) {

            "password" -> _passwordError.value = null
            "confirmPassword" -> _confirmPasswordError.value = null
        }
    }

    fun onResetPassword(
        accessToken: String,
        email:String,
        newPassword:String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit) {

        viewModelScope.launch {

            try {
                _isLoading.value = true
                when (val result = resetPassword(accessToken,email,newPassword)) { // Call the network function
                    is Result.Success -> {
                        onSuccess()
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
                errorMessage = "Something Went Wrong"
                onError(errorMessage)
                t.printStackTrace()
            }finally {
                _isLoading.value = false // Reset loading state
            }

        }
    }


    private suspend fun resetPassword(accessToken:String,email:String, newPassword:String): Result<ResponseReply> {

        // Create the Bearer token
        val authToken = "Bearer ${accessToken}"

        return try {
            val response = AuthClient.instance.create(AuthService::class.java).resetPassword(
                authToken,
                email,
                newPassword)

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



    fun onResetPasswordProtected(
        userId: Long,
        accessToken: String,
        email:String,
        newPassword:String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit) {

        viewModelScope.launch {
            try {
                _isLoading.value = true
                when (val result = resetPasswordProtected(
                    userId,
                    accessToken,email,newPassword)) { // Call the network function
                    is Result.Success -> {

                        onSuccess(result.data.message)
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
                errorMessage = "Something Went Wrong"
                onError(errorMessage)
                t.printStackTrace()
            }finally {
                _isLoading.value = false // Reset loading state

            }




        }



    }


    private suspend fun resetPasswordProtected(
        userId:Long, accessToken:String, email:String, newPassword:String): Result<ResponseReply> {

        // Create the Bearer token
        val authToken = "Bearer ${accessToken}"

        return try {
            val response = AppClient.instance.create(AccountSettingsService::class.java).resetPassword(
                userId,
                authToken,
                email,
                newPassword)

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

}
