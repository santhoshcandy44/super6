package com.lts360.compose.ui.profile.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.lts360.api.app.AppClient
import com.lts360.api.common.errors.ErrorResponse
import com.lts360.api.common.responses.ResponseReply
import com.lts360.api.utils.Result
import com.lts360.api.utils.mapExceptionToError
import com.lts360.api.app.ProfileSettingsService
import com.lts360.app.database.daos.profile.UserProfileDao
import com.lts360.compose.ui.managers.UserSharedPreferencesManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import javax.inject.Inject


class EditFirstNameViewModel (
    private val userProfileDao: UserProfileDao
) : ViewModel() {

    val userId: Long = UserSharedPreferencesManager.userId


    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading


    // State management using StateFlow
    private val _firstName = MutableStateFlow("")
    val firstName: StateFlow<String> = _firstName


    // StateFlow properties for error messages
    private val _firstNameError = MutableStateFlow<String?>(null)
    val firstNameError: StateFlow<String?> get() = _firstNameError


    private var errorMessage: String = ""


    init {
        viewModelScope.launch(Dispatchers.IO){
            userProfileDao.getFirstNameFlow(userId).filterNotNull().collect {
                _firstName.value = it
            }
        }
    }

    // Validate all fields
    fun validateFirstName(): Boolean {
        var isValid = true

        // Validate First Name
        if (_firstName.value.isEmpty()) {
            _firstNameError.value = "First name is required"
            isValid = false
        }else if(_firstName.value.length >70){
            _firstNameError.value = "First name exceeds maximum length of 70 characters"
            isValid = false
        } else{
            _firstNameError.value = null // Clear error if valid
        }

        return isValid
    }


    fun onFirstNameChanged(value: String) {
        _firstName.value = value
        clearError()
    }


    private fun clearError() {
        _firstNameError.value = null
    }

    fun onUpdateFirstName(
        userId: Long,
        newFirstName: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
    ) {

        viewModelScope.launch {


            try {
                _isLoading.value = true
                when (val result =
                    updateFirstName(userId, newFirstName)) { // Call the network function
                    is Result.Success -> {


                        val gsonData = Gson().fromJson(result.data.data, JsonObject::class.java)
                        val firstName = gsonData.get("first_name").asString
                        val updatedAt = gsonData.get("updated_at").asString

                        userProfileDao.updateFirstName(userId, firstName, updatedAt)

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
                errorMessage = "Something Went Wrong"
                onError(errorMessage)
                t.printStackTrace()

            } finally {
                _isLoading.value = false // Reset loading state
            }

        }


    }


    private suspend fun updateFirstName(
        userId: Long,
        newFirstName: String,
    ): Result<ResponseReply> {

        return try {
            // Assume RetrofitClient has a suspend function for updateFirstName
            val response = AppClient.instance.create(ProfileSettingsService::class.java)
                .updateFirstName(userId, newFirstName)

            // Handle response and update the database as needed
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
                    e.printStackTrace()
                    "An unknown error occurred"
                }
                Result.Error(Exception(errorMessage))
            }
        } catch (t: Throwable) {
            Result.Error(t)
        }
    }
}
