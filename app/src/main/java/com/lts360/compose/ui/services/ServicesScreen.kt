package com.lts360.compose.ui.services

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
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
import com.lts360.R
import com.lts360.api.models.service.Service
import com.lts360.compose.dropUnlessResumedV2
import com.lts360.compose.ui.ShimmerBox
import com.lts360.compose.ui.common.CircularProgressIndicatorLegacy
import com.lts360.compose.ui.main.common.NoInternetScreen
import com.lts360.compose.ui.main.viewmodels.HomeViewModel
import com.lts360.compose.ui.managers.NetworkConnectivityManager
import com.lts360.compose.ui.services.navhosts.ServiceReviewsNavHost
import com.lts360.compose.ui.theme.customColorScheme
import com.lts360.compose.ui.theme.icons
import com.lts360.compose.ui.utils.FormatterUtils.formatCurrency
import com.lts360.compose.ui.viewmodels.ServicesViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceScreen(
    navController: NavController,
    onNavigateUpServiceDetailedScreen: (Service) -> Unit,
    onNavigateUpServiceOwnerProfile: (Service, Long) -> Unit,
    showChooseIndustriesSheet: () -> Unit,
    onPopBackStack: () -> Unit,
    homeViewModel: HomeViewModel,
    viewModel: ServicesViewModel) {


    val userId = viewModel.userId

    val searchQuery = viewModel.submittedQuery
    val onlySearchBar = viewModel.onlySearchBar

    val context = LocalContext.current

    // Collect state from ViewModel
//    val searchOldQuery by viewModel.searchOldQuery.collectAsState()

//    val serviceItems by viewModel.serviceItems.collectAsState()
//    val isLoading by viewModel.isLoading.collectAsState()
//    val errorMessage by viewModel.errorMessage.collectAsState()
    val selectedItem by viewModel.selectedItem.collectAsState()


    // Get CoroutineScope
    val scope = rememberCoroutineScope()

    // Initialize the ModalBottomSheetState with default value

    val serviceInfoBottomSheetState = rememberModalBottomSheetState()

    val commentsModalBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

//    val lazyPagingServices: LazyPagingItems<Service> = viewModel.services.collectAsLazyPagingItems()

//    val cachedData by viewModel.cachedData.collectAsState() // Your cached data state


//    val items: List<Service> = cachedData

    val focusManager = LocalFocusManager.current


    val initialLoadState by viewModel.pageSource.initialLoadState.collectAsState()
    val isLoadingItems by viewModel.pageSource.isLoadingItems.collectAsState()
    val isRefreshingItems by viewModel.pageSource.isRefreshingItems.collectAsState()

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
                        searchQuery
                    ) // Make sure to pass necessary parameters
                }
            }
    }


    val connectivityManager = viewModel.connectivityManager


    val statusCallback: (NetworkConnectivityManager.STATUS) -> Unit = {
        when (it) {
            NetworkConnectivityManager.STATUS.STATUS_CONNECTED -> {
                viewModel.updateLastLoadedItemPosition(-1)
                viewModel.refresh(userId, searchQuery)

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






    BackHandler(commentsModalBottomSheetState.currentValue == SheetValue.Expanded) {
        scope.launch {
            commentsModalBottomSheetState.hide()
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize() // This makes the Box take up the entire available space

    ) {

        Column(modifier = Modifier.fillMaxSize()) {


            Box(
                modifier = Modifier
                    .fillMaxSize() // This makes the Box take up the entire available space

            ) {


                if (initialLoadState && items.isEmpty()) {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp), // Adjust the space between items
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (onlySearchBar) {
                            item {
                                Box(modifier = Modifier.fillParentMaxSize()) {
                                    CircularProgressIndicatorLegacy(
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
                    }

                } else {

                    PullToRefreshBox(
                        state = pullToRefreshState,
                        modifier = Modifier.fillMaxSize(),
                        isRefreshing = isRefreshingItems,
                        onRefresh = onRefresh
                    ) {

                        LazyColumn(
                            state = lazyListState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 8.dp),
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
                                            onNavigateUpServiceDetailedScreen(item)
                                        } ,
                                        onItemOptionClick = {
                                            viewModel.setSelectedItem(item)
                                            scope.launch {
                                                serviceInfoBottomSheetState.expand()
                                            }
                                        },
                                        onItemProfileClick = {
                                            onNavigateUpServiceOwnerProfile(item, item.user.userId)
                                        },

                                        onReviewsClicked = {


                                            scope.launch {
                                                commentsModalBottomSheetState.expand()
                                            }

                                            if (selectedItem == item) {
                                                return@ServiceCard
                                            }

                                            viewModel.loadReViewsSelectedItem(item)


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
                                            item.plans[0].planPriceUnit
                                        ),
                                        commentsCount = item.commentsCount
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


                                            CircularProgressIndicatorLegacy(
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
                                                            searchQuery
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


            }
        }

        if (serviceInfoBottomSheetState.currentValue == SheetValue.Expanded) {
            ModalBottomSheet(
                modifier = Modifier
                    .safeDrawingPadding(),
                onDismissRequest = {
                    scope.launch {
                        serviceInfoBottomSheetState.hide()
                    }
                },
                shape = RectangleShape, // Set shape to square (rectangle)
                sheetState = serviceInfoBottomSheetState,
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
                                    R.drawable.ic_dark_bookmark
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

        if (commentsModalBottomSheetState.currentValue == SheetValue.Expanded) {
            ModalBottomSheet(
                {
                    scope.launch {
                        commentsModalBottomSheetState.hide()
                    }
                },
                dragHandle = null,
                shape = RectangleShape,
                sheetState = commentsModalBottomSheetState,
                modifier = Modifier.safeDrawingPadding()
            ) {
                ServiceReviewsNavHost(userId, selectedItem, viewModel)

            }

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
    onReviewsClicked: () -> Unit = {},
    deleteBookmarkClicked: () -> Unit = {},
    commentsCount: Int = 0,
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
            .build()
    )



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
                        painter = painterResource(MaterialTheme.icons.location),
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
                            dropUnlessResumedV2(lifecycleOwner) {
                                onItemProfileClick()
                            }
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


            val imageRequest = ImageRequest.Builder(context)
                .size(Size.ORIGINAL)
                .data(serviceThumbnailUrl) // Use placeholder drawable if imageUrl is null
                .build()

//            val screenWidth = LocalConfiguration.current.screenWidthDp.dp
//            val aspectRatio = imageWidth.toFloat() / imageHeight.toFloat()

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
                            R.drawable.ic_dark_bookmark
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

            /*     Row(
                     modifier = Modifier.fillMaxWidth().padding(8.dp),
                     horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                     verticalAlignment = Alignment.CenterVertically
                 ) {




                     if(commentsCount>0){

                         Row(modifier = Modifier.wrapContentSize()
                             .clickable {
                                 onReviewsClicked()
                             },
                             horizontalArrangement = Arrangement.spacedBy(8.dp)

                             ){
                             Image(
                                 painterResource(R.drawable.ic_comment),
                                 contentDescription = null,
                                 modifier = Modifier
                                     .size(16.dp),
                                 colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.onBackground)
                             )

                             Text("(${commentsCount})", style = MaterialTheme.typography.bodySmall)
                         }

                     }

                 }*/
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