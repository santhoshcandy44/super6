package com.lts360.compose.ui.main.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.lts360.api.app.AppClient
import com.lts360.api.app.ManageUsedProductListingService
import com.lts360.api.auth.managers.TokenManager
import com.lts360.api.common.errors.ErrorResponse
import com.lts360.api.common.responses.ResponseReply
import com.lts360.api.models.service.FeedUserProfileInfo
import com.lts360.api.models.service.UsedProductListing
import com.lts360.api.utils.Result
import com.lts360.api.utils.mapExceptionToError
import com.lts360.app.database.daos.chat.ChatUserDao
import com.lts360.app.database.daos.profile.UserLocationDao
import com.lts360.app.database.daos.profile.UserProfileDao
import com.lts360.app.database.models.chat.ChatUser
import com.lts360.compose.ui.chat.repos.ChatUserRepository
import com.lts360.compose.ui.managers.NetworkConnectivityManager
import com.lts360.compose.ui.managers.UserSharedPreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.annotation.KoinViewModel
import javax.inject.Inject


class SecondsRepository @Inject constructor(
    val userProfileDao: UserProfileDao,
    private val guestUserLocationDao: UserLocationDao,
    val chatUserDao: ChatUserDao,
    val chatUserRepository: ChatUserRepository,
    networkConnectivityManager: NetworkConnectivityManager,
) {

    var submittedQuery: String? = null
    var onlySearchBar = false
    private var key = 0

    val connectivityManager = networkConnectivityManager


    private val _selectedItem = MutableStateFlow<UsedProductListing?>(null)
    val selectedItem = _selectedItem.asStateFlow()

    private val _lastLoadedItemPosition = MutableStateFlow(-1)
    val lastLoadedItemPosition = _lastLoadedItemPosition.asStateFlow()


    private var error: String = ""

    private var _pageSource = SecondsPageSource(pageSize = 30)
    val pageSource: SecondsPageSource get() = _pageSource


    private var loadingItemsJob: Job? = null

    private val _nestedSecondsOwnerProfileSelectedItem = MutableStateFlow<UsedProductListing?>(null)
    val nestedSecondsOwnerProfileSelectedItem = _nestedSecondsOwnerProfileSelectedItem.asStateFlow()


    suspend fun loadSeconds(userId:Long, isGuest: Boolean = false) {
        if (isGuest) {
            val userLocation = guestUserLocationDao.getLocation(userId)

            pageSource.guestNextPage(
                userId,
                submittedQuery,
                userLocation?.latitude,
                userLocation?.longitude
            )

        } else {
            pageSource.nextPage(userId, submittedQuery)
        }
    }


    fun updateSubmittedQuery(value: String) {
        submittedQuery = value
    }

    fun updateOnlySearchBar(value: Boolean) {
        onlySearchBar = value
    }

    fun updateKey(value: Int) {
        key = value
    }

    fun getKey(): Int {
        return key
    }

    fun updateSelectedItem(item: UsedProductListing?) {
        _selectedItem.value = item
    }

    fun updateNestedSecondsOwnerProfileSelectedItem(item: UsedProductListing?) {
        _nestedSecondsOwnerProfileSelectedItem.value = item
    }

    fun updateUsedProductListingBookMarkedInfo(productId: Long, isBookMarked: Boolean) {
        _pageSource.updateUsedProductListingBookMarkedInfo(productId, isBookMarked)
    }

    fun updateLoadingItemsJob(job: Job?) {
        loadingItemsJob = job
    }

    fun getLoadingItemsJob(): Job? {
        return loadingItemsJob
    }

    fun updateLastLoadedItemPosition(lastLoadedItemPosition: Int) {
        _lastLoadedItemPosition.value = lastLoadedItemPosition
    }

    fun updateError(message: String) {
        error = message
    }


    suspend fun bookmark(
        userId: Long,
        item: UsedProductListing,
    ): Result<ResponseReply> {
        return try {
            AppClient.instance.create(ManageUsedProductListingService::class.java)
                .bookmarkUsedProductListing(
                    userId,
                    item.productId
                )
                .let {
                    if (it.isSuccessful) {
                        val body = it.body()
                        if (body != null && body.isSuccessful) {
                            Result.Success(body)

                        } else {
                            val errorMessage = "Failed, try again later..."
                            Result.Error(Exception(errorMessage))
                        }
                    } else {
                        val errorBody = it.errorBody()?.string()
                        val errorMessage = try {
                            Gson().fromJson(errorBody, ErrorResponse::class.java).message
                        } catch (_: Exception) {
                            "An unknown error occurred"
                        }
                        Result.Error(Exception(errorMessage))
                    }
                }

        } catch (t: Throwable) {
            Result.Error(t)
        }

    }
     suspend fun removeBookmark(
        userId: Long,
        item: UsedProductListing,
    ): Result<ResponseReply> {


        return try {
            AppClient.instance.create(ManageUsedProductListingService::class.java)
                .removeBookmarkUsedProductListing(
                    userId,
                    item.productId
                ).let {
                    if (it.isSuccessful) {
                        val body = it.body()
                        if (body != null && body.isSuccessful) {
                            Result.Success(body)

                        } else {
                            val errorMessage = "Failed, try again later..."
                            Result.Error(Exception(errorMessage))
                        }


                    } else {

                        val errorBody = it.errorBody()?.string()
                        val errorMessage = try {
                            Gson().fromJson(errorBody, ErrorResponse::class.java).message
                        } catch (_: Exception) {
                            "An unknown error occurred"
                        }
                        Result.Error(Exception(errorMessage))
                    }
                }


        } catch (t: Exception) {
            Result.Error(t)
        }
    }

}

