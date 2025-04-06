package com.lts360.compose.ui.main.viewmodels

import androidx.lifecycle.SavedStateHandle
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.lts360.api.utils.ResultError
import com.lts360.api.utils.mapExceptionToError
import com.lts360.api.app.AppClient
import com.lts360.api.app.ManageServicesApiService
import com.lts360.api.common.errors.ErrorResponse
import com.lts360.api.models.service.Service
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.cancellation.CancellationException

class PageSource(
    val savedStateHandle: SavedStateHandle,
    private val pageSize: Int = 1
) {
    private val _initialLoadState = MutableStateFlow(true)
    val initialLoadState = _initialLoadState.asStateFlow()

    private val _items = MutableStateFlow<List<Service>>(emptyList())
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

    private val _industriesCount = MutableStateFlow(-1)
    val industriesCount = _industriesCount.asStateFlow()

    private var page = 1
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

    private var currentIndustriesCount: Int
        get() = _industriesCount.value
        set(value) {
            _industriesCount.value = value
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


    fun setRefreshingItems(value: Boolean) {
        _isRefreshingItems.value = value
    }


    fun setNetWorkError(value: Boolean) {
        currentNetworkError = value
    }


    fun updateServiceBookMarkedInfo(serviceId: Long, isBookMarked: Boolean) {
        // Update the service item's isBookmarked property
        _items.update { currentItems ->
            currentItems.map { service ->
                if (service.serviceId == serviceId) {
                    service.copy(isBookmarked = isBookMarked) // Assuming Service is a data class
                } else {
                    service
                }
            }
        }

    }

    // Refresh the data
    suspend fun refresh(userId: Long, query: String?) {
        if (isLoadingItems.value) {
            setRefreshingItems(false)
            return
        }
        _isRefreshingItems.value = true
        nextPage(userId, query, isRefreshing = true)

    }


    // Refresh the data
    suspend fun guestRefresh(
        userId: Long, query: String?,
        industries: List<Int>,
        latitude: Double? = null,
        longitude: Double? = null,
    ) {
        if (isLoadingItems.value) {
            setRefreshingItems(false)
            return
        }
        _isRefreshingItems.value = true
        guestNextPage(userId, query, industries, latitude, longitude, isRefreshing = true)

    }

    // Retry loading more data
    suspend fun retry(userId: Long, query: String?) {
        if (isRefreshingItems.value) {
            return
        }
        currentAppendError = false
        nextPage(userId, query)
    }

    // Retry loading more data
    suspend fun guestRetry(
        userId: Long, query: String?,
        industries: List<Int>,
        latitude: Double? = null,
        longitude: Double? = null,

        ) {
        if (isRefreshingItems.value) {
            return
        }
        currentAppendError = false
        guestNextPage(userId, query, industries, latitude, longitude)
    }

    // Load the next page of data
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
                AppClient.instance.create(ManageServicesApiService::class.java)
                    .getServices(userId, page, query, lastTimeStamp, lastTotalRelevance)
                    .also { response ->

                        if (response.isSuccessful) {
                            val body = response.body()
                            if (body != null && body.isSuccessful) {
                                val services = Gson().fromJson(
                                    body.data,
                                    object : TypeToken<List<Service>>() {}.type
                                ) as List<Service>

                                if (services.isNotEmpty()) {
                                    if (lastTimeStamp == null) {
                                        currentLastTimeStamp = services[0].initialCheckAt
                                    }

                                    currentLastTotalRelevance = services.last().totalRelevance

                                    _items.value = if (isRefreshing) {
                                        services // Replace items on refresh
                                    } else {
                                        _items.value + services // Append items
                                    }

                                    currentPage++
                                    currentHasMoreItems = services.size == pageSize
                                    currentIndustriesCount = -1
                                    currentAppendError = false

                                } else {

                                    if (page == 1) {
                                        _items.value = emptyList()
                                    }
                                    currentHasMoreItems = false
                                    currentIndustriesCount = -1
                                    currentAppendError = false
                                }
                            } else {
                                if (isRefreshing) {
                                    _items.value = emptyList()
                                    currentHasMoreItems = true
                                }
                                currentIndustriesCount = -1
                                currentAppendError = true
                            }
                        } else {


                            if (isRefreshing) {
                                _items.value = emptyList()
                                currentHasMoreItems = true
                            }

                            val errorBody = response.errorBody()?.string()
                            val errorCode = errorBody?.let {
                                Gson().fromJson(it, ErrorResponse::class.java).data.error.code
                            }

                            when (errorCode) {
                                "EMPTY_SERVICE_INDUSTRIES" -> {
                                    currentIndustriesCount = 0
                                    currentAppendError = false
                                }

                                else -> {
                                    currentIndustriesCount = -1
                                    currentAppendError = true
                                }
                            }
                        }
                    }


                currentNetworkError = false
            } catch (c: CancellationException) {
                throw c
            } catch (e: Exception) {
                e.printStackTrace()
                handleError(e, isRefreshing)
            } finally {
                finalizeLoad(isRefreshing)
            }
        }
    }


    suspend fun guestNextPage(
        userId: Long, query: String?,
        industries: List<Int>,
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
                AppClient.instance.create(ManageServicesApiService::class.java)
                    .guestGetServices(
                        userId, page, query, industries, lastTimeStamp, lastTotalRelevance,
                        latitude, longitude
                    ).also { response ->

                        if (response.isSuccessful) {
                            val body = response.body()
                            if (body != null && body.isSuccessful) {
                                val services = Gson().fromJson(
                                    body.data,
                                    object : TypeToken<List<Service>>() {}.type
                                ) as List<Service>

                                if (services.isNotEmpty()) {
                                    if (lastTimeStamp == null) {
                                        currentLastTimeStamp = services[0].initialCheckAt
                                    }
                                    currentLastTotalRelevance = services.last().totalRelevance


                                    _items.value = if (isRefreshing) {
                                        services // Replace items on refresh
                                    } else {
                                        _items.value + services // Append items
                                    }

                                    currentPage++ // Increment page after successful fetch
                                    currentHasMoreItems =
                                        services.size == pageSize // Check for more items
                                    currentIndustriesCount = -1
                                    currentAppendError = false

                                } else {
                                    if (page == 1) {
                                        _items.value = emptyList()
                                    }
                                    currentIndustriesCount = -1
                                    currentHasMoreItems = false
                                    currentAppendError = false
                                }
                            } else {

                                if (isRefreshing) {
                                    _items.value = emptyList()
                                    currentHasMoreItems = true
                                }
                                currentIndustriesCount = -1
                                currentAppendError = true
                            }
                        } else {

                            if (isRefreshing) {
                                _items.value = emptyList()
                                currentHasMoreItems = true
                            }

                            val errorBody = response.errorBody()?.string()
                            val errorCode = errorBody?.let {
                                Gson().fromJson(it, ErrorResponse::class.java).data.error.code
                            }

                            when (errorCode) {
                                "EMPTY_SERVICE_INDUSTRIES" -> {
                                    currentIndustriesCount = 0
                                    currentAppendError = false
                                }

                                else -> {
                                    currentIndustriesCount = -1
                                    currentAppendError = true
                                }
                            }
                        }

                        currentNetworkError = false

                    }


            } catch (e: Exception) {
                e.printStackTrace()
                handleError(e, isRefreshing)
            } finally {
                finalizeLoad(isRefreshing)
            }
        }
    }


    // Reset the state for a new fetch
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
                if (currentNetworkError) {
                    setNetWorkError(false)
                }
                currentAppendError = true
            }
        } else {

            if (error is ResultError.NoInternet && page == 1) {
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

