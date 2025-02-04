package com.super6.pot.ui.chat

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.hilt.navigation.compose.hiltViewModel
import com.super6.pot.ui.chat.viewmodels.ChatViewModel
import com.super6.pot.ui.chat.viewmodels.IsolatedChatActivityViewModel
import com.super6.pot.ui.chat.viewmodels.ItemType
import com.super6.pot.ui.chat.viewmodels.UserState
import com.super6.pot.utils.LogUtils.TAG


@Composable
fun IsolatedChatScreen(
    onNavigateUpVideoPlayer: (Uri, Int, Int, Long) -> Unit,
    onNavigateImageSlider: (Uri, Int, Int) -> Unit,
    userState: UserState,
    isolatedChatActivityViewModel: IsolatedChatActivityViewModel = hiltViewModel(),
    onPopBackStack: () -> Unit,
    viewModel: ChatViewModel,
) {


    val isMessagesLoaded = true /*userState!=null && userState!!.isMessagesLoaded*/


    val messages = userState?.messages ?: emptyMap()
    val allMessages = viewModel.flattenMessagesWithHeaders(messages)

    val lazyListState =
        rememberLazyListState(initialFirstVisibleItemIndex = userState.firstVisibleItemIndex)

    val lastLoadedItemId by viewModel.lastLoadedItemId.collectAsState()
    val firstItemId by viewModel.firstItemId.collectAsState()

    val beforeTotalItemsCount by viewModel.beforeTotalItemsCount.collectAsState()
    val beforeFirstVisibleItemIndex by viewModel.beforeFirstVisibleItemIndex.collectAsState()



    LaunchedEffect(allMessages) {

        val isRedundant = allMessages.size == beforeTotalItemsCount

        if (isRedundant) {
            return@LaunchedEffect
        }

        val firstMessage = allMessages.firstOrNull()?.message?.receivedMessage

        if (beforeTotalItemsCount == 0 && firstItemId == -1L) {
            lazyListState.scrollToItem(0)
        } else {
            if (firstMessage != null && firstMessage.id > firstItemId) {

                // Self message scroll to bottom
                if (firstMessage.senderId == viewModel.userId) {
                    lazyListState.scrollToItem(0)
                } else {
                    if (beforeFirstVisibleItemIndex == 0) {
                        lazyListState.scrollToItem(0)
                    }
                }
            }
        }

        firstMessage?.let {
            viewModel.updateFirstItemId(it.id)
        }


    }

    LaunchedEffect(allMessages) {
        snapshotFlow { lazyListState.firstVisibleItemIndex }
            .collect { firstVisibleItemIndex ->

                viewModel.updateBeforeTotalItemsCount(lazyListState.layoutInfo.totalItemsCount)
                viewModel.updateBeforeFirstVisibleItemIndex(firstVisibleItemIndex)

                val collectedAllMessages = viewModel.flattenMessagesWithHeaders(messages)

                if (userState?.chatUser == null) {
                    return@collect
                }

                val lastVisibleItem = lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()

                if (lastVisibleItem != null) {

                    val pageSize =
                        if (lastLoadedItemId != -1L) isolatedChatActivityViewModel.SELECTED_CHAT_USER_MESSAGES_SIZE_AFTER_INITIAL_LOAD else isolatedChatActivityViewModel.SELECTED_CHAT_USER_MESSAGES_SIZE

                    val isNearEnd =
                        lastVisibleItem.index >= if (lastLoadedItemId != -1L) allMessages.size - isolatedChatActivityViewModel.SELECTED_CHAT_USER_MESSAGES_SIZE_AFTER_INITIAL_LOAD else
                            allMessages.size - pageSize


                    if (isNearEnd) {

                        val lastMessage =
                            collectedAllMessages.lastOrNull { it.itemType == ItemType.MESSAGE }

                        val lastMessageId = lastMessage?.message?.receivedMessage?.id

                        val size =
                            if (lastLoadedItemId != -1L) pageSize else isolatedChatActivityViewModel.SELECTED_CHAT_USER_MESSAGES_SIZE_AFTER_INITIAL_LOAD

                        if (lastMessageId != null && (lastLoadedItemId == -1L || lastMessageId < lastLoadedItemId)) {
                            // Update the lastLoadedItemId to prevent loading the same message

                            viewModel.updateLastLoadedItemId(lastMessageId)

                            // Ensure that the chatUser is not null before attempting to load messages
                            userState?.chatUser?.let { chatUser ->
                                // Load more messages when user is near the last 10 items
                                isolatedChatActivityViewModel.loadMessages(
                                    chatUser,
                                    lastMessage.message.receivedMessage,
                                    size
                                )
                            }

                        }
                    }
                }
            }
    }

    ChatContent(
        viewModel.chatUsersProfileImageLoader,
        userState.chatUser.userProfile,
        messages,
        isMessagesLoaded,
        lazyListState,
        onNavigateUpVideoPlayer,
        onNavigateImageSlider,
        viewModel,
        onPopBackStack
    )
}
