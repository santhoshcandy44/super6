package com.lts360.compose.ui.main

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.lts360.compose.ui.common.CircularProgressIndicatorLegacy
import com.lts360.compose.ui.main.models.CurrentLocation
import com.lts360.compose.ui.main.navhosts.routes.BottomBar
import com.lts360.compose.ui.main.viewmodels.HomeViewModel
import com.lts360.compose.ui.main.viewmodels.SecondsViewmodel
import com.lts360.compose.ui.services.ServiceScreen
import com.lts360.compose.ui.usedproducts.SecondsScreen
import com.lts360.compose.ui.viewmodels.ServicesViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    onNavigateUpServiceDetailedScreen: () -> Unit,
    onNavigateUpUsedProductListingDetailedScreen: () -> Unit,
    onNavigateUpServiceOwnerProfile: (Long) -> Unit,
    showChooseIndustriesSheet: () -> Unit,
    onPopBackStack: () -> Unit,
    viewModel: HomeViewModel,
    servicesViewModel: ServicesViewModel,
    secondsViewModel: SecondsViewmodel,
    onDockedFabAddNewSecondsChanged: (Boolean) -> Unit,
    onlySearchBar: Boolean = false,
    nestedType: String? = null
) {


    val boards = listOf("Services", "Second Hands")
    val initialPageIndex =
        if (nestedType.isNullOrEmpty()) 0 else boards.indexOf(nestedType).coerceAtLeast(0)


    val pagerState = rememberPagerState(
        initialPageIndex,
        pageCount = { boards.size })


    val userId = viewModel.userId
    val signInMethod = viewModel.signInMethod
    val selectedLocationGeo by viewModel.selectedLocationGeo.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val topBarBarHeight = remember { mutableFloatStateOf(0f) }
    val topBarOffsetHeightPx = remember { mutableFloatStateOf(0f) }
    val showTopBar = remember { mutableStateOf(true) }

    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchJob by viewModel.searchJob.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()

    val suggestions by viewModel.suggestions.collectAsState()

    val isLazyLoading by viewModel.isLazyLoading.collectAsState()

    val focusManager = LocalFocusManager.current


    val scope = rememberCoroutineScope()

    if (!onlySearchBar) {
        BackHandler(isSearching) {
            // Handle search collapse on the main screen
            if (isSearching) {
                viewModel.collapseSearchAction(true)
            }
        }
    }

    if (onlySearchBar) {
        BackHandler(isSearching) {
            // Handle search collapse or pop overlay if no search
            if (isSearching) {
                viewModel.collapseSearchAction(true)
                scope.launch {
                    delay(100)
                    focusManager.clearFocus()
                }
            }
        }
    }


    val coroutineScope = rememberCoroutineScope()


    val modalBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val bottomSheetNavController = rememberNavController()


    val context = LocalContext.current

    Surface {
        Box(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(remember {

                        object : NestedScrollConnection {
                            override fun onPreScroll(
                                available: Offset,
                                source: NestedScrollSource
                            ): Offset {
                                val delta = available.y
                                val newOffset = topBarOffsetHeightPx.floatValue + delta
                                topBarOffsetHeightPx.floatValue =
                                    newOffset.coerceIn(-topBarBarHeight.floatValue, 0f)
                                showTopBar.value = newOffset >= 0f
                                return Offset.Zero
                            }
                        }
                    })
            ) {


                AnimatedVisibility(
                    visible = showTopBar.value,
                    enter = fadeIn(),  // Optional fade-in for showing
                    exit = fadeOut()   // Optional fade-out for hiding
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned { coordinates ->
                                topBarBarHeight.floatValue = coordinates.size.height.toFloat()
                            }, verticalArrangement = Arrangement.Center
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .padding(top = 16.dp, bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                "Lts360", fontSize = 22.sp, fontWeight = FontWeight.Medium,
                            )
                            LocationWrapper(location = selectedLocationGeo) {
                                coroutineScope.launch {
                                    modalBottomSheetState.expand()
                                }
                            }
                        }
                    }


                }


                Spacer(Modifier.height(8.dp))

                if (onlySearchBar) {

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth(),
                        shadowElevation = 0.dp
                    ) {

                        SearchBar(
                            query = searchQuery,
                            onQueryChange = { query ->

                                if (query.text.trim().isNotEmpty()) {

                                    if (viewModel.searchQuery.value.text != query.text) {

                                        viewModel.setSearching(true)
                                        viewModel.setSearchQuery(query.text)
                                        // Cancel any ongoing search job
                                        searchJob?.cancel()
                                        viewModel.clearJob()

                                        if (boards[pagerState.currentPage] == "Services") {
                                            viewModel.onGetServiceSearchQuerySuggestions(
                                                userId,
                                                query.text
                                            )
                                        } else if (boards[pagerState.currentPage] == "Second Hands") {
                                            viewModel.onGetUsedProductListingSearchQuerySuggestions(
                                                userId,
                                                query.text
                                            )

                                        }


                                    }

                                } else {

                                    if (viewModel.searchQuery.value.text != query.text
                                        && viewModel.isSearching.value
                                    ) {
                                        searchJob?.cancel()
                                        viewModel.clearJob()
                                        viewModel.setSearching(false)
                                        viewModel.setSearchQuery("")
                                        viewModel.setSuggestions(emptyList())
                                    }

                                }

                            },
                            onSearch = {

                                if (searchQuery.text.isNotEmpty()) {


                                    if (boards[pagerState.currentPage] == "Services") {
                                        viewModel.navigateToOverlay(
                                            navController, BottomBar.NestedServices(
                                                servicesViewModel.getKey() + 1,
                                                searchQuery.text,
                                                true
                                            )
                                        )
                                    } else if (boards[pagerState.currentPage] == "Second Hands") {

                                        viewModel.navigateToOverlay(
                                            navController, BottomBar.NestedSeconds(
                                                secondsViewModel.getKey() + 1,
                                                searchQuery.text,
                                                true
                                            )
                                        )

                                    }

                                }

                            },
                            onBackButtonClicked = {

                                if (!isSearching) {
                                    onPopBackStack()
                                } else {
                                    searchJob?.cancel()
                                    viewModel.setSuggestions(emptyList())
                                    viewModel.setSearching(false)
                                }
                            },
                            onClearClicked = {
                                searchJob?.cancel()
                                viewModel.setSuggestions(emptyList())
                                viewModel.setSearchQuery("")
                                viewModel.setSearching(false)
                            },
                            focusRequesterEnabled = false,
                            onFocus = {
                                if (it) {

                                    viewModel.setSearching(true)
                                }
                            }
                        )

                    }


                } else {


                    Surface(
                        modifier = Modifier
                            .fillMaxWidth(),
                        shadowElevation = 0.dp
                    ) {


                        SearchBar(
                            query = searchQuery,
                            onQueryChange = { query ->

                                if (!isSearching) {
                                    viewModel.setSearching(true)
                                }

                                if (query.text.trim().isNotEmpty()) {

                                    if (viewModel.searchQuery.value.text != query.text
                                        && viewModel.isSearching.value
                                    ) {


                                        viewModel.setSearchQuery(query.text)
                                        // Cancel any ongoing search job
                                        searchJob?.cancel()
                                        viewModel.clearJob()

                                        if (boards[pagerState.currentPage] == "Services") {
                                            viewModel.onGetServiceSearchQuerySuggestions(
                                                userId,
                                                query.text
                                            )
                                        } else if (boards[pagerState.currentPage] == "Second Hands") {

                                            viewModel.onGetUsedProductListingSearchQuerySuggestions(
                                                userId,
                                                query.text
                                            )
                                        }

                                    }


                                } else {

                                    if (viewModel.searchQuery.value.text != query.text
                                        && viewModel.isSearching.value
                                    ) {
                                        searchJob?.cancel()
                                        viewModel.clearJob()
                                        viewModel.setSearchQuery("")
                                        viewModel.setSuggestions(emptyList())
                                    }

                                }
                            },
                            onSearch = {
                                if (searchQuery.text.isNotEmpty()) {

                                    scope.launch {

                                        if (boards[pagerState.currentPage] == "Services") {
                                            viewModel.navigateToOverlay(
                                                navController, BottomBar.NestedServices(
                                                    servicesViewModel.getKey() + 1,
                                                    searchQuery.text,
                                                    true

                                                )
                                            )
                                        } else if (boards[pagerState.currentPage] == "Second Hands") {
                                            viewModel.navigateToOverlay(
                                                navController, BottomBar.NestedSeconds(
                                                    secondsViewModel.getKey() +1,
                                                    searchQuery.text,
                                                    true

                                                )
                                            )

                                        }
                                    }
                                }

                            },
                            onBackButtonClicked = { },
                            onClearClicked = {
                                searchJob?.cancel()
                                viewModel.setSuggestions(emptyList())
                                viewModel.setSearchQuery("")
                                viewModel.setSearching(false)
                            },
                            onFocus = {
                                if (it) {
                                    viewModel.setSearching(true)
                                }
                            },
                            focusRequesterEnabled = false,
                            isBackButtonEnabled = false
                        )

                    }

                }

                Box(modifier = Modifier.fillMaxSize()) {
                    if (!onlySearchBar) {

                        Boards(
                            boards,
                            pagerState,
                            servicesContent = {
                                ServiceScreen(
                                    navController,
                                    {
                                        viewModel.setSelectedServiceItem(it)
                                        servicesViewModel.setSelectedItem(it)
                                        onNavigateUpServiceDetailedScreen()
                                    },
                                    { service, ownerId ->
                                        viewModel.setSelectedServiceOwnerServiceItem(service)
                                        servicesViewModel.setSelectedItem(service)
                                        onNavigateUpServiceOwnerProfile(ownerId)
                                    },
                                    {
                                        showChooseIndustriesSheet()
                                    },
                                    onPopBackStack,
                                    viewModel,
                                    servicesViewModel
                                )
                            }, secondsContent = {
                                SecondsScreen(
                                    navController,
                                    {
                                        viewModel.setSelectedUsedProductListingItem(it)
                                        secondsViewModel.setSelectedItem(it)
                                        onNavigateUpUsedProductListingDetailedScreen()
                                    },
                                    {},
                                    viewModel,
                                    secondsViewModel
                                )

                            }, {
                                if (it == 1) {
                                    onDockedFabAddNewSecondsChanged(true)
                                } else {
                                    onDockedFabAddNewSecondsChanged(false)
                                }
                            })


                    } else {

                        if (nestedType == "Services") {
                            ServiceScreen(
                                navController,
                                {
                                    viewModel.setSelectedServiceItem(it)
                                    servicesViewModel.setSelectedItem(it)
                                    onNavigateUpServiceDetailedScreen()
                                },
                                { service, ownerId ->
                                    viewModel.setSelectedServiceOwnerServiceItem(service)
                                    servicesViewModel.setSelectedItem(service)
                                    onNavigateUpServiceOwnerProfile(ownerId)
                                },
                                {
                                    showChooseIndustriesSheet()
                                },
                                onPopBackStack,
                                viewModel,
                                servicesViewModel
                            )
                        }

                        if (nestedType == "Second Hands") {
                            SecondsScreen(
                                navController,
                                {
                                    viewModel.setSelectedUsedProductListingItem(it)
                                    secondsViewModel.setSelectedItem(it)
                                    onNavigateUpUsedProductListingDetailedScreen()
                                },
                                {},
                                viewModel,
                                secondsViewModel
                            )
                        }

                    }

                    if (isSearching) {


                        Box(
                            modifier = Modifier
                                .fillMaxSize() // This makes the Box take up the entire available space
                                .background(MaterialTheme.colorScheme.surface)
                        ) {


                            if (isLazyLoading) {
                                CircularProgressIndicatorLegacy(
                                    modifier = Modifier
                                        .align(Alignment.Center),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {

                                LazyColumn(modifier = Modifier.fillMaxSize()) {

                                    items(suggestions) {
                                        // Content inside the card with padding
                                        Column {

                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        viewModel.setSearchQuery(it)
                                                        if (it.isNotEmpty()) {

                                                            if (boards[pagerState.currentPage] == "Services") {
                                                                viewModel.navigateToOverlay(
                                                                    navController,
                                                                    BottomBar.NestedServices(
                                                                        servicesViewModel.getKey() + 1,
                                                                        it,
                                                                        true

                                                                    )
                                                                )
                                                            } else if (boards[pagerState.currentPage] == "Second Hands") {
                                                                viewModel.navigateToOverlay(
                                                                    navController,
                                                                    BottomBar.NestedSeconds(
                                                                        servicesViewModel.getKey() + 1,
                                                                        it,
                                                                        true

                                                                    )
                                                                )

                                                            }

                                                            scope.launch {
                                                                delay(100)
                                                                focusManager.clearFocus()
                                                            }
                                                        }
                                                    }
                                                    .padding(
                                                        16.dp,
                                                        vertical = 8.dp
                                                    ), // Padding inside the card,

                                                verticalAlignment = Alignment.CenterVertically // Align items vertically

                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Search,
                                                    contentDescription = null
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = it,
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                            }

                                        }

                                    }

                                }
                            }


                        }

                    }

                }

            }

            if (modalBottomSheetState.currentValue == SheetValue.Expanded) {
                ModalBottomSheet(
                    {
                        coroutineScope.launch {
                            modalBottomSheetState.hide()
                        }
                    },
                    dragHandle = null,
                    sheetGesturesEnabled = false,
                    shape = RectangleShape,
                    sheetState = modalBottomSheetState,
                    modifier = Modifier.safeDrawingPadding(),
                    properties = ModalBottomSheetProperties(shouldDismissOnBackPress = false)

                ) {

                    BackHandler(modalBottomSheetState.currentValue == SheetValue.Expanded) {

                        if (bottomSheetNavController.previousBackStackEntry == null) {
                            coroutineScope.launch {
                                modalBottomSheetState.hide()
                            }
                        }
                    }


                    if (signInMethod == "guest") {

                        GuestUserLocationAccessBottomSheetScreen(
                            modalBottomSheetState.currentValue,
                            { currentLocation ->
                                viewModel.setGuestCurrentLocation(
                                    userId, currentLocation
                                )
                                Toast.makeText(
                                    context,
                                    "Location updated successfully",
                                    Toast.LENGTH_SHORT
                                ).show()

                            },
                            { recentLocation ->
                                viewModel.setGuestRecentLocation(
                                    userId, recentLocation
                                )
                                Toast.makeText(
                                    context,
                                    "Location updated successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            { district, callback ->
                                viewModel.setGuestCurrentLocation(
                                    userId, CurrentLocation(
                                        district.coordinates.latitude,
                                        district.coordinates.longitude,
                                        district.district,
                                        "approximate"
                                    )
                                )
                                Toast.makeText(
                                    context,
                                    "Location updated successfully",
                                    Toast.LENGTH_SHORT
                                ).show()

                                callback()

                            },
                            {
                                coroutineScope.launch {
                                    modalBottomSheetState.hide()
                                }
                            },
                            viewModel,
                            true
                        )


                    } else {

                        UserLocationBottomSheetScreen(
                            bottomSheetNavController,
                            modalBottomSheetState.currentValue,
                            { currentLocation ->
                                viewModel.setCurrentLocation(currentLocation, {
                                    Toast.makeText(context, it, Toast.LENGTH_SHORT)
                                        .show()
                                    servicesViewModel.updateLastLoadedItemPosition(-1)
                                    servicesViewModel.refresh(userId, searchQuery.text)
                                    coroutineScope.launch {
                                        modalBottomSheetState.hide()
                                    }
                                }) {
                                    Toast
                                        .makeText(context, it, Toast.LENGTH_SHORT)
                                        .show()
                                }
                            },
                            { recentLocation ->
                                viewModel.setRecentLocation(recentLocation, {
                                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                                    servicesViewModel.updateLastLoadedItemPosition(-1)
                                    servicesViewModel.refresh(userId, searchQuery.text)
                                    coroutineScope.launch {
                                        modalBottomSheetState.expand()
                                    }
                                }) {
                                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                                }
                            },
                            { district, callback ->
                                viewModel.setCurrentLocation(
                                    CurrentLocation(
                                        district.coordinates.latitude,
                                        district.coordinates.longitude,
                                        district.district,
                                        "approximate"
                                    ), {
                                        callback()
                                        Toast.makeText(context, it, Toast.LENGTH_SHORT)
                                            .show()
                                        servicesViewModel.updateLastLoadedItemPosition(-1)
                                        servicesViewModel.refresh(userId, searchQuery.text)
                                        coroutineScope.launch {
                                            modalBottomSheetState.expand()
                                        }
                                    }) {
                                    Toast
                                        .makeText(context, it, Toast.LENGTH_SHORT)
                                        .show()
                                }
                            },
                            {
                                coroutineScope.launch {
                                    modalBottomSheetState.hide()
                                }
                            },
                            isLoading = isLoading,
                            homeViewModel = viewModel
                        )

                    }
                }

            }

        }
    }



}














