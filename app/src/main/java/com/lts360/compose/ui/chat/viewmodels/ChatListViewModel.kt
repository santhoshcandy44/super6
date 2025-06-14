package com.lts360.compose.ui.chat.viewmodels


import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil3.request.ImageRequest
import com.lts360.api.auth.managers.socket.SocketManager
import com.lts360.app.database.daos.chat.ChatUserDao
import com.lts360.app.database.daos.chat.MessageDao
import com.lts360.app.database.models.chat.ChatUser
import com.lts360.app.database.models.chat.Message
import com.lts360.app.database.models.chat.MessageWithReply
import com.lts360.compose.ui.chat.ChatUserEventsManager
import com.lts360.compose.ui.chat.repos.ChatUserRepository
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import org.koin.android.annotation.KoinViewModel

data class UserState(
    val chatUser: ChatUser,
    val lastMessage: Message? = null,
    val unreadCount: Int = 0,
    val onlineStatus: Boolean = false,
    val typingStatus: Boolean = false,
    val messages: Map<String, List<MessageWithReply>> = emptyMap(),
    val isMessagesLoaded: Boolean = false,
    val firstVisibleItemIndex: Int = 0,
)

@KoinViewModel
class ChatListViewModel(
    val applicationContext: Context,
    private val chatUserDao: ChatUserDao,
    private val messageDao: MessageDao,
    val repository: ChatUserRepository,
    val socketManager: SocketManager,
    val savedStateHandle: SavedStateHandle
) : ViewModel() {


    private val chatUserEventsManager = ChatUserEventsManager()

    var socket: Socket? = null

    private val socketFlow = socketManager.socketFlow
    private val statusFlow = socketManager.statusFlow


    private val chatUserStatusUpdatesJobs = mutableMapOf<Long, Job>()

    private val _userStates = MutableStateFlow<List<UserState>>(emptyList())
    val userStates = _userStates.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()


    private val onlineStatusListener = Emitter.Listener { args ->
        val data = args[0] as JSONObject
        val senderId = data.getLong("user_id")
        val status = data.getBoolean("online")
        viewModelScope.launch {
            _userStates.value.find { it.chatUser.recipientId == senderId }?.let { userState ->
                updateOnlineStatus(userState.chatUser.recipientId, status)
            }
        }

    }

    private val typingStatusListener = Emitter.Listener { args ->
        val data = args[0] as JSONObject
        val senderId = data.getLong("sender")
        val typing = data.getBoolean("is_typing")

        viewModelScope.launch {
            _userStates.value.find { it.chatUser.recipientId == senderId }?.let { userState ->
                updateTypingStatus(userState.chatUser.recipientId, typing)
            }
        }

    }

    private val profileInfoListener = Emitter.Listener { args ->
        val data = args[0] as JSONObject
        val profileInfoUserId = data.getLong("user_id")
        val profilePicUrl = data.getString("profile_pic_url")
        val profilePicUrl96By96 = data.optString("profile_pic_url_96x96")

        viewModelScope.launch(Dispatchers.IO) {

            chatUserDao.updateProfilePicUrls(
                profileInfoUserId,
                profilePicUrl,
                profilePicUrl96By96
            )

            _userStates.value.find { it.chatUser.recipientId == profileInfoUserId }
                ?.let { userState ->
                    val nonNullChatUser = userState.chatUser
                    if (profileInfoUserId == nonNullChatUser.recipientId) {
                        chatUsersProfileImageLoader.enqueue(
                            ImageRequest.Builder(applicationContext)
                                .data(profilePicUrl96By96)
                                .build()
                        )

                        updateProfilePicUrls(
                            nonNullChatUser.recipientId,
                            profilePicUrl,
                            profilePicUrl96By96
                        )
                    }
                }

        }

    }


    private val _lastLoadedChatId = MutableStateFlow(-1)
    val lastLoadedChatId = _lastLoadedChatId.asStateFlow()


    private val messagesHandlerJobs = mutableMapOf<Int, Job>()


    var SELECTED_CHAT_USER_MESSAGES_SIZE = 0
    var SELECTED_CHAT_USER_MESSAGES_SIZE_AFTER_INITIAL_LOAD = 200

    var CHAT_LIST_SIZE = 0
    var CHAT_LIST_SIZE_AFTER_INITIAL_LOAD = 30


    private val _selectedChatUser = MutableStateFlow<UserState?>(null)
    val selectedChatUser = _selectedChatUser.asStateFlow()

    val chatUsersProfileImageLoader = repository.chatUsersProfileImageLoader

    init {
        viewModelScope.launch {


            launch(Dispatchers.IO) {

                savedStateHandle.get<Int>("selected_chat_id")?.let { nonNullSelectedChatId ->


                    val chatUserWithDetails = chatUserDao.getSpecificChatUserWithUnreadMessages(
                        messageDao,
                        nonNullSelectedChatId,
                        30
                    )

                    val chatUser = chatUserWithDetails.chatUser
                    val messages = chatUserWithDetails.messages

                    chatUsersProfileImageLoader.enqueue(
                        ImageRequest.Builder(applicationContext).data(chatUser.userProfile.profilePicUrl96By96)
                            .build()
                    )

                    SELECTED_CHAT_USER_MESSAGES_SIZE = messages.size

                    val groupedMessages = repository.groupMessagesByDay(messages)
                    val allMessages = flattenMessagesWithHeaders(groupedMessages)

                    val firstUnreadIndex =
                        allMessages.indexOfLast { it.itemType == ItemType.MESSAGE && !it.message!!.receivedMessage.read }


                    val updatedState = UserState(
                        chatUser = chatUser,
                        messages = groupedMessages,
                        lastMessage = chatUserWithDetails.lastMessage,
                        unreadCount = chatUserWithDetails.unreadCount,
                        isMessagesLoaded = true,
                        firstVisibleItemIndex = if (firstUnreadIndex == -1) 0 else firstUnreadIndex
                    )

                    _selectedChatUser.value = updatedState

                }

                val chatUsers = chatUserDao.getAllChatUsersWithUnreadMessages(
                    applicationContext, chatUsersProfileImageLoader,
                    messageDao, 10, 30
                )

                CHAT_LIST_SIZE = chatUsers.size

                _userStates.value = _userStates.value.toMutableList().apply {
                    chatUsers.forEach { chatUserWithDetails ->

                        val chatUser = chatUserWithDetails.chatUser
                        val messages = chatUserWithDetails.messages

                        val groupedMessages = repository.groupMessagesByDay(messages)
                        val allMessages = flattenMessagesWithHeaders(groupedMessages)

                        val firstUnreadIndex =
                            allMessages.indexOfLast { it.itemType == ItemType.MESSAGE && !it.message!!.receivedMessage.read }

                        val updatedState = UserState(
                            messages = repository.groupMessagesByDay(messages),
                            chatUser = chatUser,
                            lastMessage = chatUserWithDetails.lastMessage,
                            unreadCount = chatUserWithDetails.unreadCount,
                            isMessagesLoaded = true,
                            firstVisibleItemIndex = if (firstUnreadIndex == -1) 0 else firstUnreadIndex
                        )

                        val existingIndex =
                            indexOfFirst { it.chatUser.recipientId == chatUser.recipientId }
                        if (existingIndex != -1) {
                            this[existingIndex] = updatedState
                        } else {
                            add(updatedState)
                        }

                    }
                }

                _isLoading.value = false

            }.join()

            launch {
                _userStates.value.forEach { userState ->
                    fetchChatUserCharMessageInfo(userState.chatUser)
                }
                loadChatUsersAndProfiles()
            }

            launch {

                socketFlow.filterNotNull()
                    .combine(chatUserDao.getAllChatUsersFlow().distinctUntilChangedBy { chatUsers ->
                        chatUsers.map { it.chatId }
                    }) { socketInstance, chatUsers ->
                        socketInstance to chatUsers
                    }.collectLatest { (socketInstance, chatUsers) ->
                        socket = socketInstance
                        chatUserStatusUpdatesJobs.values.forEach { it.cancel() }
                        chatUserStatusUpdatesJobs.clear()
                        chatUsers.forEach { chatUser ->

                            chatUserEventsManager.registerListeners(
                                socketInstance,
                                chatUser.recipientId,
                                onlineStatusListener,
                                typingStatusListener,
                                profileInfoListener
                            )


                            socket?.emit(
                                "chat:subscribeToStatus",
                                JSONObject().put(
                                    "userIds",
                                    JSONArray(listOf(chatUser.recipientId))
                                )
                            )

                            chatUserStatusUpdatesJobs[chatUser.recipientId] =
                                viewModelScope.launch {
                                    statusFlow.filter { socketStatus -> !socketStatus }
                                        .collectLatest {
                                            updateOnlineStatus(
                                                chatUser.recipientId,
                                                false
                                            )
                                            updateTypingStatus(
                                                chatUser.recipientId,
                                                false
                                            )

                                            chatUserEventsManager.unregisterListeners(
                                                socketInstance,
                                                chatUser.recipientId,
                                                onlineStatusListener,
                                                typingStatusListener,
                                                profileInfoListener
                                            )

                                            chatUserStatusUpdatesJobs[chatUser.recipientId]?.cancel()
                                            chatUserStatusUpdatesJobs.remove(chatUser.recipientId)
                                        }
                                }
                        }

                    }
            }

        }
    }

    fun updateLastLoadedChatId(id: Int) {
        viewModelScope.launch {
            _lastLoadedChatId.value = id
        }
    }

    fun updateSelectedChatUser(chatUser: ChatUser) {
        val userState = _userStates.value.find { it.chatUser.recipientId == chatUser.recipientId }
            ?: UserState(
                chatUser = chatUser,
                isMessagesLoaded = true
            )

        _selectedChatUser.value = userState

        userState.run {
            SELECTED_CHAT_USER_MESSAGES_SIZE = messages.values.flatten().size
            savedStateHandle["selected_chat_id"] = chatUser.chatId
        }
    }

    fun updateSelectedChatId(userState: UserState) {
        _selectedChatUser.value = userState
        userState.run {
            SELECTED_CHAT_USER_MESSAGES_SIZE = messages.values.flatten().size
            savedStateHandle["selected_chat_id"] = chatUser.chatId
        }
    }

    fun removeSelectedChatId() {
        savedStateHandle.remove<Int>("selected_chat_id")
    }

    private fun updateOnlineStatus(recipientId: Long, onlineStatus: Boolean) {
        updateUserState(recipientId) { it.copy(onlineStatus = onlineStatus) }
    }


    private fun updateTypingStatus(recipientId: Long, isTyping: Boolean) {
        updateUserState(recipientId) { it.copy(typingStatus = isTyping) }
    }

    private fun updateProfilePicUrls(
        recipientId: Long,
        profilePicUrl: String,
        profileImageUrl96By96: String
    ) {


        updateUserState(recipientId) {
            it.copy(
                chatUser = it.chatUser.copy(
                    userProfile = it.chatUser.userProfile.copy(
                        profilePicUrl = profilePicUrl,
                        profilePicUrl96By96 = profileImageUrl96By96
                    )
                )
            )
        }

        _selectedChatUser.value
            ?.takeIf { it.chatUser.recipientId == recipientId }
            ?.let { nonNullSelectedChatUser ->
                nonNullSelectedChatUser.copy(
                    chatUser = nonNullSelectedChatUser.chatUser.copy(
                        userProfile = nonNullSelectedChatUser.chatUser.userProfile.copy(
                            profilePicUrl = profilePicUrl,
                            profilePicUrl96By96 = profileImageUrl96By96
                        )
                    )
                ).also {
                    _selectedChatUser.update { it }
                }
            }


    }

    private fun updateMessageList(
        recipientId: Long,
        messageList: Map<String, List<MessageWithReply>>
    ) {

        updateUserState(recipientId) { it.copy(messages = messageList) }

        _selectedChatUser.value
            ?.takeIf { it.chatUser.recipientId == recipientId }
            ?.let {
                _selectedChatUser.value = it.copy(messages = messageList)
            }
    }

    private inline fun updateUserState(recipientId: Long, update: (UserState) -> UserState) {
        _userStates.value.indexOfFirst { it.chatUser.recipientId == recipientId }
            .takeIf { it != -1 }
            ?.let { index ->
                _userStates.update {
                    it.toMutableList().apply {
                        this[index] = update(this[index])
                    }
                }
            }
    }


    fun flattenMessagesWithHeaders(groupedMessages: Map<String, List<MessageWithReply>>): List<MessageItem> {
        return repository.flattenMessagesWithHeaders(groupedMessages)
    }


    fun loadChatUsers(lastChatUser: ChatUser, pageSize: Int) {
        viewModelScope.launch {
            _userStates.update { currentStates ->
                val updatedStates = currentStates.toMutableList()

                val chatUsers = withContext(Dispatchers.IO) {
                    chatUserDao.getAllChatUsersWithUnreadMessagesAfterChatId(
                        messageDao,
                        lastChatUser.chatId,
                        pageSize,
                        30
                    )
                }

                chatUsers.forEach { chatUserWithDetails ->
                    val chatUser = chatUserWithDetails.chatUser
                    val lastMessage = chatUserWithDetails.lastMessage
                    val unreadCount = chatUserWithDetails.unreadCount
                    val messages = chatUserWithDetails.messages

                    val updatedState =
                        currentStates.find { it.chatUser.recipientId == chatUser.recipientId }
                            ?.copy(
                                messages = repository.groupMessagesByDay(messages),
                                chatUser = chatUser,
                                lastMessage = lastMessage,
                                unreadCount = unreadCount,
                                isMessagesLoaded = true
                            )
                            ?: UserState(
                                chatUser = chatUser,
                                messages = repository.groupMessagesByDay(messages),
                                lastMessage = lastMessage,
                                unreadCount = unreadCount,
                                isMessagesLoaded = true
                            )

                    val index =
                        updatedStates.indexOfFirst { it.chatUser.recipientId == chatUser.recipientId }
                    if (index != -1) {
                        updatedStates[index] = updatedState
                    } else {
                        updatedStates.add(updatedState)
                    }
                }

                updatedStates
            }
        }
    }

    fun loadMessages(chatUser: ChatUser, lastMessage: Message, pageSize: Int) {

        viewModelScope.launch {


            val messages = withContext(Dispatchers.IO) {
                messageDao.getMessagesBefore(chatUser.chatId, lastMessage.id, pageSize)
            }


            val selectedUserUpdatedState = _selectedChatUser.value?.let {
                val mergeMessages = it.messages.values.flatten() + messages
                val combinedMessages = repository.groupMessagesByDay(mergeMessages)
                it.copy(messages = combinedMessages)
            } ?: return@launch

            _selectedChatUser.value = selectedUserUpdatedState


            val combineLastMessage = _selectedChatUser.value
                ?.messages?.values
                ?.flatten()
                ?.lastOrNull()
                ?.receivedMessage

            combineLastMessage?.let {
                messagesHandlerJobs[chatUser.chatId]?.cancel()
                messagesHandlerJob(chatUser, combineLastMessage)
            }

        }
    }


    private fun loadChatUsersAndProfiles(skipInitialCheck: Boolean = true) {

        var skipInitiallyCollectingItems = skipInitialCheck

        viewModelScope.launch {
            chatUserDao.getAllChatUsersFlow().collectLatest { chatUsers ->

                if (skipInitiallyCollectingItems) {
                    skipInitiallyCollectingItems = false
                    return@collectLatest
                }

                if (_userStates.value.isEmpty()) {
                    _userStates.update { currentStates ->

                        val updatedStates = currentStates.toMutableList()

                        chatUsers.forEach { chatUser ->
                            val updatedState =
                                currentStates.find { it.chatUser.recipientId == chatUser.recipientId }
                                    ?.copy(chatUser = chatUser)
                                    ?: UserState(chatUser = chatUser)


                            chatUsersProfileImageLoader.enqueue(
                                ImageRequest.Builder(applicationContext)
                                    .data(chatUser.userProfile.profilePicUrl96By96)
                                    .build()
                            )

                            val index =
                                updatedStates.indexOfFirst { it.chatUser.recipientId == chatUser.recipientId }
                            if (index != -1) {
                                updatedStates[index] = updatedState
                            } else {
                                updatedStates.add(updatedState)
                            }
                        }

                        updatedStates
                    }

                    _userStates.value.forEach { userState ->
                        fetchChatUserCharMessageInfo(userState.chatUser)
                    }


                } else {

                    if (chatUsers.isEmpty()) {
                        _userStates.value = emptyList()
                        messagesHandlerJobs.forEach { it.value.cancel() }
                        messagesHandlerJobs.clear()
                        return@collectLatest
                    }


                    val newChatUsers = chatUsers.filter { chatUser ->
                        _userStates.value.find { it.chatUser.recipientId == chatUser.recipientId } == null
                    }

                    _userStates.update { currentStates ->
                        val updatedStates = currentStates.toMutableList()

                        newChatUsers.forEach { chatUser ->
                            val updatedState =
                                currentStates.find { it.chatUser.recipientId == chatUser.recipientId }
                                    ?.copy(chatUser = chatUser)
                                    ?: UserState(chatUser = chatUser)


                            chatUsersProfileImageLoader.enqueue(
                                ImageRequest.Builder(applicationContext)
                                    .data(chatUser.userProfile.profilePicUrl96By96)
                                    .build()
                            )

                            if (updatedStates.none { it.chatUser.recipientId == chatUser.recipientId }) {
                                updatedStates.add(updatedState)
                            }


                        }

                        updatedStates
                    }


                    newChatUsers.forEach { chatUser ->
                        fetchChatUserCharMessageInfo(chatUser)
                    }
                }

            }
        }
    }

    private fun fetchChatUserCharMessageInfo(chatUser: ChatUser) {

        viewModelScope.launch {
            launch(Dispatchers.IO) {
                messageDao.getLastMessageFlow(chatUser.chatId).collectLatest { lastMessage ->
                    updateUserState(chatUser.recipientId) { it.copy(lastMessage = lastMessage) }
                }
            }
            launch(Dispatchers.IO) {
                messageDao.countUnreadMessagesByChatIdFlow(chatUser.recipientId, chatUser.chatId)
                    .collectLatest { unreadMessageCount ->
                        updateUserState(chatUser.recipientId) { it.copy(unreadCount = unreadMessageCount) }
                    }
            }
        }


        val foundedLastMessage = _userStates.value
            .find { it.chatUser.recipientId == chatUser.recipientId }
            ?.messages
            ?.values
            ?.flatten()
            ?.lastOrNull()
            ?.receivedMessage

        messagesHandlerJob(chatUser, foundedLastMessage)

    }


    private fun messagesHandlerJob(
        chatUser: ChatUser,
        lastMessage: Message?
    ) {

        val userState =
            _userStates.value.find { it.chatUser.recipientId == chatUser.recipientId } ?: return

        messagesHandlerJobs[chatUser.chatId] = viewModelScope.launch(Dispatchers.IO) {

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

                    val isMessagesLoaded = userState.isMessagesLoaded
                    val groupedMessages = repository.groupMessagesByDay(messageList)

                    val allMessages = flattenMessagesWithHeaders(groupedMessages)

                    val firstUnreadIndex =
                        allMessages.indexOfLast { it.itemType == ItemType.MESSAGE && !it.message!!.receivedMessage.read }


                    updateUserState(chatUser.recipientId) { it.copy(firstVisibleItemIndex = if (firstUnreadIndex == -1) 0 else firstUnreadIndex) }

                    if (!isMessagesLoaded) {
                        updateUserState(chatUser.recipientId) { it.copy(isMessagesLoaded = true) }
                    }

                    updateMessageList(chatUser.recipientId, groupedMessages)

                }
        }
    }


    fun formatMessageCount(number: Int): String {
        return if (number > 999) {
            "999+"
        } else {
            number.toString()
        }
    }

    override fun onCleared() {
        chatUserStatusUpdatesJobs.values.forEach { it.cancel() }
        chatUserStatusUpdatesJobs.clear()
        messagesHandlerJobs.values.forEach { it.cancel() }
        messagesHandlerJobs.clear()
        _userStates.value.forEach { userState ->
            socket?.let {
                chatUserEventsManager.unregisterListeners(
                    it,
                    userState.chatUser.recipientId,
                    onlineStatusListener,
                    typingStatusListener,
                    profileInfoListener
                )
            }
        }

        super.onCleared()

    }


}