@KoinViewModel
class SecondsViewmodel(
    val userProfileDao: UserProfileDao,
    private val guestUserLocationDao: UserLocationDao,
    val chatUserDao: ChatUserDao,
    val tokenManager: TokenManager,
    val chatUserRepository: ChatUserRepository,
    val networkConnectivityManager: NetworkConnectivityManager
) : ViewModel() {


    val userId = UserSharedPreferencesManager.userId

    val isGuest = tokenManager.isGuest()

    val repositories = mutableMapOf<Int, SecondsRepository>()

    init {
        loadSecondsRepository("", false, 0)
        loadSeconds(0)
    }

    fun loadSecondsRepository(
        submittedQuery: String,
        onlySearchBar: Boolean,
        key: Int
    ) {
        repositories.put(
            key, SecondsRepository(
                userProfileDao,
                guestUserLocationDao,
                chatUserDao,
                chatUserRepository,
                networkConnectivityManager
            ).apply {
                updateSubmittedQuery(submittedQuery)
                updateOnlySearchBar(onlySearchBar)
                updateKey(key)
            })
    }


    fun loadSeconds(key: Int) {
        viewModelScope.launch {
            getSecondsRepository(key)
                .apply {
                    loadSeconds(userId, isGuest)
                }
        }
    }

    fun getSecondsRepository(key: Int): SecondsRepository =
        repositories[key] ?: throw IllegalStateException("No repository found")

    fun getKey(key: Int): Int =
        repositories[key]?.getKey() ?: throw IllegalStateException("No repository found")

    fun setNestedSecondsOwnerProfileSelectedItem(key: Int, item: UsedProductListing?) {
        getSecondsRepository(key)
            .apply {
                updateNestedSecondsOwnerProfileSelectedItem(item)
            }
    }

    fun setSelectedItem(key: Int, item: UsedProductListing?) {
        getSecondsRepository(key).apply {
            updateSelectedItem(item)
        }
    }

    fun directUpdateSecondsIsBookMarked(key: Int, productId: Long, isBookMarked: Boolean) {
        getSecondsRepository(key).apply {
            updateUsedProductListingBookMarkedInfo(productId, isBookMarked)
        }
    }


    fun updateLastLoadedItemPosition(key: Int, newPosition: Int) {
        viewModelScope.launch {
            getSecondsRepository(key)
                .apply {
                    updateLastLoadedItemPosition(newPosition)
                }

        }
    }



    suspend fun getChatUser(userId: Long, userProfile: FeedUserProfileInfo): ChatUser {
        return withContext(Dispatchers.IO) {

            chatUserDao.getChatUserByRecipientId(userProfile.userId) ?: run {
                ChatUser(
                    userId = userId,
                    recipientId = userProfile.userId,
                    timestamp = System.currentTimeMillis(),
                    userProfile = userProfile.copy(isOnline = false)
                ).let {
                    it.copy(chatId = chatUserDao.insertChatUser(it).toInt())
                }
            }

        }
    }


    fun nextPage(key: Int, userId: Long, query: String?) {
        viewModelScope.launch {

            getSecondsRepository(key)
                .apply {

                    if (isGuest) {
                        val userLocation = guestUserLocationDao.getLocation(userId)

                        pageSource.guestNextPage(
                            userId,
                            query,
                            userLocation?.latitude,
                            userLocation?.longitude
                        )

                    } else {
                        getLoadingItemsJob()?.cancel()
                        updateLoadingItemsJob(launch { pageSource.nextPage(userId, query) })
                    }

                }

        }

    }



    fun refresh(key: Int, userId: Long, query: String?) {

        viewModelScope.launch {
            getSecondsRepository(key)
                .apply {
                    if (isGuest) {
                        val userLocation = guestUserLocationDao.getLocation(userId)

                        pageSource.guestRefresh(
                            userId,
                            query,
                            userLocation?.latitude,
                            userLocation?.longitude
                        )
                    } else {
                        getLoadingItemsJob()?.cancel()
                        updateLoadingItemsJob(launch { pageSource.refresh(userId, query) })
                    }

                }
        }
    }

    fun retry(key: Int, userId: Long, query: String?) {
        viewModelScope.launch {
            getSecondsRepository(key)
                .apply {
                    if (isGuest) {
                        val userLocation = guestUserLocationDao.getLocation(userId)

                        pageSource.guestRetry(
                            userId,
                            query,
                            userLocation?.latitude,
                            userLocation?.longitude
                        )
                    } else {
                        pageSource.retry(userId, query)
                    }
                }

        }
    }


    fun onBookmark(
        key: Int,
        userId: Long,
        item: UsedProductListing,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            getSecondsRepository(key)
                .apply {

                    try {
                        when (val result = bookmark(userId, item)) {
                            is Result.Success -> {
                                onSuccess()
                            }

                            is Result.Error -> {
                                val error = mapExceptionToError(result.error).errorMessage
                                updateError(error)
                                onError(error)
                            }
                        }
                    } catch (_: Exception) {
                        val error = "Something Went Wrong"
                        updateError(error)
                        onError(error)

                    }
                }


        }
    }


    fun onRemoveBookmark(
        key: Int,
        userId: Long,
        item: UsedProductListing,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch {
            getSecondsRepository(key)
                .apply {

                    try {
                        when (val result = removeBookmark(userId, item)) {
                            is Result.Success -> {
                                onSuccess()
                            }

                            is Result.Error -> {
                                val error = mapExceptionToError(result.error).errorMessage
                                updateError(error)
                                onError(error)
                            }
                        }
                    } catch (_: Exception) {
                        val error = "Something Went Wrong"
                        updateError(error)
                        onError(error)

                    }
                }

        }
    }


}







