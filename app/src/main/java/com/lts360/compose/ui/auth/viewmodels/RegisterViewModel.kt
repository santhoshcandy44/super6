package com.lts360.compose.ui.auth.viewmodels

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.gson.Gson
import com.lts360.App
import com.lts360.api.auth.AuthClient
import com.lts360.api.auth.models.LogInResponse
import com.lts360.api.common.errors.ErrorResponse
import com.lts360.api.common.responses.ResponseReply
import com.lts360.api.utils.Result
import com.lts360.api.utils.mapExceptionToError
import com.lts360.api.auth.services.AuthService
import com.lts360.compose.ui.auth.navhost.AuthScreen
import com.lts360.compose.ui.auth.repos.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    @ApplicationContext val context:Context,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {


    private val args = savedStateHandle.toRoute<AuthScreen.Register>()
    val accountType = args.accountType

    // State management using StateFlow
    private val _firstName = MutableStateFlow("")
    val firstName = _firstName.asStateFlow()

    private val _lastName = MutableStateFlow("")
    val lastName = _lastName.asStateFlow()

    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword = _confirmPassword.asStateFlow()

    private val _isTermsAccepted = MutableStateFlow(false)
    val isTermsAccepted = _isTermsAccepted.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()


    // StateFlow properties for error messages
    private val _firstNameError = MutableStateFlow<String?>(null)
    val firstNameError = _firstNameError.asStateFlow()

    private val _lastNameError = MutableStateFlow<String?>(null)
    val lastNameError = _lastNameError.asStateFlow()

    private val _emailError = MutableStateFlow<String?>(null)
    val emailError = _emailError.asStateFlow()

    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError = _passwordError.asStateFlow()

    private val _confirmPasswordError = MutableStateFlow<String?>(null)
    val confirmPasswordError = _confirmPasswordError.asStateFlow()

    private val _termsError = MutableStateFlow<String?>(null)
    val termsError = _termsError.asStateFlow()


    private var errorMessage: String = ""

    private var gsoErrorMessage: String = ""


    // Validate all fields
    fun validateFields(): Boolean {
        var isValid = true

        // Validate First Name
        if (_firstName.value.isEmpty()) {
            _firstNameError.value = "First name is required"
            isValid = false
        } else {
            _firstNameError.value = null // Clear error if valid
        }

        // Validate Last Name
        if (_lastName.value.isEmpty()) {
            _lastNameError.value = "Last name is required"
            isValid = false
        } else {
            _lastNameError.value = null // Clear error if valid
        }

        // Validate Email
        if (_email.value.isEmpty()) {
            _emailError.value = "Email is required"
            isValid = false
        } else if (!isValidEmail(_email.value)) {
            _emailError.value = "Invalid email format"
            isValid = false
        } else {
            _emailError.value = null // Clear error if valid
        }

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

        // Validate Terms and Conditions
        if (!_isTermsAccepted.value) {
            _termsError.value = "You must accept the terms and conditions"
            isValid = false
        } else {
            _termsError.value = null // Clear error if valid
        }

        return isValid
    }


    // Helper function to validate email format
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }


    fun onFirstNameChanged(value: String) {
        _firstName.value = value
        clearError("firstName")
    }

    fun onLastNameChanged(value: String) {
        _lastName.value = value
        clearError("lastName")
    }

    fun onEmailChanged(value: String) {
        _email.value = value
        clearError("email")
    }

    fun onPasswordChanged(value: String) {
        _password.value = value
        clearError("password")
    }

    fun onConfirmPasswordChanged(value: String) {
        _confirmPassword.value = value
        clearError("confirmPassword")
    }

    fun onTermsAcceptedChanged(isAccepted: Boolean) {
        _isTermsAccepted.value = isAccepted
        clearError("termsAccepted")
    }


    private fun clearError(field: String) {
        when (field) {
            "firstName" -> _firstNameError.value = null
            "lastName" -> _lastNameError.value = null
            "email" -> _emailError.value = null
            "password" -> _passwordError.value = null
            "confirmPassword" -> _confirmPasswordError.value = null
            "termsAccepted" -> _termsError.value = null
        }
    }

    fun setLoading(isLoading:Boolean){
        _isLoading.value = isLoading
    }


    fun onLegacyEmailSignUp(email: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {

            try {
                _isLoading.value = true
                when (val result = sendEmailVerification(email)) { // Call the network function
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

            } catch (t: Throwable) {
                t.printStackTrace()
                errorMessage = "Something Went Wrong"
                onError(errorMessage)
            }
            finally {
                _isLoading.value = false // Reset loading state
            }
        }

    }


    fun onGoogleSignUp(
        idToken: String,
        accountType: String,
        onSuccess: () -> Unit, onError: (String) -> Unit,
    ) {
        viewModelScope.launch {

            try {
                _isLoading.value = true
                when (val result =googleSignUp(idToken, accountType)) { // Call the network function
                    is Result.Success -> {
                        val data = Gson().fromJson(result.data.data, LogInResponse::class.java)
                        (context.applicationContext as App).setIsInvalidSession(false)
                        authRepository.saveGoogleSignInInfo(data.accessToken,data.refreshToken)
                        authRepository.saveUserId(data.userId)
                        withContext(Dispatchers.IO) {
                            authRepository.boardDao.clearAndInsertSelectedBoards(data.boards)
                            authRepository.updateProfileIfNeeded(data.userDetails)
                        }
                        onSuccess()

                        // Handle success
                    }

                    is Result.Error -> {
                        errorMessage = mapExceptionToError(result.error).errorMessage
                        onError(errorMessage)
                        // Handle the error and update the UI accordingly
                    }

                }

            } catch (t: Throwable) {
                t.printStackTrace()
                errorMessage = "Something Went Wrong"
                onError(errorMessage)
            }
            finally {
                _isLoading.value = false // Reset loading state
            }
        }
    }


    // Function to make the network call and return the result
    private suspend fun sendEmailVerification(email: String): Result<ResponseReply> {
        return try {
            val response =
                AuthClient.instance.create(AuthService::class.java).sendEmailVerificationOTP(email)
            if (response.isSuccessful) {
                val loginResponse = response.body()
                if (loginResponse != null && loginResponse.isSuccessful) {
                    Result.Success(loginResponse)
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

    fun onGoogleSignUpOAuth(
        context: Context,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch {
            authRepository.googleSignInOAuth(context, onSuccess, onError)
        }
    }

    // Method to handle registration
    private suspend fun googleSignUp(idToken: String, accountType: String): Result<ResponseReply> {

        return try {
            val response = AuthClient.instance.create(AuthService::class.java)
                .googleSignUpRegister(idToken, accountType, "google")

            if (response.isSuccessful) {
                // Handle successful response
                val body = response.body()


                if (body != null && body.isSuccessful) {


                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                    try {
                        FirebaseAuth.getInstance().signInWithCredential(firebaseCredential)
                            .await()
                        Result.Success(body)
                    } catch (e: Exception) {
                        Result.Error(Exception("Failed to log in try again"))
                    }

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
