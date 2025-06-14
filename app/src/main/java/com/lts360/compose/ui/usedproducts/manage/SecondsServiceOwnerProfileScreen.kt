package com.lts360.compose.ui.usedproducts.manage

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import com.lts360.R
import com.lts360.api.models.service.FeedUserProfileInfo
import com.lts360.api.models.service.UsedProductListing
import com.lts360.app.database.models.chat.ChatUser
import com.lts360.compose.ui.auth.AuthActivity
import com.lts360.compose.ui.auth.ForceWelcomeScreen
import com.lts360.compose.ui.bookmarks.BookmarksViewModel
import com.lts360.compose.ui.main.profile.LoadingServiceOwnerProfileScreen
import com.lts360.compose.ui.main.profile.ProfileAboutSection
import com.lts360.compose.ui.main.profile.ProfileInfoChatUser
import com.lts360.compose.ui.main.profile.ProfilePicUrlHeader
import com.lts360.compose.ui.main.profile.ProfileSecondsSection
import com.lts360.compose.ui.main.viewmodels.SecondsViewmodel
import com.lts360.compose.ui.usedproducts.SecondsOwnerProfileViewModel
import com.lts360.libs.ui.ShortToast
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Composable
fun SecondsServiceOwnerProfileScreen(
    key: Int,
    onNavigateUpChat: (ChatUser, Int, Long) -> Unit,
    onNavigateUpDetailedSeconds: (Int, UsedProductListing) -> Unit,
    onPopBackStack: () -> Unit,
    secondsViewModel: SecondsViewmodel,
    secondsOwnerProfileViewModel: SecondsOwnerProfileViewModel
) {


    val userId = secondsViewModel.userId
    val selectedParentService by secondsViewModel.getSecondsRepository(key).selectedItem.collectAsState()


    val scope = rememberCoroutineScope()
    var job by remember { mutableStateOf<Job?>(null) }

    SecondsOwnerProfileScreenContent(
        selectedParentService,
        {
            if (job?.isActive == true) {
                return@SecondsOwnerProfileScreenContent
            }

            job = scope.launch {
                selectedParentService?.let { nonNullSelectedService ->

                    val selectedChatUser =
                        secondsViewModel.getChatUser(userId, nonNullSelectedService.user)
                    val selectedChatId = selectedChatUser.chatId
                    onNavigateUpChat(
                        selectedChatUser,
                        selectedChatId,
                        nonNullSelectedService.user.userId
                    )
                }
            }

        },
        {
            secondsViewModel.setNestedSecondsOwnerProfileSelectedItem(key, it)
            onNavigateUpDetailedSeconds(key, it)
        },
        onPopBackStack,
        secondsOwnerProfileViewModel
    )

}


