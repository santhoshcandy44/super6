package com.lts360.compose.ui.localjobs.manage

import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.dropUnlessResumed
import coil3.compose.AsyncImage
import com.lts360.R
import com.lts360.components.utils.errorLogger
import com.lts360.compose.ui.common.CircularProgressIndicatorLegacy
import com.lts360.compose.ui.localjobs.manage.viewmodels.PublishedLocalJobViewModel
import com.lts360.compose.ui.main.common.NoInternetScreen
import com.lts360.compose.ui.managers.NetworkConnectivityManager
import com.lts360.libs.ui.ShortToast


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagePublishedLocalJobApplicantsScreen(
    onPopBackStack: () -> Unit,
    viewModel: PublishedLocalJobViewModel
) {


    val selectedItem by viewModel.selectedItem.collectAsState()

    val item = selectedItem

    if (item == null) return

    val userId = viewModel.userId

    val context = LocalContext.current

    val initialLoadState by viewModel.pageSource.initialLoadState.collectAsState()
    val isLoadingItems by viewModel.pageSource.isLoadingItems.collectAsState()
    val isRefreshingItems by viewModel.pageSource.isRefreshingItems.collectAsState()

    val items by viewModel.pageSource.items.collectAsState()

    val hasNetworkError by viewModel.pageSource.hasNetworkError.collectAsState()
    val hasAppendError by viewModel.pageSource.hasAppendError.collectAsState()
    val hasMoreItems by viewModel.pageSource.hasMoreItems.collectAsState()

    val connectivityManager = viewModel.connectivityManager

    val lazyListState = rememberLazyListState()
    val lastLoadedItemPosition by viewModel.pageSource.lastLoadedItemPosition.collectAsState()

    val serviceInfoBottomSheetState = rememberModalBottomSheetState()

    val scope = rememberCoroutineScope()

    LaunchedEffect(initialLoadState) {
        errorLogger(initialLoadState.toString())
    }

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

                    viewModel.onUpdateLastLoadedApplicantItemPosition(if (lastLoadedItemPosition == -1) 0 else lastVisibleItemIndex)
                    viewModel.onNextPageLocalJobApplicants(item.localJobId)
                }
            }
    }

    val statusCallback: (NetworkConnectivityManager.STATUS) -> Unit = {
        when (it) {
            NetworkConnectivityManager.STATUS.STATUS_CONNECTED -> {
                viewModel.onUpdateLastLoadedApplicantItemPosition(-1)
                viewModel.onRefreshApplicants(item.localJobId)
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

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = {
                Text("Applicants", style = MaterialTheme.typography.titleMedium)
            }, navigationIcon = {
                IconButton(onClick = dropUnlessResumed {
                    onPopBackStack()
                }) {
                    Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = null)
                }
            })
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pullToRefresh(
                        isRefreshingItems, pullToRefreshState,
                        enabled = !(initialLoadState && items.isEmpty())
                    ) {
                        onRefresh()
                    }
            ) {


                if (initialLoadState && items.isEmpty()) {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
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

                } else {

                    if (hasNetworkError) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            NoInternetScreen(modifier = Modifier.align(Alignment.Center)) {
                                onRetry()
                            }
                        }
                    } else if (!isLoadingItems && !hasAppendError && items.isEmpty()) {
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
                    } else {
                        LazyColumn(
                            state = lazyListState,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(8.dp),
                            modifier = Modifier
                                .fillMaxSize()
                        ) {

                            items(
                                items,
                                key = { "list_items_${it.applicantId}" }) { applicant ->
                                LocalJobApplicantCard(
                                    profileUrl = applicant.user.profilePicUrl,
                                    firstName = applicant.user.firstName,
                                    lastName = applicant.user.lastName,
                                    email = applicant.user.email,
                                    phoneCountryCode = applicant.user.phoneCountryCode,
                                    phoneNumber = applicant.user.phoneNumber,
                                    location = applicant.user.geo,
                                    appliedAt = viewModel.formatZonedDateTimeFormat(applicant.appliedAt),
                                    isReviewed = applicant.isReviewed,
                                    onReviewStatsChanged = {
                                        if (it) {
                                            viewModel.onMarkAsReviewedLocalJob(
                                                userId,
                                                item.localJobId,
                                                applicant.applicantId
                                            ) {
                                                ShortToast(context, it)
                                            }
                                        } else {
                                            viewModel.onUnmarkAsReviewedLocalJob(
                                                userId,
                                                item.localJobId,
                                                applicant.applicantId
                                            ){
                                                ShortToast(context, it)
                                            }
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
                                                    viewModel.onRetryGetApplicants(item.localJobId)
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

            if (serviceInfoBottomSheetState.isVisible) {
                /*      ModalBottomSheet(
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

                              viewModel.setSelectedItem(
                                  key,
                                  nonNullSelectedItem.copy(isBookmarked = nonNullSelectedItem.isBookmarked)
                              )

                              Column(modifier = Modifier.fillMaxWidth()) {


                                  Row(
                                      modifier = Modifier
                                          .fillMaxWidth()
                                          .clickable {


                                          }
                                          .padding(16.dp),
                                      verticalAlignment = Alignment.CenterVertically
                                  ) {


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



                              }

                          }


                      }*/
            }

        }
    }


}


@Composable
private fun LocalJobApplicantCard(
    profileUrl: String?,
    firstName: String,
    lastName: String?,
    email: String,
    phoneCountryCode: String?,
    phoneNumber: String?,
    location: String?,
    appliedAt: String,
    isReviewed: Boolean,
    onReviewStatsChanged: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    profileUrl,
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, Color.Gray, CircleShape),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text("$firstName $lastName", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(email, style = MaterialTheme.typography.bodySmall)
                    if (phoneCountryCode != null && phoneNumber != null) {
                        Text(
                            "$phoneCountryCode $phoneNumber",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            location?.let {
                Text("Location: $location", style = MaterialTheme.typography.bodyMedium)
            }

            Text("Applied At: $appliedAt", style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {


                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .then(
                            if (isReviewed) Modifier.background(Color(0xFF34A853))
                            else Modifier.border(1.dp, Color(0xFF34A853), RoundedCornerShape(8.dp))
                        )
                        .clickable { onReviewStatsChanged(!isReviewed) }
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Reviewed",
                            tint = if (isReviewed) Color.White else Color(0xFF34A853),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Mark as Reviewed",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isReviewed) Color.White else Color.Unspecified
                        )
                    }
                }


                if (phoneCountryCode != null && phoneNumber != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(color = Color(0xFF1A73E8))
                            .clickable { }
                            .padding(vertical = 4.dp, horizontal = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Call",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Call",
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

            }

        }


    }
}
