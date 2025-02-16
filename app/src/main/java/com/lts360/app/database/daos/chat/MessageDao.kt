package com.lts360.app.database.daos.chat

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.lts360.app.database.models.chat.ChatMessageStatus
import com.lts360.app.database.models.chat.Message
import com.lts360.app.database.models.chat.MessageMediaMetadata
import com.lts360.app.database.models.chat.MessageWithReply
import com.lts360.app.database.models.chat.UnreadCount
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Transaction
    @Query(
        """
    SELECT m.* FROM messages m
    WHERE m.chat_id IN (:chatIds) AND m.read = 0
    AND m.id IN (
        SELECT MIN(m2.id) FROM messages m2
        WHERE m2.chat_id = m.chat_id AND m2.read = 0
        GROUP BY m2.chat_id
    )
"""
    )
    fun getFirstUnreadMessagesForUsers(chatIds: List<Int>): List<MessageWithReply>


    // Inside your method:
    fun getUnreadMessagesForUsersAsMap(chatIds: List<Int>): Map<Int, MessageWithReply> {
        val unreadMessages = getFirstUnreadMessagesForUsers(chatIds)

        // Convert the list to a map with chatId as the key
        return unreadMessages.associateBy { it.receivedMessage.chatId }
    }



    @Transaction
    @Query("SELECT * FROM messages WHERE chat_id = :chatId ORDER BY id DESC LIMIT :limit")
    fun getMessagesWithReplies(chatId: Int, limit: Int): List<MessageWithReply>

    @Transaction
    @Query("SELECT * FROM messages WHERE chat_id = :chatId ORDER BY id DESC")
    fun getMessagesWithRepliesFlow(chatId: Int): Flow<List<MessageWithReply>>

    @Transaction
    @Query(
        """
    SELECT chat_id, COUNT(*) AS unreadCount
    FROM messages
    WHERE chat_id IN (:chatIds) AND read = 0
    GROUP BY chat_id
"""
    )
    fun getUnreadCountsForSpecificUsers(chatIds: List<Int>): List<UnreadCount>


    @Transaction
    @Query("SELECT * FROM messages WHERE chat_id = :chatId AND id < :lastMessageId ORDER BY id DESC LIMIT :limit")
    fun getMessagesBefore(chatId: Int, lastMessageId: Long, limit: Int): List<MessageWithReply>


    @Transaction
    @Query("SELECT * FROM messages WHERE chat_id = :chatId AND read = 0 ORDER BY id  ASC LIMIT 1")
    fun getFirstUnreadMessage(chatId: Int): MessageWithReply?


    @Transaction
    @Query("SELECT * FROM messages WHERE chat_id = :chatId AND id >= :messageId ORDER BY id DESC")
    fun getMessagesAfterMessageWithInclusive(chatId: Int, messageId: Long): List<MessageWithReply>


    @Transaction
    @Query("SELECT * FROM messages WHERE chat_id = :chatId AND id >= :messageId ORDER BY id DESC")
    fun getMessagesAfterMessageWithInclusiveFlow(
        chatId: Int,
        messageId: Long
    ): Flow<List<MessageWithReply>>


    @Insert
    suspend fun insertMessage(message: Message): Long


    @Transaction
    suspend fun insertMessageAndMetadata(
        newMessage: Message, mediaMetaDataDao: MessageMediaMetaDataDao,
        fileMetadata: MessageMediaMetadata
    ): Long {
        // Insert the message and get the inserted message ID
        val insertedId = insertMessage(newMessage)

        mediaMetaDataDao.insertMessageMediaMetaData(fileMetadata.copy(messageId = insertedId))
        return insertedId
    }


    @Query("UPDATE messages SET status = :status WHERE id = :messageId")
    suspend fun updateMessageStatus(messageId: Long, status: ChatMessageStatus)


    @Query("UPDATE messages SET status = :status WHERE sender_id =:senderId AND recipient_id=:recipientId AND id = :messageId")
    suspend fun updateMessageStatus(
        senderId: Long,
        recipientId: Long,
        messageId: Long,
        status: ChatMessageStatus
    )


    @Query("UPDATE messages SET read = 1 WHERE id IN (:messageIds)")
    suspend fun markMessagesAsRead(messageIds: List<Long>)

    @Query("SELECT * FROM messages WHERE status = :status")
    suspend fun getQueuedMessages(status: ChatMessageStatus = ChatMessageStatus.QUEUED): List<Message>


    @Query(
        """
    SELECT * FROM messages
    WHERE id = :messageId
    """
    )
    fun getMessageById(messageId: Long): Message?


    @Query(
        """
    SELECT * FROM messages
    WHERE chat_id = :chatId
    ORDER BY timestamp DESC
    LIMIT 1
    """
    )
    fun getLastMessageFlow(chatId: Int): Flow<Message?>


    @Query(
        """
    SELECT * FROM messages
    WHERE sender_id = :userId AND chat_id = :chatId AND read = 0
    ORDER BY timestamp DESC
    LIMIT 6
    """
    )
    fun getLastSixUnreadMessage(userId: Long, chatId: Int): List<Message>


    @Query(
        """
        SELECT COUNT(*) FROM messages
        WHERE sender_id = :userId AND chat_id = :chatId AND read = 0
    """
    )
    fun countUnreadMessagesByChatIdFlow(userId: Long, chatId: Int): Flow<Int>

    @Query(
        """
        SELECT COUNT(*) FROM messages
        WHERE sender_id = :userId AND chat_id = :chatId AND read = 0
    """
    )
    fun countUnreadMessagesByChatId(userId: Long, chatId: Int): Int

    @Query("SELECT COUNT(*)  FROM messages WHERE read = 0")
    fun countAllUnreadMessagesFlow(): Flow<Int>

    @Query("SELECT COUNT(*)  FROM messages WHERE read = 0")
    fun countAllUnreadMessages(): Int


}








