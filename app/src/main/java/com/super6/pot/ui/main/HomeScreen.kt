package com.super6.pot.ui.main

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation.NavController

import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.error
import coil3.request.placeholder
import coil3.size.Size
import com.super6.pot.R
import com.super6.pot.ui.ShimmerBox
import com.super6.pot.ui.dropUnlessResumedV2
import com.super6.pot.ui.formatCurrency
import com.super6.pot.ui.main.models.CurrentLocation
import com.super6.pot.ui.main.viewmodels.HomeViewModel
import com.super6.pot.ui.theme.customColorScheme
import com.super6.pot.ui.managers.NetworkConnectivityManager
import com.super6.pot.ui.viewmodels.ServicesViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    onNavigateUpServiceDetailedScreen: () -> Unit,
    onNavigateUpServiceOwnerProfile: (Long) -> Unit,
    showChooseIndustriesSheet: () -> Unit,
    onPopBackStack:() -> Unit,
    viewModel: HomeViewModel,
    servicesViewModel: ServicesViewModel
) {


    val userId = viewModel.userId
    val signInMethod = viewModel.signInMethod
    val selectedLocationGeo by viewModel.selectedLocationGeo.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val topBarBarHeight = remember { mutableFloatStateOf(0f) }
    val topBarOffsetHeightPx = remember { mutableFloatStateOf(0f) }
    val showTopBar = remember { mutableStateOf(true) }

    val searchQuery by servicesViewModel.searchQuery.collectAsState()


    val nestedScrollConnection = remember {

        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                val newOffset = topBarOffsetHeightPx.floatValue + delta
                topBarOffsetHeightPx.floatValue =
                    newOffset.coerceIn(-topBarBarHeight.floatValue, 0f)
                showTopBar.value = newOffset >= 0f
                return Offset.Zero
            }
        }

    }


    val coroutineScope = rememberCoroutineScope()

    val bottomSheetScaffoldState = androidx.compose.material.rememberBottomSheetScaffoldState()

    BackHandler(bottomSheetScaffoldState.bottomSheetState.currentValue == BottomSheetValue.Expanded) {
        coroutineScope.launch {
            bottomSheetScaffoldState.bottomSheetState.collapse()
        }
    }


    val context = LocalContext.current





//    val allowedScreens = listOf(
//        HomeDetail::class,
//    )
//
//    // Step 1: Get the current route from NavController
//    val currentRoute = homeNavController.currentBackStackEntryAsState().value?.destination?.route
//
//
//    // Step 2: Get the list of allowed screens' qualified names
//    val allowedRoutes = allowedScreens.map { it.qualifiedName.orEmpty() }
//
//    // Step 3: Clean the current route (remove path and query parameters)
//    val cleanedRoute = currentRoute
//        ?.replace(Regex("/\\{[^}]+\\}"), "") // Remove path parameters
//        ?.replace(Regex("\\?.*"), "")?.trim() // Remove query parameters and trim whitespace
//
//    val isAllowedHeader = cleanedRoute != null && allowedRoutes.contains(cleanedRoute)
//

//    var headerVisibility by rememberSaveable { mutableStateOf(false) }
//
//
//    LaunchedEffect(isAllowedHeader) {
//        headerVisibility=isAllowedHeader
//    }


    androidx.compose.material.BottomSheetScaffold(
        scaffoldState = bottomSheetScaffoldState,
        sheetContent = {

            if (signInMethod == "guest") {

                GuestUserLocationAccessBottomSheetScreen(
                    bottomSheetScaffoldState.bottomSheetState.currentValue,
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
                            bottomSheetScaffoldState.bottomSheetState.collapse()
                        }
                    },
                    viewModel,
                    true
                )


            } else {

                UserLocationBottomSheetScreen(
                    bottomSheetScaffoldState.bottomSheetState.currentValue,
                    { currentLocation ->
                        viewModel.setCurrentLocation(currentLocation, {
                            Toast.makeText(context, it, Toast.LENGTH_SHORT)
                                .show()
                            servicesViewModel.updateLastLoadedItemPosition(-1)
                            servicesViewModel.refresh(userId, searchQuery.text)
                            coroutineScope.launch {
                                bottomSheetScaffoldState.bottomSheetState.collapse()
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
                                bottomSheetScaffoldState.bottomSheetState.collapse()
                            }
                        }) {
                            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                        }
                    },
                    { district, callback ->
                        viewModel.setCurrentLocation(CurrentLocation(
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
                                bottomSheetScaffoldState.bottomSheetState.collapse()
                            }
                        }) {
                            Toast
                                .makeText(context, it, Toast.LENGTH_SHORT)
                                .show()
                        }
                    },
                    {
                        coroutineScope.launch {
                            bottomSheetScaffoldState.bottomSheetState.collapse()
                        }
                    },
                    isLoading = isLoading,
                    homeViewModel = viewModel
                )

            }


        },

        sheetPeekHeight = 0.dp, // Default height when sheet is collapsed
        sheetGesturesEnabled = false, // Allow gestures to hide/show bottom sheet
