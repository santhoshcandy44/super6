package com.lts360.compose.ui.onboarding


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
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lts360.compose.ui.main.OnBoardGuestUserLocationAccessBottomSheetScreen
import com.lts360.compose.ui.main.OnBoardUserLocationBottomSheetScreen
import com.lts360.compose.ui.main.models.CurrentLocation
import com.lts360.compose.ui.onboarding.viewmodels.LocationAccessViewModel
import com.lts360.compose.ui.theme.icons
import com.lts360.libs.ui.ShortToast
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationAccessScreen(
    type: String,
    viewModel: LocationAccessViewModel = koinViewModel(),
    onLocationUpdated: () -> Unit = {}
) {

    val coroutineScope = rememberCoroutineScope()

    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            SheetValue.Hidden,
            skipHiddenState = false
        )
    )

    BackHandler(bottomSheetScaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
        coroutineScope.launch {
            bottomSheetScaffoldState.bottomSheetState.hide()
        }
    }

    val isLoading by viewModel.isLoading.collectAsState()
    val context = LocalContext.current

    BottomSheetScaffold(
        scaffoldState = bottomSheetScaffoldState,
        sheetContent = {

            if (bottomSheetScaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
                if (type == "guest") {
                    OnBoardGuestUserLocationAccessBottomSheetScreen(
                        bottomSheetScaffoldState.bottomSheetState.currentValue == SheetValue.Expanded,
                        { currentLocation ->
                            viewModel.setGuestCurrentLocationAndCreateAccount(
                                context,
                                currentLocation, {
                                    onLocationUpdated()
                                    Toast.makeText(
                                        context,
                                        "Location updated successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }) {
                                Toast.makeText(context, it, Toast.LENGTH_SHORT)
                                    .show()
                            }
                        },
                        { district ->
                            viewModel.setGuestCurrentLocationAndCreateAccount(
                                context,
                                CurrentLocation(
                                    district.coordinates.latitude,
                                    district.coordinates.longitude,
                                    district.district,
                                    "approximate"
                                ), {
                                    onLocationUpdated()
                                    Toast.makeText(
                                        context,
                                        "Location updated successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                }) {
                                Toast.makeText(context, it, Toast.LENGTH_SHORT)
                                    .show()
                            }
                        },
                        {
                            coroutineScope.launch {
                                bottomSheetScaffoldState.bottomSheetState.hide()
                            }
                        })
                } else {

                    OnBoardUserLocationBottomSheetScreen(
                        bottomSheetScaffoldState.bottomSheetState.currentValue == SheetValue.Expanded,
                        { currentLocation ->
                            viewModel.setCurrentLocation(currentLocation, { message ->
                                onLocationUpdated()
                                currentLocation.countryCode?.let {
                                    viewModel.selectCountry(it)
                                }

                                ShortToast(context, message)

                            }) {
                                ShortToast(context, it)
                            }
                        },
                        { recentLocation ->
                            viewModel.setRecentLocation(recentLocation, {
                                onLocationUpdated()
                                ShortToast(context, it)
                            }) {
                                ShortToast(context, it)
                            }
                        },
                        { district ->
                            viewModel.setCurrentLocation(
                                CurrentLocation(
                                    district.coordinates.latitude,
                                    district.coordinates.longitude,
                                    district.district,
                                    "approximate"
                                ), {
                                    onLocationUpdated()
                                    ShortToast(context, it)
                                }) {
                                ShortToast(context, it)
                            }
                        },
                        {
                            coroutineScope.launch {
                                bottomSheetScaffoldState.bottomSheetState.hide()
                            }
                        },
                        isLoading = isLoading
                    )
                }
            }
        },
        sheetShape = RectangleShape,
        sheetDragHandle = null,
        sheetPeekHeight = 0.dp,
        sheetSwipeEnabled = false,
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Image(
                painter = painterResource(MaterialTheme.icons.locationAccessOnBoarding), // Your location image
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


