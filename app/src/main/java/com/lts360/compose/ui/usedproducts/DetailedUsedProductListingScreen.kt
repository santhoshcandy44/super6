package com.lts360.compose.ui.usedproducts

import android.content.Intent
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.layout.ContentScale
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
import coil3.request.ImageRequest
import coil3.request.error
import coil3.request.placeholder
import com.lts360.R
import com.lts360.api.models.service.FeedUserProfileInfo
import com.lts360.api.models.service.Image
import com.lts360.api.models.service.UsedProductListing
import com.lts360.app.database.models.chat.ChatUser
import com.lts360.compose.dropUnlessResumedV2
import com.lts360.compose.noRippleClickable
import com.lts360.compose.ui.ShimmerBox
import com.lts360.compose.ui.auth.AuthActivity
import com.lts360.compose.ui.auth.ForceWelcomeScreen
import com.lts360.compose.ui.bookmarks.BookmarksViewModel
import com.lts360.compose.ui.main.navhosts.routes.BottomNavRoutes
import com.lts360.compose.ui.main.viewmodels.SecondsViewmodel
import com.lts360.compose.ui.services.SendMessageButton
import com.lts360.compose.ui.utils.FormatterUtils.formatCurrency
import com.lts360.compose.utils.ExpandableText
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Composable
fun DetailedUsedProductListingScreen(
    navHostController: NavHostController,
    key: Int,
    onNavigateUpSlider: (Int) -> Unit,
    navigateUpChat: (ChatUser, Int, Long) -> Unit,
    onUsedProductListingOwnerProfileClicked: (Long) -> Unit,
    viewModel: SecondsViewmodel
) {

    val userId = viewModel.userId
    val isGuest = viewModel.isGuest
    val selectedItem by viewModel.getSecondsRepository(key).selectedItem.collectAsState()


    val scope = rememberCoroutineScope()
    var job by remember { mutableStateOf<Job?>(null) }

    DetailedUsedProductListingContent(
        userId,
        isGuest,
        selectedItem,
        onNavigateUpSlider,

        {
            selectedItem?.let {
                if (job?.isActive == true) {
                    return@let
                }

                job = scope.launch {
                    val selectedChatUser = viewModel.getChatUser(userId, it.user)
                    val selectedChatId = selectedChatUser.chatId

                    navigateUpChat(
                        selectedChatUser,
                        selectedChatId,
                        it.user.userId
                    )
                }
            }
        },
        {
            selectedItem?.let {
                onUsedProductListingOwnerProfileClicked(it.user.userId)
            }
        },
    ) {
        navHostController.popBackStack()
    }

}

