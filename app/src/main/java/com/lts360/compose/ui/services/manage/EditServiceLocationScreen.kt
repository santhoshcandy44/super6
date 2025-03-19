package com.lts360.compose.ui.services.manage


import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.lts360.compose.ui.auth.LoadingDialog
import com.lts360.compose.ui.main.EditLocationBottomSheetScreen
import com.lts360.compose.ui.services.manage.viewmodels.PublishedServicesViewModel
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditServiceLocationScreen(
    onPopBackStack: () -> Unit,
    viewModel: PublishedServicesViewModel
) {


    val isUpdating by viewModel.isUpdating.collectAsState()
    val location by viewModel.editableLocation.collectAsState()

    val selectedService by viewModel.selectedService.collectAsState()

    val context = LocalContext.current


    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            SheetValue.Hidden,
            skipHiddenState = false
        )
    )


    val coroutineScope = rememberCoroutineScope()

    BackHandler(bottomSheetScaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
        coroutineScope.launch {
            bottomSheetScaffoldState.bottomSheetState.hide()
        }
    }



    BottomSheetScaffold(
        scaffoldState = bottomSheetScaffoldState,
        sheetShape = RectangleShape,
        sheetContent = {
            EditLocationBottomSheetScreen(
                bottomSheetScaffoldState.bottomSheetState.currentValue,
                { currentLocation ->
                    location?.let {
                        viewModel.updateLocation(
                            it.copy(
                                latitude = currentLocation.latitude,
                                longitude = currentLocation.longitude,
                                geo = currentLocation.geo,
                                locationType = currentLocation.locationType
                            )
                        )
                    }

                    coroutineScope.launch {
                        bottomSheetScaffoldState.bottomSheetState.hide()
                    }

                }, { recentLocation ->
                    location?.let {
                        viewModel.updateLocation(
                            it.copy(
                                latitude = recentLocation.latitude,
                                longitude = recentLocation.longitude,
                                geo = recentLocation.geo,
                                locationType = recentLocation.locationType
                            )
                        )
                    }

                    coroutineScope.launch {
                        bottomSheetScaffoldState.bottomSheetState.hide()
                    }

                }, { district, callback ->
                    location?.let {
                        viewModel.updateLocation(
                            it.copy(
                                latitude = district.coordinates.latitude,
                                longitude = district.coordinates.longitude,
                                geo = district.district,
                                locationType = "approximate"
                            )
                        )
                    }
                    callback()
                },
                {
                    coroutineScope.launch {
                        bottomSheetScaffoldState.bottomSheetState.hide()
                    }
                },
                locationStatesEnabled = false,
                publishedServicesViewModel = viewModel
            )

        },
        sheetPeekHeight = 0.dp, // Default height when sheet is collapsed
        sheetSwipeEnabled = false // Allow gestures to hide/show bottom sheet

    ) { innerPadding ->

        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        navigationIcon = {
                            IconButton(onClick = onPopBackStack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back Icon"
                                )
                            }
                        },
                        title = {
                            Text(
                                text = "Manage Location",
                                style = MaterialTheme.typography.titleMedium
                            )
                        })
                },
                content = { contentPadding ->
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(contentPadding),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)

                    ) {


                        item {
                            Text(
                                text = "Edit Service Location",
                                style = MaterialTheme.typography.titleMedium
                            )

                            Spacer(modifier = Modifier.height(8.dp))


                            OutlinedTextField(
                                value = location?.geo ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(text = "Location") },
                                modifier = Modifier
                                    .fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedButton(
                                onClick = {
                                    coroutineScope.launch {
                                        bottomSheetScaffoldState.bottomSheetState.expand()
                                    }
                                },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                ),
                                shape = RectangleShape,
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Choose Location",
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text("Choose Location")
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    selectedService?.let { nonNullSelectedService ->
                                        if (viewModel.validateSelectedLocation()) {
                                            viewModel.onUpdateServiceLocation(
                                                viewModel.userId,
                                                nonNullSelectedService.serviceId,
                                                location!!,
                                                {
                                                    Toast.makeText(
                                                        context, it, Toast.LENGTH_SHORT
                                                    ).show()
                                                }, {
                                                    Toast.makeText(
                                                        context, it, Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            )
                                        }

                                    }
                                },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color.White,
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {

                                Text("Update Location")
                            }
                        }


                    }

                }
            )


            if (isUpdating) {
                LoadingDialog()
            }
        }

    }


}



