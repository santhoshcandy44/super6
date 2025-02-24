package com.lts360.compose.ui.usedproducts

import android.content.Intent
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.error
import coil3.request.placeholder
import com.lts360.R
import com.lts360.api.models.service.FeedUserProfileInfo
import com.lts360.api.models.service.Image
import com.lts360.api.models.service.UsedProductListing
import com.lts360.app.database.models.chat.ChatUser
import com.lts360.compose.dropUnlessResumedV2
import com.lts360.compose.ui.ShimmerBox
import com.lts360.compose.ui.auth.AuthActivity
import com.lts360.compose.ui.auth.ForceWelcomeScreen
import com.lts360.compose.ui.main.navhosts.routes.SecondsOwnerProfile
import com.lts360.compose.ui.main.viewmodels.SecondsViewmodel
import com.lts360.compose.ui.bookmarks.BookmarksViewModel
import com.lts360.compose.ui.theme.customColorScheme
import com.lts360.compose.ui.utils.FormatterUtils.formatCurrency
import com.lts360.compose.utils.ExpandableText
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Composable
fun DetailedUsedProductListingScreen(
    key: Int,
    navHostController: NavHostController,
    onNavigateUpSlider: (Int) -> Unit,
    navigateUpChat: (ChatUser, Int, Long, FeedUserProfileInfo) -> Unit,
    onNavigateUpForceJoinNow: () -> Unit,
    onUsedProductListingClicked:(Long)-> Unit,
    viewModel: SecondsViewmodel
) {

    val userId = viewModel.userId
    val signInMethod = viewModel.signInMethod
    val selectedItem by viewModel.selectedItem.collectAsState()


    val scope = rememberCoroutineScope()
    var job by remember { mutableStateOf<Job?>(null) } // Track job reference

    DetailedUsedProductListingContent(
        userId,
        selectedItem,
        onNavigateUpSlider,
        onNavigateUpForceJoinNow,
        {
            selectedItem?.let {
                onUsedProductListingClicked(it.user.userId)
            }
        },
        {
            selectedItem?.let {
                Button(
                    onClick = dropUnlessResumed {

                        if (job?.isActive == true) {
                            return@dropUnlessResumed
                        }

                        job = scope.launch {
                            val selectedChatUser = viewModel.getChatUser(userId, it.user)
                            val selectedChatId = selectedChatUser.chatId

                            if (signInMethod == "guest") {
                                it()
                            } else {
                                navigateUpChat(
                                    selectedChatUser,
                                    selectedChatId,
                                    it.user.userId,
                                    it.user
                                )
                            }
                        }

                    },

                    shape = RectangleShape,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = "Send message", color = Color.White)
                }
            }
        }
    ) {
        navHostController.popBackStack()
    }

}

@Composable
fun FeedUserDetailedSecondsInfoScreen(
    navHostController: NavHostController,
    key: Int,
    onNavigateUpSlider: (Int) -> Unit,
    navigateUpChat: (ChatUser, Int, Long, FeedUserProfileInfo) -> Unit,
    onNavigateUpForceJoinNow: () -> Unit,
    servicesViewModel: SecondsViewmodel,
    viewModel: SecondsOwnerProfileViewModel = hiltViewModel(remember {
        navHostController.getBackStackEntry<SecondsOwnerProfile>()
    })) {



    val userId = viewModel.userId
    val signInMethod = viewModel.signInMethod

    val selectedItem by servicesViewModel.nestedServiceOwnerProfileSelectedItem.collectAsState()


    val scope = rememberCoroutineScope()
    var job by remember { mutableStateOf<Job?>(null) } // Track job reference

    DetailedUsedProductListingContent(
        userId,
        selectedItem,
        onNavigateUpSlider,
        onNavigateUpForceJoinNow,
        {},
        {

            selectedItem?.let {
                Button(
                    onClick = dropUnlessResumed {

                        if (job?.isActive == true) {
                            return@dropUnlessResumed
                        }

                        job = scope.launch {
                            val selectedChatUser = viewModel.getChatUser(userId, it.user)
                            val selectedChatId = selectedChatUser.chatId

                            if (signInMethod == "guest") {
                                it()
                            } else {
                                navigateUpChat(
                                    selectedChatUser,
                                    selectedChatId,
                                    it.user.userId,
                                    it.user
                                )
                            }
                        }

                    },

                    shape = RectangleShape,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = "Send message", color = Color.White)
                }
            }

        }
    ) {
        navHostController.popBackStack()
    }
}

