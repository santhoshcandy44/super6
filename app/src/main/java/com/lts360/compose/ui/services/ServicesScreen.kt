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
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.pullToRefresh
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.dropUnlessResumed
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.error
import coil3.request.placeholder
import coil3.size.Size
import com.lts360.R
import com.lts360.api.models.service.Service
import com.lts360.compose.dropUnlessResumedV2
import com.lts360.compose.ui.NoRippleInteractionSource
import com.lts360.compose.ui.ShimmerBox
import com.lts360.compose.ui.common.CircularProgressIndicatorLegacy
import com.lts360.compose.ui.main.common.NoInternetScreen
import com.lts360.compose.ui.managers.NetworkConnectivityManager
import com.lts360.compose.ui.onboarding.ChooseIndustryInfo
import com.lts360.compose.ui.onboarding.GuestChooseIndustryInfo
import com.lts360.compose.ui.theme.customColorScheme
import com.lts360.compose.ui.theme.icons
import com.lts360.compose.ui.utils.FormatterUtils.formatCurrency
import com.lts360.compose.ui.viewmodels.ServicesViewModel
import com.lts360.libs.ui.ShortToast
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesScreen(
    key: Int,
    isTopBarVisible: Boolean,
    onNavigateUpServiceDetailedScreen: (Service) -> Unit,
    onNavigateUpServiceOwnerProfile: (Service, Long) -> Unit,
    viewModel: ServicesViewModel
) {


    val userId = viewModel.userId

    val serviceRepository = viewModel.getServiceRepository(key)

    val searchQuery = serviceRepository.submittedQuery
    val onlySearchBar = serviceRepository.onlySearchBar

    val context = LocalContext.current

    val isGuest = viewModel.isGuest


    val selectedItem by serviceRepository.selectedItem.collectAsState()


    val scope = rememberCoroutineScope()
    val serviceInfoBottomSheetState = rememberModalBottomSheetState()
    val commentsModalBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)


    val initialLoadState by serviceRepository.pageSource.initialLoadState.collectAsState()
    val isLoadingItems by serviceRepository.pageSource.isLoadingItems.collectAsState()
    val isRefreshingItems by serviceRepository.pageSource.isRefreshingItems.collectAsState()

    val items by serviceRepository.pageSource.items.collectAsState()

    val hasNetworkError by serviceRepository.pageSource.hasNetworkError.collectAsState()
    val hasAppendError by serviceRepository.pageSource.hasAppendError.collectAsState()
    val hasMoreItems by serviceRepository.pageSource.hasMoreItems.collectAsState()

    val industriesCount by serviceRepository.pageSource.industriesCount.collectAsState()


    val lazyListState = rememberLazyListState()


    val lastLoadedItemPosition by serviceRepository.lastLoadedItemPosition.collectAsState()

    LaunchedEffect(lazyListState) {
        snapshotFlow { lazyListState.layoutInfo }
            .collect { layoutInfo ->

                val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull {
                    (it.key as? String)?.startsWith("lazy_items_") == true
                }?.index

                if (!isLoadingItems
                    && hasMoreItems
                    && !hasAppendError
                    && lastVisibleItemIndex != null
                    && lastVisibleItemIndex == items.size - 10
                    && lastVisibleItemIndex >= lastLoadedItemPosition
                ) {
                    viewModel.updateLastLoadedItemPosition(
                        key,
                        if (lastLoadedItemPosition == -1) 0 else lastVisibleItemIndex
                    )
                    viewModel.nextPage(
                        key,
                        userId,
                        searchQuery
                    )
                }
            }
    }


    val connectivityManager = serviceRepository.connectivityManager


    val statusCallback: (NetworkConnectivityManager.STATUS) -> Unit = {
        when (it) {
            NetworkConnectivityManager.STATUS.STATUS_CONNECTED -> {
                viewModel.updateLastLoadedItemPosition(key, -1)
                viewModel.refresh(key, userId, searchQuery)
            }

            NetworkConnectivityManager.STATUS.STATUS_NOT_CONNECTED_INITIALLY -> {
                serviceRepository.pageSource.setNetWorkError(true)
            }

            NetworkConnectivityManager.STATUS.STATUS_NOT_CONNECTED_ON_COMPLETED_JOB -> {
                serviceRepository.pageSource.setNetWorkError(true)
                serviceRepository.pageSource.setRefreshingItems(false)
                ShortToast(context, "No internet connection")
            }
        }
    }


    val onRefresh: () -> Unit = {
        serviceRepository.pageSource.setRefreshingItems(true)
        connectivityManager.checkForSeconds(
            Handler(Looper.getMainLooper()), statusCallback,
            4000
        )
    }

    val onRetry = {
        serviceRepository.pageSource.setRefreshingItems(true)
        connectivityManager.checkForSeconds(
            Handler(Looper.getMainLooper()), statusCallback,
            4000
        )
    }

    val pullToRefreshState = rememberPullToRefreshState()

    BackHandler(commentsModalBottomSheetState.isVisible) {
        scope.launch {
            commentsModalBottomSheetState.hide()
        }
    }

    Box(modifier = Modifier.pullToRefresh(
            isRefreshingItems, pullToRefreshState,
            enabled = !(initialLoadState && items.isEmpty()) && isTopBarVisible,
            threshold = 160.dp
        ) { onRefresh() }) {

        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {

            if (initialLoadState && items.isEmpty()) {
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
            } else if (hasNetworkError) {
                item {
                    Box(modifier = Modifier.fillParentMaxSize()) {
                        NoInternetScreen(modifier = Modifier.align(Alignment.Center)) {
                            onRetry()
                        }
                    }
                }
            } else if (industriesCount == 0) {

                item {
                    Box(modifier = Modifier.fillParentMaxSize()) {
                        if (isGuest) {
                            GuestChooseIndustryInfo {
                                onRefresh()
                            }
                        } else {
                            ChooseIndustryInfo {
                                onRefresh()
                            }
                        }
                    }
                }
            }

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


                items(items, key = { "lazy_items_${it.serviceId}" }) { item ->

                    ServiceCard(
                        onItemClick = {
                            onNavigateUpServiceDetailedScreen(item)
                        },
                        onItemOptionClick = {
                            viewModel.setSelectedItem(key, item)
                            scope.launch {
                                serviceInfoBottomSheetState.expand()
                            }
                        },
                        onItemProfileClick = {
                            onNavigateUpServiceOwnerProfile(item, item.user.userId)
                        },
                        onReviewsClicked = {
                           /* scope.launch {
                                commentsModalBottomSheetState.expand()
                            }
                            if (selectedItem == item) {
                                return@ServiceCard
                            }
                            viewModel.loadReViewsSelectedItem(item)*/
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
                                            key,
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

        Indicator(
            modifier = Modifier.align(Alignment.TopCenter),
            isRefreshing = isRefreshingItems,
            state = pullToRefreshState,
        )

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
            shape = RectangleShape,
            sheetState = serviceInfoBottomSheetState,
            dragHandle = null

        ) {


            selectedItem?.let { nonNullSelectedItem ->

                viewModel.setSelectedItem(key, nonNullSelectedItem.copy(isBookmarked = nonNullSelectedItem.isBookmarked))

                Column(modifier = Modifier.fillMaxWidth()) {


                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (nonNullSelectedItem.isBookmarked) {
                                    viewModel.setSelectedItem(
                                        key,
                                        nonNullSelectedItem.copy(
                                            isBookmarked = false
                                        )
                                    )

                                    viewModel.onRemoveBookmark(
                                        key,
                                        viewModel.userId,
                                        nonNullSelectedItem, onSuccess = {
                                            viewModel.setSelectedItem(
                                                key,
                                                nonNullSelectedItem.copy(
                                                    isBookmarked = false
                                                )
                                            )
                                            viewModel.directUpdateServiceIsBookMarked(
                                                key,
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
                                                key,
                                                nonNullSelectedItem.copy(
                                                    isBookmarked = true
                                                )
                                            )

                                        })


                                } else {

                                    viewModel.setSelectedItem(
                                        key,
                                        selectedItem?.copy(
                                            isBookmarked = true
                                        )
                                    )

                                    viewModel.onBookmark(
                                        key,
                                        viewModel.userId,
                                        nonNullSelectedItem,
                                        onSuccess = {

                                            viewModel.setSelectedItem(
                                                key,
                                                nonNullSelectedItem.copy(
                                                    isBookmarked = true
                                                )
                                            )

                                            viewModel.directUpdateServiceIsBookMarked(
                                                key,
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
                                                key,
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
                                MaterialTheme.icons.bookmarkedRed
                            ) else painterResource(
                                MaterialTheme.icons.bookmark
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

/*    if (commentsModalBottomSheetState.currentValue == SheetValue.Expanded) {
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

    }*/


}


@Composable
fun ServiceCard(
    location: String?,
    distance: String?,
    userName: String,
    profileImageUrl: String?,
    isUserOnline: Boolean,
    serviceThumbnailUrl: String?,
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


    val context = LocalContext.current

    var bookMarkStatus by remember(isBookmarked) { mutableStateOf(isBookmarked) }

    val lifecycleOwner = LocalLifecycleOwner.current


    Card(
        onClick = dropUnlessResumed {
            onItemClick()
        },
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(8.dp), // Remove rounded corners
        elevation = CardDefaults.cardElevation(2.dp),
        interactionSource = NoRippleInteractionSource()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

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


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
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
                    AsyncImage(
                        ImageRequest.Builder(context)
                            .data(profileImageUrl) // Set the image URL
                            .placeholder(R.drawable.user_placeholder) // Placeholder image
                            .error(R.drawable.user_placeholder) // Error image in case of failure
                            .build(),
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


            val imageRequest = ImageRequest.Builder(context)
                .size(Size.ORIGINAL)
                .data(serviceThumbnailUrl) // Use placeholder drawable if imageUrl is null
                .build()

            /*            val screenWidth = LocalConfiguration.current.screenWidthDp.dp
                        val aspectRatio = imageWidth.toFloat() / imageHeight.toFloat()*/

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

                        Icon(
                            if (bookMarkStatus) painterResource(
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
fun ShimmerServiceCard() {

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
                Row(
                    modifier = Modifier
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

                    /*    if (enableBookmarkedServiceIcon) {
                                 Icon(
                                     if (bookMarkStatus) painterResource(
                                         R.drawable.ic_bookmarked
                                     ) else painterResource(
                                         R.drawable.ic_bookmark
                                     ),
                                     tint = Color.Unspecified,
                                     contentDescription = null,
                                     modifier = Modifier
                                         .size(32.dp)
                                 )
                        }*/

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
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