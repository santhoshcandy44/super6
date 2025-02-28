package com.lts360.compose.ui.main.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.lts360.api.Utils.Result
import com.lts360.api.Utils.mapExceptionToError
import com.lts360.api.app.AppClient
import com.lts360.api.app.ManageUsedProductListingService
import com.lts360.api.auth.managers.TokenManager
import com.lts360.api.common.errors.ErrorResponse
import com.lts360.api.common.responses.ResponseReply
import com.lts360.api.models.service.FeedUserProfileInfo
import com.lts360.api.models.service.GuestIndustryDao
import com.lts360.api.models.service.UsedProductListing
import com.lts360.app.database.daos.chat.ChatUserDao
import com.lts360.app.database.daos.profile.UserLocationDao
import com.lts360.app.database.daos.profile.UserProfileDao
import com.lts360.app.database.models.chat.ChatUser
import com.lts360.compose.ui.chat.repos.ChatUserRepository
import com.lts360.compose.ui.managers.NetworkConnectivityManager
import com.lts360.compose.ui.managers.UserSharedPreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class SecondsViewmodel @Inject constructor(
    val savedStateHandle: SavedStateHandle,
    val userProfileDao: UserProfileDao,
    private val guestIndustryDao: GuestIndustryDao,
    private val guestUserLocationDao: UserLocationDao,
    val chatUserDao: ChatUserDao,
    val tokenManager: TokenManager,
    val chatUserRepository: ChatUserRepository,
    networkConnectivityManager: NetworkConnectivityManager,
) : ViewModel() {

    val submittedQuery = savedStateHandle.get<String?>("submittedQuery")
    val onlySearchBar = savedStateHandle.get<Boolean>("onlySearchBar") ?: false
    private val key =  savedStateHandle.get<Int>("key") ?: 0

    val userId = UserSharedPreferencesManager.userId

    val connectivityManager = networkConnectivityManager


    private val _selectedItem = MutableStateFlow<UsedProductListing?>(null)
    val selectedItem = _selectedItem.asStateFlow()


    private val _lastLoadedItemPosition = MutableStateFlow(-1)
    val lastLoadedItemPosition = _lastLoadedItemPosition.asStateFlow()


    private var error: String = ""



    private var _pageSource = SecondsPageSource(savedStateHandle) // Initialize PageSource
    val pageSource: SecondsPageSource get() = _pageSource // Expose pageSource as read-only

    val signInMethod = tokenManager.getSignInMethod()
    val isValidSignInMethodFeaturesEnabled = tokenManager.isValidSignInMethodFeaturesEnabled()


    private var loadingItemsJob: Job? = null


    init {

        if (signInMethod == "guest") {
            viewModelScope.launch {

                launch {


                    val userLocation = guestUserLocationDao.getLocation(userId)

                    if (userLocation != null) {
                        pageSource.guestNextPage(
                            userId,
                            submittedQuery,
                            userLocation.latitude,
                            userLocation.longitude
                        )
                    } else {
                        pageSource.guestNextPage(
                            userId,
                            submittedQuery)
                    }


                }.join()


            }
        } else {


               loadingItemsJob = viewModelScope.launch {

                   launch {
                       pageSource.nextPage(userId, submittedQuery)
                   }.join()

               }
        }
    }


    fun getKey() = key


    // Method to update the position
    fun updateLastLoadedItemPosition(newPosition: Int) {
        viewModelScope.launch {
            _lastLoadedItemPosition.value = newPosition
        }
    }


    // Load next page
    fun nextPage(userId: Long, query: String?) {

        if (signInMethod == "guest") {
            viewModelScope.launch {
                val userLocation = guestUserLocationDao.getLocation(userId)

                if (userLocation != null) {
                    pageSource.guestNextPage(
                        userId, query, userLocation.latitude,
                        userLocation.longitude
                    )
                } else {
                    pageSource.guestNextPage(userId, query)
                }
            }
        } else {
            loadingItemsJob = viewModelScope.launch {
                pageSource.nextPage(userId, query)
            }
        }

    }


    // Refresh data
    fun refresh(userId: Long, query: String?) {
        if (signInMethod == "guest") {
            viewModelScope.launch {
                val userLocation = guestUserLocationDao.getLocation(userId)
                val selectedIndustries = if (onlySearchBar) {
                    emptyList()
                } else {
                    guestIndustryDao.getSelectedIndustries().map { it.industryId }
                }

                if (userLocation != null) {
                    pageSource.guestRefresh(
                        userId,
                        query,
                        userLocation.latitude,
                        userLocation.longitude
                    )

                } else {
                    pageSource.guestRefresh(userId, query)
                }
            }
        } else {

            loadingItemsJob?.let {
                it.cancel()
                it.invokeOnCompletion {
                    loadingItemsJob = viewModelScope.launch {
                        pageSource.refresh(userId, query)
                    }
                }
            } ?: run {
                loadingItemsJob = viewModelScope.launch {
                    pageSource.refresh(userId, query)
                }
            }

        }
    }

    // Retry loading more data
    fun retry(userId: Long, query: String?) {
        if (signInMethod == "guest") {
            viewModelScope.launch {
                val userLocation = guestUserLocationDao.getLocation(userId)

                val selectedIndustries = if (onlySearchBar) {
                    emptyList()
                } else {
                    guestIndustryDao.getSelectedIndustries().map { it.industryId }
                }

                if (userLocation != null) {
                    pageSource.guestRetry(
                        userId,
                        query,
                        selectedIndustries,
                        userLocation.latitude,
                        userLocation.longitude
                    )
                } else {
                    pageSource.guestRetry(userId, query, selectedIndustries)
                }

            }
        } else {
            viewModelScope.launch {
                pageSource.retry(userId, query)
            }
        }
    }



    fun setSelectedItem(item: UsedProductListing?) {
        _selectedItem.value = item
        /*item?.let {
            savedStateHandle["selected_item"] = item.serviceId
        }*/
    }


    fun directUpdateServiceIsBookMarked(serviceId: Long, isBookMarked: Boolean) {
        _pageSource.updateServiceBookMarkedInfo(serviceId, isBookMarked)
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
        service: UsedProductListing,
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
            chatUserDao.getChatUserByRecipientId(userProfile.userId) ?: run {
                val newChatUser = ChatUser(
                    userId = userId,
                    recipientId = userProfile.userId,
                    timestamp = System.currentTimeMillis(),
                    userProfile = userProfile.copy(isOnline = false)
                )
                newChatUser.copy(chatId = chatUserDao.insertChatUser(newChatUser).toInt())
            }

        }
    }


}







