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
import androidx.compose.runtime.setValue
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
import com.lts360.app.database.models.app.Board
import com.lts360.compose.ui.common.CircularProgressIndicatorLegacy
import com.lts360.compose.ui.main.models.CurrentLocation
import com.lts360.compose.ui.main.navhosts.routes.BottomBar
import com.lts360.compose.ui.main.viewmodels.HomeViewModel
import com.lts360.compose.ui.main.viewmodels.SecondsViewmodel
import com.lts360.compose.ui.services.ServicesScreen
import com.lts360.compose.ui.usedproducts.SecondsScreen
import com.lts360.compose.ui.viewmodels.ServicesViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    boardItems: List<Board>,
    onNavigateUpServiceDetailedScreen: () -> Unit,
    onNavigateUpUsedProductListingDetailedScreen: () -> Unit,
    onNavigateUpServiceOwnerProfile: (Long) -> Unit,
    onPopBackStack: () -> Unit,
    viewModel: HomeViewModel,
    servicesViewModel: ServicesViewModel,
    secondsViewModel: SecondsViewmodel,
    onDockedFabAddNewSecondsVisibility: (Boolean) -> Unit,
    onlySearchBar: Boolean = false,
    nestedType: String? = null
) {

    val boardLabels by remember { mutableStateOf(boardItems.map { it.boardLabel }) }

    val initialPageIndex =
        if (nestedType.isNullOrEmpty()) 0 else boardLabels.indexOf(nestedType).coerceAtLeast(0)

    val pagerState = rememberPagerState(initialPageIndex, pageCount = { boardItems.size })

    val userId = viewModel.userId
    val isGuest = viewModel.isGuest
    val selectedLocationGeo by viewModel.selectedLocationGeo.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val searchQuery by viewModel.searchQuery.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()
    val isLazyLoading by viewModel.isLazyLoading.collectAsState()

    val focusManager = LocalFocusManager.current

    val scope = rememberCoroutineScope()

    if (isSearching) {
        BackHandler {
            viewModel.collapseSearchAction(true)

            if (onlySearchBar) {
                scope.launch {
                    delay(100)
                    focusManager.clearFocus()
                }
            }
        }
    }

    val coroutineScope = rememberCoroutineScope()

    val modalBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val context = LocalContext.current


    var topBarBarHeight by remember { mutableFloatStateOf(0f) }
    var topBarOffsetHeightPx by remember { mutableFloatStateOf(0f) }
    var showTopBar by remember { mutableStateOf(true) }

    Surface(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(remember {
                    object : NestedScrollConnection {
                        override fun onPreScroll(
                            available: Offset,
                            source: NestedScrollSource
                        ): Offset {
                            val newOffset = topBarOffsetHeightPx + available.y
                            topBarOffsetHeightPx = newOffset.coerceIn(-topBarBarHeight, 0f)
                            showTopBar = newOffset >= 0f
                            return Offset.Zero
                        }
                    }
                })
        ) {

            AnimatedVisibility(
                visible = showTopBar,
                enter = fadeIn(),  // Optional fade-in for showing
                exit = fadeOut()   // Optional fade-out for hiding
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { coordinates ->
                            topBarBarHeight = coordinates.size.height.toFloat()
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

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 0.dp
            ) {


                fun handleQueryChange(query: String) {
                    val currentLabel = boardLabels[pagerState.currentPage.coerceIn(0, boardItems.lastIndex)]
                    if (query.trim().isNotEmpty()) {
                        if (viewModel.searchQuery.value.text != query) {
                            viewModel.setSearching(true)
                            viewModel.setSearchQuery(query)
                            viewModel.clearJob()

                            when (currentLabel) {
                                "services" -> viewModel.onGetServiceSearchQuerySuggestions(userId, query)
                                "second_hands" -> viewModel.onGetUsedProductListingSearchQuerySuggestions(userId, query)
                            }
                        }
                    } else if (viewModel.searchQuery.value.text != query && viewModel.isSearching.value) {
                        viewModel.setSearching(false)
                        viewModel.clearJob()
                        viewModel.setSearchQuery("")
                        viewModel.setSuggestions(emptyList())
                    }
                }

                fun navigateToResults() {
                    val currentLabel = boardLabels[pagerState.currentPage.coerceIn(0, boardItems.lastIndex)]
                    if (searchQuery.text.isNotEmpty()) {
                        val destination = when (currentLabel) {
                            "services" -> BottomBar.NestedServices(servicesViewModel.getKey() + 1, searchQuery.text, true)
                            "second_hands" -> BottomBar.NestedSeconds(secondsViewModel.getKey() + 1, searchQuery.text, true)
                            else -> return
                        }

                        if (onlySearchBar) {
                            viewModel.navigateToOverlay(navController, destination)
                        } else {
                            scope.launch {
                                viewModel.navigateToOverlay(navController, destination)
                            }
                        }
                    }
                }

                SearchBar(
                    query = searchQuery,
                    onQueryChange = { handleQueryChange(it.text) },
                    onSearch = { navigateToResults() },
                    onBackButtonClicked = {
                        if (!isSearching) {
                            onPopBackStack()
                        } else {
                            viewModel.clearJob()
                            viewModel.setSuggestions(emptyList())
                            viewModel.setSearching(false)
                        }
                    },
                    onClearClicked = {
                        viewModel.clearJob()
                        viewModel.setSuggestions(emptyList())
                        viewModel.setSearchQuery("")
                        viewModel.setSearching(false)
                    },
                    isBackButtonEnabled = onlySearchBar // disable back button only for full screen
                )
            }



            Box(modifier = Modifier.fillMaxSize()) {


                val servicesContent: @Composable () -> Unit = {
                    ServicesScreen(
                        showTopBar,
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
                       servicesViewModel
                    )
                }

                val secondsContent: @Composable () -> Unit = {
                    SecondsScreen(
                        showTopBar,
                        {
                            viewModel.setSelectedUsedProductListingItem(it)
                            secondsViewModel.setSelectedItem(it)
                            onNavigateUpUsedProductListingDetailedScreen()
                        },
                        secondsViewModel
                    )
                }

                if (!onlySearchBar) {
                    Boards(
                        boards = boardItems,
                        pagerState = pagerState,
                        servicesContent = servicesContent,
                        secondsContent = secondsContent,
                        onPageChanged = {
                            onDockedFabAddNewSecondsVisibility(it == "second_hands")
                        }
                    )
                } else {
                    when (nestedType) {
                        "services" -> servicesContent()
                        "second_hands" -> secondsContent()
                    }
                }


                if (isSearching) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
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

                                                        if (boardLabels[pagerState.currentPage] == "services") {
                                                            viewModel.navigateToOverlay(
                                                                navController,
                                                                BottomBar.NestedServices(
                                                                    servicesViewModel.getKey() + 1,
                                                                    it,
                                                                    true

                                                                )
                                                            )
                                                        } else if (boardLabels[pagerState.currentPage] == "second_hands") {
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

            val bottomSheetNavController = rememberNavController()


            ModalBottomSheet(
                {
                    coroutineScope.launch {
                        modalBottomSheetState.hide()
                    }
                },
                sheetGesturesEnabled = false,
                dragHandle = null,
                shape = RectangleShape,
                sheetState = modalBottomSheetState,
                properties = ModalBottomSheetProperties(
                    shouldDismissOnBackPress = false,
                    isAppearanceLightStatusBars = false,
                    isAppearanceLightNavigationBars = false
                ),
                modifier = Modifier.safeDrawingPadding()
            ) {

                Box(modifier = Modifier.fillMaxSize()) {

                    BackHandler(modalBottomSheetState.currentValue == SheetValue.Expanded) {

                        if (bottomSheetNavController.previousBackStackEntry == null) {
                            coroutineScope.launch {
                                modalBottomSheetState.hide()
                            }
                        }
                    }


                    fun reloadItems() {
                        if (boardLabels[pagerState.currentPage] == "services") {
                            servicesViewModel.updateLastLoadedItemPosition(-1)
                            servicesViewModel.refresh(userId, searchQuery.text)
                        }
                        if (boardLabels[pagerState.currentPage] == "second_hands") {
                            secondsViewModel.updateLastLoadedItemPosition(-1)
                            secondsViewModel.refresh(userId, searchQuery.text)
                        }
                        coroutineScope.launch {
                            modalBottomSheetState.hide()
                        }
                    }

                    if (isGuest) {

                        GuestUserLocationAccessBottomSheetScreen(
                            modalBottomSheetState.currentValue,
                            { currentLocation ->
                                viewModel.setGuestCurrentLocation(userId, currentLocation) {
                                    reloadItems()
                                    Toast.makeText(
                                        context,
                                        "Location updated successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            { recentLocation ->
                                viewModel.setGuestRecentLocation(
                                    userId, recentLocation
                                ) {
                                    reloadItems()
                                    Toast.makeText(
                                        context,
                                        "Location updated successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                            },
                            { district ->

                                viewModel.setGuestCurrentLocation(
                                    userId, CurrentLocation(
                                        district.coordinates.latitude,
                                        district.coordinates.longitude,
                                        district.district,
                                        "approximate"
                                    )
                                ) {
                                    reloadItems()
                                    Toast.makeText(
                                        context,
                                        "Location updated successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

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
                                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                                    reloadItems()
                                }) {
                                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                                }
                            },
                            { recentLocation ->
                                viewModel.setRecentLocation(recentLocation, {
                                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                                    reloadItems()
                                }) {
                                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                                }
                            },
                            { district ->
                                viewModel.setCurrentLocation(
                                    CurrentLocation(
                                        district.coordinates.latitude,
                                        district.coordinates.longitude,
                                        district.district,
                                        "approximate"
                                    ), {
                                        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                                        reloadItems()
                                    }) {
                                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
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
