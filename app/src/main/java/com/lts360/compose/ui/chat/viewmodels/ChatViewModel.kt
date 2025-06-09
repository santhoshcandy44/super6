package com.lts360.compose.ui.chat.viewmodels

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.lts360.App
import com.lts360.api.app.AppClient
import com.lts360.api.auth.managers.socket.SocketManager
import com.lts360.api.auth.services.CommonService
import com.lts360.app.database.daos.chat.ChatUserDao
import com.lts360.app.database.daos.chat.MessageDao
import com.lts360.app.database.models.chat.ChatMessageStatus
import com.lts360.app.database.models.chat.ChatMessageType
import com.lts360.app.database.models.chat.ChatUser
import com.lts360.app.database.models.chat.Message
import com.lts360.app.database.models.chat.MessageMediaMetadata
import com.lts360.app.database.models.chat.MessageWithReply
import com.lts360.app.database.models.chat.PublicKeyVersion
import com.lts360.app.workers.chat.utils.generateUniqueFileName
import com.lts360.app.workers.chat.utils.getFileCategoryByExtension
import com.lts360.app.workers.chat.utils.getFileExtension
import com.lts360.app.workers.chat.utils.getFileMimeType
import com.lts360.app.workers.chat.utils.getFolderTypeByExtension
import com.lts360.app.workers.helpers.MediaUploadWorkerHelper
import com.lts360.app.workers.helpers.VisualMediaUploadWorkerHelper
import com.lts360.app.workers.chat.upload.models.FileUploadInfo
import com.lts360.components.utils.compressImageAsByteArray
import com.lts360.components.utils.isUriExist
import com.lts360.compose.ui.auth.repos.DecryptionFileStatus
import com.lts360.compose.ui.auth.repos.decryptFile
import com.lts360.compose.ui.auth.repos.encryptMessage
import com.lts360.compose.ui.chat.ChatUtilNativeBaseActivity
import com.lts360.compose.ui.chat.repos.ChatUserRepository
import com.lts360.compose.ui.main.navhosts.routes.MainRoutes
import com.lts360.compose.ui.managers.UserSharedPreferencesManager

import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.socket.client.Ack
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.ResponseBody
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.concurrent.CancellationException
import javax.inject.Inject
import androidx.core.net.toUri

@Serializable
sealed class MediaDownloadState {

    @Serializable
    data object Started : MediaDownloadState()

    @Serializable
    data class InProgress(val downloadedBytes: Long) : MediaDownloadState()

    @Serializable
    data object Downloaded : MediaDownloadState()

    @Serializable
    data object Failed : MediaDownloadState()
}


// Enum to define item types (HEADER for date headers, MESSAGE for actual messages)
enum class ItemType {
    HEADER, MESSAGE, E2EE_HEADER, PROFILE_HEADER
}

// Data class to represent either a header or a message with the type
data class MessageItem(
    val itemType: ItemType,
    val date: String? = null, // Only used if it's a header
    val message: MessageWithReply? = null, // Only used if it's a message
)


@Serializable
sealed class FileUploadState {
    // Represents the progress of a file upload, storing the percentage of progress.
    @Serializable
    data class InProgress(val progress: Int) : FileUploadState()

    @Serializable
    object None : FileUploadState()

    @Serializable
    object Started : FileUploadState()

    @Serializable
    data class Retry(val reason: String) : FileUploadState()

    @Serializable
    object Completed : FileUploadState()

    @Serializable
    object Failed : FileUploadState()


}

fun serializeFileUploadState(state: FileUploadState): String {
    return Json.encodeToString(state)
}

fun deserializeFileUploadState(jsonString: String): FileUploadState {
    return Json.decodeFromString(jsonString)
}


