package com.lts360.compose.ui.onboarding.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.lts360.api.utils.Result
import com.lts360.api.utils.mapExceptionToError
import com.lts360.api.auth.managers.TokenManager
import com.lts360.app.database.daos.prefs.BoardDao
import com.lts360.app.database.daos.profile.UserProfileDao
import com.lts360.app.database.models.profile.RecentLocation
import com.lts360.app.database.models.profile.UserLocation
import com.lts360.app.database.models.profile.UserProfile
import com.lts360.compose.ui.auth.repos.AuthRepository
import com.lts360.compose.ui.main.models.CurrentLocation
import com.lts360.compose.ui.main.models.LocationRepository
import com.lts360.compose.ui.main.prefs.viewmodels.BoardPref
import com.lts360.compose.ui.main.prefs.viewmodels.BoardsPreferencesRepository
import com.lts360.compose.ui.managers.UserSharedPreferencesManager
import com.lts360.compose.ui.onboarding.GuestIdUtil.generateGuestId
import com.lts360.compose.ui.settings.viewmodels.RegionalSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class LocationAccessViewModel @Inject constructor(
    val userProfileDao: UserProfileDao,
    val boardDao:BoardDao,
    tokenManager: TokenManager,
    val authRepository: AuthRepository,
    private val locationRepository: LocationRepository,
    private val boardsPreferencesRepository: BoardsPreferencesRepository,
    private val regionalSettingsRepository: RegionalSettingsRepository
) : ViewModel() {


    val userId = UserSharedPreferencesManager.userId
    val signInMethod = tokenManager.getSignInMethod()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()


    var error = ""

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


    fun setGuestCurrentLocationAndCreateAccount(
        context: Context,
        currentLocation: CurrentLocation,
        onSuccess: () -> Unit,
        onError: (String) -> Unit) {

        _isLoading.value = true

        val generateGuestId = generateGuestId(context)

        if (generateGuestId == null) {
            onError("Couldn't create guest account")
            return
        }

        viewModelScope.launch {
            try {
                when (val result = boardsPreferencesRepository.guestGetBoards(userId)) { // Call the network function
                    is Result.Success -> {

                        withContext(Dispatchers.IO){
                            boardDao.clearAndInsertSelectedBoards((Gson().fromJson(
                                result.data.data,
                                object : TypeToken<List<BoardPref>>() {}.type
                            ) as List<BoardPref>)
                                .map { boardItem ->
                                    boardItem
                                }
                            )


                            userProfileDao.insert(
                                UserProfile(
                                    generateGuestId, "Guest", "",
                                    null, null, "",
                                    "guest",
                                    System.currentTimeMillis().toString(),
                                    System.currentTimeMillis().toString()
                                )
                            )

                            locationRepository.onGuestSaveLocationCoordinates(
                                generateGuestId,
                                currentLocation.latitude,
                                currentLocation.longitude,
                                currentLocation.locationType,
                                currentLocation.geo
                            )

                            authRepository.saveUserId(generateGuestId)
                            authRepository.saveGuestSignInInfo()
                        }

                        onSuccess()
                    }

                    is Result.Error -> {
                        if (result.error is CancellationException) {
                            return@launch
                        }
                        val error = mapExceptionToError(result.error)
                        onError( error.errorMessage)
                    }

                }

            } catch (t: Throwable) {
                onError("Something went wrong")
                t.printStackTrace()
            } finally {

                if (_isLoading.value) {
                    _isLoading.value = false
                }

            }
        }

    }


    fun selectCountry(isoCountryCode: String) {
        regionalSettingsRepository.saveCountryToPreferences(isoCountryCode)
    }

}