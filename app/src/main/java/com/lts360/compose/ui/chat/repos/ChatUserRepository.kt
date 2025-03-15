package com.lts360.compose.ui.chat.repos

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import com.lts360.App
import com.lts360.app.database.daos.chat.ChatUserDao
import com.lts360.app.database.daos.chat.MessageDao
import com.lts360.app.database.daos.chat.MessageMediaMetaDataDao
import com.lts360.app.database.daos.chat.MessageProcessingDataDao
import com.lts360.app.database.models.chat.ChatMessageStatus
import com.lts360.app.database.models.chat.Message
import com.lts360.app.database.models.chat.MessageMediaMetadata
import com.lts360.app.database.models.chat.MessageProcessingData
import com.lts360.app.database.models.chat.MessageWithReply
import com.lts360.app.database.models.chat.PublicKeyVersion
import com.lts360.app.workers.chat.utils.getFolderTypeByExtension
import com.lts360.compose.ui.auth.repos.encryptFile
import com.lts360.compose.ui.chat.viewmodels.ItemType
import com.lts360.compose.ui.chat.viewmodels.MessageItem
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.InputStream
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject


class UploadWorkerUtilRepository @Inject constructor(
    val messageDao: MessageDao,
    val messageProcessingDataDao: MessageProcessingDataDao,
    val messageMediaMetaDataDao: MessageMediaMetaDataDao

) {


    suspend fun updateLastSentByteOffsetByMessageId(messageId: Long, byteOffset: Long) {
        return withContext(Dispatchers.IO) {
            messageProcessingDataDao.updateLastSentByteOffsetByMessageId(messageId, byteOffset)
        }
    }

    suspend fun updateLastChunkIndexByMessageId(messageId: Long, lastChunkIndex: Int) {
        return withContext(Dispatchers.IO) {
            messageProcessingDataDao.updateLastChunkIndexByMessageId(messageId, lastChunkIndex)
        }
    }

    suspend fun updateLastSentThumbnailChunkIndexByMessageId(messageId: Long, lastChunkIndex: Int) {
        return withContext(Dispatchers.IO) {
            messageProcessingDataDao.updateLastSentThumbnailChunkIndexByMessageId(
                messageId,
                lastChunkIndex
            )
        }
    }


    suspend fun updateLastSentThumbnailByteOffsetByMessageId(messageId: Long, byteOffset: Long) {
        return withContext(Dispatchers.IO) {
            messageProcessingDataDao.updateLastSentThumbnailByteOffsetByMessageId(
                messageId,
                byteOffset
            )
        }
    }


    suspend fun getLastSentByteOffset(messageId: Long): Long {
        return withContext(Dispatchers.IO) {
            val result = messageProcessingDataDao.getLastSentByteOffsetByMessageId(messageId)
            if (result == -1L) 0 else result
        }
    }

    suspend fun getLastSentChunkIndex(messageId: Long): Int {
        return withContext(Dispatchers.IO) {
            val result = messageProcessingDataDao.getLastChunkIndexByMessageId(messageId)
            if (result == -1) 0 else result
        }
    }


    suspend fun getLastSentThumbnailByteOffsetByMessageId(messageId: Long): Long {
        return withContext(Dispatchers.IO) {
            val result =
                messageProcessingDataDao.getLastSentThumbnailByteOffsetByMessageId(messageId)
            if (result == -1L) 0 else result
        }
    }

    suspend fun getLastSentThumbnailChunkIndexByMessageId(messageId: Long): Int {
        return withContext(Dispatchers.IO) {
            val result =
                messageProcessingDataDao.getLastSentThumbnailChunkIndexByMessageId(messageId)
            if (result == -1) 0 else result
        }
    }


    suspend fun insertOrUpdateMessageProcessingData(messageProcessingData: MessageProcessingData) {
        return withContext(Dispatchers.IO) {
            messageProcessingDataDao.insertOrUpdateMessageProcessingData(messageProcessingData)
        }
    }

    suspend fun getMessageProcessingDataByMessageId(messageId: Long): MessageProcessingData? {
        return withContext(Dispatchers.IO) {
            messageProcessingDataDao.getMessageProcessingDataByMessageId(messageId)
        }
    }

    suspend fun getFileIdByMessageId(messageId: Long): String? {
        return withContext(Dispatchers.IO) {
            messageProcessingDataDao.getFileIdByMessageId(messageId)
        }
    }

    suspend fun getMessageById(messageId: Long): Message? {
        return withContext(Dispatchers.IO) {
            messageDao.getMessageById(messageId)
        }
    }

    suspend fun getFileCachePathByMessageId(messageId: Long): String? {
        return withContext(Dispatchers.IO) {
            messageMediaMetaDataDao.getFileCachePathByMessageId(messageId)
        }
    }

    suspend fun updateMessageFileCachePath(messageId: Long, fileCachePath: String?) {
        withContext(Dispatchers.IO) {
            messageMediaMetaDataDao.updateMessageFileCachePath(messageId, fileCachePath)
        }
    }

    suspend fun getFileThumbPathByMessageId(messageId: Long): String? {
        return withContext(Dispatchers.IO) {
            messageMediaMetaDataDao.getFileThumbPathByMessageId(messageId)
        }
    }

    suspend fun getFileThumbDataByMessageId(messageId: Long): String? {
        return withContext(Dispatchers.IO) {
            messageMediaMetaDataDao.getFileThumbDataByMessageId(messageId)
        }
    }


    suspend fun updateMessageFileThumbPath(messageId: Long, fileThumbPath: String?) {
        withContext(Dispatchers.IO) {
            messageMediaMetaDataDao.updateMessageFileThumbPath(messageId, fileThumbPath)
        }
    }

    suspend fun updateMessageStatus(messageId: Long, status: ChatMessageStatus) {
        withContext(Dispatchers.IO) {
            messageDao.updateMessageStatus(messageId, status)
        }
    }


    suspend fun getEncryptedFileBytes(
        context: Context,
        messageId: Long,
        inputStream: InputStream,
        publicKey: String,
        cachedFilePath: String?,
        extension: String,
        fileName: String,
        type: String
    ): File {
        return  if (cachedFilePath == null) {
            // No cached file, need to encrypt
            val fileCategoryAndTypeByExtension = getFolderTypeByExtension(extension)
            val directory = File(context.externalCacheDir, fileCategoryAndTypeByExtension.first)

            if (!directory.exists()) {
                directory.mkdirs()  // Create directory if not exists
            }

            val encryptedFileName = "${fileName}_${messageId}_${System.currentTimeMillis()}"
            val destinationFile = File(directory, "${encryptedFileName.substringBeforeLast(".")}.enc")
            val lockFile = File(directory, "${encryptedFileName.substringBeforeLast(".")}.enc.lock")

            if (type == "file") {
                updateMessageFileCachePath(messageId, destinationFile.absolutePath)
            } else {
                updateMessageFileThumbPath(messageId, destinationFile.absolutePath)
            }

            // Encrypt file asynchronously
            withContext(Dispatchers.IO) {
                lockFile.createNewFile()
                try {
                    encryptFile(inputStream, FileOutputStream(destinationFile), publicKey)
                } finally {
                    // Ensure lock file is deleted even if encryption fails
                    if (lockFile.exists()) {
                        lockFile.delete()
                    }
                }
            }


            destinationFile


        } else {
            // Cached file exists, check if lock file exists to determine if encryption is in progress
            val encryptedFile = File(cachedFilePath)
            val lockFile = File("$cachedFilePath.lock")

            if (lockFile.exists()) {
                // If encryption is in progress, skip encrypted bytes and continue encryption
                withContext(Dispatchers.IO) {
                    val aesKeySize = 256 // AES key size (256 bytes)
                    val ivSize = 16 // AES IV size (16 bytes)

                    val encryptedFileLength = encryptedFile.length()

                    val bytesToSkip = encryptedFileLength - aesKeySize - ivSize

                    val safeBytesToSkip = if (bytesToSkip < 0) 0 else bytesToSkip

                    inputStream.skip(safeBytesToSkip)

                    try {
                        encryptFile(
                            inputStream,
                            FileOutputStream(encryptedFile, true),
                            publicKey
                        )
                    } finally {
                        // Ensure lock file is deleted even if encryption fails
                        lockFile.delete()
                    }
                }
                encryptedFile
            } else {
                // No lock file, assume encryption is complete

                if(!encryptedFile.exists()){
                    throw FileNotFoundException("Encrypted file is not found")
                }
                encryptedFile
            }
        }
    }

}


