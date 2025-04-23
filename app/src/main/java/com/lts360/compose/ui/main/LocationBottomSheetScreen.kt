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
import com.lts360.compose.ui.localjobs.manage.viewmodels.LocalJobWorkFlowViewModel
import com.lts360.compose.ui.localjobs.manage.viewmodels.PublishedLocalJobViewModel
import com.lts360.compose.ui.main.models.CurrentLocation
import com.lts360.compose.ui.main.navhosts.routes.LocationSetUpRoutes
import com.lts360.compose.ui.main.viewmodels.HomeViewModel
import com.lts360.compose.ui.services.manage.viewmodels.PublishedServicesViewModel
import com.lts360.compose.ui.services.manage.viewmodels.ServicesWorkflowViewModel
import com.lts360.compose.ui.usedproducts.manage.viewmodels.UsedProductsListingWorkflowViewModel
import com.lts360.compose.ui.usedproducts.manage.viewmodels.PublishedUsedProductsListingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditLocationBottomSheetScreen(
    bottomSheetValue: SheetValue? = null,
    onCurrentLocationSelected: (CurrentLocation) -> Unit,
    onRecentLocationSelected: (RecentLocation) -> Unit,
    onDistrictSelected: (District) -> Unit,
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
    onDistrictSelected: (District) -> Unit,
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
    onDistrictSelected: (District) -> Unit,
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
fun CreateLocalJobLocationBottomSheetScreen(
    bottomSheetValue: SheetValue? = null,
    onCurrentLocationSelected: (CurrentLocation) -> Unit,
    onRecentLocationSelected: (RecentLocation) -> Unit,
    onDistrictSelected: (District) -> Unit,
    onPopUpLocationBottomSheet: () -> Unit,
    viewModel: LocalJobWorkFlowViewModel,
    locationStatesEnabled: Boolean = true,
    isLoading: Boolean = false,
) {
    CreateLocalJobBottomSheetContent(
        bottomSheetValue,
        onCurrentLocationSelected,
        onRecentLocationSelected,
        onDistrictSelected,
        onPopUpLocationBottomSheet,
        locationStatesEnabled,
        isLoading,
        viewModel
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateUsedProductListingLocationBottomSheetScreen(
    bottomSheetValue: SheetValue? = null,
    onCurrentLocationSelected: (CurrentLocation) -> Unit,
    onRecentLocationSelected: (RecentLocation) -> Unit,
    onDistrictSelected: (District) -> Unit,
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
fun ManagePublishedLocalJobLocationBottomSheetScreen(
    bottomSheetValue: SheetValue? = null,
    onCurrentLocationSelected: (CurrentLocation) -> Unit,
    onRecentLocationSelected: (RecentLocation) -> Unit,
    onDistrictSelected: (District) -> Unit,
    onPopUpLocationBottomSheet: () -> Unit,
    viewModel: PublishedLocalJobViewModel,
    locationStatesEnabled: Boolean = true,
    isLoading: Boolean = false,
) {
    PublishedLocalJobBottomSheetContent(
        bottomSheetValue,
        onCurrentLocationSelected,
        onRecentLocationSelected,
        onDistrictSelected,
        onPopUpLocationBottomSheet,
        locationStatesEnabled,
        isLoading,
        viewModel
    )
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagePublishedUsedProductListingLocationBottomSheetScreen(
    bottomSheetValue: SheetValue? = null,
    onCurrentLocationSelected: (CurrentLocation) -> Unit,
    onRecentLocationSelected: (RecentLocation) -> Unit,
    onDistrictSelected: (District) -> Unit,
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
    onDistrictSelected: (District) -> Unit,
    onPopUpLocationBottomSheet: () -> Unit,
    locationStatesEnabled: Boolean = true,
    isLoading: Boolean = false
) {


    OnBoardLocationBottomSheetContent(
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
    onDistrictSelected: (District) -> Unit,
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
                        LocationSetUpRoutes.LocationChooser()
                    else LocationSetUpRoutes.LocationChooser(
                        false
                    ),
                    modifier = Modifier.fillMaxSize()
                ) {
                    noTransitionComposable<LocationSetUpRoutes.LocationChooser> {

                        UserLocationBottomSheet(
                            bottomSheetValue,
                            onCloseClick = {
                                onPopUpLocationBottomSheet()
                            },
                            onCurrentLocationSelected = onCurrentLocationSelected,
                            onRecentLocationSelected = onRecentLocationSelected,
                            onStateClick = {
                                bottomSheetNavController.navigate(LocationSetUpRoutes.Districts)
                            },
                            homeViewModel)
                    }

                    noTransitionComposable<LocationSetUpRoutes.Districts> {
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
fun PublishedLocalJobBottomSheetContent(
    bottomSheetValue: SheetValue? = null,
    onCurrentLocationSelected: (CurrentLocation) -> Unit,
    onRecentLocationSelected: (RecentLocation) -> Unit,
    onDistrictSelected: (District) -> Unit,
    onPopUpLocationBottomSheet: () -> Unit,
    locationStatesEnabled: Boolean = true,
    isLoading: Boolean = false,
    viewModel: PublishedLocalJobViewModel,
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
                        LocationSetUpRoutes.LocationChooser()
                    else LocationSetUpRoutes.LocationChooser(
                        false
                    ),
                    modifier = Modifier.fillMaxSize()
                ) {
                    noTransitionComposable<LocationSetUpRoutes.LocationChooser> {

                        EditPublishedLocalJobLocationBottomSheet(
                            bottomSheetValue,
                            onCloseClick = {
                                onPopUpLocationBottomSheet()
                            },
                            onCurrentLocationSelected = onCurrentLocationSelected,
                            onRecentLocationSelected = onRecentLocationSelected,
                            onStateClick = {
                                bottomSheetNavController.navigate(LocationSetUpRoutes.Districts)
                            },
                            viewModel)
                    }

                    noTransitionComposable<LocationSetUpRoutes.Districts> {
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
fun PublishedUsedProductListingBottomSheetContent(
    bottomSheetValue: SheetValue? = null,
    onCurrentLocationSelected: (CurrentLocation) -> Unit,
    onRecentLocationSelected: (RecentLocation) -> Unit,
    onDistrictSelected: (District) -> Unit,
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
                        LocationSetUpRoutes.LocationChooser()
                    else LocationSetUpRoutes.LocationChooser(
                        false
                    ),
                    modifier = Modifier.fillMaxSize()
                ) {
                    noTransitionComposable<LocationSetUpRoutes.LocationChooser> {

                        EditPublishedUsedProductListingLocationBottomSheet(
                            bottomSheetValue,
                            onCloseClick = {
                                onPopUpLocationBottomSheet()
                            },
                            onCurrentLocationSelected = onCurrentLocationSelected,
                            onRecentLocationSelected = onRecentLocationSelected,
                            onStateClick = {
                                bottomSheetNavController.navigate(LocationSetUpRoutes.Districts)
                            },
                            publishedUsedProductsListingViewModel)
                    }

                    noTransitionComposable<LocationSetUpRoutes.Districts> {
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
    onDistrictSelected: (District) -> Unit,
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
                        LocationSetUpRoutes.LocationChooser()
                    else LocationSetUpRoutes.LocationChooser(
                        false
                    ),
                    modifier = Modifier.fillMaxSize()
                ) {
                    noTransitionComposable<LocationSetUpRoutes.LocationChooser> {

                        CreateUsedProductListingLocationBottomSheet(
                            bottomSheetValue,
                            onCloseClick = {
                                onPopUpLocationBottomSheet()
                            },
                            onCurrentLocationSelected = onCurrentLocationSelected,
                            onRecentLocationSelected = onRecentLocationSelected,
                            onStateClick = {
                                bottomSheetNavController.navigate(LocationSetUpRoutes.Districts)
                            },
                            usedProductsListingWorkflowViewModel)
                    }

                    noTransitionComposable<LocationSetUpRoutes.Districts> {
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
fun CreateLocalJobBottomSheetContent(
    bottomSheetValue: SheetValue? = null,
    onCurrentLocationSelected: (CurrentLocation) -> Unit,
    onRecentLocationSelected: (RecentLocation) -> Unit,
    onDistrictSelected: (District) -> Unit,
    onPopUpLocationBottomSheet: () -> Unit,
    locationStatesEnabled: Boolean = true,
    isLoading: Boolean = false,
    viewModel: LocalJobWorkFlowViewModel,
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
                        LocationSetUpRoutes.LocationChooser()
                    else LocationSetUpRoutes.LocationChooser(
                        false
                    ),
                    modifier = Modifier.fillMaxSize()
                ) {
                    noTransitionComposable<LocationSetUpRoutes.LocationChooser> {

                        CreateLocalJobLocationBottomSheet(
                            bottomSheetValue,
                            onCloseClick = {
                                onPopUpLocationBottomSheet()
                            },
                            onCurrentLocationSelected = onCurrentLocationSelected,
                            onRecentLocationSelected = onRecentLocationSelected,
                            onStateClick = {
                                bottomSheetNavController.navigate(LocationSetUpRoutes.Districts)
                            },
                            viewModel)
                    }

                    noTransitionComposable<LocationSetUpRoutes.Districts> {
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
    onDistrictSelected: (District) -> Unit,
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
                        LocationSetUpRoutes.LocationChooser()
                    else LocationSetUpRoutes.LocationChooser(
                        false
                    ),
                    modifier = Modifier.fillMaxSize()
                ) {
                    noTransitionComposable<LocationSetUpRoutes.LocationChooser> {

                        CreateServiceLocationBottomSheet(
                            bottomSheetValue,
                            onCloseClick = {
                                onPopUpLocationBottomSheet()
                            },
                            onCurrentLocationSelected = onCurrentLocationSelected,
                            onRecentLocationSelected = onRecentLocationSelected,
                            onStateClick = {
                                bottomSheetNavController.navigate(LocationSetUpRoutes.Districts)
                            },
                            createServiceViewModel)
                    }

                    noTransitionComposable<LocationSetUpRoutes.Districts> {
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
fun OnBoardLocationBottomSheetContent(
    bottomSheetValue: SheetValue? = null,
    onCurrentLocationSelected: (CurrentLocation) -> Unit,
    onRecentLocationSelected: (RecentLocation) -> Unit,
    onDistrictSelected: (District) -> Unit,
    onPopUpLocationBottomSheet: () -> Unit,
    locationStatesEnabled: Boolean = true,
    isLoading: Boolean = false) {
    val bottomSheetNavController = rememberNavController()

    Box{
        Scaffold { contentPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
            ) {

                NavHost(
                    navController = bottomSheetNavController,
                    startDestination = if (locationStatesEnabled)
                        LocationSetUpRoutes.LocationChooser()
                    else LocationSetUpRoutes.LocationChooser(
                        false
                    ),
                    modifier = Modifier.fillMaxSize()
                ) {
                    noTransitionComposable<LocationSetUpRoutes.LocationChooser> {

                        OnBoardLocationBottomSheet(
                            bottomSheetValue,
                            onCloseClick = {
                                onPopUpLocationBottomSheet()
                            },
                            onCurrentLocationSelected = onCurrentLocationSelected,
                            onRecentLocationSelected = onRecentLocationSelected,
                            onStateClick = {
                                bottomSheetNavController.navigate(LocationSetUpRoutes.Districts)
                            })
                    }

                    noTransitionComposable<LocationSetUpRoutes.Districts> {
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
    onDistrictSelected: (District) -> Unit,
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
                        LocationSetUpRoutes.LocationChooser()
                    else LocationSetUpRoutes.LocationChooser(
                        false
                    ),
                    modifier = Modifier.fillMaxSize()
                ) {
                    noTransitionComposable<LocationSetUpRoutes.LocationChooser> {

                        EditPublishedServiceLocationBottomSheet(
                            bottomSheetValue,
                            onCloseClick = {
                                onPopUpLocationBottomSheet()
                            },
                            onCurrentLocationSelected = onCurrentLocationSelected,
                            onRecentLocationSelected = onRecentLocationSelected,
                            onStateClick = {
                                bottomSheetNavController.navigate(LocationSetUpRoutes.Districts)
                            },
                            publishedServicesViewModel)
                    }

                    noTransitionComposable<LocationSetUpRoutes.Districts> {
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

