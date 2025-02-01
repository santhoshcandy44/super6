package com.super6.pot.ui.auth.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.super6.pot.api.app.AppClient
import com.super6.pot.api.common.errors.ErrorResponse
import com.super6.pot.api.common.responses.ResponseReply
import com.super6.pot.app.database.daos.profile.UserProfileDao
import com.super6.pot.ui.auth.AccountType
import com.super6.pot.ui.main.navhosts.routes.SwitchAccountType
import com.super6.pot.ui.managers.UserSharedPreferencesManager
import com.super6.pot.api.Utils.Result
import com.super6.pot.api.Utils.mapExceptionToError
import com.super6.pot.api.app.AccountSettingsService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class SwitchAccountTypeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val userProfileDao: UserProfileDao,
) : ViewModel() {

    private val args = savedStateHandle.toRoute<SwitchAccountType>()

    val accountType = args.accountType

    val userId = UserSharedPreferencesManager.userId


    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private var errorMessage: String = ""


    fun onSwitchAccountType(
        userId: Long,
        accountType: AccountType,
        onSuccess: (String, String) -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch {

            try {
                _isLoading.value = true

                when (val result = switchAccountType(userId, accountType)) {
                    is Result.Success -> {

                        val gsonData = Gson().fromJson(result.data.data, JsonObject::class.java)
                        val updatedAccountType = gsonData.get("account_type").asString
                        val updatedAt = gsonData.get("updated_at").asString

                        userProfileDao.updateAccountType(userId, updatedAccountType, updatedAt)

                        onSuccess(
                            updatedAccountType,
                            result.data.message
                        )  // Proceed to next step or navigate to OTP screen
                    }

                    is Result.Error -> {
                        if (result.error is CancellationException) {
                            return@launch
                        }
                        errorMessage = mapExceptionToError(result.error).errorMessage
                        onError(errorMessage)
                    }
                }

            } catch (t: Throwable) {
                t.printStackTrace()
                errorMessage = "Something Went Wrong"
                onError(errorMessage)
            } finally {
                _isLoading.value = false
            }


        }
    }


    // Method to handle registration
    private suspend fun switchAccountType(
        userId: Long,
        accountType: AccountType,

        ): Result<ResponseReply> {


        return try {
            val response = AppClient.instance.create(AccountSettingsService::class.java)
                .updateAccountType(userId, accountType.name)

            if (response.isSuccessful) {
                // Handle successful response
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