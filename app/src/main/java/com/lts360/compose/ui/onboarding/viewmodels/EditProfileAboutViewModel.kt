package com.lts360.compose.ui.onboarding.viewmodels

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
import com.lts360.compose.ui.onboarding.navhost.OnBoardingScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.InjectedParam
import kotlin.coroutines.cancellation.CancellationException

@KoinViewModel
class EditProfileAboutViewModel (
    private val userProfileDao: UserProfileDao,
    @InjectedParam val args:OnBoardingScreen.CompleteAbout
) : ViewModel() {

    val userId = UserSharedPreferencesManager.userId
    val type: String? = args.type

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // State management using StateFlow for "About" instead of last name
    private val _about = MutableStateFlow("")
    val about: StateFlow<String> = _about

    // StateFlow properties for error messages
    private val _aboutError = MutableStateFlow<String?>(null)
    val aboutError: StateFlow<String?> get() = _aboutError

    private var errorMessage: String = ""

    init {
        viewModelScope.launch(Dispatchers.IO){
            userProfileDao.getAboutFlow(userId).filterNotNull().collect {
                _about.value = it
            }
        }
    }

    // Validate about field
    fun validateAbout(): Boolean {
        var isValid = true

        // Validate About
        if (_about.value.isEmpty()) {
            _aboutError.value = "About information is required"
            isValid = false
        }
        else if (_about.value.length>160) {
            _aboutError.value = "About exceeds maximum length of 160 characters"
            isValid = false
        }
        else {
            _aboutError.value = null // Clear error if valid
        }

        return isValid
    }

    fun onAboutChanged(value: String) {
        _about.value = value
        clearError()
    }

    private fun clearError() {
        _aboutError.value = null
    }

    fun onUpdateAbout(
        userId: Long,
        newAbout: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch {

            try {
                _isLoading.value = true

                when (val result = updateAbout(userId, newAbout)) { // Call the network function
                    is Result.Success -> {

                        val gsonData = Gson().fromJson(result.data.data, JsonObject::class.java)
                        val about = gsonData.get("about").asString
                        val updatedAt = gsonData.get("updated_at").asString
                        userProfileDao.updateAbout(userId, about, updatedAt)

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
                errorMessage = "Something Went Wrong"
                onError(errorMessage)
                t.printStackTrace()
            }finally {
                _isLoading.value = false // Reset loading state
            }


        }
    }

    private suspend fun updateAbout(
        userId: Long,
        newAbout: String
    ): Result<ResponseReply> {
        return try {
            val response = AppClient.instance.create(ProfileSettingsService::class.java)
                .updateAbout(userId, newAbout) // Assume there's an API for updating the about information

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
        } catch (t: Throwable) {
            Result.Error(t)
        }
    }

}