class ChatUserRepository @Inject constructor(
    @ApplicationContext val context: Context,
    val messageDao: MessageDao,
    val chatUserDao: ChatUserDao,
    val messageMediaMetaDataDao: MessageMediaMetaDataDao
) {


    val chatUsersProfileImageLoader = (context as App).chatUsersImageLoader

    suspend fun getMessageMediaMetaDataByMessageId(messageId: Long): MessageMediaMetadata? {
        return messageMediaMetaDataDao.getMessageMediaMetaDataByMessageId(messageId)
    }

    suspend fun updatePublicKeyByRecipientId(
        recipientId: Long,
        publicKey: String,
        keyVersion: Long,
    ): Int {

        return withContext(Dispatchers.IO) {
            chatUserDao.updatePublicKeyByRecipientId(recipientId, publicKey, keyVersion)
        }
    }


    fun getLastMessageFlow(chatId: Int): Flow<Message?> {
        return messageDao.getLastMessageFlow(chatId)
    }


    fun countUnreadMessages(recipientId: Long, chatId: Int): Flow<Int> {
        return messageDao.countUnreadMessagesByChatIdFlow(recipientId, chatId)
    }


    suspend fun getPublicKeyWithVersionByRecipientId(recipientId: Long): Flow<PublicKeyVersion?> {

        return withContext(Dispatchers.IO) {
            chatUserDao.getPublicKeyWithVersionByRecipientId(recipientId)
        }
    }

    suspend fun getQueuedMessages(): List<Message> = messageDao.getQueuedMessages()

    suspend fun getChatMessagesWithReplies(chatId: Int): Flow<List<MessageWithReply>> {

        return withContext(Dispatchers.IO) {
            messageDao.getMessagesWithRepliesFlow(chatId)
        }
    }


    suspend fun insertMessageAndMetadata(
        message: Message,
        mediaMetadata: MessageMediaMetadata
    ): Long {

        return withContext(Dispatchers.IO) {
            messageDao.insertMessageAndMetadata(message, messageMediaMetaDataDao, mediaMetadata)
        }
    }

    suspend fun insertMessage(message: Message): Long {
        return withContext(Dispatchers.IO) {
            messageDao.insertMessage(message) // This returns the inserted row ID
        }
    }


    suspend fun updateMediaMessageDownloadedMediaInfo(
        id: Long,
        fileAbsPath: String,
        fileCachedPath: String?
    ) {
        withContext(Dispatchers.IO) {
            // Update the message in the database
            messageMediaMetaDataDao.updateMediaMessageDownloadedMediaInfo(
                id,
                fileAbsPath,
                fileCachedPath
            )
        }
    }


    suspend fun updateVisualMediaMessageDownloadedMediaInfo(
        id: Long,
        fileAbsPath: String,
        fileCachedPath: String?,
        fileMetadata: MessageMediaMetadata
    ) {
        withContext(Dispatchers.IO) {

            var width: Int = -1
            var height: Int = -1
            var totalDuration: Long = -1

            // Check the MIME type to handle image or video separately
            if (fileMetadata.fileMimeType.startsWith("image/")) {
                // For image files, extract width and height
                val (imgWidth, imgHeight) = extractImageMetadata(fileAbsPath)
                width = imgWidth
                height = imgHeight
            } else if (fileMetadata.fileMimeType.startsWith("video/")) {
                // For video files, extract width, height, and duration
                val (vidWidth, vidHeight, vidDuration) = extractMediaMetadata(fileAbsPath)
                width = vidWidth
                height = vidHeight
                totalDuration = vidDuration
            } else if (fileMetadata.fileMimeType.startsWith("audio/")) {

                totalDuration = extractAudioMetadata(fileAbsPath)

            }


            messageMediaMetaDataDao.updateMessageMediaMetaData(
                fileMetadata.copy(
                    width = width,
                    height = height,
                    totalDuration = totalDuration,
                    fileAbsolutePath = fileAbsPath,
                    fileCachePath = fileCachedPath
                )
            )
        }
    }

    private fun extractImageMetadata(filePath: String): Pair<Int, Int> {
        // Decode the image using BitmapFactory
        val bitmap = BitmapFactory.decodeFile(filePath)

        // Return the width and height of the image as a pair
        return Pair(bitmap?.width ?: 0, bitmap?.height ?: 0)
    }


    private fun extractMediaMetadata(fileAbsPath: String): Triple<Int, Int, Long> {
        var width: Int
        var height: Int
        var totalDuration: Long
        var rotation: Int



        MediaMetadataRetriever().apply {
            setDataSource(fileAbsPath)
            width = extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toInt() ?: -1
            height =
                extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toInt() ?: -1

            rotation =
                extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)?.toIntOrNull()
                    ?: 0
            totalDuration =
                extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong()
                    ?: -1L

            totalDuration =
                extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: -1L
            release()
        }

        // Adjust width and height for rotation (swap width/height directly if rotated 90° or 270°)
        if (rotation == 90 || rotation == 270) {
            // Swap the width and height directly
            val tempWidth = width
            width = height
            height = tempWidth
        }


        return Triple(width, height, totalDuration)
    }


    private fun extractAudioMetadata(fileAbsPath: String): Long {
        var totalDuration: Long

        MediaMetadataRetriever().apply {
            setDataSource(fileAbsPath)
            totalDuration =
                extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: -1L
            release()
        }

        return totalDuration
    }


    suspend fun updateMessageFileCachePath(id: Long, cachePath: String?) {
        withContext(Dispatchers.IO) {
            messageMediaMetaDataDao.updateMessageFileCachePath(
                id,
                cachePath
            ) // This returns the inserted row ID
        }
    }


    suspend fun updateMessage(id: Long, message: ChatMessageStatus) {
        withContext(Dispatchers.IO) {
            messageDao.updateMessageStatus(
                id,
                message
            ) // This returns the inserted row ID
        }
    }


    suspend fun markMessageAsRead(ids: List<Long>) {
        messageDao.markMessagesAsRead(ids)
    }


    fun groupMessagesByDay(messagesWithReply: List<MessageWithReply>): Map<String, List<MessageWithReply>> {
        val groupedMessages = mutableMapOf<String, MutableList<MessageWithReply>>()

        val now = LocalDate.now()

        for (messageWithReply in messagesWithReply) {

            val message = messageWithReply.receivedMessage

            // Convert timestamp to LocalDate
            val messageLocalDate = Instant.ofEpochMilli(message.timestamp)
                .atZone(ZoneId.systemDefault())  // Adjust the ZoneId if needed (e.g., UTC)
                .toLocalDate()

            // Calculate days between the current date and message date
            val daysBetween = now.toEpochDay() - messageLocalDate.toEpochDay()

            // Determine the grouping label
            val label = when {
                messageLocalDate.isEqual(now) -> "Today"  // If the message date is today
                messageLocalDate.isEqual(now.minusDays(1)) -> "Yesterday"  // If the message date is yesterday
                daysBetween in 1..6 -> messageLocalDate.format(DateTimeFormatter.ofPattern("EEEE"))  // For the last 7 days (including today and yesterday)
                else -> messageLocalDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))  // For dates outside the last 7 days
            }


            // Add message to the corresponding group
            groupedMessages.computeIfAbsent(label) { mutableListOf() }.add(messageWithReply)
        }

        return groupedMessages
    }


    fun flattenMessagesWithHeaders(groupedMessages: Map<String, List<MessageWithReply>>): List<MessageItem> {

        val flattenedList = mutableListOf<MessageItem>()
        // Iterate over the map of messages grouped by date
        groupedMessages.forEach { (date, messages) ->
            // Add all the messages under this header
            messages.forEach { message ->
                flattenedList.add(MessageItem(ItemType.MESSAGE, message = message))
            }

            // Add the header (date) item first
            flattenedList.add(MessageItem(ItemType.HEADER, date))
        }

        flattenedList.add(MessageItem(ItemType.PROFILE_HEADER))
        flattenedList.add(MessageItem(ItemType.E2EE_HEADER))

        return flattenedList
    }


}