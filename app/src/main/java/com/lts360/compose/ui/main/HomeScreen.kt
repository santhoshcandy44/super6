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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.rememberNavBackStack
import com.lts360.app.database.models.app.Board
import com.lts360.compose.ui.common.CircularProgressIndicatorLegacy
import com.lts360.compose.ui.localjobs.LocalJobsScreen
import com.lts360.compose.ui.localjobs.LocalJobsViewmodel
import com.lts360.compose.ui.main.models.CurrentLocation
import com.lts360.compose.ui.main.navhosts.routes.BottomBar
import com.lts360.compose.ui.main.navhosts.routes.LocationSetUpRoutes
import com.lts360.compose.ui.main.viewmodels.HomeViewModel
import com.lts360.compose.ui.main.viewmodels.SecondsViewmodel
import com.lts360.compose.ui.services.ServicesScreen
import com.lts360.compose.ui.usedproducts.SecondsScreen
import com.lts360.compose.ui.viewmodels.ServicesViewModel
import com.lts360.libs.ui.ShortToast
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    backStack: NavBackStack,
    boardItems: List<Board>,
    onNavigateUpServiceDetailedScreen: () -> Unit,
    onNavigateUpServiceOwnerProfile: (Long) -> Unit,
    onNavigateUpUsedProductListingDetailedScreen: () -> Unit,
    onNavigateUpLocalJobDetailedScreen: () -> Unit,
    onPopBackStack: () -> Unit,
    viewModel: HomeViewModel,
    servicesViewModel: ServicesViewModel,
    secondsViewModel: SecondsViewmodel,
    localJobsViewModel: LocalJobsViewmodel,
    onDockedFabAddNewSecondsVisibility: (Boolean) -> Unit,
    onlySearchBar: Boolean = false
) {

    val boardLabels by remember { mutableStateOf(boardItems.map { it.boardLabel }) }

    var initialPageIndex by rememberSaveable { mutableIntStateOf(0) }

    val pagerState = rememberPagerState(initialPageIndex, pageCount = { boardItems.size })

    val userId = viewModel.userId
    val isGuest = viewModel.isGuest
    val selectedLocationGeo by viewModel.selectedLocationGeo.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val searchQuery by viewModel.searchQuery.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()
    val isLazyLoading by viewModel.isLazyLoading.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    if (isSearching) {
        BackHandler {
            viewModel.collapseSearchAction(true)
            if (onlySearchBar) {
                coroutineScope.launch {
                    delay(100)
                    focusManager.clearFocus()
                }
            }
        }
    }


    val modalBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val context = LocalContext.current

    var topBarBarHeight by remember { mutableFloatStateOf(0f) }
    var topBarOffsetHeightPx by remember { mutableFloatStateOf(0f) }
    var showTopBar by remember { mutableStateOf(true) }
    var initialServicesKey = 0
    var initialSecondsKey = 0
    var initialLocalJobsKey = 0

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
                enter = fadeIn(),
                exit = fadeOut()) {

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

            Surface(modifier = Modifier.fillMaxWidth(),
                shadowElevation = 0.dp) {

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
                                "local_jobs" -> { viewModel.onGetLocalJobSearchQuerySuggestions(userId, query) }
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
                            "services" -> BottomBar.NestedServices(servicesViewModel.getKey(initialServicesKey) + 1, searchQuery.text, true)
                            "second_hands" -> BottomBar.NestedSeconds(secondsViewModel.getKey(initialSecondsKey) + 1, searchQuery.text, true)
                            "local_jobs" ->  BottomBar.NestedLocalJobs(localJobsViewModel.getKey(initialLocalJobsKey) + 1, searchQuery.text, true)
                            else -> return
                        }

                        when (currentLabel) {
                            "services" ->  {

                                servicesViewModel.loadServiceRepository(
                                    searchQuery.text,
                                    true,
                                    servicesViewModel.getKey(initialServicesKey) + 1
                                )

                                servicesViewModel.loadServices(servicesViewModel.getKey(initialServicesKey) + 1)
                            }
                            "second_hands" ->{

                                secondsViewModel.loadSecondsRepository(
                                    searchQuery.text,
                                    true,
                                    secondsViewModel.getKey(initialServicesKey) + 1
                                )

                                secondsViewModel.loadSeconds(secondsViewModel.getKey(initialSecondsKey) + 1)
                            }
                            "local_jobs" ->  {
                                localJobsViewModel.loadLocalJobsRepository(
                                    searchQuery.text,
                                    true,
                                    localJobsViewModel.getKey(initialLocalJobsKey) + 1
                                )

                                localJobsViewModel.loadLocalJobs(localJobsViewModel.getKey(initialLocalJobsKey) + 1)
                            }
                            else -> return
                        }

                        viewModel.navigateToOverlay(backStack, destination)

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
                    isBackButtonEnabled = onlySearchBar
                )
            }



            Box(modifier = Modifier.fillMaxSize()) {


                val servicesContent: @Composable () -> Unit = {
                    ServicesScreen(
                        initialServicesKey,
                        showTopBar,
                        {
                            viewModel.setSelectedServiceItem(it)
                            servicesViewModel.setSelectedItem(initialServicesKey, it)
                            onNavigateUpServiceDetailedScreen()
                        },
                        { service, ownerId ->
                            viewModel.setSelectedServiceOwnerServiceItem(service)
                            servicesViewModel.setSelectedItem(initialServicesKey, service)
                            onNavigateUpServiceOwnerProfile(ownerId)
                        },
                       servicesViewModel
                    )
                }

                val secondsContent: @Composable () -> Unit = {
                    SecondsScreen(
                        initialSecondsKey,
                        showTopBar,
                        {
                            viewModel.setSelectedUsedProductListingItem(it)
                            secondsViewModel.setSelectedItem(initialSecondsKey, it)
                            onNavigateUpUsedProductListingDetailedScreen()
                        },
                        secondsViewModel
                    )
                }


                val localJobsContent: @Composable () -> Unit = {
                    LocalJobsScreen(
                        initialLocalJobsKey,
                        showTopBar,
                        {
                            viewModel.setSelectedLocalJobItem(it)
                            localJobsViewModel.setSelectedItem(initialLocalJobsKey, it)
                            onNavigateUpLocalJobDetailedScreen()
                        },
                        localJobsViewModel
                    )
                }

                Boards(
                    boards = boardItems,
                    pagerState = pagerState,
                    servicesContent = servicesContent,
                    secondsContent = secondsContent,
                    localJobsContent = localJobsContent,
                    onPageChanged = {
                        initialPageIndex =  boardLabels.indexOf(it).coerceAtLeast(0)
                        onDockedFabAddNewSecondsVisibility(it == "second_hands")
                    }
                )

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
                                    Column {

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    viewModel.setSearchQuery(it)
                                                    if (it.isNotEmpty()) {

                                                        if (boardLabels[pagerState.currentPage] == "services") {

                                                            servicesViewModel.loadServiceRepository(
                                                                it,
                                                                true,
                                                                servicesViewModel.getKey(initialServicesKey) + 1
                                                            )

                                                            servicesViewModel.loadServices(servicesViewModel.getKey(initialServicesKey) + 1)

                                                            viewModel.navigateToOverlay(
                                                                backStack,
                                                                BottomBar.NestedServices(
                                                                    servicesViewModel.getKey(initialServicesKey) + 1,
                                                                    it,
                                                                    true

                                                                )
                                                            )
                                                        } else if (boardLabels[pagerState.currentPage] == "second_hands") {


                                                            secondsViewModel.loadSecondsRepository(
                                                                it,
                                                                true,
                                                                secondsViewModel.getKey(initialSecondsKey) + 1
                                                            )

                                                            secondsViewModel.loadSeconds(secondsViewModel.getKey(initialSecondsKey) + 1)


                                                            viewModel.navigateToOverlay(
                                                                backStack,
                                                                BottomBar.NestedSeconds(
                                                                    servicesViewModel.getKey(initialServicesKey) + 1,
                                                                    it,
                                                                    true
                                                                )
                                                            )

                                                        }else if(boardLabels[pagerState.currentPage] == "local_jobs"){
                                                            localJobsViewModel.loadLocalJobsRepository(
                                                                it,
                                                                true,
                                                                localJobsViewModel.getKey(initialLocalJobsKey) + 1
                                                            )

                                                            localJobsViewModel.loadLocalJobs(localJobsViewModel.getKey(initialLocalJobsKey) + 1)


                                                            viewModel.navigateToOverlay(
                                                                backStack,
                                                                BottomBar.NestedLocalJobs(
                                                                    localJobsViewModel.getKey(initialLocalJobsKey) + 1,
                                                                    it,
                                                                    true
                                                                )
                                                            )
                                                        }

                                                        coroutineScope.launch {
                                                            delay(100)
                                                            focusManager.clearFocus()
                                                        }
                                                    }
                                                }
                                                .padding(
                                                    16.dp,
                                                    vertical = 8.dp
                                                ),

                                            verticalAlignment = Alignment.CenterVertically

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

        if (modalBottomSheetState.isVisible) {

            val backStack = rememberNavBackStack(LocationSetUpRoutes.LocationChooser())

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

                        if (backStack.isNotEmpty()) {
                            coroutineScope.launch {
                                modalBottomSheetState.hide()
                            }
                        }
                    }

                    fun reloadItems() {
                        if (boardLabels[pagerState.currentPage] == "services") {
                            servicesViewModel.updateLastLoadedItemPosition(initialServicesKey, -1)
                            servicesViewModel.refresh(initialServicesKey, userId, searchQuery.text)
                        }
                        if (boardLabels[pagerState.currentPage] == "second_hands") {
                            secondsViewModel.updateLastLoadedItemPosition(initialSecondsKey,-1)
                            secondsViewModel.refresh(initialSecondsKey, userId, searchQuery.text)
                        }
                        if(boardLabels[pagerState.currentPage] == "local_jobs"){
                            localJobsViewModel.updateLastLoadedItemPosition(initialLocalJobsKey, -1)
                            localJobsViewModel.refresh(initialLocalJobsKey, userId, searchQuery.text)
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
                            backStack,
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




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NestedServicesScreen(
    backStack: NavBackStack,
    key:Int,
    onNavigateUpServiceDetailedScreen: () -> Unit,
    onNavigateUpServiceOwnerProfile: (Long) -> Unit,
    onPopBackStack: () -> Unit,
    viewModel: HomeViewModel,
    servicesViewModel: ServicesViewModel,
    onlySearchBar: Boolean = false
) {


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
                enter = fadeIn(),
                exit = fadeOut()) {

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

            Surface(modifier = Modifier.fillMaxWidth(),
                shadowElevation = 0.dp) {

                fun handleQueryChange(query: String) {
                    if (query.trim().isNotEmpty()) {
                        if (viewModel.searchQuery.value.text != query) {
                            viewModel.setSearching(true)
                            viewModel.setSearchQuery(query)
                            viewModel.clearJob()
                            viewModel.onGetServiceSearchQuerySuggestions(userId, query)

                        }
                    } else if (viewModel.searchQuery.value.text != query && viewModel.isSearching.value) {
                        viewModel.setSearching(false)
                        viewModel.clearJob()
                        viewModel.setSearchQuery("")
                        viewModel.setSuggestions(emptyList())
                    }
                }

                fun navigateToResults() {
                    if (searchQuery.text.isNotEmpty()) {
                        servicesViewModel.loadServiceRepository(
                            searchQuery.text,
                            true,
                            servicesViewModel.getKey(key) + 1
                        )

                        servicesViewModel.loadServices(servicesViewModel.getKey(key) + 1)

                        val destination = BottomBar.NestedServices(servicesViewModel.getKey(key) + 1, searchQuery.text, true)
                        viewModel.navigateToOverlay(backStack, destination)
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
                    isBackButtonEnabled = onlySearchBar
                )
            }



            Box(modifier = Modifier.fillMaxSize()) {



                ServicesScreen(
                    key,
                    showTopBar,
                    {
                        viewModel.setSelectedServiceItem(it)
                        servicesViewModel.setSelectedItem(key, it)
                        onNavigateUpServiceDetailedScreen()
                    },
                    { service, ownerId ->
                        viewModel.setSelectedServiceOwnerServiceItem(service)
                        servicesViewModel.setSelectedItem(key, service)
                        onNavigateUpServiceOwnerProfile(ownerId)
                    },
                    servicesViewModel
                )

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
                                    Column {

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    viewModel.setSearchQuery(it)
                                                    if (it.isNotEmpty()) {

                                                        servicesViewModel.loadServiceRepository(
                                                            it,
                                                            true,
                                                            servicesViewModel.getKey(key) + 1
                                                        )

                                                        servicesViewModel.loadServices(servicesViewModel.getKey(key) + 1)

                                                        viewModel.navigateToOverlay(
                                                            backStack,
                                                            BottomBar.NestedServices(
                                                                servicesViewModel.getKey(key) + 1,
                                                                it,
                                                                true

                                                            )
                                                        )

                                                        scope.launch {
                                                            delay(100)
                                                            focusManager.clearFocus()
                                                        }
                                                    }
                                                }
                                                .padding(
                                                    16.dp,
                                                    vertical = 8.dp
                                                ),

                                            verticalAlignment = Alignment.CenterVertically

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

        if (modalBottomSheetState.isVisible) {

            val backStack = rememberNavBackStack(LocationSetUpRoutes.LocationChooser())

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

                        if (backStack.isEmpty()) {
                            coroutineScope.launch {
                                modalBottomSheetState.hide()
                            }
                        }
                    }

                    fun reloadItems() {
                        servicesViewModel.updateLastLoadedItemPosition(key, -1)
                        servicesViewModel.refresh(key, userId, searchQuery.text)
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
                            backStack,
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



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NestedSecondsScreen(
    backStack: NavBackStack,
    key: Int,
    onNavigateUpUsedProductListingDetailedScreen: () -> Unit,
    onPopBackStack: () -> Unit,
    viewModel: HomeViewModel,
    secondsViewModel: SecondsViewmodel,
    onlySearchBar: Boolean = false
) {

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
                enter = fadeIn(),
                exit = fadeOut()) {

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

            Surface(modifier = Modifier.fillMaxWidth(),
                shadowElevation = 0.dp) {

                fun handleQueryChange(query: String) {
                    if (query.trim().isNotEmpty()) {
                        if (viewModel.searchQuery.value.text != query) {
                            viewModel.setSearching(true)
                            viewModel.setSearchQuery(query)
                            viewModel.clearJob()
                            viewModel.onGetUsedProductListingSearchQuerySuggestions(userId, query)
                        }
                    } else if (viewModel.searchQuery.value.text != query && viewModel.isSearching.value) {
                        viewModel.setSearching(false)
                        viewModel.clearJob()
                        viewModel.setSearchQuery("")
                        viewModel.setSuggestions(emptyList())
                    }
                }

                fun navigateToResults() {
                    if (searchQuery.text.isNotEmpty()) {

                        secondsViewModel.loadSecondsRepository(
                            searchQuery.text,
                            true,
                            secondsViewModel.getKey(key) + 1
                        )

                        secondsViewModel.loadSeconds(secondsViewModel.getKey(key) + 1)

                        val destination = BottomBar.NestedSeconds(secondsViewModel.getKey(key) + 1, searchQuery.text, true)


                        viewModel.navigateToOverlay(backStack, destination)

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
                    isBackButtonEnabled = onlySearchBar
                )
            }



            Box(modifier = Modifier.fillMaxSize()) {

                SecondsScreen(
                    key,
                    showTopBar,
                    {
                        viewModel.setSelectedUsedProductListingItem(it)
                        secondsViewModel.setSelectedItem(key, it)
                        onNavigateUpUsedProductListingDetailedScreen()
                    },
                    secondsViewModel
                )


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
                                    Column {

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    viewModel.setSearchQuery(it)
                                                    if (it.isNotEmpty()) {

                                                        secondsViewModel.loadSecondsRepository(
                                                            it,
                                                            true,
                                                            secondsViewModel.getKey(key) + 1
                                                        )

                                                        secondsViewModel.loadSeconds(secondsViewModel.getKey(key) + 1)

                                                        viewModel.navigateToOverlay(
                                                            backStack,
                                                            BottomBar.NestedSeconds(
                                                                secondsViewModel.getKey(key) + 1,
                                                                it,
                                                                true
                                                            )
                                                        )

                                                        scope.launch {
                                                            delay(100)
                                                            focusManager.clearFocus()
                                                        }
                                                    }
                                                }
                                                .padding(
                                                    16.dp,
                                                    vertical = 8.dp
                                                ),

                                            verticalAlignment = Alignment.CenterVertically

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

        if (modalBottomSheetState.isVisible) {

            val backStack = rememberNavBackStack(LocationSetUpRoutes.LocationChooser())


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

                        if (backStack.isEmpty()) {
                            coroutineScope.launch {
                                modalBottomSheetState.hide()
                            }
                        }
                    }

                    fun reloadItems() {
                        secondsViewModel.updateLastLoadedItemPosition(key, -1)
                        secondsViewModel.refresh(key, userId, searchQuery.text)
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
                            backStack,
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NestedLocalJobsScreen(
    backStack: NavBackStack,
    key:Int,
    onNavigateUpLocalJobDetailedScreen: () -> Unit,
    onPopBackStack: () -> Unit,
    viewModel: HomeViewModel,
    localJobsViewModel: LocalJobsViewmodel,
    onlySearchBar: Boolean = false
) {

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
                enter = fadeIn(),
                exit = fadeOut()) {

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

            Surface(modifier = Modifier.fillMaxWidth(),
                shadowElevation = 0.dp) {

                fun handleQueryChange(query: String) {
                    if (query.trim().isNotEmpty()) {
                        if (viewModel.searchQuery.value.text != query) {
                            viewModel.setSearching(true)
                            viewModel.setSearchQuery(query)
                            viewModel.clearJob()

                            viewModel.onGetLocalJobSearchQuerySuggestions(userId, query)
                        }
                    } else if (viewModel.searchQuery.value.text != query && viewModel.isSearching.value) {
                        viewModel.setSearching(false)
                        viewModel.clearJob()
                        viewModel.setSearchQuery("")
                        viewModel.setSuggestions(emptyList())
                    }
                }

                fun navigateToResults() {
                    if (searchQuery.text.isNotEmpty()) {

                        localJobsViewModel.loadLocalJobsRepository(
                            searchQuery.text,
                            true,
                            localJobsViewModel.getKey(key) + 1
                        )

                        localJobsViewModel.loadLocalJobs(localJobsViewModel.getKey(key) + 1)

                        val destination =  BottomBar.NestedLocalJobs(localJobsViewModel.getKey(key) + 1, searchQuery.text, true)
                        viewModel.navigateToOverlay(backStack, destination)

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
                    isBackButtonEnabled = onlySearchBar
                )
            }



            Box(modifier = Modifier.fillMaxSize()) {

                LocalJobsScreen(
                    key,
                    showTopBar,
                    {
                        viewModel.setSelectedLocalJobItem(it)
                        localJobsViewModel.setSelectedItem(key, it)
                        onNavigateUpLocalJobDetailedScreen()
                    },
                    localJobsViewModel
                )

                if (isSearching) {
                    Box(modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface)) {

                        if (isLazyLoading) {
                            CircularProgressIndicatorLegacy(
                                modifier = Modifier
                                    .align(Alignment.Center),
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {

                                items(suggestions) {
                                    Column {

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    viewModel.setSearchQuery(it)
                                                    if (it.isNotEmpty()) {

                                                        localJobsViewModel.loadLocalJobsRepository(
                                                            searchQuery.text,
                                                            true,
                                                            localJobsViewModel.getKey(key) + 1
                                                        )

                                                        localJobsViewModel.loadLocalJobs(localJobsViewModel.getKey(key) + 1)
                                                        viewModel.navigateToOverlay(
                                                            backStack,
                                                            BottomBar.NestedSeconds(
                                                                localJobsViewModel.getKey(key) + 1,
                                                                it,
                                                                true
                                                            )
                                                        )

                                                        scope.launch {
                                                            delay(100)
                                                            focusManager.clearFocus()
                                                        }
                                                    }
                                                }
                                                .padding(
                                                    16.dp,
                                                    vertical = 8.dp
                                                ),

                                            verticalAlignment = Alignment.CenterVertically

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

        if (modalBottomSheetState.isVisible) {

            val backStack = rememberNavBackStack(LocationSetUpRoutes.LocationChooser())

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

                        if (backStack.isEmpty()) {
                            coroutineScope.launch {
                                modalBottomSheetState.hide()
                            }
                        }
                    }

                    fun reloadItems() {
                        localJobsViewModel.updateLastLoadedItemPosition(key, -1)
                        localJobsViewModel.refresh(key, userId, searchQuery.text)
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
                                    ShortToast(context, "Location updated successfully")
                                }
                            },
                            { recentLocation ->
                                viewModel.setGuestRecentLocation(
                                    userId, recentLocation
                                ) {
                                    reloadItems()
                                    ShortToast(context, "Location updated successfully")

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
                                    ShortToast(context, "Location updated successfully")
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
                            backStack,
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





