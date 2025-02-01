package com.super6.pot.ui.onboarding


import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.BottomSheetValue
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.super6.pot.R
import com.super6.pot.api.auth.managers.TokenManager
import com.super6.pot.ui.auth.repos.AuthRepository
import com.super6.pot.ui.main.OnBoardGuestUserLocationAccessBottomSheetScreen
import com.super6.pot.ui.main.OnBoardUserLocationBottomSheetScreen
import com.super6.pot.ui.main.models.CurrentLocation
import com.super6.pot.ui.main.models.LocationRepository
import com.super6.pot.ui.managers.UserSharedPreferencesManager
import com.super6.pot.ui.onboarding.GuestIdUtil.generateGuestId
import com.super6.pot.api.Utils.Result
import com.super6.pot.api.Utils.mapExceptionToError
import com.super6.pot.app.database.daos.profile.UserProfileDao
import com.super6.pot.app.database.models.profile.RecentLocation
import com.super6.pot.app.database.models.profile.UserLocation
import com.super6.pot.app.database.models.profile.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class LocationAccessViewModel @Inject constructor(
    val authRepository: AuthRepository,
    val locationRepository: LocationRepository,
    val userProfileDao: UserProfileDao,
    tokenManager: TokenManager,
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
            withContext(Dispatchers.IO) {
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
            }

            authRepository.saveUserId(generateGuestId)
            authRepository.saveGuestSignInInfo()
            onSuccess()
            _isLoading.value = false
        }

    }

}


@Composable
fun LocationAccessScreen(
    type: String,
    onNavigateChooseIndustries: (Long, String) -> Unit,
    viewModel: LocationAccessViewModel = hiltViewModel(),
) {


    val userId = remember { UserSharedPreferencesManager.userId }

    val coroutineScope = rememberCoroutineScope()

    val bottomSheetScaffoldState = androidx.compose.material.rememberBottomSheetScaffoldState()


    BackHandler(bottomSheetScaffoldState.bottomSheetState.currentValue == BottomSheetValue.Expanded) {
        coroutineScope.launch {
            bottomSheetScaffoldState.bottomSheetState.collapse()
        }
    }


    val isLoading by viewModel.isLoading.collectAsState()


    val context = LocalContext.current


    androidx.compose.material.BottomSheetScaffold(
        scaffoldState = bottomSheetScaffoldState,
        sheetContent = {
            if (type == "guest") {
                OnBoardGuestUserLocationAccessBottomSheetScreen(
                    bottomSheetScaffoldState.bottomSheetState.currentValue,
                    { currentLocation ->
                        viewModel.setGuestCurrentLocationAndCreateAccount(
                            context,
                            currentLocation, {
                                Toast.makeText(
                                    context,
                                    "Location updated successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                                onNavigateChooseIndustries(userId, "guest")
                            }) {
                            Toast.makeText(context, it, Toast.LENGTH_SHORT)
                                .show()
                        }
                    },
                    {


                    },
                    { district, callback ->
                        viewModel.setGuestCurrentLocationAndCreateAccount(
                            context,
                            CurrentLocation(
                                district.coordinates.latitude,
                                district.coordinates.longitude,
                                district.district,
                                "approximate"
                            ), {
                                Toast.makeText(
                                    context,
                                    "Location updated successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                                callback()
                                onNavigateChooseIndustries(userId, "guest")

                            }) {
                            Toast.makeText(context, it, Toast.LENGTH_SHORT)
                                .show()
                        }
                    },

                    {
                        coroutineScope.launch {
                            bottomSheetScaffoldState.bottomSheetState.collapse()
                        }
                    })
            } else {


                OnBoardUserLocationBottomSheetScreen(
                    bottomSheetScaffoldState.bottomSheetState.currentValue,
                    { currentLocation ->
                        viewModel.setCurrentLocation(currentLocation, {
                            Toast.makeText(context, it, Toast.LENGTH_SHORT)
                                .show()
                            onNavigateChooseIndustries(userId, "valid_user")

                        }) {
                            Toast
                                .makeText(context, it, Toast.LENGTH_SHORT)
                                .show()
                        }
                    },
                    { recentLocation ->
                        viewModel.setRecentLocation(recentLocation, {
                            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                            onNavigateChooseIndustries(userId, "valid_user")
                        }) {
                            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                        }

                    },
                    { district, callback ->
                        viewModel.setCurrentLocation(
                            CurrentLocation(
                                district.coordinates.latitude,
                                district.coordinates.longitude,
                                district.district,
                                "approximate"
                            ), {
                                callback()
                                Toast.makeText(context, it, Toast.LENGTH_SHORT)
                                    .show()
                                onNavigateChooseIndustries(userId, "valid_user")


                            }) {
                            Toast
                                .makeText(context, it, Toast.LENGTH_SHORT)
                                .show()

                        }
                    },
                    {
                        coroutineScope.launch {
                            bottomSheetScaffoldState.bottomSheetState.collapse()
                        }
                    },
                    isLoading = isLoading
                )
            }

        },
        sheetPeekHeight = 0.dp, // Default height when sheet is collapsed
        sheetGesturesEnabled = false, // Allow gestures to hide/show bottom sheet
//            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    ) { innerPadding ->

        Scaffold { nestedInnerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .padding(nestedInnerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                Image(
                    painter = painterResource(R.drawable.ic_location_access_on_boarding), // Your location image
                    contentDescription = "Location",
                    modifier = Modifier.size(96.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))


                Text(
                    text = "Allow Your Location",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "We need your location to provide personalized experiences.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = {
                    coroutineScope.launch {
                        bottomSheetScaffoldState.bottomSheetState.expand()
                    }
                }) {
                    Text(text = "Sure, I'd like that")
                }


                /*
                            Spacer(modifier = Modifier.height(24.dp))
                            Text("Skip Now", style = MaterialTheme.typography.bodyLarge)
                */
            }
        }


    }


}