@Composable
fun BookmarkedSecondsOwnerProfileScreen(
    onNavigateUpChat: (Int, Long, FeedUserProfileInfo) -> Unit,
    onNavigateUpDetailedService: () -> Unit,
    onPopBackStack: () -> Unit,
    servicesViewModel: BookmarksViewModel,
    secondsOwnerProfileViewModel: SecondsOwnerProfileViewModel
) {

    val userId = servicesViewModel.userId
    val selectedParentService by servicesViewModel.selectedItem.collectAsState()
    val scope = rememberCoroutineScope()

    var job by remember { mutableStateOf<Job?>(null) }


    val item = selectedParentService

    if (item !is UsedProductListing) return

    SecondsOwnerProfileScreenContent(
        item, {

            if (job?.isActive == true) {
                return@SecondsOwnerProfileScreenContent
            }

            job = scope.launch {
                val selectedChatUser =
                    servicesViewModel.getChatUser(userId, item.user)
                val selectedChatId = selectedChatUser.chatId

                onNavigateUpChat(
                    selectedChatId,
                    item.user.userId,
                    item.user
                )
            }

        }, {
            onNavigateUpDetailedService()
        },
        onPopBackStack,
        secondsOwnerProfileViewModel
    )

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SecondsOwnerProfileScreenContent(
    selectedParentService: UsedProductListing?,
    onNavigateUpChat: () -> Unit,
    onNavigateUpDetailedSeconds: (UsedProductListing) -> Unit,
    onPopBackStack: () -> Unit,
    viewModel: SecondsOwnerProfileViewModel,
) {

    val userId = viewModel.userId

    val signInMethod = viewModel.signInMethod

    val coroutineScope = rememberCoroutineScope()


    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            SheetValue.Hidden,
            skipHiddenState = false
        )
    )

    BackHandler(bottomSheetScaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
        coroutineScope.launch {
            bottomSheetScaffoldState.bottomSheetState.hide()
        }
    }


    val context = LocalContext.current


    val sheetState = rememberModalBottomSheetState()

    // Observe the state from ViewModel
    var bottomSheetState by rememberSaveable { mutableStateOf(false) }

    val selectedItem by viewModel.selectedItem.collectAsState()

    val createdServices =
        remember(selectedParentService) { selectedParentService?.createdUsedProductListings }

    LaunchedEffect(bottomSheetState) {
        if (bottomSheetState) {
            sheetState.expand()
        } else {
            sheetState.hide()
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
                            })

                }) {
                coroutineScope.launch {
                    bottomSheetScaffoldState.bottomSheetState.hide()
                }
            }
        },
        sheetPeekHeight = 0.dp,
        sheetSwipeEnabled = true,
    ) { innerPadding ->

        Column(modifier = Modifier.padding(innerPadding)) {

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
                    Text(text = "Profile", style = MaterialTheme.typography.titleMedium)
                }
            )

            selectedParentService?.let { nonNullSelectedParentService ->

                SecondsOwnerProfile(
                    createdServices ?: emptyList(),
                    nonNullSelectedParentService.user,
                    {
                        if (nonNullSelectedParentService.user.userId != userId) {
                            ProfileInfoChatUser(
                                nonNullSelectedParentService.user.profilePicUrl,
                                nonNullSelectedParentService.user.isOnline
                            ) {

                                if (signInMethod == "guest") {
                                    coroutineScope.launch {
                                        bottomSheetScaffoldState.bottomSheetState.expand()
                                    }
                                } else {
                                    onNavigateUpChat()
                                }
                            }
                        }
                    }, {
                        onNavigateUpDetailedSeconds(it)
                    }, {
                        viewModel.setSelectedItem(it)
                        bottomSheetState = true

                    })

            } ?: run {
                LoadingServiceOwnerProfileScreen()
            }

            if (bottomSheetState) {
                ModalBottomSheet(
                    modifier = Modifier
                        .safeDrawingPadding(),
                    onDismissRequest = {
                        bottomSheetState = false
                    },
                    shape = RectangleShape,
                    sheetState = sheetState,
                    dragHandle = null

                ) {

                    selectedItem?.let { nonNullSelectedItem ->

                        viewModel.setSelectedItem(nonNullSelectedItem.copy(isBookmarked = nonNullSelectedItem.isBookmarked))

                        Column(modifier = Modifier.fillMaxWidth()) {

                            if (signInMethod != "guest") {
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
                                                            nonNullSelectedItem.productId,
                                                            false
                                                        )
                                                        ShortToast(
                                                            context = context,
                                                            message = "Bookmark removed"
                                                        )

                                                    }, onError = {
                                                        viewModel.setSelectedItem(
                                                            nonNullSelectedItem.copy(isBookmarked = true)
                                                        )
                                                        ShortToast(
                                                            context = context,
                                                            message = "Something wrong"
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
                                                            nonNullSelectedItem.copy(isBookmarked = true)
                                                        )
                                                        viewModel.directUpdateServiceIsBookMarked(
                                                            nonNullSelectedItem.productId,
                                                            true
                                                        )

                                                        ShortToast(
                                                            context = context,
                                                            message = "Bookmarked"
                                                        )

                                                    },
                                                    onError = {
                                                        ShortToast(
                                                            context = context,
                                                            message = "Something wrong"
                                                        )

                                                        viewModel.setSelectedItem(
                                                            nonNullSelectedItem.copy(isBookmarked = false)
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
                                                ShortToast(
                                                    context = context,
                                                    message = "No app to open"
                                                )
                                            }

                                        }

                                    }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically) {

                                Icon(
                                    Icons.Default.Share,
                                    contentDescription = "Share",
                                    modifier = Modifier.size(24.dp)
                                )

                                Text(
                                    text = "Share",
                                    modifier = Modifier.padding(horizontal = 4.dp)
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
private fun SecondsOwnerProfile(
    userServices: List<UsedProductListing>,
    userProfile: FeedUserProfileInfo,
    onChatClick: @Composable BoxScope.() -> Unit,
    onNavigateUpDetailedService: (UsedProductListing) -> Unit,
    onOptionItemClick: (UsedProductListing) -> Unit
) {


    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(
                top = 16.dp,
                start = 16.dp,
                end = 16.dp
            )
        ) {

            item {
                ProfilePicUrlHeader(userProfile)
            }

            item {
                userProfile.about?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    ProfileAboutSection(userProfile.about)
                }
            }


            if (userServices.isNotEmpty()) {
                item {
                    // Available Services Section
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Available Services",
                        style = MaterialTheme.typography.titleMedium
                    )
                }


                items(userServices) { service ->
                    Spacer(modifier = Modifier.height(8.dp))
                    ProfileSecondsSection(
                        if (service.images.isNotEmpty()) service.images[0].imageUrl else
                            null,
                        service.name,
                        service.description,
                        onItemClick = { onNavigateUpDetailedService(service) },
                        onOptionItemClick = { onOptionItemClick(service) }
                    )
                }
            }

        }

        onChatClick()
    }

}