@HiltViewModel
class ChatViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    socketManager: SocketManager,
    val savedStateHandle: SavedStateHandle,
    val repository: ChatUserRepository,
    val chatUserDao: ChatUserDao,
    val messageDao: MessageDao
) : ViewModel() {


    val chatUsersProfileImageLoader = repository.chatUsersProfileImageLoader

    val userId = UserSharedPreferencesManager.userId
    val args = savedStateHandle.toRoute<MainRoutes.ChatWindow>()


    private val _lastLoadedItemId = MutableStateFlow<Long>(-1L)
    val lastLoadedItemId = _lastLoadedItemId.asStateFlow()

    private val _firstItemId = MutableStateFlow<Long>(-1L)
    val firstItemId = _firstItemId.asStateFlow()


    private val _beforeTotalItemsCount = MutableStateFlow<Int>(-1)
    val beforeTotalItemsCount = _beforeTotalItemsCount.asStateFlow()


    private val _beforeFirstVisibleItemIndex = MutableStateFlow(-1)
    val beforeFirstVisibleItemIndex = _beforeFirstVisibleItemIndex.asStateFlow()


    fun updateBeforeFirstVisibleItemIndex(count: Int) {
        viewModelScope.launch {
            _beforeFirstVisibleItemIndex.value = count
        }
    }


    fun updateBeforeTotalItemsCount(count: Int) {
        viewModelScope.launch {
            _beforeTotalItemsCount.value = count
        }
    }

    fun updateLastLoadedItemId(id: Long) {
        viewModelScope.launch {
            _lastLoadedItemId.value = id
        }
    }

    fun updateFirstItemId(id: Long) {
        viewModelScope.launch {
            _firstItemId.value = id
        }
    }

    val chatId = args.chatId
    val recipientId = args.recipientId

    private val _chatUser = MutableStateFlow<ChatUser?>(null)
    val chatUser: StateFlow<ChatUser?> = _chatUser

    private var e2eeCredentials: PublicKeyVersion? = null


    private var socket: Socket? = null

    private val socketFlow = socketManager.socketFlow


    private val _onlineStatus = MutableStateFlow("")
    val onlineStatus: StateFlow<String> get() = _onlineStatus


    private val _isTyping = MutableStateFlow(false)
    val isTyping = _isTyping.asStateFlow()


    private val _selectedMessage = MutableStateFlow<Message?>(null)
    val selectedMessage = _selectedMessage.asStateFlow()

    private val _selectedMessageMessageFileMetaData = MutableStateFlow<MessageMediaMetadata?>(null)
    val selectedMessageMessageMediaMetadata = _selectedMessageMessageFileMetaData.asStateFlow()

    private val onlineStatusHandler = Emitter.Listener { args ->
        val data = args[0] as JSONObject
        val sender = data.getLong("user_id")
        val status = data.getBoolean("online")
        val lastVisible = data.getString("last_active")
        viewModelScope.launch {
            if (sender == recipientId) {
                _onlineStatus.value = if (status) "online" else lastSeenTimestamp(lastVisible)
            }
        }
    }

    private val typingHandler = Emitter.Listener { args ->
        val data = args[0] as JSONObject
        val sender = data.getLong("sender")
        val isTyping = data.getBoolean("is_typing")

        viewModelScope.launch {
            if (sender == recipientId) {
                _onlineStatus.value = if (isTyping) "typing..." else "online"
                _isTyping.value = isTyping
            }
        }
    }


    init {

        viewModelScope.launch {

            launch(Dispatchers.IO) {
                repository.getPublicKeyWithVersionByRecipientId(recipientId).collectLatest {
                    e2eeCredentials = it
                }
            }

            launch {
                socketFlow.collectLatest {

                    it?.let {

                        socket = it

                        socket!!.emit("chat:chatOpen", JSONObject().apply {
                            put("user_id", userId)
                            put("recipient_id", recipientId)
                        })

                        socket!!.on("chat:onlineStatus-${recipientId}", onlineStatusHandler)
                        socket!!.on("chat:typing", typingHandler)

                        socket!!.on(Socket.EVENT_DISCONNECT) {

                            viewModelScope.launch {
                                _onlineStatus.value = ""
                                _isTyping.value = false
                                socket!!.off(
                                    "chat:onlineStatus-${recipientId}",
                                    onlineStatusHandler
                                )
                                socket!!.off("chat:typing", typingHandler)
                            }

                        }

                    }
                }

            }
        }


    }


    private val _uploadStates = MutableStateFlow<Map<Long, FileUploadState>>(emptyMap())
    val uploadStates: StateFlow<Map<Long, FileUploadState>> = _uploadStates


    private val _downloadStates = MutableStateFlow<Map<Long, MediaDownloadState>>(emptyMap())
    val downloadStates: StateFlow<Map<Long, MediaDownloadState>> = _downloadStates


    fun updateUploadState(messageId: Long, state: FileUploadState) {
        viewModelScope.launch {
            val updatedStates = _uploadStates.value.toMutableMap()
            updatedStates[messageId] = state
            _uploadStates.value = updatedStates
        }

    }


    fun updateMessage(id: Long, message: ChatMessageStatus) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateMessage(
                id,
                message
            ) // This returns the inserted row ID
        }
    }


    fun updateDownloadState(messageId: Long, state: MediaDownloadState) {
        val downloadStates = _downloadStates.value.toMutableMap()
        downloadStates[messageId] = state
        _downloadStates.value = downloadStates
    }


    fun insertVisualMediaMessageAndSend(
        blurredThumbnailCacheFile: File,
        senderId: Long,
        recipientId: Long,
        originalFileName: String,
        mediaAbsolutePath: String,
        fileSize: Long,
        width: Int,
        height: Int,
        senderMessageId: Long,
        replyId: Long,
        totalDuration: Long = -1
    ) {

        val originalFileExtension = getFileExtension(originalFileName)
        val mimeType = getFileMimeType(originalFileExtension)


        val category = getFileCategoryByExtension(originalFileExtension)

        try {
            // Create the message based on MIME type
            val newMessage = when (category) {
                "image" -> {
                    // Handle image type files (JPEG, PNG, etc.)
                    Message(
                        chatId = chatId,
                        senderId = senderId,
                        recipientId = recipientId,
                        content = "Image \uD83D\uDDBC\uFE0F",
                        read = true,
                        timestamp = System.currentTimeMillis(),
                        senderMessageId = -1,
                        replyId = replyId,
                        type = ChatMessageType.IMAGE,
                        status = ChatMessageStatus.QUEUED_MEDIA,
                    )
                }

                "gif" -> {
                    // Handle image type files (JPEG, PNG, etc.)
                    Message(
                        chatId = chatId,
                        senderId = senderId,
                        recipientId = recipientId,
                        content = "Gif \uD83D\uDDBC\uFE0F",
                        read = true,
                        timestamp = System.currentTimeMillis(),
                        senderMessageId = -1,
                        replyId = replyId,
                        type = ChatMessageType.GIF,
                        status = ChatMessageStatus.QUEUED_MEDIA,
                    )
                }

                "video" -> {
                    // Handle document files (PDF, Word, Excel, etc.)
                    Message(
                        chatId = chatId,
                        senderId = senderId,
                        recipientId = recipientId,
                        content = "Video \uD83C\uDFA5",
                        read = true,
                        timestamp = System.currentTimeMillis(),
                        senderMessageId = -1,
                        replyId = replyId,
                        type = ChatMessageType.VIDEO,
                        status = ChatMessageStatus.QUEUED_MEDIA,

                        )
                }


                else -> throw IllegalStateException("Only Image/Gif/Video allowed")
            }


            viewModelScope.launch {

                val insertedId = repository.insertMessageAndMetadata(
                    newMessage, MessageMediaMetadata(
                        messageId = -1,
                        fileSize = fileSize,
                        fileMimeType = mimeType,
                        width = width,
                        height = height,
                        totalDuration = totalDuration,
                        fileExtension = originalFileExtension,
                        originalFileName = originalFileName,
                        fileAbsolutePath = mediaAbsolutePath,
                        thumbData = blurredThumbnailCacheFile.absolutePath
                    )
                )



                VisualMediaUploadWorkerHelper.doVisualMediaUpload(
                    (context.applicationContext as App),
                    FileUploadInfo(
                        chatId = chatId,
                        senderId = senderId,
                        recipientId = recipientId,
                        messageId = insertedId,
                        replyId = senderMessageId,
                        content = newMessage.content,
                        category = category,
                        fileName = originalFileName,
                        extension = originalFileExtension,
                        mimeType = mimeType,
                        mediaLength = fileSize,
                        mediaAbsPath = mediaAbsolutePath,
                        width = width,
                        height = height,
                        totalDuration = totalDuration,
                    )
                )


            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    fun insertMediaMessageAndSend(
        originalFile: File,
        mediaUri: String,
        senderId: Long,
        recipientId: Long,
        senderMessageId: Long = -1L,
        replyId: Long = -1L,
        totalDuration: Long = -1L
    ) {
        val originalFileName = originalFile.name
        val originalBytes = originalFile.readBytes()
        val originalFileExtension = getFileExtension(originalFileName)
        val mimeType = getFileMimeType(originalFileExtension)
        val fileSize = originalBytes.size.toLong()


        // Get the category based on file extension
        val category = getFileCategoryByExtension(originalFileExtension)

        try {
            // Create a base message with common properties
            val newMessage = Message(
                chatId = chatId,
                senderId = senderId,
                recipientId = recipientId,
                content = when (category) {
                    "audio" -> "Audio \uD83C\uDFA7"
                    "file" -> "File \uD83D\uDCC1"
                    "other" -> "File \uD83D\uDCC1"
                    else -> throw UnsupportedOperationException("Unsupported file")
                },
                read = true,
                timestamp = System.currentTimeMillis(),
                senderMessageId = -1,
                replyId = replyId,
                status = ChatMessageStatus.QUEUED_MEDIA,
                type = when (category) {
                    "audio" -> ChatMessageType.AUDIO
                    "file" -> ChatMessageType.FILE
                    "other" -> ChatMessageType.FILE
                    else -> throw UnsupportedOperationException("Unsupported file")
                }
            )

            viewModelScope.launch {
                val insertedId = repository.insertMessageAndMetadata(
                    newMessage,
                    MessageMediaMetadata(
                        messageId = -1,
                        fileSize = fileSize,
                        fileMimeType = mimeType,
                        fileExtension = originalFileExtension,
                        originalFileName = originalFileName,
                        fileAbsolutePath = mediaUri,
                        totalDuration = totalDuration
                    )
                )

                MediaUploadWorkerHelper.doMediaUpload(
                    (context.applicationContext as App),
                    FileUploadInfo(
                        chatId = chatId,
                        senderId = senderId,
                        recipientId = recipientId,
                        messageId = insertedId,
                        replyId = senderMessageId,
                        content = newMessage.content,
                        category = category,
                        fileName = originalFileName,
                        extension = originalFileExtension,
                        mimeType = mimeType,
                        mediaLength = fileSize,
                        mediaAbsPath = mediaUri,
                        totalDuration = totalDuration
                    )
                )
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun onRetrySendVisualMedia(
        context: Context,
        messageId: Long,
        senderId: Long,
        recipientId: Long,
        content: String,
        absolutePath: String,
        fileMetadata: MessageMediaMetadata,
        senderMessageId: Long,
        onRetry: () -> Unit,
        onFailed: () -> Unit
    ) {


        val category = getFileCategoryByExtension(fileMetadata.fileExtension)

        if (!isUriExist(context, absolutePath.toUri())) {
            onFailed()
            viewModelScope.launch {
            repository.updateMessage(messageId, ChatMessageStatus.FAILED)
            }
            return
        }


        try {
            onRetry()
            VisualMediaUploadWorkerHelper.doVisualMediaUpload(
                (context.applicationContext as App),

                FileUploadInfo(
                    chatId = chatId,
                    senderId = senderId,
                    recipientId = recipientId,
                    messageId = messageId,
                    replyId = senderMessageId,
                    content = content,
                    category = category,
                    fileName = fileMetadata.originalFileName,
                    extension = fileMetadata.fileExtension,
                    mimeType = fileMetadata.fileMimeType,
                    mediaLength = fileMetadata.fileSize,
                    mediaAbsPath = absolutePath,
                    width = fileMetadata.width,
                    height = fileMetadata.height,
                    totalDuration = fileMetadata.totalDuration,
                )
            )

        } catch (_: Exception) {
            onFailed()
        }

    }


    fun onRetrySendMedia(
        messageId: Long,
        senderId: Long,
        recipientId: Long,
        content: String,
        absolutePath: String,
        fileMetadata: MessageMediaMetadata,
        senderMessageId: Long = -1L,
        onRetry: () -> Unit,
        onFailed: () -> Unit
    ) {

        val category = getFileCategoryByExtension(fileMetadata.fileExtension)

        if (!isUriExist(context, absolutePath.toUri())) {
            onFailed()

            viewModelScope.launch {
                repository.updateMessage(messageId, ChatMessageStatus.FAILED)
            }
            return
        }

        try {
            onRetry()
            MediaUploadWorkerHelper.doMediaUpload(
                (context.applicationContext as App),
                FileUploadInfo(
                    chatId = chatId,
                    senderId = senderId,
                    recipientId = recipientId,
                    replyId = senderMessageId,
                    content = content,
                    messageId = messageId,
                    category = category,
                    fileName = fileMetadata.originalFileName,
                    extension = fileMetadata.fileExtension,
                    mimeType = fileMetadata.fileMimeType,
                    mediaLength = fileMetadata.fileSize,
                    mediaAbsPath = absolutePath,
                )
            )

        } catch (_: Exception) {
            onFailed()
        }
    }


    private val downloadJobs = mutableMapOf<Long, Job>()


    fun downloadMediaAndUpdateMessage(
        context: Activity,
        messageId: Long,
        senderId: Long,
        mediaDownloadUrl: String,
        cachedFileAbsPath: String?,
        fileMetadata: MessageMediaMetadata
    ) {
        downloadJobs[messageId] = viewModelScope.launch(Dispatchers.IO) {
            updateDownloadState(messageId, MediaDownloadState.Started)
            try {

                val originalFileName = fileMetadata.originalFileName
                val fileExtension = fileMetadata.fileExtension

                // Check if the file is already cached
                val cachedFile = if (cachedFileAbsPath != null) File(cachedFileAbsPath) else null
                // If the cached file exists, resume the download from where it left off


                if (cachedFile != null && cachedFile.exists()) {

                    // Check if the file is fully downloaded or not
                    val currentFileSize = cachedFile.length()
                    updateDownloadState(messageId, MediaDownloadState.InProgress(currentFileSize))


                    if (currentFileSize < fileMetadata.fileSize) {
                        // Call the download function to resume from the cached file's length
                        val responseBody = downloadMediaWithRetry(
                            mediaDownloadUrl,
                            currentDownloadedBytes = currentFileSize
                        )

                        if (responseBody != null) {
                            // Resume writing to the file
                            var readLength: Int
                            val buffer = ByteArray(4 * 1024)
                            val bis = BufferedInputStream(responseBody.byteStream(), 4 * 1024)

                            var totalBytesDownloaded =
                                currentFileSize // start where the previous download left off

                            // Open the file in append mode to continue writing
                            val fileOutputStream = FileOutputStream(cachedFile, true)
                            val fileChannel = fileOutputStream.channel
                            val bufferByteBuffer = ByteBuffer.allocate(4 * 1024)

                            withContext(Dispatchers.IO) {
                                while (bis.read(buffer).also { readLength = it } != -1) {
                                    if (!isActive) {
                                        cancel() // Stop the download if the job is cancelled
                                        return@withContext
                                    }

                                    totalBytesDownloaded += readLength.toLong()
                                    bufferByteBuffer.clear()
                                    bufferByteBuffer.put(buffer, 0, readLength)
                                    bufferByteBuffer.flip()
                                    fileChannel.write(bufferByteBuffer)
                                    updateDownloadState(
                                        messageId,
                                        MediaDownloadState.InProgress(totalBytesDownloaded)
                                    )
                                }
                            }

                            bis.close()
                            fileOutputStream.flush()
                            fileOutputStream.close()


                        } else {
                            updateDownloadState(messageId, MediaDownloadState.Failed)
                        }
                    }


                    socket?.let { nonNullSocket ->
                        if (nonNullSocket.connected()) {
                            try {
                                withTimeout(5000) { // Set timeout to 5 seconds
                                    suspendCancellableCoroutine<Unit> { continuation ->
                                        nonNullSocket.emit("chat:mediaStatus", JSONObject().apply {
                                            put("status", "MEDIA_DOWNLOADED")
                                            put("download_url", mediaDownloadUrl)
                                            put("sender", senderId)
                                            put("recipient_id", recipientId)
                                            put(
                                                "message_id",
                                                messageId
                                            ) // Add the inserted message ID to JSON
                                        }, Ack {
                                            continuation.resume(Unit) { cause, value, context ->
                                                // Acknowledgment received, log it
                                            }
                                        })
                                    }
                                }


                                val mediaFile = getAppSpecificMediaFolder(context, originalFileName)

                                when (val decryptedFile = decryptFile(cachedFile, mediaFile)) {
                                    is DecryptionFileStatus.Success -> {

                                        // Cache the decrypted file
                                        decryptedFile.decryptedFile.let {
                                            updateDownloadState(
                                                messageId,
                                                MediaDownloadState.Downloaded
                                            )
                                            if (fileMetadata.fileMimeType.startsWith("image/")
                                                || fileMetadata.fileMimeType.startsWith("video/") || fileMetadata.fileMimeType.startsWith(
                                                    "audio/"
                                                )
                                            ) {
                                                repository.updateVisualMediaMessageDownloadedMediaInfo(
                                                    it.absolutePath,
                                                    null,
                                                    fileMetadata
                                                )
                                            } else {
                                                repository.updateMediaMessageDownloadedMediaInfo(
                                                    messageId,
                                                    it.absolutePath,
                                                    null
                                                )
                                            }
                                            cachedFile.delete()

                                        }
                                    }

                                    is DecryptionFileStatus.DecryptionFailed -> {
                                        mediaFile.delete()
                                        cachedFile.delete()
                                        repository.updateMessage(
                                            messageId,
                                            ChatMessageStatus.FAILED_TO_DISPLAY_REASON_DECRYPTION_FAILED
                                        )
                                        updateDownloadState(messageId, MediaDownloadState.Failed)
                                    }

                                    is DecryptionFileStatus.UnknownError -> {
                                        mediaFile.delete()
                                        cachedFile.delete()
                                        repository.updateMessage(
                                            messageId,
                                            ChatMessageStatus.FAILED_TO_DISPLAY_REASON_UNKNOWN
                                        )
                                    }
                                }


                            } catch (_: TimeoutCancellationException) {
                                // Log timeout failure
                                updateDownloadState(messageId, MediaDownloadState.Failed)
                            } catch (_: Exception) {
                                // Handle any unexpected errors
                                updateDownloadState(messageId, MediaDownloadState.Failed)
                            }
                        } else {
                            // Socket not connected, update state as failed
                            updateDownloadState(messageId, MediaDownloadState.Failed)
                        }
                    }


                    return@launch
                }

                // If the file is not cached, start the download from scratch
                val responseBody = downloadMediaWithRetry(mediaDownloadUrl)
                if (responseBody == null) {
                    updateDownloadState(messageId, MediaDownloadState.Failed)
                    return@launch
                }


                // Get the cache directory where the file should be stored
                val dir = context.externalCacheDir

                val fileCategoryAndTypeByExtension = getFolderTypeByExtension(fileExtension)
                val directory = File(dir, fileCategoryAndTypeByExtension.first)

                if (!directory.exists()) {
                    directory.mkdirs()  // Create the directory if it doesn't exist
                }

                // Create the file in the desired directory
                val destinationFile =
                    File(directory, "${originalFileName.substringBeforeLast(".")}.bin")

                repository.updateMessageFileCachePath(
                    messageId,
                    destinationFile.absolutePath
                )


                var readLength: Int
                val buffer = ByteArray(4 * 1024)  // 4 KB buffer size
                val bis = BufferedInputStream(responseBody.byteStream(), 4 * 1024)

                var totalBytesDownloaded: Long = 0


                // Write directly to disk in chunks
                val fileOutputStream = FileOutputStream(destinationFile, false)
                val fileChannel = fileOutputStream.channel
                val bufferByteBuffer = ByteBuffer.allocate(4 * 1024)



                withContext(Dispatchers.IO) {
                    while (bis.read(buffer).also { readLength = it } != -1) {

                        if (!isActive) {
                            cancel() // Stop the download if the job is cancelled
                            return@withContext
                        }
                        totalBytesDownloaded += readLength.toLong()
                        bufferByteBuffer.clear()
                        bufferByteBuffer.put(buffer, 0, readLength)
                        bufferByteBuffer.flip()
                        fileChannel.write(bufferByteBuffer)
                        updateDownloadState(
                            messageId,
                            MediaDownloadState.InProgress(totalBytesDownloaded)
                        )

                    }
                }

                bis.close()
                fileOutputStream.flush()
                fileOutputStream.close()

                val mediaFile = getAppSpecificMediaFolder(context, originalFileName)
                socket?.let { nonNullSocket ->
                    if (nonNullSocket.connected()) {
                        try {
                            withTimeout(5000) { // Set timeout to 5 seconds
                                suspendCancellableCoroutine<Unit> { continuation ->
                                    nonNullSocket.emit("chat:mediaStatus", JSONObject().apply {
                                        put("status", "MEDIA_DOWNLOADED")
                                        put("download_url", mediaDownloadUrl)
                                        put("sender", senderId)
                                        put("recipient_id", recipientId)
                                        put(
                                            "message_id",
                                            messageId
                                        ) // Add the inserted message ID to JSON
                                    }, Ack {
                                        continuation.resume(Unit) { cause, value, context ->
                                            // Acknowledgment received, log it
                                        }
                                    })
                                }
                            }


                            when (val decryptedFile = decryptFile(destinationFile, mediaFile)) {
                                is DecryptionFileStatus.Success -> {

                                    // Cache the decrypted file
                                    decryptedFile.decryptedFile.also {
                                        updateDownloadState(
                                            messageId,
                                            MediaDownloadState.Downloaded
                                        )
                                        if (fileMetadata.fileMimeType.startsWith("image/")
                                            || fileMetadata.fileMimeType.startsWith("video/")
                                            || fileMetadata.fileMimeType.startsWith(
                                                "audio/"
                                            )
                                        ) {
                                            repository.updateVisualMediaMessageDownloadedMediaInfo(
                                                it.absolutePath,
                                                null,
                                                fileMetadata
                                            )
                                        } else {
                                            repository.updateMediaMessageDownloadedMediaInfo(
                                                messageId,
                                                it.absolutePath,
                                                null
                                            )
                                        }

                                        destinationFile.delete()

                                    }
                                }

                                is DecryptionFileStatus.DecryptionFailed -> {
                                    mediaFile.delete()
                                    destinationFile.delete()
                                    repository.updateMessage(
                                        messageId,
                                        ChatMessageStatus.FAILED_TO_DISPLAY_REASON_DECRYPTION_FAILED
                                    )
                                    updateDownloadState(messageId, MediaDownloadState.Failed)
                                }

                                is DecryptionFileStatus.UnknownError -> {
                                    mediaFile.delete()
                                    destinationFile.delete()
                                    repository.updateMessage(
                                        messageId,
                                        ChatMessageStatus.FAILED_TO_DISPLAY_REASON_UNKNOWN
                                    )
                                }
                            }

                        } catch (_: TimeoutCancellationException) {
                            // Log timeout failure
                            updateDownloadState(messageId, MediaDownloadState.Failed)
                        } catch (_: Exception) {
                            // Handle any unexpected errors
                            updateDownloadState(messageId, MediaDownloadState.Failed)
                        }
                    } else {
                        // Socket not connected, update state as failed
                        updateDownloadState(messageId, MediaDownloadState.Failed)
                    }
                }


            } catch (e: Exception) {
                updateDownloadState(messageId, MediaDownloadState.Failed)
                e.printStackTrace()
            }
        }
    }


    fun cancelVisualMediaUpload(chatId: Int, messageId: Long) {
        VisualMediaUploadWorkerHelper.cancelVisualMediaUploadWorker(
            (context.applicationContext) as App,
            chatId,
            messageId
        )
    }


    fun cancelMediaUpload(chatId: Int, messageId: Long) {

        MediaUploadWorkerHelper.cancelMediaUploadWorker(
            (context.applicationContext) as App,
            chatId,
            messageId
        )

    }


    fun cancelDownload(messageId: Long) {
        downloadJobs[messageId]?.cancel(CancellationException("Cancelled job"))
    }


    private suspend fun downloadMediaWithRetry(
        url: String,
        delayTime: Long = 1000L,
        currentDownloadedBytes: Long = 0L
    ): ResponseBody? {
        var responseBody: ResponseBody?
        while (true) {
            try {


                val rangeHeader = if (currentDownloadedBytes > 0) {
                    "bytes=$currentDownloadedBytes-"  // Resume from the current downloaded byte position
                } else {
                    // For fresh download, don't pass any Range header
                    null
                }

                // Make the download request with the Range header
                responseBody = AppClient.mediaDownloadInstance.create(CommonService::class.java)
                    .downloadMediaResponse(url, rangeHeader).body()


                return responseBody
            } catch (e: Exception) {
                e.printStackTrace()
                delay(delayTime)  // Retry after a delay
            }
        }
    }


    fun writeDataToAppSpecificFolder(
        context: Activity,
        data: ByteArray,
        fileName: String,
    ): File? {


        val extension = getFileExtension(fileName)
        // Get the app-specific media directory (this will return a list of directories)
        val mediaDirs = context.externalMediaDirs


        val fileCategoryAndTypeByExtension = getFolderTypeByExtension(extension)

        // Select the primary external media directory for your app
        val directory = File(
            mediaDirs[0],
            fileCategoryAndTypeByExtension.first
        ) // Create the "Super6 Images" folder within the first directory


        // Ensure the directory exists
        if (!directory.exists()) {
            directory.mkdirs()  // Create the directory if it doesn't exist
        }
        if (!File(directory, ".nomedia").exists()) {
            File(directory, ".nomedia").mkdirs()  // Create the directory if it doesn't exist
        }

        // Generate a unique file name and ensure it does not exist

        var uniqueFileName = fileName

        // Only generate a unique name for Image, Video, or Audio
        if (fileCategoryAndTypeByExtension.second in listOf("Image", "Video", "Audio")) {
            uniqueFileName = generateUniqueFileName(extension)

            // Check if file exists, if so, regenerate the unique name
            while (File(directory, uniqueFileName).exists()) {
                uniqueFileName = generateUniqueFileName(extension)
            }
        }


        // Create a new file in the directory
        val file = File(directory, uniqueFileName)

        try {
            // Open the file output stream to write data
            val fos = FileOutputStream(file)
            // Write the data to the file
            fos.write(data)

            // Close the stream
            fos.close()

            // Return the absolute path of the saved file
            return file
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }


    fun createBlurredThumbnailAndWriteGifDataToAppSpecificFolder(
        context: Activity,
        data: ByteArray,
        fileName: String
    ): Pair<File, File>? {


        val mediaExtension = getFileExtension(fileName)

        val thumbnailBitmap = (context as ChatUtilNativeBaseActivity).createThumbnail(
            BitmapFactory.decodeByteArray(
                data,
                0,
                data.size
            )
        )

        val cachedDir = context.getExternalFilesDir(null)

        val fileCategoryAndTypeByExtension = getFolderTypeByExtension(mediaExtension)

        val cachedDirectory = File(
            cachedDir,
            fileCategoryAndTypeByExtension.first
        )

        if (!cachedDirectory.exists()) {
            cachedDirectory.mkdirs()  // Create the directory if it doesn't exist
        }

        if (!File(cachedDirectory, ".nomedia").exists()) {
            File(cachedDirectory, ".nomedia").mkdirs()  // Create the directory if it doesn't exist
        }


        if (fileCategoryAndTypeByExtension.second in listOf("Gif")) {

            var cachedThumbnailUniqueFileName = generateUniqueFileName(mediaExtension)
            // Check if file exists, if so, regenerate the unique name
            while (File(cachedDirectory, cachedThumbnailUniqueFileName).exists()) {
                cachedThumbnailUniqueFileName = generateUniqueFileName(mediaExtension)
            }

            // Create a new file in the directory
            val cachedThumbnailFile = File(cachedDir, cachedThumbnailUniqueFileName)

            val byteArrayOutputStream = ByteArrayOutputStream()

            thumbnailBitmap.compress(
                if (mediaExtension == ".jpg" || mediaExtension == ".jpeg" || mediaExtension == ".gif")
                    Bitmap.CompressFormat.JPEG
                else if (mediaExtension == ".png") Bitmap.CompressFormat.PNG
                else if (mediaExtension == ".webp")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        Bitmap.CompressFormat.WEBP_LOSSLESS
                    } else {
                        Bitmap.CompressFormat.WEBP
                    }
                else
                    throw UnsupportedOperationException("Unsupported file format $mediaExtension"),
                100,
                byteArrayOutputStream
            )

            byteArrayOutputStream.close()

            val blurredThumbnailBytes = byteArrayOutputStream.toByteArray()


            FileOutputStream(cachedThumbnailFile).use {
                it.write(compressImageAsByteArray(blurredThumbnailBytes))
            }


            // Get the app-specific media directory (this will return a list of directories)
            val mediaDirs = context.externalMediaDirs


            // Select the primary external media directory for your app
            val directory = File(
                mediaDirs[0],
                fileCategoryAndTypeByExtension.first
            ) // Create the "Super6 Images" folder within the first directory

            if (!File(directory, ".nomedia").exists()) {
                File(directory, ".nomedia").mkdirs()  // Create the directory if it doesn't exist
            }

            // Ensure the directory exists
            if (!directory.exists()) {
                directory.mkdirs()  // Create the directory if it doesn't exist
            }


            // Only generate a unique name for Image, Video
            if (fileCategoryAndTypeByExtension.second in listOf("Gif")) {
                var uniqueFileName = generateUniqueFileName(mediaExtension)

                // Check if file exists, if so, regenerate the unique name
                while (File(directory, uniqueFileName).exists()) {
                    uniqueFileName = generateUniqueFileName(mediaExtension)
                }
                // Create a new file in the directory
                val file = File(directory, uniqueFileName)

                try {
                    FileOutputStream(file).use {
                        it.write(data)
                    }
                    // Return the absolute path of the saved file
                    return Pair(cachedThumbnailFile, file)
                } catch (e: IOException) {
                    e.printStackTrace()
                    return null
                }

            } else {
                return null
            }


        } else {
            return null
        }


    }


    fun createBlurredThumbnailAndWriteDataToAppSpecificFolder(
        context: Activity,
        data: ByteArray,
        fileName: String
    ): Pair<File, File>? {


        val mediaExtension = getFileExtension(fileName)

        val thumbnailBitmap = (context as ChatUtilNativeBaseActivity).createThumbnail(
            BitmapFactory.decodeByteArray(
                data,
                0,
                data.size
            )
        )

        val cachedDir = context.getExternalFilesDir(null)

        val fileCategoryAndTypeByExtension = getFolderTypeByExtension(mediaExtension)

        val cachedDirectory = File(
            cachedDir,
            fileCategoryAndTypeByExtension.first
        )

        if (!cachedDirectory.exists()) {
            cachedDirectory.mkdirs()  // Create the directory if it doesn't exist
        }

        if (!File(cachedDirectory, ".nomedia").exists()) {
            File(cachedDirectory, ".nomedia").mkdirs()  // Create the directory if it doesn't exist
        }


        // Only generate a unique name for Image, Video
        if (fileCategoryAndTypeByExtension.second in listOf("Image")) {

            var cachedThumbnailUniqueFileName = generateUniqueFileName(mediaExtension)
            // Check if file exists, if so, regenerate the unique name
            while (File(cachedDirectory, cachedThumbnailUniqueFileName).exists()) {
                cachedThumbnailUniqueFileName = generateUniqueFileName(mediaExtension)
            }

            // Create a new file in the directory
            val cachedThumbnailFile = File(cachedDir, cachedThumbnailUniqueFileName)

            val byteArrayOutputStream = ByteArrayOutputStream()

            thumbnailBitmap.compress(
                if (mediaExtension == ".jpg" || mediaExtension == ".jpeg" || mediaExtension == ".gif")
                    Bitmap.CompressFormat.JPEG
                else if (mediaExtension == ".png") Bitmap.CompressFormat.PNG
                else if (mediaExtension == ".webp")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        Bitmap.CompressFormat.WEBP_LOSSLESS
                    } else {
                        Bitmap.CompressFormat.WEBP
                    }
                else
                    throw UnsupportedOperationException("Unsupported file format $mediaExtension"),
                100,
                byteArrayOutputStream
            )

            byteArrayOutputStream.close()

            val blurredThumbnailBytes = byteArrayOutputStream.toByteArray()


            FileOutputStream(cachedThumbnailFile).use {
                it.write(compressImageAsByteArray(blurredThumbnailBytes))
            }


            // Get the app-specific media directory (this will return a list of directories)
            val mediaDirs = context.externalMediaDirs


            // Select the primary external media directory for your app
            val directory = File(
                mediaDirs[0],
                fileCategoryAndTypeByExtension.first
            ) // Create the "Super6 Images" folder within the first directory

            if (!File(directory, ".nomedia").exists()) {
                File(directory, ".nomedia").mkdirs()  // Create the directory if it doesn't exist
            }

            // Ensure the directory exists
            if (!directory.exists()) {
                directory.mkdirs()  // Create the directory if it doesn't exist
            }


            // Only generate a unique name for Image, Video
            if (fileCategoryAndTypeByExtension.second in listOf("Image", "Video")) {
                var uniqueFileName = generateUniqueFileName(mediaExtension)

                // Check if file exists, if so, regenerate the unique name
                while (File(directory, uniqueFileName).exists()) {
                    uniqueFileName = generateUniqueFileName(mediaExtension)
                }
                // Create a new file in the directory
                val file = File(directory, uniqueFileName)

                try {
                    FileOutputStream(file).use {
                        it.write(data)
                    }
                    // Return the absolute path of the saved file
                    return Pair(cachedThumbnailFile, file)
                } catch (e: IOException) {
                    e.printStackTrace()
                    return null
                }

            } else {
                return null
            }


        } else {
            return null
        }


    }


    suspend fun createBlurredThumbnailVideoAndWriteDataToAppSpecificFolder(
        context: Activity,
        inputStream: InputStream,
        fileName: String
    ): Triple<File, Triple<Int, Int, Long>, File>? {

        return withContext(Dispatchers.IO) {

            val mediaExtension = getFileExtension(fileName)


            val mediaDirs = context.externalMediaDirs

            val fileCategoryAndTypeByExtension = getFolderTypeByExtension(mediaExtension)


            val directory = File(
                mediaDirs[0],
                fileCategoryAndTypeByExtension.first
            )

            if (!File(directory, ".nomedia").exists()) {
                File(directory, ".nomedia").mkdirs()  // Create the directory if it doesn't exist
            }

            if (!directory.exists()) {
                directory.mkdirs()  // Create the directory if it doesn't exist
            }


            if (fileCategoryAndTypeByExtension.second in listOf("Video")) {
                var uniqueFileName = generateUniqueFileName(mediaExtension)

                // Check if file exists, if so, regenerate the unique name
                while (File(directory, uniqueFileName).exists()) {
                    uniqueFileName = generateUniqueFileName(mediaExtension)
                }

                val file = File(directory, uniqueFileName)
                try {

                    val buffer = ByteArray(1024 * 1024)

                    // Open the output file with FileOutputStream
                    FileOutputStream(file).use { fileOutputStream ->

                        inputStream.use { input ->
                            var bytesRead: Int
                            // Read data from input stream and write it to the output file
                            while (input.read(buffer)
                                    .also { bytesRead = it } != -1
                            ) {
                                fileOutputStream.write(
                                    buffer,
                                    0,
                                    bytesRead
                                ) // Write chunk to output
                                fileOutputStream.flush()

                            }
                        }


                    }


                    var videoWidth: Int
                    var videoHeight: Int
                    var totalDuration: Long
                    val thumbnail: Bitmap?
                    var rotation: Int

                    MediaMetadataRetriever()
                        .apply {
                            setDataSource(file.absolutePath)
                            thumbnail = getFrameAtTime(0)

                            rotation =
                                extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)?.toIntOrNull()
                                    ?: 0  // Fallback to 0 if rotation is null or invalid


                            videoWidth =
                                extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toInt()
                                    ?: -1
                            videoHeight =
                                extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toInt()
                                    ?: -1
                            totalDuration =
                                extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong()
                                    ?: -1L
                            release()
                        }

                    // Adjust width and height for rotation (swap width/height directly if rotated 90° or 270°)
                    if (rotation == 90 || rotation == 270) {
                        // Swap the width and height directly
                        val tempWidth = videoWidth
                        videoWidth = videoHeight
                        videoHeight = tempWidth
                    }

                    if (thumbnail == null || videoWidth < 0 || videoHeight < 0 || totalDuration <= 0L) {
                        throw IOException("Couldn't fetch media meta data")
                    }


                    val thumbnailByteArrayOutputStream = ByteArrayOutputStream()
                    thumbnail.compress(
                        Bitmap.CompressFormat.JPEG,
                        100,
                        thumbnailByteArrayOutputStream
                    )
                    thumbnailByteArrayOutputStream.close()

                    val thumbnailBytes = thumbnailByteArrayOutputStream.toByteArray()

                    val blurredThumbnailBitmap =
                        (context as ChatUtilNativeBaseActivity).createThumbnail(
                            BitmapFactory.decodeByteArray(
                                thumbnailBytes,
                                0,
                                thumbnailBytes.size
                            )
                        )


                    val cachedDir = context.getExternalFilesDir(null)


                    val cachedDirectory = File(
                        cachedDir,
                        fileCategoryAndTypeByExtension.first
                    )


                    if (!cachedDirectory.exists()) {
                        cachedDirectory.mkdirs()  // Create the directory if it doesn't exist
                    }

                    if (!File(cachedDirectory, ".nomedia").exists()) {
                        File(
                            cachedDirectory,
                            ".nomedia"
                        ).mkdirs()  // Create the directory if it doesn't exist
                    }

                    if (fileCategoryAndTypeByExtension.second in listOf("Image", "Video")) {
                        var cachedThumbnailUniqueFileName = generateUniqueFileName(".jpg")

                        // Check if file exists, if so, regenerate the unique name
                        while (File(cachedDirectory, cachedThumbnailUniqueFileName).exists()) {
                            cachedThumbnailUniqueFileName = generateUniqueFileName(".jpg")
                        }

                        // Create a new file in the directory
                        val cachedBlurredThumbnailFile =
                            File(cachedDir, cachedThumbnailUniqueFileName)


                        val byteArrayOutputStream = ByteArrayOutputStream()
                        blurredThumbnailBitmap.compress(
                            Bitmap.CompressFormat.JPEG,
                            100,
                            byteArrayOutputStream
                        )
                        byteArrayOutputStream.close()

                        val thumbnailData = byteArrayOutputStream.toByteArray()

                        FileOutputStream(cachedBlurredThumbnailFile).use {
                            it.write(compressImageAsByteArray(thumbnailData))

                        }

                        // Return the absolute path of the saved file
                        Triple(
                            cachedBlurredThumbnailFile,
                            Triple(videoWidth, videoHeight, totalDuration), file
                        )

                    } else {
                        null
                    }


                } catch (e: IOException) {
                    e.printStackTrace()
                    null
                }
            } else {
                null
            }
        }


    }


    suspend fun createBlurredThumbnailVideoFromUriAndWriteDataToAppSpecificFolder(
        context: Activity,
        data: Uri,
        fileName: String
    ): Pair<File, Triple<Int, Int, Long>>? {

        return withContext(Dispatchers.IO) {

            val mediaExtension = getFileExtension(fileName)

            val fileCategoryAndTypeByExtension = getFolderTypeByExtension(mediaExtension)

            if (fileCategoryAndTypeByExtension.second in listOf("Video")) {

                try {

                    var videoWidth: Int
                    var videoHeight: Int
                    var rotation: Int
                    var totalDuration: Long
                    val thumbnail: Bitmap?

                    val openFileDescriptor = context.contentResolver.openFileDescriptor(data, "r")
                        ?: throw IOException("Failed to open file descriptor")



                    MediaMetadataRetriever()
                        .apply {
                            setDataSource(openFileDescriptor.fileDescriptor)
                            thumbnail = getFrameAtTime(0)

                            rotation =
                                extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)?.toIntOrNull()
                                    ?: 0  // Fallback to 0 if rotation is null or invalid


                            videoWidth =
                                extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toInt()
                                    ?: -1
                            videoHeight =
                                extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toInt()
                                    ?: -1
                            totalDuration =
                                extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong()
                                    ?: -1L
                            release()
                            openFileDescriptor.close()
                        }


                    // Adjust width and height for rotation (swap width/height directly if rotated 90° or 270°)
                    if (rotation == 90 || rotation == 270) {
                        // Swap the width and height directly
                        val tempWidth = videoWidth
                        videoWidth = videoHeight
                        videoHeight = tempWidth
                    }

                    if (thumbnail == null || videoWidth < 0 || videoHeight < 0 || totalDuration <= 0L) {
                        throw IOException("Couldn't fetch media meta data")
                    }

                    val thumbnailByteArrayOutputStream = ByteArrayOutputStream()
                    thumbnail.compress(
                        Bitmap.CompressFormat.JPEG,
                        100,
                        thumbnailByteArrayOutputStream
                    )
                    thumbnailByteArrayOutputStream.close()

                    val thumbnailBytes = thumbnailByteArrayOutputStream.toByteArray()

                    val blurredThumbnailBitmap =
                        (context as ChatUtilNativeBaseActivity).createThumbnail(
                            BitmapFactory.decodeByteArray(
                                thumbnailBytes,
                                0,
                                thumbnailBytes.size
                            )
                        )

                    val cachedDir = context.getExternalFilesDir(null)


                    val cachedDirectory = File(cachedDir, fileCategoryAndTypeByExtension.first)


                    if (!cachedDirectory.exists()) {
                        cachedDirectory.mkdirs()  // Create the directory if it doesn't exist
                    }

                    if (!File(cachedDirectory, ".nomedia").exists()) {
                        File(
                            cachedDirectory,
                            ".nomedia"
                        ).mkdirs()  // Create the directory if it doesn't exist
                    }

                    if (fileCategoryAndTypeByExtension.second in listOf("Image", "Video")) {
                        var cachedThumbnailUniqueFileName = generateUniqueFileName(".jpg")

                        // Check if file exists, if so, regenerate the unique name
                        while (File(cachedDirectory, cachedThumbnailUniqueFileName).exists()) {
                            cachedThumbnailUniqueFileName = generateUniqueFileName(".jp")
                        }

                        // Create a new file in the directory
                        val cachedBlurredThumbnailFile =
                            File(cachedDir, cachedThumbnailUniqueFileName)


                        val byteArrayOutputStream = ByteArrayOutputStream()
                        blurredThumbnailBitmap.compress(
                            Bitmap.CompressFormat.JPEG,
                            100,
                            byteArrayOutputStream
                        )
                        byteArrayOutputStream.close()

                        val thumbnailData = byteArrayOutputStream.toByteArray()

                        FileOutputStream(cachedBlurredThumbnailFile).use {
                            it.write(compressImageAsByteArray(thumbnailData))
                        }

                        // Return the absolute path of the saved file
                        Pair(
                            cachedBlurredThumbnailFile,
                            Triple(videoWidth, videoHeight, totalDuration)
                        )

                    } else {
                        null
                    }


                } catch (e: IOException) {
                    e.printStackTrace()
                    null
                }
            } else {
                null
            }
        }


    }


    fun getAppSpecificMediaFolder(
        context: Activity,
        fileName: String,
    ): File {


        val extension = getFileExtension(fileName)
        // Get the app-specific media directory (this will return a list of directories)
        val mediaDirs = context.externalMediaDirs


        val fileCategoryAndTypeByExtension = getFolderTypeByExtension(extension)

        // Select the primary external media directory for your app
        val directory = File(
            mediaDirs[0],
            fileCategoryAndTypeByExtension.first
        ) // Create the "Super6 Images" folder within the first directory


        // Ensure the directory exists
        if (!directory.exists()) {
            directory.mkdirs()  // Create the directory if it doesn't exist
        }
        if (!File(directory, ".nomedia").exists()) {
            File(directory, ".nomedia").mkdirs()  // Create the directory if it doesn't exist
        }

        // Generate a unique file name and ensure it does not exist

        var uniqueFileName = fileName

        // Only generate a unique name for Image, Video, or Audio
        if (fileCategoryAndTypeByExtension.second in listOf("Image", "Video", "Audio")) {
            uniqueFileName = generateUniqueFileName(extension)

            // Check if file exists, if so, regenerate the unique name
            while (File(directory, uniqueFileName).exists()) {
                uniqueFileName = generateUniqueFileName(extension)
            }
        }



        return File(directory, uniqueFileName)
    }

    fun setSelectedMessage(selectedMessage: Message?) {
        _selectedMessage.value = selectedMessage
    }


    private var typingJob: Job? = null

    fun onUserTyping(userId: Long, recipientId: Long) {
        typingJob?.cancel()
        sendTypingEvent(userId, recipientId, true)
        typingJob = viewModelScope.launch {
            delay(1000L)
            sendTypingEvent(userId, recipientId, false)
        }
    }

    private fun sendTypingEvent(userId: Long, recipientId: Long, isTyping: Boolean) {
        socket?.emit("chat:typing",  JSONObject().apply {
            put("user_id", userId)
            put("recipient_id", recipientId)
            put("is_typing", isTyping)
        })
    }


    fun sendMessage(
        message: String,
        senderMessageId: Long = -1L,
        replyId: Long = -1L,
        updateLazyListState: (Int) -> Unit
    ) {


        if (message.isNotEmpty()) {
            viewModelScope.launch {

                socket?.let { nonNullSocket ->

                    val insertedId = repository.insertMessage(
                        Message(
                            chatId = chatId,
                            senderId = userId,
                            recipientId = recipientId,
                            content = message,
                            read = true,
                            timestamp = System.currentTimeMillis(),
                            senderMessageId = -1,
                            replyId = replyId
                        )
                    )


                    val jsonData = JSONObject().apply {
                        put("user_id", userId)
                        put("recipient_id", recipientId)
                    }

                    if (nonNullSocket.connected()) {

                        e2eeCredentials?.let {

                            try {

                                jsonData.put("message", encryptMessage(message, it.publicKey))
                                jsonData.put("key_version", it.keyVersion)
                                jsonData.put("message_id", insertedId)
                                jsonData.put("reply_id", senderMessageId)
                                jsonData.put("type", if (replyId != -1L) "reply" else "normal")
                                jsonData.put("category", "text")

                                nonNullSocket.emit("chat:chatMessage", jsonData, Ack { args ->

                                    if (args.isNotEmpty()) {
                                        val data = args[1] as? JSONObject

                                        val status = data?.optString("status")

                                        if (status != null && status == "KEY_ERROR") {
                                            val recipientId = data.getLong("recipient_id")
                                            val publicKey = data.getString("publicKey")
                                            val keyVersion = data.getLong("keyVersion")

                                            viewModelScope.launch {
                                                repository.updatePublicKeyByRecipientId(
                                                    recipientId,
                                                    publicKey,
                                                    keyVersion
                                                )

                                                repository.updateMessage(
                                                    insertedId,
                                                    ChatMessageStatus.QUEUED
                                                )
                                                val queuedMessages = repository.getQueuedMessages()

                                                queuedMessages.forEach {
                                                    if (nonNullSocket.connected()) {

                                                        val queuedData = JSONObject().apply {
                                                            put("user_id", userId)
                                                            put("recipient_id", it.recipientId)
                                                            put(
                                                                "message",
                                                                encryptMessage(message, publicKey)
                                                            )
                                                            put("key_version", keyVersion)
                                                            put("message_id", it.id)
                                                            put("reply_id", it.replyId)
                                                            put(
                                                                "type",
                                                                if (it.replyId != -1L) "reply" else "normal"
                                                            )
                                                            put("category", "text")

                                                        }

                                                        nonNullSocket.emit(
                                                            "chat:chatMessage",
                                                            queuedData,
                                                            Ack { args ->
                                                                // Handle the acknowledgment from the server
                                                                if (args.isNotEmpty()) {

                                                                    val queuedReceivedData =
                                                                        args[1] as? JSONObject
                                                                    val queuedStatus =
                                                                        queuedReceivedData?.optString(
                                                                            "status"
                                                                        )
                                                                    if (queuedStatus != null && queuedStatus == "KEY_ERROR") {
                                                                        viewModelScope.launch {
                                                                            repository.updateMessage(
                                                                                it.id,
                                                                                ChatMessageStatus.FAILED
                                                                            )
                                                                        }
                                                                    } else if (queuedStatus != null && queuedStatus == "USER_NOT_ACTIVE_ERROR") {
                                                                        viewModelScope.launch {
                                                                            repository.updateMessage(
                                                                                it.id,
                                                                                ChatMessageStatus.FAILED
                                                                            )
                                                                        }
                                                                    } else {
                                                                        viewModelScope.launch {
                                                                            repository.updateMessage(
                                                                                it.id,
                                                                                ChatMessageStatus.SENT
                                                                            )
                                                                        }

                                                                    }


                                                                }
                                                            })

                                                    }
                                                }
                                            }


                                        }
                                        else if (status != null && status == "USER_NOT_ACTIVE_ERROR") {
                                            viewModelScope.launch {
                                                repository.updateMessage(
                                                    insertedId,
                                                    ChatMessageStatus.FAILED
                                                )
                                            }
                                        } else {
                                            viewModelScope.launch {
                                                repository.updateMessage(
                                                    insertedId,
                                                    ChatMessageStatus.SENT
                                                )
                                                updateLazyListState(0)
                                            }
                                        }
                                    }
                                })

                            } catch (e: Exception) {
                                e.printStackTrace()
                                repository.updateMessage(insertedId, ChatMessageStatus.FAILED)
                            }

                        } ?: run {
                            nonNullSocket.emit("chat:queryPublicKey", jsonData, Ack { args ->
                                // Handle the acknowledgment from the server
                                if (args.isNotEmpty()) {
                                    //val message = args[0] as String
                                    val response = args[1] as JSONObject

                                    val recipientId = response.getLong("recipient_id")
                                    val publicKey = response.getString("publicKey")
                                    val keyVersion = response.getLong("keyVersion")

                                    viewModelScope.launch {
                                        repository.updatePublicKeyByRecipientId(
                                            recipientId,
                                            publicKey,
                                            keyVersion
                                        )
                                        repository.updateMessage(
                                            insertedId,
                                            ChatMessageStatus.QUEUED
                                        )
                                        val queuedMessages = repository.getQueuedMessages()

                                        queuedMessages.forEach {
                                            if (nonNullSocket.connected()) {

                                                val queuedData = JSONObject().apply {
                                                    put("user_id", userId)
                                                    put("recipient_id", it.recipientId)
                                                    put(
                                                        "message",
                                                        encryptMessage(message, publicKey)
                                                    )
                                                    put("key_version", keyVersion)
                                                    put("message_id", it.id)
                                                    put("reply_id", it.replyId)
                                                    put(
                                                        "type",
                                                        if (it.replyId != -1L) "reply" else "normal"
                                                    )
                                                    put("category", "text")

                                                }

                                                nonNullSocket.emit(
                                                    "chat:chatMessage",
                                                    queuedData,
                                                    Ack { args ->
                                                        // Handle the acknowledgment from the server
                                                        if (args.isNotEmpty()) {

                                                            val queuedReceivedData =
                                                                args[1] as? JSONObject
                                                            val queuedStatus =
                                                                queuedReceivedData?.optString("status")
                                                            if (queuedStatus != null && queuedStatus == "KEY_ERROR") {
                                                                viewModelScope.launch {
                                                                    repository.updateMessage(
                                                                        it.id,
                                                                        ChatMessageStatus.FAILED
                                                                    )
                                                                }
                                                            } else if (queuedStatus != null && queuedStatus == "USER_NOT_ACTIVE_ERROR") {
                                                                viewModelScope.launch {
                                                                    repository.updateMessage(
                                                                        it.id,
                                                                        ChatMessageStatus.FAILED
                                                                    )
                                                                }
                                                            } else {
                                                                viewModelScope.launch {
                                                                    repository.updateMessage(
                                                                        it.id,
                                                                        ChatMessageStatus.SENT
                                                                    )
                                                                }

                                                            }

                                                        }
                                                    })

                                            }
                                        }

                                    }

                                } else {
                                    viewModelScope.launch {
                                        repository.updateMessage(
                                            insertedId,
                                            ChatMessageStatus.FAILED
                                        )
                                        updateLazyListState(0)

                                    }
                                }

                            })
                        }


                    } else {
                        // Optionally, you can store the message in a pending state or notify the user
                        viewModelScope.launch {
                            repository.updateMessage(insertedId, ChatMessageStatus.QUEUED)
                            updateLazyListState(0)
                        }
                    }
                } ?: run {
                    repository.insertMessage(
                        Message(
                            chatId = chatId,
                            senderId = userId,
                            recipientId = recipientId,
                            content = message,
                            read = true,
                            timestamp = System.currentTimeMillis(),
                            senderMessageId = -1,
                            replyId = replyId,
                            status = ChatMessageStatus.QUEUED
                        )
                    )
                }
            }
        }
    }


    fun markMessageAsRead(ids: List<Long>) {
        viewModelScope.launch {
            repository.markMessageAsRead(ids)
        }
    }


    fun formatMessageReceived(timestamp: Long): String {

        return Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())  // Adjust ZoneId if needed
            .toLocalTime().format(DateTimeFormatter.ofPattern("h:mm a"))  // Format as "h:mm a"

    }


    private fun lastSeenTimestamp(timestamp: String): String {
        // Define the input format
        val inputFormatter = DateTimeFormatter.ISO_DATE_TIME

        // Parse the timestamp to a ZonedDateTime object
        val dateTime = ZonedDateTime.parse(timestamp, inputFormatter)

        // Determine the date of the timestamp
        val dateOfTimestamp = dateTime.toLocalDate()

        // Get the current date
        val now = LocalDate.now()

        // Check if the date is today
        if (dateOfTimestamp.isEqual(now)) {
            // Format: HH:mm a (e.g., 02:00 AM)
            val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")
            return dateTime.withZoneSameInstant(ZoneId.systemDefault()).format(timeFormatter)
        }

        // Check if the date is yesterday
        if (dateOfTimestamp.isEqual(now.minus(1, ChronoUnit.DAYS))) {
            // Format: Yesterday HH:mm a (e.g., Yesterday 12:00 AM)
            val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")
            val timePart =
                dateTime.withZoneSameInstant(ZoneId.systemDefault()).format(timeFormatter)
            return "Yesterday $timePart"
        }

        // Check if the date is within the last 7 days and return the weekday name
        if (!dateOfTimestamp.isBefore(now.minus(7, ChronoUnit.DAYS))) {
            val dayOfWeek = dateOfTimestamp.dayOfWeek
            val weekdayName = dayOfWeek.name.lowercase()
                .replaceFirstChar { it.uppercase() } // Capitalize the first letter


            // Format time part for the last 7 days
            val timeOfTimestamp = dateTime.toLocalTime() // Get time from ZonedDateTime
            val timeFormatted = timeOfTimestamp.format(DateTimeFormatter.ofPattern("h:mm a"))

            return "$weekdayName $timeFormatted"

        }

        // For any other date, format as: MMM d, yyyy h:mm a (e.g., Sep 12, 2024 5:37 AM)
        val dateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a")
        return dateTime.withZoneSameInstant(ZoneId.systemDefault()).format(dateTimeFormatter)
    }


    fun findMessageIndex(messages: Map<String, List<MessageWithReply>>, messageId: Long): Int {

        return repository.flattenMessagesWithHeaders(messages)
            .indexOfFirst { it.itemType == ItemType.MESSAGE && it.message!!.receivedMessage.id == messageId }
    }


    fun flattenMessagesWithHeaders(groupedMessages: Map<String, List<MessageWithReply>>): List<MessageItem> {
        return repository.flattenMessagesWithHeaders(groupedMessages)
    }


    // Clean up observer when ViewModel is cleared
    override fun onCleared() {
        super.onCleared()

        socket?.off("onlineStatus-${recipientId}", onlineStatusHandler)
        socket?.off("is_typing", typingHandler)
    }


//    private fun playMessageTone() {
//        mediaPlayer?.start()
//    }
//
//    private fun initMediaPlayer() {
//        mediaPlayer = MediaPlayer.create(this, R.raw.message_tone)
//    }
//
//    private fun releaseMediaPlayer() {
//        mediaPlayer?.release()
//        mediaPlayer = null
//    }
//
//    private fun stopMediaPlayer() {
//        mediaPlayer?.stop()
//        mediaPlayer?.release()
//        mediaPlayer = null
//    }
}


