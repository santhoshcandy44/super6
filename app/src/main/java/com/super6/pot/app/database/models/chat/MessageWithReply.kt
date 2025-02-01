package com.super6.pot.app.database.models.chat

import androidx.room.Embedded
import androidx.room.Relation


data class MessageWithReply(
    @Embedded val receivedMessage: Message, // The current message

    // Fetch the message being replied to (if any)
    @Relation(
        parentColumn = "reply_id", // Link received message's reply_id to the id of the replied message
        entityColumn = "id"
    )
    val repliedToMessage: Message?, // The message being replied to (null if no reply)

    // Fetch file metadata for the received message (current message)
    @Relation(
        parentColumn = "id", // Link received message's id to message_id in MessageFileMetadata
        entityColumn = "message_id"
    )
    val receivedMessageFileMeta: MessageMediaMetadata?, // Metadata for the received message

    // Fetch file metadata for the replied message (if any)
    @Relation(
        parentColumn = "reply_id",    // Link the received message's reply_id to the message_id of the replied message
        entityColumn = "message_id"   // Foreign key in MessageFileMetadata
    )
    val repliedMessageFileMeta: MessageMediaMetadata? // Metadata for the replied-to message (if any)
)


