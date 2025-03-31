package com.lts360.compose.ui.bookmarks


import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
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
import com.lts360.api.Utils.ResultError
import com.lts360.api.models.service.Service
import com.lts360.api.models.service.UsedProductListing
import com.lts360.compose.ui.getCurrencySymbol
import com.lts360.compose.ui.main.common.NoInternetScreen
import com.lts360.compose.ui.services.ServiceCard
import com.lts360.compose.ui.services.ShimmerServiceCard
import com.lts360.compose.ui.utils.FormatterUtils.formatCurrency


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksScreen(
    onNavigateUpDetailedService: () -> Unit,
    onNavigateUpServiceOwnerProfile: (Long) -> Unit,
    onNavigateUpDetailedUsedProductListing: () -> Unit,

    onPopBackStack: () -> Unit,
    viewModel: BookmarksViewModel
) {


    val userId = viewModel.userId

    val context = LocalContext.current

    val items by viewModel.bookmarks.collectAsState()

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
        viewModel.onFetchUserBookmarks(
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
                        item(3) {
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
                    } else if (error is ResultError) {

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
                    } else if (items.isEmpty()) {


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
                        items(items) { bookmarkItem ->

                            when (bookmarkItem.type) {
                                "service" -> {

                                    val item = bookmarkItem as Service

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
                                        serviceTitle = item.title,
                                        serviceDescription = item.shortDescription,
                                        startingPrice = formatCurrency(
                                            item.plans[0].planPrice.toDouble(),
                                            item.plans[0].planPriceUnit
                                        ),
                                        deleteBookmarkClicked = {
                                            viewModel.updateServiceItem(item, false)

                                            viewModel.onRemoveBookmark(userId, item, {
                                                viewModel.removeServiceItem(item)
                                                Toast.makeText(
                                                    context,
                                                    "Bookmark removed",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }) {
                                                viewModel.updateServiceItem(item, true)
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

                                "used_product_listing" -> {
                                    val item = bookmarkItem as UsedProductListing
                                    BookmarkedUsedProductListingCard(viewModel.signInMethod, item, {
                                        viewModel.setSelectedItem(item)
                                        onNavigateUpDetailedUsedProductListing()
                                    }, {

                                        viewModel.updateUsedProductItem(item, false)

                                        viewModel.onRemoveUsedProductListingBookmark(userId, item, {
                                            viewModel.removeUsedProductListingItem(item)
                                            Toast.makeText(
                                                context,
                                                "Bookmark removed",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }) {
                                            viewModel.updateUsedProductItem(item, true)
                                            Toast.makeText(
                                                context,
                                                "Failed to remove bookmark",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }

                                    })
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
                selectedItem?.let { nonNullSelectedItem ->

                    if(nonNullSelectedItem is Service){
                        Column(modifier = Modifier.fillMaxWidth()) {

                            Row(modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    try {

                                        val shareIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(
                                                Intent.EXTRA_TEXT,
                                                nonNullSelectedItem.shortCode
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


}




@Composable
fun BookmarkedUsedProductListingCard(signInMethod:String, usedProductListing:UsedProductListing, onItemClicked:()-> Unit, onFavouriteClicked:()->Unit){

    val greenColor = Color(0xFF1BB24B)


    OutlinedCard(onClick =onItemClicked

    ){
        Row(modifier = Modifier.wrapContentSize()){
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .aspectRatio(1f)
            ) {

                if(usedProductListing.images.isNotEmpty()){
                    AsyncImage(
                        usedProductListing.images[0].imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }


                if(signInMethod!="guest"){

                    // 🔹 Favorite Icon Background Box
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd) // 🔹 Positions it at the top-right
                            .padding(8.dp) // 🔹 Adds spacing from edges
                            .size(32.dp) // 🔹 Fixed size
                            .background(
                                Color.LightGray.copy(alpha = 0.4f),
                                shape = CircleShape
                            ).clickable {

                                onFavouriteClicked()

                            },
                        contentAlignment = Alignment.Center
                    ) {
                        // 🔹 Favorite Icon
                        Image(
                            imageVector = Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(if(usedProductListing.isBookmarked){
                                Color.Red
                            }else{
                                Color.White
                            }),
                            modifier = Modifier.size(24.dp) // 🔹 Set proper size (without extra padding)
                        )
                    }

                }

                Box(
                    modifier = Modifier.background(Color(0xFFFFA500))
                        .align(Alignment.BottomStart)
                ){
                    Text("Seconds", color = Color.White, modifier = Modifier
                        .padding(4.dp), style = MaterialTheme.typography.bodySmall)
                }


            }

            Column(modifier = Modifier.fillMaxWidth()
                .padding(8.dp)
                .weight(1f)
            ) {

                Text(usedProductListing.name,
                    maxLines = 1,
                    style = MaterialTheme.typography.titleMedium,
                    overflow = TextOverflow.Ellipsis
                )

                usedProductListing.location?.geo?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 3,
                        minLines = 3,
                        overflow = TextOverflow.Clip
                    )
                }

                // 🔹 Aligns the price to the END
                Row(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        usedProductListing.price.toString()+ getCurrencySymbol(usedProductListing.priceUnit),
                        style = MaterialTheme.typography.headlineSmall,
                        color = greenColor
                    )
                }



            }
        }
    }
}




