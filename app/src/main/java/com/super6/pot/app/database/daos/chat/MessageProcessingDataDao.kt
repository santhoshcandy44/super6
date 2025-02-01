package com.super6.pot.app.database.daos.chat

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.super6.pot.app.database.models.chat.MessageProcessingData


@Dao
interface MessageProcessingDataDao{


    @Query("SELECT last_sent_chunk_index FROM messages_processing_data WHERE message_id=:messageId")
    fun getLastChunkIndexByMessageId(messageId: Long): Int

    @Query("SELECT last_sent_byte_offset FROM messages_processing_data WHERE message_id=:messageId")
    fun getLastSentByteOffsetByMessageId(messageId: Long): Long

    @Query("SELECT last_sent_thumbnail_chunk_index FROM messages_processing_data WHERE message_id=:messageId")
    fun getLastSentThumbnailChunkIndexByMessageId(messageId: Long): Int

    @Query("SELECT last_sent_thumbnail_byte_offset FROM messages_processing_data WHERE message_id=:messageId")
    fun getLastSentThumbnailByteOffsetByMessageId(messageId: Long): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateMessageProcessingData(messageProcessingData: MessageProcessingData)

    @Query("UPDATE messages_processing_data SET file_id = :fileId WHERE message_id = :messageId")
    suspend fun updateFileIdByMessageId(messageId: Long, fileId: String)

    // Insert a new record
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessageProcessingData(data: MessageProcessingData)

    @Query("SELECT * FROM messages_processing_data WHERE message_id = :messageId")
    suspend fun getMessageProcessingDataByMessageId(messageId: Long): MessageProcessingData?

    @Query("SELECT file_id FROM messages_processing_data WHERE message_id = :messageId")
    suspend fun getFileIdByMessageId(messageId: Long): String?

    @Query("UPDATE messages_processing_data SET last_sent_thumbnail_chunk_index = :lastSentThumbnailChunkIndex WHERE message_id = :messageId")
    suspend fun updateLastSentThumbnailChunkIndexByMessageId(messageId: Long, lastSentThumbnailChunkIndex: Int)


    @Query("UPDATE messages_processing_data SET last_sent_chunk_index = :lastChunkIndex WHERE message_id = :messageId")
    suspend fun updateLastChunkIndexByMessageId(messageId: Long, lastChunkIndex: Int)


    @Query("UPDATE messages_processing_data SET last_sent_thumbnail_byte_offset = :lastSentThumbnailByteOffset WHERE message_id = :messageId")
    suspend fun updateLastSentThumbnailByteOffsetByMessageId(messageId: Long, lastSentThumbnailByteOffset: Long)

    @Query("UPDATE messages_processing_data SET last_sent_byte_offset = :lastSentByteOffset WHERE message_id = :messageId")
    suspend fun updateLastSentByteOffsetByMessageId(messageId: Long, lastSentByteOffset: Long)

} 
