package com.lts360.app.workers.chat.upload.models



data class FileUploadInfo(
    val chatId: Int,                 // Chat ID to associate the file with a conversation
    val senderId: Long,              // ID of the sender
    val recipientId: Long,           // ID of the recipient
    val messageId: Long,             // Message ID, for associating the file with a specific message
    val replyId: Long,             // Message ID, for associating the file with a specific message
    val content:String,
    val category: String,            // Category/type of file (e.g., "image", "video", "document", "audio")
    val fileName: String,            // Name of the file being uploaded
    val extension: String,           // File extension (e.g., ".jpg", ".pdf", ".mp4")
    val mimeType: String,            // MIME type (e.g., "image/jpeg", "application/pdf")
    val mediaLength: Long,           // Length/size of the media (in bytes)
    val mediaAbsPath: String,        // Absolute path to the file on the device
    val width: Int=-1,                  // Width of the media (relevant for images/videos)
    val height: Int=-1,
    val totalDuration: Long=-1L,     // Height of the media (relevant for images/videos)
    val thumbnailCachedPath: String?=null // Path to a cached thumbnail for the media (optional)
)
