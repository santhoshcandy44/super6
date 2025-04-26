package com.lts360.compose.ui.localjobs

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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.lts360.app.database.models.chat.ChatUser
import com.lts360.compose.dropUnlessResumedV2
import com.lts360.compose.ui.ShimmerBox
import com.lts360.compose.ui.auth.AuthActivity
import com.lts360.compose.ui.auth.ForceWelcomeScreen
import com.lts360.compose.ui.bookmarks.BookmarksActivity
import com.lts360.compose.ui.bookmarks.BookmarksViewModel
import com.lts360.compose.ui.localjobs.models.LocalJob
import com.lts360.compose.ui.localjobs.models.getMaritalStatusLabel
import com.lts360.compose.ui.services.SendMessageButton
import com.lts360.compose.ui.utils.FormatterUtils.formatCurrency
import com.lts360.compose.utils.ExpandableText
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Composable
fun DetailedLocalJobScreen(
    navHostController: NavHostController,
    onNavigateUpSlider: (Int) -> Unit,
    navigateUpChat: (ChatUser, Int, Long) -> Unit,
    viewModel: LocalJobsViewmodel
) {

    val userId = viewModel.userId
    val isGuest = viewModel.isGuest
    val selectedItem by viewModel.selectedItem.collectAsState()


    val scope = rememberCoroutineScope()
    var job by remember { mutableStateOf<Job?>(null) }

    DetailedLocalJobContent(
        userId,
        selectedItem,
        onNavigateUpSlider,
        {
            selectedItem?.let {
                OutlinedButton(
                    onClick = dropUnlessResumed {

                        if (job?.isActive == true) {
                            return@dropUnlessResumed
                        }

                        job = scope.launch {
                            val selectedChatUser = viewModel.getChatUser(userId, it.user)
                            val selectedChatId = selectedChatUser.chatId

                            if (isGuest) {
                                it()
                            } else {
                                navigateUpChat(
                                    selectedChatUser,
                                    selectedChatId,
                                    it.user.userId
                                )
                            }
                        }

                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.onSurface,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = "Send message", color = MaterialTheme.colorScheme.surface)
                }
            }
        }
    ) {
        navHostController.popBackStack()
    }

}


@Composable
fun BookmarkedDetailedLocalJobInfoScreen(
    navHostController: NavHostController,
    onNavigateUpSlider: (Int) -> Unit,
    navigateUpChat: (Int, Long, FeedUserProfileInfo) -> Unit,
    viewModel: BookmarksViewModel,
) {

    val userId = viewModel.userId
    val signInMethod = viewModel.signInMethod

    val selectedItem by viewModel.selectedItem.collectAsState()

    val item = selectedItem

    if (item !is LocalJob) return

    val scope = rememberCoroutineScope()
    var job by remember { mutableStateOf<Job?>(null) }

    DetailedLocalJobContent(
        userId,
        item,
        onNavigateUpSlider,
        {

            item.let {
                OutlinedButton(
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
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.onSurface,
                    ),
                    shape = RectangleShape,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = "Send message", color = MaterialTheme.colorScheme.surface)
                }
            }
        }
    ) {
        navHostController.popBackStack()
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailedLocalJobContent(
    userId: Long,
    item: LocalJob?,
    onNavigateUpSlider: (Int) -> Unit,
    onChatButtonClicked: @Composable (() -> Unit) -> Unit,
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
                        text = "Local Job Info",
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
            item?.let {
                DetailedLocalJobInfo(
                    userId,
                    it,
                    onNavigateUpSlider
                ) {
                    onChatButtonClicked {
                        coroutineScope.launch {
                            bottomSheetScaffoldState.bottomSheetState.expand()
                        }
                    }
                }
            } ?: run {
                LoadingDetailedLocalJobInfo()
            }
        }

    }
}


@Composable
private fun DetailedLocalJobInfo(
    userId: Long,
    item: LocalJob,
    onNavigateUpSlider: (Int) -> Unit,
    chatButtonClicked: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            item(key = "images-${item.localJobId}") {
                SecondsImagesSliderDetailedSecondsInfo(item.images, onNavigateUpSlider)
            }

            item(key = "owner-${item.user.userId}") {
                LocalJobOwner(
                    "${item.user.firstName} ${item.user.lastName ?: ""}",
                    item.user.profilePicUrl,
                    "${item.country ?: ""}/${item.state ?: ""}",
                    item.user.isOnline
                )
            }

            item(key = "description-${item.localJobId}") {
                LocalJobDescription(
                    title = item.title,
                    description = item.description,
                    salary = "${formatCurrency(item.salaryMin.toDouble(), item.salaryUnit)} - ${formatCurrency(item.salaryMax.toDouble(), item.salaryUnit)}"
                )
            }

            item {
                InfoRow(label = "Company", value = item.company)
                InfoRow(label = "Age Range", value = "${item.ageMin} - ${item.ageMax}")
                InfoRow(
                    label = "Marital Status", value =
                        item.maritalStatuses.joinToString(", ") {
                            item.getMaritalStatusLabel(
                                it
                            )
                        }
                )
            }
        }

        if (userId != item.user.userId) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                SendMessageButton(chatButtonClicked)
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun LoadingDetailedLocalJobInfo() {

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 16.dp)
    ) {

        item(key = "localJobOwner-${0}") {

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
                            text = "Seconds owner name",
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

        item(key = "LocalJobImages-${0}") {

            ShimmerBox {
                Spacer(modifier = Modifier.aspectRatio(16 / 9f))
            }
        }

        item(key = "LocalJobDescription-${0}") {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {

                ShimmerBox {
                    Text(
                        color = Color.Transparent,
                        modifier = Modifier.fillMaxWidth(),
                        text = "Short description",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                ShimmerBox {
                    Text(
                        color = Color.Transparent,
                        modifier = Modifier.fillMaxWidth(0.6f),
                        text = "Long description",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }


    }

}

@Composable
private fun LocalJobOwner(
    secondsOwner: String,
    urlImage: String?,
    secondsFrom: String,
    isOnline: Boolean
) {

    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
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
private fun LocalJobDescription(
    title: String,
    description: String,
    salary: String
) {


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)

    ) {


        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))


        ExpandableText(
            description,
            style = MaterialTheme.typography.bodyMedium,
            showMoreStyle = SpanStyle(color = Color(0xFF4399FF)),
            showLessStyle = SpanStyle(color = Color(0xFF4399FF)),
            textModifier = Modifier.wrapContentSize()
        )


        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Salary", modifier = Modifier.padding(vertical = 8.dp))
            Text(
                text = salary,
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
