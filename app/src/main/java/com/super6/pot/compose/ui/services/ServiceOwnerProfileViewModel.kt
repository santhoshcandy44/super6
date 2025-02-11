package com.super6.pot.compose.ui.services

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.google.gson.Gson
import com.super6.pot.api.Utils.Result
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
import com.super6.pot.compose.ui.main.navhosts.routes.ServiceOwnerProfile
import com.super6.pot.compose.ui.main.navhosts.routes.UserProfileSerializer
import com.super6.pot.compose.ui.managers.UserSharedPreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class ServiceOwnerProfileViewModel @Inject constructor(
    @ApplicationContext
    context: Context,
    val savedStateHandle: SavedStateHandle,
    private val chatUserDao: ChatUserDao,
    val chatUserRepository: ChatUserRepository,
    tokenManager: TokenManager,
) : ViewModel() {


    val arg = savedStateHandle.toRoute<ServiceOwnerProfile>()
    private val serviceOwnerId = arg.serviceOwnerId


    val userId = UserSharedPreferencesManager.userId
    val signInMethod = tokenManager.getSignInMethod()

    /*    private val _isLoading = MutableStateFlow(true)
        val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()*/

    private var error: String = ""
    /*
        private val _services = MutableStateFlow<List<Service>>(emptyList())
        val services = _services.asStateFlow()*/

    private val _selectedItem = MutableStateFlow<Service?>(null)
    val selectedItem: StateFlow<Service?> = _selectedItem.asStateFlow()


    var _selectedChatId = MutableStateFlow<Int>(-1)
    var selectedChatId = _selectedChatId.asStateFlow()


    var _selectedRecipientId = MutableStateFlow<Long>(-1)
    var selectedRecipientId = _selectedRecipientId.asStateFlow()


    var _userProfileInfo = MutableStateFlow<FeedUserProfileInfo?>(null)
    var userProfileInfo = _userProfileInfo.asStateFlow()


    init {

        savedStateHandle.get<Int>("chatId")?.let {
            _selectedChatId.value = it
        }
        savedStateHandle.get<Long>("selectedRecipientId")?.let {
            _selectedRecipientId.value = it
        }

        savedStateHandle.get<String>("userProfileInfo")?.let {
            _userProfileInfo.value = UserProfileSerializer.deserializeFeedUserProfile(it)
        }

        /*


                val data = savedStateHandle.get<String>("selected_item")
                    ?.let { ServicesSerializer.deserializeService(it) }

                data?.let {
                    _selectedItem.value = it
                }
        */


    }


    fun onRemoveBookmark(
        userId: Long,
        service: Service,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch {
            try {
                when (val result = removeBookmark(userId, service)) {
                    is Result.Success -> {
                        // Deserialize the search terms and set suggestions
                        onSuccess()
                    }

                    is Result.Error -> {
                        // Handle error
                        error = mapExceptionToError(result.error).errorMessage
                        onError(error)
                        // Optionally log the error message
                    }
                }
            } catch (t: Exception) {
                error = "Something Went Wrong"
                onError(error)

            }
        }
    }


    fun directUpdateServiceIsBookMarked(serviceId: Long, isBookMarked: Boolean) {
        _selectedItem.update { currentItem ->
            // Proceed only if currentItem is not null
            currentItem?.let {
                // Check if any change is needed
                val updatedServices = it.createdServices?.map { service ->
                    if (service.serviceId == serviceId) {
                        // If the isBookmarked value is already the same, return the service as is
                        if (service.isBookmarked == isBookMarked) {
                            return@map service // No change needed, return original
                        }
                        // Return the updated service with the new isBookmarked value
                        return@map service.copy(isBookmarked = isBookMarked)
                    } else {
                        // Return the service as is if the IDs don't match
                        return@map service
                    }
                }

                // Only return updated currentItem if the services have changed
                // If no services changed, return the currentItem as is
                if (updatedServices != it.createdServices) {
                    it.copy(createdServices = updatedServices)
                } else {
                    it // No change to createdServices, so return it as is
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
                Result.Error(Exception(errorMessage))
            }

        } catch (t: Exception) {
            Result.Error(t)
        }
    }


    fun onBookmark(
        userId: Long,
        service: Service,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch {
            try {
                when (val result = bookmark(userId, service)) {
                    is Result.Success -> {
                        // Deserialize the search terms and set suggestions
                        onSuccess()
                    }

                    is Result.Error -> {
                        // Handle error
                        error = mapExceptionToError(result.error).errorMessage
                        onError(error)
                        // Optionally log the error message
                    }
                }
            } catch (t: Exception) {
                error = "Something Went Wrong"
                onError(error)

            }
        }
    }


    private suspend fun bookmark(
        userId: Long,
        item: Service,
    ): Result<ResponseReply> {
        return try {
            val response = AppClient.instance.create(ManageServicesApiService::class.java)
                .bookmarkService(
                    userId,
                    item.serviceId
                )

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
                Result.Error(Exception(errorMessage))
            }
        } catch (t: Throwable) {
            Result.Error(t)
        }

    }

    suspend fun getChatUser(userId: Long, userProfile: FeedUserProfileInfo): ChatUser {

        return withContext(Dispatchers.IO) {
            // If chat user exists, update selected values
            chatUserDao.getChatUserByRecipientId(userProfile.userId)?.let {
                updateSavedState(it.chatId, userId, userProfile)
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
                updateSavedState(chatId, userId, userProfile)
                newChatUser.copy(chatId = chatId)
            }
        }

    }


    // Helper function to update the saved state
    private fun updateSavedState(chatId: Int, recipientId: Long, userProfile: FeedUserProfileInfo) {
        _selectedChatId.value = chatId
        _selectedRecipientId.value = recipientId
        _userProfileInfo.value = userProfile

        savedStateHandle["chatId"] = chatId
        savedStateHandle["selectedRecipientId"] = recipientId
        savedStateHandle["userProfileInfo"] =
            UserProfileSerializer.serializeFeedUserProfileInfo(userProfile)
    }


    fun setSelectedItem(service: Service?) {
        _selectedItem.value = service
        service?.let {
            savedStateHandle["selected_item"] = service.serviceId
        }

    }


    /*


        private fun onGetPublishedServices(
            signInMethod: String,
            userId: Long,
            onSuccess: () -> Unit,
            onError: (Throwable) -> Unit,
        ) {

            viewModelScope.launch {

                _isLoading.value = true
                try {
                    when (val result =
                        fetchPublishedServices(signInMethod, userId)) { // Call the network function
                        is Result.Success -> {

                            val data = Gson().fromJson(
                                result.data.data,
                                object : TypeToken<List<Service>>() {}.type
                            )
                                    as List<Service>
                            _services.value = data

                            onSuccess()
                        }

                        is Result.Error -> {
                            onError(result.error)
                        }
                    }
                } catch (t: Throwable) {
                    t.printStackTrace()
                    onError(Exception("Something Went Wrong"))
                } finally {
                    _isLoading.value = false
                }

            }

        }
    */


    // Function to fetch services from the API
    /*  private suspend fun fetchPublishedServices(
          signInMethod: String,
          userId: Long,
      ): Result<ResponseReply> {


          return try {
              val response = if (signInMethod == "guest") {
                  AppClient.instance.create(ManageServicesApiService::class.java)
                      .getServicesByFeedGuestUserId(userId)
              } else {
                  AppClient.instance.create(ManageServicesApiService::class.java)
                      .getServicesByFeedUserId(userId)
              }

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
                  Result.Error(Exception(errorMessage))
              }
          } catch (t: Throwable) {
              t.printStackTrace()
              Result.Error(t)
          }
      }*/

}