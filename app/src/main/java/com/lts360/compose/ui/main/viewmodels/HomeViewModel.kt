package com.lts360.compose.ui.main.viewmodels

import android.content.Context
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.lts360.api.utils.Result
import com.lts360.api.utils.mapExceptionToError
import com.lts360.api.app.AppClient
import com.lts360.api.app.ManageLocalJobService
import com.lts360.api.app.ManageServicesApiService
import com.lts360.api.app.ManageUsedProductListingService
import com.lts360.api.auth.managers.TokenManager
import com.lts360.api.common.errors.ErrorResponse
import com.lts360.api.common.responses.ResponseReply
import com.lts360.api.models.service.Service
import com.lts360.api.models.service.UsedProductListing
import com.lts360.app.database.daos.profile.UserProfileDao
import com.lts360.app.database.models.profile.RecentLocation
import com.lts360.app.database.models.profile.UserLocation
import com.lts360.compose.ui.localjobs.models.LocalJob
import com.lts360.compose.ui.main.models.CurrentLocation
import com.lts360.compose.ui.main.models.LocationRepository
import com.lts360.compose.ui.main.models.SearchTerm
import com.lts360.compose.ui.main.navhosts.routes.BottomBar
import com.lts360.compose.ui.managers.UserSharedPreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException


