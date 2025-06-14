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
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import com.lts360.app.database.models.profile.RecentLocation
import com.lts360.compose.ui.auth.LoadingDialog
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
    backStack: NavBackStack,
    bottomSheetValue: SheetValue? = null,
    onCurrentLocationSelected: (CurrentLocation) -> Unit,
    onRecentLocationSelected: (RecentLocation) -> Unit,
    onDistrictSelected: (District) -> Unit,
    onPopUpLocationBottomSheet: () -> Unit,
    homeViewModel: HomeViewModel,
    locationStatesEnabled: Boolean = true,
    isLoading: Boolean = false
) {


    UserLocationBottomSheetContent(
        backStack,
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
        isLoading
    )

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserLocationBottomSheetContent(
    backStack: NavBackStack,
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

                NavDisplay(
                    backStack = backStack,
                    modifier = Modifier.fillMaxSize(),
                    entryDecorators = listOf(
                        rememberSceneSetupNavEntryDecorator(),
                        rememberSavedStateNavEntryDecorator(),
                        rememberViewModelStoreNavEntryDecorator()
                    ),
                    entryProvider = entryProvider {
                        entry<LocationSetUpRoutes.LocationChooser> {
                            UserLocationBottomSheet(
                                bottomSheetValue = bottomSheetValue,
                                onCloseClick = { onPopUpLocationBottomSheet() },
                                onCurrentLocationSelected = onCurrentLocationSelected,
                                onRecentLocationSelected = onRecentLocationSelected,
                                onStateClick = { backStack.add(LocationSetUpRoutes.Districts) },
                                homeViewModel = homeViewModel
                            )
                        }

                        entry<LocationSetUpRoutes.Districts> {
                            DistrictsScreen(
                                isLoading = isLoading,
                                onDistrictSelected = onDistrictSelected,
                                onPopDown = { backStack.removeLastOrNull() }
                            )
                        }
                    }
                )

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
    val backStack = rememberNavBackStack(
        if (locationStatesEnabled)
            LocationSetUpRoutes.LocationChooser()
        else
            LocationSetUpRoutes.LocationChooser(false)
    )

    Box {
        Scaffold { contentPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
            ) {
                NavDisplay(
                    backStack = backStack,
                    modifier = Modifier.fillMaxSize(),
                    entryDecorators = listOf(
                        rememberSceneSetupNavEntryDecorator(),
                        rememberSavedStateNavEntryDecorator(),
                        rememberViewModelStoreNavEntryDecorator()
                    ),
                    entryProvider = entryProvider {
                        entry<LocationSetUpRoutes.LocationChooser> {
                            EditPublishedLocalJobLocationBottomSheet(
                                bottomSheetValue = bottomSheetValue,
                                onCloseClick = onPopUpLocationBottomSheet,
                                onCurrentLocationSelected = onCurrentLocationSelected,
                                onRecentLocationSelected = onRecentLocationSelected,
                                onStateClick = {
                                    backStack.add(LocationSetUpRoutes.Districts)
                                },
                                viewModel = viewModel
                            )
                        }

                        entry<LocationSetUpRoutes.Districts> {
                            DistrictsScreen(
                                isLoading = isLoading,
                                onDistrictSelected = onDistrictSelected,
                                onPopDown = { backStack.removeLastOrNull() }
                            )
                        }
                    }
                )
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
    val backStack = rememberNavBackStack(
        if (locationStatesEnabled)
            LocationSetUpRoutes.LocationChooser()
        else
            LocationSetUpRoutes.LocationChooser(false)
    )

    Box {
        Scaffold { contentPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
            ) {
                NavDisplay(
                    backStack = backStack,
                    modifier = Modifier.fillMaxSize(),
                    entryDecorators = listOf(
                        rememberSceneSetupNavEntryDecorator(),
                        rememberSavedStateNavEntryDecorator(),
                        rememberViewModelStoreNavEntryDecorator()
                    ),
                    entryProvider = entryProvider {
                        entry<LocationSetUpRoutes.LocationChooser> {
                            EditPublishedUsedProductListingLocationBottomSheet(
                                bottomSheetValue = bottomSheetValue,
                                onCloseClick = onPopUpLocationBottomSheet,
                                onCurrentLocationSelected = onCurrentLocationSelected,
                                onRecentLocationSelected = onRecentLocationSelected,
                                onStateClick = {
                                    backStack.add(LocationSetUpRoutes.Districts)
                                },
                                publishedUsedProductsListingViewModel = publishedUsedProductsListingViewModel
                            )
                        }

                        entry<LocationSetUpRoutes.Districts> {
                            DistrictsScreen(
                                isLoading = isLoading,
                                onDistrictSelected = onDistrictSelected,
                                onPopDown = { backStack.removeLastOrNull() }
                            )
                        }
                    }
                )
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
    val backStack = rememberNavBackStack(
        if (locationStatesEnabled)
            LocationSetUpRoutes.LocationChooser()
        else
            LocationSetUpRoutes.LocationChooser(false)
    )

    Box {
        Scaffold { contentPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
            ) {
                NavDisplay(
                    backStack = backStack,
                    modifier = Modifier.fillMaxSize(),
                    entryDecorators = listOf(
                        rememberSceneSetupNavEntryDecorator(),
                        rememberSavedStateNavEntryDecorator(),
                        rememberViewModelStoreNavEntryDecorator()
                    ),
                    entryProvider = entryProvider {
                        entry<LocationSetUpRoutes.LocationChooser> {
                            CreateUsedProductListingLocationBottomSheet(
                                bottomSheetValue = bottomSheetValue,
                                onCloseClick = onPopUpLocationBottomSheet,
                                onCurrentLocationSelected = onCurrentLocationSelected,
                                onRecentLocationSelected = onRecentLocationSelected,
                                onStateClick = {
                                    backStack.add(LocationSetUpRoutes.Districts)
                                },
                                usedProductsListingWorkflowViewModel = usedProductsListingWorkflowViewModel
                            )
                        }

                        entry<LocationSetUpRoutes.Districts> {
                            DistrictsScreen(
                                isLoading = isLoading,
                                onDistrictSelected = onDistrictSelected,
                                onPopDown = { backStack.removeLastOrNull() }
                            )
                        }
                    }
                )
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
    val backStack = rememberNavBackStack(
        if (locationStatesEnabled)
            LocationSetUpRoutes.LocationChooser()
        else
            LocationSetUpRoutes.LocationChooser(false)
    )

    Box {
        Scaffold { contentPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding))
            {
                NavDisplay(
                    backStack = backStack,
                    modifier = Modifier.fillMaxSize(),
                    entryDecorators = listOf(
                        rememberSceneSetupNavEntryDecorator(),
                        rememberSavedStateNavEntryDecorator(),
                        rememberViewModelStoreNavEntryDecorator()
                    ),
                    entryProvider = entryProvider {
                        entry<LocationSetUpRoutes.LocationChooser> {
                            CreateLocalJobLocationBottomSheet(
                                bottomSheetValue = bottomSheetValue,
                                onCloseClick = onPopUpLocationBottomSheet,
                                onCurrentLocationSelected = onCurrentLocationSelected,
                                onRecentLocationSelected = onRecentLocationSelected,
                                onStateClick = {
                                    backStack.add(LocationSetUpRoutes.Districts)
                                },
                                viewModel = viewModel
                            )
                        }

                        entry<LocationSetUpRoutes.Districts> {
                            DistrictsScreen(
                                isLoading = isLoading,
                                onDistrictSelected = onDistrictSelected,
                                onPopDown = { backStack.removeLastOrNull() }
                            )
                        }
                    }
                )
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
    val backStack = rememberNavBackStack(
        if (locationStatesEnabled)
            LocationSetUpRoutes.LocationChooser()
        else
            LocationSetUpRoutes.LocationChooser(false)
    )

    Box {
        Scaffold { contentPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding))
            {
                NavDisplay(
                    backStack = backStack,
                    modifier = Modifier.fillMaxSize(),
                    entryDecorators = listOf(
                        rememberSceneSetupNavEntryDecorator(),
                        rememberSavedStateNavEntryDecorator(),
                        rememberViewModelStoreNavEntryDecorator()
                    ),
                    entryProvider = entryProvider {
                        entry<LocationSetUpRoutes.LocationChooser> {
                            CreateServiceLocationBottomSheet(
                                bottomSheetValue = bottomSheetValue,
                                onCloseClick = onPopUpLocationBottomSheet,
                                onCurrentLocationSelected = onCurrentLocationSelected,
                                onRecentLocationSelected = onRecentLocationSelected,
                                onStateClick = {
                                    backStack.add(LocationSetUpRoutes.Districts)
                                },
                                createServiceViewModel = createServiceViewModel
                            )
                        }

                        entry<LocationSetUpRoutes.Districts> {
                            DistrictsScreen(
                                isLoading = isLoading,
                                onDistrictSelected = onDistrictSelected,
                                onPopDown = { backStack.removeLastOrNull() }
                            )
                        }
                    }
                )
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
    isLoading: Boolean = false
) {
    val backStack = rememberNavBackStack(
        if (locationStatesEnabled)
            LocationSetUpRoutes.LocationChooser()
        else
            LocationSetUpRoutes.LocationChooser(false)
    )

    Box {
        Scaffold { contentPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
            ) {
                NavDisplay(
                    backStack = backStack,
                    modifier = Modifier.fillMaxSize(),
                    entryDecorators = listOf(
                        rememberSceneSetupNavEntryDecorator(),
                        rememberSavedStateNavEntryDecorator(),
                        rememberViewModelStoreNavEntryDecorator()
                    ),
                    entryProvider = entryProvider {
                        entry<LocationSetUpRoutes.LocationChooser> {
                            OnBoardLocationBottomSheet(
                                bottomSheetValue = bottomSheetValue,
                                onCloseClick = onPopUpLocationBottomSheet,
                                onCurrentLocationSelected = onCurrentLocationSelected,
                                onRecentLocationSelected = onRecentLocationSelected,
                                onStateClick = {
                                    backStack.add(LocationSetUpRoutes.Districts)
                                }
                            )
                        }

                        entry<LocationSetUpRoutes.Districts> {
                            DistrictsScreen(
                                isLoading = isLoading,
                                onDistrictSelected = onDistrictSelected,
                                onPopDown = { backStack.removeLastOrNull() }
                            )
                        }
                    }
                )
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
    val backStack = rememberNavBackStack(
        if (locationStatesEnabled)
            LocationSetUpRoutes.LocationChooser()
        else
            LocationSetUpRoutes.LocationChooser(false)
    )

    Box {
        Scaffold { contentPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding))
            {
                NavDisplay(
                    backStack = backStack,
                    modifier = Modifier.fillMaxSize(),
                    entryDecorators = listOf(
                        rememberSceneSetupNavEntryDecorator(),
                        rememberSavedStateNavEntryDecorator(),
                        rememberViewModelStoreNavEntryDecorator()
                    ),
                    entryProvider = entryProvider {
                        entry<LocationSetUpRoutes.LocationChooser> {
                            EditPublishedServiceLocationBottomSheet(
                                bottomSheetValue = bottomSheetValue,
                                onCloseClick = onPopUpLocationBottomSheet,
                                onCurrentLocationSelected = onCurrentLocationSelected,
                                onRecentLocationSelected = onRecentLocationSelected,
                                onStateClick = {
                                    backStack.add(LocationSetUpRoutes.Districts)
                                },
                                publishedServicesViewModel = publishedServicesViewModel
                            )
                        }

                        entry<LocationSetUpRoutes.Districts> {
                            DistrictsScreen(
                                isLoading = isLoading,
                                onDistrictSelected = onDistrictSelected,
                                onPopDown = { backStack.removeLastOrNull() }
                            )
                        }
                    }
                )
            }

        }

        if (isLoading) {
            LoadingDialog()
        }
    }
}

