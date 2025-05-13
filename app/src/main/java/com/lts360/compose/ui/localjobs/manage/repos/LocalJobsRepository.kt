package com.lts360.compose.ui.localjobs.manage.repos

import com.google.gson.Gson
import com.lts360.api.app.AppClient
import com.lts360.api.app.ManageLocalJobService
import com.lts360.api.common.errors.ErrorResponse
import com.lts360.api.common.responses.ResponseReply
import com.lts360.api.utils.Result
import com.lts360.app.database.daos.chat.ChatUserDao
import com.lts360.app.database.daos.profile.UserLocationDao
import com.lts360.app.database.daos.profile.UserProfileDao
import com.lts360.compose.ui.chat.repos.ChatUserRepository
import com.lts360.compose.ui.localjobs.LocalJobsPageSource
import com.lts360.compose.ui.localjobs.models.LocalJob
import com.lts360.compose.ui.managers.NetworkConnectivityManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject


class LocalJobsRepository @Inject constructor(
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


    private val _selectedItem = MutableStateFlow<LocalJob?>(null)
    val selectedItem = _selectedItem.asStateFlow()

    private val _lastLoadedItemPosition = MutableStateFlow(-1)
    val lastLoadedItemPosition = _lastLoadedItemPosition.asStateFlow()

    private var error: String = ""

    private var _pageSource = LocalJobsPageSource(pageSize = 30)
    val pageSource: LocalJobsPageSource get() = _pageSource


    private var loadingItemsJob: Job? = null


    suspend fun loadLocalJobs(userId: Long, isGuest: Boolean = false) {
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

    fun updateSelectedItem(item: LocalJob?) {
        _selectedItem.value = item
    }

    fun updateSelectedItemIsApplied(isApplied: Boolean) {
        _selectedItem.value = _selectedItem.value?.copy(isApplied = isApplied)
    }

    fun updateUsedProductListingBookMarkedInfo(productId: Long, isBookMarked: Boolean) {
        _pageSource.updateLocalJobBookMarkedInfo(productId, isBookMarked)
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


    suspend fun removeBookmark(
        userId: Long,
        item: LocalJob,
    ): Result<ResponseReply> {


        return try {
            AppClient.instance.create(ManageLocalJobService::class.java)
                .removeBookmarkLocalJob(
                    userId,
                    item.localJobId
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

                        Result.Error(
                            Exception(
                                try {
                                    Gson().fromJson(
                                        it.errorBody()?.string(),
                                        ErrorResponse::class.java
                                    ).message
                                } catch (_: Exception) {
                                    "An unknown error occurred"
                                }
                            )
                        )
                    }
                }


        } catch (t: Exception) {
            Result.Error(t)
        }
    }


    suspend fun bookmark(
        userId: Long,
        item: LocalJob,
    ): Result<ResponseReply> {
        return try {
            AppClient.instance.create(ManageLocalJobService::class.java)
                .bookmarkLocalJob(
                    userId,
                    item.localJobId
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
                        Result.Error(
                            Exception(
                                try {
                                    Gson().fromJson(
                                        it.errorBody()?.string(),
                                        ErrorResponse::class.java
                                    ).message
                                } catch (_: Exception) {
                                    "An unknown error occurred"
                                }
                            )
                        )
                    }
                }

        } catch (t: Throwable) {
            Result.Error(t)
        }

    }


    suspend fun applyLocalJob(
        userId: Long,
        localJobId: Long,
    ): Result<ResponseReply> {
        return try {
            AppClient.instance.create(ManageLocalJobService::class.java)
                .applyLocalJob(
                    userId,
                    localJobId
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
                        Result.Error(
                            Exception(
                                try {
                                    Gson().fromJson(
                                        it.errorBody()?.string(),
                                        ErrorResponse::class.java
                                    ).message
                                } catch (_: Exception) {
                                    "An unknown error occurred"
                                }
                            )
                        )
                    }
                }

        } catch (t: Throwable) {
            Result.Error(t)
        }

    }


}

