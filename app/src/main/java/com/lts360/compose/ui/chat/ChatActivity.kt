package com.lts360.compose.ui.chat


import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.KeyboardDoubleArrowDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import com.lts360.App
import com.lts360.R
import com.lts360.api.auth.managers.socket.SocketManager
import com.lts360.api.models.service.FeedUserProfileInfo
import com.lts360.app.database.daos.chat.ChatUserDao
import com.lts360.app.database.daos.chat.MessageDao
import com.lts360.app.database.daos.chat.MessageMediaMetaDataDao
import com.lts360.app.database.models.chat.ChatMessageType
import com.lts360.app.database.models.chat.MessageWithReply
import com.lts360.app.database.models.chat.ThumbnailLoader.getThumbnailBitmap
import com.lts360.app.workers.chat.utils.getFileExtension
import com.lts360.components.findActivity
import com.lts360.components.utils.compressImageAsByteArray
import com.lts360.compose.ui.auth.navhost.noTransitionComposable
import com.lts360.compose.ui.chat.camera.CameraVisualPickerActivityContracts
import com.lts360.compose.ui.chat.panel.ChatTopBarScreen
import com.lts360.compose.ui.chat.panel.CustomWavyTypingIndicator
import com.lts360.compose.ui.chat.panel.DecryptionSafeGuard
import com.lts360.compose.ui.chat.panel.E2EEEMessageHeader
import com.lts360.compose.ui.chat.panel.MessageDateHeader
import com.lts360.compose.ui.chat.panel.ProfileHeader
import com.lts360.compose.ui.chat.panel.StringTextField
import com.lts360.compose.ui.chat.panel.message.ChatMeAudioMessageItem
import com.lts360.compose.ui.chat.panel.message.ChatMeFileMessageItem
import com.lts360.compose.ui.chat.panel.message.ChatMeImageMessageItem
import com.lts360.compose.ui.chat.panel.message.ChatMeMessageItem
import com.lts360.compose.ui.chat.panel.message.ChatMeVideoMessageItem
import com.lts360.compose.ui.chat.panel.message.ChatOtherAudioMessageItem
import com.lts360.compose.ui.chat.panel.message.ChatOtherFileMessageItem
import com.lts360.compose.ui.chat.panel.message.ChatOtherImageMessageItem
import com.lts360.compose.ui.chat.panel.message.ChatOtherMessageItem
import com.lts360.compose.ui.chat.panel.message.ChatOtherVideoMessageItem
import com.lts360.compose.ui.chat.panel.reply.OverAllMeRepliedMessageItem
import com.lts360.compose.ui.chat.panel.reply.OverAllOtherRepliedMessageItem
import com.lts360.compose.ui.chat.panel.reply.ReplyMessageContent
import com.lts360.compose.ui.chat.panel.reply.ReplyMessageVideoMediaContent
import com.lts360.compose.ui.chat.panel.reply.ReplyMessageVisualMediaContent
import com.lts360.compose.ui.chat.repos.ChatUserRepository
import com.lts360.compose.ui.chat.viewmodels.ChatActivityViewModel
import com.lts360.compose.ui.chat.viewmodels.ChatViewModel
import com.lts360.compose.ui.chat.viewmodels.factories.ChatActivityViewModelFactory
import com.lts360.compose.ui.main.MainActivity
import com.lts360.compose.ui.main.navhosts.routes.MainRoutes
import com.lts360.compose.ui.theme.AppTheme
import com.lts360.compose.ui.utils.FormatterUtils.formatTimeSeconds
import com.lts360.compose.ui.utils.touchConsumer
import com.lts360.compose.utils.ChatMessageLinkPreviewHeader
import com.lts360.compose.utils.ChatMessageLinkPreviewHeaderLoading
import com.lts360.compose.utils.SafeDrawingBox
import com.lts360.libs.visualpicker.GalleryVisualPagerActivityResultContracts
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@AndroidEntryPoint
class ChatActivity : ChatUtilNativeBaseActivity() {

    @Inject
    lateinit var chatUserDao: ChatUserDao

    @Inject
    lateinit var messageDao: MessageDao

    @Inject
    lateinit var messageMediaMetadataDao: MessageMediaMetaDataDao

    @Inject
    lateinit var chatUserRepository: ChatUserRepository

    @Inject
    lateinit var socketManager: SocketManager


    private lateinit var chatActivityViewModel: ChatActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val senderId = intent.getLongExtra("sender_id", -1)

        chatActivityViewModel = ViewModelProvider(
            this,
            ChatActivityViewModelFactory(
                application as App,
                chatUserDao, messageDao, senderId, messageMediaMetadataDao
            )
        )[ChatActivityViewModel::class.java]

        lifecycleScope.launch {

            if (!chatActivityViewModel.loadingComplete.isCompleted) {
                chatActivityViewModel.loadingComplete.await()
            }

            val userState = chatActivityViewModel.selectedChatUser.value

            if (userState == null) {
                startActivity(Intent(this@ChatActivity, MainActivity::class.java))
                finishAffinity()
                return@launch
            }

            setContent {
                AppTheme {
                    Surface {
                        SafeDrawingBox {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {


                                val navHostController = rememberNavController()
                                NavHost(
                                    navHostController,
                                    MainRoutes.ChatWindow(userState.chatUser.chatId, senderId)
                                ) {
                                    noTransitionComposable<MainRoutes.ChatWindow> {
                                        NotificationChatScreen(
                                            { uri, videoWidth, videoHeight, totalDuration ->
                                                openPlayerActivity(
                                                    this@ChatActivity,
                                                    uri,
                                                    videoWidth,
                                                    videoHeight,
                                                    totalDuration
                                                )
                                            },
                                            { uri, imageWidth, imageHeight ->
                                                openImageSliderActivity(
                                                    this@ChatActivity,
                                                    uri,
                                                    imageWidth,
                                                    imageHeight
                                                )
                                            },
                                            chatActivityViewModel,
                                            userState.firstVisibleItemIndex,
                                            { navHostController.popBackStack() }
                                        )
                                    }
                                }

                            }
                        }
                    }
                }
            }

        }

    }

}

fun openPlayerActivity(
    context: Context,
    uri: Uri,
    videoWidth: Int,
    videoHeight: Int,
    totalDuration: Long
) {
    context.startActivity(
        Intent(context, ChatPlayerActivity::class.java).apply {
            setDataAndType(uri, "video/*")
            putExtra("videoWidth", videoWidth)
            putExtra("videoHeight", videoHeight)
            putExtra("totalDuration", totalDuration)
        }
    )
}