@Composable
fun FeedUserDetailedSecondsInfoScreen(
    navHostController: NavHostController,
    onNavigateUpSlider: (Int) -> Unit,
    navigateUpChat: (ChatUser, Int, Long) -> Unit,
    viewModel: SecondsOwnerProfileViewModel = hiltViewModel(remember {
        navHostController.getBackStackEntry<BottomNavRoutes.SecondsOwnerProfile>()
    })
) {


    val userId = viewModel.userId
    val signInMethod = viewModel.signInMethod
    val isGuest = signInMethod == "guest"

    val selectedItem by viewModel.selectedItem.collectAsState()

    val scope = rememberCoroutineScope()
    var job by remember { mutableStateOf<Job?>(null) }

    DetailedUsedProductListingContent(
        userId,
        isGuest,
        selectedItem,
        onNavigateUpSlider,
        {
            selectedItem?.let {
                if (job?.isActive == true) {
                    return@let
                }

                job = scope.launch {
                    val selectedChatUser = viewModel.getChatUser(userId, it.user)
                    val selectedChatId = selectedChatUser.chatId

                    navigateUpChat(
                        selectedChatUser,
                        selectedChatId,
                        it.user.userId
                    )
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
    onNavigateUpSecondsOwnerProfile: (Long) -> Unit,
    viewModel: BookmarksViewModel,
) {

    val userId = viewModel.userId
    val signInMethod = viewModel.signInMethod
    val isGuest = signInMethod == "guest"

    val selectedItem by viewModel.selectedItem.collectAsState()

    val item = selectedItem

    if (item !is UsedProductListing) return

    val scope = rememberCoroutineScope()
    var job by remember { mutableStateOf<Job?>(null) }

    DetailedUsedProductListingContent(
        userId,
        isGuest,
        item,
        onNavigateUpSlider,
        {

            item.let {
                job = scope.launch {
                    val selectedChatUser = viewModel.getChatUser(userId, it.user)
                    val selectedChatId = selectedChatUser.chatId

                    navigateUpChat(
                        selectedChatId,
                        it.user.userId,
                        it.user
                    )
                }
            }
        },
        {
            onNavigateUpSecondsOwnerProfile(item.user.userId)
        },
    ) {
        navHostController.popBackStack()
    }
}

@Composable
fun BookmarkedFeedUserDetailedUsedProductListingInfoScreen(
    navHostController: NavHostController,
    onNavigateUpSlider: (Int) -> Unit,
    navigateUpChat: (Int, Long, FeedUserProfileInfo) -> Unit,
    viewModel: SecondsOwnerProfileViewModel

) {


    val userId = viewModel.userId
    val signInMethod = viewModel.signInMethod
    val isGuest = signInMethod == "guest"

    val selectedItem by viewModel.selectedItem.collectAsState()


    val scope = rememberCoroutineScope()
    var job by remember { mutableStateOf<Job?>(null) }


    val item = selectedItem

    if (item !is UsedProductListing) return


    DetailedUsedProductListingContent(
        userId,
        isGuest,
        item,
        onNavigateUpSlider,
        {
            if (job?.isActive == true) {
                return@DetailedUsedProductListingContent
            }

            job = scope.launch {
                val selectedChatUser = viewModel.getChatUser(userId, item.user)
                val selectedChatId = selectedChatUser.chatId
                navigateUpChat(
                    selectedChatId,
                    item.user.userId,
                    item.user
                )
            }
        }
    ) {
        navHostController.popBackStack()
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailedUsedProductListingContent(
    userId: Long,
    isGuest: Boolean,
    selectedSeconds: UsedProductListing?,
    onNavigateUpSlider: (Int) -> Unit,
    onChatButtonClicked: () -> Unit,
    onUsedProductListingOwnerProfileClicked: () -> Unit = {},
    onPopBackStack: () -> Unit
) {

    val context = LocalContext.current


    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            SheetValue.Hidden,
            skipHiddenState = false
        )
    )

    val coroutineScope = rememberCoroutineScope()



    BackHandler(bottomSheetScaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
        coroutineScope.launch {
            bottomSheetScaffoldState.bottomSheetState.hide()
        }
    }


    BottomSheetScaffold(
        sheetDragHandle = null,
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
        sheetPeekHeight = 0.dp,
        sheetSwipeEnabled = true,
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
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            selectedSeconds?.let {
                DetailedUsedProductListingInfo(
                    userId,
                    it,
                    onNavigateUpSlider,
                    onUsedProductListingOwnerProfileClicked
                ) {
                    if (isGuest) {
                        coroutineScope.launch {
                            bottomSheetScaffoldState.bottomSheetState.expand()
                        }
                    } else {
                        onChatButtonClicked()
                    }

                }
            } ?: run {
                LoadingDetailedSecondsInfo()
            }
        }

    }
}

@Composable
private fun DetailedUsedProductListingInfo(
    userId: Long,
    item: UsedProductListing,
    onNavigateUpSlider: (Int) -> Unit,
    onUsedProductListingOwnerProfileClicked: () -> Unit,
    chatButtonClicked: () -> Unit
) {

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {


            item(key = "secondsImages-${item.productId}") {
                SecondsImagesSliderDetailedSecondsInfo(item.images, onNavigateUpSlider)
            }

            item(key = "secondsOwner-${item.user.userId}") {
                SecondsOwner(
                    "${item.user.firstName} ${item.user.lastName ?: ""}",
                    item.user.profilePicUrl,
                    "${item.country}/${item.state}",
                    item.user.isOnline,
                    onUsedProductListingOwnerProfileClicked
                )
            }

            item(key = "secondsDescription-${item.productId}") {
                SecondsDescription(
                    item.name,
                    item.description,
                    formatCurrency(
                        item.price,
                        item.priceUnit
                    )
                )
            }


        }
        if(item.user.userId != userId){
            Box(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    CallButton(onClick = {

                    }, modifier = Modifier.weight(1f))

                    SendMessageButton(chatButtonClicked, modifier = Modifier.weight(1f))
                }
            }
        }

    }


}

@Composable
private fun LoadingDetailedSecondsInfo() {

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 16.dp)
    ) {

        item(key = "secondsOwner-${0}") {


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
                            text = "Seconds owner name", // Replace with your data
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
                                text = "Seconds from and verified icon",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }

        // Image Slider
        item(key = "SecondsImages-${0}") {

            ShimmerBox {
                Spacer(modifier = Modifier.aspectRatio(16 / 9f))
            }
        }

        item(key = "secondsDescription-${0}") {
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
private fun SecondsOwner(
    secondsOwner: String,
    urlImage: String?,
    secondsFrom: String,
    isOnline: Boolean,
    onUsedProductListingOwnerProfileClicked: () -> Unit
) {

    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .noRippleClickable { onUsedProductListingOwnerProfileClicked() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(40.dp)) {
            AsyncImage(
                ImageRequest.Builder(context)
                    .data(urlImage)
                    .placeholder(R.drawable.user_placeholder)
                    .error(R.drawable.user_placeholder)
                    .build(),
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
                        )
                        .clip(CircleShape)
                )
            }

        }

        Spacer(modifier = Modifier.width(8.dp))

        Column {
            Text(
                text = secondsOwner,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium

            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = secondsFrom,
                    style = MaterialTheme.typography.bodyMedium

                )
                Image(
                    painter = painterResource(id = R.drawable.ic_verified_service),
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
private fun SecondsDescription(
    name: String,
    description: String,
    price: String
) {


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)

    ) {


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
private fun SecondsImagesSliderDetailedSecondsInfo(
    images: List<Image>,
    onImageClick: (Int) -> Unit,
) {

    val lifecycleOwner = LocalLifecycleOwner.current
    val pagerState = rememberPagerState(pageCount = { images.size })

    if (images.isNotEmpty()) {

        // Image Pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(MaterialTheme.colorScheme.surfaceContainerLow),
            pageSpacing = 8.dp
        ) { page ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        dropUnlessResumedV2(lifecycleOwner) {
                            onImageClick(page)
                        }
                    }
            ) {
                AsyncImage(
                    model = images[page].imageUrl,
                    contentDescription = "Product image ${page + 1}",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Page indicators (optional)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(images.size) { iteration ->
                val color = if (pagerState.currentPage == iteration)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)

                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }
    }
}

@Composable
private fun CallButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick, shape = RoundedCornerShape(8.dp), modifier = modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF25D366),
            contentColor = Color.White
        ),

    ) {
        Text("Call")
    }
}
