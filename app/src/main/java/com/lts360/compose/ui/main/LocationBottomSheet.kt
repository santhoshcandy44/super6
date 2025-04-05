package com.lts360.compose.ui.main

import android.Manifest
import android.app.Activity.RESULT_OK
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.dropUnlessResumed
import com.lts360.R
import com.lts360.app.database.models.profile.RecentLocation
import com.lts360.components.findActivity
import com.lts360.compose.ui.main.models.CurrentLocation
import com.lts360.compose.ui.main.viewmodels.HomeViewModel
import com.lts360.compose.ui.services.manage.viewmodels.PublishedServicesViewModel
import com.lts360.compose.ui.services.manage.viewmodels.ServicesWorkflowViewModel
import com.lts360.compose.ui.theme.icons
import com.lts360.compose.ui.usedproducts.manage.viewmodels.PublishedUsedProductsListingViewModel
import com.lts360.compose.ui.usedproducts.manage.viewmodels.UsedProductsListingWorkflowViewModel
import com.lts360.compose.ui.viewmodels.LocationViewModel
import kotlinx.serialization.Serializable

@Serializable
data class StateLocation(
    val latitude: Double,
    val longitude: Double,
)

@Serializable
data class District(
    val district: String,
    val coordinates: Coordinates,
)

@Serializable
data class Coordinates(
    val latitude: Double,
    val longitude: Double,
)

