package com.super6.pot.compose.ui.onboarding


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
import com.super6.pot.R
import com.super6.pot.compose.ui.main.OnBoardGuestUserLocationAccessBottomSheetScreen
import com.super6.pot.compose.ui.main.OnBoardUserLocationBottomSheetScreen
import com.super6.pot.compose.ui.main.models.CurrentLocation
import com.super6.pot.compose.ui.managers.UserSharedPreferencesManager
import com.super6.pot.compose.ui.onboarding.viewmodels.LocationAccessViewModel
import kotlinx.coroutines.launch


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


