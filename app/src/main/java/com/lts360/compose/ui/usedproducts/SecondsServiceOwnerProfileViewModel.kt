package com.lts360.compose.ui.usedproducts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.lts360.api.utils.Result
import com.lts360.api.utils.mapExceptionToError
import com.lts360.api.app.AppClient
import com.lts360.api.app.ManageUsedProductListingService
import com.lts360.api.auth.managers.TokenManager
import com.lts360.api.common.errors.ErrorResponse
import com.lts360.api.common.responses.ResponseReply
import com.lts360.api.models.service.FeedUserProfileInfo
import com.lts360.api.models.service.UsedProductListing
import com.lts360.app.database.daos.chat.ChatUserDao
import com.lts360.app.database.models.chat.ChatUser
import com.lts360.compose.ui.chat.repos.ChatUserRepository
import com.lts360.compose.ui.managers.UserSharedPreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class SecondsOwnerProfileViewModel @Inject constructor(
    private val chatUserDao: ChatUserDao,
    val chatUserRepository: ChatUserRepository,
    tokenManager: TokenManager,
) : ViewModel() {

    val userId = UserSharedPreferencesManager.userId
    val signInMethod = tokenManager.getSignInMethod()

    private var error: String = ""

    private val _selectedItem = MutableStateFlow<UsedProductListing?>(null)
    val selectedItem: StateFlow<UsedProductListing?> = _selectedItem.asStateFlow()

    fun isSelectedUsedProductListingNull()  = _selectedItem.value==null


    fun directUpdateServiceIsBookMarked(serviceId: Long, isBookMarked: Boolean) {
        _selectedItem.update { currentItem ->
            currentItem?.let {
                val updatedServices = it.createdUsedProductListings?.map { service ->
                    if (service.productId == serviceId) {
                        if (service.isBookmarked == isBookMarked) {
                            return@map service
                        }
                        return@map service.copy(isBookmarked = isBookMarked)
                    } else {
                        return@map service
                    }
                }

                if (updatedServices != it.createdUsedProductListings) {
                    it.copy(createdUsedProductListings = updatedServices)
                } else {
                    it
                }
            }
        }
    }



    fun setSelectedItem(service: UsedProductListing?) {
        _selectedItem.value = service
    }

    fun onRemoveBookmark(
        userId: Long,
        service: UsedProductListing,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch {
            try {
                when (val result = removeBookmark(userId, service)) {
                    is Result.Success -> {
                        onSuccess()
                    }

                    is Result.Error -> {
                        error = mapExceptionToError(result.error).errorMessage
                        onError(error)
                    }
                }
            } catch (_: Exception) {
                error = "Something Went Wrong"
                onError(error)

            }
        }
    }



    private suspend fun removeBookmark(
        userId: Long,
        item: UsedProductListing,
    ): Result<ResponseReply> {


        return try {
            val response = AppClient.instance.create(ManageUsedProductListingService::class.java)
                .removeBookmarkUsedProductListing(
                    userId,
                    item.productId
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
                } catch (_: Exception) {
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
        service: UsedProductListing,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch {
            try {
                when (val result = bookmark(userId, service)) {
                    is Result.Success -> {
                        onSuccess()
                    }

                    is Result.Error -> {
                        error = mapExceptionToError(result.error).errorMessage
                        onError(error)
                    }
                }
            } catch (_: Exception) {
                error = "Something Went Wrong"
                onError(error)

            }
        }
    }


    private suspend fun bookmark(
        userId: Long,
        item: UsedProductListing,
    ): Result<ResponseReply> {
        return try {
            val response = AppClient.instance.create(ManageUsedProductListingService::class.java)
                .bookmarkUsedProductListing(
                    userId,
                    item.productId
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
                } catch (_: Exception) {
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
            chatUserDao.getChatUserByRecipientId(userProfile.userId) ?: run {
                val newChatUser = ChatUser(
                    userId = userId,
                    recipientId = userProfile.userId,
                    timestamp = System.currentTimeMillis(),
                    userProfile = userProfile.copy(isOnline = false)
                )
                val chatId = chatUserDao.insertChatUser(newChatUser).toInt()
                newChatUser.copy(chatId = chatId)
            }
        }

    }

}