fun openImageSliderActivity(context: Context, uri: Uri, imageWidth: Int, imageHeight: Int) {
    context.startActivity(
        Intent(context, ChatImagesSliderActivity::class.java).apply {
            setDataAndType(uri, "image/*")
            putExtra("imageWidth", imageWidth)
            putExtra("imageHeight", imageHeight)
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun ChatPanel(
    chatUsersProfileImageLoader: ImageLoader,
    userProfileInfo: FeedUserProfileInfo,
    groupedMessages: Map<String, List<MessageWithReply>>,
    isMessagesLoaded: Boolean,
    lazyListState: LazyListState,
    onNavigateVideoPlayer: (Uri, Int, Int, Long) -> Unit,
    onNavigateImageSlider: (Uri, Int, Int) -> Unit,
    viewModel: ChatViewModel,
    onPopBackStack: () -> Unit
) {


    val userId = viewModel.userId

    val context = LocalContext.current

    var value by remember { mutableStateOf("") }

    val textFieldState = remember {

        TextFieldState(
            initialText = value,
            initialSelection = TextRange(value.length)
        )
    }

    val clipBoard = LocalClipboard.current

    val onlineStatus by viewModel.onlineStatus.collectAsState()


    val isTyping by viewModel.isTyping.collectAsState()


    var updatedHeader by rememberSaveable { mutableStateOf(Pair("", "")) }

    val selectedMessage by viewModel.selectedMessage.collectAsState()

    val selectedMessageMessageMediaMetadata by viewModel.selectedMessageMessageMediaMetadata.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    val replyMessageBottomSheetState = rememberModalBottomSheetState()

    var replyMessageBottomSheet by rememberSaveable { mutableStateOf(false) }

    var showReplyContent by rememberSaveable { mutableStateOf(false) }

    var highlightedMessageId by remember { mutableStateOf<Long?>(null) }


    val highlightedMessageBackgroundColor by animateColorAsState(
        targetValue = if (highlightedMessageId != null) Color(0xFFE5F9FF) else Color.Unspecified,
        animationSpec = tween(durationMillis = 500),
        label = ""
    )


    val isGoToBottom by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex != 0 && lazyListState.layoutInfo.totalItemsCount > 0
        }
    }


    var isLinkPreviewAvailable by remember { mutableStateOf(false) }

    // State for the link preview
    var linkPreviewData by remember { mutableStateOf<LinkPreviewData?>(null) }


    var isVisibleMediaLibrary by remember { mutableStateOf(false) }
    val hideMediaLibrary: () -> Unit = {
        if (isVisibleMediaLibrary) {
            isVisibleMediaLibrary = false
        }
    }

    fun processMediaUri(uri: Uri) {
        try {


            if (showReplyContent) {
                showReplyContent = false
            }
            selectedMessage?.let {
                viewModel.setSelectedMessage(null)
            }


            val contentResolver = context.contentResolver

            // Query to get the file name
            val cursor = contentResolver.query(uri, null, null, null, null)

            val (fileName, fileSize) = cursor?.use {
                if (it.moveToFirst()) {
                    val name =
                        it.getString(it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                    val size = it.getLong(it.getColumnIndexOrThrow(OpenableColumns.SIZE))
                    name to size
                } else {
                    null to 0L  // Default values if no data is found
                }
            } ?: (null to 0L)  // Default values if cursor is null


            // If file name is null, show a toast and return
            if (fileName == null || fileSize == 0L) {
                Toast.makeText(context, "Error processing media", Toast.LENGTH_SHORT).show()
                return
            }

            val mimeType = contentResolver.getType(uri)
            var width: Int
            var height: Int

            val mediaExtension = getFileExtension(fileName)

            mimeType?.let { nonNullMimeType ->
                if (nonNullMimeType.startsWith("image")) {

                    if (mediaExtension == ".gif") {

                        contentResolver.openInputStream(uri)?.use { stream ->

                            val imageBytes = stream.readBytes()

                            val options =
                                BitmapFactory.Options().apply { inJustDecodeBounds = true }
                            BitmapFactory.decodeByteArray(
                                imageBytes,
                                0,
                                imageBytes.size,
                                options
                            )
                            width = options.outWidth
                            height = options.outHeight

                            /*
                            if (width > 1280 || height > 1280 || imageBytes.size > 1048576) {
                                letCompressedImageBytes = compressImage(imageBytes)
                            }*/

                            viewModel.createBlurredThumbnailAndWriteGifDataToAppSpecificFolder(
                                context.findActivity(),
                                imageBytes,
                                fileName
                            )?.let { file ->


                                val originalFile = file.second

                                val fileUri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.provider", // File provider authority
                                    originalFile
                                )

                                viewModel.insertVisualMediaMessageAndSend(
                                    file.first,
                                    userId,
                                    viewModel.recipientId,
                                    originalFile.name,
                                    fileUri.toString(),
                                    originalFile.length(),
                                    width,
                                    height,
                                    selectedMessage?.senderMessageId ?: -1,
                                    selectedMessage?.id ?: -1
                                )

                            } ?: run {
                                Toast.makeText(
                                    context,
                                    "Error processing media",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                        } ?: run {
                            Toast.makeText(
                                context,
                                "Error processing media",
                                Toast.LENGTH_SHORT
                            ).show()
                        }


                    } else {
                        contentResolver.openInputStream(uri)?.use { stream ->

                            val imageBytes = stream.readBytes()

                            val options =
                                BitmapFactory.Options().apply { inJustDecodeBounds = true }
                            BitmapFactory.decodeByteArray(
                                imageBytes,
                                0,
                                imageBytes.size,
                                options
                            )
                            width = options.outWidth
                            height = options.outHeight


                            var letCompressedImageBytes = imageBytes

                            if (width > 1280 || height > 1280 || imageBytes.size > 1048576) {
                                letCompressedImageBytes =
                                    compressImageAsByteArray(imageBytes)
                            }

                            viewModel.createBlurredThumbnailAndWriteDataToAppSpecificFolder(
                                context.findActivity(),
                                letCompressedImageBytes,
                                fileName
                            )?.let { file ->


                                val originalFile = file.second

                                val fileUri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.provider", // File provider authority
                                    originalFile
                                )

                                viewModel.insertVisualMediaMessageAndSend(
                                    file.first,
                                    userId,
                                    viewModel.recipientId,
                                    originalFile.name,
                                    fileUri.toString(),
                                    originalFile.length(),
                                    width,
                                    height,
                                    selectedMessage?.senderMessageId ?: -1,
                                    selectedMessage?.id ?: -1
                                )

                            } ?: run {
                                Toast.makeText(
                                    context,
                                    "Error processing media",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                        } ?: run {
                            Toast.makeText(
                                context,
                                "Error processing media",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }


                } else if (nonNullMimeType.startsWith("video")) {

                    coroutineScope.launch {
                        if (fileSize > 64 * 1024 * 1024) {

                            viewModel.createBlurredThumbnailVideoFromUriAndWriteDataToAppSpecificFolder(
                                context.findActivity(),
                                uri,
                                fileName
                            )?.let { file ->

                                width = file.second.first
                                height = file.second.second


                                viewModel.insertVisualMediaMessageAndSend(
                                    file.first,
                                    userId,
                                    viewModel.recipientId,
                                    fileName,
                                    uri.toString(),
                                    fileSize,
                                    width,
                                    height,
                                    selectedMessage?.senderMessageId ?: -1,
                                    selectedMessage?.id ?: -1,
                                    file.second.third
                                )

                            } ?: run {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        context,
                                        "Error processing media",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                            }

                        } else {

                            contentResolver.openInputStream(uri)
                                ?.let { nonNullInputStream ->

                                    viewModel.createBlurredThumbnailVideoAndWriteDataToAppSpecificFolder(
                                        context.findActivity(),
                                        nonNullInputStream,
                                        fileName
                                    )?.let { file ->

                                        width = file.second.first
                                        height = file.second.second

                                        val originalFile = file.third

                                        val fileUri = FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.provider", // File provider authority
                                            originalFile
                                        )

                                        viewModel.insertVisualMediaMessageAndSend(
                                            file.first,
                                            userId,
                                            viewModel.recipientId,
                                            originalFile.name,
                                            fileUri.toString(),
                                            originalFile.length(),
                                            width,
                                            height,
                                            selectedMessage?.senderMessageId ?: -1,
                                            selectedMessage?.id ?: -1,
                                            file.second.third
                                        )


                                    } ?: run {
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(
                                                context,
                                                "Error processing media",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }

                                    }

                                }

                        }

                    }

                } else {

                    contentResolver.openInputStream(uri)?.readBytes()
                        ?.let { nonNullMediaBytes ->

                            viewModel.writeDataToAppSpecificFolder(
                                context.findActivity(),
                                nonNullMediaBytes,
                                fileName
                            )?.let { file ->

                                val totalDuration = if (mimeType.startsWith("audio")) {
                                    val retriever = MediaMetadataRetriever()
                                    try {
                                        retriever.setDataSource(file.absolutePath)
                                        val durationStr =
                                            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                                        durationStr?.toLong()
                                            ?: 0L // Return the duration in milliseconds
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        -1L // Return 0 in case of any error
                                    } finally {
                                        retriever.release()
                                    }
                                } else {
                                    -1L // Return -1 if not an audio file
                                }

                                val mediaUri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.provider", // File provider authority
                                    file
                                )
                                viewModel.insertMediaMessageAndSend(
                                    file,
                                    mediaUri.toString(),
                                    userId,
                                    viewModel.recipientId,
                                    selectedMessage?.senderMessageId ?: -1,
                                    selectedMessage?.id ?: -1,
                                    totalDuration
                                )

                            } ?: run {
                                Toast.makeText(
                                    context,
                                    "Error processing media",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }


                        } ?: run {
                        Toast.makeText(
                            context,
                            "Error processing media",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }
            }

        } catch (e: Exception) {
            Toast.makeText(context, "${e.message}", Toast.LENGTH_SHORT)
                .show()
        }
    }


    val cameraLauncher = rememberLauncherForActivityResult(
        CameraVisualPickerActivityContracts.TakeCameraMedia()
    ) {
        if (isVisibleMediaLibrary) {
            hideMediaLibrary()
        }

        it?.let {
            processMediaUri(it)
        } ?: run {
            Toast.makeText(
                context,
                "Failed to capture",
                Toast.LENGTH_SHORT
            ).show()
        }

    }


    val documentTreeLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
            hideMediaLibrary()
            it?.let {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
                processMediaUri(it)
            }
        }

    val galleryVisualLauncher = rememberLauncherForActivityResult(
        GalleryVisualPagerActivityResultContracts.PickSingleVisual()
    ) {
        hideMediaLibrary()
        it?.let {
            processMediaUri(it)
        }
    }


    LaunchedEffect(value) {

        val firstLink = Regex(
            "(?<!\\S)(https?|ftp)://([a-zA-Z0-9\\-]+\\.)+[a-zA-Z]{2,}(/[^\\s]*)?|(?:www\\.)[a-zA-Z0-9\\-]+\\.[a-zA-Z]{2,}(/[^\\s]*)?",
            setOf(RegexOption.IGNORE_CASE)
        ).find(value)?.value


        if (firstLink != null) {
            isLinkPreviewAvailable = true
            linkPreviewData = fetchLinkPreview(firstLink)
            if (linkPreviewData == null) {
                isLinkPreviewAvailable = false
            }

        } else {
            isLinkPreviewAvailable = false
            linkPreviewData = null
        }
    }



    LaunchedEffect(groupedMessages) {
        snapshotFlow { lazyListState.layoutInfo.visibleItemsInfo }
            .collect { visibleItems ->
                val firstVisibleItemIndex = visibleItems.firstOrNull()?.index ?: -1

                val allMessages = groupedMessages.values.flatten()

                highlightedMessageId?.let { nonNullHighlightedMessageId ->


                    val isVisible = visibleItems
                        .mapNotNull { it.key as? Long }
                        .firstOrNull { id -> id == nonNullHighlightedMessageId } != null


                    if (isVisible) {
                        delay(1500)
                        highlightedMessageId = null
                    }
                }

                if (firstVisibleItemIndex >= 0) {


                    val unreadMessage = visibleItems
                        .mapNotNull { it.key as? Long } // Convert visible items to their IDs
                        .mapNotNull { id -> allMessages.find { it.receivedMessage.id == id } } // Find the message by ID
                        .firstOrNull { !it.receivedMessage.read } // Find the first unread message


                    unreadMessage?.let { nonNullUnreadMessage ->
                        val unreadId =
                            nonNullUnreadMessage.receivedMessage.id // Store the ID of the first unread message
                        val unreadIndex =
                            allMessages.indexOfFirst { it.receivedMessage.id == unreadId } // Find the index of that message

                        // In reverse layout, take all messages starting from the unread message to the bottom of the list
                        val unreadMessageIds = allMessages
                            .takeLast(allMessages.size - unreadIndex) // Take all messages from the first unread to the end (reverse layout)
                            .filter { !it.receivedMessage.read } // Filter unread messages
                            .map { it.receivedMessage.id } // Get their IDs


                        // If there are unread messages, mark them as read
                        if (unreadMessageIds.isNotEmpty()) {
                            viewModel.markMessageAsRead(unreadMessageIds)
                        }
                    }
                        ?: run {


                            val visibleMessage = visibleItems
                                .mapNotNull { it.key as? Long }
                                .firstNotNullOfOrNull { id -> allMessages.find { it.receivedMessage.id == id } }

                            if (visibleMessage != null) {

                                val messageIndex =
                                    allMessages.indexOfFirst { it.receivedMessage.id == visibleMessage.receivedMessage.id } // Find the index of that message


                                val messageIds = allMessages
                                    .takeLast(allMessages.size - messageIndex) // Take all messages from the first unread to the end (reverse layout)
                                    .filter { !it.receivedMessage.read } // Filter unread messages
                                    .map { it.receivedMessage.id }

                                if (messageIds.isNotEmpty()) {
                                    viewModel.markMessageAsRead(messageIds)
                                }

                            }

                        }

                }


                val lastHeader = visibleItems.lastOrNull { it.key.toString().startsWith("header-") }

                if (lastHeader == null) {

                    // If no header is found, find the most recent message (first visible message in reverse layout)
                    val firstMessage = visibleItems
                        .mapNotNull { visibleItem ->
                            // Safely check if the key is a Long before attempting to use it
                            (visibleItem.key as? Long)?.let { id ->
                                allMessages.find { it.receivedMessage.id == id }  // Find the corresponding message in allMessages
                            }
                        }
                        .lastOrNull()  // Get the most recent message from visible items

                    firstMessage?.let { nonNullFirstMessage ->
                        for ((key, messages) in groupedMessages) {
                            // Find the message that matches the first visible message
                            val foundMessage =
                                messages.find { nonNullFirstMessage.receivedMessage.id == it.receivedMessage.id }

                            if (foundMessage != null) {
                                // If the message is found, we determine the header direction
                                updatedHeader = Pair(
                                    "down",
                                    key
                                )  // Scroll down (to older messages) in reverse layout
                                break // Exit the loop once the header is found
                            }
                        }
                    }
                } else {

                    // If a header is found, determine which direction to scroll
                    val headerKey = lastHeader.key.toString()
                    val currentHeader = headerKey.removePrefix("header-")

                    val nextHeaderIndex =
                        groupedMessages.keys.indexOf(currentHeader) + 1 // Find the next header key for reverse layout

                    val lastHeaderNextItemIndex = lastHeader.index + 1

                    // Check if the next item is visible in the layout
                    val isNextItemVisible = visibleItems.any { it.index == lastHeaderNextItemIndex }

                    // Now determine the scroll direction based on the reverse layout logic
                    updatedHeader =
                        if (nextHeaderIndex < groupedMessages.keys.size && isNextItemVisible) {
                            // If there's a next header and it's visible, scroll "up" to the next group (newer messages in reverse)
                            Pair(
                                "up",
                                groupedMessages.keys.elementAt(nextHeaderIndex)
                            )  // Scroll up (to newer message group in reverse layout)
                        } else {
                            // Otherwise, scroll "down" to older messages
                            Pair("down", currentHeader)  // Scroll down (to older message group)
                        }
                }


            }

    }



    LaunchedEffect(replyMessageBottomSheet) {

        if (replyMessageBottomSheet) {
            replyMessageBottomSheetState.expand()
        } else {
            replyMessageBottomSheetState.hide()
        }
    }


    Surface {
        Column(modifier = Modifier.fillMaxSize()) {
            SharedTransitionLayout(modifier = Modifier.fillMaxSize()) {
                var isExpanded by remember { mutableStateOf(false) }

                BackHandler(isExpanded) {
                    isExpanded = false
                }

                AnimatedContent(
                    targetState = isExpanded, label = "",
                    transitionSpec = {
                        ContentTransform(
                            targetContentEnter = fadeIn(),
                            initialContentExit = fadeOut(),
                            sizeTransform = SizeTransform { _, _ ->
                                tween(durationMillis = 0)
                            }
                        )
                    }) { target ->

                    if (!target) {

                        Column(modifier = Modifier.fillMaxSize()) {

                            Surface(shadowElevation = 4.dp, modifier = Modifier.fillMaxWidth()) {

                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .then(
                                            if (isVisibleMediaLibrary) Modifier.touchConsumer(
                                                pass = PointerEventPass.Initial,
                                                onDown = {
                                                    hideMediaLibrary()
                                                }
                                            ) else Modifier)
                                        .clickable {
                                            isExpanded = !isExpanded
                                        },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    IconButton(onClick = dropUnlessResumed { onPopBackStack() }) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Back"
                                        )
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp)
                                            .weight(1f),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Start
                                    ) {

                                        Column(
                                            modifier = Modifier
                                                .wrapContentWidth()
                                                .padding(end = 8.dp)
                                        ) {

                                            AsyncImage(
                                                ImageRequest.Builder(context)
                                                    .data(userProfileInfo.profilePicUrl96By96)
                                                    .placeholder(R.drawable.user_placeholder)
                                                    .error(R.drawable.user_placeholder)
                                                    .crossfade(true)
                                                    .build(),
                                                imageLoader = chatUsersProfileImageLoader,
                                                contentDescription = "User Profile Image",
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .sharedBounds(
                                                        rememberSharedContentState(key = "profile-pic-${userProfileInfo.userId}"),
                                                        animatedVisibilityScope = this@AnimatedContent
                                                    )
                                                    .clip(CircleShape),
                                                contentScale = ContentScale.Crop
                                            )
                                        }

                                        Column(modifier = Modifier.wrapContentWidth()) {

                                            Text(
                                                text = "${userProfileInfo.firstName} ${userProfileInfo.lastName ?: ""}",
                                                style = MaterialTheme.typography.bodyMedium
                                            )

                                            if (onlineStatus.isNotEmpty()) {
                                                Text(
                                                    text = onlineStatus,
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                            }

                                        }
                                    }
                                }
                            }

                            Box(modifier = Modifier.fillMaxSize()) {

                                if (!isMessagesLoaded) {

                                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                                } else {

                                    Column(modifier = Modifier.fillMaxSize()) {

                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .weight(1f)
                                        ) {

                                            LazyColumn(
                                                state = lazyListState,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .align(Alignment.BottomCenter)
                                                    .then(
                                                        if (isVisibleMediaLibrary) Modifier.touchConsumer(
                                                            pass = PointerEventPass.Initial,
                                                            onDown = {
                                                                hideMediaLibrary()
                                                            }
                                                        ) else Modifier),
                                                reverseLayout = true
                                            ) {


                                                groupedMessages.forEach { (day, messages) ->

                                                    items(
                                                        messages,
                                                        key = { it.receivedMessage.id }) { message ->
                                                        if (message.receivedMessage.senderId == userId) {
                                                            Column(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .then(
                                                                        if (highlightedMessageId == message.receivedMessage.id) {
                                                                            Modifier.background(
                                                                                highlightedMessageBackgroundColor
                                                                            )
                                                                        } else {
                                                                            Modifier
                                                                        }
                                                                    )
                                                                    .padding(horizontal = 16.dp)
                                                            ) {


                                                                when (message.receivedMessage.type) {
                                                                    ChatMessageType.TEXT -> {

                                                                        OverAllMeRepliedMessageItem(
                                                                            message,
                                                                            userProfileInfo,
                                                                            viewModel
                                                                        ) {
                                                                            val findMessageIndex =
                                                                                viewModel.findMessageIndex(
                                                                                    groupedMessages,
                                                                                    it.id
                                                                                )

                                                                            if (findMessageIndex != -1) {

                                                                                highlightedMessageId =
                                                                                    it.id
                                                                                coroutineScope.launch {
                                                                                    val viewportHeight =
                                                                                        lazyListState.layoutInfo.viewportSize.height
                                                                                    val offset =
                                                                                        (viewportHeight * 0.7).toInt()  // 70% of the viewport height
                                                                                    lazyListState.animateScrollToItem(
                                                                                        findMessageIndex,
                                                                                        -offset
                                                                                    )
                                                                                }
                                                                            }
                                                                        }

                                                                        ChatMeMessageItem(
                                                                            "You",
                                                                            message.receivedMessage.content,
                                                                            viewModel.formatMessageReceived(
                                                                                message.receivedMessage.timestamp
                                                                            ),
                                                                            message.receivedMessage.status
                                                                        )

                                                                    }

                                                                    ChatMessageType.IMAGE -> {

                                                                        OverAllMeRepliedMessageItem(
                                                                            message,
                                                                            userProfileInfo,
                                                                            viewModel
                                                                        ) {
                                                                            val findMessageIndex =
                                                                                viewModel.findMessageIndex(
                                                                                    groupedMessages,
                                                                                    it.id
                                                                                )

                                                                            if (findMessageIndex != -1) {

                                                                                highlightedMessageId =
                                                                                    it.id
                                                                                coroutineScope.launch {
                                                                                    val viewportHeight =
                                                                                        lazyListState.layoutInfo.viewportSize.height
                                                                                    val offset =
                                                                                        (viewportHeight * 0.7).toInt()  // 70% of the viewport height
                                                                                    lazyListState.animateScrollToItem(
                                                                                        findMessageIndex,
                                                                                        -offset
                                                                                    )
                                                                                }
                                                                            }
                                                                        }
                                                                        ChatMeImageMessageItem(
                                                                            message.receivedMessage,
                                                                            message.repliedToMessage,
                                                                            message.receivedMessageFileMeta,
                                                                            "You",
                                                                            onNavigateImageSlider,
                                                                            viewModel
                                                                        )


                                                                    }

                                                                    ChatMessageType.GIF -> {

                                                                        OverAllMeRepliedMessageItem(
                                                                            message,
                                                                            userProfileInfo,
                                                                            viewModel
                                                                        ) {
                                                                            val findMessageIndex =
                                                                                viewModel.findMessageIndex(
                                                                                    groupedMessages,
                                                                                    it.id
                                                                                )

                                                                            if (findMessageIndex != -1) {

                                                                                highlightedMessageId =
                                                                                    it.id
                                                                                coroutineScope.launch {
                                                                                    val viewportHeight =
                                                                                        lazyListState.layoutInfo.viewportSize.height
                                                                                    val offset =
                                                                                        (viewportHeight * 0.7).toInt()  // 70% of the viewport height
                                                                                    lazyListState.animateScrollToItem(
                                                                                        findMessageIndex,
                                                                                        -offset
                                                                                    )
                                                                                }
                                                                            }
                                                                        }
                                                                        ChatMeImageMessageItem(
                                                                            message.receivedMessage,
                                                                            message.repliedToMessage,
                                                                            message.receivedMessageFileMeta,
                                                                            "You",
                                                                            onNavigateImageSlider,
                                                                            viewModel
                                                                        )


                                                                    }

                                                                    ChatMessageType.VIDEO -> {

                                                                        OverAllMeRepliedMessageItem(
                                                                            message,
                                                                            userProfileInfo,
                                                                            viewModel
                                                                        ) {
                                                                            val findMessageIndex =
                                                                                viewModel.findMessageIndex(
                                                                                    groupedMessages,
                                                                                    it.id
                                                                                )


                                                                            if (findMessageIndex != -1) {

                                                                                highlightedMessageId =
                                                                                    it.id
                                                                                coroutineScope.launch {
                                                                                    val viewportHeight =
                                                                                        lazyListState.layoutInfo.viewportSize.height
                                                                                    val offset =
                                                                                        (viewportHeight * 0.7).toInt()  // 70% of the viewport height
                                                                                    lazyListState.animateScrollToItem(
                                                                                        findMessageIndex,
                                                                                        -offset
                                                                                    )
                                                                                }
                                                                            }
                                                                        }

                                                                        ChatMeVideoMessageItem(
                                                                            message.receivedMessage,
                                                                            message.repliedToMessage,
                                                                            message.receivedMessageFileMeta,
                                                                            "You",
                                                                            onNavigateVideoPlayer,
                                                                            viewModel
                                                                        )

                                                                    }

                                                                    ChatMessageType.AUDIO -> {

                                                                        OverAllMeRepliedMessageItem(
                                                                            message,
                                                                            userProfileInfo,
                                                                            viewModel
                                                                        ) {
                                                                            val findMessageIndex =
                                                                                viewModel.findMessageIndex(
                                                                                    groupedMessages,
                                                                                    it.id
                                                                                )

                                                                            if (findMessageIndex != -1) {

                                                                                highlightedMessageId =
                                                                                    it.id
                                                                                coroutineScope.launch {
                                                                                    val viewportHeight =
                                                                                        lazyListState.layoutInfo.viewportSize.height
                                                                                    val offset =
                                                                                        (viewportHeight * 0.7).toInt()  // 70% of the viewport height
                                                                                    lazyListState.animateScrollToItem(
                                                                                        findMessageIndex,
                                                                                        -offset
                                                                                    )
                                                                                }
                                                                            }
                                                                        }
                                                                        ChatMeAudioMessageItem(
                                                                            message.receivedMessage,
                                                                            message.repliedToMessage,
                                                                            message.receivedMessageFileMeta,
                                                                            "You",
                                                                            viewModel
                                                                        )
                                                                    }

                                                                    ChatMessageType.FILE -> {

                                                                        OverAllMeRepliedMessageItem(
                                                                            message,
                                                                            userProfileInfo,
                                                                            viewModel
                                                                        ) {
                                                                            val findMessageIndex =
                                                                                viewModel.findMessageIndex(
                                                                                    groupedMessages,
                                                                                    it.id
                                                                                )

                                                                            if (findMessageIndex != -1) {

                                                                                highlightedMessageId =
                                                                                    it.id
                                                                                coroutineScope.launch {
                                                                                    val viewportHeight =
                                                                                        lazyListState.layoutInfo.viewportSize.height
                                                                                    val offset =
                                                                                        (viewportHeight * 0.7).toInt()  // 70% of the viewport height
                                                                                    lazyListState.animateScrollToItem(
                                                                                        findMessageIndex,
                                                                                        -offset
                                                                                    )
                                                                                }
                                                                            }
                                                                        }
                                                                        ChatMeFileMessageItem(
                                                                            message.receivedMessage,
                                                                            message.repliedToMessage,
                                                                            message.receivedMessageFileMeta,
                                                                            "You",
                                                                            viewModel
                                                                        )
                                                                    }

                                                                }

                                                            }
                                                        } else {
                                                            Column(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .then(
                                                                        // Apply background color only if highlightedMessageId is equal to message.receivedMessage.id
                                                                        if (highlightedMessageId == message.receivedMessage.id) {
                                                                            Modifier.background(
                                                                                highlightedMessageBackgroundColor
                                                                            )  // Apply Yellow background if the condition is true
                                                                        } else {
                                                                            Modifier // No background if the condition is false
                                                                        }
                                                                    )
                                                                    .pointerInput(message) {

                                                                        detectTapGestures(
                                                                            onLongPress = {
                                                                                viewModel.setSelectedMessage(
                                                                                    message.receivedMessage
                                                                                )
                                                                                replyMessageBottomSheet =
                                                                                    true
                                                                            }
                                                                        )

                                                                    }
                                                                    .padding(horizontal = 16.dp)

                                                            ) {


                                                                when (message.receivedMessage.type) {
                                                                    ChatMessageType.TEXT -> {

                                                                        DecryptionSafeGuard(
                                                                            message,
                                                                            userProfileInfo,
                                                                            viewModel
                                                                        ) {
                                                                            OverAllOtherRepliedMessageItem(
                                                                                message,
                                                                                userProfileInfo,
                                                                                viewModel
                                                                            ) {
                                                                                val findMessageIndex =
                                                                                    viewModel.findMessageIndex(
                                                                                        groupedMessages,
                                                                                        it.id
                                                                                    )


                                                                                if (findMessageIndex != -1) {

                                                                                    highlightedMessageId =
                                                                                        it.id
                                                                                    coroutineScope.launch {
                                                                                        val viewportHeight =
                                                                                            lazyListState.layoutInfo.viewportSize.height
                                                                                        val offset =
                                                                                            (viewportHeight * 0.7).toInt()  // 70% of the viewport height
                                                                                        lazyListState.animateScrollToItem(
                                                                                            findMessageIndex,
                                                                                            -offset
                                                                                        )
                                                                                    }
                                                                                }
                                                                            }

                                                                            ChatOtherMessageItem(
                                                                                viewModel,
                                                                                "${userProfileInfo.firstName} ${userProfileInfo.lastName ?: ""}",
                                                                                userProfileInfo.profilePicUrl96By96,
                                                                                message.receivedMessage.content,
                                                                                viewModel.formatMessageReceived(
                                                                                    message.receivedMessage.timestamp
                                                                                )
                                                                            )
                                                                        }

                                                                    }

                                                                    ChatMessageType.IMAGE -> {

                                                                        DecryptionSafeGuard(
                                                                            message,
                                                                            userProfileInfo,
                                                                            viewModel
                                                                        ) {
                                                                            OverAllOtherRepliedMessageItem(
                                                                                message,
                                                                                userProfileInfo,
                                                                                viewModel
                                                                            ) {
                                                                                val findMessageIndex =
                                                                                    viewModel.findMessageIndex(
                                                                                        groupedMessages,
                                                                                        it.id
                                                                                    )


                                                                                if (findMessageIndex != -1) {

                                                                                    highlightedMessageId =
                                                                                        it.id
                                                                                    coroutineScope.launch {
                                                                                        val viewportHeight =
                                                                                            lazyListState.layoutInfo.viewportSize.height
                                                                                        val offset =
                                                                                            (viewportHeight * 0.7).toInt()  // 70% of the viewport height
                                                                                        lazyListState.animateScrollToItem(
                                                                                            findMessageIndex,
                                                                                            -offset
                                                                                        )
                                                                                    }
                                                                                }
                                                                            }

                                                                            ChatOtherImageMessageItem(
                                                                                message.receivedMessage,
                                                                                message.receivedMessageFileMeta,
                                                                                "${userProfileInfo.firstName} ${userProfileInfo.lastName ?: ""}",
                                                                                userProfileInfo.profilePicUrl96By96,
                                                                                onNavigateImageSlider,
                                                                                viewModel,
                                                                            )
                                                                        }


                                                                    }


                                                                    ChatMessageType.GIF -> {

                                                                        DecryptionSafeGuard(
                                                                            message,
                                                                            userProfileInfo,
                                                                            viewModel
                                                                        ) {
                                                                            OverAllOtherRepliedMessageItem(
                                                                                message,
                                                                                userProfileInfo,
                                                                                viewModel
                                                                            ) {
                                                                                val findMessageIndex =
                                                                                    viewModel.findMessageIndex(
                                                                                        groupedMessages,
                                                                                        it.id
                                                                                    )


                                                                                if (findMessageIndex != -1) {

                                                                                    highlightedMessageId =
                                                                                        it.id
                                                                                    coroutineScope.launch {
                                                                                        val viewportHeight =
                                                                                            lazyListState.layoutInfo.viewportSize.height
                                                                                        val offset =
                                                                                            (viewportHeight * 0.7).toInt()  // 70% of the viewport height
                                                                                        lazyListState.animateScrollToItem(
                                                                                            findMessageIndex,
                                                                                            -offset
                                                                                        )
                                                                                    }
                                                                                }
                                                                            }

                                                                            ChatOtherImageMessageItem(
                                                                                message.receivedMessage,
                                                                                message.receivedMessageFileMeta,
                                                                                "${userProfileInfo.firstName} ${userProfileInfo.lastName ?: ""}",
                                                                                userProfileInfo.profilePicUrl96By96,
                                                                                onNavigateImageSlider,
                                                                                viewModel,
                                                                            )
                                                                        }


                                                                    }


                                                                    ChatMessageType.VIDEO -> {
                                                                        DecryptionSafeGuard(
                                                                            message,
                                                                            userProfileInfo,
                                                                            viewModel
                                                                        ) {
                                                                            OverAllOtherRepliedMessageItem(
                                                                                message,
                                                                                userProfileInfo,
                                                                                viewModel
                                                                            ) {
                                                                                val findMessageIndex =
                                                                                    viewModel.findMessageIndex(
                                                                                        groupedMessages,
                                                                                        it.id
                                                                                    )

                                                                                if (findMessageIndex != -1) {

                                                                                    highlightedMessageId =
                                                                                        it.id
                                                                                    coroutineScope.launch {
                                                                                        val viewportHeight =
                                                                                            lazyListState.layoutInfo.viewportSize.height
                                                                                        val offset =
                                                                                            (viewportHeight * 0.7).toInt()  // 70% of the viewport height
                                                                                        lazyListState.animateScrollToItem(
                                                                                            findMessageIndex,
                                                                                            -offset
                                                                                        )
                                                                                    }
                                                                                }
                                                                            }

                                                                            ChatOtherVideoMessageItem(
                                                                                message.receivedMessage,
                                                                                message.receivedMessageFileMeta,
                                                                                "${userProfileInfo.firstName} ${userProfileInfo.lastName ?: ""}",
                                                                                userProfileInfo.profilePicUrl96By96,
                                                                                viewModel,
                                                                                onNavigateVideoPlayer
                                                                            )
                                                                        }

                                                                    }

                                                                    ChatMessageType.AUDIO -> {
                                                                        DecryptionSafeGuard(
                                                                            message,
                                                                            userProfileInfo,
                                                                            viewModel
                                                                        ) {
                                                                            OverAllOtherRepliedMessageItem(
                                                                                message,
                                                                                userProfileInfo,
                                                                                viewModel
                                                                            ) {
                                                                                val findMessageIndex =
                                                                                    viewModel.findMessageIndex(
                                                                                        groupedMessages,
                                                                                        it.id
                                                                                    )

                                                                                if (findMessageIndex != -1) {

                                                                                    highlightedMessageId =
                                                                                        it.id
                                                                                    coroutineScope.launch {
                                                                                        val viewportHeight =
                                                                                            lazyListState.layoutInfo.viewportSize.height
                                                                                        val offset =
                                                                                            (viewportHeight * 0.7).toInt()  // 70% of the viewport height
                                                                                        lazyListState.animateScrollToItem(
                                                                                            findMessageIndex,
                                                                                            -offset
                                                                                        )
                                                                                    }
                                                                                }
                                                                            }
                                                                            ChatOtherAudioMessageItem(
                                                                                message.receivedMessage,
                                                                                message.receivedMessageFileMeta,
                                                                                "${userProfileInfo.firstName} ${userProfileInfo.lastName ?: ""}",
                                                                                userProfileInfo.profilePicUrl96By96,
                                                                                viewModel
                                                                            )
                                                                        }
                                                                    }

                                                                    ChatMessageType.FILE -> {
                                                                        DecryptionSafeGuard(
                                                                            message,
                                                                            userProfileInfo,
                                                                            viewModel
                                                                        ) {
                                                                            OverAllOtherRepliedMessageItem(
                                                                                message,
                                                                                userProfileInfo,
                                                                                viewModel
                                                                            ) {
                                                                                val findMessageIndex =
                                                                                    viewModel.findMessageIndex(
                                                                                        groupedMessages,
                                                                                        it.id
                                                                                    )


                                                                                if (findMessageIndex != -1) {

                                                                                    highlightedMessageId =
                                                                                        it.id
                                                                                    coroutineScope.launch {
                                                                                        val viewportHeight =
                                                                                            lazyListState.layoutInfo.viewportSize.height
                                                                                        val offset =
                                                                                            (viewportHeight * 0.7).toInt()  // 70% of the viewport height
                                                                                        lazyListState.animateScrollToItem(
                                                                                            findMessageIndex,
                                                                                            -offset
                                                                                        )
                                                                                    }
                                                                                }
                                                                            }

                                                                            ChatOtherFileMessageItem(
                                                                                message.receivedMessage,
                                                                                message.receivedMessageFileMeta,
                                                                                "${userProfileInfo.firstName} ${userProfileInfo.lastName ?: ""}",
                                                                                userProfileInfo.profilePicUrl96By96,
                                                                                viewModel
                                                                            )
                                                                        }
                                                                    }
                                                                }


                                                            }
                                                        }
                                                    }

                                                    item(key = "header-${day}") {
                                                        MessageDateHeader(day)
                                                    }

                                                }

                                                item(key = "profile-header") {
                                                    ProfileHeader(
                                                        chatUsersProfileImageLoader,
                                                        userProfileInfo
                                                    )
                                                }

                                                item(key = "e2ee-message") {
                                                    E2EEEMessageHeader()
                                                }
                                            }


                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .align(Alignment.BottomCenter)
                                            ) {

                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .then(
                                                            if (isVisibleMediaLibrary) Modifier.touchConsumer(
                                                                pass = PointerEventPass.Initial,
                                                                onDown = {
                                                                    hideMediaLibrary()
                                                                }
                                                            ) else Modifier)
                                                ) {

                                                    if (isTyping) {
                                                        CustomWavyTypingIndicator()
                                                    }

                                                    if (isGoToBottom) {
                                                        Box(
                                                            modifier = Modifier
                                                                .wrapContentSize()
                                                                .align(Alignment.BottomEnd)
                                                                .padding(
                                                                    horizontal = 16.dp,
                                                                    vertical = 8.dp
                                                                )


                                                        ) {
                                                            FloatingActionButton(
                                                                onClick = {
                                                                    coroutineScope.launch {
                                                                        lazyListState.animateScrollToItem(
                                                                            0
                                                                        )
                                                                    }
                                                                },
                                                                modifier = Modifier.size(32.dp)

                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Filled.KeyboardDoubleArrowDown,
                                                                    contentDescription = "Scroll to Bottom",
                                                                    tint = Color.White
                                                                )
                                                            }

                                                        }
                                                    }

                                                }

                                                AnimatedChooseMediaLibrary(
                                                    isVisibleMediaLibrary,
                                                    onChooseCamera = {
                                                        cameraLauncher.launch(Unit)
                                                        hideMediaLibrary()
                                                    },
                                                    onChooseLibrary = {
                                                        documentTreeLauncher.launch(arrayOf("*/*"))
                                                        hideMediaLibrary()
                                                    }, onChooseGallery = {
                                                        galleryVisualLauncher.launch(Unit)
                                                        hideMediaLibrary()
                                                    })

                                            }


                                        }



                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .then(
                                                    if (isVisibleMediaLibrary) Modifier.touchConsumer(
                                                        pass = PointerEventPass.Initial,
                                                        onDown = {
                                                            hideMediaLibrary()
                                                        }
                                                    ) else Modifier),
                                        ) {
                                            selectedMessage?.let {
                                                if (showReplyContent) {

                                                    when (it.type) {
                                                        ChatMessageType.TEXT -> {
                                                            ReplyMessageContent(
                                                                it.content,
                                                                "${userProfileInfo.firstName} ${userProfileInfo.lastName ?: ""}",
                                                                viewModel.formatMessageReceived(it.timestamp)
                                                            ) {
                                                                showReplyContent = false
                                                                viewModel.setSelectedMessage(null)
                                                            }
                                                        }

                                                        ChatMessageType.IMAGE -> {

                                                            selectedMessageMessageMediaMetadata?.let { selectedMessageMessageMediaMetadata ->
                                                                ReplyMessageVisualMediaContent(
                                                                    it.content,
                                                                    selectedMessageMessageMediaMetadata.fileAbsolutePath
                                                                        ?: selectedMessageMessageMediaMetadata.fileThumbPath,
                                                                    "${userProfileInfo.firstName} ${userProfileInfo.lastName ?: ""}",
                                                                    viewModel.formatMessageReceived(
                                                                        it.timestamp
                                                                    )
                                                                ) {
                                                                    showReplyContent = false
                                                                    viewModel.setSelectedMessage(
                                                                        null
                                                                    )
                                                                }
                                                            }
                                                        }

                                                        ChatMessageType.GIF -> {

                                                            selectedMessageMessageMediaMetadata?.let { selectedMessageMessageMediaMetadata ->
                                                                ReplyMessageVisualMediaContent(
                                                                    it.content,
                                                                    selectedMessageMessageMediaMetadata.fileAbsolutePath
                                                                        ?: selectedMessageMessageMediaMetadata.fileThumbPath,
                                                                    "${userProfileInfo.firstName} ${userProfileInfo.lastName ?: ""}",
                                                                    viewModel.formatMessageReceived(
                                                                        it.timestamp
                                                                    )
                                                                ) {
                                                                    showReplyContent = false
                                                                    viewModel.setSelectedMessage(
                                                                        null
                                                                    )
                                                                }
                                                            }
                                                        }

                                                        ChatMessageType.AUDIO -> {

                                                            selectedMessageMessageMediaMetadata?.let { selectedMessageMessageMediaMetadata ->

                                                                ReplyMessageContent(
                                                                    "${it.content} ${
                                                                        formatTimeSeconds(
                                                                            selectedMessageMessageMediaMetadata.totalDuration / 1000f
                                                                        )

                                                                    }",
                                                                    "${userProfileInfo.firstName} ${userProfileInfo.lastName ?: ""}",
                                                                    viewModel.formatMessageReceived(
                                                                        it.timestamp
                                                                    )
                                                                ) {
                                                                    showReplyContent = false
                                                                    viewModel.setSelectedMessage(
                                                                        null
                                                                    )
                                                                }
                                                            }
                                                        }

                                                        ChatMessageType.VIDEO -> {

                                                            selectedMessageMessageMediaMetadata?.let { selectedMessageMessageMediaMetadata ->
                                                                ReplyMessageVideoMediaContent(
                                                                    it.content,
                                                                    getThumbnailBitmap(
                                                                        selectedMessageMessageMediaMetadata.thumbData
                                                                    ),
                                                                    selectedMessageMessageMediaMetadata.fileAbsolutePath,
                                                                    "${userProfileInfo.firstName} ${userProfileInfo.lastName ?: ""}",
                                                                    viewModel.formatMessageReceived(
                                                                        it.timestamp
                                                                    )
                                                                ) {
                                                                    showReplyContent = false
                                                                    viewModel.setSelectedMessage(
                                                                        null
                                                                    )
                                                                }
                                                            }

                                                        }

                                                        ChatMessageType.FILE -> {
                                                            ReplyMessageContent(
                                                                it.content,
                                                                "${userProfileInfo.firstName} ${userProfileInfo.lastName ?: ""}",
                                                                viewModel.formatMessageReceived(it.timestamp)
                                                            ) {
                                                                showReplyContent = false
                                                                viewModel.setSelectedMessage(null)
                                                            }
                                                        }
                                                    }

                                                }
                                            }


                                            if (isLinkPreviewAvailable) {
                                                linkPreviewData?.let {
                                                    ChatMessageLinkPreviewHeader(it)
                                                } ?: run {
                                                    ChatMessageLinkPreviewHeaderLoading()
                                                }
                                            }
                                        }


                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .wrapContentHeight()
                                                .padding(8.dp)
                                                .then(
                                                    if (isVisibleMediaLibrary) Modifier.touchConsumer(
                                                        pass = PointerEventPass.Initial,
                                                        onDown = {
                                                            hideMediaLibrary()
                                                        }
                                                    ) else Modifier),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {

                                            StringTextField(
                                                value,
                                                textFieldState,
                                                {
                                                    hideMediaLibrary()

                                                    value = it
                                                    viewModel.onUserTyping(
                                                        userId,
                                                        userProfileInfo.userId
                                                    ) // Handle typing event

                                                }, Modifier
                                                    .heightIn(min = 48.dp)
                                                    .weight(1f)
                                            ) {

                                                isVisibleMediaLibrary = !isVisibleMediaLibrary
                                            }


                                            if (value.trim().isNotEmpty()) {
                                                // Send Button
                                                IconButton(
                                                    onClick = {


                                                        if (value.trim().isEmpty()) {
                                                            return@IconButton
                                                        }

                                                        selectedMessage?.let { nonNullSelectedMessage ->
                                                            viewModel.sendMessage(
                                                                value.trim(),
                                                                nonNullSelectedMessage.senderMessageId,
                                                                nonNullSelectedMessage.id
                                                            ) {
                                                            }
                                                        } ?: run {
                                                            viewModel.sendMessage(value.trim()) {

                                                            }
                                                        }

                                                        textFieldState.clearText()
                                                        showReplyContent = false
                                                        viewModel.setSelectedMessage(null)
                                                        if (isLinkPreviewAvailable) {
                                                            isLinkPreviewAvailable = false
                                                            linkPreviewData = null
                                                        }
                                                    },

                                                    ) {
                                                    Icon(
                                                        painter = painterResource(R.drawable.ic_send), // Replace with actual drawable
                                                        contentDescription = "Send",
                                                        modifier = Modifier.size(24.dp),
                                                        tint = Color.Unspecified
                                                    )
                                                }
                                            }

                                        }

                                    }

                                    if (groupedMessages.isNotEmpty() && isGoToBottom) {
                                        Box(
                                            modifier = Modifier
                                                .wrapContentSize()
                                                .wrapContentSize()
                                                .padding(8.dp)
                                                .background(
                                                    Color(0xFFF1F3F4),
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .clip(RoundedCornerShape(8.dp))
                                                .align(Alignment.TopEnd)
                                        ) {

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(8.dp)
                                            ) {

                                                if (updatedHeader.first == "up") {
                                                    Icon(
                                                        imageVector = Icons.Filled.ArrowUpward,
                                                        contentDescription = "Direction Icon",
                                                        modifier = Modifier.size(18.dp),
                                                        tint = Color.Black
                                                    )

                                                } else {
                                                    Icon(
                                                        imageVector = Icons.Filled.ArrowDownward,
                                                        contentDescription = "Direction Icon",
                                                        modifier = Modifier.size(18.dp),
                                                        tint = Color.Black
                                                    )
                                                }
                                                Text(
                                                    text = updatedHeader.second,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = Color.Black
                                                )
                                            }
                                        }
                                    }


                                }
                            }

                            if (replyMessageBottomSheet) {
                                ModalBottomSheet(
                                    modifier = Modifier
                                        .safeDrawingPadding(),
                                    onDismissRequest = {
                                        replyMessageBottomSheet = false
                                    },
                                    shape = RectangleShape,
                                    sheetState = replyMessageBottomSheetState,
                                    dragHandle = null
                                ) {


                                    selectedMessage?.let {

                                        Column(modifier = Modifier.fillMaxWidth()) {

                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        showReplyContent = true
                                                        replyMessageBottomSheet = false
                                                    }
                                                    .padding(16.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {

                                                Icon(
                                                    Icons.AutoMirrored.Filled.Reply,
                                                    contentDescription = "Reply",
                                                    modifier = Modifier.size(24.dp),
                                                )

                                                Text(
                                                    text = "Reply",
                                                    modifier = Modifier.padding(horizontal = 4.dp),
                                                )
                                            }

                                            if (it.type == ChatMessageType.TEXT) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable {
                                                            clipBoard.nativeClipboard.setPrimaryClip(
                                                                ClipData.newPlainText(
                                                                    null,
                                                                    it.content
                                                                )
                                                            )
                                                            replyMessageBottomSheet = false
                                                        }
                                                        .padding(16.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {


                                                    Icon(
                                                        Icons.Filled.ContentCopy,
                                                        contentDescription = "Copy",
                                                        modifier = Modifier.size(24.dp),
                                                    )

                                                    Text(
                                                        text = "Copy",
                                                        modifier = Modifier.padding(horizontal = 4.dp),
                                                    )
                                                }
                                            }
                                        }

                                    }

                                }
                            }


                        }

                    } else {
                        ChatTopBarScreen(
                            userProfileInfo = userProfileInfo,
                            imageLoader = chatUsersProfileImageLoader,
                            animatedVisibilityScope = this@AnimatedContent
                        ) {
                            isExpanded = !isExpanded
                        }
                    }
                }
            }
        }
    }


}



















































