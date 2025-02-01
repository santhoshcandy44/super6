package com.super6.pot.ui.viewmodels

import android.content.Context
import android.util.Log
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.super6.pot.api.app.AppClient
import com.super6.pot.api.auth.managers.TokenManager
import com.super6.pot.api.common.errors.ErrorResponse
import com.super6.pot.api.common.responses.ResponseReply
import com.super6.pot.api.models.service.FeedUserProfileInfo
import com.super6.pot.api.models.service.GuestIndustryDao
import com.super6.pot.api.models.service.Service
import com.super6.pot.ui.chat.repos.ChatUserRepository
import com.super6.pot.ui.main.models.SearchTerm
import com.super6.pot.ui.main.navhosts.routes.BottomBarScreen
import com.super6.pot.ui.main.viewmodels.PageSource
import com.super6.pot.ui.managers.UserSharedPreferencesManager
import com.super6.pot.utils.LogUtils.TAG
import com.super6.pot.api.Utils.Result
import com.super6.pot.api.Utils.mapExceptionToError
import com.super6.pot.api.app.ManageServicesApiService
import com.super6.pot.app.database.daos.chat.ChatUserDao
import com.super6.pot.app.database.daos.profile.UserProfileDao
import com.super6.pot.app.database.models.chat.ChatUser
import com.super6.pot.app.database.daos.profile.UserLocationDao
import com.super6.pot.ui.managers.NetworkConnectivityManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

/*
class ServicesPagingSource(
    private val userId: Int,
    private val query: String?, // Add any additional parameters you need
    private val onFirstLoad: () -> Unit, // Callback for first load

) : PagingSource<Int, Service>() {

    private var isFirstLoad = true // Track if it's the first load
    private var lastTimeStamp: String? = null
    private var lastTotalRelevance: String? = null

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Service> {
        return try {

            val nextPage = params.key ?: 1

            val response = RetrofitClient.instance.create(ManageServicesApiService::class.java)
                .getServices(userId, nextPage, query, lastTimeStamp, lastTotalRelevance)

            if (response.isSuccessful) {
                val body = response.body()

                // Ensure the body is not null
                if (body != null && body.isSuccessful) {
                    val services = Gson().fromJson(
                        body.data,
                        object : TypeToken<List<Service>>() {}.type
                    ) as List<Service>


                    if (services.isNotEmpty() && lastTimeStamp == null) {
                        lastTimeStamp = services[0].initialCheckAt
                    }

                    if (services.isNotEmpty()) {
                        lastTotalRelevance = services[services.size - 1].totalRelevance
                    }

                    LoadResult.Page(
                        data = services, // Assuming body contains the 'users' list
                        prevKey = if (nextPage == 1) null else nextPage - 1,
                        nextKey = if (services.isEmpty()) null else nextPage + 1
                    )
                } else {
                    val errorMessage = "Failed, try again later..."
                    // Handle the case where body is null (unexpected behavior)
                    LoadResult.Error(Exception(errorMessage))
                }
            } else {

                val errorBody = response.errorBody()?.string()
                val errorMessage = try {
                    Gson().fromJson(errorBody, ErrorResponse::class.java).message
                } catch (e: Exception) {
                    "An unknown error occurred"
                }
                // Handle API error response
                LoadResult.Error(Exception(errorMessage))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Handle network or parsing exceptions
            LoadResult.Error(e)
        } finally {
            if (isFirstLoad) {
                isFirstLoad = false // Update the flag
                onFirstLoad() // Notify about the first load
            }
        }
    }

    //Refreshing page from starting position
    override fun getRefreshKey(state: PagingState<Int, Service>): Int? {
        // Always return null to indicate starting from the first page on refresh
        return null
    }

*//*    override fun getRefreshKey(state: PagingState<Int, Service>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }*//*
}*/


