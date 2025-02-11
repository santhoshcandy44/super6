package com.super6.pot.compose.ui.services.bookmark

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.super6.pot.api.Utils.Result
import com.super6.pot.api.Utils.ResultError
import com.super6.pot.api.Utils.mapExceptionToError
import com.super6.pot.api.app.AppClient
import com.super6.pot.api.app.ManageServicesApiService
import com.super6.pot.api.auth.managers.TokenManager
import com.super6.pot.api.common.errors.ErrorResponse
import com.super6.pot.api.common.responses.ResponseReply
import com.super6.pot.api.models.service.FeedUserProfileInfo
import com.super6.pot.api.models.service.Service
import com.super6.pot.app.database.daos.chat.ChatUserDao
import com.super6.pot.app.database.models.chat.ChatUser
import com.super6.pot.compose.ui.chat.repos.ChatUserRepository
import com.super6.pot.compose.ui.managers.UserSharedPreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class BookmarkedServicesViewModel @Inject constructor(
    val savedStateHandle: SavedStateHandle,
    val tokenManager: TokenManager,
    private val chatUserDao: ChatUserDao,
    val chatUserRepository: ChatUserRepository,

    ) : ViewModel() {

    val userId = UserSharedPreferencesManager.userId

    val signInMethod = tokenManager.getSignInMethod()


    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()


    private val _error = MutableStateFlow<ResultError?>(null)
    val error = _error.asStateFlow()

    private var errorMessage: String = ""

    private val _services = MutableStateFlow(emptyList<Service>())
    val services = _services.asStateFlow()


    private val _selectedItem = MutableStateFlow<Service?>(null)
    val selectedItem: StateFlow<Service?> get() = _selectedItem


    private val _nestedServiceOwnerProfileSelectedItem = MutableStateFlow<Service?>(null)
    val nestedServiceOwnerProfileSelectedItem = _nestedServiceOwnerProfileSelectedItem.asStateFlow()


    init {


        viewModelScope.launch {
            fetchBookmarkedServices(userId, onSuccess = {}) {}
        }

    }



    fun setNestedServiceOwnerProfileSelectedItem(service: Service?) {
        _nestedServiceOwnerProfileSelectedItem.value = service
        /*  service?.let {
              savedStateHandle["nested_service_owner_profile_selected_item"] = service.serviceId
          }*/

    }

    // Function to update the error message
    fun updateError(exception: Throwable?) {

        _error.value = exception?.let {
            mapExceptionToError(it)
        }
    }


    fun setSelectedItem(service: Service?) {
        _selectedItem.value = service
    }


    // Function to update the error message
    fun removeItem(service: Service) {

        _services.value = _services.value.filter {
            it.serviceId != service.serviceId
        }

    }

    // Function to update the error message
    fun updateItem(updateService: Service, isBookmarked: Boolean) {
        val index = _services.value.indexOfFirst { it.serviceId == updateService.serviceId }

        // Check if the service exists
        if (index != -1) {
            val updatedService = _services.value[index].copy(
                isBookmarked = isBookmarked
            )

            // Create a new list with the updated service
            _services.value = _services.value.mapIndexed { i, service ->
                if (i == index) updatedService else service
            }
        }
    }


    suspend fun getChatUser(userId: Long, userProfile: FeedUserProfileInfo): ChatUser {


        return withContext(Dispatchers.IO) {
            val chatUser = chatUserDao.getChatUserByRecipientId(userProfile.userId)

            // If chat user exists, update selected values
            chatUser?.let {
                it
            } ?: run {
                // Insert new chat user and then update the state
                val newChatUser = ChatUser(
                    userId = userId,
                    recipientId = userProfile.userId,
                    timestamp = System.currentTimeMillis(),
                    userProfile = userProfile.copy(isOnline = false)
                )
                val chatId = chatUserDao.insertChatUser(newChatUser).toInt() // New chat ID
                newChatUser.copy(chatId = chatId)
            }
        }

    }


     fun onFetchBookmarkedServices(
        userId: Long,
        isLoading: Boolean = true,
        isRefreshing: Boolean = false,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
    ){
        viewModelScope.launch {
            fetchBookmarkedServices(userId, isLoading, isRefreshing, onSuccess, onError)
        }
    }

   private suspend fun fetchBookmarkedServices(
        userId: Long,
        isLoading: Boolean = true,
        isRefreshing: Boolean = false,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
    ) {


        try {
            if (isLoading) {
                _isLoading.value = true

            }
            if (isRefreshing) {
                _isRefreshing.value = true
            }

            when (val result = fetchBookmarkedServices(userId)) {
                is Result.Success -> {

                    _services.value = Gson().fromJson(
                        result.data.data,
                        object : TypeToken<List<Service>>() {}.type
                    )
                    onSuccess(result.data.message)
                    updateError(null)

                }

                is Result.Error -> {
                    if (result.error is CancellationException) {
                        return
                    }
                    updateError(result.error)
                    errorMessage = mapExceptionToError(result.error).errorMessage
                    onError(errorMessage)
                }
            }

        } catch (t: Throwable) {
            t.printStackTrace()
            errorMessage = "Something Went Wrong"
            onError(errorMessage)
        } finally {
            if (isLoading) {
                _isLoading.value = false

            }
            if (isRefreshing) {
                _isRefreshing.value = false
            }
        }

    }


    fun onRemoveBookmark(
        userId: Long,
        item: Service,
        onSuccess: () -> Unit,
        onError: () -> Unit,
    ) {

        viewModelScope.launch {

            when (removeBookmark(userId, item)) {
                is Result.Success -> {
                    onSuccess()
                }

                is Result.Error -> {
                    onError()
                }
            }
        }
    }


    private suspend fun removeBookmark(
        userId: Long,
        item: Service,
    ): Result<ResponseReply> {

        return try {
            val response = AppClient.instance.create(ManageServicesApiService::class.java)
                .removeBookmarkService(
                    userId,
                    item.serviceId
                )
            val body = response.body()

            if (response.isSuccessful) {
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

        } catch (t: Exception) {
            Result.Error(t)
        }
    }


    // Method to handle registration
    private suspend fun fetchBookmarkedServices(
        userId: Long,
    ): Result<ResponseReply> {


        return try {
            val response = AppClient.instance.create(ManageServicesApiService::class.java)
                .getBookmarkedServices(userId)

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
            t.printStackTrace()
            Result.Error(t)
        }
    }


}