@Composable
fun BookmarkedDetailedUsedProductListingInfoScreen(
    navHostController: NavHostController,
    onNavigateUpSlider: (Int) -> Unit,
    navigateUpChat: (Int, Long, FeedUserProfileInfo) -> Unit,
    onNavigateUpForceJoinNow: () -> Unit,
    onNavigateUpServiceOwnerProfile: (Long) -> Unit,
    viewModel: BookmarksViewModel,
) {

    val userId = viewModel.userId
    val signInMethod = viewModel.signInMethod

    val selectedItem by viewModel.selectedItem.collectAsState()

    val item = selectedItem // Store in a local variable

    if (item !is UsedProductListing) return // Smart cast now works

    val scope = rememberCoroutineScope()
    var job by remember { mutableStateOf<Job?>(null) } // Track job reference

    DetailedUsedProductListingContent(
        userId,
        item,
        onNavigateUpSlider,
        onNavigateUpForceJoinNow,
        {
            onNavigateUpServiceOwnerProfile(item.user.userId)
        },
        {

            item.let {
                Button(
                    onClick = dropUnlessResumed {
                        if (job?.isActive == true) {
                            return@dropUnlessResumed
                        }
                        job = scope.launch {
                            val selectedChatUser = viewModel.getChatUser(userId, it.user)
                            val selectedChatId = selectedChatUser.chatId

                            if (signInMethod == "guest") {
                                it()
                            } else {
                                navigateUpChat(
                                    selectedChatId,
                                    it.user.userId,
                                    it.user
                                )
                            }
                        }

                    },

                    shape = RectangleShape,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = "Send message", color = Color.White)
                }
            }
        }
    ) {
        navHostController.popBackStack()
    }
}

@Composable
fun BookmarkedFeedUserUsedProductListingInfoScreen(
    navHostController: NavHostController,
    onNavigateUpSlider: (Int) -> Unit,
    navigateUpChat: (ChatUser, Int, Long, FeedUserProfileInfo) -> Unit,
    onNavigateUpForceJoinNow: () -> Unit,
    servicesViewModel: BookmarksViewModel,
    viewModel: SecondsOwnerProfileViewModel = hiltViewModel(remember {
        navHostController.getBackStackEntry<SecondsOwnerProfile>()
    })

    ) {



    val userId = viewModel.userId
    val signInMethod = viewModel.signInMethod

    val selectedItem by servicesViewModel.nestedServiceOwnerProfileSelectedItem.collectAsState()


    val scope = rememberCoroutineScope()
    var job by remember { mutableStateOf<Job?>(null) } // Track job reference


    val item = selectedItem // Store in a local variable

    if (item !is UsedProductListing) return // Smart cast now works


    DetailedUsedProductListingContent(
        userId,
        item,
        onNavigateUpSlider,
        onNavigateUpForceJoinNow,
        {},
        {

            item?.let {
                Button(
                    onClick = dropUnlessResumed {

                        if (job?.isActive == true) {
                            return@dropUnlessResumed
                        }

                        job = scope.launch {
                            val selectedChatUser = viewModel.getChatUser(userId, it.user)
                            val selectedChatId = selectedChatUser.chatId

                            if (signInMethod == "guest") {
                                it()
                            } else {
                                navigateUpChat(
                                    selectedChatUser,
                                    selectedChatId,
                                    it.user.userId,
                                    it.user
                                )
                            }
                        }

                    },

                    shape = RectangleShape,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = "Send message", color = Color.White)
                }
            }

        }
    ) {
        navHostController.popBackStack()
    }
}





