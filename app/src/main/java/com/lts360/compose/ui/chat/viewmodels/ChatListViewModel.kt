package com.lts360.compose.ui.chat.viewmodels


import android.content.Context
import android.util.Log
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
import com.lts360.components.utils.LogUtils.TAG
import com.lts360.compose.ui.chat.ChatUserEventsManager
import com.lts360.compose.ui.chat.repos.ChatUserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
import javax.inject.Inject

data class UserState(
    val chatUser: ChatUser,
    val lastMessage: Message? = null,
    val userName: String = "",
    val profileImageUrl: String = "",
    val profileImageUrl96By96: String = "",
    /*
        val profilePicBitmap: Bitmap? = null,
    */
    val unreadCount: Int = 0,
    val onlineStatus: Boolean = false,
    val typingStatus: Boolean = false,
    val messages: Map<String, List<MessageWithReply>> = emptyMap(),
    val isMessagesLoaded: Boolean = false,
    val firstVisibleItemIndex: Int = 0,
)


@HiltViewModel
class ChatListViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    private val chatUserDao: ChatUserDao,
    private val messageDao: MessageDao,
    val socketManager: SocketManager,
    val repository: ChatUserRepository,
    val savedStateHandle: SavedStateHandle,
) : ViewModel() {


    private val chatUserEventsManager = ChatUserEventsManager()

    var socket: Socket? = null

    private val socketFlow = socketManager.socketFlow
    private val statusFlow = socketManager.statusFlow


    private val chatUserStatusUpdatesJobs = mutableMapOf<Long, Job>()

    private val _userStates = MutableStateFlow<List<UserState>>(emptyList())
    val userStates = _userStates.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()


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
                    // Access the non-null chat user here
                    val nonNullChatUser = userState.chatUser
                    if (profileInfoUserId == nonNullChatUser.recipientId) {
                        // Call your function to update the profile image URLs
                        chatUsersProfileImageLoader.enqueue(
                            ImageRequest.Builder(context)
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
                    val lastMessage = chatUserWithDetails.lastMessage
                    val unreadCount = chatUserWithDetails.unreadCount
                    val messages = chatUserWithDetails.messages

                    chatUsersProfileImageLoader.enqueue(
                        ImageRequest.Builder(context)
                            .data(chatUser.userProfile.profilePicUrl96By96)
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
                        lastMessage = lastMessage,
                        unreadCount = unreadCount,
                        userName = "${chatUser.userProfile.firstName} ${chatUser.userProfile.lastName.orEmpty()}",
                        profileImageUrl = chatUser.userProfile.profilePicUrl.orEmpty(),
                        profileImageUrl96By96 = chatUser.userProfile.profilePicUrl96By96.orEmpty(),
                        isMessagesLoaded = true,
                        firstVisibleItemIndex = if (firstUnreadIndex == -1) 0 else firstUnreadIndex
                    )

                    _selectedChatUser.value = updatedState

                }

                val chatUsers = chatUserDao.getAllChatUsersWithUnreadMessages(
                    context, chatUsersProfileImageLoader,
                    messageDao, 10, 30
                )

                CHAT_LIST_SIZE = chatUsers.size

                _userStates.value = _userStates.value.toMutableList().apply {
                    chatUsers.forEach { chatUserWithDetails ->




                        val chatUser = chatUserWithDetails.chatUser


                        val lastMessage = chatUserWithDetails.lastMessage
                        val unreadCount = chatUserWithDetails.unreadCount
                        val messages = chatUserWithDetails.messages

                        val groupedMessages = repository.groupMessagesByDay(messages)
                        val allMessages = flattenMessagesWithHeaders(groupedMessages)

                        val firstUnreadIndex =
                            allMessages.indexOfLast { it.itemType == ItemType.MESSAGE && !it.message!!.receivedMessage.read }

                        val updatedState = UserState(
                            messages = repository.groupMessagesByDay(messages),
                            chatUser = chatUser,
                            lastMessage = lastMessage,
                            unreadCount = unreadCount,
                            userName = "${chatUser.userProfile.firstName} ${chatUser.userProfile.lastName.orEmpty()}",
                            profileImageUrl = chatUser.userProfile.profilePicUrl.orEmpty(),
                            profileImageUrl96By96 = chatUser.userProfile.profilePicUrl96By96.orEmpty(),
                            isMessagesLoaded = true,
                            firstVisibleItemIndex = if (firstUnreadIndex == -1) 0 else firstUnreadIndex
                        )

                        // Find and update the user state if it exists, or add it if not
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
                    fetchChatUserProfileInfoLimited(userState.chatUser)
                }
                loadChatUsersAndProfiles()
            }

            launch {

                socketFlow.filterNotNull()
                    .combine(chatUserDao.getAllChatUsersFlow().distinctUntilChangedBy { chatUsers ->
                        // Extract a list of user IDs or a unique identifier for comparison
                        chatUsers.map { it.chatId }
                    }) { socketInstance, chatUsers ->
                        // Now `chatUsers` will only be emitted if the list of users changes
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


    // A function to load initial page and subsequent pages
    fun loadChatUsers(lastChatUser: ChatUser, pageSize: Int) {
        viewModelScope.launch {
            _userStates.update { currentStates ->
                val updatedStates = currentStates.toMutableList()

                // Fetch chat users with unread messages from the database
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

                    // Create or update the user state
                    val updatedState =
                        currentStates.find { it.chatUser.recipientId == chatUser.recipientId }
                            ?.copy(
                                messages = repository.groupMessagesByDay(messages),
                                chatUser = chatUser,
                                lastMessage = lastMessage,
                                unreadCount = unreadCount,
                                userName = "${chatUser.userProfile.firstName} ${chatUser.userProfile.lastName.orEmpty()}",
                                profileImageUrl = chatUser.userProfile.profilePicUrl.orEmpty(),
                                profileImageUrl96By96 = chatUser.userProfile.profilePicUrl96By96.orEmpty(),
                                isMessagesLoaded = true
                            )
                            ?: UserState(
                                chatUser = chatUser,
                                messages = repository.groupMessagesByDay(messages),
                                lastMessage = lastMessage,
                                unreadCount = unreadCount,
                                userName = "${chatUser.userProfile.firstName} ${chatUser.userProfile.lastName.orEmpty()}",
                                profileImageUrl = chatUser.userProfile.profilePicUrl.orEmpty(),
                                profileImageUrl96By96 = chatUser.userProfile.profilePicUrl96By96.orEmpty(),
                                isMessagesLoaded = true
                            )

                    // Update the list by adding or replacing the user state
                    val index =
                        updatedStates.indexOfFirst { it.chatUser.recipientId == chatUser.recipientId }
                    if (index != -1) {
                        updatedStates[index] = updatedState // Update the existing user state
                    } else {
                        updatedStates.add(updatedState) // Add the new user state
                    }
                }

                updatedStates // Return the updated list of user states
            }
        }
    }


    // A function to load initial page and subsequent pages
    fun loadMessages(chatUser: ChatUser, lastMessage: Message, pageSize: Int) {

        viewModelScope.launch {


            val messages = withContext(Dispatchers.IO) {
                messageDao.getMessagesBefore(chatUser.chatId, lastMessage.id, pageSize)
            }


           val selectedUserUpdatedState= _selectedChatUser.value?.let {
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


    // Function to update the last loaded message ID
    fun updateLastLoadedChatId(id: Int) {
        viewModelScope.launch {
            _lastLoadedChatId.value = id
        }
    }


    fun updateSelectedChatUser(chatUser: ChatUser) {
        val userState = _userStates.value.find { it.chatUser.recipientId == chatUser.recipientId }
            ?: UserState(
                chatUser = chatUser,
                userName = "${chatUser.userProfile.firstName} ${chatUser.userProfile.lastName.orEmpty()}",
                profileImageUrl = chatUser.userProfile.profilePicUrl.orEmpty(),
                profileImageUrl96By96 = chatUser.userProfile.profilePicUrl96By96.orEmpty(),
                isMessagesLoaded = true
            )


        _selectedChatUser.value = userState
        userState.apply {
            SELECTED_CHAT_USER_MESSAGES_SIZE = messages.values.flatten().size
            savedStateHandle["selected_chat_id"] = chatUser.chatId
        }
    }


    fun updateSelectedChatId(userState: UserState) {
        _selectedChatUser.value = userState
        userState.apply {
            SELECTED_CHAT_USER_MESSAGES_SIZE = messages.values.flatten().size
            savedStateHandle["selected_chat_id"] = chatUser.chatId
        }
    }


    fun removeSelectedChatId() {
        savedStateHandle.remove<Int>("selected_chat_id")
    }


    private fun loadChatUsersAndProfiles(skipInitialCheck: Boolean = true) {

        var skipInitiallyCollectingItems = skipInitialCheck


        viewModelScope.launch {


            // Collect chat users
            chatUserDao.getAllChatUsersFlow().collectLatest { chatUsers ->


                if (skipInitiallyCollectingItems) {
                    // Mark initial collection as done (once it's the first time collecting)
                    skipInitiallyCollectingItems = false
                    return@collectLatest // Skip processing for initial load
                }

                if (_userStates.value.isEmpty()) {


                    // Update the list with the new profiles
                    _userStates.update { currentStates ->
                        // Convert the current states to a mutable list
                        val updatedStates = currentStates.toMutableList()

                        chatUsers.forEach { chatUser ->
                            val updatedState =
                                currentStates.find { it.chatUser.recipientId == chatUser.recipientId }
                                    ?.copy(chatUser = chatUser)
                                    ?: UserState(chatUser = chatUser)


                            chatUsersProfileImageLoader.enqueue(
                                ImageRequest.Builder(context)
                                    .data(chatUser.userProfile.profilePicUrl96By96)
                                    .build()
                            )

                            // Update the list or add the new user if not already in the list
                            val index =
                                updatedStates.indexOfFirst { it.chatUser.recipientId == chatUser.recipientId }
                            if (index != -1) {
                                updatedStates[index] =
                                    updatedState // Update the existing user state
                            } else {
                                updatedStates.add(updatedState) // Add the new user state
                            }
                        }

                        updatedStates
                    }

                    _userStates.value.forEach { userState ->
                        fetchChatUserProfileInfo(userState.chatUser)
                    }


                } else {


                    if (chatUsers.isEmpty()) {
                        _userStates.value = emptyList()
                        messagesHandlerJobs.forEach { it.value.cancel() }
                        messagesHandlerJobs.clear()
                        return@collectLatest
                    }


                    // Filter users whose profiles are not already loaded
                    val newChatUsers = chatUsers.filter { chatUser ->
                        _userStates.value.find { it.chatUser.recipientId == chatUser.recipientId } == null
                    }

                    // Update the list with new profiles if they are not already loaded
                    _userStates.update { currentStates ->
                        // Convert the current state to a mutable list
                        val updatedStates = currentStates.toMutableList()

                        // Add new chat users if not already in the list
                        newChatUsers.forEach { chatUser ->
                            val updatedState =
                                currentStates.find { it.chatUser.recipientId == chatUser.recipientId }
                                    ?.copy(chatUser = chatUser)
                                    ?: UserState(chatUser = chatUser)


                            chatUsersProfileImageLoader.enqueue(
                                ImageRequest.Builder(context)
                                    .data(chatUser.userProfile.profilePicUrl96By96)
                                    .build()
                            )

                            // Only add if the user is not already in the list
                            if (updatedStates.none { it.chatUser.recipientId == chatUser.recipientId }) {
                                updatedStates.add(updatedState)
                            }


                        }

                        updatedStates
                    }


                    // Fetch profiles for new users only
                    newChatUsers.forEach { chatUser ->
                        fetchChatUserProfileInfo(chatUser)
                    }
                }

            }
        }
    }


    private fun updateOnlineStatus(recipientId: Long, status: Boolean) {
        _userStates.update { currentStates ->
            // Find the user state by recipientId
            val userState = currentStates.find { it.chatUser.recipientId == recipientId }
                ?: return@update currentStates

            // Update the user state with the new onlineStatus
            currentStates.toMutableList().apply {
                val updatedState = userState.copy(onlineStatus = status)
                this[this.indexOf(userState)] = updatedState
            }
        }

    }


    private fun updateTypingStatus(recipientId: Long, isTyping: Boolean) {
        _userStates.update { currentStates ->
            // Find the user state by recipientId
            val userState = currentStates.find { it.chatUser.recipientId == recipientId }
                ?: return@update currentStates

            // Update the user state with the new typingStatus
            currentStates.toMutableList().apply {
                // Replace the old userState with the updated one
                val updatedState = userState.copy(typingStatus = isTyping)
                this[this.indexOf(userState)] = updatedState
            }
        }
    }

    private fun updateProfilePicUrls(
        recipientId: Long,
        profilePicUrl: String,
        profileImageUrl96By96: String
    ) {
        val userStateIndex =
            _userStates.value.indexOfFirst { it.chatUser.recipientId == recipientId }
        if (userStateIndex == -1) return  // User not found

        /*
                val bitmap = decodeBitmapForTargetSize(profileImageUrl96By96)
        */
        val currentUserState = _userStates.value[userStateIndex]

        val updatedState = currentUserState.copy(
            profileImageUrl = profilePicUrl,
            profileImageUrl96By96 = profileImageUrl96By96,
            chatUser = currentUserState.chatUser.copy(
                userProfile = currentUserState.chatUser.userProfile.copy(
                    profilePicUrl = profilePicUrl,
                    profilePicUrl96By96 = profileImageUrl96By96
                )
            )


            /*
                        profilePicBitmap = bitmap
            */
        )


        _userStates.update { currentState ->
            currentState.toMutableList().apply {
                set(userStateIndex, updatedState)
            }
        }


        _selectedChatUser.value?.let {
            if (it.chatUser.recipientId == recipientId) {
                val selectedUserState = it.copy(
                    profileImageUrl = profilePicUrl,
                    profileImageUrl96By96 = profileImageUrl96By96,
                    chatUser = currentUserState.chatUser.copy(
                        userProfile = currentUserState.chatUser.userProfile.copy(
                            profilePicUrl = profilePicUrl,
                            profilePicUrl96By96 = profileImageUrl96By96
                        )
                    )
                )

                _selectedChatUser.update {
                    selectedUserState
                }
            }
        }

    }


    private fun updateMessageList(
        recipientId: Long,
        messageList: Map<String, List<MessageWithReply>>
    ) {
        val userStateIndex =
            _userStates.value.indexOfFirst { it.chatUser.recipientId == recipientId }

        if (userStateIndex == -1) return  // User not found


        val updatedState = _userStates.value[userStateIndex].copy(messages = messageList)

        // Update the user state in the list
        _userStates.value = _userStates.value.toMutableList().apply {
            set(userStateIndex, updatedState)
        }


        // Update selected chat user state if available
        _selectedChatUser.value?.let {
            if (it.chatUser.recipientId == recipientId) {
                _selectedChatUser.value = it.copy(messages = messageList)
            }
        }


    }


    private fun updateLastMessage(recipientId: Long, lastMessage: Message?) {
        val userStateIndex =
            _userStates.value.indexOfFirst { it.chatUser.recipientId == recipientId }
        if (userStateIndex == -1) return  // User not found

        // Get the current state and create an updated state with the new last message
        val userState = _userStates.value[userStateIndex]
        val updatedState = userState.copy(lastMessage = lastMessage)

        // Update the user state in the list
        _userStates.value = _userStates.value.toMutableList().apply {
            set(userStateIndex, updatedState)
        }
    }


    private fun updateUnreadMessageCount(recipientId: Long, unreadCount: Int) {
        val userStateIndex =
            _userStates.value.indexOfFirst { it.chatUser.recipientId == recipientId }
        if (userStateIndex == -1) return  // User not found

        // Get the current state and create an updated state with the new unread count
        val userState = _userStates.value[userStateIndex]
        val updatedState = userState.copy(unreadCount = unreadCount)

        // Update the user state in the list
        _userStates.value = _userStates.value.toMutableList().apply {
            set(userStateIndex, updatedState)
        }
    }


    private fun updateFirstUnreadIndex(recipientId: Long, unreadIndex: Int) {
        val userStateIndex =
            _userStates.value.indexOfFirst { it.chatUser.recipientId == recipientId }
        if (userStateIndex == -1) return  // User not found

        // Get the current state and create an updated state with the new unread index
        val userState = _userStates.value[userStateIndex]
        val updatedState = userState.copy(firstVisibleItemIndex = unreadIndex)

        // Update the user state in the list
        _userStates.value = _userStates.value.toMutableList().apply {
            set(userStateIndex, updatedState)
        }
    }


    private fun updateIsMessagesLoaded(recipientId: Long) {
        val userStateIndex =
            _userStates.value.indexOfFirst { it.chatUser.recipientId == recipientId }
        if (userStateIndex == -1) return  // User not found

        // Get the current state and create an updated state with the new isMessagesLoaded value
        val userState = _userStates.value[userStateIndex]
        val updatedState = userState.copy(isMessagesLoaded = true)

        // Update the user state in the list
        _userStates.value = _userStates.value.toMutableList().apply {
            set(userStateIndex, updatedState)
        }
    }


    private fun fetchChatUserProfileInfoLimited(chatUser: ChatUser) {


        // Launch the background tasks in a structured manner
        viewModelScope.launch {

            /* launch(Dispatchers.IO) {

                 val profilePicBitmap = try {
                     chatUser.userProfile.profilePicUrl96By96?.let {
                         decodeBitmapForTargetSize(it)
                     }
                 } catch (e: Exception) {
                     null
                 }

                 updateProfilePicBitmap(chatUser.recipientId, profilePicBitmap)

             }
 */

            // Collect the last message
            launch(Dispatchers.IO) {
                messageDao.getLastMessageFlow(chatUser.chatId).collect { lastMessage ->
                    updateLastMessage(chatUser.recipientId, lastMessage)
                }
            }

            // Collect the unread message count
            launch(Dispatchers.IO) {
                messageDao.countUnreadMessagesByChatIdFlow(chatUser.recipientId, chatUser.chatId)
                    .collect { unreadMessageCount ->
                        updateUnreadMessageCount(chatUser.recipientId, unreadMessageCount)
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


    private fun fetchChatUserProfileInfo(chatUser: ChatUser) {


        // Launch the background tasks in a structured manner
        viewModelScope.launch {
/*


            launch(Dispatchers.IO) {

                val profilePicBitmap = try {
                    chatUser.userProfile.profilePicUrl96By96?.let {
                        decodeBitmapForTargetSize(it)
                    }
                } catch (e: Exception) {

                    null
                }

                updateProfilePicBitmap(chatUser.recipientId, profilePicBitmap)
            }
*/

            // Collect the last message
            launch(Dispatchers.IO) {
                messageDao.getLastMessageFlow(chatUser.chatId).collect { lastMessage ->
                    updateLastMessage(chatUser.recipientId, lastMessage)
                }
            }

            // Collect the unread message count
            launch(Dispatchers.IO) {
                messageDao.countUnreadMessagesByChatIdFlow(chatUser.recipientId, chatUser.chatId)
                    .collect { unreadMessageCount ->
                        updateUnreadMessageCount(chatUser.recipientId, unreadMessageCount)
                    }
            }


            val updatedState =
                _userStates.value.find { it.chatUser.recipientId == chatUser.recipientId }?.copy(
                    chatUser = chatUser,
                    userName = "${chatUser.userProfile.firstName} ${chatUser.userProfile.lastName.orEmpty()}",
                    profileImageUrl = chatUser.userProfile.profilePicUrl.orEmpty(),
                    profileImageUrl96By96 = chatUser.userProfile.profilePicUrl96By96.orEmpty()
                ) ?: UserState(
                    chatUser = chatUser,
                    userName = "${chatUser.userProfile.firstName} ${chatUser.userProfile.lastName.orEmpty()}",
                    profileImageUrl = chatUser.userProfile.profilePicUrl.orEmpty(),
                    profileImageUrl96By96 = chatUser.userProfile.profilePicUrl96By96.orEmpty(),
                )

            _userStates.value = _userStates.value.toMutableList().apply {
                val index = indexOfFirst { it.chatUser.recipientId == chatUser.recipientId }
                if (index != -1) {
                    this[index] = updatedState // Update existing user state
                } else {
                    add(updatedState) // Add new user state if not found
                }
            }


        }


        val foundedLastMessage = _userStates.value
            .find { it.chatUser.recipientId == chatUser.recipientId } // Find UserState by recipientId
            ?.messages // Get the messages map
            ?.values // Get all message lists
            ?.flatten() // Flatten the list of lists into a single list
            ?.lastOrNull() // Get the last message or null if no messages
            ?.receivedMessage // Extract the receivedMessage field


        messagesHandlerJob(chatUser, foundedLastMessage)

    }

    private fun messagesHandlerJob(
        chatUser: ChatUser,
        lastMessage: Message?
    ) {

        val userState = findUserStateForRecipientId(chatUser.recipientId) ?: return

        // Collect chat messages with replies
        messagesHandlerJobs[chatUser.chatId] = viewModelScope.launch(Dispatchers.IO) {

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




                    val isMessagesLoaded = userState.isMessagesLoaded
                    val groupedMessages = repository.groupMessagesByDay(messageList)

                    val allMessages = flattenMessagesWithHeaders(groupedMessages)

                    val firstUnreadIndex =
                        allMessages.indexOfLast { it.itemType == ItemType.MESSAGE && !it.message!!.receivedMessage.read }

                    updateFirstUnreadIndex(
                        chatUser.recipientId,
                        if (firstUnreadIndex == -1) 0 else firstUnreadIndex
                    )

                    if (!isMessagesLoaded) {
                        viewModelScope.launch {
                            // Update LazyListState and message list
                            updateIsMessagesLoaded(chatUser.recipientId)
                            updateMessageList(chatUser.recipientId, groupedMessages)
                        }

                    } else {
                        updateMessageList(chatUser.recipientId, groupedMessages)
                    }
                }
        }
    }

    private fun findUserStateForRecipientId(recipientId: Long): UserState? {
        return _userStates.value.find { it.chatUser.recipientId == recipientId }
    }

    fun flattenMessagesWithHeaders(groupedMessages: Map<String, List<MessageWithReply>>): List<MessageItem> {
        return repository.flattenMessagesWithHeaders(groupedMessages)
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
        // Cancel all jobs in the map
        messagesHandlerJobs.values.forEach { it.cancel() }
        // Clear the map after cancellation to avoid holding references
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




