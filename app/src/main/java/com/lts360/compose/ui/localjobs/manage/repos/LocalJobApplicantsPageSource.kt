package com.lts360.compose.ui.localjobs.manage.repos

import androidx.lifecycle.viewModelScope
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.lts360.api.app.AppClient
import com.lts360.api.app.ManageLocalJobService
import com.lts360.api.common.errors.ErrorResponse
import com.lts360.api.common.responses.ResponseReply
import com.lts360.api.utils.Result
import com.lts360.api.utils.ResultError
import com.lts360.api.utils.mapExceptionToError
import com.lts360.compose.ui.localjobs.models.EditableLocalJob
import com.lts360.compose.ui.localjobs.models.LocalJobApplicant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.cancellation.CancellationException

class LocalJobApplicantsPageSource(
    private val pageSize: Int = 1
    ) {

    private val _initialLoadState = MutableStateFlow(true)
    val initialLoadState = _initialLoadState.asStateFlow()

    private val _items = MutableStateFlow<List<LocalJobApplicant>>(emptyList())
    val items = _items.asStateFlow()

    private val _isLoadingItems = MutableStateFlow(false)
    val isLoadingItems = _isLoadingItems.asStateFlow()

    private val _isRefreshingItems = MutableStateFlow(false)
    val isRefreshingItems = _isRefreshingItems.asStateFlow()

    private val _hasNetworkError = MutableStateFlow(false)
    val hasNetworkError = _hasNetworkError.asStateFlow()

    private val _hasAppendError = MutableStateFlow(false)
    val hasAppendError = _hasAppendError.asStateFlow()

    private val _hasMoreItems = MutableStateFlow(true)
    val hasMoreItems = _hasMoreItems.asStateFlow()

    private val _lastLoadedItemPosition = MutableStateFlow(-1)
    val lastLoadedItemPosition = _lastLoadedItemPosition.asStateFlow()



    private var page =  1
    private var lastTimeStamp: String? = null
    private var lastTotalRelevance: String? = null

    private val mutex = Mutex()

    var currentPage: Int
        get() = page
        set(value) {
            if (value > 0) {
                page = value
            } else {
                throw IllegalArgumentException("Page must be greater than 0")
            }
        }

    private var currentLastTimeStamp: String?
        get() = lastTimeStamp
        set(value) {
            lastTimeStamp = value
        }

    private var currentLastTotalRelevance: String?
        get() = lastTotalRelevance
        set(value) {
            lastTotalRelevance = value
        }

    private var currentHasMoreItems: Boolean
        get() = _hasMoreItems.value
        set(value) {
            _hasMoreItems.value = value
        }

    private var currentInitialLoadState: Boolean
        get() = _initialLoadState.value
        set(value) {
            _initialLoadState.value = value
        }

    private var currentNetworkError: Boolean
        get() = _hasNetworkError.value
        set(value) {
            _hasNetworkError.value = value
        }

    private var currentAppendError: Boolean
        get() = _hasAppendError.value
        set(value) {
            _hasAppendError.value = value
        }

    fun updateLastLoadedItemPosition(lastLoadedItemPosition: Int) {
        _lastLoadedItemPosition.value = lastLoadedItemPosition
    }

    fun setRefreshingItems(value: Boolean){
        _isRefreshingItems.value=value
    }

    fun setNetWorkError(value: Boolean){
        currentNetworkError=value
    }

    suspend fun refresh(localJobId: Long) {
        if (isLoadingItems.value) {
            setRefreshingItems(false)
            return
        }
        _isRefreshingItems.value = true
        nextPage(localJobId, isRefreshing = true)

    }

    suspend fun retry(localJobId: Long) {
        if (isRefreshingItems.value) {
            return
        }
        currentAppendError = false
        nextPage(localJobId)
    }

    suspend fun nextPage(localJobId: Long, isRefreshing: Boolean = false) {

        if (!isRefreshing) {
            if (_isLoadingItems.value || !_hasMoreItems.value) return
            _isLoadingItems.value = true
        }

        mutex.withLock {

            if (isRefreshing) {
                resetState()
            }

            try {
                val response = AppClient.instance.create(ManageLocalJobService::class.java)
                    .getLocalJobApplicantsByLocalJobId(localJobId, page, lastTimeStamp)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.isSuccessful) {
                        val items = Gson().fromJson(
                            body.data,
                            object : TypeToken<List<LocalJobApplicant>>() {}.type
                        ) as List<LocalJobApplicant>

                        if (items.isNotEmpty()) {
                            if (lastTimeStamp == null) {
                                currentLastTimeStamp = items[0].initialCheckAt
                            }

                            _items.value = if (isRefreshing) {
                                items
                            } else {
                                _items.value + items
                            }

                            currentPage++
                            currentHasMoreItems = items.size == pageSize
                            currentAppendError = false
                        } else {

                            if (page == 1) {
                                _items.value = emptyList()
                            }
                            currentHasMoreItems = false
                        }
                    } else {
                        if (isRefreshing) {
                            _items.value = emptyList()
                            currentHasMoreItems = true
                        }
                        currentAppendError= true
                    }
                } else {
                    if (isRefreshing) {
                        _items.value = emptyList()
                        currentHasMoreItems= true
                    }
                    currentAppendError = true
                }

                currentNetworkError=false
            }
            catch (c:CancellationException){
                throw c
            }
            catch (e: Exception) {
                e.printStackTrace()
                handleError(e, isRefreshing)
            } finally {
                finalizeLoad(isRefreshing)
            }
        }
    }



    private fun resetState() {
        page = 1
        currentLastTimeStamp = null
        currentLastTotalRelevance = null
//        _items.value = emptyList() //Let cache
    }

    private fun handleError(e: Exception, isRefreshing: Boolean) {
        val error = mapExceptionToError(e)
        if (isRefreshing) {
            currentHasMoreItems = true
            _items.value = emptyList()

            if (error is ResultError.NoInternet) {
                currentNetworkError = true
            } else {
                if(currentNetworkError){
                    setNetWorkError(false)
                }
                currentAppendError = true
            }
        } else {

            if (error is ResultError.NoInternet && page==1) {
                currentNetworkError = true
            } else {
                currentAppendError = true
            }

        }
    }


    suspend fun markAsReviewedLocalJob(userId: Long, localJoId: Long, applicantId: Long, onSuccess:()-> Unit={},
                                       onError:(String)-> Unit={}) {
        return try {
            val response = AppClient.instance.create(ManageLocalJobService::class.java)
                .markAsReviewedLocalJob(userId, localJoId, applicantId)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.isSuccessful) {
                    _items.value = _items.value.map { item ->
                        if (item.applicantId == applicantId) {
                            item.copy(isReviewed = true)
                        } else {
                            item
                        }
                    }
                    onSuccess()
                } else {
                    val errorMessage = "Failed, try again later..."
                    onError(errorMessage)
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = try {
                    Gson().fromJson(errorBody, ErrorResponse::class.java).message
                } catch (_: Exception) {
                    "An unknown error occurred"
                }
                onError(errorMessage)
            }
        } catch (t: Throwable) {
            t.printStackTrace()
            onError("Something went wrong")
        }
    }

    suspend fun unmarkAsReviewedLocalJob(userId: Long,
                                         localJobId: Long,
                                         applicantId: Long,
                                         onSuccess: () -> Unit={},
                                         onError: (String) -> Unit={}) {
        return try {
            val response = AppClient.instance.create(ManageLocalJobService::class.java)
                .unmarkAsReviewedLocalJob(userId, localJobId, applicantId)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.isSuccessful) {

                    _items.value = _items.value.map { item ->
                        if (item.applicantId == applicantId) {
                            item.copy(isReviewed = false)
                        } else {
                            item
                        }
                    }
                    onSuccess()
                } else {
                    val errorMessage = "Failed, try again later..."
                    onError(errorMessage)
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = try {
                    Gson().fromJson(errorBody, ErrorResponse::class.java).message
                } catch (_: Exception) {
                    "An unknown error occurred"
                }
                onError(errorMessage)
            }
        } catch (t: Throwable) {
            t.printStackTrace()
            onError("Something went wrong")
        }
    }


    private fun finalizeLoad(isRefreshing: Boolean) {
        if (isRefreshing) {
            _isRefreshingItems.value = false
        } else {
            _isLoadingItems.value = false
        }
        if (_initialLoadState.value) {
            currentInitialLoadState = false
        }
    }
}