@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailedUsedProductListingContent(
    userId: Long,
    selectedService: UsedProductListing?,
    onNavigateUpSlider: (Int) -> Unit,
    onNavigateUpForceJoinNow: () -> Unit,
    onUsedProductListingOwnerProfileClicked:()-> Unit,
    onChatButtonClicked: @Composable (() -> Unit) -> Unit,
    onPopBackStack: () -> Unit,

) {


    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            SheetValue.Hidden,
            skipHiddenState = false
        )
    )

    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current


    BackHandler(bottomSheetScaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
        coroutineScope.launch {
            bottomSheetScaffoldState.bottomSheetState.hide()
        }
    }


    BottomSheetScaffold(
        scaffoldState = bottomSheetScaffoldState,
        sheetContent = {
            ForceWelcomeScreen(
                onLogInNavigate = {
                    context.startActivity(
                        Intent(context, AuthActivity::class.java)
                            .apply {
                                flags =
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                                putExtra("force_type", "force_login")
                            })
                }, onSelectAccountNavigate = {
                    context.startActivity(
                        Intent(context, AuthActivity::class.java)
                            .apply {
                                flags =
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                                putExtra("force_type", "force_register")
                            }
                    )

                }) {
                coroutineScope.launch {
                    bottomSheetScaffoldState.bottomSheetState.hide()
                }
            }
        },
        sheetPeekHeight = 0.dp, // Default height when sheet is collapsed
        sheetSwipeEnabled = true, // Allow gestures to hide/show bottom sheet

        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = dropUnlessResumed { onPopBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back Icon"
                        )
                    }
                },
                title = {
                    Text(
                        text = "Seconds Info",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            )
        }
    ) {


        Scaffold { paddingValues ->

            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                selectedService?.let {
                    DetailedUsedProductListingInfo(
                        userId,
                        it,
                        onNavigateUpSlider,
                        onUsedProductListingOwnerProfileClicked

                    ) {
                        onChatButtonClicked {
                            coroutineScope.launch {
                                bottomSheetScaffoldState.bottomSheetState.expand()
                            }
                        }
                    }
                } ?: run {

                    LoadingDetailedServiceInfo()
                }
            }


        }
    }
}

@Composable
fun DetailedUsedProductListingInfo(
    userId: Long,
    service: UsedProductListing,
    onNavigateUpSlider: (Int) -> Unit,
    onUsedProductListingOwnerProfileClicked:()-> Unit,
    chatButtonClicked: @Composable () -> Unit
) {
    // Set up the top bar with the toolbar and title

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 16.dp)
    ) {

        // Service Owner
        item(key = "serviceOwner-${service.user.userId}") {
            ServiceOwner(
                "${service.user.firstName} ${service.user.lastName ?: ""}",
                service.user.profilePicUrl,
                "${service.country}/${service.state}",
                service.user.isOnline,
                onUsedProductListingOwnerProfileClicked
            )
        }

        // Image Slider
        item(key = "serviceImages-${service.productId}") {
            ServiceImagesSliderDetailedServiceInfo(service.images, onNavigateUpSlider)
        }

        // Service Title and Description
        item(key = "serviceDescription-${service.productId}") {
            ServiceDescription(
                service.name,
                service.description,
                formatCurrency(
                    service.price,
                    service.priceUnit
                )
            )
        }



        // Send Message Button (if not the service owner)
        if (userId != service.user.userId) {
            item(key = "sendMessage-${service.productId}") {
                Spacer(modifier = Modifier.height(8.dp))
                SendMessageButton(chatButtonClicked)
            }
        }

    }

}

