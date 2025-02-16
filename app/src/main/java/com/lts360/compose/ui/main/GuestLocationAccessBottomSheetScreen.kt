package com.lts360.compose.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomSheetValue
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.lts360.app.database.models.profile.RecentLocation
import com.lts360.compose.ui.auth.LoadingDialog
import com.lts360.compose.ui.auth.navhost.noTransitionComposable
import com.lts360.compose.ui.main.models.CurrentLocation
import com.lts360.compose.ui.main.navhosts.routes.Districts
import com.lts360.compose.ui.main.navhosts.routes.LocationChooser
import com.lts360.compose.ui.main.viewmodels.HomeViewModel


@Composable
fun OnBoardGuestUserLocationAccessBottomSheetScreen(
    bottomSheetValue: BottomSheetValue? = null,
    onCurrentLocationSelected: (CurrentLocation) -> Unit,
    onRecentLocationSelected: (RecentLocation) -> Unit,
    onDistrictSelected: (District, () -> Unit) -> Unit,
    onPopUpLocationBottomSheet: () -> Unit,
    locationStatesEnabled: Boolean = true,
    isLoading: Boolean = false,
) {

    val bottomSheetNavController = rememberNavController()

    Box {
        Scaffold { contentPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
            ) {
                // NavHost inside the bottom sheet
                NavHost(
                    navController = bottomSheetNavController,
                    startDestination = if (locationStatesEnabled) LocationChooser()
                    else LocationChooser(false),
                    modifier = Modifier.fillMaxSize()
                ) {
                    noTransitionComposable<LocationChooser> {
                        LocationBottomSheet(
                            bottomSheetValue,
                            onCloseClick = {
                                onPopUpLocationBottomSheet()
                            },
                            onRecentLocationSelected = onRecentLocationSelected,
                            onCurrentLocationSelected = onCurrentLocationSelected,
                            onStateClick = {
                                bottomSheetNavController.navigate(Districts)
                            })
                    }


                    noTransitionComposable<Districts> {
                        DistrictsScreen(bottomSheetNavController,
                            isLoading,
                            onDistrictSelected) {
                            bottomSheetNavController.popBackStack()
                        }
                    }
                }

            }

        }

        if (isLoading) {
            LoadingDialog()
        }
    }


}


@Composable
fun GuestUserLocationAccessBottomSheetScreen(
    bottomSheetValue: BottomSheetValue? = null,
    onCurrentLocationSelected: (CurrentLocation) -> Unit,
    onRecentLocationSelected: (RecentLocation) -> Unit,
    onDistrictSelected: (District, () -> Unit) -> Unit,
    onPopUpLocationBottomSheet: () -> Unit,
    homeViewModel: HomeViewModel,
    locationStatesEnabled: Boolean = true,
    isLoading: Boolean = false) {

    val bottomSheetNavController = rememberNavController()

    Box {
        Scaffold { contentPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
            ) {
                // NavHost inside the bottom sheet
                NavHost(
                    navController = bottomSheetNavController,
                    startDestination = if (locationStatesEnabled) LocationChooser()
                    else LocationChooser(false),
                    modifier = Modifier.fillMaxSize()
                ) {
                    noTransitionComposable<LocationChooser> {
                        GuestUserLocationBottomSheet(
                            bottomSheetValue,
                            onCloseClick = {
                                onPopUpLocationBottomSheet()
                            },
                            onRecentLocationSelected = onRecentLocationSelected,
                            onCurrentLocationSelected = onCurrentLocationSelected,
                            onStateClick = {
                                bottomSheetNavController.navigate(Districts)
                            }, homeViewModel = homeViewModel)
                    }


                    noTransitionComposable<Districts> {
                        DistrictsScreen(bottomSheetNavController,
                            isLoading,
                            onDistrictSelected) {
                            bottomSheetNavController.popBackStack()
                        }
                    }
                }

            }

        }

        if (isLoading) {
            LoadingDialog()
        }
    }


}
