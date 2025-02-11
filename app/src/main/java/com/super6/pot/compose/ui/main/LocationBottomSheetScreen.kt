package com.super6.pot.compose.ui.main

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
import com.super6.pot.app.database.models.profile.RecentLocation
import com.super6.pot.compose.ui.auth.LoadingDialog
import com.super6.pot.compose.ui.auth.navhost.noTransitionComposable
import com.super6.pot.compose.ui.main.models.CurrentLocation
import com.super6.pot.compose.ui.main.navhosts.routes.Districts
import com.super6.pot.compose.ui.main.navhosts.routes.LocationChooser
import com.super6.pot.compose.ui.main.viewmodels.HomeViewModel
import com.super6.pot.compose.ui.services.manage.viewmodels.PublishedServicesViewModel
import com.super6.pot.compose.ui.services.manage.viewmodels.ServicesWorkflowViewModel

@Composable
fun EditLocationBottomSheetScreen(
    bottomSheetValue: BottomSheetValue? = null,
    onCurrentLocationSelected: (CurrentLocation) -> Unit,
    onRecentLocationSelected: (RecentLocation) -> Unit,
    onDistrictSelected: (District, () -> Unit) -> Unit,
    onPopUpLocationBottomSheet: () -> Unit,
    publishedServicesViewModel: PublishedServicesViewModel,
    locationStatesEnabled: Boolean = true,
    isLoading: Boolean = false,
) {


    EditLocationBottomSheetContent(
        bottomSheetValue,
        onCurrentLocationSelected,
        onRecentLocationSelected,
        onDistrictSelected,
        onPopUpLocationBottomSheet,
        locationStatesEnabled,
        isLoading,
        publishedServicesViewModel
    )

}


@Composable
fun UserLocationBottomSheetScreen(
    bottomSheetValue: BottomSheetValue? = null,
    onCurrentLocationSelected: (CurrentLocation) -> Unit,
    onRecentLocationSelected: (RecentLocation) -> Unit,
    onDistrictSelected: (District, () -> Unit) -> Unit,
    onPopUpLocationBottomSheet: () -> Unit,
    homeViewModel: HomeViewModel,
    locationStatesEnabled: Boolean = true,
    isLoading: Boolean = false,
) {


    UserLocationBottomSheetContent(
        bottomSheetValue,
        onCurrentLocationSelected,
        onRecentLocationSelected,
        onDistrictSelected,
        onPopUpLocationBottomSheet,
        locationStatesEnabled,
        isLoading,
        homeViewModel
    )

}





@Composable
fun CreateServiceLocationBottomSheetScreen(
    bottomSheetValue: BottomSheetValue? = null,
    onCurrentLocationSelected: (CurrentLocation) -> Unit,
    onRecentLocationSelected: (RecentLocation) -> Unit,
    onDistrictSelected: (District, () -> Unit) -> Unit,
    onPopUpLocationBottomSheet: () -> Unit,
    createServiceViewModel: ServicesWorkflowViewModel,
    locationStatesEnabled: Boolean = true,
    isLoading: Boolean = false,
) {


    CreateServiceLocationBottomSheetContent(
        bottomSheetValue,
        onCurrentLocationSelected,
        onRecentLocationSelected,
        onDistrictSelected,
        onPopUpLocationBottomSheet,
        locationStatesEnabled,
        isLoading,
        createServiceViewModel
    )

}






@Composable
fun OnBoardUserLocationBottomSheetScreen(
    bottomSheetValue: BottomSheetValue? = null,
    onCurrentLocationSelected: (CurrentLocation) -> Unit,
    onRecentLocationSelected: (RecentLocation) -> Unit,
    onDistrictSelected: (District, () -> Unit) -> Unit,
    onPopUpLocationBottomSheet: () -> Unit,
    locationStatesEnabled: Boolean = true,
    isLoading: Boolean = false,
) {


    LocationBottomSheetContent(
        bottomSheetValue,
        onCurrentLocationSelected,
        onRecentLocationSelected,
        onDistrictSelected,
        onPopUpLocationBottomSheet,
        locationStatesEnabled,
        isLoading)

}