@Serializable
data class StateModel(
    val state: String,
    val district_count: Int,
    val state_location: StateLocation,
    val districts: List<District>,
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnBoardLocationBottomSheet(
    bottomSheetValue: SheetValue? = null,
    onCloseClick: () -> Unit,
    onCurrentLocationSelected: (CurrentLocation) -> Unit,
    onRecentLocationSelected: (RecentLocation) -> Unit,
    onStateClick: (String) -> Unit = {},
) {

    LocationBottomSheetContent(
        bottomSheetValue,
        onCloseClick,
        null,
        onCurrentLocationSelected,
        onRecentLocationSelected,
        onStateClick)
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserLocationBottomSheet(
    bottomSheetValue: SheetValue? = null,
    onCloseClick: () -> Unit,
    onCurrentLocationSelected: (CurrentLocation) -> Unit,
    onRecentLocationSelected: (RecentLocation) -> Unit,
    onStateClick: (String) -> Unit = {},
    homeViewModel: HomeViewModel,
) {

    val selectedLocationGeo by homeViewModel.selectedLocationGeo.collectAsState()

    LocationBottomSheetContent(
        bottomSheetValue,
        onCloseClick,
        selectedLocationGeo,
        onCurrentLocationSelected,
        onRecentLocationSelected,
        onStateClick)
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuestUserLocationBottomSheet(
    bottomSheetValue: SheetValue? = null,
    onCloseClick: () -> Unit,
    onCurrentLocationSelected: (CurrentLocation) -> Unit,
    onRecentLocationSelected: (RecentLocation) -> Unit,
    onStateClick: (String) -> Unit = {},
    homeViewModel: HomeViewModel,
) {

    val selectedLocationGeo by homeViewModel.selectedLocationGeo.collectAsState()

    LocationBottomSheetContent(
        bottomSheetValue,
        onCloseClick,
        selectedLocationGeo,
        onCurrentLocationSelected,
        onRecentLocationSelected,
        onStateClick)
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateServiceLocationBottomSheet(
    bottomSheetValue: SheetValue? = null,
    onCloseClick: () -> Unit,
    onCurrentLocationSelected: (CurrentLocation) -> Unit,
    onRecentLocationSelected: (RecentLocation) -> Unit,
    onStateClick: (String) -> Unit = {},
    createServiceViewModel: ServicesWorkflowViewModel,
) {

    val selectedLocationGeo by createServiceViewModel.selectedLocation.collectAsState()

    LocationBottomSheetContent(
        bottomSheetValue,
        onCloseClick,
        selectedLocationGeo?.geo,
        onCurrentLocationSelected,
        onRecentLocationSelected,
        onStateClick)
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateUsedProductListingLocationBottomSheet(
    bottomSheetValue: SheetValue? = null,
    onCloseClick: () -> Unit,
    onCurrentLocationSelected: (CurrentLocation) -> Unit,
    onRecentLocationSelected: (RecentLocation) -> Unit,
    onStateClick: (String) -> Unit = {},
    usedProductsListingWorkflowViewModel: UsedProductsListingWorkflowViewModel,
) {

    val selectedLocationGeo by usedProductsListingWorkflowViewModel.selectedLocation.collectAsState()

    LocationBottomSheetContent(
        bottomSheetValue,
        onCloseClick,
        selectedLocationGeo?.geo,
        onCurrentLocationSelected,
        onRecentLocationSelected,
        onStateClick)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPublishedUsedProductListingLocationBottomSheet(
    bottomSheetValue: SheetValue? = null,
    onCloseClick: () -> Unit,
    onCurrentLocationSelected: (CurrentLocation) -> Unit,
    onRecentLocationSelected: (RecentLocation) -> Unit,
    onStateClick: (String) -> Unit = {},
    publishedUsedProductsListingViewModel: PublishedUsedProductsListingViewModel
) {

    val selectedLocationGeo by publishedUsedProductsListingViewModel.selectedLocation.collectAsState()

    LocationBottomSheetContent(
        bottomSheetValue,
        onCloseClick,
        selectedLocationGeo?.geo,
        onCurrentLocationSelected,
        onRecentLocationSelected,
        onStateClick)
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPublishedServiceLocationBottomSheet(
    bottomSheetValue: SheetValue? = null,
    onCloseClick: () -> Unit,
    onCurrentLocationSelected: (CurrentLocation) -> Unit,
    onRecentLocationSelected: (RecentLocation) -> Unit,
    onStateClick: (String) -> Unit = {},
    publishedServicesViewModel: PublishedServicesViewModel,
) {

    val selectedLocationGeo by publishedServicesViewModel.editableLocation.collectAsState()

    LocationBottomSheetContent(
        bottomSheetValue,
        onCloseClick,
        selectedLocationGeo?.geo,
        onCurrentLocationSelected,
        onRecentLocationSelected,
        onStateClick)
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocationBottomSheetContent(
    bottomSheetValue: SheetValue? = null,
    onCloseClick: () -> Unit,
    selectedLocationGeo:String?,
    onCurrentLocationSelected: (CurrentLocation) -> Unit,
    onRecentLocationSelected: (RecentLocation) -> Unit,
    onStateClick: (String) -> Unit = {},
    locationViewModel: LocationViewModel = hiltViewModel(),
) {

    if (bottomSheetValue != null) {

        LaunchedEffect(bottomSheetValue) {
            if (bottomSheetValue == SheetValue.Hidden) {
                locationViewModel.removeLocationUpdates()
            }
        }
    }

    val isLocationStatesEnabled = locationViewModel.isLocationStatesEnabled

    val context = LocalContext.current

    val currentLocation by locationViewModel.currentLocation.collectAsState()
    /*
        val selectedLocationGeo by locationViewModel.selectedLocationGeo.collectAsState()
    */
    val recentLocations by locationViewModel.recentLocations.collectAsState()

    val isLoading by locationViewModel.isLoading.collectAsState()


    val locations by locationViewModel.locations.collectAsState()


    val activity = context.findActivity() as ComponentActivity

    // Registering the result launcher for intent sender resolution
    val resolutionLauncher = remember {
        activity.activityResultRegistry.register(
            "resolutionLauncher",
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                locationViewModel.requestLocationUpdates()
            } else {
                locationViewModel.removeLocationUpdates()
            }
        }
    }


    // Remember the permissions launcher
    val requestPermissionsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->

        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (fineLocationGranted || coarseLocationGranted) {
            // Permissions granted, request location updates
            locationViewModel.enableLoc(context, resolutionLauncher)
        } else {
            locationViewModel.onLocationPermissionDenied()
            Toast.makeText(context, "Location permissions denied", Toast.LENGTH_SHORT).show()
        }
    }

    val launchLocationPermission: () -> Unit = {
        requestPermissionsLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    val isLocationManagerAvailable by locationViewModel.isLocationManagerAvailable.collectAsState()

    // Remember Places Client
/*    val placesClient = remember { Places.createClient(context) }
    val citySuggestions by locationViewModel.citySuggestions.collectAsState()


    val value by remember { mutableStateOf("") }

    val textFieldState = remember {
        TextFieldState(
            initialText = value,
            // Initialize the cursor to be at the end of the field.
            initialSelection = TextRange(value.length)
        )
    }

    val focusManager = LocalFocusManager.current


    BackHandler(textFieldState.text.isNotEmpty()) {
        focusManager.clearFocus()
        textFieldState.clearText()
    }*/

    LaunchedEffect(isLocationManagerAvailable) {
        if (!isLocationManagerAvailable) {
            locationViewModel.locationManagerInitialize(context)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp)
        ) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Title
                Text(
                    text = "Choose Location",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.weight(1f)
                )

                // Close Button
                IconButton(onClick = onCloseClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_close_black),
                        contentDescription = "Close"
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))


            /*
                        SearchTextField("",
                            textFieldState,
                            onValueChange = {
                                value = it
                                locationViewModel.fetchCities(it, placesClient)
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))*/



            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    item {

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {

                            // Location Image
                            Image(
                                painter = painterResource(R.drawable.choose_location),
                                contentDescription = "Location",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Description Text
                            Text(
                                text = if (isLocationStatesEnabled) "Get better nearby results for your convenience."
                                else "Let peoples find your service near by."
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Choose Location Button
                            OutlinedButton(
                                onClick = {
                                    locationViewModel.chooseCurrentLocation(
                                        context,
                                        resolutionLauncher
                                    ) {
                                        launchLocationPermission()
                                    }
                                },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = "Choose Location", textAlign = TextAlign.Center)
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            currentLocation?.let {

                                // Current Location Section
                                Text(
                                    text = "Current Location",
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onCurrentLocationSelected(it)
                                        }
                                        .padding(vertical = 8.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_gps),
                                        contentDescription = "GPS",
                                        modifier = Modifier.size(32.dp),
                                        tint = Color.Unspecified
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = it.geo,
                                        modifier = Modifier.align(Alignment.CenterVertically)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                            }


                            selectedLocationGeo?.let {

                                // Current Location Section
                                Text(
                                    text = "Selected Location",
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_gps),
                                        contentDescription = "GPS",
                                        modifier = Modifier.size(32.dp),
                                        tint = Color.Unspecified
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = it, // bind current location text here
                                        modifier = Modifier.align(Alignment.CenterVertically),
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                            }


                        }

                    }
                    if (isLocationStatesEnabled) {
                        if (recentLocations.isNotEmpty()) {

                            item {
                                Text(
                                    text = "Recently Used",
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            items(recentLocations) { recentLocation ->
                                RecentlyUsedLocationItem(recentLocation) {
                                    onRecentLocationSelected(it)
                                }
                            }
                        }

                    }

                    locations?.let { locations ->
                        item {
                            Text(
                                text = "Or Choose from",
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        items(locations.toList(), key = { it.first }) {

                            CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                                Card(shape = RectangleShape,
                                    onClick = dropUnlessResumed {
                                        locationViewModel.updateDistricts(it.first)
                                        onStateClick(it.first)
                                    }) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,

                                        ) {
                                        Text(
                                            it.second.state, modifier = Modifier
                                                .padding(16.dp)
                                                .padding(start = 8.dp)
                                                .weight(1f)
                                        )
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                            modifier = Modifier.padding(end = 8.dp),
                                            contentDescription = "Arrow Forward"
                                        )
                                    }
                                }
                            }

                        }

                    }
                }

                /*
                                if (textFieldState.text.isNotEmpty()) {
                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxSize()
                                    ) {

                                        // Display each city suggestion
                                        items(citySuggestions) { prediction ->
                                            // Extract the primary text (place name, e.g., city name)
                                            val cityName = prediction.getPrimaryText(null).toString()

                                            // Row for displaying city names
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        // Handle click (e.g., show a Toast, or navigate)
                                                        Toast
                                                            .makeText(
                                                                context,
                                                                "Selected: $cityName",
                                                                Toast.LENGTH_SHORT
                                                            )
                                                            .show()
                                                    }
                                                    .padding(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = cityName,
                                                )
                                            }
                                        }

                                    }
                                }
                */
            }
        }


        if (isLoading) {

            Box(modifier = Modifier
                .fillMaxSize()
                .clickable(interactionSource = remember {
                    MutableInteractionSource()
                }, indication = null) {} // Consume clicks
                .background(Color.Black.copy(alpha = 0.5f)) // Semi-transparent background
                .wrapContentSize(Alignment.Center)) {

                CircularProgressIndicator(modifier = Modifier.padding(8.dp))
            }

        }
    }
}


@Composable
private fun RecentlyUsedLocationItem(
    recentLocation: RecentLocation,
    onLocationSelected: (RecentLocation) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onLocationSelected(recentLocation) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(MaterialTheme.icons.location), // use your location icon
            contentDescription = "Location Icon",
            modifier = Modifier.size(24.dp),
            tint = Color.Unspecified

        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = recentLocation.geo, // bind the location geo text here
            modifier = Modifier.align(Alignment.CenterVertically),
        )
    }
}