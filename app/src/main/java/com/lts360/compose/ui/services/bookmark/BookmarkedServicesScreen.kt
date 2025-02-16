package com.lts360.compose.ui.services.bookmark


import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lts360.R
import com.lts360.api.Utils.ResultError
import com.lts360.compose.ui.main.ServiceCard
import com.lts360.compose.ui.main.ShimmerServiceCard
import com.lts360.compose.ui.main.common.NoInternetScreen
import com.lts360.compose.ui.utils.FormatterUtils.formatCurrency


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarkedServicesScreen(
    onNavigateUpDetailedService: () -> Unit,
    onNavigateUpServiceOwnerProfile: (Long) -> Unit,
    onPopBackStack:()-> Unit,
    viewModel: BookmarkedServicesViewModel = hiltViewModel()
    ) {


    val userId = viewModel.userId

    val context = LocalContext.current

    val items by viewModel.services.collectAsState()

    val isLoading by viewModel.isLoading.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val error by viewModel.error.collectAsState()


    val refreshStateEmptyItemsState = rememberScrollState()

    val sheetState = rememberModalBottomSheetState()

    // Observe the state from ViewModel
    var bottomSheetState by rememberSaveable { mutableStateOf(false) }

    val selectedItem by viewModel.selectedItem.collectAsState()


    LaunchedEffect(bottomSheetState) {
        if (bottomSheetState) {
            sheetState.expand()
        } else {
            sheetState.hide()
        }
    }


    fun onRefresh() {
        viewModel.onFetchBookmarkedServices(
            userId,
            isLoading = false,
            isRefreshing = true,
            onSuccess = {}
        ) { errorMessage ->
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }


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
                    Text(text = "Bookmarked Services", style = MaterialTheme.typography.titleMedium)
                }
            )
        },
    ) { contentPadding ->
        // Toolbar

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { onRefresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
        ) {




            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {

                    LazyColumn {
                      item(3){
                          ShimmerServiceCard()
                      }
                    }
                }
            } else {


                // Main content
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp) // Adjust the space between items

                ) {


                    if (error is ResultError.NoInternet) {
                        item {
                            Box(modifier = Modifier.fillParentMaxSize()) {
                                NoInternetScreen(modifier = Modifier.align(Alignment.Center)) {
                                    onRefresh()
                                }
                            }
                        }
                    }
                    else if(error is ResultError){

                        item {
                            Box(
                                modifier = Modifier
                                    .fillParentMaxSize()
                                    .verticalScroll(refreshStateEmptyItemsState),
                                contentAlignment = Alignment.Center // Center content within the Box

                            ) {

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally

                                ) {

                                    Image(
                                        painter = painterResource(R.drawable.something_went_wrong),
                                        contentDescription = "Image from drawable",
                                        modifier = Modifier
                                            .sizeIn(
                                                maxWidth = 200.dp,
                                                maxHeight = 200.dp
                                            )
                                    )

                                    Spacer(Modifier.height(16.dp))

                                    Text(text = "Oops, something went wrong")

                                }
                            }
                        }
                    }
                    else if (items.isEmpty()) {


                        item {
                            Box(
                                modifier = Modifier
                                    .fillParentMaxSize()
                                    .verticalScroll(refreshStateEmptyItemsState),
                                contentAlignment = Alignment.Center // Center content within the Box

                            ) {

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally

                                ) {

                                    Image(
                                        painter = painterResource(R.drawable.all_caught_up),
                                        contentDescription = "Image from drawable",
                                        modifier = Modifier
                                            .sizeIn(
                                                maxWidth = 200.dp,
                                                maxHeight = 200.dp
                                            )
                                    )

                                    Spacer(Modifier.height(16.dp))

                                    Text(text = "Oops, nothing to catch")

                                }
                            }
                        }

                    } else {
                        items(items) { item ->
                            ServiceCard(
                                onItemClick = {
                                    viewModel.setSelectedItem(item)
                                    onNavigateUpDetailedService()
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
                                distance = if (item.distance != null) item.distance.toString() else null,
                                userName = "${item.user.firstName} ${item.user.lastName ?: ""}",
                                profileImageUrl = item.user.profilePicUrl,
                                isUserOnline = item.user.isOnline,
                                serviceThumbnailUrl = item.thumbnail?.imageUrl,
                                imageWidth = if (item.images.isNotEmpty()) item.images[0].width else 0,
                                imageHeight = if (item.images.isNotEmpty()) item.images[0].height else 0,
                                serviceTitle = item.title,
                                serviceDescription = item.shortDescription,
                                startingPrice = formatCurrency(
                                    item.plans[0].planPrice.toDouble(),
                                    item.plans[0].planPriceUnit
                                ),
                                deleteBookmarkClicked = {
                                    viewModel.updateItem(item, false)

                                    viewModel.onRemoveBookmark(userId, item, {
                                        viewModel.removeItem(item)
                                        Toast.makeText(
                                            context,
                                            "Bookmark removed",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }) {
                                        viewModel.updateItem(item, true)
                                        Toast.makeText(
                                            context,
                                            "Failed to remove bookmark",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                                isBookmarked = item.isBookmarked,
                                enableBookmarkedServiceIcon = true
                            )

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
                selectedItem?.let { nonNullSelectedItem ->


                    Column(modifier = Modifier.fillMaxWidth()) {

                        Row(modifier = Modifier
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
                                            type = "text/plain"  // MIME type for text
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
                            verticalAlignment = Alignment.CenterVertically) {


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
