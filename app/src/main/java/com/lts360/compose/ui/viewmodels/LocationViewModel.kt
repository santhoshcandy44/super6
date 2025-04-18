package com.lts360.compose.ui.viewmodels

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.lts360.app.database.daos.profile.RecentLocationDao
import com.lts360.app.database.daos.profile.UserProfileDao
import com.lts360.app.database.models.profile.RecentLocation
import com.lts360.components.findActivity
import com.lts360.components.utils.LogUtils.TAG
import com.lts360.compose.ui.main.District
import com.lts360.compose.ui.main.State
import com.lts360.compose.ui.main.models.CurrentLocation
import com.lts360.compose.ui.main.models.LocationRepository
import com.lts360.compose.ui.main.navhosts.routes.LocationSetUpRoutes
import com.lts360.compose.ui.main.navhosts.routes.RecentLocationSerializer
import com.lts360.compose.ui.main.navhosts.routes.StateSerializer
import com.lts360.compose.ui.managers.LocationManager
import com.lts360.compose.ui.managers.UserSharedPreferencesManager
import com.lts360.compose.ui.settings.viewmodels.RegionalSettingsRepository
import com.lts360.libs.ui.ShortToast
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


@HiltViewModel
class LocationViewModel @Inject constructor(
    val savedStateHandle: SavedStateHandle,
    @ApplicationContext
    context: Context,
    val userProfileDao: UserProfileDao,
    private val recentLocationDao: RecentLocationDao,
    val repository: LocationRepository,
    regionalSettingsRepository: RegionalSettingsRepository
) : ViewModel() {

    private  val countryCode = regionalSettingsRepository.getCountryFromPreferences()?.code
        ?: regionalSettingsRepository.getCountryFromSim()?.code


    private val args = savedStateHandle.toRoute<LocationSetUpRoutes.LocationChooser>()
    val isLocationStatesEnabled = args.locationStatesEnabled


    val userId = UserSharedPreferencesManager.userId

    private val _currentLocation = MutableStateFlow<CurrentLocation?>(null)
    val currentLocation = _currentLocation.asStateFlow()


    private val _recentLocations = MutableStateFlow<List<RecentLocation>>(emptyList())
    val recentLocations = _recentLocations.asStateFlow()


    private var isAnyLocationCaptured = false


    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()


    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    private var locationManager: LocationManager? = null


    private val _isLocationManagerAvailable = MutableStateFlow(false)
    val isLocationManagerAvailable = _isLocationManagerAvailable.asStateFlow()


    private val _locations = MutableStateFlow< Map<String, State>?>(null)
    val locations: StateFlow< Map<String, State>?> = _locations.asStateFlow()


    private val _districts = MutableStateFlow<List<District>>(emptyList())
    val districts: StateFlow<List<District>> = _districts.asStateFlow()


    init {


        if (isLocationStatesEnabled) {
            val stateMap = savedStateHandle.get<String>("locations")
                ?.let {
                    StateSerializer.deserializeLocationsList(it)
                }

            if (stateMap != null) {
                _locations.value = stateMap
                loadDistrictsIf()
            } else {
                countryCode?.let {
                    loadLocations(context, it)
                }
            }

            val recentLocations = savedStateHandle.get<String>("recent_locations")
                ?.let {
                    RecentLocationSerializer.deserializeLocationsList(it)
                }

            viewModelScope.launch {
                _locations.collectLatest { stateMap ->
                    stateMap?.let {
                        savedStateHandle["locations"] = StateSerializer.serializeLocationsList(it)
                    }
                }
            }

            if (recentLocations != null) {
                _recentLocations.value = recentLocations
            } else {
                viewModelScope.launch {
                    recentLocationDao.getAllLocations().collectLatest {
                        _recentLocations.value = it
                    }
                }
            }

            viewModelScope.launch {
                _recentLocations.collectLatest {
                    if (it.isNotEmpty()) {
                        savedStateHandle["recent_locations"] = RecentLocationSerializer.serializeLocationsList(it)
                    }
                }
            }
        }

    }


    private fun loadDistrictsIf() {
        val savedSelectedDistrictKey = savedStateHandle.get<String>("selected_district")
        if (savedSelectedDistrictKey != null) {
            _locations.value?.let {
                _districts.value = it[savedSelectedDistrictKey]?.districts ?: emptyList()
            }
        }
    }

    private fun loadLocations(context: Context, countryCode:String) {
        _locations.value = readJsonFromAssets(context, countryCode)
    }


    private fun readJsonFromAssets(context: Context, countryCode: String): Map<String, State>? =
        runCatching {
            loadJsonFromAssets(context, countryCode)?.let { parseJson(it) }
        }.getOrNull()


    private fun loadJsonFromAssets(context: Context, countryCode:String): String? {
        return try {
            val assetManager = context.assets
            val inputStream: InputStream = when(countryCode){
                "IN" -> assetManager.open("in_locations.json")
                else -> {
                    throw UnsupportedOperationException("Invalid country user")
                }
            }
            val reader = BufferedReader(InputStreamReader(inputStream))
            val stringBuilder = StringBuilder()
            var line: String?

            while (reader.readLine().also { line = it } != null) {
                stringBuilder.append(line)
            }
            reader.close()
            stringBuilder.toString()
        } catch (e: Exception) {
            throw e
        }
    }



    private fun parseJson(data: String): Map<String, State> {
        val json = Json { ignoreUnknownKeys = true }
        return json.decodeFromString<Map<String, State>>(data)
    }


    fun removeSelectedDistrictSavedState() {
        savedStateHandle.remove<String>("selected_district")
    }

    fun updateDistricts(key: String) {

        savedStateHandle["selected_district"] = key
        _locations.value?.let {
            _districts.value = it[key]?.districts ?: emptyList()
        }

    }


    private fun setLoading(isLoading: Boolean) {
        _isLoading.value = isLoading
    }


    private fun setLocationCaptured(isCaptured: Boolean) {
        isAnyLocationCaptured = isCaptured
    }

    fun requestLocationUpdates() {
        locationManager?.requestLocationUpdates()
    }

    fun removeLocationUpdates() {
        locationManager?.removeLocationUpdates()
        setLoading(false)
    }

    fun onLocationPermissionDenied() {
        setLoading(false)
    }


    fun locationManagerInitialize(context: Context) {

        locationManager = LocationManager(
            context,
            fusedLocationClient,
            callbackPreciseLocationUpdate = { coordinates ->
                if (coordinates != null && !isAnyLocationCaptured) {
                    setLoading(false)
                    setLocationCaptured(true)
                    handleLocationUpdate(
                        coordinates.latitude,
                        coordinates.longitude,
                        "precise",
                        context
                    ) {

                        if (it) {
                            Handler(Looper.getMainLooper()).post {
                                ShortToast(context, "Current Location is Retrieved")
                            }

                        } else {

                            Handler(Looper.getMainLooper()).post {
                                ShortToast(context, "Failed to Retrieve Current Location")
                            }

                        }

                    }

                    locationManager?.removeLocationUpdates()

                }
            },
            callbackApproximateLocationUpdate = { coordinates ->
                if (coordinates != null && !isAnyLocationCaptured) {
                    setLocationCaptured(true)
                    handleLocationUpdate(
                        coordinates.latitude,
                        coordinates.longitude,
                        "approximate",
                        context
                    ) {

                        if (it) {
                            Handler(Looper.getMainLooper()).post {
                                ShortToast(context, "Current Location is Retrieved")
                            }
                        } else {

                            Handler(Looper.getMainLooper()).post {
                                ShortToast(context, "Failed to Retrieve Current Location")
                            }
                        }
                    }
                    locationManager?.removeLocationUpdates()


                }
            }
        )
        _isLocationManagerAvailable.value = true
    }

    fun chooseCurrentLocation(
        context: Context,
        intentSenderResultLauncher: ActivityResultLauncher<IntentSenderRequest>,
        launchLocationPermission: () -> Unit,
    ) {
        setLoading(true)
        setLocationCaptured(false)
        locationManager?.let {
            if (it.checkLocationPermissions()) {
                enableLoc(context, intentSenderResultLauncher)
            } else {
                launchLocationPermission()
            }
        }
    }


    fun enableLoc(
        context: Context,
        intentSenderResultLauncher: ActivityResultLauncher<IntentSenderRequest>,
    ) {
        val activity = context.findActivity()
        // Create a LocationRequest object

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 30 * 1000L)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(5 * 1000L)
            .setMaxUpdateDelayMillis(100)
            .build()

        // Build LocationSettingsRequest
        val builder = LocationSettingsRequest.Builder().apply {
            addLocationRequest(locationRequest)
            setAlwaysShow(true)  // Always show the prompt if location is not enabled
        }

        // Check the location settings
        val result =
            LocationServices.getSettingsClient(activity).checkLocationSettings(builder.build())
        result.addOnCompleteListener { task ->

            if (task.isSuccessful) {
                requestLocationUpdates()
            } else {

                // The task failed, handle the exception
                try {
                    val exception = task.exception
                    if (exception is ResolvableApiException) {
                        // The exception is of type ResolvableApiException, so you need to resolve it
                        val resolvable = exception
                        // Start resolution for result

                        intentSenderResultLauncher.launch(
                            IntentSenderRequest.Builder(
                                resolvable.resolution.intentSender
                            ).build()
                        )

                    } else {
                        // Handle other types of exceptions (e.g., network issues, internal errors)
                        Toast.makeText(
                            activity,
                            "Location settings could not be resolved.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } catch (e: ClassCastException) {
                    // Catch any potential casting issues (though unlikely)
                    e.printStackTrace()
                }
            }
        }
    }


    private val _citySuggestions = MutableStateFlow<List<AutocompletePrediction>>(emptyList())
    val citySuggestions = _citySuggestions.asStateFlow()

    fun fetchCities(query: String, placesClient: PlacesClient) {

        if (query.isEmpty()) return

        val token = AutocompleteSessionToken.newInstance()
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .setSessionToken(token)
            .setCountries("IN")  // Restrict to India
            .build()

        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->

                // Update the state with the predictions
                _citySuggestions.value = response.autocompletePredictions
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, exception.message.toString())
                if (exception is ApiException) {
                    Log.e("Places", "Place not found: ${exception.statusCode}")
                }
            }

    }


    private fun handleLocationUpdate(
        lat: Double,
        lon: Double,
        type: String,
        context: Context,
        onGeoCallBack: (Boolean) -> Unit,
    ) {
        viewModelScope.launch {
            isAnyLocationCaptured = true

            val geoResult = withContext(Dispatchers.IO) {
                getAddressName(context, lat, lon) {
                    onGeoCallBack(false)
                }
            }

            val addressName  = geoResult?.first

            if (addressName != null) {
                _currentLocation.value = CurrentLocation(
                    lat,
                    lon,
                    addressName,
                    type,
                    geoResult.second
                )
                setLoading(false)
                onGeoCallBack(true)
            } else {
                setLoading(false)
                onGeoCallBack(false)
            }
        }

    }


    private suspend fun getAddressName(
        context: Context,
        lat: Double,
        lon: Double,
        onError: (String) -> Unit,
    ): Pair<String?, String?>? = suspendCoroutine { continuation ->
        LocationManager.getAddressName(context, lat, lon, { address, countryCode ->
            if (address != null) {
                continuation.resume(Pair(address.toString(), countryCode))
            } else {
                continuation.resume(null)
            }
        }) {
            onError(it)
        }
    }


    override fun onCleared() {
        super.onCleared()
        removeLocationUpdates()
    }

}