@HiltViewModel
class ServicesViewModel @Inject constructor(
    @ApplicationContext context: Context,
    val savedStateHandle: SavedStateHandle,
    val userProfileDao: UserProfileDao,
    private val guestIndustryDao: GuestIndustryDao,
    private val guestUserLocationDao: UserLocationDao,
    val chatUserDao: ChatUserDao,
    val tokenManager: TokenManager,
    val chatUserRepository: ChatUserRepository,
    networkConnectivityManager: NetworkConnectivityManager,
    ) : ViewModel() {


/*

    val args = savedStateHandle.toRoute<BottomBarScreen.Home>()
*/


    val onlySearchBar = savedStateHandle.get<Boolean>("onlySearchBar") ?: false
    private val submittedQuery = savedStateHandle.get<String?>("submittedQuery")
    private val key = savedStateHandle.get<Int>("key")?: 0

    val userId = UserSharedPreferencesManager.userId

    val connectivityManager = networkConnectivityManager


    private val _searchQuery = MutableStateFlow(
        if (submittedQuery != null) TextFieldValue(
            text = submittedQuery,
            selection = TextRange(0)
        ) else TextFieldValue()
    )
    val searchQuery = _searchQuery.asStateFlow()


    private val _searchOldQuery = MutableStateFlow(submittedQuery)
    val searchOldQuery = _searchOldQuery.asStateFlow()


    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    private val _isLoadingCache = MutableStateFlow(true)
    val isLoadingCache = _isLoadingCache.asStateFlow()

    private val _isLazyLoading = MutableStateFlow(false)
    val isLazyLoading = _isLazyLoading.asStateFlow()


    private val _suggestions = MutableStateFlow<List<String>>(emptyList())
    val suggestions = _suggestions.asStateFlow()

    private val _selectedItem = MutableStateFlow<Service?>(null)
    val selectedItem = _selectedItem.asStateFlow()


    private val _searchJob = MutableStateFlow<Job?>(null)
    val searchJob = _searchJob.asStateFlow()


    private  val _lastLoadedItemPosition = MutableStateFlow<Int>(-1)
    val lastLoadedItemPosition = _lastLoadedItemPosition.asStateFlow()


    private var error: String = ""


    /*    private var pagingSource:PagingSource<*,*>?=null

        private val _cachedData = MutableStateFlow<List<Service>>(emptyList())
        val cachedData: StateFlow<List<Service>> get() = _cachedData

        val services = Pager(PagingConfig(pageSize = 1, prefetchDistance = 2,enablePlaceholders =false)) {
            ServicesPagingSource(userId, submittedQuery) {
                if (_initialLoadState.value) {
                    _initialLoadState.value = false
                }
            }.also {
                pagingSource=it
            }

        }.flow.cachedIn(viewModelScope)

        fun cacheData(items:List<Service>){
            _cachedData.value=items
        }*/


    private var _pageSource = PageSource(savedStateHandle) // Initialize PageSource
    val pageSource: PageSource get() = _pageSource // Expose pageSource as read-only

    val signInMethod = tokenManager.getSignInMethod()
    val isValidSignInMethodFeaturesEnabled = tokenManager.isValidSignInMethodFeaturesEnabled()


    var loadingItemsJob: Job? = null

    private val _nestedServiceOwnerProfileSelectedItem = MutableStateFlow<Service?>(null)
    val nestedServiceOwnerProfileSelectedItem = _nestedServiceOwnerProfileSelectedItem.asStateFlow()


    init {

        if (signInMethod == "guest") {
            viewModelScope.launch {

                launch {
                    val startTime = System.currentTimeMillis()  // Record start time



                    val endTime = System.currentTimeMillis()  // Record end time
                    val elapsedTime = endTime - startTime  // Calculate elapsed time

                    // Log the execution time
                    Log.d(TAG, "Query executed in: $elapsedTime ms ")

                    _isLoadingCache.value=false

                    val userLocation = guestUserLocationDao.getLocation(userId)
                    val selectedIndustries = if (onlySearchBar) {
                        emptyList()
                    } else {
                        guestIndustryDao.getSelectedIndustries().map { it.industryId }
                    }

                    if (userLocation != null) {
                        pageSource.guestNextPage(
                            userId,
                            _searchQuery.value.text,
                            selectedIndustries,
                            userLocation.latitude,
                            userLocation.longitude
                        )
                    } else {
                        pageSource.guestNextPage(
                            userId,
                            _searchQuery.value.text,
                            selectedIndustries
                        )
                    }


                }.join()


            }
        } else {


            loadingItemsJob = viewModelScope.launch {

                launch {

                    val startTime = System.currentTimeMillis()  // Record start time

                   /* val services = savedStateHandle.get<Boolean>("is_cache_services")
                        ?.let {
                            withContext(Dispatchers.IO) {
                                getCachedResult(context,10).map { it.toService() }
                            }

                        }*/

                    val endTime = System.currentTimeMillis()  // Record end time
                    val elapsedTime = endTime - startTime  // Calculate elapsed time


                    // Log the execution time
                    Log.d(TAG, "Query executed in: $elapsedTime ms ")


               /*     if (services != null) {
                        pageSource.setItems(services)

                        savedStateHandle.get<Long>("nested_service_owner_profile_selected_item")
                            ?.let { nonNullNestedServiceOwnerProfileSelectedItem ->
                                _nestedServiceOwnerProfileSelectedItem.value = services.firstOrNull {
                                    it.serviceId == nonNullNestedServiceOwnerProfileSelectedItem
                                }

                            }
                        _isLoadingCache.value=false
                    } else {

                    }
*/

/*
                    _isLoadingCache.value=false
*/
                    pageSource.nextPage(userId, _searchQuery.value.text)
                }.join()

                launch {
                    pageSource.items.collectLatest {
                        withContext(Dispatchers.IO) {
/*
                            serviceDao.deleteAll()
*/
/*
                            serviceDao.insertAll(it.map { it.toServiceEntity("feed") })
*/
/*
                            savedStateHandle["is_cache_services"] = true
*/
                        }
                    }
                }
            }
        }

/*
        viewModelScope.launch {
            savedStateHandle.get<Long>("selected_item")
                ?.let {
                    withContext(Dispatchers.IO){
                        serviceDao.getServiceById(it)
                    }
                }
                ?.let {
                _selectedItem.value = it.toService()
            }
        }*/




    }



    // Method to update the position
    fun updateLastLoadedItemPosition(newPosition: Int) {
        viewModelScope.launch {
            _lastLoadedItemPosition.value = newPosition
        }
    }

    fun setNestedServiceOwnerProfileSelectedItem(service: Service?) {
        _nestedServiceOwnerProfileSelectedItem.value = service
      /*  service?.let {
            savedStateHandle["nested_service_owner_profile_selected_item"] = service.serviceId
        }*/

    }

    // Load next page
    fun nextPage(userId: Long, query: String?) {

        if (signInMethod == "guest") {
            viewModelScope.launch {
                val userLocation = guestUserLocationDao.getLocation(userId)
                val selectedIndustries = if (onlySearchBar) {
                    emptyList()
                } else {
                    guestIndustryDao.getSelectedIndustries().map { it.industryId }
                }
                if (userLocation != null) {
                    pageSource.guestNextPage(
                        userId, _searchQuery.value.text, selectedIndustries, userLocation.latitude,
                        userLocation.longitude
                    )
                } else {
                    pageSource.guestNextPage(userId, _searchQuery.value.text, selectedIndustries)
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
                        _searchQuery.value.text,
                        selectedIndustries,
                        userLocation.latitude,
                        userLocation.longitude
                    )

                } else {
                    pageSource.guestRefresh(userId, _searchQuery.value.text, selectedIndustries)
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
                        _searchQuery.value.text,
                        selectedIndustries,
                        userLocation.latitude,
                        userLocation.longitude
                    )
                } else {
                    pageSource.guestRetry(userId, _searchQuery.value.text, selectedIndustries)
                }

            }
        } else {
            viewModelScope.launch {
                pageSource.retry(userId, query)
            }
        }
    }


    fun clearJob() {
        _searchJob.value = null
    }


    fun navigateToOverlay(navController: NavController) {

        viewModelScope.launch {
            navController.navigate(BottomBarScreen.NestedHome(key + 1, _searchQuery.value.text, true))
            delay(100)
            collapseSearchAction()
        }
    }


    fun collapseSearchAction() {
        if (_isSearching.value) {
            // Execute your collapse logic
            setSuggestions(emptyList())
            val oldQuery = searchOldQuery.value // Safe access to old query
            if (!oldQuery.isNullOrEmpty()) {
                setSubmitSearchQuery(oldQuery)
            } else {
                setSubmitSearchQuery("")
            }
            setSearching(false)
        }
    }


    fun setSubmitSearchQuery(query: String) {
        // Update the TextFieldValue with new query and move the cursor to the end
        _searchQuery.value = TextFieldValue(text = query, selection = TextRange(0))
    }

    fun setSearchQuery(query: String) {

        // Update the TextFieldValue with new query and move the cursor to the end
        _searchQuery.value = TextFieldValue(
            text = query,
            selection = TextRange(query.length)
        )

    }


    fun setSearching(isSearching: Boolean) {
        _isSearching.value = isSearching
    }


    fun setSuggestions(suggestions: List<String>) {
        _suggestions.value = suggestions
    }

    fun setSelectedItem(item: Service?) {
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


    fun onGetSearchQuerySuggestions(userId: Long, query: String) {


        if (signInMethod == "guest") {

            _searchJob.value = viewModelScope.launch {
                try {
                    when (val result = getGuestSearchQuerySuggestions(userId, query)) {
                        is Result.Success -> {
                            // Deserialize the search terms and set suggestions
                            val searchTerms = Gson().fromJson(
                                result.data.data,
                                object : TypeToken<List<SearchTerm>>() {}.type
                            ) as List<SearchTerm>
                            setSuggestions(searchTerms.map { it.searchTerm })
                        }

                        is Result.Error -> {
                            // Handle error
                            setSuggestions(emptyList())
                            val errorMsg = mapExceptionToError(result.error).errorMessage
                            // Optionally log the error message
                        }
                    }
                } catch (t: Exception) {
                    t.printStackTrace()
                    if (t is CancellationException) {
                        return@launch // Early return on cancellation
                    }
                    // Handle other exceptions
                    setSuggestions(emptyList())
                    // Optionally log the error
                }
            }

        } else {
            _searchJob.value = viewModelScope.launch {
                try {
                    when (val result = getSearchQuerySuggestions(userId, query)) {
                        is Result.Success -> {
                            // Deserialize the search terms and set suggestions
                            val searchTerms = Gson().fromJson(
                                result.data.data,
                                object : TypeToken<List<SearchTerm>>() {}.type
                            ) as List<SearchTerm>
                            setSuggestions(searchTerms.map { it.searchTerm })
                        }

                        is Result.Error -> {
                            // Handle error
                            setSuggestions(emptyList())
                            val errorMsg = mapExceptionToError(result.error).errorMessage
                            // Optionally log the error message
                        }
                    }
                } catch (t: Exception) {
                    t.printStackTrace()
                    if (t is CancellationException) {
                        return@launch // Early return on cancellation
                    }
                    // Handle other exceptions
                    setSuggestions(emptyList())
                    // Optionally log the error
                }
            }

        }

    }


    private suspend fun getGuestSearchQuerySuggestions(
        userId: Long,
        query: String,
    ): Result<ResponseReply> {


        return try {
            val response =
                AppClient.instance.create(ManageServicesApiService::class.java)
                    .guestSearchFilter(userId, query)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.isSuccessful) {

                    Result.Success(body)
                } else {
                    // Handle unsuccessful response
                    setSuggestions(emptyList())
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = try {
                        Gson().fromJson(errorBody, ErrorResponse::class.java).message
                    } catch (e: Exception) {
                        "An unknown error occurred"
                    }
                    Result.Error(Exception(errorMessage))
                }
            } else {
                // Handle unsuccessful HTTP response
                Result.Error(Exception("Failed to retrieve suggestions"))
            }
        } catch (e: Exception) {
            // Handle exceptions
            if (e is CancellationException) {
                throw e // Re-throw the cancellation exception
            }
            Result.Error(e)
        }
    }


    private suspend fun getSearchQuerySuggestions(
        userId: Long,
        query: String,
    ): Result<ResponseReply> {
        return try {
            val response =
                AppClient.instance.create(ManageServicesApiService::class.java)
                    .searchFilter(userId, query)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.isSuccessful) {

                    Result.Success(body)
                } else {
                    // Handle unsuccessful response
                    setSuggestions(emptyList())
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = try {
                        Gson().fromJson(errorBody, ErrorResponse::class.java).message
                    } catch (e: Exception) {
                        "An unknown error occurred"
                    }
                    Result.Error(Exception(errorMessage))
                }
            } else {
                // Handle unsuccessful HTTP response
                Result.Error(Exception("Failed to retrieve suggestions"))
            }
        } catch (e: Exception) {
            // Handle exceptions
            if (e is CancellationException) {
                throw e // Re-throw the cancellation exception
            }
            Result.Error(e)
        }
    }


    fun formatDistance(distance: Double?): String {
        return if (distance != null) {
            // Format distance to show one decimal place, but remove ".0" if it's an integer
            if (distance % 1.0 == 0.0) {
                "${distance.toInt()} km" // If the distance is a whole number, show as an integer
            } else {
                "%.1f km".format(distance) // Otherwise, show it with one decimal place
            }
        } else {
            "Distance not available"
        }
    }


    suspend fun getChatUser(userId: Long, userProfile: FeedUserProfileInfo): ChatUser {
        return withContext(Dispatchers.IO) {

            val chatUser = chatUserDao.getChatUserByRecipientId(userProfile.userId)
            // If chat user exists, update selected values
            chatUser?.let {
                it
            } ?: run {
                val newChatUser = ChatUser(
                    userId = userId,
                    recipientId = userProfile.userId,
                    timestamp = System.currentTimeMillis(),
                    userProfile = userProfile.copy(isOnline = false)
                )
                val chatId = chatUserDao.insertChatUser(newChatUser).toInt() // New chat ID
                newChatUser.copy(chatId = chatId)
            }

        }
    }




}







