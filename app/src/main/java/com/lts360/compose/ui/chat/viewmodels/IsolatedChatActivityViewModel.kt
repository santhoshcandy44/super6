package com.lts360.compose.ui.chat.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil3.request.ImageRequest
import com.lts360.app.database.daos.chat.ChatUserDao
import com.lts360.app.database.daos.chat.MessageDao
import com.lts360.app.database.models.chat.ChatUser
import com.lts360.app.database.models.chat.Message
import com.lts360.app.database.models.chat.MessageWithReply
import com.lts360.compose.ui.chat.repos.ChatUserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class IsolatedChatActivityViewModel @Inject constructor(
    val context: Context,
    private val messageDao: MessageDao,
    private val chatUserDao: ChatUserDao,
    private val repository: ChatUserRepository
) : ViewModel() {

    private val _selectedChatUser = MutableStateFlow<UserState?>(null)
    val selectedChatUser = _selectedChatUser.asStateFlow()

    val chatUsersProfileImageLoader = repository.chatUsersProfileImageLoader

    private var messagesHandlerJob: Job? = null

    var SELECTED_CHAT_USER_MESSAGES_SIZE = 0
    var SELECTED_CHAT_USER_MESSAGES_SIZE_AFTER_INITIAL_LOAD = 200


    fun loadChatUser(selectedChatUser: ChatUser) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {

                val chatUserWithDetails = chatUserDao.getSpecificChatUserWithUnreadMessages(
                    messageDao,
                    selectedChatUser.chatId,
                    30
                )

                val chatUser = chatUserWithDetails.chatUser
                val messages = chatUserWithDetails.messages


                SELECTED_CHAT_USER_MESSAGES_SIZE = messages.size


                val groupedMessages = repository.groupMessagesByDay(messages)
                val allMessages = flattenMessagesWithHeaders(groupedMessages)

                val firstUnreadIndex =
                    allMessages.indexOfLast { it.itemType == ItemType.MESSAGE && !it.message!!.receivedMessage.read }



                chatUsersProfileImageLoader.enqueue(
                    ImageRequest.Builder(context)
                        .data(chatUser.userProfile.profilePicUrl96By96)
                        .build()
                )


                _selectedChatUser.value = UserState(
                    chatUser = chatUser,
                    messages = groupedMessages,
                    lastMessage = chatUserWithDetails.lastMessage,
                    unreadCount = chatUserWithDetails.unreadCount,
                    isMessagesLoaded = true,
                    firstVisibleItemIndex = if (firstUnreadIndex == -1) 0 else firstUnreadIndex
                )


                val foundedLastMessage =
                    _selectedChatUser.value?.messages?.values?.flatten()?.lastOrNull()
                        ?.receivedMessage

                messagesHandlerJob(chatUser, foundedLastMessage)


            }
        }
    }

    fun loadMessages(chatUser: ChatUser, lastMessage: Message, pageSize: Int) {

        viewModelScope.launch {

            val messages = withContext(Dispatchers.IO) {
                messageDao.getMessagesBefore(chatUser.chatId, lastMessage.id, pageSize)
            }

            val updatedState = _selectedChatUser.value?.let {

                val mergeMessages = it.messages.values.flatten() + messages
                val combinedMessages = repository.groupMessagesByDay(mergeMessages)

                it.copy(
                    messages = combinedMessages,
                    chatUser = chatUser,
                    isMessagesLoaded = true
                )
            }
                ?: UserState(
                    chatUser = chatUser,
                    messages = repository.groupMessagesByDay(messages),
                    isMessagesLoaded = true
                )

            _selectedChatUser.value = updatedState

            val combineLastMessage =
                _selectedChatUser.value?.messages?.values?.flatten()?.lastOrNull()
                    ?.receivedMessage

            combineLastMessage?.let {
                messagesHandlerJob?.cancel()
                messagesHandlerJob(chatUser, combineLastMessage)
            }

        }
    }

    private fun messagesHandlerJob(
        chatUser: ChatUser,
        lastMessage: Message?
    ) {

        val userState = _selectedChatUser.value ?: return


        messagesHandlerJob = viewModelScope.launch(Dispatchers.IO) {

            val messagesCollectionFlow = if (lastMessage == null) {
                messageDao.getMessagesWithRepliesFlow(chatUser.chatId)

            } else {
                messageDao.getMessagesAfterMessageWithInclusiveFlow(chatUser.chatId, lastMessage.id)

            }

            messagesCollectionFlow
                .filter { newMessages ->
                    newMessages != userState.messages.values.flatten()
                }
                .collect { messageList ->

                    /*      if (skipInitiallyCollectingItems) {
                              // Mark initial collection as done (once it's the first time collecting)
                              skipInitiallyCollectingItems = false

                              return@collect // Skip processing for initial load
                          }*/


                    val isMessagesLoaded = userState.isMessagesLoaded
                    val groupedMessages = repository.groupMessagesByDay(messageList)

                    if (!isMessagesLoaded) {
                        viewModelScope.launch {
                            updateIsMessagesLoaded()
                            updateMessageList(groupedMessages)
                        }

                    } else {
                        updateMessageList(groupedMessages)
                    }
                }
        }
    }

    private fun updateIsMessagesLoaded() {
        updateUserState { it.copy(isMessagesLoaded = true) }
    }

    private fun updateMessageList(
        messageList: Map<String, List<MessageWithReply>>
    ) {
        updateUserState { it.copy(messages = messageList) }
    }

    private inline fun updateUserState(update: (UserState) -> UserState) {

        _selectedChatUser.update {
            if (it == null) return@update it
            update(it)
        }
    }


    private fun flattenMessagesWithHeaders(groupedMessages: Map<String, List<MessageWithReply>>): List<MessageItem> {
        return repository.flattenMessagesWithHeaders(groupedMessages)
    }

    override fun onCleared() {
        super.onCleared()
        messagesHandlerJob?.cancel()
    }

}

