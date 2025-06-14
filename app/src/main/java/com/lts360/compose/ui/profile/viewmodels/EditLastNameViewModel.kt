package com.lts360.compose.ui.profile.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.lts360.api.utils.Result
import com.lts360.api.utils.mapExceptionToError
import com.lts360.api.app.AppClient
import com.lts360.api.app.ProfileSettingsService
import com.lts360.api.common.errors.ErrorResponse
import com.lts360.api.common.responses.ResponseReply
import com.lts360.app.database.daos.profile.UserProfileDao
import com.lts360.compose.ui.managers.UserSharedPreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException


class EditLastNameViewModel(
    private val userProfileDao: UserProfileDao
) : ViewModel() {

    val userId: Long = UserSharedPreferencesManager.userId

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // State management using StateFlow
    private val _lastName = MutableStateFlow("")
    val lastName: StateFlow<String> = _lastName

    // StateFlow properties for error messages
    private val _lastNameError = MutableStateFlow<String?>(null)
    val lastNameError: StateFlow<String?> get() = _lastNameError

    private var errorMessage: String = ""

    init {
        viewModelScope.launch(Dispatchers.IO){
            userProfileDao.getLastNameFlow(userId).filterNotNull().collect {
                _lastName.value = it
            }
        }
    }

    // Validate last name field
    fun validateLastName(): Boolean {
        var isValid = true

        // Validate Last Name
        if (_lastName.value.isEmpty()) {
            _lastNameError.value = "Last name is required"
            isValid = false
        }else if(_lastName.value.length >70){
            _lastNameError.value = "Last name exceeds maximum length of 50 characters"
            isValid = false
        } else {
            _lastNameError.value = null // Clear error if valid
        }

        return isValid
    }

    fun onLastNameChanged(value: String) {
        _lastName.value = value
        clearError()
    }

    private fun clearError() {
        _lastNameError.value = null

    }



    fun onUpdateLastName(
        userId: Long,
        newLastName: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                when (val result = updateLastName(userId, newLastName)) { // Call the network function
                    is Result.Success -> {
                        val gsonData = Gson().fromJson(result.data.data, JsonObject::class.java)
                        val lastName = gsonData.get("last_name").asString
                        val updatedAt = gsonData.get("updated_at").asString

                        userProfileDao.updateLastName(userId, lastName, updatedAt)

                        onSuccess(result.data.message)
                    }

                    is Result.Error -> {
                        if(result.error is CancellationException){
                            return@launch
                        }
                        errorMessage = mapExceptionToError(result.error).errorMessage
                        onError(errorMessage)
                    }
                }
            }catch (t:Throwable){
                t.printStackTrace()
                errorMessage = "Something Went Wrong"
                onError(errorMessage)
            }finally {
                _isLoading.value = false // Reset loading state
            }

        }
    }

    private suspend fun updateLastName(
        userId: Long,
        newLastName: String
    ): Result<ResponseReply> {
        return try {
            val response = AppClient.instance.create(ProfileSettingsService::class.java)
                .updateLastName(userId, newLastName) // Assume there's an API for updating the last name

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
                Result.Error(Exception(errorMessage))            }
        } catch (t: Throwable) {
            Result.Error(t)
        }
    }
}