//            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    ) { _ ->


        Scaffold(
            modifier = Modifier.nestedScroll(nestedScrollConnection),
            topBar = {


                AnimatedVisibility(
                    visible = showTopBar.value,
                    enter = fadeIn(),  // Optional fade-in for showing
                    exit = fadeOut()   // Optional fade-out for hiding
                ) {

                    TopAppBar(
                        modifier = Modifier
                            .onGloballyPositioned { coordinates ->
                                topBarBarHeight.floatValue = coordinates.size.height.toFloat()
                            },
//                    scrollBehavior = scrollBehavior,
                        title = {
                            Row(
                                modifier = Modifier.padding(end = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    "Super6", fontSize = 22.sp, fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(vertical = 16.dp)
                                )
                                LocationWrapper(location = selectedLocationGeo ?: null) {
                                    coroutineScope.launch {
                                        bottomSheetScaffoldState.bottomSheetState.expand()
                                    }
                                }
                            }
                        })
                }


            }) { nestedInnerPadding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(nestedInnerPadding)
            ) {


                HomeContent(
                    navController,
                    { onNavigateUpServiceDetailedScreen() },
                    { onNavigateUpServiceOwnerProfile(it) },
                    {
                        showChooseIndustriesSheet()
                    },
                    onPopBackStack,
                    servicesViewModel)


            }

        }

    }

}





