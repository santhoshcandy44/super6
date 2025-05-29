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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.lts360.compose.ui.auth.LoadingDialog
import com.lts360.compose.ui.bookmarks.BookmarksViewModel
import com.lts360.compose.ui.localjobs.models.LocalJob
import com.lts360.compose.ui.localjobs.models.getMaritalStatusLabel
import com.lts360.compose.ui.profile.EditPhoneBottomSheet
import com.lts360.compose.ui.services.SendMessageButton
import com.lts360.compose.ui.utils.FormatterUtils.formatCurrency
import com.lts360.compose.utils.ExpandableText
import com.lts360.libs.ui.ShortToast
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Composable
fun DetailedLocalJobScreen(
    navHostController: NavHostController,
    key: Int,
    onNavigateUpSlider: (Int) -> Unit,
    navigateUpChat: (ChatUser, Int, Long) -> Unit,
    viewModel: LocalJobsViewmodel,

    ) {

    val userId = viewModel.userId
    val isGuest = viewModel.isGuest

    val isPhoneNumberVerified by viewModel.isPhoneNumberVerified.collectAsState()

    val isDontAskAgainChecked by viewModel.isDontAskAgainChecked.collectAsState(initial = true)
    val selectedItem by viewModel.getLocalJobsRepository(key).selectedItem.collectAsState()
    val isApplying by viewModel.isApplying.collectAsState()

    val scope = rememberCoroutineScope()
    var job by remember { mutableStateOf<Job?>(null) }


    val context = LocalContext.current

    DetailedLocalJobContent(
        userId,
        isGuest,
        isPhoneNumberVerified,
        isDontAskAgainChecked,
        selectedItem,
        isApplying,
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
            navHostController.popBackStack()
        },
        {
            viewModel.setLocalJobPersonalInfoPromptIsDontAskAgainChecked(it)
        },
        {
            selectedItem?.let { nonNullSelectedItem ->
                viewModel.onApplyLocalJob(key, userId, nonNullSelectedItem.localJobId, {
                    viewModel.updateLocalJobIsApplied(key, nonNullSelectedItem.localJobId)
                    ShortToast(context, it)
                }) {
                    ShortToast(context, it)
                }
            }
        }
    )

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
    val isGuest = signInMethod == "guest"
    val selectedItem by viewModel.selectedItem.collectAsState()
    val isPhoneNumberVerified by viewModel.isPhoneNumberVerified.collectAsState()
    val isDontAskAgainChecked by viewModel.isDontAskAgainChecked.collectAsState(initial = true)
    val isApplying by viewModel.isApplying.collectAsState()

    val item = selectedItem

    if (item !is LocalJob) return

    val scope = rememberCoroutineScope()
    var job by remember { mutableStateOf<Job?>(null) }

    val context = LocalContext.current

    DetailedLocalJobContent(
        userId,
        isGuest,
        isPhoneNumberVerified,
        isDontAskAgainChecked,
        item,
        isApplying,
        onNavigateUpSlider,
        {
            item.let {
                if (job?.isActive == true) {
                    return@let
                }
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
            navHostController.popBackStack()
        },
        {
            viewModel.setLocalJobPersonalInfoPromptIsDontAskAgainChecked(it)
        },
        {
            viewModel.onApplyLocalJob(userId, item.localJobId, {
                ShortToast(context, it)
            }) {
                ShortToast(context, it)
            }
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailedLocalJobContent(
    userId: Long,
    isGuest: Boolean,
    isPhoneNumberVerified: Boolean,
    isDontAskAgainChecked: Boolean,
    item: LocalJob?,
    isApplying: Boolean,
    onNavigateUpSlider: (Int) -> Unit,
    onChatButtonClick: () -> Unit,
    onPopBackStack: () -> Unit,
    onDontAskAgainCheckedChanged: (Boolean) -> Unit,
    onApplyClick: () -> Unit
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

    Box(modifier = Modifier.fillMaxSize()) {
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
                        isPhoneNumberVerified,
                        isDontAskAgainChecked,
                        it,
                        onNavigateUpSlider,
                        {
                            if (isGuest) {
                                coroutineScope.launch {
                                    bottomSheetScaffoldState.bottomSheetState.expand()
                                }
                            } else {
                                onChatButtonClick()
                            }
                        },
                        onDontAskAgainCheckedChanged,
                        onApplyClick
                    )
                } ?: run {
                    LoadingDetailedLocalJobInfo()
                }
            }

        }
        if (isApplying) {
            LoadingDialog()
        }
    }


}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailedLocalJobInfo(
    userId: Long,
    isPhoneNumberVerified: Boolean,
    isDontAskAgainChecked: Boolean,
    item: LocalJob,
    onNavigateUpSlider: (Int) -> Unit,
    onChatButtonClick: () -> Unit,
    onDontAskAgainCheckedChanged: (Boolean) -> Unit,
    onApplyClick: () -> Unit
) {

    var bottomProfileInfoShareState by remember { mutableStateOf(false) }
    var bottomPhoneNumberVerifyState by remember { mutableStateOf(false) }

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
                    salary = if (item.salaryMax != -1)
                        "${
                            formatCurrency(
                                item.salaryMin.toDouble(),
                                item.salaryUnit
                            )
                        } - ${formatCurrency(item.salaryMax.toDouble(), item.salaryUnit)}"
                    else
                        formatCurrency(item.salaryMin.toDouble(), item.salaryUnit)
                )
            }

            item {
                InfoCard(label = "Company", value = item.company)
                InfoCard(label = "Age Range", value = "${item.ageMin} - ${item.ageMax}")
                InfoCard(
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

            if (item.isApplied) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {


                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = Color.Cyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            "You have already applied for this job. The job poster will review your application shortly.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    SendMessageButton(onChatButtonClick)
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    ApplyButton(onClick = {
                        if (!isPhoneNumberVerified)
                            bottomPhoneNumberVerifyState = true
                        else
                            if (!isDontAskAgainChecked) {
                                bottomProfileInfoShareState = true
                            } else {
                                onApplyClick()
                            }
                    }, modifier = Modifier.weight(1f))

                    SendMessageButton(onChatButtonClick, modifier = Modifier.weight(1f))
                }
            }

        }





        if (bottomProfileInfoShareState) {
            LocalJobApplyPromptSheet(
                isDontAskAgainChecked = isDontAskAgainChecked,
                onApplyClick = {
                    bottomProfileInfoShareState = false
                    onApplyClick()
                }, onDismiss = {
                    bottomProfileInfoShareState = false
                }, onDontAskAgainCheckedChanged = onDontAskAgainCheckedChanged
            )
        }

        if (bottomPhoneNumberVerifyState) {
            EditPhoneBottomSheet(onVerifyClick = {

            }, onDismiss = {
                bottomPhoneNumberVerifyState = false
            })
        }


    }
}

