package com.lts360.compose.ui.bookmarks

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken
import com.lts360.api.utils.Result
import com.lts360.api.utils.ResultError
import com.lts360.api.utils.mapExceptionToError
import com.lts360.api.app.ApiService
import com.lts360.api.app.AppClient
import com.lts360.api.app.ManageLocalJobService
import com.lts360.api.app.ManageServicesApiService
import com.lts360.api.app.ManageUsedProductListingService
import com.lts360.api.auth.managers.TokenManager
import com.lts360.api.common.errors.ErrorResponse
import com.lts360.api.common.responses.ResponseReply
import com.lts360.api.models.service.BookMarkedItem
import com.lts360.api.models.service.FeedUserProfileInfo
import com.lts360.api.models.service.Service
import com.lts360.api.models.service.UsedProductListing
import com.lts360.app.database.daos.chat.ChatUserDao
import com.lts360.app.database.models.chat.ChatUser
import com.lts360.compose.ui.chat.repos.ChatUserRepository
import com.lts360.compose.ui.localjobs.models.LocalJob
import com.lts360.compose.ui.managers.UserGeneralPreferencesManager
import com.lts360.compose.ui.managers.UserSharedPreferencesManager
import com.lts360.compose.ui.profile.repos.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.reflect.Type
import javax.inject.Inject

class BookMarkedItemTypeAdapter : JsonDeserializer<BookMarkedItem?> {

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): BookMarkedItem? {
        val jsonObject = json?.asJsonObject
        val type = jsonObject?.get("type")?.asString
        return when (type) {
            "service" -> context?.deserialize<Service>(json, Service::class.java)
            "used_product_listing" -> context?.deserialize<UsedProductListing>(
                json,
                UsedProductListing::class.java
            )

            "local_job" -> context?.deserialize<LocalJob>(json, LocalJob::class.java)
            else -> throw JsonParseException("Unknown type: $type")
        }
    }

}

