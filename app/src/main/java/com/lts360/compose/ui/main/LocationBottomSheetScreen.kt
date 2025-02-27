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
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.lts360.app.database.models.profile.RecentLocation
import com.lts360.compose.ui.auth.LoadingDialog
import com.lts360.compose.ui.auth.navhost.noTransitionComposable
import com.lts360.compose.ui.main.models.CurrentLocation
import com.lts360.compose.ui.main.navhosts.routes.Districts
import com.lts360.compose.ui.main.navhosts.routes.LocationChooser
import com.lts360.compose.ui.main.viewmodels.HomeViewModel
import com.lts360.compose.ui.services.manage.viewmodels.PublishedServicesViewModel
import com.lts360.compose.ui.services.manage.viewmodels.ServicesWorkflowViewModel
import com.lts360.compose.ui.usedproducts.manage.viewmodels.PublishedUsedProductsListingViewModel
import com.lts360.compose.ui.usedproducts.manage.viewmodels.UsedProductsListingWorkflowViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditLocationBottomSheetScreen(
    bottomSheetValue: SheetValue? = null,
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserLocationBottomSheetScreen(
    navHostController: NavHostController,
    bottomSheetValue: SheetValue? = null,
    onCurrentLocationSelected: (CurrentLocation) -> Unit,
    onRecentLocationSelected: (RecentLocation) -> Unit,
    onDistrictSelected: (District, () -> Unit) -> Unit,
    onPopUpLocationBottomSheet: () -> Unit,
    homeViewModel: HomeViewModel,
    locationStatesEnabled: Boolean = true,
    isLoading: Boolean = false) {


    UserLocationBottomSheetContent(
        navHostController,
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





@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateServiceLocationBottomSheetScreen(
    bottomSheetValue: SheetValue? = null,
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




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateUsedProductListingLocationBottomSheetScreen(
    bottomSheetValue: SheetValue? = null,
    onCurrentLocationSelected: (CurrentLocation) -> Unit,
    onRecentLocationSelected: (RecentLocation) -> Unit,
    onDistrictSelected: (District, () -> Unit) -> Unit,
    onPopUpLocationBottomSheet: () -> Unit,
    usedProductsListingWorkflowViewModel: UsedProductsListingWorkflowViewModel,
    locationStatesEnabled: Boolean = true,
    isLoading: Boolean = false,
) {


    CreateUsedProductListingBottomSheetContent(
        bottomSheetValue,
        onCurrentLocationSelected,
        onRecentLocationSelected,
        onDistrictSelected,
        onPopUpLocationBottomSheet,
        locationStatesEnabled,
        isLoading,
        usedProductsListingWorkflowViewModel
    )

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagePublishedUsedProductListingLocationBottomSheetScreen(
    bottomSheetValue: SheetValue? = null,
    onCurrentLocationSelected: (CurrentLocation) -> Unit,
    onRecentLocationSelected: (RecentLocation) -> Unit,
    onDistrictSelected: (District, () -> Unit) -> Unit,
    onPopUpLocationBottomSheet: () -> Unit,
    usedProductsListingWorkflowViewModel: PublishedUsedProductsListingViewModel,
    locationStatesEnabled: Boolean = true,
    isLoading: Boolean = false,
) {


    PublishedUsedProductListingBottomSheetContent(
        bottomSheetValue,
        onCurrentLocationSelected,
        onRecentLocationSelected,
        onDistrictSelected,
        onPopUpLocationBottomSheet,
        locationStatesEnabled,
        isLoading,
        usedProductsListingWorkflowViewModel
    )

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnBoardUserLocationBottomSheetScreen(
    bottomSheetValue: SheetValue? = null,
    onCurrentLocationSelected: (CurrentLocation) -> Unit,
    onRecentLocationSelected: (RecentLocation) -> Unit,
    onDistrictSelected: (District, () -> Unit) -> Unit,
    onPopUpLocationBottomSheet: () -> Unit,
    locationStatesEnabled: Boolean = true,
    isLoading: Boolean = false
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



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserLocationBottomSheetContent(
    bottomSheetNavController:NavHostController,
    bottomSheetValue: SheetValue? = null,
    onCurrentLocationSelected: (CurrentLocation) -> Unit,
    onRecentLocationSelected: (RecentLocation) -> Unit,
    onDistrictSelected: (District, () -> Unit) -> Unit,
    onPopUpLocationBottomSheet: () -> Unit,
    locationStatesEnabled: Boolean = true,
    isLoading: Boolean = false,
    homeViewModel: HomeViewModel
) {

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



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishedUsedProductListingBottomSheetContent(
    bottomSheetValue: SheetValue? = null,
    onCurrentLocationSelected: (CurrentLocation) -> Unit,
    onRecentLocationSelected: (RecentLocation) -> Unit,
    onDistrictSelected: (District, () -> Unit) -> Unit,
    onPopUpLocationBottomSheet: () -> Unit,
    locationStatesEnabled: Boolean = true,
    isLoading: Boolean = false,
    publishedUsedProductsListingViewModel: PublishedUsedProductsListingViewModel,
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

                        PublishedUsedProductListingLocationBottomSheet(
                            bottomSheetValue,
                            onCloseClick = {
                                onPopUpLocationBottomSheet()
                            },
                            onCurrentLocationSelected = onCurrentLocationSelected,
                            onRecentLocationSelected = onRecentLocationSelected,
                            onStateClick = {
                                bottomSheetNavController.navigate(Districts)
                            },
                            publishedUsedProductsListingViewModel)
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



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateUsedProductListingBottomSheetContent(
    bottomSheetValue: SheetValue? = null,
    onCurrentLocationSelected: (CurrentLocation) -> Unit,
    onRecentLocationSelected: (RecentLocation) -> Unit,
    onDistrictSelected: (District, () -> Unit) -> Unit,
    onPopUpLocationBottomSheet: () -> Unit,
    locationStatesEnabled: Boolean = true,
    isLoading: Boolean = false,
    usedProductsListingWorkflowViewModel: UsedProductsListingWorkflowViewModel,
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

                        CreateUsedProductListingLocationBottomSheet(
                            bottomSheetValue,
                            onCloseClick = {
                                onPopUpLocationBottomSheet()
                            },
                            onCurrentLocationSelected = onCurrentLocationSelected,
                            onRecentLocationSelected = onRecentLocationSelected,
                            onStateClick = {
                                bottomSheetNavController.navigate(Districts)
                            },
                            usedProductsListingWorkflowViewModel)
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




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateServiceLocationBottomSheetContent(
    bottomSheetValue: SheetValue? = null,
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




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationBottomSheetContent(
    bottomSheetValue: SheetValue? = null,
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditLocationBottomSheetContent(
    bottomSheetValue: SheetValue? = null,
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

