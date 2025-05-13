package com.lts360.compose.ui.localjobs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lts360.api.auth.managers.TokenManager
import com.lts360.api.models.service.FeedUserProfileInfo
import com.lts360.api.utils.Result
import com.lts360.api.utils.mapExceptionToError
import com.lts360.app.database.daos.chat.ChatUserDao
import com.lts360.app.database.daos.profile.UserLocationDao
import com.lts360.app.database.daos.profile.UserProfileDao
import com.lts360.app.database.models.chat.ChatUser
import com.lts360.compose.ui.chat.repos.ChatUserRepository
import com.lts360.compose.ui.localjobs.manage.repos.LocalJobsRepository
import com.lts360.compose.ui.localjobs.models.LocalJob
import com.lts360.compose.ui.managers.NetworkConnectivityManager
import com.lts360.compose.ui.managers.UserGeneralPreferencesManager
import com.lts360.compose.ui.managers.UserSharedPreferencesManager
import com.lts360.compose.ui.profile.repos.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class LocalJobsViewmodel @Inject constructor(
    val userProfileDao: UserProfileDao,
    private val guestUserLocationDao: UserLocationDao,
    val chatUserDao: ChatUserDao,
    val tokenManager: TokenManager,
    userProfileRepository: UserProfileRepository,
    val chatUserRepository: ChatUserRepository,
    val networkConnectivityManager: NetworkConnectivityManager,
    private val userGeneralPreferencesManager: UserGeneralPreferencesManager
) : ViewModel() {

    val userId = UserSharedPreferencesManager.userId

    val isGuest = tokenManager.isGuest()

    val repositories = mutableMapOf<Int, LocalJobsRepository>()

    val isPhoneNumberVerified = userProfileRepository
        .getUserProfileSettingsInfoFlow(userId)
        .map {
            it?.phoneCountryCode != null && it.phoneNumber != null
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    val isDontAskAgainChecked = userGeneralPreferencesManager.isDontAskAgainChecked

    private val _isApplying = MutableStateFlow(false)
    val isApplying = _isApplying.asStateFlow()

    init {
        loadLocalJobsRepository("", false, 0)
        loadLocalJobs(0)
    }

    fun loadLocalJobsRepository(
        submittedQuery: String,
        onlySearchBar: Boolean,
        key: Int
    ) {
        repositories.put(
            key, LocalJobsRepository(
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


    fun loadLocalJobs(key: Int) {
        viewModelScope.launch {
            getLocalJobsRepository(key)
                .apply {
                    loadLocalJobs(userId, isGuest)
                }
        }
    }

    fun getLocalJobsRepository(key: Int): LocalJobsRepository =
        repositories[key] ?: throw IllegalStateException("No repository found")

    fun getKey(key: Int): Int =
        repositories[key]?.getKey() ?: throw IllegalStateException("No repository found")


    fun setSelectedItem(key: Int, item: LocalJob?) {
        getLocalJobsRepository(key).apply {
            updateSelectedItem(item)
        }
    }

    fun directUpdateLocalJobIsBookMarked(key: Int, productId: Long, isBookMarked: Boolean) {
        getLocalJobsRepository(key).apply {
            updateUsedProductListingBookMarkedInfo(productId, isBookMarked)
        }
    }


    fun updateLastLoadedItemPosition(key: Int, newPosition: Int) {
        viewModelScope.launch {
            getLocalJobsRepository(key)
                .apply {
                    updateLastLoadedItemPosition(newPosition)
                }

        }
    }


    fun setLocalJobPersonalInfoPromptIsDontAskAgainChecked(value: Boolean) {
        viewModelScope.launch {
            userGeneralPreferencesManager.setLocalJobPersonalInfoPromptIsDontAskAgainChecked(value)
        }
    }

    fun updateLocalJobIsApplied(key: Int, localJobId: Long) {
        viewModelScope.launch {
            getLocalJobsRepository(key)
                .apply {
                    pageSource.updateLocalJobAppliedInfo(localJobId, true)
                    updateSelectedItemIsApplied(true)
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

            getLocalJobsRepository(key)
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
            getLocalJobsRepository(key)
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
            getLocalJobsRepository(key)
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

    fun onApplyLocalJob(
        key: Int,
        userId: Long,
        localJobId: Long,
        onSuccess: (String) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        _isApplying.value = true
        viewModelScope.launch {
            getLocalJobsRepository(key)
                .apply {
                    try {
                        when (val result = applyLocalJob(userId, localJobId)) {
                            is Result.Success -> {
                                onSuccess(result.data.message)
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

                    } finally {
                        _isApplying.value = false
                    }
                }


        }
    }


    fun onBookmark(
        key: Int,
        userId: Long,
        item: LocalJob,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            getLocalJobsRepository(key)
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
        item: LocalJob,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch {
            getLocalJobsRepository(key)
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







