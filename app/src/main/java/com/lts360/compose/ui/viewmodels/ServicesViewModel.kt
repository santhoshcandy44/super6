package com.lts360.compose.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
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
import com.lts360.api.utils.Result
import com.lts360.api.utils.mapExceptionToError
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.coroutineContext
import kotlin.text.toInt

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



class ServicesRepository @Inject constructor(
    val userProfileDao: UserProfileDao,
    private val guestIndustryDao: GuestIndustryDao,
    private val guestUserLocationDao: UserLocationDao,
    val chatUserDao: ChatUserDao,
    val chatUserRepository: ChatUserRepository,
    networkConnectivityManager: NetworkConnectivityManager,
) {

    var submittedQuery: String? = null
    var onlySearchBar = false
    private var key = 0

    val connectivityManager = networkConnectivityManager


    private val _selectedItem = MutableStateFlow<Service?>(null)
    val selectedItem = _selectedItem.asStateFlow()

    private val _lastLoadedItemPosition = MutableStateFlow(-1)
    val lastLoadedItemPosition = _lastLoadedItemPosition.asStateFlow()


    private var error: String = ""

    private var _pageSource = PageSource(pageSize = 30)
    val pageSource: PageSource get() = _pageSource


    private var loadingItemsJob: Job? = null

    private val _nestedServiceOwnerProfileSelectedItem = MutableStateFlow<Service?>(null)
    val nestedServiceOwnerProfileSelectedItem = _nestedServiceOwnerProfileSelectedItem.asStateFlow()

    private val _isReviewsLoading = MutableStateFlow(false)
    val isReviewsLoading = _isReviewsLoading.asStateFlow()


    private val _isReviewsReplyLoading = MutableStateFlow(false)
    val isReviewsReplyLoading = _isReviewsReplyLoading.asStateFlow()


    private val _selectedCommentId = MutableStateFlow(-1)
    val selectedCommentId = _selectedCommentId.asStateFlow()

    private val _selectedReviews = MutableStateFlow<List<ServiceReview>>(emptyList())
    val selectedReviews = _selectedReviews.asStateFlow()


    private val _selectedReviewReplies = MutableStateFlow<List<ServiceReviewReply>>(emptyList())
    val selectedReviewReplies = _selectedReviewReplies.asStateFlow()

    private val _isReviewPosting = MutableStateFlow(false)
    val isReviewPosting = _isReviewPosting.asStateFlow()

    private val _isReplyPosting = MutableStateFlow(false)
    val isReplyPosting = _isReplyPosting.asStateFlow()


    suspend fun loadServices(userId:Long, isGuest: Boolean = false) {
        if (isGuest) {
            val userLocation = guestUserLocationDao.getLocation(userId)
            val selectedIndustries = if (onlySearchBar) {
                emptyList()
            } else {
                guestIndustryDao.getSelectedIndustries().map { it.industryId }
            }

            pageSource.guestNextPage(
                userId,
                submittedQuery,
                selectedIndustries,
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

    fun updateSelectedItem(item: Service?) {
        _selectedItem.value = item
    }

    fun updateNestedServiceOwnerProfileSelectedItem(item: Service?) {
        _nestedServiceOwnerProfileSelectedItem.value = item
    }

    fun updateServiceBookMarkedInfo(serviceId: Long, isBookMarked: Boolean) {
        _pageSource.updateServiceBookMarkedInfo(serviceId, isBookMarked)
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

    private suspend fun loadSelectedServiceReviews(item: Service): Result<ResponseReply> {


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

    private suspend fun loadSelectedServiceReviewRepliesApiCall(serviceReview: ServiceReview): Result<ResponseReply> {


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

    suspend fun removeBookmark(
        userId: Long,
        item: Service
    ): Result<ResponseReply> {


        return try {
            AppClient.instance.create(ManageServicesApiService::class.java)
                .removeBookmarkService(
                    userId,
                    item.serviceId
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


    suspend fun bookmark(
        userId: Long,
        item: Service,
    ): Result<ResponseReply> {
        return try {
            AppClient.instance.create(ManageServicesApiService::class.java)
                .bookmarkService(
                    userId,
                    item.serviceId
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
                        } catch (e: Exception) {
                            "An unknown error occurred"
                        }
                        Result.Error(Exception(errorMessage))
                    }
                }


        } catch (t: Throwable) {
            Result.Error(t)
        }

    }

}


@HiltViewModel
class ServicesViewModel @Inject constructor(
    val userProfileDao: UserProfileDao,
    private val guestIndustryDao: GuestIndustryDao,
    private val guestUserLocationDao: UserLocationDao,
    val chatUserDao: ChatUserDao,
    val tokenManager: TokenManager,
    val chatUserRepository: ChatUserRepository,
    val networkConnectivityManager: NetworkConnectivityManager,
) : ViewModel() {

    val userId = UserSharedPreferencesManager.userId

    val isGuest = tokenManager.isGuest()

    val repositories = mutableMapOf<Int, ServicesRepository>()

    init {
        loadServiceRepository("", false, 0)
        loadServices(0)
    }

    fun loadServiceRepository(
        submittedQuery: String,
        onlySearchBar: Boolean,
        key: Int
    ) {
        repositories.put(
            key, ServicesRepository(
                userProfileDao,
                guestIndustryDao,
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


    fun loadServices(key: Int) {
        viewModelScope.launch {
            getServiceRepository(key)
                .apply {
                    loadServices(userId, isGuest)
                }
        }
    }

    fun getServiceRepository(key: Int): ServicesRepository =
        repositories[key] ?: throw IllegalStateException("No repository found")

    fun getKey(key: Int): Int =
        repositories[key]?.getKey() ?: throw IllegalStateException("No repository found")


    fun setNestedServiceOwnerProfileSelectedItem(key: Int, service: Service?) {
        getServiceRepository(key)
            .apply {
                updateNestedServiceOwnerProfileSelectedItem(service)
            }
    }

    fun setSelectedItem(key: Int, item: Service?) {
        getServiceRepository(key).apply {
            updateSelectedItem(item)
        }
    }

    fun directUpdateServiceIsBookMarked(key: Int, serviceId: Long, isBookMarked: Boolean) {
        getServiceRepository(key).apply {
            updateServiceBookMarkedInfo(serviceId, isBookMarked)
        }
    }


    fun updateLastLoadedItemPosition(key: Int, newPosition: Int) {
        viewModelScope.launch {
            getServiceRepository(key)
                .apply {
                    updateLastLoadedItemPosition(newPosition)
                }

        }
    }


    suspend fun getChatUser(userId: Long, userProfile: FeedUserProfileInfo): ChatUser {
        return withContext(Dispatchers.IO) {

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

    fun nextPage(key: Int, userId: Long, query: String?) {
        viewModelScope.launch {

            getServiceRepository(key)
                .apply {

                    if (isGuest) {
                        val userLocation = guestUserLocationDao.getLocation(userId)
                        val selectedIndustries = if (onlySearchBar) emptyList()
                        else guestIndustryDao.getSelectedIndustries().map { it.industryId }

                        pageSource.guestNextPage(
                            userId,
                            query,
                            selectedIndustries,
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
            getServiceRepository(key)
                .apply {
                    if (isGuest) {
                        val userLocation = guestUserLocationDao.getLocation(userId)
                        val selectedIndustries = if (onlySearchBar) emptyList()
                        else guestIndustryDao.getSelectedIndustries().map { it.industryId }

                        pageSource.guestRefresh(
                            userId,
                            query,
                            selectedIndustries,
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
            getServiceRepository(key)
                .apply {
                    if (isGuest) {
                        val userLocation = guestUserLocationDao.getLocation(userId)
                        val selectedIndustries = if (onlySearchBar) emptyList()
                        else guestIndustryDao.getSelectedIndustries().map { it.industryId }

                        pageSource.guestRetry(
                            userId,
                            query,
                            selectedIndustries,
                            userLocation?.latitude,
                            userLocation?.longitude
                        )
                    } else {
                        pageSource.retry(userId, query)
                    }
                }

        }
    }

    /*
        fun loadReViewsSelectedItem(key: Int, service: Service) {

            viewModelScope.launch {

                getServiceRepository(key)
                    .apply {
                        updateSelectedItem(service)
    //                    _isReviewsLoading.value = true

                        try {
                            when (val result = loadSelectedServiceReviews(service)) {
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
                                    kotlin.error = mapExceptionToError(result.error).errorMessage
                                    // Optionally log the error message
                                }
                            }
                        } catch (t: Exception) {
                            kotlin.error = "Something Went Wrong"

                        } finally {
    //                        _isReviewsLoading.value = false
                        }

                    }

            }
        }
    */

    /*
        fun loadReViewRepliesSelectedItem(serviceReview: ServiceReview) {

            viewModelScope.launch {

                _isReviewsReplyLoading.value = true

                _selectedCommentId.value = serviceReview.id

                try {
                    when (val result = loadSelectedServiceReviewRepliesApiCall(serviceReview)) {
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
                            kotlin.error = mapExceptionToError(result.error).errorMessage
                            // Optionally log the error message
                        }
                    }
                } catch (t: Exception) {
                    kotlin.error = "Something Went Wrong"

                } finally {

                    _isReviewsReplyLoading.value = false
                }
            }
        }
    */

    /*
        fun insertReview(serviceId: Long, userId: Long, reviewText: String) {
            viewModelScope.launch {
                _isReviewPosting.value = true

                try {
                    // Insert review
                    when (val result = insertReviewApiCall(serviceId, userId, reviewText)) {
                        is Result.Success -> {


                            val insertedReview = Gson().fromJson(
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
                            kotlin.error = mapExceptionToError(result.error).errorMessage


                        }
                    }
                } catch (t: Exception) {
                    kotlin.error = "Something went wrong while posting the review."
                } finally {
                    _isReviewPosting.value = false
                }
            }
        }
    */

    /*
        fun insertReply(commentId: Int, userId: Long, replyText: String, replyToUserId: Long?) {
            viewModelScope.launch {
                _isReplyPosting.value = true

                try {
                    // Insert reply
                    when (val result =
                        insertReplyApiCall(commentId, userId, replyText, replyToUserId)) {
                        is Result.Success -> {


                            val insertedReply = Gson().fromJson(
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
                            kotlin.error = mapExceptionToError(result.error).errorMessage
                        }
                    }
                } catch (t: Exception) {
                    kotlin.error = "Something went wrong while posting the reply."
                } finally {
                    _isReplyPosting.value = false
                }
            }
        }
    */



    fun onBookmark(
        key: Int,
        userId: Long,
        service: Service,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch {
            getServiceRepository(key)
                .apply {

                    try {
                        when (val result = bookmark(userId, service)) {
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
        service: Service,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch {
            getServiceRepository(key)
                .apply {

                    try {
                        when (val result = removeBookmark(userId, service)) {
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


    fun formatDistance(distance: Double?): String {
        return if (distance != null) {
            if (distance % 1.0 == 0.0) {
                "${distance.toInt()} km"
            } else {
                "%.1f km".format(distance)
            }
        } else {
            "Distance not available"
        }
    }

}







