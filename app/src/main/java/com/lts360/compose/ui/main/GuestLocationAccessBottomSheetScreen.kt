package com.lts360.compose.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.lts360.app.database.models.profile.RecentLocation
import com.lts360.compose.ui.auth.LoadingDialog
import com.lts360.compose.ui.auth.navhost.noTransitionComposable
import com.lts360.compose.ui.main.models.CurrentLocation
import com.lts360.compose.ui.main.navhosts.routes.LocationSetUpRoutes
import com.lts360.compose.ui.main.viewmodels.HomeViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnBoardGuestUserLocationAccessBottomSheetScreen(
    bottomSheetValue: SheetValue? = null,
    onCurrentLocationSelected: (CurrentLocation) -> Unit,
    onDistrictSelected: (District) -> Unit,
    onPopUpLocationBottomSheet: () -> Unit,
    onRecentLocationSelected: (RecentLocation) -> Unit={},
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
                    startDestination = if (locationStatesEnabled) LocationSetUpRoutes.LocationChooser()
                    else LocationSetUpRoutes.LocationChooser(false),
                    modifier = Modifier.fillMaxSize()
                ) {
                    noTransitionComposable<LocationSetUpRoutes.LocationChooser> {
                        OnBoardLocationBottomSheet(
                            bottomSheetValue,
                            onCloseClick = {
                                onPopUpLocationBottomSheet()
                            },
                            onRecentLocationSelected = onRecentLocationSelected,
                            onCurrentLocationSelected = onCurrentLocationSelected,
                            onStateClick = {
                                bottomSheetNavController.navigate(LocationSetUpRoutes.Districts)
                            })
                    }


                    noTransitionComposable<LocationSetUpRoutes.Districts> {
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuestUserLocationAccessBottomSheetScreen(
    bottomSheetValue: SheetValue? = null,
    onCurrentLocationSelected: (CurrentLocation) -> Unit,
    onRecentLocationSelected: (RecentLocation) -> Unit,
    onDistrictSelected: (District) -> Unit,
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
                    startDestination = if (locationStatesEnabled) LocationSetUpRoutes.LocationChooser()
                    else LocationSetUpRoutes.LocationChooser(false),
                    modifier = Modifier.fillMaxSize()
                ) {
                    noTransitionComposable<LocationSetUpRoutes.LocationChooser> {
                        GuestUserLocationBottomSheet(
                            bottomSheetValue,
                            onCloseClick = {
                                onPopUpLocationBottomSheet()
                            },
                            onRecentLocationSelected = onRecentLocationSelected,
                            onCurrentLocationSelected = onCurrentLocationSelected,
                            onStateClick = {
                                bottomSheetNavController.navigate(LocationSetUpRoutes.Districts)
                            }, homeViewModel = homeViewModel)
                    }


                    noTransitionComposable<LocationSetUpRoutes.Districts> {
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
