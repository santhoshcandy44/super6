package com.super6.pot.app.database.daos.chat

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.super6.pot.app.database.models.chat.MessageMediaMetadata

@Dao
interface MessageMediaMetaDataDao {


    @Insert
    suspend fun insertMessageMediaMetaData(messageMediaMetadata: MessageMediaMetadata)

    @Query("SELECT * FROM messages_media_metadata WHERE message_id = :messageId ")
    suspend fun getMessageMediaMetaDataByMessageId(messageId: Long): MessageMediaMetadata?



    @Update
    suspend fun updateMessageMediaMetaData(mediaMessageData:MessageMediaMetadata)

    @Query("UPDATE messages_media_metadata SET file_abs_path = :fileAbsPath, cache_path=:fileCachedPath WHERE message_id = :messageId")
    suspend fun updateMediaMessageDownloadedMediaInfo(messageId: Long, fileAbsPath: String, fileCachedPath:String?)


    @Query("UPDATE messages_media_metadata SET cache_path = :fileCachePath WHERE message_id = :messageId")
    suspend fun updateMessageFileCachePath(messageId: Long, fileCachePath: String?)

    @Query("UPDATE messages_media_metadata SET thumb_path = :fileThumbPath WHERE message_id = :messageId")
    suspend fun updateMessageFileThumbPath(messageId: Long, fileThumbPath: String?)


    @Query("SELECT cache_path FROM messages_media_metadata WHERE message_id = :messageId")
    suspend fun getFileCachePathByMessageId(messageId: Long): String?


    @Query("SELECT thumb_path FROM messages_media_metadata WHERE message_id = :messageId")
    suspend fun getFileThumbPathByMessageId(messageId: Long): String?

    @Query("SELECT thumb_data FROM messages_media_metadata WHERE message_id = :messageId")
    suspend fun getFileThumbDataByMessageId(messageId: Long): String?

}