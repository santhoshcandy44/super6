package com.lts360.compose.ui.auth.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.lts360.api.utils.Result
import com.lts360.api.utils.mapExceptionToError
import com.lts360.api.app.AccountSettingsService
import com.lts360.api.app.AppClient
import com.lts360.api.common.errors.ErrorResponse
import com.lts360.api.common.responses.ResponseReply
import com.lts360.app.database.daos.profile.UserProfileDao
import com.lts360.compose.ui.auth.AccountType
import com.lts360.compose.ui.main.navhosts.routes.AccountAndProfileSettingsRoutes
import com.lts360.compose.ui.managers.UserSharedPreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.InjectedParam
import kotlin.coroutines.cancellation.CancellationException

@KoinViewModel
class SwitchAccountTypeViewModel(
    private val userProfileDao: UserProfileDao,
    @InjectedParam val args : AccountAndProfileSettingsRoutes.SwitchAccountType
) : ViewModel() {

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