@Composable
fun LoadingDetailedServiceInfo() {
    // Set up the top bar with the toolbar and title

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 16.dp)
    ) {

        // Service Owner
        item(key = "serviceOwner-${0}") {


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(40.dp)
                ) {
                    ShimmerBox(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {

                    ShimmerBox {
                        Text(
                            color = Color.Transparent,
                            text = "Service owner name", // Replace with your data
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodyMedium

                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {

                        ShimmerBox {
                            Text(
                                color = Color.Transparent,
                                text = "Service from and verified icon",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }

        // Image Slider
        item(key = "serviceImages-${0}") {

            ShimmerBox {
                Box(modifier = Modifier.aspectRatio(16 / 9f)) {
                }
            }
        }

        // Service Title and Description
        item(key = "serviceDescription-${0}") {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {

                ShimmerBox {
                    Text(
                        color = Color.Transparent,
                        modifier = Modifier.fillMaxWidth(),
                        text = "Short description", // Replace with your data
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                ShimmerBox {
                    Text(
                        color = Color.Transparent,
                        modifier = Modifier.fillMaxWidth(0.6f),
                        text = "Long description", // Replace with your data
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }


    }

}

@Composable
fun ServiceOwner(
    serviceOwner: String,
    urlImage: String?,
    serviceFrom: String,
    isOnline: Boolean,
    onUsedProductListingClicked:()->Unit
) {

    val context = LocalContext.current

    val painter = rememberAsyncImagePainter(
        ImageRequest.Builder(context)
            .data(urlImage)
            .placeholder(R.drawable.user_placeholder)
            .error(R.drawable.user_placeholder)
            .build()
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp).clickable { onUsedProductListingClicked() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp)
        ) {
            Image(
                painter = painter, // Replace with your image resource
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            if (isOnline) {
                // Online status dot
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .align(Alignment.BottomEnd)
                        .background(
                            Color.Green,
                            shape = CircleShape
                        ) // Use your drawable resource if necessary
                )
            }

        }

        Spacer(modifier = Modifier.width(8.dp))

        Column {
            Text(
                text = serviceOwner, // Replace with your data
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium

            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = serviceFrom,
                    style = MaterialTheme.typography.bodyMedium

                )
                Image(
                    painter = painterResource(id = R.drawable.ic_verified_service), // Replace with your drawable
                    contentDescription = null,
                    modifier = Modifier
                        .size(16.dp)
                        .padding(start = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun ServiceDescription(
    name: String,
    description: String,
    price:String
) {


    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = name, // Replace with your data
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))


        // Expandable long description

        ExpandableText(
            description,
            style = MaterialTheme.typography.bodyMedium,
            showMoreStyle = SpanStyle(color = Color(0xFF4399FF)),
            showLessStyle = SpanStyle(color = Color(0xFF4399FF)),
            textModifier = Modifier.wrapContentSize()
        )



        Spacer(modifier = Modifier.height(8.dp))

        // Starting from price
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Price", modifier = Modifier.padding(vertical = 8.dp))
            Text(
                text = price,
                fontSize = 24.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }

    }
}

@Composable
private fun ServiceImagesSliderDetailedServiceInfo(
    images: List<Image>,
    onNavigateUpSlider: (Int) -> Unit,
) {


    val context = LocalContext.current

    val lifecycleOwner = LocalLifecycleOwner.current

    // Create a pager state to manage the pager's state
    val pagerState = rememberPagerState(pageCount = { images.size })

    val cellConfiguration = if (LocalConfiguration.current.orientation == ORIENTATION_LANDSCAPE) {
        StaggeredGridCells.Adaptive(minSize = 175.dp)
    } else StaggeredGridCells.Fixed(2)


    if (images.isNotEmpty()) {


        /*       LazyHorizontalStaggeredGrid(
                   rows = cellConfiguration,
                   contentPadding = PaddingValues(16.dp),
                   verticalArrangement = Arrangement.spacedBy(16.dp),
                   horizontalItemSpacing = 16.dp,
                   modifier = Modifier
                       .fillMaxWidth()
                       .width(100.dp)  // Maintain a 16:9 aspect ratio
               ) {
                   itemsIndexed(images) { index, image ->
                       AsyncImage(
                           model = image.imageUrl,  // Use the URL for the image
                           contentDescription = null,
                           contentScale = ContentScale.Fit,  // Adjust content scale based on your needs
                           modifier = Modifier
                               .clickable {
                                   executeIfResumed(lifecycleOwner) {
                                       onNavigateUpSlider(index)  // Pass the index of the image
                                   }
                               }
                       )
                   }
               }*/

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16 / 9f)
                .background(MaterialTheme.customColorScheme.serviceSurfaceContainer),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            itemsIndexed(images) { index, image ->
                AsyncImage(
                    model = image.imageUrl,  // Use the URL for the image
                    contentDescription = null,
                    contentScale = ContentScale.Fit,  // Adjust content scale based on your needs
                    modifier = Modifier
                        .wrapContentWidth()
                        .clickable {
                            dropUnlessResumedV2(lifecycleOwner) {
                                onNavigateUpSlider(index)  // Pass the index of the image
                            }
                        }
                )
            }
        }
    }


}

@Composable
fun SendMessageButton(chatButtonClicked: @Composable () -> Unit) {
    chatButtonClicked()
}