package com.lts360.app.database.models.chat


import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.io.File
import java.io.FileInputStream

enum class ChatMessageStatus {
    SENDING,
    SENT,       // Message has been sent to the server
    DELIVERED,  // Message has been delivered to the recipient
    READ,       // Message has been read by the recipient
    FAILED,     // Message failed to send
    QUEUED,      // Message is queued to be sent later
    QUEUED_MEDIA,      // Message is queued to be sent later
    QUEUED_MEDIA_RETRY,
    FAILED_TO_DISPLAY_REASON_DECRYPTION_FAILED,      // Message is queued to be sent later
    FAILED_TO_DISPLAY_REASON_UNKNOWN,      // Message is queued to be sent later
}

enum class ChatMessageType{
    TEXT,
    IMAGE,
    GIF,
    AUDIO,
    VIDEO,
    FILE,
}



@Entity(
    tableName = "messages",
    indices = [
        Index(value = ["id"]),
        Index(value = ["reply_id"]),
        Index(value = ["chat_id"]),
        Index(value = ["sender_id"]),
        Index(value = ["recipient_id"])
    ]
)
@TypeConverters(MessageConverters::class)
data class Message(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,

    @ColumnInfo(name = "chat_id")
    val chatId: Int,

    @ColumnInfo(name = "sender_id")
    val senderId: Long,

    @ColumnInfo(name = "recipient_id")
    val recipientId: Long,

    @ColumnInfo(name = "sender_message_id")
    val senderMessageId: Long,

    @ColumnInfo(name = "reply_id")
    val replyId: Long,

    @ColumnInfo(name = "content")
    val content: String,

    @ColumnInfo(name = "read")
    val read: Boolean = false,

    @ColumnInfo(name = "status")
    val status: ChatMessageStatus = ChatMessageStatus.SENDING,

    @ColumnInfo(name = "type")
    val type: ChatMessageType = ChatMessageType.TEXT,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long
)


@Entity(
    tableName = "messages_media_metadata",
    foreignKeys = [
        ForeignKey(
            entity = Message::class,
            parentColumns = ["id"],
            childColumns = ["message_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE

        )
    ],
    indices = [Index(value = ["message_id"])]
)
data class MessageMediaMetadata(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,

    @ColumnInfo(name = "message_id")
    val messageId: Long,  // Reference to the associated message

    @ColumnInfo(name = "file_abs_path")
    val fileAbsolutePath: String? = null,

    @ColumnInfo(name = "thumb_path")
    val fileThumbPath: String? = null,

    @ColumnInfo(name = "thumb_data")
    val thumbData: String? = null,

    @ColumnInfo(name = "cache_path")
    val fileCachePath: String? = null,

    @ColumnInfo(name = "download_url")
    val fileDownloadUrl: String? = null,

    @ColumnInfo(name = "original_file_name")
    val originalFileName: String,

    @ColumnInfo(name = "width")
    val width: Int = -1,

    @ColumnInfo(name = "height")
    val height: Int = -1,

    @ColumnInfo(name = "total_duration")
    val totalDuration: Long = -1,

    @ColumnInfo(name = "file_mime_type")
    val fileMimeType: String,

    @ColumnInfo(name = "file_extension")
    val fileExtension: String,

    @ColumnInfo(name = "file_size")
    val fileSize: Long
)



@Entity(
    tableName = "messages_processing_data",

    foreignKeys = [
        ForeignKey(
            entity = Message::class,
            parentColumns = ["id"],
            childColumns = ["message_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE

        ),
    ],

    indices = [Index(value = ["message_id"], unique = true)] // Add an index for performance
)
data class MessageProcessingData(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,

    @ColumnInfo(name = "message_id")
    val messageId: Long,  // Reference to the associated message

    @ColumnInfo(name = "file_id")
    val fileId: String?,  // Reference to the associated file metadata (nullable)

    @ColumnInfo(name = "last_sent_thumbnail_chunk_index")
    val lastSentThumbnailChunkIndex: Int? = null,

    @ColumnInfo(name = "last_sent_chunk_index")
    val lastSentChunkIndex: Int? = null,

    @ColumnInfo(name = "last_sent_thumbnail_byte_offset")
    val lastSentThumbnailByteOffset: Long? = null,

    @ColumnInfo(name = "last_sent_byte_offset")
    val lastSentByteOffset: Long? = null
)



object ThumbnailLoader {
    fun getThumbnailBitmap(thumbData: String?): Bitmap? {
        return thumbData?.let { nonNullThumbData ->
            val file = File(nonNullThumbData)
            if (file.exists()) {
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                FileInputStream(file).use {
                    BitmapFactory.decodeStream(it, null, options)
                }

                options.inPreferredConfig = Bitmap.Config.RGB_565
                options.inJustDecodeBounds = false
                options.inSampleSize = 1

                FileInputStream(file).use {
                    return BitmapFactory.decodeStream(it, null, options)
                }
            }
            null
        }
    }
}



class MessageConverters {

    @TypeConverter
    fun fromChatMessageStatus(status: ChatMessageStatus): String {
        return status.name // Convert enum to String
    }

    @TypeConverter
    fun toChatMessageStatus(status: String): ChatMessageStatus {
        return ChatMessageStatus.valueOf(status) // Convert String back to enum
    }


    @TypeConverter
    fun fromChatMessageType(chatMessageType: ChatMessageType): String {
        return chatMessageType.name // Convert enum to String
    }

    @TypeConverter
    fun toChatMessageType(chatMessageType: String): ChatMessageType {
        return ChatMessageType.valueOf(chatMessageType) // Convert String back to enum
    }

}
