package com.lts360.compose.ui.main.viewmodels


import android.content.Context
import android.widget.Toast
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.lts360.api.Utils.Result
import com.lts360.api.Utils.mapExceptionToError
import com.lts360.api.auth.managers.TokenManager
import com.lts360.app.database.daos.profile.BoardsDao
import com.lts360.app.database.daos.profile.UserProfileDao
import com.lts360.app.database.models.profile.RecentLocation
import com.lts360.app.database.models.profile.UserLocation
import com.lts360.compose.ui.main.models.CurrentLocation
import com.lts360.compose.ui.main.models.LocationRepository
import com.lts360.compose.ui.managers.LocationCoordinates
import com.lts360.compose.ui.managers.UserSharedPreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    val savedStateHandle: SavedStateHandle,
    val locationRepository: LocationRepository,
    userProfileDao: UserProfileDao,
    tokenManager: TokenManager,
    val boardsDao: BoardsDao
) : ViewModel() {

    val userId = UserSharedPreferencesManager.userId
    val signInMethod = tokenManager.getSignInMethod()

    val type = if (tokenManager.isVerifiedUser()) "verified_user" else "guest"


    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()


    private val _selectedLocationType = MutableStateFlow<String?>(null)
    val locationType = _selectedLocationType.asStateFlow()

    private val _selectedLocationGeo = MutableStateFlow<String?>(null)
    val selectedLocationGeo = _selectedLocationGeo.asStateFlow()


    private val _selectedLocation = MutableStateFlow<LocationCoordinates?>(null)
    val selectedLocation = _selectedLocation.asStateFlow()


    var error = ""


    init {

        viewModelScope.launch (Dispatchers.IO){
            val pinnedBoards = boardsDao.getPinnedBoards()

            withContext(Dispatchers.Main){
                if(pinnedBoards.isNotEmpty()){

                    withContext(Dispatchers.IO){
                        boardsDao.updateBoard(pinnedBoards[0].copy(boardName = "Service DO"))

                    }


                    Toast.makeText(context, pinnedBoards[0].boardName, Toast.LENGTH_SHORT)
                        .show()

                }else{
                    Toast.makeText(context, "Boards empty", Toast.LENGTH_SHORT)
                        .show()
                }

            }


            userProfileDao.getUserProfileDetailsFlow(userId).collectLatest { userProfileDetails ->
                userProfileDetails?.let { userProfileDetailsNonNull ->

                    userProfileDetailsNonNull.userLocation?.let {
                        setLocationType(it.locationType)
                        setLocationGeo(it.geo)

                        if (it.latitude != null && it.longitude != null) {
                            setLocationCoordinates(
                                LocationCoordinates(
                                    it.latitude,
                                    it.longitude
                                )
                            )
                        }
                    }
                }
            }
        }

    }


    fun setLocationType(locationType: String?) {
        _selectedLocationType.value = locationType
    }

    fun setLocationGeo(locationGeo: String?) {
        _selectedLocationGeo.value = locationGeo

    }

    fun setLocationCoordinates(locationCoordinates: LocationCoordinates) {
        _selectedLocation.value = locationCoordinates
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
    ) {
        viewModelScope.launch {
            locationRepository.onGuestSaveLocationCoordinates(
                userId,
                currentLocation.latitude,
                currentLocation.longitude,
                currentLocation.locationType,
                currentLocation.geo
            )
        }
    }

    fun setGuestRecentLocation(
        userId: Long,
        currentLocation: RecentLocation,
    ) {
        viewModelScope.launch {
            locationRepository.onGuestSaveLocationCoordinates(
                userId,
                currentLocation.latitude,
                currentLocation.longitude,
                currentLocation.locationType,
                currentLocation.geo
            )
        }
    }



}
