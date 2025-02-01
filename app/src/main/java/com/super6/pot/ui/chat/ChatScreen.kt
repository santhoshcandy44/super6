package com.super6.pot.ui.chat

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import com.super6.pot.ui.auth.ResetPasswordScreen
import com.super6.pot.ui.chat.viewmodels.ChatListViewModel
import com.super6.pot.ui.chat.viewmodels.ChatViewModel
import com.super6.pot.ui.chat.viewmodels.ItemType

@Composable
fun ChatScreen(
    onNavigateUpVideoPlayer: (Uri, Int, Int, Long) -> Unit,
    chatListViewModel: ChatListViewModel,
    viewModel: ChatViewModel,
    onNavigateImageSlider: (Uri, Int, Int) -> Unit,
    onPopBackStack: () -> Unit,

    ) {



    BackHandler {
        onPopBackStack()
        chatListViewModel.removeSelectedChatId()
    }

    val userState by chatListViewModel.selectedChatUser.collectAsState()


    userState?.let { nonNullUserState ->

        val isMessagesLoaded = true /*userState!=null && userState!!.isMessagesLoaded*/

        val messages = nonNullUserState.messages
        val allMessages = chatListViewModel.flattenMessagesWithHeaders(messages)

        val firstVisibleItemIndex = nonNullUserState.firstVisibleItemIndex

        val lazyListState =
            rememberLazyListState(initialFirstVisibleItemIndex = firstVisibleItemIndex)

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

                    val collectedAllMessages =
                        chatListViewModel.flattenMessagesWithHeaders(messages)

               /*     if (userState?.chatUser == null) {
                        return@collect
                    }
*/
                    val lastVisibleItem = lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()

                    if (lastVisibleItem != null) {

                        val pageSize = if (lastLoadedItemId != -1L) chatListViewModel.SELECTED_CHAT_USER_MESSAGES_SIZE_AFTER_INITIAL_LOAD else chatListViewModel.SELECTED_CHAT_USER_MESSAGES_SIZE

                        val isNearEnd =
                            lastVisibleItem.index >= if (lastLoadedItemId != -1L) allMessages.size - chatListViewModel.SELECTED_CHAT_USER_MESSAGES_SIZE_AFTER_INITIAL_LOAD else
                                allMessages.size - pageSize


                        if (isNearEnd) {

                            val lastMessage = collectedAllMessages.lastOrNull { it.itemType == ItemType.MESSAGE }

                            val lastMessageId = lastMessage?.message?.receivedMessage?.id

                            val size = if (lastLoadedItemId != -1L) pageSize else chatListViewModel.SELECTED_CHAT_USER_MESSAGES_SIZE_AFTER_INITIAL_LOAD

                            if (lastMessageId != null && (lastLoadedItemId == -1L || lastMessageId < lastLoadedItemId)) {
                                // Update the lastLoadedItemId to prevent loading the same message

                                viewModel.updateLastLoadedItemId(lastMessageId)

                                // Ensure that the chatUser is not null before attempting to load messages
                                nonNullUserState.chatUser.also { chatUser ->
                                    // Load more messages when user is near the last 10 items
                                    chatListViewModel.loadMessages(
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
            chatListViewModel.chatUsersProfileImageLoader,
            nonNullUserState.chatUser.userProfile,
            messages,
            isMessagesLoaded,
            lazyListState,
            onNavigateUpVideoPlayer,
            onNavigateImageSlider,
            viewModel,
            onPopBackStack
        )
    } ?: run {
        onPopBackStack()
    }


}