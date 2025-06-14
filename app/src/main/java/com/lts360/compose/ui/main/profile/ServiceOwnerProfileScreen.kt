package com.lts360.compose.ui.main.profile

import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import com.lts360.R
import com.lts360.api.models.service.FeedUserProfileInfo
import com.lts360.api.models.service.Service
import com.lts360.app.database.models.chat.ChatUser
import com.lts360.compose.ui.ShimmerBox
import com.lts360.compose.ui.auth.AuthActivity
import com.lts360.compose.ui.auth.ForceWelcomeScreen
import com.lts360.compose.ui.bookmarks.BookmarksViewModel
import com.lts360.compose.ui.services.ServiceOwnerProfileViewModel
import com.lts360.compose.ui.viewmodels.ServicesViewModel
import com.lts360.libs.ui.ShortToast
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


@Composable
fun ServiceOwnerProfileScreen(
    key: Int,
    onNavigateUpChat: (ChatUser, Int, Long) -> Unit,
    onNavigateUpDetailedService: (Int) -> Unit,
    onPopBackStack: () -> Unit,
    servicesViewModel: ServicesViewModel,
    viewModel: ServiceOwnerProfileViewModel
) {


    val userId = servicesViewModel.userId
    val selectedParentService by servicesViewModel.getServiceRepository(key).selectedItem.collectAsState()


    val scope = rememberCoroutineScope()
    var job by remember { mutableStateOf<Job?>(null) }

    ServiceOwnerProfile(
        selectedParentService,
        {
            if (job?.isActive == true) {
                return@ServiceOwnerProfile
            }

            job = scope.launch {
                selectedParentService?.let { nonNullSelectedService ->

                    val selectedChatUser =
                        servicesViewModel.getChatUser(userId, nonNullSelectedService.user)
                    val selectedChatId = selectedChatUser.chatId
                    onNavigateUpChat(
                        selectedChatUser,
                        selectedChatId,
                        nonNullSelectedService.user.userId)
                }
            }

        },
        {
            servicesViewModel.setNestedServiceOwnerProfileSelectedItem(key, it)
            onNavigateUpDetailedService(key)
        },
        onPopBackStack,
        viewModel
    )

}


@Composable
fun BookmarkedServiceOwnerProfileScreen(
    onNavigateUpChat: (Int, Long, FeedUserProfileInfo) -> Unit,
    onNavigateUpDetailedService: () -> Unit,
    onPopBackStack: () -> Unit,
    servicesViewModel: BookmarksViewModel,
    viewModel: ServiceOwnerProfileViewModel
) {

    val userId = servicesViewModel.userId
    val selectedParentService by servicesViewModel.selectedItem.collectAsState()
    val scope = rememberCoroutineScope()

    var job by remember { mutableStateOf<Job?>(null) }

    val item = selectedParentService

    if (item !is Service) return

    ServiceOwnerProfile(
        item, {

            item.let { nonNullSelectedService ->

                if (job?.isActive == true) {
                    return@ServiceOwnerProfile
                }

                job = scope.launch {
                    val selectedChatUser =
                        servicesViewModel.getChatUser(userId, nonNullSelectedService.user)
                    val selectedChatId = selectedChatUser.chatId

                    onNavigateUpChat(
                        selectedChatId,
                        nonNullSelectedService.user.userId,
                        nonNullSelectedService.user
                    )
                }

            }

        }, {
            onNavigateUpDetailedService()
        },
        onPopBackStack,
        viewModel
    )

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ServiceOwnerProfile(
    selectedParentService: Service?,
    onNavigateUpChat: () -> Unit,
    onNavigateUpDetailedService: (Service) -> Unit,
    onPopBackStack: () -> Unit,
    viewModel: ServiceOwnerProfileViewModel,
) {


    val userId = viewModel.userId
    val signInMethod = viewModel.signInMethod

    val context = LocalContext.current

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


    val sheetState = rememberModalBottomSheetState()

    var bottomSheetState by rememberSaveable { mutableStateOf(false) }

    val selectedItem by viewModel.selectedItem.collectAsState()

    val createdServices = remember(selectedParentService) { selectedParentService?.createdServices }

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
        sheetPeekHeight = 0.dp, // Default height when sheet is collapsed
        sheetSwipeEnabled = true, // Allow gestures to hide/show bottom sheet
//            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

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

            selectedParentService?.let { nonNullSelectedService ->

                ServiceOwnerProfile(
                    createdServices ?: emptyList(),
                    nonNullSelectedService.user,
                    {
                        if (nonNullSelectedService.user.userId != userId) {
                            ProfileInfoChatUser(
                                nonNullSelectedService.user.profilePicUrl,
                                nonNullSelectedService.user.isOnline
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
                        viewModel.setSelectedItem(it)
                        onNavigateUpDetailedService(it)
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
                                                ShortToast(context,"No app to open")
                                            }

                                        }

                                    }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically) {


                                Icon(
                                    Icons.Default.Share,
                                    contentDescription = "Share",
                                    modifier = Modifier.size(24.dp),
                                )

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
fun LoadingServiceOwnerProfileScreen() {

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {


            ShimmerBox(
                modifier = Modifier
                    .size(100.dp)

                    .padding(4.dp)
                    .clip(CircleShape),
            )


            Spacer(modifier = Modifier.width(16.dp))

            // Name, Email, and Joined Date
            Column {
                ShimmerBox {
                    Text(
                        text = "User full name",
                        style = MaterialTheme.typography.headlineSmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = Color.Transparent

                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                ShimmerBox {
                    Text(
                        text = "useremail@email.com",
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = Color.Transparent

                    )
                }


                Spacer(modifier = Modifier.height(4.dp))
                ShimmerBox {
                    Text(
                        style = MaterialTheme.typography.bodyMedium,
                        text = "Joined at year",
                        color = Color.Transparent
                    )
                }

            }
        }

    }
}


@Composable
private fun ServiceOwnerProfile(
    userServices: List<Service>,
    userProfile: FeedUserProfileInfo,
    onChatClick: @Composable BoxScope.() -> Unit,
    onNavigateUpDetailedService: (Service) -> Unit,
    onOptionItemClick: (Service) -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            item {
                //Profile Header
                ProfilePicUrlHeader(userProfile)
            }

            item {
                // About Section
                userProfile.about?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    ProfileAboutSection(userProfile.about)
                }
            }


            if (userServices.isNotEmpty()) {
                item {
                    Text(
                        text = "Available Services",
                        style = MaterialTheme.typography.titleMedium
                    )
                }


                items(userServices) { service ->
                    ProfileServicesSection(
                        service.thumbnail?.imageUrl,
                        service.title,
                        service.shortDescription,
                        onItemClick = { onNavigateUpDetailedService(service) },
                        onOptionItemClick = { onOptionItemClick(service) }
                    )
                }
            }
        }

        onChatClick()

    }

}