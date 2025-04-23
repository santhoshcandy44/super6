package com.lts360.compose.ui.localjobs


import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.lts360.api.utils.ResultError
import com.lts360.api.utils.mapExceptionToError
import com.lts360.api.app.AppClient
import com.lts360.api.app.ManageLocalJobService
import com.lts360.compose.ui.localjobs.models.LocalJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.cancellation.CancellationException

class LocalJobsPageSource(
    private val pageSize: Int = 1,
) {

    private val _initialLoadState = MutableStateFlow(true)
    val initialLoadState = _initialLoadState.asStateFlow()

    private val _items = MutableStateFlow<List<LocalJob>>(emptyList())
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

    fun setRefreshingItems(value: Boolean){
        _isRefreshingItems.value=value
    }

    fun setNetWorkError(value: Boolean){
        currentNetworkError=value
    }


    fun updateLocalJobBookMarkedInfo(localJobId: Long, isBookMarked: Boolean) {
        _items.update { currentItems ->
            currentItems.map { item ->
                if (item.localJobId == localJobId) {
                    item.copy(isBookmarked = isBookMarked)
                } else {
                    item
                }
            }
        }

    }

    suspend fun refresh(userId: Long, query: String?) {
        if (isLoadingItems.value) {
            setRefreshingItems(false)
            return
        }
        _isRefreshingItems.value = true
        nextPage(userId, query, isRefreshing = true)

    }


    suspend fun guestRefresh(
        userId: Long, query: String?,
        latitude: Double? = null,
        longitude: Double? = null,
    ) {
        if (isLoadingItems.value) {
            setRefreshingItems(false)
            return
        }
        _isRefreshingItems.value = true
        guestNextPage(userId, query,latitude, longitude, isRefreshing = true)

    }

    suspend fun retry(userId: Long, query: String?) {
        if (isRefreshingItems.value) {
            return
        }
        currentAppendError = false
        nextPage(userId, query)
    }

    suspend fun guestRetry(
        userId: Long, query: String?,
        latitude: Double? = null,
        longitude: Double? = null,

        ) {
        if (isRefreshingItems.value) {
            return
        }
        currentAppendError= false
        guestNextPage(userId, query, latitude, longitude)
    }

    suspend fun nextPage(userId: Long, query: String?, isRefreshing: Boolean = false) {

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
                    .getLocalJobs(userId, page, query, lastTimeStamp, lastTotalRelevance)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.isSuccessful) {
                        val items = Gson().fromJson(
                            body.data,
                            object : TypeToken<List<LocalJob>>() {}.type
                        ) as List<LocalJob>

                        if (items.isNotEmpty()) {
                            if (lastTimeStamp == null) {
                                currentLastTimeStamp = items[0].initialCheckAt
                            }

                            currentLastTotalRelevance = items.last().totalRelevance


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


    suspend fun guestNextPage(
        userId: Long, query: String?,
        latitude: Double? = null,
        longitude: Double? = null,
        isRefreshing: Boolean = false,
    ) {

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
                    .guestGetLocalJobs(
                        userId, page, query, lastTimeStamp, lastTotalRelevance,
                        latitude, longitude)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.isSuccessful) {
                        val items = Gson().fromJson(
                            body.data,
                            object : TypeToken<List<LocalJob>>() {}.type
                        ) as List<LocalJob>

                        if (items.isNotEmpty()) {
                            if (lastTimeStamp == null) {
                                currentLastTimeStamp = items[0].initialCheckAt
                            }
                            currentLastTotalRelevance = items.last().totalRelevance


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
                        currentAppendError = true
                    }
                } else {
                    if (isRefreshing) {
                        _items.value = emptyList()
                        currentHasMoreItems = true
                    }
                    currentAppendError = true
                }

                currentNetworkError = false
            } catch (e: Exception) {
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

    // Handle errors during fetch
    private fun handleError(e: Exception, isRefreshing: Boolean) {
        val error = mapExceptionToError(e)
        if (isRefreshing) {
            currentHasMoreItems = true // No more items
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

    // Finalize loading state
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