@Composable
fun InfoCard(label: String, value: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
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
private fun ApplyButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick, shape = RoundedCornerShape(8.dp), modifier = modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF25D366),
            contentColor = Color.White
        )
    ) {
        Text("Apply")
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalJobApplyPromptSheet(
    onApplyClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    isDontAskAgainChecked: Boolean,
    onDontAskAgainCheckedChanged: (Boolean) -> Unit
) {

    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()



    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .wrapContentSize()
                .padding(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        "Attention", style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(8.dp)
                    )

                    Text(text = "When applying the local job profile info will be shared to the publisher.")

                    ListItem(
                        modifier = Modifier
                            .fillMaxWidth(),
                        headlineContent = {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(text = "Basic Info")
                                Text(
                                    text = "First Name, Last Name, Email, Profile Pic, Location",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.LightGray
                                )
                            }
                        },
                        leadingContent = {
                            Image(
                                painterResource(R.drawable.ic_info_phone),
                                contentDescription = null,
                                modifier = Modifier.size(48.dp)
                            )
                        },
                    )

                    ListItem(
                        modifier = Modifier
                            .fillMaxWidth(),
                        headlineContent = {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(text = "Contact Info")
                                Text(
                                    text = "Phone Number, Email",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.LightGray
                                )
                            }
                        },
                        leadingContent = {
                            Image(
                                painterResource(R.drawable.ic_info_personal_info),
                                contentDescription = null,
                                modifier = Modifier.size(48.dp)
                            )
                        },
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text("Don't ask again")
                        Checkbox(
                            isDontAskAgainChecked,
                            onDontAskAgainCheckedChanged,
                        )
                    }

                    Button(
                        onClick = onApplyClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(
                                0xFF25D366
                            )
                        ),
                        modifier = Modifier
                            .fillMaxWidth(),
                        shape = RectangleShape
                    ) {
                        Text("Continue", color = Color.White)
                    }

                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                sheetState.hide()
                                onDismiss()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth(),
                        shape = RectangleShape
                    ) {
                        Text("Cancel")
                    }

                }

            }
        }
    }

}

