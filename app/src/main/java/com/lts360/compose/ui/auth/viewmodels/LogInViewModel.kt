package com.lts360.compose.ui.auth.viewmodels

import android.content.Context
import android.util.Patterns.EMAIL_ADDRESS
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.gson.Gson
import com.lts360.App
import com.lts360.api.utils.Result
import com.lts360.api.utils.mapExceptionToError
import com.lts360.api.auth.AuthClient
import com.lts360.api.auth.models.LogInResponse
import com.lts360.api.auth.services.AuthService
import com.lts360.api.common.errors.ErrorResponse
import com.lts360.api.common.responses.ResponseReply
import com.lts360.app.database.daos.prefs.BoardDao
import com.lts360.compose.ui.auth.repos.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.koin.android.annotation.KoinViewModel
import javax.inject.Inject

@KoinViewModel
class LogInViewModel @Inject constructor(
    val applicationContext: Context,
    val boardDao:BoardDao,
    private val authRepository: AuthRepository,
) : ViewModel() {


    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()


    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()


    private val _emailError = MutableStateFlow<String?>(null)
    val emailError = _emailError.asStateFlow()

    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError = _passwordError.asStateFlow()

    private var errorMessage: String = ""

    private var gsoErrorMessage: String = ""


    fun onEmailChanged(value: String) {
        _email.value = value
        clearError("email")
    }

    fun onPasswordChanged(value: String) {
        _password.value = value
        clearError("password")
    }

    private fun clearError(field: String) {
        when (field) {
            "email" -> _emailError.value = null
            "password" -> _passwordError.value = null

        }
    }


    fun validateFields(): Boolean {
        var isValid = true

        if (_email.value.isEmpty()) {
            _emailError.value = "Email is required"
            isValid = false
        } else if (!isValidEmail(_email.value)) {
            _emailError.value = "Invalid email format"
            isValid = false
        } else {
            _emailError.value = null
        }

        if (_password.value.isEmpty()) {
            _passwordError.value = "Password is required"
            isValid = false
        } else if (_password.value.length < 8) {
            _passwordError.value = "Password must be at least 8 characters"
            isValid = false
        } else {
            _passwordError.value = null
        }


        return isValid
    }


    private fun isValidEmail(email: String): Boolean {
        return EMAIL_ADDRESS.matcher(email).matches()
    }


    fun setLoading(isLoading: Boolean) {
        _isLoading.value = isLoading
    }


    fun onLegacyEmailLogin(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                when (val result = legacyEmailLogin(email, password)) {
                    is Result.Success -> {
                        val data = Gson().fromJson(result.data.data, LogInResponse::class.java)
                        (applicationContext.applicationContext as App).setIsInvalidSession(false)
                        withContext(Dispatchers.IO) {
                            boardDao.clearAndInsertSelectedBoards(data.boards)
                            authRepository.updateProfileIfNeeded(data.userDetails)
                        }
                        authRepository.saveEmailSignInInfo(data.accessToken, data.refreshToken)
                        authRepository.saveUserId(data.userId)
                        onSuccess()
                    }

                    is Result.Error -> {
                        errorMessage = mapExceptionToError(result.error).errorMessage
                        onError(errorMessage)
                    }
                }
            } catch (t: Throwable) {
                t.printStackTrace()
                errorMessage = "Something went wrong"
                onError(errorMessage)
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun onGoogleSignIn(idToken: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        _isLoading.value = true
        viewModelScope.launch {
            try {

                when (val result = googleSignIn(idToken)) {
                    is Result.Success -> {

                        val data = Gson().fromJson(result.data.data, LogInResponse::class.java)
                        (applicationContext.applicationContext as App).setIsInvalidSession(false)
                        withContext(Dispatchers.IO) {
                            boardDao.clearAndInsertSelectedBoards(data.boards)
                            authRepository.updateProfileIfNeeded(data.userDetails)
                        }

                        authRepository.saveGoogleSignInInfo(data.accessToken, data.refreshToken)
                        authRepository.saveUserId(data.userId)

                        onSuccess()
                    }

                    is Result.Error -> {
                        gsoErrorMessage = mapExceptionToError(result.error).errorMessage
                        onError(gsoErrorMessage)
                    }

                }
            } catch (t: Throwable) {
                t.printStackTrace()
                gsoErrorMessage = "Something Went Wrong"
                onError(gsoErrorMessage)
            } finally {
                _isLoading.value = false
            }
        }
    }


    private suspend fun legacyEmailLogin(
        email: String, password: String
    ): Result<ResponseReply> {

        return try {
            val response = AuthClient.instance.create(AuthService::class.java)
                .legacyEmailLogin(email, password)

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
                } catch (_: Exception) {
                    "An unknown error occurred"
                }
                Result.Error(Exception(errorMessage))
            }
        } catch (t: Throwable) {
            Result.Error(t)
        }
    }


    fun onGoogleSignInOAuth(
        context: Context,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
    ) {

        viewModelScope.launch {
            authRepository.googleSignInOAuth(context, onSuccess, onError)
        }
    }

    private suspend fun googleSignIn(idToken: String): Result<ResponseReply> {


        return try {
            val response =
                AuthClient.instance.create(AuthService::class.java).googleLogin(idToken, "google")

            if (response.isSuccessful) {
                val loginResponse = response.body()

                if (loginResponse != null && loginResponse.isSuccessful) {

                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                    try {
                        FirebaseAuth.getInstance().signInWithCredential(firebaseCredential)
                            .await()
                        Result.Success(loginResponse)
                    } catch (_: Exception) {
                        Result.Error(Exception("Failed to log in try again"))
                    }

                } else {
                    Result.Error(Exception("Failed to log in try again"))
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
