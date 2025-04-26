package com.lts360.compose.ui.localjobs

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.lts360.R
import com.lts360.compose.ui.NoRippleInteractionSource
import com.lts360.compose.ui.common.CircularProgressIndicatorLegacy
import com.lts360.compose.ui.localjobs.models.LocalJob
import com.lts360.compose.ui.main.common.NoInternetScreen
import com.lts360.compose.ui.managers.NetworkConnectivityManager
import com.lts360.compose.ui.theme.customColorScheme
import com.lts360.compose.ui.theme.icons
import com.lts360.libs.ui.ShortToast
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalJobsScreen(
    isTopBarShowing: Boolean,
    onNavigateUpSecondsDetailedScreen: (LocalJob) -> Unit,
    viewModel: LocalJobsViewmodel
) {


    val searchQuery = viewModel.submittedQuery

    val userId = viewModel.userId
    val onlySearchBar = viewModel.onlySearchBar

    val context = LocalContext.current

    val isGuest = viewModel.isGuest

    val selectedItem by viewModel.selectedItem.collectAsState()

    val initialLoadState by viewModel.pageSource.initialLoadState.collectAsState()
    val isLoadingItems by viewModel.pageSource.isLoadingItems.collectAsState()
    val isRefreshingItems by viewModel.pageSource.isRefreshingItems.collectAsState()

    val items by viewModel.pageSource.items.collectAsState()

    val hasNetworkError by viewModel.pageSource.hasNetworkError.collectAsState()
    val hasAppendError by viewModel.pageSource.hasAppendError.collectAsState()
    val hasMoreItems by viewModel.pageSource.hasMoreItems.collectAsState()

    val connectivityManager = viewModel.connectivityManager

    val lazyListState = rememberLazyListState()
    val lastLoadedItemPosition by viewModel.lastLoadedItemPosition.collectAsState()

    val serviceInfoBottomSheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()


    LaunchedEffect(lazyListState) {
        snapshotFlow { lazyListState.layoutInfo }
            .collect { layoutInfo ->

                val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull {
                    (it.key as? String)?.startsWith("list_items_") == true
                }?.index


                if (!isLoadingItems
                    && hasMoreItems
                    && !hasAppendError
                    && lastVisibleItemIndex != null
                    && lastVisibleItemIndex >= items.size - 10
                    && lastVisibleItemIndex >= lastLoadedItemPosition
                ) {

                    viewModel.updateLastLoadedItemPosition(if (lastLoadedItemPosition == -1) 0 else lastVisibleItemIndex)
                    viewModel.nextPage(
                        userId,
                        searchQuery
                    )
                }
            }
    }

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
                ShortToast(context, "No internet connection")

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

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {

        Box(
            modifier = Modifier
                .fillMaxSize() // This makes the Box take up the entire available space
                .pullToRefresh(
                    isRefreshingItems, pullToRefreshState,
                    enabled = !(initialLoadState && items.isEmpty()) && isTopBarShowing
                ) {
                    onRefresh()
                }
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
                    }
                }

            } else {

                if (hasNetworkError) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()) // Enables nested scrolling
                    ) {
                        NoInternetScreen(modifier = Modifier.align(Alignment.Center)) {
                            onRetry()
                        }
                    }
                }

                else if (!isLoadingItems && !hasAppendError && items.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())

                    ) {
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

                else {
                    LazyColumn(
                        state = lazyListState,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        modifier = Modifier
                            .fillMaxSize()
                    ) {

                        items (
                            items,
                            key = { "list_items_${it.localJobId}" }) { item ->

                            LocalJobCard(
                                isGuest,
                                item,
                                {
                                    onNavigateUpSecondsDetailedScreen(item)
                                },
                                {
                                    if (item.isBookmarked) {

                                        viewModel.directUpdateLocalJobIsBookMarked(
                                            item.localJobId,
                                            false
                                        )

                                        viewModel.onRemoveBookmark(
                                            viewModel.userId,
                                            item, onSuccess = {

                                                viewModel.directUpdateLocalJobIsBookMarked(
                                                    item.localJobId,
                                                    false
                                                )
                                                ShortToast(context, "Bookmark removed")


                                            }, onError = {

                                                viewModel.directUpdateLocalJobIsBookMarked(
                                                    item.localJobId,
                                                    true
                                                )

                                                ShortToast(context, "Something wrong")

                                            })


                                    }
                                    else {

                                        viewModel.directUpdateLocalJobIsBookMarked(
                                            item.localJobId,
                                            true
                                        )

                                        viewModel.onBookmark(
                                            viewModel.userId,
                                            item,
                                            onSuccess = {

                                                viewModel.directUpdateLocalJobIsBookMarked(
                                                    item.localJobId,
                                                    true)

                                                ShortToast(context, "Bookmarked")

                                            },
                                            onError = {

                                                viewModel.directUpdateLocalJobIsBookMarked(
                                                    item.localJobId,
                                                    false
                                                )

                                                ShortToast(context, "Something wrong")

                                            })
                                    }
                                }
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

            }

            Indicator(
                modifier = Modifier.align(Alignment.TopCenter),
                isRefreshing = isRefreshingItems,
                state = pullToRefreshState
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
                shape = RectangleShape, // Set shape to square (rectangle)
                sheetState = serviceInfoBottomSheetState,
                dragHandle = null // Remove the drag handle

            ) {
                // Sheet content
                selectedItem?.let { nonNullSelectedItem ->

                    viewModel.setSelectedItem(nonNullSelectedItem.copy(isBookmarked = nonNullSelectedItem.isBookmarked))

                    Column(modifier = Modifier.fillMaxWidth()) {


                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {


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
                                                )
                                                type = "text/plain"
                                            }

                                            context.startActivity(
                                                Intent.createChooser(
                                                    shareIntent,
                                                    "Share via"
                                                )
                                            )
                                        } catch (_: ActivityNotFoundException) {
                                            ShortToast(context, "No app to open")
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

}


@Composable
private fun LocalJobCard(
    isGuest: Boolean,
    item: LocalJob,
    onItemClicked: () -> Unit,
    onFavouriteClicked: () -> Unit
) {

    OutlinedCard(
        onClick = onItemClicked,
        interactionSource = NoRippleInteractionSource()
    ) {
        Column(
            modifier = Modifier
                .wrapContentSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .aspectRatio(1f)
            ) {

                if(item.images.isNotEmpty()){
                    AsyncImage(
                        item.images[0].imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }


                if (!isGuest) {

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(32.dp)
                            .background(
                                Color.LightGray.copy(alpha = 0.4f),
                                CircleShape
                            )
                            .clip(CircleShape)
                            .clickable {

                                onFavouriteClicked()

                            },
                        contentAlignment = Alignment.Center
                    ) {

                        Image(
                            imageVector = Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(
                                if (item.isBookmarked) {
                                    Color.Red
                                } else {
                                    Color.White
                                }
                            ),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                }


            }

            Column(modifier = Modifier.padding(8.dp)) {

                Text(
                    item.title,
                    maxLines = 1,
                    style = MaterialTheme.typography.titleMedium,
                    overflow = TextOverflow.Ellipsis
                )

                item.location?.geo?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 3,
                        minLines = 3,
                        overflow = TextOverflow.Clip,
                        color =
                            MaterialTheme.customColorScheme.textVariant1

                    )
                }


            }

        }
    }
}


