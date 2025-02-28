package com.lts360.compose.ui.viewmodels

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lts360.api.Utils.Result
import com.lts360.api.Utils.mapExceptionToError
import com.lts360.api.app.AppClient
import com.lts360.api.app.ManageServicesApiService
import com.lts360.api.auth.AuthClient
import com.lts360.api.auth.managers.TokenManager
import com.lts360.api.auth.services.AuthService
import com.lts360.api.common.errors.ErrorResponse
import com.lts360.api.common.responses.ResponseReply
import com.lts360.api.models.service.FeedUserProfileInfo
import com.lts360.api.models.service.GuestIndustryDao
import com.lts360.api.models.service.Service
import com.lts360.app.database.daos.chat.ChatUserDao
import com.lts360.app.database.daos.profile.UserLocationDao
import com.lts360.app.database.daos.profile.UserProfileDao
import com.lts360.app.database.models.chat.ChatUser
import com.lts360.compose.ui.chat.repos.ChatUserRepository
import com.lts360.compose.ui.main.models.ServiceReview
import com.lts360.compose.ui.main.models.ServiceReviewReply
import com.lts360.compose.ui.main.viewmodels.PageSource
import com.lts360.compose.ui.managers.NetworkConnectivityManager
import com.lts360.compose.ui.managers.UserSharedPreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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



    private val _selectedItem = MutableStateFlow<Service?>(null)
    val selectedItem = _selectedItem.asStateFlow()


    private val _lastLoadedItemPosition = MutableStateFlow(-1)
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


    private var loadingItemsJob: Job? = null

    private val _nestedServiceOwnerProfileSelectedItem = MutableStateFlow<Service?>(null)
    val nestedServiceOwnerProfileSelectedItem = _nestedServiceOwnerProfileSelectedItem.asStateFlow()


    init {

        if (signInMethod == "guest") {
            viewModelScope.launch {

                launch {


                    val userLocation = guestUserLocationDao.getLocation(userId)
                    val selectedIndustries = if (onlySearchBar) {
                        emptyList()
                    } else {
                        guestIndustryDao.getSelectedIndustries().map { it.industryId }
                    }

                    if (userLocation != null) {
                        pageSource.guestNextPage(
                            userId,
                            submittedQuery,
                            selectedIndustries,
                            userLocation.latitude,
                            userLocation.longitude
                        )
                    } else {
                        pageSource.guestNextPage(
                            userId,
                            submittedQuery,
                            selectedIndustries
                        )
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

    fun setNestedServiceOwnerProfileSelectedItem(service: Service?) {
        _nestedServiceOwnerProfileSelectedItem.value = service
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
                        userId, query, selectedIndustries, userLocation.latitude,
                        userLocation.longitude
                    )
                } else {
                    pageSource.guestNextPage(userId, query, selectedIndustries)
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
                        selectedIndustries,
                        userLocation.latitude,
                        userLocation.longitude
                    )

                } else {
                    pageSource.guestRefresh(userId, query, selectedIndustries)
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











    fun setSelectedItem(item: Service?) {
        _selectedItem.value = item
        /*item?.let {
            savedStateHandle["selected_item"] = item.serviceId
        }*/
    }


    private val _isReviewsLoading = MutableStateFlow<Boolean>(false)
    val isReviewsLoading = _isReviewsLoading.asStateFlow()


    private val _isReviewsReplyLoading = MutableStateFlow<Boolean>(false)
    val isReviewsReplyLoading = _isReviewsReplyLoading.asStateFlow()


    private val _selectedCommentId = MutableStateFlow<Int>(-1)
    val selectedCommentId = _selectedCommentId.asStateFlow()

    private val _selectedReviews = MutableStateFlow<List<ServiceReview>>(emptyList())
    val selectedReviews = _selectedReviews.asStateFlow()


    private val _selectedReviewReplies = MutableStateFlow<List<ServiceReviewReply>>(emptyList())
    val selectedReviewReplies = _selectedReviewReplies.asStateFlow()


    fun loadReViewsSelectedItem(
        service: Service,

        ) {

        viewModelScope.launch {
            _selectedItem.value = service

            _isReviewsLoading.value = true

            try {
                when (val result = loadSelectedServiceReviews(userId, service)) {
                    is Result.Success -> {

                        // Deserialize the search terms and set suggestions
                        val serviceReviews = Gson().fromJson(
                            result.data.data,
                            object : TypeToken<List<ServiceReview>>() {}.type
                        ) as List<ServiceReview>

                        _selectedReviews.value = serviceReviews

                    }

                    is Result.Error -> {
                        // Handle error
                        error = mapExceptionToError(result.error).errorMessage
                        // Optionally log the error message
                    }
                }
            } catch (t: Exception) {
                error = "Something Went Wrong"

            } finally {
                _isReviewsLoading.value = false
            }
        }
    }


    private suspend fun loadSelectedServiceReviews(
        userId: Long,
        item: Service,
    ): Result<ResponseReply> {


        return try {
            val response = AuthClient.instance.create(AuthService::class.java)
                .getReviewsByServiceId(
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


    fun loadReViewRepliesSelectedItem(
        serviceReview: ServiceReview,
    ) {

        viewModelScope.launch {

            _isReviewsReplyLoading.value = true

            _selectedCommentId.value = serviceReview.id

            try {
                when (val result = loadSelectedServiceReviewReplies(userId, serviceReview)) {
                    is Result.Success -> {

                        // Deserialize the search terms and set suggestions
                        val serviceReviews = Gson().fromJson(
                            result.data.data,
                            object : TypeToken<List<ServiceReviewReply>>() {}.type
                        ) as List<ServiceReviewReply>

                        _selectedReviewReplies.value = serviceReviews

                    }

                    is Result.Error -> {
                        // Handle error
                        error = mapExceptionToError(result.error).errorMessage
                        // Optionally log the error message
                    }
                }
            } catch (t: Exception) {
                error = "Something Went Wrong"

            } finally {

                _isReviewsReplyLoading.value = false
            }
        }
    }


    private suspend fun loadSelectedServiceReviewReplies(
        userId: Long,
        serviceReview: ServiceReview,
    ): Result<ResponseReply> {


        return try {
            val response = AuthClient.instance.create(AuthService::class.java)
                .getReplyReviewsByCommentId(
                    serviceReview.id
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


    // StateFlow for tracking the review posting status
    private val _isReviewPosting = MutableStateFlow(false)
    val isReviewPosting = _isReviewPosting.asStateFlow()


    fun insertReview(serviceId: Long, userId: Long, reviewText: String) {
        viewModelScope.launch {
            _isReviewPosting.value = true

            try {
                // Insert review
                when (val result = insertReviewApiCall(serviceId, userId, reviewText)) {
                    is Result.Success -> {


                        val insertedReview = Gson().fromJson<ServiceReview>(
                            result.data.data,
                            ServiceReview::class.java
                        )

                        _selectedReviews.value = _selectedReviews.value.toMutableList()
                            .apply {
                                add(0, insertedReview)
                            }

                        // Handle success, maybe update the UI with the inserted review ID
                    }

                    is Result.Error -> {
                        // Handle error, show error message to the user
                        error = mapExceptionToError(result.error).errorMessage


                    }
                }
            } catch (t: Exception) {
                error = "Something went wrong while posting the review."
            } finally {
                _isReviewPosting.value = false
            }
        }
    }

    private suspend fun insertReviewApiCall(
        serviceId: Long,
        userId: Long,
        reviewText: String
    ): Result<ResponseReply> {
        return try {
            val response = AuthClient.instance.create(AuthService::class.java)
                .insertReview(serviceId, userId, reviewText)

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


    // StateFlow for tracking the reply posting status
    private val _isReplyPosting = MutableStateFlow(false)
    val isReplyPosting = _isReplyPosting.asStateFlow()


    fun insertReply(commentId: Int, userId: Long, replyText: String, replyToUserId: Long?) {
        viewModelScope.launch {
            _isReplyPosting.value = true

            try {
                // Insert reply
                when (val result =
                    insertReplyApiCall(commentId, userId, replyText, replyToUserId)) {
                    is Result.Success -> {


                        val insertedReply = Gson().fromJson<ServiceReviewReply>(
                            result.data.data,
                            ServiceReviewReply::class.java
                        )

                        _selectedReviewReplies.value = _selectedReviewReplies.value.toMutableList()
                            .apply {
                                add(0, insertedReply)
                            }

                    }

                    is Result.Error -> {
                        // Handle error, show error message to the user
                        error = mapExceptionToError(result.error).errorMessage
                    }
                }
            } catch (t: Exception) {
                error = "Something went wrong while posting the reply."
            } finally {
                _isReplyPosting.value = false
            }
        }
    }

    private suspend fun insertReplyApiCall(
        commentId: Int,
        userId: Long,
        replyText: String,
        replyToUserId: Long?
    ): Result<ResponseReply> {
        return try {
            val response = AuthClient.instance.create(AuthService::class.java)
                .insertReply(commentId, userId, replyText, replyToUserId)

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







