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
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import com.lts360.app.database.models.profile.RecentLocation
import com.lts360.compose.ui.auth.LoadingDialog
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

    val backStacks = rememberNavBackStack(
        if (locationStatesEnabled)
            LocationSetUpRoutes.LocationChooser()
        else
            LocationSetUpRoutes.LocationChooser(false)
    )


    Box(modifier = Modifier.fillMaxSize()){
        Scaffold(modifier = Modifier.fillMaxSize()){ contentPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
            ) {
                NavDisplay(
                    backStack = backStacks,
                    entryDecorators = listOf(
                        rememberSceneSetupNavEntryDecorator(),
                        rememberSavedStateNavEntryDecorator(),
                        rememberViewModelStoreNavEntryDecorator(shouldRemoveStoreOwner = { false })
                    ),
                    entryProvider = entryProvider {
                        entry<LocationSetUpRoutes.LocationChooser> { route ->
                            OnBoardLocationBottomSheet(
                                bottomSheetValue = bottomSheetValue,
                                onCloseClick = { onPopUpLocationBottomSheet() },
                                onRecentLocationSelected = onRecentLocationSelected,
                                onCurrentLocationSelected = onCurrentLocationSelected,
                                onStateClick = {
                                    backStacks.add(LocationSetUpRoutes.Districts)
                                }
                            )
                        }

                        entry<LocationSetUpRoutes.Districts> {
                            DistrictsScreen(
                                isLoading = isLoading,
                                onDistrictSelected = onDistrictSelected,
                            ){
                                backStacks.removeLastOrNull()
                            }
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
fun GuestUserLocationAccessBottomSheetScreen(
    bottomSheetValue: SheetValue? = null,
    onCurrentLocationSelected: (CurrentLocation) -> Unit,
    onRecentLocationSelected: (RecentLocation) -> Unit,
    onDistrictSelected: (District) -> Unit,
    onPopUpLocationBottomSheet: () -> Unit,
    homeViewModel: HomeViewModel,
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
                        rememberViewModelStoreNavEntryDecorator(shouldRemoveStoreOwner = {false})
                    ),
                    entryProvider = entryProvider {
                        entry<LocationSetUpRoutes.LocationChooser> {
                            GuestUserLocationBottomSheet(
                                bottomSheetValue = bottomSheetValue,
                                onCloseClick = onPopUpLocationBottomSheet,
                                onRecentLocationSelected = onRecentLocationSelected,
                                onCurrentLocationSelected = onCurrentLocationSelected,
                                onStateClick = {
                                    backStack.add(LocationSetUpRoutes.Districts)
                                },
                                homeViewModel = homeViewModel
                            )
                        }

                        entry<LocationSetUpRoutes.Districts> {
                            DistrictsScreen(
                                isLoading = isLoading,
                                onDistrictSelected = onDistrictSelected,
                            ){
                                backStack.removeLastOrNull()
                            }
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


