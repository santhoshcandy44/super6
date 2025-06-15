package com.lts360.compose.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
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
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditLocationBottomSheetScreen(
    onRemoveLocationUpdates: Boolean,
    onCurrentLocationSelected: (CurrentLocation) -> Unit,
    onRecentLocationSelected: (RecentLocation) -> Unit,
    onDistrictSelected: (District) -> Unit,
    onPopUpLocationBottomSheet: () -> Unit,
    publishedServicesViewModel: PublishedServicesViewModel,
    locationStatesEnabled: Boolean = true,
    isLoading: Boolean = false,
) {

    EditLocationBottomSheetContent(
        onRemoveLocationUpdates,
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
    onRemoveLocationUpdates: Boolean,
    onCurrentLocationSelected: (CurrentLocation) -> Unit,
    onRecentLocationSelected: (RecentLocation) -> Unit,
    onDistrictSelected: (District) -> Unit,
    onPopUpLocationBottomSheet: () -> Unit,
    homeViewModel: HomeViewModel,
    locationStatesEnabled: Boolean = true,
    isLoading: Boolean = false
) {

    UserLocationBottomSheetContent(
        onRemoveLocationUpdates,
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
    onRemoveLocationUpdates: Boolean,
    onCurrentLocationSelected: (CurrentLocation) -> Unit,
    onRecentLocationSelected: (RecentLocation) -> Unit,
    onDistrictSelected: (District) -> Unit,
    onPopUpLocationBottomSheet: () -> Unit,
    createServiceViewModel: ServicesWorkflowViewModel,
    locationStatesEnabled: Boolean = true,
    isLoading: Boolean = false,
) {
    CreateServiceLocationBottomSheetContent(
        onRemoveLocationUpdates,
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
    onRemoveLocationUpdates: Boolean,
    onCurrentLocationSelected: (CurrentLocation) -> Unit,
    onRecentLocationSelected: (RecentLocation) -> Unit,
    onDistrictSelected: (District) -> Unit,
    onPopUpLocationBottomSheet: () -> Unit,
    viewModel: LocalJobWorkFlowViewModel,
    locationStatesEnabled: Boolean = true,
    isLoading: Boolean = false,
) {
    CreateLocalJobBottomSheetContent(
        onRemoveLocationUpdates,
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
    onRemoveLocationUpdates: Boolean,
    onCurrentLocationSelected: (CurrentLocation) -> Unit,
    onRecentLocationSelected: (RecentLocation) -> Unit,
    onDistrictSelected: (District) -> Unit,
    onPopUpLocationBottomSheet: () -> Unit,
    usedProductsListingWorkflowViewModel: UsedProductsListingWorkflowViewModel,
    locationStatesEnabled: Boolean = true,
    isLoading: Boolean = false,
) {


    CreateUsedProductListingBottomSheetContent(
        onRemoveLocationUpdates,
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
    onRemoveLocationUpdates: Boolean,
    onCurrentLocationSelected: (CurrentLocation) -> Unit,
    onRecentLocationSelected: (RecentLocation) -> Unit,
    onDistrictSelected: (District) -> Unit,
    onPopUpLocationBottomSheet: () -> Unit,
    viewModel: PublishedLocalJobViewModel,
    locationStatesEnabled: Boolean = true,
    isLoading: Boolean = false,
) {
    PublishedLocalJobBottomSheetContent(
        onRemoveLocationUpdates,
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
    onRemoveLocationUpdates: Boolean,
    onCurrentLocationSelected: (CurrentLocation) -> Unit,
    onRecentLocationSelected: (RecentLocation) -> Unit,
    onDistrictSelected: (District) -> Unit,
    onPopUpLocationBottomSheet: () -> Unit,
    usedProductsListingWorkflowViewModel: PublishedUsedProductsListingViewModel,
    locationStatesEnabled: Boolean = true,
    isLoading: Boolean = false,
) {
    PublishedUsedProductListingBottomSheetContent(
        onRemoveLocationUpdates,
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
    onRemoveLocationUpdates: Boolean,
    onCurrentLocationSelected: (CurrentLocation) -> Unit,
    onRecentLocationSelected: (RecentLocation) -> Unit,
    onDistrictSelected: (District) -> Unit,
    onPopUpLocationBottomSheet: () -> Unit,
    locationStatesEnabled: Boolean = true,
    isLoading: Boolean = false
) {

    OnBoardLocationBottomSheetContent(
        onRemoveLocationUpdates,
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
    onRemoveLocationUpdates: Boolean,
    onCurrentLocationSelected: (CurrentLocation) -> Unit,
    onRecentLocationSelected: (RecentLocation) -> Unit,
    onDistrictSelected: (District) -> Unit,
    onPopUpLocationBottomSheet: () -> Unit,
    locationStatesEnabled: Boolean = true,
    isLoading: Boolean = false,
    homeViewModel: HomeViewModel
) {

    val selectedLocationGeo by homeViewModel.selectedLocationGeo.collectAsState()

    val backStack = rememberNavBackStack(
        if (locationStatesEnabled)
            LocationSetUpRoutes.LocationChooser()
        else
            LocationSetUpRoutes.LocationChooser(false)
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(modifier = Modifier.fillMaxSize()) { contentPadding ->
            NavDisplay(
                backStack = backStack,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
                entryDecorators = listOf(
                    rememberSceneSetupNavEntryDecorator(),
                    rememberSavedStateNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator()
                ),
                entryProvider = entryProvider {
                    entry<LocationSetUpRoutes.LocationChooser> { navEntry ->

                        UserLocationBottomSheet(
                            locationViewModel = koinViewModel(parameters = { parametersOf(navEntry) }),
                            selectedLocationGeo = selectedLocationGeo,
                            onRemoveLocationUpdates = onRemoveLocationUpdates,
                            onCloseClick = { onPopUpLocationBottomSheet() },
                            onCurrentLocationSelected = onCurrentLocationSelected,
                            onRecentLocationSelected = onRecentLocationSelected,
                            onStateClick = { backStack.add(LocationSetUpRoutes.Districts) }
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

        if (isLoading) {
            LoadingDialog()
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishedLocalJobBottomSheetContent(
    onRemoveLocationUpdates: Boolean,
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
                        entry<LocationSetUpRoutes.LocationChooser> { navEntry ->
                            EditPublishedLocalJobLocationBottomSheet(
                                locationViewModel = koinViewModel(parameters = {
                                    parametersOf(
                                        navEntry
                                    )
                                }),
                                onRemoveLocationUpdates = onRemoveLocationUpdates,
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
    onRemoveLocationUpdates: Boolean,
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
                        entry<LocationSetUpRoutes.LocationChooser> { navEntry ->
                            EditPublishedUsedProductListingLocationBottomSheet(
                                locationViewModel = koinViewModel(parameters = {
                                    parametersOf(
                                        navEntry
                                    )
                                }),

                                onRemoveLocationUpdates = onRemoveLocationUpdates,
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
    onRemoveLocationUpdates: Boolean,
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
                        entry<LocationSetUpRoutes.LocationChooser> { navEntry ->
                            CreateUsedProductListingLocationBottomSheet(
                                locationViewModel = koinViewModel(parameters = {
                                    parametersOf(
                                        navEntry
                                    )
                                }),
                                onRemoveLocationUpdates = onRemoveLocationUpdates,
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
    onRemoveLocationUpdates: Boolean,
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
                    .padding(contentPadding)
            )
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
                        entry<LocationSetUpRoutes.LocationChooser> { navEntry ->
                            CreateLocalJobLocationBottomSheet(
                                locationViewModel = koinViewModel(parameters = {
                                    parametersOf(
                                        navEntry
                                    )
                                }),
                                onRemoveLocationUpdates = onRemoveLocationUpdates,
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
    onRemoveLocationUpdates: Boolean,
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
                    .padding(contentPadding)
            )
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
                        entry<LocationSetUpRoutes.LocationChooser> { navEntry ->
                            CreateServiceLocationBottomSheet(
                                locationViewModel = koinViewModel(parameters = {
                                    parametersOf(
                                        navEntry
                                    )
                                }),
                                onRemoveLocationUpdates = onRemoveLocationUpdates,
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
    onRemoveLocationUpdates: Boolean,
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
                        entry<LocationSetUpRoutes.LocationChooser> { navEntry ->
                            OnBoardLocationBottomSheet(
                                locationViewModel = koinViewModel(parameters = {
                                    parametersOf(
                                        navEntry
                                    )
                                }),
                                onRemoveLocationUpdates = onRemoveLocationUpdates,
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
    onRemoveLocationUpdates: Boolean,
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
                    .padding(contentPadding)
            )
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
                        entry<LocationSetUpRoutes.LocationChooser> { navEntry ->
                            EditPublishedServiceLocationBottomSheet(
                                locationViewModel = koinViewModel(parameters = {
                                    parametersOf(
                                        navEntry
                                    )
                                }),
                                onRemoveLocationUpdates = onRemoveLocationUpdates,
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