@Composable
fun UserLocationBottomSheetContent(
    bottomSheetValue: BottomSheetValue? = null,
    onCurrentLocationSelected: (CurrentLocation) -> Unit,
    onRecentLocationSelected: (RecentLocation) -> Unit,
    onDistrictSelected: (District, () -> Unit) -> Unit,
    onPopUpLocationBottomSheet: () -> Unit,
    locationStatesEnabled: Boolean = true,
    isLoading: Boolean = false,
    homeViewModel: HomeViewModel,
) {
    val bottomSheetNavController = rememberNavController()

    Box {
        Scaffold { contentPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
            ) {

                NavHost(
                    navController = bottomSheetNavController,
                    startDestination = if (locationStatesEnabled)
                        LocationChooser()
                    else LocationChooser(
                        false
                    ),
                    modifier = Modifier.fillMaxSize()
                ) {
                    noTransitionComposable<LocationChooser> {

                        UserLocationBottomSheet(
                            bottomSheetValue,
                            onCloseClick = {
                                onPopUpLocationBottomSheet()
                            },
                            onCurrentLocationSelected = onCurrentLocationSelected,
                            onRecentLocationSelected = onRecentLocationSelected,
                            onStateClick = {
                                bottomSheetNavController.navigate(Districts)
                            },
                            homeViewModel)
                    }

                    noTransitionComposable<Districts> {
                        DistrictsScreen(bottomSheetNavController,
                            isLoading,
                            onDistrictSelected)
                        {
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
fun CreateServiceLocationBottomSheetContent(
    bottomSheetValue: BottomSheetValue? = null,
    onCurrentLocationSelected: (CurrentLocation) -> Unit,
    onRecentLocationSelected: (RecentLocation) -> Unit,
    onDistrictSelected: (District, () -> Unit) -> Unit,
    onPopUpLocationBottomSheet: () -> Unit,
    locationStatesEnabled: Boolean = true,
    isLoading: Boolean = false,
    createServiceViewModel: ServicesWorkflowViewModel,
) {
    val bottomSheetNavController = rememberNavController()

    Box {
        Scaffold { contentPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
            ) {

                NavHost(
                    navController = bottomSheetNavController,
                    startDestination = if (locationStatesEnabled)
                        LocationChooser()
                    else LocationChooser(
                        false
                    ),
                    modifier = Modifier.fillMaxSize()
                ) {
                    noTransitionComposable<LocationChooser> {

                        CreateServiceLocationBottomSheet(
                            bottomSheetValue,
                            onCloseClick = {
                                onPopUpLocationBottomSheet()
                            },
                            onCurrentLocationSelected = onCurrentLocationSelected,
                            onRecentLocationSelected = onRecentLocationSelected,
                            onStateClick = {
                                bottomSheetNavController.navigate(Districts)
                            },
                            createServiceViewModel)
                    }

                    noTransitionComposable<Districts> {
                        DistrictsScreen(bottomSheetNavController,
                            isLoading,
                            onDistrictSelected,{
                            bottomSheetNavController.popBackStack()
                        })
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
fun LocationBottomSheetContent(
    bottomSheetValue: BottomSheetValue? = null,
    onCurrentLocationSelected: (CurrentLocation) -> Unit,
    onRecentLocationSelected: (RecentLocation) -> Unit,
    onDistrictSelected: (District, () -> Unit) -> Unit,
    onPopUpLocationBottomSheet: () -> Unit,
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

                NavHost(
                    navController = bottomSheetNavController,
                    startDestination = if (locationStatesEnabled)
                        LocationChooser()
                    else LocationChooser(
                        false
                    ),
                    modifier = Modifier.fillMaxSize()
                ) {
                    noTransitionComposable<LocationChooser> {

                        LocationBottomSheet(
                            bottomSheetValue,
                            onCloseClick = {
                                onPopUpLocationBottomSheet()
                            },
                            onCurrentLocationSelected = onCurrentLocationSelected,
                            onRecentLocationSelected = onRecentLocationSelected,
                            onStateClick = {
                                bottomSheetNavController.navigate(Districts)
                            })
                    }

                    noTransitionComposable<Districts> {
                        DistrictsScreen(bottomSheetNavController,
                            isLoading,
                            onDistrictSelected,{
                            bottomSheetNavController.popBackStack()
                        })
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
fun EditLocationBottomSheetContent(
    bottomSheetValue: BottomSheetValue? = null,
    onCurrentLocationSelected: (CurrentLocation) -> Unit,
    onRecentLocationSelected: (RecentLocation) -> Unit,
    onDistrictSelected: (District, () -> Unit) -> Unit,
    onPopUpLocationBottomSheet: () -> Unit,
    locationStatesEnabled: Boolean = true,
    isLoading: Boolean = false,
    publishedServicesViewModel: PublishedServicesViewModel,
) {
    val bottomSheetNavController = rememberNavController()

    Box {
        Scaffold { contentPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
            ) {

                NavHost(
                    navController = bottomSheetNavController,
                    startDestination = if (locationStatesEnabled)
                        LocationChooser()
                    else LocationChooser(
                        false
                    ),
                    modifier = Modifier.fillMaxSize()
                ) {
                    noTransitionComposable<LocationChooser> {

                        EditLocationBottomSheet(
                            bottomSheetValue,
                            onCloseClick = {
                                onPopUpLocationBottomSheet()
                            },
                            onCurrentLocationSelected = onCurrentLocationSelected,
                            onRecentLocationSelected = onRecentLocationSelected,
                            onStateClick = {
                                bottomSheetNavController.navigate(Districts)
                            },
                            publishedServicesViewModel)
                    }

                    noTransitionComposable<Districts> {
                        DistrictsScreen(bottomSheetNavController,
                            isLoading,
                            onDistrictSelected,{
                            bottomSheetNavController.popBackStack()
                        })
                    }
                }

            }
        }

        if (isLoading) {
            LoadingDialog()
        }
    }


}

