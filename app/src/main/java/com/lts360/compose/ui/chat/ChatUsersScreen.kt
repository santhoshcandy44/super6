package com.lts360.compose.ui.chat

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.dropUnlessResumed
import coil3.compose.AsyncImage
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import com.lts360.R
import com.lts360.app.database.models.chat.ChatMessageStatus
import com.lts360.app.database.models.chat.ChatUser
import com.lts360.app.workers.chat.utils.lastMessageTimestamp
import com.lts360.compose.ui.chat.viewmodels.ChatListViewModel
import com.lts360.compose.ui.chat.viewmodels.UserState


@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class,
)
@Composable
fun ChatUsersScreen(
    onNavigateUpChat: (ChatUser, Int, Long) -> Unit,
    viewModel: ChatListViewModel
) {

    val userStates by viewModel.userStates.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val lazyListState = rememberLazyListState()


    val lastLoadedChatId by viewModel.lastLoadedChatId.collectAsState()


    LaunchedEffect(userStates) {
        snapshotFlow { lazyListState.firstVisibleItemIndex }
            .collect { _ ->

                val lastVisibleItem = lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()

                if (lastVisibleItem != null) {

                    val pageSize =
                        if (lastLoadedChatId != -1) viewModel.CHAT_LIST_SIZE_AFTER_INITIAL_LOAD else viewModel.CHAT_LIST_SIZE

                    val isNearEnd =
                        lastVisibleItem.index >= if (lastLoadedChatId != -1) userStates.size - viewModel.CHAT_LIST_SIZE_AFTER_INITIAL_LOAD else
                            userStates.size - pageSize

                    if (isNearEnd) {

                        val lastUser = userStates.lastOrNull()

                        val lastChatId = lastUser?.chatUser?.chatId

                        val size =
                            if (lastLoadedChatId != -1) pageSize else viewModel.CHAT_LIST_SIZE_AFTER_INITIAL_LOAD

                        if (lastChatId != null && (lastLoadedChatId == -1 || lastChatId > lastLoadedChatId)) {
                            viewModel.updateLastLoadedChatId(lastChatId)
                            viewModel.loadChatUsers(lastUser.chatUser, size)
                        }
                    }
                }


            }

    }

    val context = LocalContext.current

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            TopAppBar(
                title = {
                    Text(
                        "Chats",
                        style = MaterialTheme.typography.titleMedium
                    )
                })

            Box(modifier = Modifier.fillMaxSize()) {

                if (isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .align(Alignment.Center)
                    )
                } else {

                    SharedTransitionLayout(modifier = Modifier.fillMaxSize()) {
                        var isExpanded by remember { mutableStateOf(false) }
                        var transitionItem by remember { mutableStateOf<UserState?>(null) }

                        BackHandler(isExpanded) {
                            isExpanded = false
                        }

                        AnimatedContent(
                            targetState = isExpanded, label = "",
                            transitionSpec = {
                                ContentTransform(
                                    targetContentEnter = fadeIn(),
                                    initialContentExit = fadeOut(),
                                    sizeTransform = SizeTransform { _, _ ->
                                        tween(durationMillis = 0)
                                    }
                                )
                            }) { target ->

                            if (!target) {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    state = lazyListState
                                ) {
                                    if (userStates.isEmpty()) {
                                        item {
                                            Box(modifier = Modifier.fillParentMaxSize()) {
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    modifier = Modifier.align(Alignment.Center)

                                                ) {
                                                    Image(
                                                        painter = painterResource(R.drawable.no_chats),
                                                        contentDescription = "Image from drawable",
                                                        modifier = Modifier
                                                            .sizeIn(
                                                                maxWidth = 200.dp,
                                                                maxHeight = 200.dp
                                                            )
                                                    )

                                                    Spacer(Modifier.height(16.dp))
                                                    Text(text = "No, recent chats")

                                                }
                                            }
                                        }
                                    } else {
                                        itemsIndexed(
                                            userStates,
                                            key = { _, userState -> userState.chatUser.chatId }
                                        ) { _, userState ->

                                            val chatRecipientUser = userState.chatUser
                                            val userName = listOfNotNull(
                                                userState.chatUser.userProfile.firstName.takeIf { it.isNotBlank() },
                                                userState.chatUser.userProfile.lastName?.takeIf { it.isNotBlank() }
                                            ).joinToString(" ")
                                            val profileImageUrl96By96 =
                                                userState.chatUser.userProfile.profilePicUrl96By96

                                            val lastMessage = userState.lastMessage
                                            val unreadCount = userState.unreadCount

                                            val isOnline = userState.onlineStatus
                                            val typingStatus = userState.typingStatus


                                            Card(
                                                onClick = dropUnlessResumed {
                                                    viewModel.updateSelectedChatId(userState)
                                                    onNavigateUpChat(
                                                        userState.chatUser,
                                                        chatRecipientUser.chatId,
                                                        chatRecipientUser.recipientId
                                                    )
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth(),
                                                elevation = CardDefaults.cardElevation(0.dp),
                                                shape = RectangleShape,
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(
                                                            horizontal = 8.dp,
                                                            vertical = 16.dp
                                                        ),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Box(modifier = Modifier.wrapContentSize()) {

                                                        AsyncImage(
                                                            ImageRequest.Builder(context)
                                                                .data(profileImageUrl96By96)
                                                                .placeholder(R.drawable.user_placeholder)
                                                                .error(R.drawable.user_placeholder)
                                                                .crossfade(true)
                                                                .build(),
                                                            imageLoader = viewModel.chatUsersProfileImageLoader,
                                                            contentDescription = "User Profile Image",
                                                            modifier = Modifier
                                                                .size(50.dp)
                                                                .sharedBounds(
                                                                    rememberSharedContentState(
                                                                        key = "image-${userState.chatUser.chatId}"
                                                                    ),
                                                                    animatedVisibilityScope = this@AnimatedContent,
                                                                )
                                                                .clip(CircleShape)
                                                                .clickable {
                                                                    transitionItem = userState
                                                                    isExpanded = !isExpanded
                                                                },
                                                            contentScale = ContentScale.Crop,
                                                        )


                                                        if (isOnline) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(10.dp)
                                                                    .align(Alignment.BottomEnd)
                                                                    .background(
                                                                        Color(0xFF7CFC00),
                                                                        CircleShape
                                                                    )
                                                            )
                                                        }
                                                    }

                                                    Spacer(modifier = Modifier.width(8.dp))

                                                    Column(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(horizontal = 8.dp)
                                                    ) {
                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .padding(horizontal = 8.dp),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Text(
                                                                text = userName,
                                                                fontSize = 14.sp,
                                                                color = MaterialTheme.colorScheme.onSurface,
                                                                modifier = Modifier.weight(1f)
                                                            )

                                                            Text(text = lastMessage?.timestamp?.let {
                                                                lastMessageTimestamp(
                                                                    it
                                                                )
                                                            } ?: "",
                                                                fontSize = 14.sp,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                                modifier = Modifier.align(Alignment.CenterVertically)
                                                            )
                                                        }

                                                        Spacer(modifier = Modifier.height(8.dp))

                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .padding(horizontal = 8.dp),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {

                                                            Row(
                                                                modifier = Modifier.weight(1f),
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {

                                                                if (typingStatus) {
                                                                    Text(
                                                                        text = "typing...",
                                                                        fontSize = 14.sp,
                                                                        color = Color(0xFF9747ff),
                                                                        maxLines = 1,
                                                                        overflow = TextOverflow.Ellipsis,
                                                                        modifier = Modifier.weight(
                                                                            1f
                                                                        )
                                                                    )
                                                                } else {
                                                                    lastMessage?.let { nonNullLastMessage ->
                                                                        Row(
                                                                            modifier = Modifier.fillMaxWidth(),
                                                                            verticalAlignment = Alignment.CenterVertically
                                                                        ) {
                                                                            Text(
                                                                                text = nonNullLastMessage.content,
                                                                                fontSize = 14.sp,
                                                                                maxLines = 1,
                                                                                overflow = TextOverflow.Ellipsis,
                                                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                                                modifier = Modifier.weight(
                                                                                    1f,
                                                                                    false
                                                                                )
                                                                            )

                                                                            if (nonNullLastMessage.senderId != chatRecipientUser.recipientId) {

                                                                                when (nonNullLastMessage.status) {
                                                                                    ChatMessageStatus.SENDING,
                                                                                    ChatMessageStatus.QUEUED,
                                                                                    ChatMessageStatus.QUEUED_MEDIA,
                                                                                    ChatMessageStatus.QUEUED_MEDIA_RETRY,
                                                                                        -> {
                                                                                        Icon(
                                                                                            painterResource(
                                                                                                R.drawable.ic_message_pending
                                                                                            ),
                                                                                            contentDescription = "Sending",
                                                                                            modifier = Modifier.size(
                                                                                                16.dp
                                                                                            )
                                                                                        )
                                                                                    }

                                                                                    ChatMessageStatus.SENT -> {
                                                                                        Icon(
                                                                                            imageVector = Icons.Filled.Check,
                                                                                            contentDescription = "Message Sent",
                                                                                            modifier = Modifier
                                                                                                .size(
                                                                                                    16.dp
                                                                                                )
                                                                                        )
                                                                                    }


                                                                                    ChatMessageStatus.DELIVERED -> {
                                                                                        Box {
                                                                                            Icon(
                                                                                                imageVector = Icons.Filled.Check,
                                                                                                contentDescription = "Message Delivered",
                                                                                                modifier = Modifier.size(
                                                                                                    16.dp
                                                                                                )
                                                                                            )
                                                                                            Icon(
                                                                                                imageVector = Icons.Filled.Check,
                                                                                                contentDescription = "Message Delivered",
                                                                                                modifier = Modifier
                                                                                                    .size(
                                                                                                        16.dp
                                                                                                    )
                                                                                                    .offset(
                                                                                                        x = 8.dp
                                                                                                    )
                                                                                            )
                                                                                        }

                                                                                    }

                                                                                    ChatMessageStatus.READ -> {}
                                                                                    ChatMessageStatus.FAILED -> {}
                                                                                    ChatMessageStatus.FAILED_TO_DISPLAY_REASON_DECRYPTION_FAILED -> {}
                                                                                    ChatMessageStatus.FAILED_TO_DISPLAY_REASON_UNKNOWN -> {}

                                                                                }
                                                                            }
                                                                        }
                                                                    } ?: run {
                                                                        Text(
                                                                            text = "",
                                                                            fontSize = 14.sp,
                                                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                                            maxLines = 1,
                                                                            overflow = TextOverflow.Ellipsis
                                                                        )
                                                                    }

                                                                }

                                                            }


                                                            if (unreadCount > 0) {
                                                                Box(
                                                                    modifier = Modifier
                                                                        .background(
                                                                            Color(0xFF9747ff),
                                                                            CircleShape
                                                                        )
                                                                        .padding(
                                                                            horizontal = 8.dp,
                                                                            vertical = 4.dp
                                                                        )
                                                                ) {
                                                                    Text(
                                                                        text = viewModel.formatMessageCount(
                                                                            unreadCount
                                                                        ),
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
                                    }
                                }

                            } else {
                                Box(modifier = Modifier.fillMaxSize()) {

                                    transitionItem?.let { nonNullTransitionItem ->
                                        val profilePicUrl =
                                            nonNullTransitionItem.chatUser.userProfile.profilePicUrl

                                        val profilePicUrl96By96 =
                                            nonNullTransitionItem.chatUser.userProfile.profilePicUrl96By96


                                        val profilePicUrl96By96ImageRequest =
                                            ImageRequest.Builder(context)
                                                .data(profilePicUrl96By96)
                                                .placeholder(R.drawable.user_placeholder) // Your placeholder image
                                                .error(R.drawable.user_placeholder)
                                                .build()

                                        val profilePicUrlImageRequest =
                                            ImageRequest.Builder(context)
                                                .data(profilePicUrl)
                                                .placeholder(R.drawable.user_placeholder) // Your placeholder image
                                                .error(R.drawable.user_placeholder)
                                                .build()

                                        SubcomposeAsyncImage(
                                            profilePicUrlImageRequest,
                                            contentDescription = "User Profile Image",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .sharedBounds(
                                                    rememberSharedContentState(
                                                        key = "image-${nonNullTransitionItem.chatUser.chatId}"
                                                    ),
                                                    animatedVisibilityScope = this@AnimatedContent,
                                                )
                                                .clickable(
                                                    indication = null,
                                                    interactionSource = remember { MutableInteractionSource() }
                                                ) {
                                                    isExpanded = !isExpanded
                                                }
                                                .align(Alignment.Center),
                                            contentScale = ContentScale.Fit,
                                            loading = {
                                                AsyncImage(
                                                    profilePicUrl96By96ImageRequest,
                                                    contentDescription = "Placeholder Image",
                                                    imageLoader = viewModel.chatUsersProfileImageLoader,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .align(Alignment.Center),
                                                    contentScale = ContentScale.Fit,
                                                )
                                            },
                                            error = {
                                                AsyncImage(
                                                    profilePicUrl96By96ImageRequest,
                                                    contentDescription = "Placeholder Image",
                                                    imageLoader = viewModel.chatUsersProfileImageLoader,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .align(Alignment.Center),
                                                    contentScale = ContentScale.Fit,
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}


