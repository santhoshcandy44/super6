package com.super6.pot.ui.main.viewmodels

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.super6.pot.api.app.AppClient
import com.super6.pot.api.models.service.Service
import com.super6.pot.utils.LogUtils.TAG
import com.super6.pot.api.Utils.ResultError
import com.super6.pot.api.Utils.mapExceptionToError
import com.super6.pot.api.app.ManageServicesApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.cancellation.CancellationException

class PageSource(
    val savedStateHandle: SavedStateHandle,
    private val pageSize: Int = 1, // Default page size
) {
    // MutableStateFlow for various states
    private val _initialLoadState = MutableStateFlow(true)
    val initialLoadState: StateFlow<Boolean> = _initialLoadState

    private val _items = MutableStateFlow<List<Service>>(emptyList())
    val items: StateFlow<List<Service>> = _items

    private val _isLoadingItems = MutableStateFlow(false)
    val isLoadingItems: StateFlow<Boolean> = _isLoadingItems

    private val _isRefreshingItems = MutableStateFlow(false)
    val isRefreshingItems: StateFlow<Boolean> = _isRefreshingItems

    private val _hasNetworkError = MutableStateFlow(false)
    val hasNetworkError: StateFlow<Boolean> = _hasNetworkError

    private val _hasAppendError = MutableStateFlow(false)
    val hasAppendError: StateFlow<Boolean> = _hasAppendError

    private val _hasMoreItems = MutableStateFlow(true)
    val hasMoreItems: StateFlow<Boolean> = _hasMoreItems

    private val _industriesCount = MutableStateFlow(-1)
    val industriesCount: StateFlow<Int> get() = _industriesCount

    private var page =  1
    private var lastTimeStamp: String? = null
    private var lastTotalRelevance: String? = null

    private val mutex = Mutex()



    // Public getter and setter for 'page'
    var currentPage: Int
        get() = page // Access the private variable 'page'
        set(value) {
            if (value > 0) { // Validate: Page must be greater than 0
                page = value
            } else {
                throw IllegalArgumentException("Page must be greater than 0")
            }
        }

    // Public getter and setter for 'lastTimeStamp'
    var currentLastTimeStamp: String?
        get() = lastTimeStamp // Access the private variable 'lastTimeStamp'
        set(value) {
            lastTimeStamp = value // Update value
        }

    // Public getter and setter for 'lastTotalRelevance'
    var currentLastTotalRelevance: String?
        get() = lastTotalRelevance // Access the private variable 'lastTotalRelevance'
        set(value) {
            lastTotalRelevance = value // Update value
        }

    // Public getter and setter for 'industriesCount'
    var currentIndustriesCount: Int
        get() = _industriesCount.value ?: 0 // Access the value safely, default to 0 if null
        set(value) {
            _industriesCount.value = value
        }

    // Public getter and setter for 'hasMoreItems'
    var currentHasMoreItems: Boolean
        get() = _hasMoreItems.value ?: false // Default to false if value is null
        set(value) {
            _hasMoreItems.value = value // Simply set the boolean value
        }



    // Public getter and setter for 'hasMoreItems'
    var currentInitialLoadState: Boolean
        get() = _initialLoadState.value ?: false // Default to false if value is null
        set(value) {
            _initialLoadState.value = value // Simply set the boolean value
        }

    // Public getter and setter for 'hasMoreItems'
    var currentNetworkError: Boolean
        get() = _hasNetworkError.value ?: false // Default to false if value is null
        set(value) {
            _hasNetworkError.value = value // Simply set the boolean value
        }



    // Public getter and setter for 'hasMoreItems'
    var currentAppendError: Boolean
        get() = _hasAppendError.value ?: false // Default to false if value is null
        set(value) {
            _hasAppendError.value = value // Simply set the boolean value
        }




    fun setRefreshingItems(value: Boolean){
        _isRefreshingItems.value=value
    }



    fun setNetWorkError(value: Boolean){
        currentNetworkError=value
    }


    fun setItems(items:List<Service>){
        _items.value=items
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
        industries:List<Int>,
        latitude: Double? = null,
        longitude: Double? = null,
        ) {
        if (isLoadingItems.value) {
            setRefreshingItems(false)
            return
        }
        _isRefreshingItems.value = true
        guestNextPage(userId, query, industries,latitude, longitude, isRefreshing = true)

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
        currentAppendError= false
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
                val response = AppClient.instance.create(ManageServicesApiService::class.java)
                    .getServices(userId, page, query, lastTimeStamp, lastTotalRelevance)

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

                            if(page==1 && !isRefreshing){
                                currentIndustriesCount=services[0].industriesCount
                            }

                            _items.value = if (isRefreshing) {
                                services // Replace items on refresh
                            } else {
                                _items.value + services // Append items
                            }

                            currentPage++ // Increment page after successful fetch
                            currentHasMoreItems = services.size == pageSize // Check for more items
                            currentAppendError = false

                        } else {

                            if (page == 1) {
                                _items.value = emptyList()
                            }
                            currentHasMoreItems = false // No more items
                        }
                    } else {
                        if (isRefreshing) {
                            _items.value = emptyList()
                            currentHasMoreItems = true // No more items
                        }
                        currentAppendError= true // Handle unsuccessful response
                    }
                } else {
                    if (isRefreshing) {
                        _items.value = emptyList()
                        currentHasMoreItems= true // No more items
                    }
                    currentAppendError = true // Handle API error
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
        industries:List<Int>,
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
                val response = AppClient.instance.create(ManageServicesApiService::class.java)
                    .guestGetServices(
                        userId, page, query, industries, lastTimeStamp, lastTotalRelevance,
                        latitude, longitude)

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
                            currentHasMoreItems = services.size == pageSize // Check for more items
                            currentAppendError = false

                        } else {
                            if (page == 1) {
                                _items.value = emptyList()
                            }
                            currentHasMoreItems = false // No more items
                        }
                    } else {
                        if (isRefreshing) {
                            _items.value = emptyList()
                            currentHasMoreItems = true // No more items
                        }
                        currentAppendError = true // Handle unsuccessful response
                    }
                } else {
                    if (isRefreshing) {
                        _items.value = emptyList()
                        currentHasMoreItems = true // No more items
                    }
                    currentAppendError = true // Handle API error
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

