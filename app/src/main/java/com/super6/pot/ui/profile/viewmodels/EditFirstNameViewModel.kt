package com.super6.pot.ui.profile.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.super6.pot.api.app.AppClient
import com.super6.pot.api.common.errors.ErrorResponse
import com.super6.pot.api.common.responses.ResponseReply
import com.super6.pot.ui.managers.UserSharedPreferencesManager
import com.super6.pot.api.Utils.Result
import com.super6.pot.api.Utils.mapExceptionToError
import com.super6.pot.api.app.ProfileSettingsService
import com.super6.pot.app.database.daos.profile.UserProfileDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class EditFirstNameViewModel @Inject constructor(
    private val userProfileDao: UserProfileDao) : ViewModel() {

    // Retrieve the argument from the navigation
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
