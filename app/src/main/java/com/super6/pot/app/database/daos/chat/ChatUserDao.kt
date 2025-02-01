package com.super6.pot.app.database.daos.chat

import android.content.Context
import android.util.Log
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.TypeConverters
import coil3.ImageLoader
import coil3.request.ImageRequest
import com.google.gson.JsonParser
import com.super6.pot.api.models.service.FeedUserProfileInfo
import com.super6.pot.app.database.models.chat.ChatUser
import com.super6.pot.app.database.models.chat.ChatUserWithDetails
import com.super6.pot.app.database.models.chat.PublicKeyVersion
import com.super6.pot.app.database.models.service.converters.Converters
import com.super6.pot.utils.LogUtils.TAG
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatUserDao {


    @Insert
    suspend fun insertChatUser(chatUser: ChatUser): Long

    @Transaction
    fun getAllChatUsersWithUnreadMessages(context: Context, chatUsersProfileImageLoader:ImageLoader, messageDao: MessageDao, limit: Int, messageLimit:Int): List<ChatUserWithDetails> {
        // Fetch all chat users
        val chatUsers = getAllChatUsers(limit).onEach {
            chatUsersProfileImageLoader.enqueue(
                ImageRequest.Builder(context)
                    .data(it.userProfile.profilePicUrl96By96)
                    .build()
            )
        }

        // Fetch unread counts for all chat users in a single batch
        val unreadCounts =
            messageDao.getUnreadCountsForSpecificUsers(chatUsers.map { it.chatId }).associateBy { it.chatId }

        // Fetch the first unread message for all chat users in a batch
        val unreadMessages = messageDao.getUnreadMessagesForUsersAsMap(chatUsers.map { it.chatId })

        // Fetch messages for all users (both after the first unread message or first 20 messages)
        val messagesByChatId = chatUsers.associate { chatUser ->
            val firstUnreadMessage = unreadMessages[chatUser.chatId]

            // Fetch messages after the first unread message or the first 20 messages in batch
            val messages = if (firstUnreadMessage != null) {
                // Fetch messages after the first unread message
                val unReadMessagesForUser = messageDao.getMessagesAfterMessageWithInclusive(
                    chatUser.chatId,
                    firstUnreadMessage.receivedMessage.id)

                if(unReadMessagesForUser.size>messageLimit){
                    unReadMessagesForUser
                }else{

                    messageDao.getMessagesWithReplies(chatUser.chatId, messageLimit)
                }
            } else {
                // If no unread message exists, fetch the first 20 messages
                messageDao.getMessagesWithReplies(chatUser.chatId, messageLimit)
            }

            chatUser.chatId to messages
        }

        // Return a list of ChatUserWithDetails for each chat user
        return chatUsers.map { chatUser ->
            val unreadCount = unreadCounts[chatUser.chatId]?.unreadCount ?: 0
            val messages = messagesByChatId[chatUser.chatId] ?: emptyList()




            // Return the details of each chat user
            ChatUserWithDetails(
                chatUser = chatUser,
                unreadCount = unreadCount,
                lastMessage = messages.firstOrNull()?.receivedMessage,
                messages = messages
            )
        }
    }

    @Transaction
    fun getSpecificChatUserWithUnreadMessages(messageDao: MessageDao, chatId: Int, messagesLimit:Int): ChatUserWithDetails {
        // Fetch all chat users
        val chatUser = getSpecificChatUser(chatId)

        // Fetch unread counts for all chat users in a single batch
        val unreadCount = messageDao.countUnreadMessagesByChatId(chatUser.recipientId, chatId)

        val firstUnreadMessage =  messageDao.getFirstUnreadMessage(chatId)

        // Fetch messages after the first unread message or the first 20 messages in batch
        val messages = if (firstUnreadMessage != null) {
            // Fetch messages after the first unread message
            val unReadMessagesForUser = messageDao.getMessagesAfterMessageWithInclusive(
                chatUser.chatId,
                firstUnreadMessage.receivedMessage.id)

            if(unReadMessagesForUser.size>messagesLimit){
                unReadMessagesForUser
            }else{

                messageDao.getMessagesWithReplies(
                    chatUser.chatId, messagesLimit)
            }
        } else {
            // If no unread message exists, fetch the first 20 messages
            messageDao.getMessagesWithReplies(
                chatUser.chatId, messagesLimit)
        }

        return  ChatUserWithDetails(
            chatUser = chatUser,
            unreadCount = unreadCount,
            lastMessage = messages.firstOrNull()?.receivedMessage,
            messages = messages
        )

    }

    @Transaction
    fun getAllChatUsersWithUnreadMessagesAfterChatId(messageDao: MessageDao, lastLoadedChatId:Int, limit: Int, messageLimit:Int): List<ChatUserWithDetails> {
        // Fetch all chat users
        val chatUsers = getAllChatUsersAfterChatId(lastLoadedChatId, limit)

        // Fetch unread counts for all chat users in a single batch
        val unreadCounts =
            messageDao.getUnreadCountsForSpecificUsers(chatUsers.map { it.chatId }).associateBy { it.chatId }

        // Fetch the first unread message for all chat users in a batch
        val unreadMessages = messageDao.getUnreadMessagesForUsersAsMap(chatUsers.map { it.chatId })

        val messagesByChatId = chatUsers.associate { chatUser ->
            val firstUnreadMessage = unreadMessages[chatUser.chatId]

            val messages = if (firstUnreadMessage != null) {
                // Fetch messages after the first unread message
                val unReadMessagesForUser = messageDao.getMessagesAfterMessageWithInclusive(
                    chatUser.chatId,
                    firstUnreadMessage.receivedMessage.id)

                if(unReadMessagesForUser.size > messageLimit){
                    unReadMessagesForUser
                }else{

                    messageDao.getMessagesWithReplies(chatUser.chatId, messageLimit)
                }
            } else {
                messageDao.getMessagesWithReplies(chatUser.chatId, messageLimit)
            }

            chatUser.chatId to messages
        }

        // Return a list of ChatUserWithDetails for each chat user
        return chatUsers.map { chatUser ->
            val unreadCount = unreadCounts[chatUser.chatId]?.unreadCount ?: 0
            val messages = messagesByChatId[chatUser.chatId] ?: emptyList()

            // Return the details of each chat user
            ChatUserWithDetails(
                chatUser = chatUser,
                unreadCount = unreadCount,
                lastMessage = messages.firstOrNull()?.receivedMessage,
                messages = messages
            )
        }
    }

    @Query("SELECT * FROM chat_users ORDER BY chat_id ASC LIMIT :limit")
    fun getAllChatUsers(limit: Int): List<ChatUser>




    @Query("SELECT * FROM chat_users WHERE chat_id > :chatId ORDER BY chat_id ASC LIMIT :limit")
    fun getAllChatUsersAfterChatId(chatId: Int, limit: Int): List<ChatUser>


    @Query("SELECT * FROM chat_users WHERE chat_id =:chatId ORDER BY chat_id ASC LIMIT 1")
    fun getSpecificChatUser(chatId: Int): ChatUser


    @Query("SELECT * FROM chat_users ORDER BY chat_id ASC")
    fun getAllChatUsersFlow(): Flow<List<ChatUser>>


    @Query("SELECT * FROM chat_users WHERE recipient_id = :recipientId")
    suspend fun getChatUserByRecipientId(recipientId: Long): ChatUser?


    @Query("SELECT public_key, key_version FROM chat_users WHERE recipient_id = :recipientId AND public_key IS NOT NULL AND key_version >= 0 AND key_version != -1")
    fun getPublicKeyWithVersionByRecipientId(recipientId: Long): Flow<PublicKeyVersion?>


    @Query("SELECT public_key, key_version FROM chat_users WHERE recipient_id = :recipientId AND public_key IS NOT NULL AND key_version >= 0 AND key_version != -1")
    fun getPublicKeyWithVersionByRecipientIdRaw(recipientId: Long): PublicKeyVersion?


    @Query("UPDATE chat_users SET public_key = :publicKey, key_version = :keyVersion WHERE recipient_id = :recipientId")
    fun updatePublicKeyByRecipientId(recipientId: Long, publicKey: String, keyVersion: Long): Int


    @Transaction
    suspend fun updateProfilePicUrls(userId: Long, profilePicUrl: String?, profilePicUrl96By96: String?) {
        val chatUser = getChatUserByRecipientId(userId) ?: return

        // Directly update the user profile information
        val updatedProfile = chatUser.userProfile.copy(
            profilePicUrl = profilePicUrl,
            profilePicUrl96By96 = profilePicUrl96By96
        )

        // Apply the update
        updateUserProfileInfo(userId, updatedProfile)
    }


    @TypeConverters(Converters::class)
    @Query("""
    UPDATE chat_users 
    SET user_profile_info = :feedUserProfileInfo
    WHERE recipient_id = :userId
""")
    suspend fun updateUserProfileInfo(userId: Long, feedUserProfileInfo: FeedUserProfileInfo)

}