@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    val savedStateHandle: SavedStateHandle,
    private val locationRepository: LocationRepository,
    userProfileDao: UserProfileDao,
    tokenManager: TokenManager) : ViewModel() {

    val userId = UserSharedPreferencesManager.userId
    val isGuest = tokenManager.isGuest()

    val type = if (tokenManager.isVerifiedUser()) "verified_user" else "guest"


    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()


    private val _selectedLocationType = MutableStateFlow<String?>(null)
    val locationType = _selectedLocationType.asStateFlow()

    private val _selectedLocationGeo = MutableStateFlow<String?>(null)
    val selectedLocationGeo = _selectedLocationGeo.asStateFlow()


    var error = ""

    private val submittedQuery = savedStateHandle.get<String?>("submittedQuery")

    private val _searchQuery = MutableStateFlow(
        if (submittedQuery != null) TextFieldValue(
            text = submittedQuery,
            selection = TextRange(0)
        ) else TextFieldValue()
    )
    val searchQuery = _searchQuery.asStateFlow()


    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    private var searchJob:Job? = null


    private val _isLazyLoading = MutableStateFlow(false)
    val isLazyLoading = _isLazyLoading.asStateFlow()


    private val _suggestions = MutableStateFlow<List<String>>(emptyList())
    val suggestions = _suggestions.asStateFlow()

    private var selectedServiceItem: Service? = null

    private var selectedServiceOwnerServiceItem: Service? = null

    private var selectedUsedProductListingItem: UsedProductListing? = null

    private var selectedServiceOwnerUsedProductListingItem: UsedProductListing? = null
    private var selectedLocalJobItem: LocalJob? = null

    init {

        viewModelScope.launch (Dispatchers.IO){
            userProfileDao.getUserProfileDetailsFlow(userId).collectLatest { userProfileDetails ->
                userProfileDetails?.let { userProfileDetailsNonNull ->

                    userProfileDetailsNonNull.userLocation?.let {
                        setLocationType(it.locationType)
                        setLocationGeo(it.geo)
                    }
                }
            }
        }
    }

    fun isSelectedServiceItemNull() = selectedServiceItem ==null

    fun isSelectedUsedProductListingItemNull() = selectedUsedProductListingItem == null

    fun isSelectedServiceOwnerServiceItemNull() = selectedServiceOwnerServiceItem == null

    fun isSelectedServiceOwnerUsedProductListingItemNull() = selectedServiceOwnerUsedProductListingItem == null

    fun isSelectedLocalJobItemNull() = selectedLocalJobItem ==null


    fun setSelectedServiceItem(item: Service?) {
        selectedServiceItem = item
    }

    fun setSelectedServiceOwnerServiceItem(item: Service?) {
        selectedServiceOwnerServiceItem = item
    }

    fun setSelectedUsedProductListingItem(item: UsedProductListing?) {
        selectedUsedProductListingItem = item
    }

    fun setSelectedSecondsOwnerUsedProductListingIItem(item: UsedProductListing?) {
        selectedServiceOwnerUsedProductListingItem = item
    }

    fun setSelectedLocalJobItem(item: LocalJob?) {
        selectedLocalJobItem = item
    }

    private fun setLocationType(locationType: String?) {
        _selectedLocationType.value = locationType
    }

    private fun setLocationGeo(locationGeo: String?) {
        _selectedLocationGeo.value = locationGeo

    }

    fun setSuggestions(suggestions: List<String>) {
        _suggestions.value = suggestions
    }


    fun setSearching(isSearching: Boolean) {
        _isSearching.value = isSearching
    }


    fun setSearchQuery(query: String) {
        _searchQuery.value = TextFieldValue(
            text = query,
            selection = TextRange(query.length)
        )
    }

    fun navigateToOverlay(navController: NavController, route: BottomBar) {
        viewModelScope.launch {
            navController.navigate(
                route
            )
        }
        setEmptySuggestions()

    }

    fun collapseSearchAction(forceResetSearchText:Boolean=false) {
        if (_isSearching.value) {
            setEmptySuggestions()
            if(forceResetSearchText){
                _searchQuery.value = TextFieldValue(text = "", selection = TextRange(0))
            }
            setSearching(false)
        }
    }

    private fun setEmptySuggestions(){
        setSuggestions(emptyList())
    }

    fun setCurrentLocation(
        currentLocation: CurrentLocation,
        onSuccess: (String) -> Unit, onError: (String) -> Unit,
    ) {
        onSaveLocationCoordinates(
            userId,
            currentLocation.latitude,
            currentLocation.longitude,
            currentLocation.locationType,
            currentLocation.geo,
            onSuccess, onError
        )
    }

    fun setRecentLocation(
        recentLocation: RecentLocation,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
    ) {
        onSaveLocationCoordinates(
            userId,
            recentLocation.latitude,
            recentLocation.longitude,
            recentLocation.locationType,
            recentLocation.geo,
            onSuccess,
            onError
        )
    }

    private fun onSaveLocationCoordinates(
        userId: Long,
        lat: Double,
        lon: Double,
        type: String,
        geo: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
    ) {

        viewModelScope.launch {

            try {
                _isLoading.value = true
                when (val result = locationRepository.saveLocationCoordinates(
                    userId,
                    lat,
                    lon,
                    type,
                    geo
                )) { // Call the network function
                    is Result.Success -> {

                        val gsonData = Gson().fromJson(result.data.data, JsonObject::class.java)
                        val updatedLocationType = gsonData.get("location_type").asString
                        val updatedLocationLatitude = gsonData.get("latitude").asDouble
                        val updatedLocationLongitude = gsonData.get("longitude").asDouble
                        val updatedLocationGeo = gsonData.get("geo").asString
                        val updatedLocationUpdatedAt = gsonData.get("updated_at").asString

                        locationRepository.insertUserLocation(
                            UserLocation(
                                userId,
                                updatedLocationType,
                                updatedLocationLatitude,
                                updatedLocationLongitude,
                                geo,
                                updatedLocationUpdatedAt
                            )
                        )


                        locationRepository.insertRecentLocation(
                            RecentLocation(
                                latitude = updatedLocationLatitude,
                                longitude = updatedLocationLongitude,
                                locationType = updatedLocationType,
                                geo = updatedLocationGeo
                            )
                        )

                        onSuccess(result.data.message)
                        // Handle success
                        // Proceed to next step or navigate to OTP screen
                    }

                    is Result.Error -> {
                        error = mapExceptionToError(result.error).errorMessage
                        onError(error)
                        // Handle the error and update the UI accordingly
                    }
                }
            } catch (t: Throwable) {
                error = "Something went wrong"
                onError(error)
                t.printStackTrace()
            } finally {
                _isLoading.value = false // Reset loading state
            }
        }
    }

    fun setGuestCurrentLocation(
        userId: Long,
        currentLocation: CurrentLocation,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            locationRepository.onGuestSaveLocationCoordinates(
                userId,
                currentLocation.latitude,
                currentLocation.longitude,
                currentLocation.locationType,
                currentLocation.geo
            )
            onSuccess()
        }
    }

    fun setGuestRecentLocation(
        userId: Long,
        currentLocation: RecentLocation,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            locationRepository.onGuestSaveLocationCoordinates(
                userId,
                currentLocation.latitude,
                currentLocation.longitude,
                currentLocation.locationType,
                currentLocation.geo
            )
            onSuccess()
        }
    }




    fun onGetServiceSearchQuerySuggestions(userId: Long, query: String) {


        if (isGuest) {

            searchJob = viewModelScope.launch {
                try {
                    when (val result = getGuestServiceSearchQuerySuggestions(userId, query)) {
                        is Result.Success -> {
                            val searchTerms = Gson().fromJson(
                                result.data.data,
                                object : TypeToken<List<SearchTerm>>() {}.type
                            ) as List<SearchTerm>
                            setSuggestions(searchTerms.map { it.searchTerm })
                        }

                        is Result.Error -> {
                            setSuggestions(emptyList())
//                            val errorMsg = mapExceptionToError(result.error).errorMessage
                            // Optionally log the error message
                        }
                    }
                } catch (t: Exception) {
                    t.printStackTrace()
                    if (t is CancellationException) {
                        return@launch // Early return on cancellation
                    }
                    setSuggestions(emptyList())
                    // Optionally log the error
                }
            }

        } else {
            searchJob = viewModelScope.launch {
                try {
                    when (val result = getServiceSearchQuerySuggestions(userId, query)) {
                        is Result.Success -> {
                            val searchTerms = Gson().fromJson(
                                result.data.data,
                                object : TypeToken<List<SearchTerm>>() {}.type
                            ) as List<SearchTerm>
                            setSuggestions(searchTerms.map { it.searchTerm })
                        }

                        is Result.Error -> {
                            setSuggestions(emptyList())
//                            val errorMsg = mapExceptionToError(result.error).errorMessage
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

    fun onGetUsedProductListingSearchQuerySuggestions(userId: Long, query: String) {

        if (isGuest) {

            searchJob = viewModelScope.launch {
                try {
                    when (val result = getGuestUsedProductListingSearchQuerySuggestions(userId, query)) {
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
//                            val errorMsg = mapExceptionToError(result.error).errorMessage
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
            searchJob = viewModelScope.launch {
                try {
                    when (val result = getUsedProductListingSearchQuerySuggestions(userId, query)) {
                        is Result.Success -> {
                            val searchTerms = Gson().fromJson(
                                result.data.data,
                                object : TypeToken<List<SearchTerm>>() {}.type
                            ) as List<SearchTerm>
                            setSuggestions(searchTerms.map { it.searchTerm })
                        }

                        is Result.Error -> {
                            setSuggestions(emptyList())
//                            val errorMsg = mapExceptionToError(result.error).errorMessage
                        }
                    }
                } catch (t: Exception) {
                    t.printStackTrace()
                    if (t is CancellationException) {
                        return@launch
                    }
                    setSuggestions(emptyList())
                }
            }

        }

    }

    fun onGetLocalJobSearchQuerySuggestions(userId: Long, query: String) {

        if (isGuest) {

            searchJob = viewModelScope.launch {
                try {
                    when (val result = getGuestLocalJobSearchQuerySuggestions(userId, query)) {
                        is Result.Success -> {
                            val searchTerms = Gson().fromJson(
                                result.data.data,
                                object : TypeToken<List<SearchTerm>>() {}.type
                            ) as List<SearchTerm>
                            setSuggestions(searchTerms.map { it.searchTerm })
                        }

                        is Result.Error -> {
                            setSuggestions(emptyList())
//                            val errorMsg = mapExceptionToError(result.error).errorMessage
                        }
                    }
                } catch (t: Exception) {
                    t.printStackTrace()
                    if (t is CancellationException) {
                        return@launch
                    }
                    setSuggestions(emptyList())
                }
            }

        } else {
            searchJob = viewModelScope.launch {
                try {
                    when (val result = getLocalJobSearchQuerySuggestions(userId, query)) {
                        is Result.Success -> {
                            val searchTerms = Gson().fromJson(
                                result.data.data,
                                object : TypeToken<List<SearchTerm>>() {}.type
                            ) as List<SearchTerm>
                            setSuggestions(searchTerms.map { it.searchTerm })
                        }

                        is Result.Error -> {
                            setSuggestions(emptyList())
//                            val errorMsg = mapExceptionToError(result.error).errorMessage
                        }
                    }
                } catch (t: Exception) {
                    t.printStackTrace()
                    if (t is CancellationException) {
                        return@launch
                    }
                    setSuggestions(emptyList())
                }
            }

        }
    }


    private suspend fun getGuestUsedProductListingSearchQuerySuggestions(
        userId: Long,
        query: String,
    ): Result<ResponseReply> {


        return try {
            val response =
                AppClient.instance.create(ManageUsedProductListingService::class.java)
                    .guestSearchFilterUsedProductListing(userId, query)

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

    private suspend fun getUsedProductListingSearchQuerySuggestions(
        userId: Long,
        query: String,
    ): Result<ResponseReply> {
        return try {
            val response =
                AppClient.instance.create(ManageUsedProductListingService::class.java)
                    .searchFilterUsedProductListing(userId, query)

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



    private suspend fun getGuestLocalJobSearchQuerySuggestions(
        userId: Long,
        query: String,
    ): Result<ResponseReply> {


        return try {
            val response =
                AppClient.instance.create(ManageLocalJobService::class.java)
                    .guestSearchFilterLocalJob(userId, query)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.isSuccessful) {

                    Result.Success(body)
                } else {
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
                Result.Error(Exception("Failed to retrieve suggestions"))
            }
        } catch (e: Exception) {
            if (e is CancellationException) {
                throw e
            }
            Result.Error(e)
        }
    }

    private suspend fun getLocalJobSearchQuerySuggestions(
        userId: Long,
        query: String,
    ): Result<ResponseReply> {
        return try {
            val response =
                AppClient.instance.create(ManageLocalJobService::class.java)
                    .searchFilterLocalJob(userId, query)

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
            if (e is CancellationException) {
                throw e
            }
            Result.Error(e)
        }
    }


    private suspend fun getGuestServiceSearchQuerySuggestions(
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


    private suspend fun getServiceSearchQuerySuggestions(
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


    fun clearJob() {
        searchJob?.cancel()
        searchJob = null
    }

}
