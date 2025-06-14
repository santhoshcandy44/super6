package com.lts360.compose.ui.main.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.lts360.App
import com.lts360.api.utils.Result
import com.lts360.api.utils.mapExceptionToError
import com.lts360.api.app.AppClient
import com.lts360.api.app.ProfileService
import com.lts360.api.auth.managers.TokenManager
import com.lts360.api.common.errors.ErrorResponse
import com.lts360.api.common.responses.ResponseReply
import com.lts360.compose.ui.auth.AccountType
import com.lts360.compose.ui.main.navhosts.routes.AccountAndProfileSettingsRoutes
import com.lts360.compose.ui.managers.UserSharedPreferencesManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.InjectedParam

@KoinViewModel
class AccountAndProfileSettingsViewModel(
    val tokenManager: TokenManager,
    @InjectedParam val args: AccountAndProfileSettingsRoutes.AccountAndProfileSettings
) : ViewModel() {

    val userId = UserSharedPreferencesManager.userId

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var error: String = ""


    private val _accountType = MutableStateFlow(
        when (args.accountType) {
            AccountType.Personal.name -> AccountType.Personal
            AccountType.Business.name -> AccountType.Business
            else -> AccountType.Personal
        }
    )

    val accountType = _accountType.asStateFlow()

    // Function to update the account type
    fun setAccountType(accountTypeString: String) {
        _accountType.value = when (accountTypeString) {
            AccountType.Personal.name -> AccountType.Personal
            AccountType.Business.name -> AccountType.Business
            else -> throw IllegalArgumentException("Invalid account type")
        }
    }


    fun logOutAccount(context: Context) {
        viewModelScope.launch {
            ((context.applicationContext) as App).logout(tokenManager.getSignInMethod())
        }
    }

    fun onLogout(userId: Long, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        _isLoading.value = true
        viewModelScope.launch {

            try {
                when (val result = logout(userId)) {
                    is Result.Success -> {
                        onSuccess(result.data.message)
                    }

                    is Result.Error -> {
                        if (result.error is CancellationException) {
                            return@launch
                        }

                        error = mapExceptionToError(result.error).errorMessage
                        onError(error)
                    }
                }
            } catch (t: Throwable) {
                t.printStackTrace()
                error = "Something went wrong"
                onError(error)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun logout(userId: Long): Result<ResponseReply> {

        return try {
            val response = AppClient.instance.create(ProfileService::class.java)
                .logout(userId)
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

}
