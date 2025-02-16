package com.lts360.compose.ui.chat.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import coil3.imageLoader
import coil3.request.ImageRequest
import com.lts360.app.database.models.chat.Message
import com.lts360.app.database.daos.chat.MessageDao
import com.lts360.app.database.daos.chat.ChatUserDao
import com.lts360.app.database.models.chat.ChatUser
import com.lts360.app.database.models.chat.MessageWithReply
import com.lts360.compose.ui.chat.repos.ChatUserRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext



class ChatActivityViewModel(
    application: Application,
    private val messageDao: MessageDao,
    private val chatUserDao: ChatUserDao,
    val recipientId:Long,
    private val repository: ChatUserRepository,
) : AndroidViewModel(application) {



    private val appContext: Context = application

    private val _selectedChatUser = MutableStateFlow<UserState?>(null)
    val selectedChatUser = _selectedChatUser.asStateFlow()


    // Channel to signal when loading is complete
    private val _loadingComplete = CompletableDeferred<Unit>()

    val loadingComplete: CompletableDeferred<Unit> = _loadingComplete

    private var messagesHandlerJob: Job? = null

    init {
        loadChatUser()
    }

    var SELECTED_CHAT_USER_MESSAGES_SIZE = 0
    var SELECTED_CHAT_USER_MESSAGES_SIZE_AFTER_INITIAL_LOAD = 200


    private fun loadChatUser() {

        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {

                    chatUserDao.getChatUserByRecipientId(recipientId)?.let {

                        val startTime = System.currentTimeMillis()  // Record start time

                        val chatUserWithDetails = chatUserDao.getSpecificChatUserWithUnreadMessages(
                            messageDao,
                            it.chatId,
                            30
                        )

                        val chatUser = chatUserWithDetails.chatUser
                        val lastMessage = chatUserWithDetails.lastMessage
                        val unreadCount = chatUserWithDetails.unreadCount
                        val messages = chatUserWithDetails.messages

                        val endTime = System.currentTimeMillis()  // Record end time
                        val elapsedTime = endTime - startTime  // Calculate elapsed time

                        SELECTED_CHAT_USER_MESSAGES_SIZE = messages.size


                        val groupedMessages = repository.groupMessagesByDay(messages)
                        val allMessages = flattenMessagesWithHeaders(groupedMessages)

                        val firstUnreadIndex =
                            allMessages.indexOfLast { it.itemType == ItemType.MESSAGE && !it.message!!.receivedMessage.read }



                        appContext.imageLoader.enqueue(
                            ImageRequest.Builder(appContext)
                                .data(chatUser.userProfile.profilePicUrl96By96)
                                .build())

                        _selectedChatUser.value = UserState(
                            chatUser = chatUser,
                            messages = groupedMessages,
                            lastMessage = lastMessage,
                            unreadCount = unreadCount,
                            userName = "${chatUser.userProfile.firstName} ${chatUser.userProfile.lastName.orEmpty()}",
                            profileImageUrl = chatUser.userProfile.profilePicUrl.orEmpty(),
                            isMessagesLoaded = true,
                            firstVisibleItemIndex = if (firstUnreadIndex == -1) 0 else firstUnreadIndex
                        )


                        // Notify that loading is complete
                        _loadingComplete.complete(Unit)

                        val foundedLastMessage = _selectedChatUser.value?.messages?.values?.flatten()?.lastOrNull()
                            ?.receivedMessage

                        messagesHandlerJob(chatUser, foundedLastMessage)

                    } ?: run {
                        loadingComplete.completeExceptionally(IllegalStateException("Chat user not found"))
                    }
                }


            } catch (e: Exception) {
                // Notify that loading is complete even on error
                _loadingComplete.complete(Unit)
            }
        }
    }



    // A function to load initial page and subsequent pages
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
                    userName = "${chatUser.userProfile.firstName} ${chatUser.userProfile.lastName.orEmpty()}",
                    profileImageUrl = chatUser.userProfile.profilePicUrl.orEmpty(),
                    isMessagesLoaded = true
                )
            }
                ?: UserState(
                    chatUser = chatUser,
                    messages = repository.groupMessagesByDay(messages),
                    userName = "${chatUser.userProfile.firstName} ${chatUser.userProfile.lastName.orEmpty()}",
                    profileImageUrl = chatUser.userProfile.profilePicUrl.orEmpty(),
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
        lastMessage: Message?,
    ) {



        val userState = _selectedChatUser.value
            ?: return

        // Collect chat messages with replies
        messagesHandlerJob = viewModelScope.launch(Dispatchers.IO) {


            val messagesCollectionFlow = if (lastMessage == null) {
                messageDao.getMessagesWithRepliesFlow(chatUser.chatId) // It is new chat user

            } else {
                messageDao.getMessagesAfterMessageWithInclusiveFlow(chatUser.chatId, lastMessage.id)

            }


            messagesCollectionFlow
                .filter { newMessages ->
                    newMessages != userState.messages.values.flatten() // Compare with the already collected messages
                }
                .collect { messageList ->
/*

                if (skipInitiallyCollectingItems) {
                    // Mark initial collection as done (once it's the first time collecting)
                    skipInitiallyCollectingItems = false
                    return@collect // Skip processing for initial load
                }
*/



                val isMessagesLoaded = userState.isMessagesLoaded
                val groupedMessages = repository.groupMessagesByDay(messageList)

                if (!isMessagesLoaded) {
                    viewModelScope.launch {
                        // Update LazyListState and message list
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
        val userState = _selectedChatUser.value ?: return

        val updatedState = userState.copy(isMessagesLoaded = true)
        _selectedChatUser.value = updatedState
    }

    private fun updateMessageList(
        messageList: Map<String, List<MessageWithReply>>
    ) {

        val userState = _selectedChatUser.value
            ?: return

        val updatedState = userState.copy(messages = messageList)
        _selectedChatUser.value = updatedState

        _selectedChatUser.value?.let {
            _selectedChatUser.value = it.copy(messages = messageList)
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

class B{


}
