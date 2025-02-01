package com.super6.pot.ui.main.viewmodels

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.google.gson.Gson
import com.super6.pot.App
import com.super6.pot.api.app.AppClient
import com.super6.pot.api.auth.managers.TokenManager
import com.super6.pot.api.common.errors.ErrorResponse
import com.super6.pot.api.common.responses.ResponseReply
import com.super6.pot.ui.auth.AccountType
import com.super6.pot.ui.main.navhosts.routes.AccountAndProfileSettings
import com.super6.pot.ui.managers.UserSharedPreferencesManager
import com.super6.pot.api.Utils.Result
import com.super6.pot.api.Utils.mapExceptionToError
import com.super6.pot.api.app.ProfileService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class  AccountAndProfileSettingsViewModel @Inject constructor(savedStateHandle: SavedStateHandle,
                                                              val tokenManager: TokenManager
): ViewModel(){



    val userId = UserSharedPreferencesManager.userId

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()



    // Retrieve arguments from SavedStateHandle
    private val args = savedStateHandle.toRoute<AccountAndProfileSettings>()



    private var error: String = ""


    // Initialize MutableStateFlow with a default value from args
    private val _accountType = MutableStateFlow(
        when (args.accountType) {
            AccountType.Personal.name -> AccountType.Personal
            AccountType.Business.name -> AccountType.Business
            else -> AccountType.Personal // or any default value you choose
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




    fun logOutAccount(context:Context){
        viewModelScope.launch {
            ((context.applicationContext) as App).logout(tokenManager.getSignInMethod())
        }
    }

    fun onLogout(userId: Long, onSuccess:(String)-> Unit, onError:(String) -> Unit){
        _isLoading.value=true
        viewModelScope.launch {

            try {
                when(val result = logout(userId)){
                    is Result.Success ->{
                        onSuccess(result.data.message)
                    }
                    is Result.Error -> {
                        if(result.error is CancellationException){
                            return@launch
                        }

                        error = mapExceptionToError(result.error).errorMessage
                        onError(error)
                    }
                }
            }catch (t:Throwable){
                t.printStackTrace()
                error = "Something went wrong"
                onError(error)
            }finally {
                _isLoading.value=false
            }
        }
    }

    private suspend fun logout(userId: Long): Result<ResponseReply> {

        return  try {
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