@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeContent(
    navController: NavController,
    onNavigateUpServiceDetailedScreen: () -> Unit,
    onNavigateUpServiceOwnerProfile: (Long) -> Unit,
    showChooseIndustriesSheet: () -> Unit,
    onPopBackStack:() -> Unit,
    viewModel: ServicesViewModel
) {


    val userId = viewModel.userId

    val onlySearchBar = viewModel.onlySearchBar

    val context = LocalContext.current

    // Collect state from ViewModel
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchOldQuery by viewModel.searchOldQuery.collectAsState()

    val isSearching by viewModel.isSearching.collectAsState()
//    val serviceItems by viewModel.serviceItems.collectAsState()
//    val isLoading by viewModel.isLoading.collectAsState()
    val isLazyLoading by viewModel.isLazyLoading.collectAsState()
//    val errorMessage by viewModel.errorMessage.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()
    val selectedItem by viewModel.selectedItem.collectAsState()


    val searchJob by viewModel.searchJob.collectAsState()

    // Initialize the ModalBottomSheetState with default value

    val sheetState = rememberModalBottomSheetState()

    var bottomSheetState by rememberSaveable { mutableStateOf(false) }

    // Get CoroutineScope
    val scope = rememberCoroutineScope()

//    val lazyPagingServices: LazyPagingItems<Service> = viewModel.services.collectAsLazyPagingItems()

//    val cachedData by viewModel.cachedData.collectAsState() // Your cached data state


//    val items: List<Service> = cachedData

    val focusManager = LocalFocusManager.current



    if (!onlySearchBar) {
        BackHandler(isSearching) {
            // Handle search collapse on the main screen
            if (isSearching) {
                viewModel.collapseSearchAction()
            }
        }
    }

    if (onlySearchBar) {
        BackHandler(isSearching){
            // Handle search collapse or pop overlay if no search
            if (isSearching) {
                viewModel.collapseSearchAction()
                scope.launch {
                    delay(100)
                    focusManager.clearFocus()
                }
            }
        }
    }


    val initialLoadState by viewModel.pageSource.initialLoadState.collectAsState()
    val isLoadingItems by viewModel.pageSource.isLoadingItems.collectAsState()
    val isRefreshingItems by viewModel.pageSource.isRefreshingItems.collectAsState()
    val isLoadingCache by viewModel.isLoadingCache.collectAsState()

    val items by viewModel.pageSource.items.collectAsState()

    val hasNetworkError by viewModel.pageSource.hasNetworkError.collectAsState()
    val hasAppendError by viewModel.pageSource.hasAppendError.collectAsState()
    val hasMoreItems by viewModel.pageSource.hasMoreItems.collectAsState()

    val industriesCount by viewModel.pageSource.industriesCount.collectAsState()

    val isValidSignInMethodFeaturesEnabled = viewModel.isValidSignInMethodFeaturesEnabled


    if (isValidSignInMethodFeaturesEnabled) {
        LaunchedEffect(industriesCount) {
            if (industriesCount == 0) {
                showChooseIndustriesSheet()
            }
        }

    }

    LaunchedEffect(bottomSheetState) {

        if (bottomSheetState) {
            sheetState.expand()
        } else {
            sheetState.hide()
        }
    }


    val lazyListState = rememberLazyListState()


    val lastLoadedItemPosition by viewModel.lastLoadedItemPosition.collectAsState()


    LaunchedEffect(lazyListState) {
        snapshotFlow { lazyListState.layoutInfo }
            .collect { layoutInfo ->

                // Check if the last item is visible
                val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index


                if (lastVisibleItemIndex != null
                    && lastVisibleItemIndex == items.size - 1
                    && !hasAppendError
                    && hasMoreItems
                    && !isLoadingItems
//                    && !isLoading
                    && lastVisibleItemIndex >= lastLoadedItemPosition
                ) {
                    viewModel.updateLastLoadedItemPosition(lastVisibleItemIndex)
                    // Call nextPage if last item is visible and not currently loading
                    viewModel.nextPage(
                        userId,
                        searchQuery.text) // Make sure to pass necessary parameters
                }
            }
    }


    val connectivityManager = viewModel.connectivityManager


    val statusCallback: (NetworkConnectivityManager.STATUS) -> Unit = {
        when (it) {
            NetworkConnectivityManager.STATUS.STATUS_CONNECTED -> {
                viewModel.updateLastLoadedItemPosition(-1)
                viewModel.refresh(userId, searchQuery.text)

            }

            NetworkConnectivityManager.STATUS.STATUS_NOT_CONNECTED_INITIALLY -> {
                viewModel.pageSource.setNetWorkError(true)
            }

            NetworkConnectivityManager.STATUS.STATUS_NOT_CONNECTED_ON_COMPLETED_JOB -> {
                viewModel.pageSource.setNetWorkError(true)
                viewModel.pageSource.setRefreshingItems(false)
                Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }


    val onRefresh: () -> Unit = {

        viewModel.pageSource.setRefreshingItems(true)
        connectivityManager.checkForSeconds(
            Handler(Looper.getMainLooper()), statusCallback,
            4000
        )

    }


    val onRetry = {
        viewModel.pageSource.setRefreshingItems(true)
        connectivityManager.checkForSeconds(
            Handler(Looper.getMainLooper()), statusCallback,
            4000
        )
    }


    val pullToRefreshState = rememberPullToRefreshState()


    Box {


        Scaffold(
            topBar = {
                Column {

                    if (onlySearchBar) {

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .background(Color.Transparent),
                            shadowElevation = 0.dp) {

                            SearchBar(
                                query = searchQuery,
                                onQueryChange = { query ->

                                    if (query.text.trim().isNotEmpty()) {

                                        if (viewModel.searchQuery.value.text != query.text

                                        ) {

                                            viewModel.setSearching(true)
                                            viewModel.setSearchQuery(query.text)
                                            // Cancel any ongoing search job
                                            searchJob?.cancel()
                                            viewModel.clearJob()
                                            viewModel.onGetSearchQuerySuggestions(
                                                userId,
                                                query.text
                                            )

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
                                        viewModel.navigateToOverlay(navController)
                                    }

                                },
                                onBackButtonClicked = {

                                    if(!isSearching){
                                      onPopBackStack()
                                    }else{
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
                                .fillMaxWidth()
                                .height(64.dp),
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
                                            viewModel.onGetSearchQuerySuggestions(
                                                userId,
                                                query.text
                                            )

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
                                            viewModel.navigateToOverlay(navController)
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

                }
            },

            content = { contentPadding ->


                Surface(
                    modifier = Modifier
                        .fillMaxSize() // This makes the Box take up the entire available space
                        .padding(contentPadding)
                ) {

                    if ( /*(isLoadingCache) ||*/ initialLoadState && items.isEmpty()) {
                        LazyColumn(
                            content = {
                                if (onlySearchBar) {
                                    item {
                                        Box(modifier = Modifier.fillParentMaxSize()) {
                                            androidx.compose.material.CircularProgressIndicator(
                                                modifier = Modifier
                                                    .padding(16.dp)
                                                    .align(Alignment.Center),
                                                color = MaterialTheme.colorScheme.primary

                                            )
                                        }
                                    }
                                } else {
                                    items(3) {
                                        ShimmerServiceCard()
                                    }
                                }
                            },
                            contentPadding = PaddingValues(vertical = 8.dp, horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp), // Adjust the space between items
                            modifier = Modifier.fillMaxSize()
                        )


                    }
                    else {

                        PullToRefreshBox(
                            state = pullToRefreshState,
                            modifier = Modifier.fillMaxSize(),
                            isRefreshing = isRefreshingItems,
                            onRefresh = onRefresh
                        ) {

                            LazyColumn(
                                state = lazyListState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(vertical = 8.dp, horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp) // Adjust the space between items

                            ) {
/*                                (lazyPagingServices.loadState.refresh as? LoadState.Error)?.error?.let { error ->
                                    mapExceptionToError(error)
                                } is Error.NoInternet */

                                // Handle no internet case
                                if (hasNetworkError) {
                                    item {
                                        Box(modifier = Modifier.fillParentMaxSize()) {
                                            NoInternetScreen(modifier = Modifier.align(Alignment.Center)) {
                                                onRetry()
                                            }
                                        }
                                    }
                                }

//                                lazyPagingServices.loadState.refresh !is LoadState.Loading && lazyPagingServices.itemCount == 0
                                // Handle empty state after loading
                                else if (!isLoadingItems && !hasAppendError && items.isEmpty()) {
                                    item {
                                        Box(modifier = Modifier.fillParentMaxSize()) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier.align(Alignment.Center)
                                            ) {
                                                Image(
                                                    painter = painterResource(R.drawable.all_caught_up),
                                                    contentDescription = "Image from drawable",
                                                    modifier = Modifier.sizeIn(
                                                        maxWidth = 200.dp,
                                                        maxHeight = 200.dp
                                                    )
                                                )
                                                Spacer(Modifier.height(16.dp))
                                                Text(text = "Oops, nothing to catch")
                                            }
                                        }

                                    }
                                }
                                // Handle loaded items
                                else {

                                    /*                                    val itemCount = lazyPagingServices.itemCount
                                                                        items(
                                                                            count = if (itemCount > 0) itemCount else items.size,
                                                                            contentType = if (itemCount > 0) {
                                                                                lazyPagingServices.itemContentType { "ServiceItems" }
                                                                            } else {
                                                                                { "ServiceItems" }
                                                                            }
                                                                        ) { index ->
                                                                            // Access the item based on the current state
                                                                            val item = if (itemCount > 0) {
                                                                                lazyPagingServices[index]!!
                                                                            } else {
                                                                                items[index] // Fallback to cached data
                                                                            }*/

                                    items(items) { item ->


                                        ServiceCard(
                                            onItemClick = {
                                                viewModel.setSelectedItem(item)
                                                onNavigateUpServiceDetailedScreen()
                                            },
                                            onItemOptionClick = {
                                                viewModel.setSelectedItem(item)
                                                bottomSheetState = true
                                            },
                                            onItemProfileClick = {
                                                viewModel.setSelectedItem(item)
                                                onNavigateUpServiceOwnerProfile(item.user.userId)
                                            },
                                            location = item.location?.geo,
                                            distance = item.distance?.let {
                                                if (it > 1.0) {
                                                    "Nearly ${viewModel.formatDistance(it)} away from you"
                                                } else {
                                                    "Nearly close to you"
                                                }
                                            },
                                            userName = "${item.user.firstName} ${item.user.lastName ?: ""}",
                                            profileImageUrl = item.user.profilePicUrl,
                                            imageWidth = item.thumbnail?.width ?: 0,
                                            imageHeight = item.thumbnail?.height ?: 0,
                                            isUserOnline = item.user.isOnline,
                                            serviceThumbnailUrl = item.thumbnail?.imageUrl,
                                            serviceTitle = item.title,
                                            serviceDescription = item.shortDescription,
                                            startingPrice = formatCurrency(
                                                item.plans[0].planPrice.toDouble(),
                                                item.plans[0].planPriceUnit)
                                        )
                                    }

                                    // Loading indicator for appending more items
                                    if (isLoadingItems) {
                                        item {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.Center,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {


                                                androidx.compose.material.CircularProgressIndicator(
                                                    modifier = Modifier.padding(
                                                        16.dp
                                                    ),
                                                    color = MaterialTheme.colorScheme.primary

                                                )
                                            }
                                        }
                                    }

                                    // Handle errors for appending items
                                    if (hasAppendError) {
                                        item {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.Center,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {

                                                Text("Unable to load more.")

                                                Text(
                                                    "Retry",
                                                    color = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier
                                                        .clickable {
                                                            viewModel.retry(
                                                                userId,
                                                                searchQuery.text
                                                            )
                                                        }

                                                )
                                            }
                                        }
                                    }


                                    if (!hasMoreItems) {
                                        item {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.Center,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    "You have reached the end",
                                                    modifier = Modifier.padding(8.dp)
                                                )
                                            }
                                        }
                                    }

                                }

                            }

//                            PullRefreshIndicator(lazyPagingServices.loadState.refresh is LoadState.Loading,
//                                pullToRefreshState, Modifier.align(Alignment.TopCenter))
                        }


                    }


                    if (isSearching) {


                        Box(
                            modifier = Modifier
                                .fillMaxSize() // This makes the Box take up the entire available space
                                .background(MaterialTheme.colorScheme.surface)
                        ) {


                            if (isLazyLoading) {
                                androidx.compose.material.CircularProgressIndicator(
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
                                                            viewModel.navigateToOverlay(
                                                                navController
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


                    if (bottomSheetState) {
                        ModalBottomSheet(
                            modifier = Modifier
                                .safeDrawingPadding(),
                            onDismissRequest = {
                                bottomSheetState = false
                            },
                            shape = RectangleShape, // Set shape to square (rectangle)
                            sheetState = sheetState,
                            dragHandle = null // Remove the drag handle

                        ) {


                            // Sheet content
                            // Bookmark Section
                            selectedItem?.let { nonNullSelectedItem ->

                                viewModel.setSelectedItem(
                                    nonNullSelectedItem.copy(isBookmarked = nonNullSelectedItem.isBookmarked)
                                )

                                Column(modifier = Modifier.fillMaxWidth()) {


                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                if (nonNullSelectedItem.isBookmarked) {
                                                    viewModel.setSelectedItem(
                                                        nonNullSelectedItem.copy(
                                                            isBookmarked = false
                                                        )
                                                    )

                                                    viewModel.onRemoveBookmark(
                                                        viewModel.userId,
                                                        nonNullSelectedItem, onSuccess = {
                                                            viewModel.setSelectedItem(
                                                                nonNullSelectedItem.copy(
                                                                    isBookmarked = false
                                                                )
                                                            )
                                                            viewModel.directUpdateServiceIsBookMarked(
                                                                nonNullSelectedItem.serviceId,
                                                                false
                                                            )

                                                            Toast
                                                                .makeText(
                                                                    context,
                                                                    "Bookmark removed",
                                                                    Toast.LENGTH_SHORT
                                                                )
                                                                .show()

                                                        }, onError = {
                                                            Toast
                                                                .makeText(
                                                                    context,
                                                                    "Something wrong",
                                                                    Toast.LENGTH_SHORT
                                                                )
                                                                .show()

                                                            viewModel.setSelectedItem(
                                                                nonNullSelectedItem.copy(
                                                                    isBookmarked = true
                                                                )
                                                            )

                                                        })


                                                } else {

                                                    viewModel.setSelectedItem(
                                                        selectedItem?.copy(
                                                            isBookmarked = true
                                                        )
                                                    )

                                                    viewModel.onBookmark(
                                                        viewModel.userId,
                                                        nonNullSelectedItem,
                                                        onSuccess = {


                                                            viewModel.setSelectedItem(
                                                                nonNullSelectedItem.copy(
                                                                    isBookmarked = true
                                                                )
                                                            )

                                                            viewModel.directUpdateServiceIsBookMarked(
                                                                nonNullSelectedItem.serviceId,
                                                                true
                                                            )


                                                            Toast
                                                                .makeText(
                                                                    context,
                                                                    "Bookmarked",
                                                                    Toast.LENGTH_SHORT
                                                                )
                                                                .show()

                                                        },
                                                        onError = {
                                                            Toast
                                                                .makeText(
                                                                    context,
                                                                    "Something wrong",
                                                                    Toast.LENGTH_SHORT
                                                                )
                                                                .show()

                                                            viewModel.setSelectedItem(
                                                                nonNullSelectedItem.copy(
                                                                    isBookmarked = false
                                                                )
                                                            )
                                                        })
                                                }

                                            }
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {


                                        // Bookmark Icon
                                        Icon(
                                            painter = if (nonNullSelectedItem.isBookmarked) painterResource(
                                                R.drawable.ic_bookmarked
                                            ) else painterResource(
                                                R.drawable.ic_bookmark
                                            ),
                                            contentDescription = "Bookmark",
                                            modifier = Modifier.size(24.dp),
                                            tint = Color.Unspecified
                                        )

                                        // Text
                                        Text(
                                            text = "Bookmark",
                                            modifier = Modifier.padding(horizontal = 4.dp),
                                        )
                                    }


                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedItem?.let {
                                                    try {

                                                        val shareIntent = Intent().apply {
                                                            action = Intent.ACTION_SEND
                                                            putExtra(
                                                                Intent.EXTRA_TEXT,
                                                                it.shortCode
                                                            )  // Text you want to share
                                                            type =
                                                                "text/plain"  // MIME type for text
                                                        }
                                                        // Start the share intent
                                                        context.startActivity(
                                                            Intent.createChooser(
                                                                shareIntent,
                                                                "Share via"
                                                            )
                                                        )
                                                    } catch (e: ActivityNotFoundException) {

                                                        Toast
                                                            .makeText(
                                                                context,
                                                                "No app to open",
                                                                Toast.LENGTH_SHORT
                                                            )
                                                            .show()
                                                    }

                                                }

                                            }
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {


                                        // Bookmark Icon
                                        Icon(
                                            Icons.Default.Share,
                                            contentDescription = "Share",
                                            modifier = Modifier.size(24.dp),
                                        )

                                        // Text
                                        Text(
                                            text = "Share",
                                            modifier = Modifier.padding(horizontal = 4.dp),
                                        )
                                    }
                                }

                            }


                        }
                    }
                }
                // Define the AnimatedNavHost
            }
        )
    }

}


@Composable
fun NoInternetScreen(modifier: Modifier = Modifier, tryAgain: () -> Unit) {

    Column(
        modifier = modifier.padding(16.dp), // Padding for the Column,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Image
        Image(
            painter = painterResource(R.drawable.offline), // Replace with your drawable resource
            contentDescription = "No internet",
            modifier = Modifier.sizeIn(
                maxWidth = 200.dp,
                maxHeight = 200.dp
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Title Text
        Text(
            text = "Internet Connection Lost",
            fontSize = 16.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Description Text
        Text(
            text = "Please check your internet connection and try again to proceed.",
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Retry Button
        Button(
            onClick = {
                tryAgain()
            },
            modifier = Modifier.align(Alignment.CenterHorizontally),

            ) {
            Text(
                "Try again", style = MaterialTheme.typography.bodyLarge
            )
        }
    }

}


@Composable
fun ServiceCard(
    location: String?,
    distance: String?,
    userName: String,
    profileImageUrl: String?,
    isUserOnline: Boolean,
    serviceThumbnailUrl: String?,
    imageWidth: Int,
    imageHeight: Int,
    serviceTitle: String,
    serviceDescription: String,
    startingPrice: String,
    onItemClick: () -> Unit,
    onItemOptionClick: () -> Unit,
    onItemProfileClick: () -> Unit,
    deleteBookmarkClicked: () -> Unit = {},
    isBookmarked: Boolean = true,
    enableBookmarkedServiceIcon: Boolean = false,
) {


    var bookMarkStatus by remember(isBookmarked) { mutableStateOf(isBookmarked) }

    val lifecycleOwner = LocalLifecycleOwner.current

    val context = LocalContext.current

    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(profileImageUrl) // Set the image URL
            .placeholder(R.drawable.user_placeholder) // Placeholder image
            .error(R.drawable.user_placeholder) // Error image in case of failure
            .build())



    Card(

        onClick = dropUnlessResumed {
            onItemClick()
        },
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(8.dp), // Remove rounded corners
        elevation = CardDefaults.cardElevation(2.dp),

        ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Location Info

            location?.let {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_location),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = location,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }


            distance?.let {
                Text(
                    text = it,
                    modifier = Modifier.padding(horizontal = 8.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }


            // Profile Bar
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple()
                        ) {
                            dropUnlessResumedV2(lifecycleOwner){
                                onItemProfileClick() }
                        }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = userName,
                        modifier = Modifier.padding(horizontal = 4.dp),
                        maxLines = 2,
                        style = MaterialTheme.typography.bodyMedium,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    Box(modifier = Modifier) {
                        Image(
                            painter = painter,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                        )

                        if (isUserOnline) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(Color.Green, CircleShape)
                                    .align(Alignment.BottomEnd)
                            )
                        }
                    }

                    IconButton(onClick = {
                        onItemOptionClick()
                    }, content = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_more_vertical_dots),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    })

                }
            }


            // Service Info Layout
            val context = LocalContext.current

            val imageRequest = ImageRequest.Builder(context)
                .size(Size.ORIGINAL)
                .data(serviceThumbnailUrl) // Use placeholder drawable if imageUrl is null
                .build()

            val screenWidth = LocalConfiguration.current.screenWidthDp.dp
            val aspectRatio = imageWidth.toFloat() / imageHeight.toFloat()

            AsyncImage(
                model = imageRequest,
                contentDescription = "Upload Image",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16 / 9f)
//                    .aspectRatio(if (imageHeight > 0) imageWidth.toFloat() / imageHeight.toFloat() else 1f) // Maintain aspect ratio
                    .background(MaterialTheme.customColorScheme.serviceSurfaceContainer)
            )


            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(
                    text = serviceTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = serviceDescription,
                    fontSize = 14.sp,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    if (enableBookmarkedServiceIcon) {

                        Icon(if (bookMarkStatus) painterResource(
                            R.drawable.ic_bookmarked
                        ) else painterResource(
                            R.drawable.ic_bookmark
                        ),
                            tint = Color.Unspecified,

                            contentDescription = null,
                            modifier = Modifier
                                .size(32.dp)
                                .clickable {
                                    bookMarkStatus = false
                                    deleteBookmarkClicked()
                                }
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,

                        ) {
                        Text(
                            text = "Starting from",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = startingPrice,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun ShimmerServiceCard(
    isBookmarked: Boolean = true,
    enableBookmarkedServiceIcon: Boolean = false,
) {

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(8.dp), // Remove rounded corners
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {

            Column(modifier = Modifier.fillMaxWidth()) {
                // Profile Bar
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple()
                    ) {}
                    .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    ShimmerBox {
                        Text(
                            text = "User username",
                            modifier = Modifier.padding(horizontal = 4.dp),
                            color = Color.Transparent,
                            maxLines = 2,
                            style = MaterialTheme.typography.bodyMedium,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    ShimmerBox(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                    ) {}

                    IconButton(
                        onClick = {
                        }, content = {
                            ShimmerBox(
                                modifier = Modifier
                                    .height(24.dp)
                                    .width(16.dp)
                            )
                        })
                }
            }



            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.78f)
            )


            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                ShimmerBox {
                    Text(
                        text = "Service title",
                        color = Color.Transparent,
                        style = MaterialTheme.typography.titleLarge,
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                ShimmerBox {
                    Text(
                        text = "The given service description will goes here.",
                        color = Color.Transparent,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    if (enableBookmarkedServiceIcon) {
                        /*     Icon(
                                 if (bookMarkStatus) painterResource(
                                     R.drawable.ic_bookmarked
                                 ) else painterResource(
                                     R.drawable.ic_bookmark
                                 ),
                                 tint = Color.Unspecified,
                                 contentDescription = null,
                                 modifier = Modifier
                                     .size(32.dp)
                             )*/
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,

                        ) {
                        ShimmerBox {
                            Text(
                                text = "Starting from",
                                color = Color.Transparent,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        ShimmerBox {
                            Text(
                                text = "0.00#",
                                color = Color.Transparent,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }

                    }
                }
            }
        }
    }
}

@Composable
fun SearchBar(
    query: TextFieldValue,
    onQueryChange: (TextFieldValue) -> Unit,
    onSearch: () -> Unit,
    onBackButtonClicked: () -> Unit,
    onClearClicked:()-> Unit,
    modifier: Modifier = Modifier,
    onFocus: (Boolean) -> Unit,
    focusRequesterEnabled: Boolean = true,
    isBackButtonEnabled: Boolean = true,
) {

    // Conditionally create and use FocusRequester
    val focusRequester = remember { FocusRequester() }


    // If focusRequester is enabled, request focus once component is rendered
    if (focusRequesterEnabled) {
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
    }


    var recentQuery by remember(query) { mutableStateOf(query) }

    val focusManager = LocalFocusManager.current

    val coroutineScope = rememberCoroutineScope()


    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        if (isBackButtonEnabled) {
            IconButton(modifier = Modifier.padding(0.dp),
                onClick = {
                    onBackButtonClicked()
                    coroutineScope.launch {
                        delay(100) // Optional: Delay to ensure state is collected
                        focusManager.clearFocus() // Clear focus after state update
                    }
                }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back Icon",
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
        }

        BasicTextField(
            value = recentQuery,
            onValueChange = { textState ->
                recentQuery = textState
                onQueryChange(recentQuery)
            },
            textStyle = TextStyle.Default.copy(color = MaterialTheme.colorScheme.onSurface),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    coroutineScope.launch {
                        onSearch()
                        delay(100) // Optional: Delay to ensure state is collected
                        focusManager.clearFocus() // Clear focus after state update
                    }
                }
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(MaterialTheme.customColorScheme.searchBarColor, CircleShape)
                .padding(8.dp)
                .height(28.dp)
                .focusRequester(focusRequester)
                .onFocusChanged { focusState ->
//                    onFocus(focusState.isFocused)
                },

            ) { innerTextField ->
            Row(modifier, verticalAlignment = Alignment.CenterVertically) {

                IconButton(onClick = {
                    coroutineScope.launch {
                        delay(100) // Optional: Delay to ensure state is collected
                        focusManager.clearFocus() // Clear focus after state update
                    }
                }) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search",
                        modifier = Modifier.size(24.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.CenterStart
                ) {

                    if (recentQuery.text.trim().isEmpty()) {
                        Text(
                            text = "Search",
                            style = LocalTextStyle.current.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                fontSize = 14.sp
                            )
                        )
                    }


                    innerTextField()
                }


                if (recentQuery.text.trim().isNotEmpty()) {

                    IconButton(onClick = {
                        onClearClicked()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            modifier = Modifier.size(24.dp)
                        )
                    }

                }


            }
        }

    }
}


@Composable
fun LocationWrapper(location: String?, onClick: () -> Unit) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
    ) {

        Box(
            modifier = Modifier
                .widthIn(max = 180.dp)
                .align(Alignment.CenterEnd), // Adjust according to your layout
            contentAlignment = Alignment.CenterEnd // Aligns children to the end

        ) {
            Row(
                modifier = Modifier
                    .clickable { onClick() }, // Set focusable to false
                verticalAlignment = Alignment.CenterVertically // Center contents vertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_location),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )

                if (location != null) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = location,
                        maxLines = 1,
                        style = MaterialTheme.typography.bodyMedium,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

            }
        }
    }


}