package com.lts360.compose.ui.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.lts360.compose.ui.main.navhosts.routes.LocationSetUpRoutes
import com.lts360.compose.ui.viewmodels.LocationViewModel
import com.lts360.compose.utils.NavigatorCard


@Composable
fun DistrictsScreen(
    navHostController: NavHostController,
    isLoading: Boolean = false,
    onDistrictClicked: (District) -> Unit,
    onPopDown: () -> Unit,
) {


    val backStackEntry = remember {
        navHostController.getBackStackEntry<LocationSetUpRoutes.LocationChooser>()
    }

    val locationViewModel: LocationViewModel = hiltViewModel(backStackEntry)

    val districts by locationViewModel.districts.collectAsState()





    BackHandler {
        if (!isLoading) {
            locationViewModel.removeSelectedDistrictSavedState()
            onPopDown()
        }
    }

    Scaffold { contentPadding ->
        LazyColumn(
            modifier = Modifier.padding(contentPadding),
            contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp)
        ) {

            item {

                Text(
                    text = "Choose from",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

            }

            items(districts, key = { it.district }) { districtItem ->

                CompositionLocalProvider(
                    LocalMinimumInteractiveComponentSize provides 0.dp // Remove extra touch padding
                ) {
                    NavigatorCard(
                        isLoading = isLoading,
                        onCardClicked = {
                            onDistrictClicked(districtItem)

                        }) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                districtItem.district,
                                modifier = Modifier
                                    .padding(16.dp)
                                    .padding(start = 8.dp)
                            )
                        }
                    }
                }

            }

        }


    }
}