@HiltViewModel
class BookmarksViewModel @Inject constructor(
    val savedStateHandle: SavedStateHandle,
    val tokenManager: TokenManager,
    private val chatUserDao: ChatUserDao,
    val chatUserRepository: ChatUserRepository,
    userProfileRepository: UserProfileRepository,
    private val userGeneralPreferencesManager: UserGeneralPreferencesManager

) : ViewModel() {

    val userId = UserSharedPreferencesManager.userId

    val signInMethod = tokenManager.getSignInMethod()


    val isPhoneNumberVerified = userProfileRepository
        .getUserProfileSettingsInfoFlow(userId)
        .map { it?.phoneCountryCode != null && it.phoneNumber != null }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    val isDontAskAgainChecked =
        userGeneralPreferencesManager.isDontAskAgainChecked

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()


    private val _error = MutableStateFlow<ResultError?>(null)
    val error = _error.asStateFlow()

    private var errorMessage: String = ""

    private val _bookmarks = MutableStateFlow(emptyList<BookMarkedItem>())
    val bookmarks = _bookmarks.asStateFlow()


    private val _selectedItem = MutableStateFlow<BookMarkedItem?>(null)
    val selectedItem = _selectedItem.asStateFlow()

    private val _isApplying = MutableStateFlow(false)
    val isApplying = _isApplying.asStateFlow()

    init {
        onFetchUserBookmarks(userId)
    }


    fun updateError(exception: Throwable?) {

        _error.value = exception?.let {
            mapExceptionToError(it)
        }
    }


    fun setSelectedItem(item: BookMarkedItem?) {
        _selectedItem.value = item
    }


    fun removeServiceItem(service: Service) {
        _bookmarks.value = _bookmarks.value.filter {
            !(it is Service && it.type == "service" && it.serviceId == service.serviceId)
        }
    }

    fun isSelectedBookmarkNull() = _selectedItem.value == null

    fun removeUsedProductListingItem(service: UsedProductListing) {
        _bookmarks.value = _bookmarks.value.filter {
            !(it is UsedProductListing && it.type == "used_product_listing" && it.productId == service.productId)
        }
    }

    fun removeLocalJobItem(item: LocalJob) {
        _bookmarks.value = _bookmarks.value.filter {
            !(it is LocalJob && it.type == "local_job" && it.localJobId == item.localJobId)
        }
    }

    fun updateServiceItem(updateService: Service, isBookmarked: Boolean) {
        _bookmarks.value = _bookmarks.value.map {
            if (it is Service && it.type == "service" && it.serviceId == updateService.serviceId) {
                it.copy(isBookmarked = isBookmarked)
            } else {
                it
            }
        }
    }

    fun updateUsedProductItem(usedProductListing: UsedProductListing, isBookmarked: Boolean) {
        _bookmarks.value = _bookmarks.value.map {
            if (it is UsedProductListing && it.type == "used_product_listing" && it.productId == usedProductListing.productId) {
                it.copy(isBookmarked = isBookmarked)
            } else {
                it
            }
        }
    }


    fun updateLocalJob(item: LocalJob, isBookmarked: Boolean) {
        _bookmarks.value = _bookmarks.value.map {
            if (it is LocalJob && it.type == "local_job" && it.localJobId == item.localJobId) {
                it.copy(isBookmarked = isBookmarked)
            } else {
                it
            }
        }
    }


    fun setLocalJobPersonalInfoPromptIsDontAskAgainChecked(value: Boolean) {
        viewModelScope.launch {
            userGeneralPreferencesManager.setLocalJobPersonalInfoPromptIsDontAskAgainChecked(value)
        }
    }

    suspend fun getChatUser(userId: Long, userProfile: FeedUserProfileInfo): ChatUser {


        return withContext(Dispatchers.IO) {
            val chatUser = chatUserDao.getChatUserByRecipientId(userProfile.userId)

            chatUser ?: run {
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


    fun onFetchUserBookmarks(
        userId: Long,
        isLoading: Boolean = true,
        isRefreshing: Boolean = false,
        onSuccess: (String) -> Unit = {},
        onError: (String) -> Unit = {},
    ) {

        viewModelScope.launch {

            try {
                if (isLoading) {
                    _isLoading.value = true
                }
                if (isRefreshing) {
                    _isRefreshing.value = true
                }

                when (val result = fetchUserBookmarks(userId)) {
                    is Result.Success -> {

                        val gson = GsonBuilder()
                            .registerTypeAdapter(
                                BookMarkedItem::class.java,
                                BookMarkedItemTypeAdapter()
                            )
                            .create()

                        val itemListType = object : TypeToken<List<BookMarkedItem>>() {}.type
                        val items: List<BookMarkedItem> =
                            gson.fromJson(result.data.data, itemListType)
                        _bookmarks.value = items

                        onSuccess(result.data.message)
                        updateError(null)

                    }

                    is Result.Error -> {
                        if (result.error is CancellationException) {
                            return@launch
                        }
                        updateError(result.error)
                        errorMessage = mapExceptionToError(result.error).errorMessage
                        onError(errorMessage)
                    }
                }

            } catch (t: Throwable) {
                t.printStackTrace()
                errorMessage = "Something Went Wrong"
                onError(errorMessage)
            } finally {
                if (isLoading) {
                    _isLoading.value = false

                }
                if (isRefreshing) {
                    _isRefreshing.value = false
                }
            }
        }


    }


    fun onRemoveBookmark(
        userId: Long,
        item: Service,
        onSuccess: () -> Unit,
        onError: () -> Unit,
    ) {

        viewModelScope.launch {
            when (removeBookmark(userId, item)) {
                is Result.Success -> {
                    onSuccess()
                }

                is Result.Error -> {
                    onError()
                }
            }
        }
    }


    fun onRemoveUsedProductListingBookmark(
        userId: Long,
        service: UsedProductListing,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch {
            try {
                when (val result = removeUsedProductListingBookmark(userId, service)) {
                    is Result.Success -> {
                        onSuccess()
                    }

                    is Result.Error -> {
                        errorMessage = mapExceptionToError(result.error).errorMessage
                        onError(errorMessage)
                    }
                }
            } catch (_: Exception) {
                errorMessage = "Something Went Wrong"
                onError(errorMessage)

            }
        }
    }


    fun onRemoveLocalJobBookmark(
        userId: Long,
        item: LocalJob,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch {
            try {
                when (val result = removeLocalJobBookmark(userId, item)) {
                    is Result.Success -> {
                        onSuccess()
                    }

                    is Result.Error -> {
                        errorMessage = mapExceptionToError(result.error).errorMessage
                        onError(errorMessage)
                    }
                }
            } catch (_: Exception) {
                errorMessage = "Something Went Wrong"
                onError(errorMessage)

            }
        }
    }

    fun onApplyLocalJob(
        userId: Long,
        localJobId: Long,
        onSuccess: (String) -> Unit={},
        onError: (String) -> Unit={}
    ) {
        _isApplying.value = true
        viewModelScope.launch {
            try {
                when (val result = applyLocalJob(userId, localJobId)) {
                    is Result.Success -> {
                        onSuccess(result.data.message)
                    }

                    is Result.Error -> {
                        val error = mapExceptionToError(result.error).errorMessage
                        onError(error)
                    }
                }
            } catch (_: Exception) {
                val error = "Something Went Wrong"
                onError(error)

            }
            finally {
                _isApplying.value = false
            }
        }
    }


    private suspend fun fetchUserBookmarks(
        userId: Long,
    ): Result<ResponseReply> {


        return try {
            val response = AppClient.instance.create(ApiService::class.java)
                .getUserBookmarks(userId)

            if (response.isSuccessful) {
                // Handle successful response
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
            t.printStackTrace()
            Result.Error(t)
        }
    }

    private suspend fun removeLocalJobBookmark(
        userId: Long,
        item: LocalJob,
    ): Result<ResponseReply> {


        return try {
            val response = AppClient.instance.create(ManageLocalJobService::class.java)
                .removeBookmarkLocalJob(
                    userId,
                    item.localJobId
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
                } catch (_: Exception) {
                    "An unknown error occurred"
                }
                Result.Error(Exception(errorMessage))
            }

        } catch (t: Exception) {
            Result.Error(t)
        }
    }

    private suspend fun removeUsedProductListingBookmark(
        userId: Long,
        item: UsedProductListing,
    ): Result<ResponseReply> {


        return try {
            val response = AppClient.instance.create(ManageUsedProductListingService::class.java)
                .removeBookmarkUsedProductListing(
                    userId,
                    item.productId
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
                } catch (_: Exception) {
                    "An unknown error occurred"
                }
                Result.Error(Exception(errorMessage))
            }

        } catch (t: Exception) {
            Result.Error(t)
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
            val body = response.body()

            if (response.isSuccessful) {
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
                } catch (_: Exception) {
                    "An unknown error occurred"
                }
                Result.Error(Exception(errorMessage))
            }

        } catch (t: Exception) {
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

