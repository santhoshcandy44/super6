package com.lts360.compose.ui.chat


import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSizeIn
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.KeyboardDoubleArrowDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusEventModifierNode
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.ObserverModifierNode
import androidx.compose.ui.node.observeReads
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkQuery
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import coil3.size.Size
import com.lts360.App
import com.lts360.R
import com.lts360.api.auth.managers.socket.SocketManager
import com.lts360.api.models.service.FeedUserProfileInfo
import com.lts360.app.database.daos.chat.ChatUserDao
import com.lts360.app.database.daos.chat.MessageDao
import com.lts360.app.database.daos.chat.MessageMediaMetaDataDao
import com.lts360.app.database.models.chat.ChatMessageStatus
import com.lts360.app.database.models.chat.ChatMessageType
import com.lts360.app.database.models.chat.Message
import com.lts360.app.database.models.chat.MessageMediaMetadata
import com.lts360.app.database.models.chat.MessageWithReply
import com.lts360.app.database.models.chat.ThumbnailLoader.getThumbnailBitmap
import com.lts360.app.workers.chat.utils.getFileExtension
import com.lts360.components.findActivity
import com.lts360.components.utils.compressImageAsByteArray
import com.lts360.components.utils.isUriExist
import com.lts360.compose.ui.auth.navhost.noTransitionComposable
import com.lts360.compose.ui.chat.repos.ChatUserRepository
import com.lts360.compose.ui.chat.viewmodels.ChatActivityViewModel
import com.lts360.compose.ui.chat.viewmodels.ChatViewModel
import com.lts360.compose.ui.chat.viewmodels.FileUploadState
import com.lts360.compose.ui.chat.viewmodels.MediaDownloadState
import com.lts360.compose.ui.chat.viewmodels.deserializeFileUploadState
import com.lts360.compose.ui.chat.viewmodels.factories.ChatActivityViewModelFactory
import com.lts360.compose.ui.common.CircularProgressIndicatorLegacy
import com.lts360.compose.ui.main.MainActivity
import com.lts360.compose.ui.main.navhosts.routes.MainRoutes
import com.lts360.compose.ui.theme.AppTheme
import com.lts360.compose.ui.theme.customColorScheme
import com.lts360.compose.ui.utils.FormatterUtils.formatTimeSeconds
import com.lts360.compose.ui.utils.FormatterUtils.humanReadableBytesSize
import com.lts360.compose.ui.utils.getThumbnail
import com.lts360.compose.ui.utils.getThumbnailFromPath
import com.lts360.compose.ui.utils.touchConsumer
import com.lts360.compose.utils.ChatMessageLinkPreviewHeader
import com.lts360.compose.utils.ChatMessageLinkPreviewHeaderLoading
import com.lts360.compose.utils.ExpandableText
import com.lts360.compose.utils.SafeDrawingBox
import com.lts360.compose.utils.ScrollBarConfig
import com.lts360.compose.utils.verticalScrollWithScrollbar
import com.lts360.libs.visualpicker.GalleryVisualPagerActivityResultContracts
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
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
                navigateToMainActivityAndFinish()
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
                                SetChatContent(
                                    MainRoutes.ChatWindow(userState.chatUser.chatId, senderId),
                                    chatActivityViewModel,
                                    userState.firstVisibleItemIndex
                                )

                            }
                        }
                    }
                }
            }

        }

    }


    private fun navigateToMainActivityAndFinish() {
        startActivity(Intent(this@ChatActivity, MainActivity::class.java))
        finishAffinity()
    }

    @Composable
    private fun SetChatContent(
        startDestination: Any,
        chatActivityViewModel: ChatActivityViewModel,
        firstUnreadIndex: Int,
    ) {

        val navHostController = rememberNavController()
        NavHost(navHostController, startDestination) {
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
                        openImageSliderActivity(this@ChatActivity, uri, imageWidth, imageHeight)
                    },
                    chatActivityViewModel,
                    firstUnreadIndex,
                    { navHostController.popBackStack() }
                )
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
        Intent(context, PlayerActivity::class.java).apply {
            setDataAndType(uri, "video/*")
            putExtra("videoWidth", videoWidth)
            putExtra("videoHeight", videoHeight)
            putExtra("totalDuration", totalDuration)
        }
    )
}

fun openImageSliderActivity(context: Context, uri: Uri, imageWidth: Int, imageHeight: Int) {
    context.startActivity(
        Intent(context, ImagesSliderActivity::class.java).apply {
            setDataAndType(uri, "image/*")
            putExtra("imageWidth", imageWidth)
            putExtra("imageHeight", imageHeight)
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
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
            // Initialize the cursor to be at the end of the field.
            initialSelection = TextRange(value.length)
        )
    }

    val clipboardManager = LocalClipboardManager.current

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

    val documentTreeLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
            if(isVisibleMediaLibrary){
                isVisibleMediaLibrary = false
            }
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
        if(isVisibleMediaLibrary){
            isVisibleMediaLibrary = false
        }
        it?.let {
            processMediaUri(it)
        }
    }


    // Fetch link preview for the first link, if available.
    LaunchedEffect(value) {

        // Extract the first URL from the text.
        val firstLink = Regex(
            "(?<!\\S)(https?|ftp)://([a-zA-Z0-9\\-]+\\.)+[a-zA-Z]{2,}(/[^\\s]*)?|(?:www\\.)[a-zA-Z0-9\\-]+\\.[a-zA-Z]{2,}(/[^\\s]*)?",
            setOf(RegexOption.IGNORE_CASE) // Enable case insensitive matching
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

                // Flatten all messages from groupedMessages
                val allMessages = groupedMessages.values.flatten()

                highlightedMessageId?.let { nonNullHighlightedMessageId ->


                    val isVisible = visibleItems
                        .mapNotNull { it.key as? Long } // Convert visible items to their IDs
                        .firstOrNull { id -> id == nonNullHighlightedMessageId } != null


                    if (isVisible) {
                        // Highlight the item if it's within the visible range
                        delay(1500) // Delay to keep the highlight
                        highlightedMessageId = null // Remove the highlight after 1 second
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



    Scaffold(
        topBar = {

            TopAppBar(
                modifier = Modifier.shadow(2.dp),
                navigationIcon = {
                    IconButton(onClick = dropUnlessResumed { onPopBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                title = {

                    val headerProfileImageRequest = ImageRequest.Builder(context)
                        .data(userProfileInfo.profilePicUrl96By96)
                        .placeholder(R.drawable.user_placeholder) // Your placeholder image
                        .error(R.drawable.user_placeholder)
                        .crossfade(true)
                        .build()

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        // Profile Image Container
                        Column(
                            modifier = Modifier
                                .wrapContentWidth()
                                .padding(end = 8.dp)
                        ) {

                            AsyncImage(
                                headerProfileImageRequest,
                                imageLoader = chatUsersProfileImageLoader,
                                contentDescription = "User Profile Image",
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }

                        // User Name and Status Container
                        Column(
                            modifier = Modifier.wrapContentWidth()
                        ) {
                            Text(
                                text = "${userProfileInfo.firstName} ${userProfileInfo.lastName ?: ""}", // Replace with actual user name
                                style = MaterialTheme.typography.bodyMedium
                            )

                            if (onlineStatus.isNotEmpty()) {
                                Text(
                                    text = onlineStatus, // Replace with actual status
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                        }
                    }
                }
            )


        }
    ) { contentPadding ->


        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)

        ) {

            if (!isMessagesLoaded) {

                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

            } else {


                Column(
                    modifier = Modifier
                        .fillMaxSize()

                ) {

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
                                .then(if (isVisibleMediaLibrary) Modifier.touchConsumer(
                                    pass = PointerEventPass.Initial,
                                    onDown = {
                                        isVisibleMediaLibrary = false
                                    }
                                ) else Modifier),
                            reverseLayout = true
                        ) {


                            groupedMessages.forEach { (day, messages) ->

                                // Add each message for the grouped day (reverse messages inside each group if needed)
                                items(messages, key = { it.receivedMessage.id }) { message ->
                                    if (message.receivedMessage.senderId == userId) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .then(

                                                    // Apply background color only if highlightedMessageId is equal to message.receivedMessage.id
                                                    if (highlightedMessageId == message.receivedMessage.id) {
                                                        Modifier.background(
                                                            highlightedMessageBackgroundColor
                                                        )
                                                    } else {
                                                        Modifier // No background if the condition is false
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

                                                            highlightedMessageId = it.id
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
                                                        viewModel.formatMessageReceived(message.receivedMessage.timestamp),
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

                                                            highlightedMessageId = it.id
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

                                                            highlightedMessageId = it.id
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

                                                            highlightedMessageId = it.id
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

                                                            highlightedMessageId = it.id
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

                                                            highlightedMessageId = it.id
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
                                                            viewModel.setSelectedMessage(message.receivedMessage)
                                                            replyMessageBottomSheet = true
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

                                                                highlightedMessageId = it.id
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

                                                                highlightedMessageId = it.id
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

                                                                highlightedMessageId = it.id
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

                                                                highlightedMessageId = it.id
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

                                                                highlightedMessageId = it.id
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

                                                                highlightedMessageId = it.id
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

                                // Add a header for the date
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
                                    .then(if (isVisibleMediaLibrary) Modifier.touchConsumer(
                                        pass = PointerEventPass.Initial,
                                        onDown = {
                                            isVisibleMediaLibrary = false
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
                                            .padding(horizontal = 16.dp, vertical = 8.dp)


                                    ) {
                                        FloatingActionButton(
                                            onClick = {
                                                coroutineScope.launch {
                                                    lazyListState.animateScrollToItem(0)
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
                                onChooseLibrary = {
                                    documentTreeLauncher.launch(arrayOf("*/*"))
                                    isVisibleMediaLibrary = false
                                }, onChooseGallery = {
                                    galleryVisualLauncher.launch(Unit)
                                    isVisibleMediaLibrary = false
                                })

                        }


                    }



                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .imePadding()
                            .then(if (isVisibleMediaLibrary) Modifier.touchConsumer(
                                pass = PointerEventPass.Initial,
                                onDown = {
                                    isVisibleMediaLibrary = false
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
                                                viewModel.formatMessageReceived(it.timestamp)
                                            ) {
                                                showReplyContent = false
                                                viewModel.setSelectedMessage(null)
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
                                                viewModel.formatMessageReceived(it.timestamp)
                                            ) {
                                                showReplyContent = false
                                                viewModel.setSelectedMessage(null)
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
                                                viewModel.formatMessageReceived(it.timestamp)
                                            ) {
                                                showReplyContent = false
                                                viewModel.setSelectedMessage(null)
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
                                                viewModel.formatMessageReceived(it.timestamp)
                                            ) {
                                                showReplyContent = false
                                                viewModel.setSelectedMessage(null)
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
                            .imePadding()
                            .then(if (isVisibleMediaLibrary) Modifier.touchConsumer(
                                pass = PointerEventPass.Initial,
                                onDown = {
                                    isVisibleMediaLibrary = false
                                }
                            ) else Modifier),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        StringTextField(
                            value,
                            textFieldState,
                            {
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
                            ) // Set background color and shape
                            .clip(RoundedCornerShape(8.dp)) // Clip the Box to have rounded corners
                            .align(Alignment.TopEnd)
                    ) {

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(8.dp),

                            ) {

                            if (updatedHeader.first == "up") {
                                Icon(
                                    imageVector = Icons.Filled.ArrowUpward, // Use a Material icon
                                    contentDescription = "Direction Icon", // Description for accessibility
                                    modifier = Modifier.size(18.dp), // Set size of the icon
                                    tint = Color.Black
                                )

                            } else {
                                Icon(
                                    imageVector = Icons.Filled.ArrowDownward, // Use a Material icon
                                    contentDescription = "Direction Icon", // Description for accessibility
                                    modifier = Modifier.size(18.dp), // Set size of the icon
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
            // Modal Bottom Sheet Layout
            ModalBottomSheet(
                modifier = Modifier
                    .safeDrawingPadding(),
                onDismissRequest = {
                    replyMessageBottomSheet = false
                },
                shape = RectangleShape, // Set shape to square (rectangle)
                sheetState = replyMessageBottomSheetState,
                dragHandle = null // Remove the drag handle
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


                            // Bookmark Icon
                            Icon(
                                Icons.AutoMirrored.Filled.Reply,
                                contentDescription = "Reply",
                                modifier = Modifier.size(24.dp),
                            )

                            // Text
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
                                        clipboardManager.setText(AnnotatedString(it.content))
                                        replyMessageBottomSheet = false
                                    }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {


                                // Bookmark Icon
                                Icon(
                                    Icons.Filled.ContentCopy,
                                    contentDescription = "Copy",
                                    modifier = Modifier.size(24.dp),
                                )

                                // Text
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


}


@Composable
fun ProfileHeader(
    imageLoader: ImageLoader,
    userProfileInfo: FeedUserProfileInfo
) {

    val context = LocalContext.current

    val imageRequest = ImageRequest.Builder(context)
        .data(userProfileInfo.profilePicUrl96By96)
        .placeholder(R.drawable.user_placeholder) // Your placeholder image
        .error(R.drawable.user_placeholder)
        .crossfade(true)
        .build()


    // Header with encryption status
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .wrapContentHeight()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            // Profile Image Container
            Column(
                modifier = Modifier
                    .wrapContentWidth()
                    .padding(end = 8.dp)
            ) {

                AsyncImage(
                    imageRequest,
                    imageLoader = imageLoader,
                    contentDescription = "User Profile Image",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

            }

            // User Name and Status Container
            Column(
                modifier = Modifier.wrapContentWidth()
            ) {
                Text(
                    text = "${userProfileInfo.firstName} ${userProfileInfo.lastName ?: ""}", // Replace with actual user name
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "Joined at ${userProfileInfo.createdAt}", // Replace with actual user name
                    style = MaterialTheme.typography.bodyMedium
                )

            }
        }
    }


}


@Composable
fun E2EEEMessageHeader() {
    // Header with encryption status
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFDEECFF))
    ) {
        // Encryption Status Row
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.padding(8.dp)
        ) {
            // Lock Icon for End-to-End Encryption
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = "Encrypted",
                tint = Color(0xFFA7FFA4),
                modifier = Modifier.size(18.dp)
            )

            // Encryption status text
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                color = Color.Black,
                text = "This chat is protected by end-to-end encryption. All your messages are fully secure encrypted.",
            )
        }
    }
}


@Composable
fun RepliedMessageContent(
    selectedMessage: String,
    to: String,
    formattedTimeStamp: String,
    onRepliedMessageClicked: () -> Unit,
) {


    Row(
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .wrapContentHeight()
            .clickable {
                onRepliedMessageClicked()
            }
            .background(MaterialTheme.customColorScheme.searchBarColor)
            .padding(vertical = 8.dp, horizontal = 4.dp), // Padding inside the border
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Icon for replying
        IconButton(
            onClick = { /* Handle reply action here */ },
            modifier = Modifier
                .padding(end = 8.dp)
                .then(Modifier.minimumInteractiveComponentSize())
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Reply,
                contentDescription = "Reply",
            )
        }


        // Reply Message content, like showing the selected message
        Column {

            // Reply header with the name and timestamp
            Row(verticalAlignment = Alignment.CenterVertically) {


                Text(
                    modifier = Modifier.weight(1f),
                    text = buildAnnotatedString {
                        // Apply bold style to "Replying to"

                        append("Reply to ")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(to)
                        }
                        // Append the rest normally
                    },
                    maxLines = 1,
                    style = TextStyle(
                        platformStyle = PlatformTextStyle(
                            includeFontPadding = false,
                        ),
                    ),
                )
                Spacer(modifier = Modifier.width(4.dp)) // Small space between text and timestamp
                Text(
                    text = formattedTimeStamp,
                    style = TextStyle(
                        platformStyle = PlatformTextStyle(
                            includeFontPadding = false,
                        ),
                    )

                )

            }


            Text(
                text = selectedMessage,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    platformStyle = PlatformTextStyle(
                        includeFontPadding = false,
                    ),
                ),
            )

        }


    }

}


@Composable
fun RepliedMessageVisualMediaContent(
    selectedMessage: String,
    thumbnailPath: String?,
    to: String,
    formattedTimeStamp: String,
    onRepliedMessageClicked: () -> Unit,
) {


    Row(
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .wrapContentHeight()
            .clickable {
                onRepliedMessageClicked()
            }
            .background(MaterialTheme.customColorScheme.searchBarColor)
            .padding(vertical = 8.dp, horizontal = 4.dp), // Padding inside the border
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Icon for replying
        IconButton(
            onClick = { /* Handle reply action here */ },
            modifier = Modifier
                .padding(end = 8.dp)
                .then(Modifier.minimumInteractiveComponentSize())
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Reply,
                contentDescription = "Reply",
            )
        }


        // Reply Message content, like showing the selected message
        Column {

            // Reply header with the name and timestamp
            Row(verticalAlignment = Alignment.CenterVertically) {


                Text(
                    modifier = Modifier.weight(1f),
                    text = buildAnnotatedString {
                        // Apply bold style to "Replying to"

                        append("Reply to ")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(to)
                        }
                        // Append the rest normally
                    },
                    maxLines = 1,
                    style = TextStyle(
                        platformStyle = PlatformTextStyle(
                            includeFontPadding = false,
                        ),
                    ),
                )
                Spacer(modifier = Modifier.width(4.dp)) // Small space between text and timestamp
                Text(
                    text = formattedTimeStamp,
                    style = TextStyle(
                        platformStyle = PlatformTextStyle(
                            includeFontPadding = false,
                        ),
                    )

                )

            }





            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween // This ensures the image is placed at the end

            ) {

                Text(
                    text = selectedMessage,
                    color = Color.Gray,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(
                        platformStyle = PlatformTextStyle(
                            includeFontPadding = false,
                        ),
                    ),
                    modifier = Modifier.weight(1f)
                )

                Image(
                    rememberAsyncImagePainter(thumbnailPath),
                    contentScale = ContentScale.Crop,
                    contentDescription = "Reply media",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            }


        }


    }

}


@Composable
fun RepliedMessageVideoMediaContent(
    selectedMessage: String,
    thumbnailBitmap: Bitmap?,
    filepath: String?,
    to: String,
    formattedTimeStamp: String,
    onRepliedMessageClicked: () -> Unit,
) {


    var thumbnail by remember { mutableStateOf(thumbnailBitmap) }
    val context = LocalContext.current

    LaunchedEffect(filepath) {

        filepath?.let {
            // Run the heavy work in background thread
            if (isUriExist(context, Uri.parse(it))) {
                // Using a coroutine to load the thumbnail in the background
                thumbnail = withContext(Dispatchers.IO) {

                    // Create the video thumbnail in background thread
                    MediaMetadataRetriever().getThumbnailFromPath(it)
                }
            }
        }
    }


    Row(
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .wrapContentHeight()
            .clickable {
                onRepliedMessageClicked()
            }
            .background(MaterialTheme.customColorScheme.searchBarColor)
            .padding(vertical = 8.dp, horizontal = 4.dp), // Padding inside the border
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Icon for replying
        IconButton(
            onClick = { /* Handle reply action here */ },
            modifier = Modifier
                .padding(end = 8.dp)
                .then(Modifier.minimumInteractiveComponentSize())
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Reply,
                contentDescription = "Reply",
            )
        }


        // Reply Message content, like showing the selected message
        Column {

            // Reply header with the name and timestamp
            Row(verticalAlignment = Alignment.CenterVertically) {


                Text(
                    modifier = Modifier.weight(1f),
                    text = buildAnnotatedString {
                        // Apply bold style to "Replying to"

                        append("Reply to ")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(to)
                        }
                        // Append the rest normally
                    },
                    maxLines = 1,
                    style = TextStyle(
                        platformStyle = PlatformTextStyle(
                            includeFontPadding = false,
                        ),
                    ),
                )
                Spacer(modifier = Modifier.width(4.dp)) // Small space between text and timestamp
                Text(
                    text = formattedTimeStamp,
                    style = TextStyle(
                        platformStyle = PlatformTextStyle(
                            includeFontPadding = false,
                        ),
                    )

                )

            }





            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween // This ensures the image is placed at the end

            ) {

                Text(
                    text = selectedMessage,
                    color = Color.Gray,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(
                        platformStyle = PlatformTextStyle(
                            includeFontPadding = false,
                        ),
                    ),
                    modifier = Modifier.weight(1f)
                )

                thumbnail?.let {
                    Image(
                        it.asImageBitmap(),
                        contentScale = ContentScale.Crop,
                        contentDescription = "Reply media",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(8.dp)) // Rounded corners with 8dp radius

                    )
                }

            }


        }


    }

}


@Composable
fun ReplyMessageContent(
    selectedMessage: String,
    senderName: String,
    formattedTimeStamp: String,
    onCloseClicked: () -> Unit,
) {


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(MaterialTheme.customColorScheme.searchBarColor)
            .padding(vertical = 8.dp, horizontal = 4.dp), // Padding inside the border

        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon for replying
        IconButton(
            onClick = { /* Handle reply action here */ },
            modifier = Modifier
                .padding(end = 8.dp)
                .then(Modifier.minimumInteractiveComponentSize())
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Reply,
                contentDescription = "Reply",
            )
        }


        // Reply Message content, like showing the selected message
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {

            // Reply header with the name and timestamp
            Row(verticalAlignment = Alignment.CenterVertically) {

                // Text content taking up space
                Row(
                    modifier = Modifier.weight(1f),

                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.weight(1f),

                        text = buildAnnotatedString {
                            // Apply bold style to "Replying to"

                            append("Reply to ")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(senderName)
                            }
                            // Append the rest normally
                        },
                        style = TextStyle(
                            platformStyle = PlatformTextStyle(
                                includeFontPadding = false,
                            ),
                        ),
                        maxLines = 1,
                    )
                    Spacer(modifier = Modifier.width(4.dp)) // Small space between text and timestamp
                    Text(
                        text = formattedTimeStamp,
                        style = TextStyle(
                            platformStyle = PlatformTextStyle(
                                includeFontPadding = false,
                            ),
                        ),
                    )
                }


                Spacer(modifier = Modifier.width(4.dp)) // Small space between text and timestamp

                IconButton(
                    {
                        onCloseClicked()
                    },

                    modifier = Modifier
                        .then(Modifier.minimumInteractiveComponentSize())
                ) {

                    Icon(
                        imageVector = Icons.Default.Close, contentDescription = "Close"
                    )

                }
            }


            Text(
                text = selectedMessage,
                color = Color.Gray,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    platformStyle = PlatformTextStyle(
                        includeFontPadding = false,
                    ),
                ),
            )

        }


    }

}


@Composable
fun ReplyMessageVisualMediaContent(
    selectedMessage: String,
    thumbnailPath: String?,
    senderName: String,
    formattedTimeStamp: String,
    onCloseClicked: () -> Unit,
) {


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(MaterialTheme.customColorScheme.searchBarColor)
            .padding(vertical = 8.dp, horizontal = 4.dp), // Padding inside the border

        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon for replying
        IconButton(
            onClick = { /* Handle reply action here */ },
            modifier = Modifier
                .padding(end = 8.dp)
                .then(Modifier.minimumInteractiveComponentSize())
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Reply,
                contentDescription = "Reply",
            )
        }


        // Reply Message content, like showing the selected message
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {

            // Reply header with the name and timestamp
            Row(verticalAlignment = Alignment.CenterVertically) {

                // Text content taking up space
                Row(
                    modifier = Modifier.weight(1f),

                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.weight(1f),

                        text = buildAnnotatedString {
                            // Apply bold style to "Replying to"

                            append("Reply to ")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(senderName)
                            }
                            // Append the rest normally
                        },
                        style = TextStyle(
                            platformStyle = PlatformTextStyle(
                                includeFontPadding = false,
                            ),
                        ),
                        maxLines = 1,
                    )
                    Spacer(modifier = Modifier.width(4.dp)) // Small space between text and timestamp
                    Text(
                        text = formattedTimeStamp,
                        style = TextStyle(
                            platformStyle = PlatformTextStyle(
                                includeFontPadding = false,
                            ),
                        ),
                    )
                }


                Spacer(modifier = Modifier.width(4.dp)) // Small space between text and timestamp

                IconButton(
                    {
                        onCloseClicked()
                    },

                    modifier = Modifier
                        .then(Modifier.minimumInteractiveComponentSize())
                ) {

                    Icon(
                        imageVector = Icons.Default.Close, contentDescription = "Close"
                    )

                }
            }



            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween // This ensures the image is placed at the end

            ) {

                Text(
                    text = selectedMessage,
                    color = Color.Gray,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(
                        platformStyle = PlatformTextStyle(
                            includeFontPadding = false,
                        ),
                    ),
                    modifier = Modifier.weight(1f)
                )

                Image(
                    rememberAsyncImagePainter(thumbnailPath),
                    contentScale = ContentScale.Crop,
                    contentDescription = "Reply media",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp)) // Rounded corners with 8dp radius

                )
            }


        }


    }

}


@Composable
fun ReplyMessageVideoMediaContent(
    selectedMessage: String,
    thumbnailBitmap: Bitmap?,
    filepath: String?,
    senderName: String,
    formattedTimeStamp: String,
    onCloseClicked: () -> Unit,
) {


    var thumbnail by remember { mutableStateOf(thumbnailBitmap) }

    val context = LocalContext.current


    LaunchedEffect(filepath) {
        filepath?.let {
            // Run the heavy work in background thread
            if (isUriExist(context, Uri.parse(it))) {
                // Using a coroutine to load the thumbnail in the background
                thumbnail = withContext(Dispatchers.IO) {
                    // Create the video thumbnail in background thread
                    MediaMetadataRetriever().getThumbnail(context, Uri.parse(it))
                }
            }
        }
    }



    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(MaterialTheme.customColorScheme.searchBarColor)
            .padding(vertical = 8.dp, horizontal = 4.dp), // Padding inside the border

        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon for replying
        IconButton(
            onClick = { /* Handle reply action here */ },
            modifier = Modifier
                .padding(end = 8.dp)
                .then(Modifier.minimumInteractiveComponentSize())
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Reply,
                contentDescription = "Reply",
            )
        }


        // Reply Message content, like showing the selected message
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {

            // Reply header with the name and timestamp
            Row(verticalAlignment = Alignment.CenterVertically) {

                // Text content taking up space
                Row(
                    modifier = Modifier.weight(1f),

                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.weight(1f),

                        text = buildAnnotatedString {
                            // Apply bold style to "Replying to"

                            append("Reply to ")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(senderName)
                            }
                            // Append the rest normally
                        },
                        style = TextStyle(
                            platformStyle = PlatformTextStyle(
                                includeFontPadding = false,
                            ),
                        ),
                        maxLines = 1,
                    )
                    Spacer(modifier = Modifier.width(4.dp)) // Small space between text and timestamp
                    Text(
                        text = formattedTimeStamp,
                        style = TextStyle(
                            platformStyle = PlatformTextStyle(
                                includeFontPadding = false,
                            ),
                        ),
                    )
                }


                Spacer(modifier = Modifier.width(4.dp)) // Small space between text and timestamp

                IconButton(
                    {
                        onCloseClicked()
                    },

                    modifier = Modifier
                        .then(Modifier.minimumInteractiveComponentSize())
                ) {

                    Icon(
                        imageVector = Icons.Default.Close, contentDescription = "Close"
                    )

                }
            }



            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween // This ensures the image is placed at the end

            ) {

                Text(
                    text = selectedMessage,
                    color = Color.Gray,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(
                        platformStyle = PlatformTextStyle(
                            includeFontPadding = false,
                        ),
                    ),
                    modifier = Modifier.weight(1f)
                )
                thumbnail?.let {
                    Image(
                        it.asImageBitmap(),
                        contentScale = ContentScale.Crop,
                        contentDescription = "Thumbnail media",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(8.dp)) // Rounded corners with 8dp radius

                    )
                }


            }


        }


    }

}


@Composable
fun MessageDateHeader(date: String) {
    // This composable can style the date header as needed

    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        OutlinedCard(
            modifier = Modifier.wrapContentSize()
        ) {
            Text(
                text = date,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(8.dp)
            )
        }

    }

}


@Composable
fun ChatMeMessageItem(
    userName: String,
    message: String,
    timestamp: String,
    status: ChatMessageStatus,
) {


    Column(
        modifier = Modifier
            .fillMaxWidth(0.8f)
    ) {

        // User Name
        Text(
            text = userName,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 4.dp),
            maxLines = 1
        )


        Row(verticalAlignment = Alignment.Bottom) {


            // Message Card
            ExpandableText(
                message,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.Black
                ),
                showMoreStyle = SpanStyle(color = Color(0xFF4399FF)),
                showLessStyle = SpanStyle(color = Color(0xFF4399FF)),
                textModifier = Modifier.wrapContentSize(),
                modifier = Modifier
                    .background(
                        Color(0xFFECECEA),
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomEnd = 16.dp,
                            bottomStart = 0.dp
                        )
                    )
                    .padding(vertical = 4.dp, horizontal = 16.dp)
                    .weight(1f, false)

            )

        }



        Spacer(modifier = Modifier.height(4.dp))

        // Show timer icon if sending
        when (status) {
            ChatMessageStatus.SENDING, ChatMessageStatus.QUEUED -> {
                Icon(
                    painter = painterResource(R.drawable.ic_message_pending),
                    contentDescription = "Sending",
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.Start) // Align icon to the end of the message
                )
            }

            ChatMessageStatus.SENT -> {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Message Sent",
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.Start) // Align icon to the start of the message
                )
            }

            ChatMessageStatus.DELIVERED -> {
                Box(
                    modifier = Modifier
                        .align(Alignment.Start)

                ) {
                    Icon(
                        imageVector = Icons.Filled.Check, // First check icon
                        contentDescription = "Message Delivered",
                        modifier = Modifier.size(16.dp)
                    )
                    Icon(
                        imageVector = Icons.Filled.Check, // Second check icon for double check
                        contentDescription = "Message Delivered",
                        modifier = Modifier
                            .size(16.dp)
                            .offset(x = 8.dp)
                    )
                }


            }

            ChatMessageStatus.READ -> {}
            ChatMessageStatus.FAILED -> {
                Icon(
                    imageVector = Icons.Filled.Error, // First check icon
                    contentDescription = "Message Delivered",
                    modifier = Modifier.size(16.dp),
                    tint = Color.Red
                )
            }

            ChatMessageStatus.FAILED_TO_DISPLAY_REASON_DECRYPTION_FAILED -> {}
            ChatMessageStatus.FAILED_TO_DISPLAY_REASON_UNKNOWN -> {}
            else -> {}

        }



        Spacer(modifier = Modifier.height(8.dp))

        // Timestamp
        Text(
            text = timestamp,
            color = Color(0xFFC0C0C0),
            fontSize = 10.sp,
            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),

            )


    }
}


@Composable
fun ChatMeImageMessageItem(
    message: Message,
    repliedMessage: Message?,
    mediaMetadata: MessageMediaMetadata?,
    userName: String,
    onNavigateImageSlider: (Uri, Int, Int) -> Unit,
    viewModel: ChatViewModel
) {
    val context = LocalContext.current


    val width = mediaMetadata?.width ?: 0
    val height = mediaMetadata?.height ?: 0

    val fileAbsPath = mediaMetadata?.fileAbsolutePath

    val blurredThumbnailBitmap by remember(mediaMetadata) {
        mutableStateOf(getThumbnailBitmap(mediaMetadata?.thumbData))
    }

    val timestamp = viewModel.formatMessageReceived(message.timestamp)


    val uploadState by viewModel.uploadStates.collectAsState()

    val fileUploadState = uploadState[message.id] ?: when (message.status) {
        ChatMessageStatus.QUEUED_MEDIA -> FileUploadState.Started
        ChatMessageStatus.QUEUED_MEDIA_RETRY -> {
            FileUploadState.Retry("Retry")
        }

        else -> FileUploadState.None
    }


    val workInfoList by WorkManager.getInstance(context)
        .getWorkInfosFlow(WorkQuery.fromUniqueWorkNames("visual_media_upload_${viewModel.chatId}_${message.id}"))
        .collectAsState(null)

    LaunchedEffect(workInfoList) {

        // Find the relevant work and retrieve the messageId and state
        workInfoList?.firstOrNull()?.let { workInfo ->

            val workerState = workInfo.state // WorkInfo state is important here


            if (workerState == WorkInfo.State.CANCELLED) {
                viewModel.repository.updateMessage(
                    message.id,
                    ChatMessageStatus.QUEUED_MEDIA_RETRY
                )
            } else {
                val progressMessageId = workInfo.progress.getLong("messageId", -1)
                val state = workInfo.progress.getString("state")

                if (progressMessageId != -1L && state != null) {
                    viewModel.updateUploadState(
                        progressMessageId,
                        deserializeFileUploadState(state)
                    )
                }
            }

        }

    }


    Column(
        modifier = Modifier
            .fillMaxWidth(0.8f)
    ) {

        // User Name
        Text(
            text = userName,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 4.dp),
            maxLines = 1
        )

        Box(
            modifier = Modifier
                .background(
                    Color(0xFFECECEA),
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomEnd = 0.dp,
                        bottomStart = 16.dp
                    )
                )
                .padding(4.dp)
                .clickable {
                    fileAbsPath?.let {
                        if (isUriExist(context, Uri.parse(fileAbsPath))) {
                            onNavigateImageSlider(Uri.parse(fileAbsPath), width, height)
                        } else {
                            Toast
                                .makeText(context, "Media is not exist", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
        ) {

            SubcomposeAsyncImage(
                fileAbsPath,
                contentDescription = "Loaded Image",
                error = {
                    blurredThumbnailBitmap?.let {
                        Image(
                            it.asImageBitmap(),
                            contentDescription = "Loaded Image",
                            modifier = Modifier
                                .clip(shape = RoundedCornerShape(16.dp))
                                .then(
                                    if (width > 0 && height > 0) {
                                        Modifier.aspectRatio(width.toFloat() / height.toFloat()) // Use the original size of the image
                                    } else {
                                        Modifier
                                    }
                                )
                        )
                    }
                },
                loading = {

                    blurredThumbnailBitmap?.let {
                        Image(
                            it.asImageBitmap(),
                            contentDescription = "Loaded Image",
                            modifier = Modifier
                                .clip(shape = RoundedCornerShape(16.dp))
                                .then(
                                    if (width > 0 && height > 0) {
                                        Modifier.aspectRatio(width.toFloat() / height.toFloat()) // Use the original size of the image
                                    } else {
                                        Modifier
                                    }
                                )
                        )
                    }
                },
                modifier = Modifier
                    .clip(shape = RoundedCornerShape(16.dp))
                    .then(
                        if (width > 0 && height > 0) {
                            Modifier.aspectRatio(width.toFloat() / height.toFloat()) // Use the original size of the image
                        } else {
                            Modifier
                        }
                    )
            )


            when (fileUploadState) {

                is FileUploadState.Retry -> {

                    fileAbsPath?.let { nonNullFileAbsPath ->


                        RetryMediaButton(
                            Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                        ) {
                            viewModel.onRetrySendVisualMedia(
                                context,
                                message.id,
                                viewModel.userId,
                                viewModel.recipientId,
                                message.content,
                                nonNullFileAbsPath,
                                mediaMetadata,
                                repliedMessage?.senderMessageId ?: -1L,

                                {
                                    viewModel.updateUploadState(
                                        message.id,
                                        FileUploadState.Started
                                    )
                                }
                            ) {
                                viewModel.updateUploadState(
                                    message.id,
                                    FileUploadState.Retry("Error: Sending visual media")
                                )
                            }
                        }


                    }

                }

                is FileUploadState.Started -> {
                    PreLoadingMediaButton {
                        viewModel.cancelVisualMediaUpload(viewModel.chatId, message.id)
                        viewModel.updateUploadState(
                            message.id,
                            FileUploadState.Retry("Cancelled by user")
                        )
                    }

                }

                is FileUploadState.InProgress -> {

                    UploadingMediaButton(
                        fileUploadState.progress
                    ) {
                        viewModel.cancelVisualMediaUpload(viewModel.chatId, message.id)
                        viewModel.updateUploadState(
                            message.id,
                            FileUploadState.Retry("Cancelled uploading by user")
                        )
                    }
                }


                else -> {}

            }
        }


        Spacer(modifier = Modifier.height(4.dp))

        // Show timer icon if sending
        when (message.status) {
            ChatMessageStatus.SENDING,
            ChatMessageStatus.QUEUED,
            ChatMessageStatus.QUEUED_MEDIA,
            ChatMessageStatus.QUEUED_MEDIA_RETRY,
                -> {
                Icon(
                    painter = painterResource(R.drawable.ic_message_pending), // Using Material Icons
                    contentDescription = "Sending",
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.Start) // Align icon to the end of the message
                )
            }

            ChatMessageStatus.SENT -> {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Message Sent",
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.Start) // Align icon to the start of the message
                )
            }

            ChatMessageStatus.DELIVERED -> {
                Box(
                    modifier = Modifier
                        .align(Alignment.Start)

                ) {
                    Icon(
                        imageVector = Icons.Filled.Check, // First check icon
                        contentDescription = "Message Delivered",
                        modifier = Modifier.size(16.dp)
                    )
                    Icon(
                        imageVector = Icons.Filled.Check, // Second check icon for double check
                        contentDescription = "Message Delivered",
                        modifier = Modifier
                            .size(16.dp)
                            .offset(x = 8.dp)
                    )
                }


            }

            ChatMessageStatus.READ -> {}
            ChatMessageStatus.FAILED -> {
                Icon(
                    imageVector = Icons.Filled.Error, // First check icon
                    contentDescription = "Message not delivered",
                    modifier = Modifier.size(16.dp),
                    tint = Color.Red
                )
            }

            ChatMessageStatus.FAILED_TO_DISPLAY_REASON_DECRYPTION_FAILED -> {}
            ChatMessageStatus.FAILED_TO_DISPLAY_REASON_UNKNOWN -> {}
        }

        // Timestamp
        Text(
            text = timestamp,
            color = Color(0xFFC0C0C0),
            fontSize = 10.sp,
            modifier = Modifier
                .padding(top = 4.dp)
        )
    }
}


@Composable
fun ChatMeVideoMessageItem(
    message: Message,
    repliedMessage: Message?,
    mediaMetadata: MessageMediaMetadata?,
    userName: String,
    onNavigateVideoPlayer: (Uri, Int, Int, Long) -> Unit,
    viewModel: ChatViewModel
) {


    val context = LocalContext.current

    var thumbnail by remember { mutableStateOf<Bitmap?>(null) }


    val width = mediaMetadata?.width ?: 0
    val height = mediaMetadata?.height ?: 0

    val fileAbsPath = mediaMetadata?.fileAbsolutePath

    val blurredThumbnailBitmap by remember(mediaMetadata) {
        mutableStateOf(getThumbnailBitmap(mediaMetadata?.thumbData))
    }

    val timestamp = viewModel.formatMessageReceived(message.timestamp)


    // Collect only the upload state for the specific messageId
    val uploadState by viewModel.uploadStates.collectAsState()

    // Get the upload state for the specific messageId

    val fileUploadState = uploadState[message.id] ?: when (message.status) {
        ChatMessageStatus.QUEUED_MEDIA -> FileUploadState.Started
        ChatMessageStatus.QUEUED_MEDIA_RETRY -> FileUploadState.Retry("Retry")
        else -> FileUploadState.None
    }


    val workInfoList by WorkManager.getInstance(context)
        .getWorkInfosFlow(WorkQuery.fromUniqueWorkNames("visual_media_upload_${viewModel.chatId}_${message.id}"))
        .collectAsState(null)

    LaunchedEffect(workInfoList) {
        workInfoList?.firstOrNull()?.let { workInfo ->

            val workerState = workInfo.state // WorkInfo state is important here

            if (workerState == WorkInfo.State.CANCELLED || workerState == WorkInfo.State.FAILED) {
                viewModel.updateUploadState(
                    message.id,
                    FileUploadState.Retry("Upload worker failed or cancelled")
                )
                viewModel.repository.updateMessage(
                    message.id,
                    ChatMessageStatus.QUEUED_MEDIA_RETRY
                )
            } else {


                val progressMessageId = workInfo.progress.getLong("messageId", -1)
                val state = workInfo.progress.getString("state")


                if (progressMessageId != -1L && state != null) {
                    viewModel.updateUploadState(
                        progressMessageId,
                        deserializeFileUploadState(state)
                    )
                }
            }

        }
    }

    LaunchedEffect(mediaMetadata?.fileAbsolutePath) {
        mediaMetadata?.fileAbsolutePath?.let {
            if (isUriExist(context, Uri.parse(it))) {
                // Using a coroutine to load the thumbnail in the background
                thumbnail = withContext(Dispatchers.IO) {
                    // Create the video thumbnail in background thread
                    MediaMetadataRetriever().getThumbnail(context, Uri.parse(it))
                }
            }
        }
    }


    Column(
        modifier = Modifier
            .fillMaxWidth(0.8f)
    ) {

        // User Name
        Text(
            text = userName,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 4.dp),
            maxLines = 1
        )

        Box(
            modifier = Modifier
                .background(
                    Color(0xFFECECEA),
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomEnd = 0.dp,
                        bottomStart = 16.dp
                    )
                )
                .padding(4.dp)
                .clickable {
                    fileAbsPath?.let { nonNullFileAbsPath ->

                        if (isUriExist(context, Uri.parse(nonNullFileAbsPath))) {

                            mediaMetadata.let {
                                onNavigateVideoPlayer(
                                    Uri.parse(nonNullFileAbsPath),
                                    it.width,
                                    it.height,
                                    it.totalDuration
                                )
                            }

                        } else {
                            Toast
                                .makeText(context, "Media is not exist", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }

                }
        ) {


            thumbnail?.let {
                Image(
                    it.asImageBitmap(),
                    contentDescription = "Loaded Image",
                    modifier = Modifier
                        .clip(shape = RoundedCornerShape(16.dp))
                        .then(
                            if (width > 0 && height > 0) {
                                Modifier.aspectRatio(width.toFloat() / height.toFloat()) // Use the original size of the image
                            } else {
                                Modifier
                            }
                        )
                )
            } ?: run {

                blurredThumbnailBitmap?.let {
                    Image(
                        it.asImageBitmap(),
                        contentDescription = "Loaded Image",
                        modifier = Modifier
                            .clip(shape = RoundedCornerShape(16.dp))
                            .then(
                                if (width > 0 && height > 0) {
                                    Modifier.aspectRatio(width.toFloat() / height.toFloat()) // Use the original size of the image
                                } else {
                                    Modifier
                                }
                            )
                    )
                }

            }


            Image(
                painterResource(android.R.drawable.ic_media_play),
                contentDescription = "Play video",
                Modifier
                    .size(40.dp, 40.dp)
                    .align(Alignment.Center)
            )


            when (fileUploadState) {

                is FileUploadState.Retry -> {

                    fileAbsPath?.let { nonNullFileAbsPath ->
                        RetryMediaButton(
                            Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                        ) {

                            viewModel.onRetrySendVisualMedia(
                                context,
                                message.id,
                                viewModel.userId,
                                viewModel.recipientId,
                                message.content,
                                nonNullFileAbsPath,
                                mediaMetadata,
                                repliedMessage?.senderMessageId ?: -1,
                                {
                                    viewModel.updateUploadState(
                                        message.id,
                                        FileUploadState.Started
                                    )
                                }
                            ) {
                                viewModel.updateUploadState(
                                    message.id,
                                    FileUploadState.Retry("Error: Sending video")
                                )
                            }
                        }
                    }

                }

                is FileUploadState.Started -> {
                    PreLoadingMediaButton {
                        viewModel.cancelVisualMediaUpload(viewModel.chatId, message.id)
                    }

                }

                is FileUploadState.InProgress -> {

                    UploadingMediaButton(
                        fileUploadState.progress
                    ) {
                        viewModel.cancelVisualMediaUpload(viewModel.chatId, message.id)
                    }

                }

                else -> {}

            }
        }


        Spacer(modifier = Modifier.height(4.dp))

        // Show timer icon if sending
        when (message.status) {
            ChatMessageStatus.SENDING,
            ChatMessageStatus.QUEUED,
            ChatMessageStatus.QUEUED_MEDIA,
            ChatMessageStatus.QUEUED_MEDIA_RETRY,
                -> {
                Icon(
                    painter = painterResource(R.drawable.ic_message_pending), // Using Material Icons
                    contentDescription = "Sending",
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.Start) // Align icon to the end of the message
                )
            }

            ChatMessageStatus.SENT -> {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Message Sent",
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.Start) // Align icon to the start of the message
                )
            }

            ChatMessageStatus.DELIVERED -> {
                Box(
                    modifier = Modifier
                        .align(Alignment.Start)

                ) {
                    Icon(
                        imageVector = Icons.Filled.Check, // First check icon
                        contentDescription = "Message Delivered",
                        modifier = Modifier.size(16.dp)
                    )
                    Icon(
                        imageVector = Icons.Filled.Check, // Second check icon for double check
                        contentDescription = "Message Delivered",
                        modifier = Modifier
                            .size(16.dp)
                            .offset(x = 8.dp)
                    )
                }


            }

            ChatMessageStatus.READ -> {}
            ChatMessageStatus.FAILED -> {
                Icon(
                    imageVector = Icons.Filled.Error, // First check icon
                    contentDescription = "Message not delivered",
                    modifier = Modifier.size(16.dp),
                    tint = Color.Red
                )
            }

            ChatMessageStatus.FAILED_TO_DISPLAY_REASON_DECRYPTION_FAILED -> {}
            ChatMessageStatus.FAILED_TO_DISPLAY_REASON_UNKNOWN -> {}
        }

        // Timestamp
        Text(
            text = timestamp,
            color = Color(0xFFC0C0C0),
            fontSize = 10.sp,
            modifier = Modifier
                .padding(top = 4.dp)
        )
    }


}

@Composable
fun ChatMeAudioMessageItem(
    message: Message,
    repliedMessage: Message?,
    mediaMetadata: MessageMediaMetadata?,
    userName: String,
    viewModel: ChatViewModel
) {


    val context = LocalContext.current


    val fileColor = when (mediaMetadata?.fileMimeType) {
        // Specific colors for different audio file types
        "audio/mpeg" -> Color(0xFF4CAF50)  // Green for MP3
        "audio/wav" -> Color(0xFF2196F3)  // Blue for WAV
        "audio/ogg" -> Color(0xFFFF5722)  // Orange for OGG
        "audio/flac" -> Color(0xFF9C27B0)  // Purple for FLAC
        "audio/aac" -> Color(0xFFFF9800)  // Amber for AAC

        // Default color for other file types (non-audio)
        else -> Color.LightGray  // Blue for non-audio files
    }


    val timestamp by rememberSaveable { mutableStateOf(viewModel.formatMessageReceived(message.timestamp)) }


    // Collect only the upload state for the specific messageId
    val uploadState by viewModel.uploadStates.collectAsState()

    // Get the upload state for the specific messageId

    val fileUploadState = uploadState[message.id] ?: when (message.status) {
        ChatMessageStatus.QUEUED_MEDIA -> FileUploadState.Started
        ChatMessageStatus.QUEUED_MEDIA_RETRY -> FileUploadState.Retry("Retry")
        else -> FileUploadState.None
    }
    val workInfoList by WorkManager.getInstance(context)
        .getWorkInfosFlow(
            WorkQuery.fromUniqueWorkNames("media_upload_${viewModel.chatId}_${message.id}")
        ).collectAsState(null)


    LaunchedEffect(workInfoList) {

        workInfoList?.firstOrNull()?.let { workInfo ->

            val workerState = workInfo.state // WorkInfo state is important here

            if (workerState == WorkInfo.State.CANCELLED) {
                viewModel.repository.updateMessage(
                    message.id,
                    ChatMessageStatus.QUEUED_MEDIA_RETRY
                )
            } else {
                val progressMessageId = workInfo.progress.getLong("messageId", -1)
                val state = workInfo.progress.getString("state")

                if (progressMessageId != -1L && state != null) {
                    viewModel.updateUploadState(
                        progressMessageId,
                        deserializeFileUploadState(state)
                    )
                }
            }

        }

    }


    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .fillMaxHeight()
        ) {


            // User Name
            Text(
                text = userName,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .padding(start = 8.dp),
                maxLines = 1
            )


            Box(
                modifier = Modifier
                    .background(
                        Color(0xFFECECEA),
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomEnd = 0.dp,
                            bottomStart = 16.dp
                        )
                    )
                    .padding(4.dp)
            ) {


                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomEnd = 0.dp,
                                bottomStart = 16.dp
                            )
                        ),
                    horizontalAlignment = Alignment.Start
                ) {


                    mediaMetadata?.fileAbsolutePath?.let {
                        AudioPlayerUI(it)
                    }


                    Column(
                        Modifier
                            .fillMaxWidth()
                            .background(fileColor.copy(alpha = 0.4f))
                            .padding(8.dp)

                    ) {

                        mediaMetadata?.let {
                            // File Name
                            Text(
                                text = it.originalFileName,
                                fontSize = 12.sp,
                                color = Color.White,
                                style = TextStyle(
                                    platformStyle = PlatformTextStyle(
                                        includeFontPadding = false
                                    )
                                )
                            )
                        }




                        Spacer(Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                        ) {

                            mediaMetadata?.let {
                                Text(
                                    text = humanReadableBytesSize(it.fileSize),
                                    fontSize = 10.sp,
                                    color = Color.White.copy(0.7f),
                                    style = TextStyle(
                                        platformStyle = PlatformTextStyle(
                                            includeFontPadding = false
                                        )
                                    )
                                )

                            }
                            mediaMetadata?.let {
                                Text(
                                    text = "•${
                                        it.fileExtension.removePrefix(".").uppercase()
                                    }",
                                    fontSize = 10.sp,
                                    color = Color.White.copy(0.7f),
                                    style = TextStyle(
                                        platformStyle = PlatformTextStyle(
                                            includeFontPadding = false
                                        )
                                    )
                                )

                            }


                        }
                    }

                }


                when (fileUploadState) {

                    is FileUploadState.Retry -> {


                        mediaMetadata?.fileAbsolutePath?.let { nonNullFileAbsPath ->
                            RetryMediaButton(
                                Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(8.dp)
                            ) {

                                viewModel.onRetrySendMedia(
                                    message.id,
                                    viewModel.userId,
                                    viewModel.recipientId,
                                    message.content,
                                    nonNullFileAbsPath,
                                    mediaMetadata,
                                    repliedMessage?.senderMessageId ?: -1,

                                    {
                                        viewModel.updateUploadState(
                                            message.id,
                                            FileUploadState.Started
                                        )
                                    }
                                ) {
                                    viewModel.updateUploadState(
                                        message.id,
                                        FileUploadState.Retry("Error: Sending audio")
                                    )
                                }
                            }


                        }
                    }

                    is FileUploadState.Started -> {
                        PreLoadingMediaButton {
                            viewModel.cancelMediaUpload(viewModel.chatId, message.id)
                            viewModel.updateUploadState(
                                message.id,
                                FileUploadState.Retry("PreLoading: Audio cancelled by user")
                            )
                        }

                    }

                    is FileUploadState.InProgress -> {

                        UploadingMediaButton(
                            fileUploadState.progress
                        ) {
                            viewModel.cancelMediaUpload(viewModel.chatId, message.id)
                            viewModel.updateUploadState(
                                message.id,
                                FileUploadState.Retry("Uploading: Audio cancelled by the user")
                            )
                        }

                    }

                    else -> {}

                }


            }



            Spacer(modifier = Modifier.height(4.dp))

            // Show timer icon if sending
            when (message.status) {
                ChatMessageStatus.SENDING,
                ChatMessageStatus.QUEUED,
                ChatMessageStatus.QUEUED_MEDIA,
                ChatMessageStatus.QUEUED_MEDIA_RETRY,
                    -> {
                    Icon(
                        painter = painterResource(R.drawable.ic_message_pending), // Using Material Icons
                        contentDescription = "Sending",
                        modifier = Modifier
                            .size(16.dp)
                            .align(Alignment.Start) // Align icon to the end of the message
                    )
                }

                ChatMessageStatus.SENT -> {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Message Sent",
                        modifier = Modifier
                            .size(16.dp)
                            .align(Alignment.Start) // Align icon to the start of the message
                    )
                }

                ChatMessageStatus.DELIVERED -> {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Start)

                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check, // First check icon
                            contentDescription = "Message Delivered",
                            modifier = Modifier.size(16.dp)
                        )
                        Icon(
                            imageVector = Icons.Filled.Check, // Second check icon for double check
                            contentDescription = "Message Delivered",
                            modifier = Modifier
                                .size(16.dp)
                                .offset(x = 8.dp)
                        )
                    }


                }

                ChatMessageStatus.READ -> {}
                ChatMessageStatus.FAILED -> {
                    Icon(
                        imageVector = Icons.Filled.Error, // First check icon
                        contentDescription = "Message not delivered",
                        modifier = Modifier.size(16.dp),
                        tint = Color.Red
                    )
                }

                ChatMessageStatus.FAILED_TO_DISPLAY_REASON_DECRYPTION_FAILED -> {}
                ChatMessageStatus.FAILED_TO_DISPLAY_REASON_UNKNOWN -> {}
            }


            // Timestamp
            Text(
                text = timestamp,
                color = Color(0xFFC0C0C0),
                fontSize = 10.sp,
                modifier = Modifier
                    .padding(top = 4.dp)
            )
        }
    }
}


@Composable
fun ChatMeFileMessageItem(
    message: Message,
    repliedMessage: Message?,
    mediaMetadata: MessageMediaMetadata?,
    userName: String,
    viewModel: ChatViewModel

) {

    val context = LocalContext.current


    val timestamp by rememberSaveable { mutableStateOf(viewModel.formatMessageReceived(message.timestamp)) }


    // Collect only the upload state for the specific messageId
    val uploadState by viewModel.uploadStates.collectAsState()

    // Get the upload state for the specific messageId

    val fileUploadState = uploadState[message.id] ?: when (message.status) {
        ChatMessageStatus.QUEUED_MEDIA -> FileUploadState.Started
        ChatMessageStatus.QUEUED_MEDIA_RETRY -> FileUploadState.Retry("Retry")

        else -> FileUploadState.None
    }

    val fileColor = when (mediaMetadata?.fileMimeType) {
        "application/pdf" -> Color.Red
        "application/vnd.ms-excel" -> Color(0xFF01723A)
        "application/msword" -> Color(0xFF2B7CD3)
        "application/vnd.ms-powerpoint" -> Color(0xFFD04423)
        "text/csv" -> Color(0xFF45b058)
        "text/plain" -> Color.LightGray

        else -> Color.Blue
    }

    val workInfoList by WorkManager.getInstance(context)
        .getWorkInfosFlow(
            WorkQuery.fromUniqueWorkNames("media_upload_${viewModel.chatId}_${message.id}")
        ).collectAsState(null)

    LaunchedEffect(workInfoList) {

        workInfoList?.firstOrNull()?.let { workInfo ->

            val workerState = workInfo.state // WorkInfo state is important here

            if (workerState == WorkInfo.State.CANCELLED) {
                viewModel.updateMessage(
                    message.id,
                    ChatMessageStatus.QUEUED_MEDIA_RETRY
                )
            } else {
                val progressMessageId = workInfo.progress.getLong("messageId", -1)
                val state = workInfo.progress.getString("state")

                if (progressMessageId != -1L && state != null) {
                    viewModel.updateUploadState(
                        progressMessageId,
                        deserializeFileUploadState(state)
                    )
                }
            }

        }

    }


    Column(
        modifier = Modifier
            .fillMaxWidth(0.8f)
    ) {
        // User Name
        Text(
            userName,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 4.dp),
            maxLines = 1
        )

        Box(
            modifier = Modifier
                .background(
                    Color(0xFFECECEA),
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomEnd = 0.dp,
                        bottomStart = 16.dp
                    )
                )
                .padding(4.dp)
        ) {

            Column(
                modifier = Modifier
                    .width(180.dp)
                    .clip(
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomEnd = 0.dp,
                            bottomStart = 16.dp
                        )
                    ),
                horizontalAlignment = Alignment.Start
            ) {


                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .background(fileColor),
                    contentAlignment = Alignment.Center,
                ) {


                    mediaMetadata?.let {
                        // File Name
                        Text(
                            text = it.fileExtension.removePrefix(".").uppercase(),
                            fontSize = 32.sp,
                            modifier = Modifier.padding(8.dp),
                            color = Color.White
                        )

                    }

                    when (fileUploadState) {

                        is FileUploadState.Retry -> {

                            mediaMetadata?.fileAbsolutePath?.let { nonNullFileAbsPath ->
                                RetryMediaButton(
                                    Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(8.dp)

                                ) {

                                    viewModel.onRetrySendMedia(
                                        message.id,
                                        viewModel.userId,
                                        viewModel.recipientId,
                                        message.content,
                                        nonNullFileAbsPath,
                                        mediaMetadata,
                                        repliedMessage?.senderMessageId ?: -1,
                                        {
                                            viewModel.updateUploadState(
                                                message.id,
                                                FileUploadState.Started
                                            )
                                        }
                                    ) {
                                        viewModel.updateUploadState(
                                            message.id,
                                            FileUploadState.Retry("Error: Sending file")
                                        )
                                    }
                                }
                            }

                        }

                        is FileUploadState.Started -> {
                            PreLoadingMediaButton {
                                viewModel.cancelMediaUpload(viewModel.chatId, message.id)
                                viewModel.updateUploadState(
                                    message.id,
                                    FileUploadState.Retry("Preloading: File cancelled by user")
                                )
                            }

                        }

                        is FileUploadState.InProgress -> {

                            UploadingMediaButton(
                                fileUploadState.progress
                            ) {
                                viewModel.cancelMediaUpload(viewModel.chatId, message.id)
                                viewModel.updateUploadState(
                                    message.id,
                                    FileUploadState.Retry("Uploading: File cancelled by the user")
                                )
                            }

                        }

                        else -> {}

                    }


                }


                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(fileColor.copy(alpha = 0.4f))
                        .padding(8.dp)

                ) {
                    mediaMetadata?.let {
                        // File Name
                        Text(
                            text = it.originalFileName,
                            fontSize = 12.sp,
                            color = Color.White,
                            style = TextStyle(
                                platformStyle = PlatformTextStyle(
                                    includeFontPadding = false
                                )
                            )
                        )
                    }


                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                    ) {

                        /*   Text(
                               text = "2 pages",
                               fontSize = 10.sp,
                               color = Color.White.copy(0.7f),
                               style = TextStyle(
                                   platformStyle = PlatformTextStyle(
                                       includeFontPadding = false
                                   )
                               )

                           )*/

                        mediaMetadata?.let {
                            Text(
                                text = "•${humanReadableBytesSize(it.fileSize)}",
                                fontSize = 10.sp,
                                color = Color.White.copy(0.7f),
                                style = TextStyle(
                                    platformStyle = PlatformTextStyle(
                                        includeFontPadding = false
                                    )
                                )
                            )
                        }




                        mediaMetadata?.let {
                            Text(
                                text = "•${it.fileExtension.removePrefix(".").uppercase()}",
                                fontSize = 10.sp,
                                color = Color.White.copy(0.7f),
                                style = TextStyle(
                                    platformStyle = PlatformTextStyle(
                                        includeFontPadding = false
                                    )
                                )
                            )
                        }


                    }
                }


            }

        }




        Spacer(modifier = Modifier.height(4.dp))

        // Show timer icon if sending
        when (message.status) {
            ChatMessageStatus.SENDING,
            ChatMessageStatus.QUEUED,
            ChatMessageStatus.QUEUED_MEDIA,
            ChatMessageStatus.QUEUED_MEDIA_RETRY,

                -> {
                Icon(
                    painter = painterResource(R.drawable.ic_message_pending), // Using Material Icons
                    contentDescription = "Sending",
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.Start) // Align icon to the end of the message
                )
            }

            ChatMessageStatus.SENT -> {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Message Sent",
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.Start) // Align icon to the start of the message
                )
            }

            ChatMessageStatus.DELIVERED -> {
                Box(
                    modifier = Modifier
                        .align(Alignment.Start)

                ) {
                    Icon(
                        imageVector = Icons.Filled.Check, // First check icon
                        contentDescription = "Message Delivered",
                        modifier = Modifier.size(16.dp)
                    )
                    Icon(
                        imageVector = Icons.Filled.Check, // Second check icon for double check
                        contentDescription = "Message Delivered",
                        modifier = Modifier
                            .size(16.dp)
                            .offset(x = 8.dp)
                    )
                }


            }

            ChatMessageStatus.READ -> {}
            ChatMessageStatus.FAILED -> {
                Icon(
                    imageVector = Icons.Filled.Error, // First check icon
                    contentDescription = "Message Delivered",
                    modifier = Modifier.size(16.dp),
                    tint = Color.Red
                )
            }

            ChatMessageStatus.FAILED_TO_DISPLAY_REASON_DECRYPTION_FAILED -> {}
            ChatMessageStatus.FAILED_TO_DISPLAY_REASON_UNKNOWN -> {}
        }

        // Timestamp
        Text(
            text = timestamp,
            color = Color(0xFFC0C0C0),
            fontSize = 10.sp,
            modifier = Modifier
                .padding(top = 4.dp)
        )
    }


}


@Composable
fun OverAllMeRepliedMessageItem(
    message: MessageWithReply,
    userProfileInfo: FeedUserProfileInfo,
    viewModel: ChatViewModel, onRepliedMessageClicked: (Message) -> Unit
) {

    val fileMetadata = message.repliedMessageFileMeta



    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        message.repliedToMessage?.let {


            when (it.type) {
                ChatMessageType.TEXT -> {
                    RepliedMessageContent(
                        it.content,
                        "${userProfileInfo.firstName} ${userProfileInfo.lastName ?: ""}",
                        viewModel.formatMessageReceived(it.timestamp)
                    ) { onRepliedMessageClicked(it) }
                }

                ChatMessageType.IMAGE -> {

                    fileMetadata?.let { fileMetadata ->
                        RepliedMessageVisualMediaContent(
                            it.content,
                            fileMetadata.fileAbsolutePath ?: fileMetadata.fileThumbPath,
                            "${userProfileInfo.firstName} ${userProfileInfo.lastName ?: ""}",

                            viewModel.formatMessageReceived(it.timestamp)
                        ) { onRepliedMessageClicked(it) }
                    }

                }

                ChatMessageType.GIF -> {
                    fileMetadata?.let { fileMetadata ->
                        RepliedMessageVisualMediaContent(
                            it.content,
                            fileMetadata.fileAbsolutePath ?: fileMetadata.fileThumbPath,
                            "${userProfileInfo.firstName} ${userProfileInfo.lastName ?: ""}",

                            viewModel.formatMessageReceived(it.timestamp)
                        ) { onRepliedMessageClicked(it) }
                    }
                }


                ChatMessageType.AUDIO -> {
                    fileMetadata?.let { fileMetadata ->

                        RepliedMessageContent(
                            "${it.content} ${formatTimeSeconds(fileMetadata.totalDuration / 1000f)}",
                            "${userProfileInfo.firstName} ${userProfileInfo.lastName ?: ""}",

                            viewModel.formatMessageReceived(it.timestamp)
                        ) { onRepliedMessageClicked(it) }
                    }
                }

                ChatMessageType.VIDEO -> {
                    fileMetadata?.let { fileMetadata ->
                        RepliedMessageVideoMediaContent(
                            it.content,
                            getThumbnailBitmap(fileMetadata.thumbData),
                            fileMetadata.fileAbsolutePath,
                            "${userProfileInfo.firstName} ${userProfileInfo.lastName ?: ""}",

                            viewModel.formatMessageReceived(it.timestamp)
                        ) { onRepliedMessageClicked(it) }
                    }
                }

                ChatMessageType.FILE -> {
                    RepliedMessageContent(
                        it.content,
                        "${userProfileInfo.firstName} ${userProfileInfo.lastName ?: ""}",

                        viewModel.formatMessageReceived(it.timestamp)
                    ) { onRepliedMessageClicked(it) }

                }
            }

        } ?: run {
            if (message.receivedMessage.replyId != -1L) {
                RepliedMessageContent(
                    "...",
                    "${userProfileInfo.firstName} ${userProfileInfo.lastName ?: ""}",

                    ""
                ) {}
            }
        }
    }
}


@Composable
fun ChatOtherMessageItem(
    viewModel: ChatViewModel,
    userName: String,
    profileIUrl: String?, // Use drawable resource ID for the profile image
    message: String,
    timestamp: String,
) {


    val context = LocalContext.current

    val imageRequest = ImageRequest.Builder(context)
        .data(profileIUrl)
        .placeholder(R.drawable.user_placeholder) // Your placeholder image
        .error(R.drawable.user_placeholder)
        .crossfade(true)
        .build()



    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .fillMaxHeight()
        ) {


            Row(verticalAlignment = Alignment.CenterVertically) {


                // User Name
                Text(
                    text = userName,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .padding(start = 8.dp),
                    maxLines = 1
                )

                Spacer(modifier = Modifier.width(8.dp))

                AsyncImage(
                    imageRequest,
                    imageLoader = viewModel.chatUsersProfileImageLoader,
                    contentDescription = "User Profile Image",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

            }

            Spacer(modifier = Modifier.height(8.dp))

            ExpandableText(
                message,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.White
                ),
                showMoreStyle = SpanStyle(color = Color.Black),
                showLessStyle = SpanStyle(color = Color.Black),
                textModifier = Modifier.wrapContentSize(),
                linkColor = Color.LightGray,
                modifier = Modifier
                    .background(
                        Color(0xFF4399FF),
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomEnd = 0.dp,
                            bottomStart = 16.dp
                        )
                    )
                    .padding(vertical = 4.dp, horizontal = 16.dp)
            )

            // Timestamp
            Text(
                text = timestamp,
                color = Color(0xFFC0C0C0),
                fontSize = 10.sp,
                modifier = Modifier.padding(top = 4.dp)
            )


        }
    }
}


@Composable
fun ChatOtherImageMessageItem(
    message: Message,
    mediaMetadata: MessageMediaMetadata?,
    userName: String,
    profileIUrl: String?,
    onNavigateImageSlider: (Uri, Int, Int) -> Unit,
    viewModel: ChatViewModel
) {

    val context = LocalContext.current

    val width = mediaMetadata?.width ?: -1
    val height = mediaMetadata?.height ?: -1

    val mediaAbsPath = mediaMetadata?.fileAbsolutePath

    val blurredThumbnailBitmap by remember(mediaMetadata) {
        mutableStateOf(getThumbnailBitmap(mediaMetadata?.thumbData))
    }


    val timestamp = viewModel.formatMessageReceived(message.timestamp)

    val isDownloaded = mediaMetadata?.fileAbsolutePath?.let { File(it).exists() } ?: false


    val downloadState by viewModel.downloadStates.collectAsState()

    val downloadStatus = downloadState[message.id]


    val imageRequest = ImageRequest.Builder(context)
        .data(profileIUrl)
        .placeholder(R.drawable.user_placeholder) // Your placeholder image
        .error(R.drawable.user_placeholder)
        .crossfade(true)
        .build()

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .fillMaxHeight()
        ) {


            AsyncImage(
                imageRequest,
                contentDescription = "User Profile Image",
                imageLoader = viewModel.chatUsersProfileImageLoader,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )


            // User Name
            Text(
                text = userName,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .padding(start = 8.dp),
                maxLines = 1
            )


            Box(
                modifier = Modifier
                    .background(
                        Color(0xFFECECEA),
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomEnd = 0.dp,
                            bottomStart = 16.dp
                        )
                    )
                    .padding(4.dp)
                    .clickable {

                        mediaAbsPath?.let {
                            if (!File(it).exists()) {
                                Toast
                                    .makeText(context, "Media is not exist", Toast.LENGTH_SHORT)
                                    .show()
                            } else {
                                onNavigateImageSlider(Uri.parse(mediaAbsPath), width, height)

                            }
                        }
                    }

            ) {

                SubcomposeAsyncImage(ImageRequest
                    .Builder(context)
                    .data(mediaAbsPath)
                    .also {
                        if (width > 0 && height > 0) {
                            it.size(Size(width, height)) // Use the original size of the image
                        }
                    }
                    .build(),
                    contentDescription = "Loaded Image",
                    error = {
                        blurredThumbnailBitmap?.let {
                            Image(
                                it.asImageBitmap(),
                                contentDescription = "Loaded Image",
                                modifier = Modifier
                                    .clip(shape = RoundedCornerShape(16.dp))
                                    .then(
                                        if (width > 0 && height > 0) {
                                            Modifier.aspectRatio(width.toFloat() / height.toFloat()) // Use the original size of the image
                                        } else {
                                            Modifier
                                        }
                                    )
                            )
                        }
                    },
                    loading = {
                        blurredThumbnailBitmap?.let {

                            Image(
                                it.asImageBitmap(),
                                contentDescription = "Loaded Image",
                                modifier = Modifier
                                    .clip(shape = RoundedCornerShape(16.dp))
                                    .then(
                                        if (width > 0 && height > 0) {
                                            Modifier.aspectRatio(width.toFloat() / height.toFloat()) // Use the original size of the image
                                        } else {
                                            Modifier
                                        }
                                    )
                            )

                        }
                    },
                    modifier = Modifier
                        .clip(shape = RoundedCornerShape(16.dp))
                        .then(
                            if (width > 0 && height > 0) {
                                Modifier.aspectRatio(width.toFloat() / height.toFloat()) // Use the original size of the image
                            } else {
                                Modifier
                            }
                        )
                )


                // Download Icon shown only if not downloaded
                if (!isDownloaded && (downloadStatus == null || downloadStatus == MediaDownloadState.Failed)) {


                    mediaMetadata?.let {
                        if (it.fileDownloadUrl != null) {
                            DownloadMediaButton(
                                it.fileSize,
                                Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(8.dp)
                            ) {
                                viewModel.downloadMediaAndUpdateMessage(
                                    context.findActivity(),
                                    message.id,
                                    message.senderId,
                                    it.fileDownloadUrl,
                                    it.fileCachePath,
                                    it
                                )

                            }
                        }
                    }

                }

                if (!isDownloaded) {

                    downloadStatus?.let {

                            nonNullDownloadStatus ->
                        mediaMetadata?.let {
                            when (nonNullDownloadStatus) {
                                is MediaDownloadState.Started -> PreLoadingMediaButton {
                                    viewModel.cancelDownload(message.id)
                                }

                                is MediaDownloadState.InProgress -> DownloadingMediaButton(
                                    it.fileSize,
                                    nonNullDownloadStatus.downloadedBytes
                                ) {
                                    viewModel.cancelDownload(message.id)
                                }

                                else -> {}
                            }
                        }
                    }


                }


            }

            // Timestamp
            Text(
                text = timestamp,
                color = Color(0xFFC0C0C0),
                fontSize = 10.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}


@Composable
fun ChatOtherVideoMessageItem(
    message: Message,
    mediaMetadata: MessageMediaMetadata?,
    userName: String,
    profileIUrl: String?, // Use drawable resource ID for the profile image
    viewModel: ChatViewModel,
    onNavigateVideoPlayer: (Uri, Int, Int, Long) -> Unit
) {

    val context = LocalContext.current

    val width = mediaMetadata?.width ?: 0
    val height = mediaMetadata?.height ?: 0

    var thumbnail by remember { mutableStateOf<Bitmap?>(null) }

    val blurredThumbnailBitmap: Bitmap? by remember {
        mutableStateOf(
            getThumbnailBitmap(
                mediaMetadata?.thumbData
            )
        )
    }


    // Timestamp (simplified)
    val timestamp = viewModel.formatMessageReceived(message.timestamp)

    // Check if the file is downloaded (simplified)
    val isDownloaded = mediaMetadata?.fileAbsolutePath?.let { File(it).exists() } ?: false


    val downloadState by viewModel.downloadStates.collectAsState()

    val downloadStatus = downloadState[message.id]


    val imageRequest = ImageRequest.Builder(context)
        .data(profileIUrl)
        .placeholder(R.drawable.user_placeholder) // Your placeholder image
        .error(R.drawable.user_placeholder)
        .crossfade(true)
        .build()


    LaunchedEffect(mediaMetadata) {
        mediaMetadata?.fileAbsolutePath?.let {
            // Run the heavy work in background thread
            if (File(it).exists()) {
                // Using a coroutine to load the thumbnail in the background
                thumbnail = withContext(Dispatchers.IO) {
                    MediaMetadataRetriever().getThumbnailFromPath(it)
                }
            }
        }
    }




    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .fillMaxHeight()
        ) {


            AsyncImage(
                imageRequest,
                contentDescription = "User Profile Image",
                imageLoader = viewModel.chatUsersProfileImageLoader,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )


            // User Name
            Text(
                text = userName,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .padding(start = 8.dp),
                maxLines = 1
            )


            Box(
                modifier = Modifier
                    .background(
                        Color(0xFFECECEA),
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomEnd = 0.dp,
                            bottomStart = 16.dp
                        )
                    )
                    .padding(4.dp)
                    .clickable {
                        mediaMetadata?.let { nonNullFileMetaData ->
                            nonNullFileMetaData.fileAbsolutePath?.let {

                                if (!File(it).exists()) {
                                    Toast
                                        .makeText(context, "Media is not exist", Toast.LENGTH_SHORT)
                                        .show()
                                } else {
                                    onNavigateVideoPlayer(
                                        Uri.parse(it),
                                        nonNullFileMetaData.width,
                                        nonNullFileMetaData.height,
                                        nonNullFileMetaData.totalDuration
                                    )
                                }

                            }
                        }

                    }
            ) {


                thumbnail?.let {
                    Image(
                        it.asImageBitmap(),
                        contentDescription = "Loaded Image",
                        modifier = Modifier
                            .clip(shape = RoundedCornerShape(16.dp))
                            .then(
                                if (width > 0 && height > 0) {
                                    Modifier.aspectRatio(width.toFloat() / height.toFloat()) // Use the original size of the image
                                } else {
                                    Modifier
                                }
                            )
                    )
                } ?: run {

                    blurredThumbnailBitmap?.let {
                        Image(
                            it.asImageBitmap(),
                            contentDescription = "Loaded Image",
                            modifier = Modifier
                                .clip(shape = RoundedCornerShape(16.dp))
                                .then(
                                    if (width > 0 && height > 0) {
                                        Modifier.aspectRatio(width.toFloat() / height.toFloat()) // Use the original size of the image
                                    } else {
                                        Modifier
                                    }
                                )
                        )
                    }

                }


                Image(
                    painterResource(android.R.drawable.ic_media_play),
                    contentDescription = "Play video",
                    Modifier
                        .size(40.dp, 40.dp)
                        .align(Alignment.Center)
                )


                // Download Icon shown only if not downloaded
                if (!isDownloaded && (downloadStatus == null || downloadStatus == MediaDownloadState.Failed)) {

                    mediaMetadata?.let {
                        if (it.fileDownloadUrl != null) {
                            DownloadMediaButton(
                                it.fileSize,
                                Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(8.dp)
                            ) {
                                viewModel.downloadMediaAndUpdateMessage(
                                    context.findActivity(),
                                    message.id,
                                    message.senderId,
                                    it.fileDownloadUrl,
                                    it.fileCachePath,
                                    it
                                )

                            }
                        }
                    }

                }

                if (!isDownloaded) {

                    mediaMetadata?.let { fileMetaData ->
                        downloadStatus?.let {
                            when (it) {
                                is MediaDownloadState.Started -> PreLoadingMediaButton {
                                    viewModel.cancelDownload(message.id)
                                }

                                is MediaDownloadState.InProgress -> DownloadingMediaButton(
                                    fileMetaData.fileSize,
                                    it.downloadedBytes
                                ) {
                                    viewModel.cancelDownload(message.id)
                                }

                                else -> {}
                            }
                        }
                    }

                }


            }

            // Timestamp
            Text(
                text = timestamp,
                color = Color(0xFFC0C0C0),
                fontSize = 10.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}


@Composable
fun ChatOtherAudioMessageItem(
    message: Message,
    mediaMetadata: MessageMediaMetadata?,
    userName: String,
    profileIUrl: String?,
    viewModel: ChatViewModel
) {


    val context = LocalContext.current


    // Direct assignment for fileMetadata (no need for remember here)
    val fileMetadata = mediaMetadata


    val fileColor = when (fileMetadata?.fileMimeType) {
        // Specific colors for different audio file types
        "audio/mpeg" -> Color(0xFF4CAF50)  // Green for MP3
        "audio/wav" -> Color(0xFF2196F3)  // Blue for WAV
        "audio/ogg" -> Color(0xFFFF5722)  // Orange for OGG
        "audio/flac" -> Color(0xFF9C27B0)  // Purple for FLAC
        "audio/aac" -> Color(0xFFFF9800)  // Amber for AAC

        // Default color for other file types (non-audio)
        else -> Color.LightGray  // Blue for non-audio files
    }


    // Timestamp (simplified)
    val timestamp = viewModel.formatMessageReceived(message.timestamp)

    val isDownloaded = fileMetadata?.fileAbsolutePath?.let { File(it).exists() } ?: false


    val downloadState by viewModel.downloadStates.collectAsState()

    val downloadStatus = downloadState[message.id]


    val imageRequest = ImageRequest.Builder(context)
        .data(profileIUrl)
        .placeholder(R.drawable.user_placeholder) // Your placeholder image
        .error(R.drawable.user_placeholder)
        .crossfade(true)
        .build()

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .fillMaxHeight()
        ) {

            AsyncImage(
                imageRequest,
                contentDescription = "User Profile Image",
                imageLoader = viewModel.chatUsersProfileImageLoader,

                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )


            // User Name
            Text(
                text = userName,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .padding(start = 8.dp),
                maxLines = 1
            )


            Box(
                modifier = Modifier
                    .background(
                        Color(0xFFECECEA),
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomEnd = 0.dp,
                            bottomStart = 16.dp
                        )
                    )
                    .padding(4.dp)
            ) {


                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomEnd = 0.dp,
                                bottomStart = 16.dp
                            )
                        ),
                    horizontalAlignment = Alignment.Start
                ) {


                    if (isDownloaded) {
                        fileMetadata?.let { nonNullMediaMetadata ->
                            nonNullMediaMetadata.fileAbsolutePath?.let {
                                AudioPlayerUI(it)
                            }
                        }

                    } else {

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 140.dp)
                                .background(fileColor),
                            contentAlignment = Alignment.Center,
                        ) {


                            fileMetadata?.let {
                                Text(
                                    text = it.fileExtension.removePrefix(".").uppercase(),
                                    fontSize = 32.sp,
                                    modifier = Modifier.padding(8.dp),
                                    color = Color.White
                                )

                            }


                            // Download Icon shown only if not downloaded
                            if ((downloadStatus == null || downloadStatus == MediaDownloadState.Failed)) {

                                fileMetadata?.let {
                                    if (it.fileDownloadUrl != null) {
                                        DownloadMediaButton(
                                            it.fileSize,
                                            Modifier
                                                .align(Alignment.BottomEnd)
                                                .padding(8.dp)
                                        ) {
                                            viewModel.downloadMediaAndUpdateMessage(
                                                context.findActivity(),
                                                message.id,
                                                message.senderId,
                                                it.fileDownloadUrl,
                                                it.fileCachePath,
                                                it
                                            )

                                        }
                                    }
                                }

                            } else {
                                fileMetadata?.let { fileMetadata ->

                                    when (downloadStatus) {
                                        is MediaDownloadState.Started -> PreLoadingMediaButton {
                                            viewModel.cancelDownload(message.id)
                                        }

                                        is MediaDownloadState.InProgress -> DownloadingMediaButton(
                                            fileMetadata.fileSize,
                                            downloadStatus.downloadedBytes
                                        ) {
                                            viewModel.cancelDownload(message.id)
                                        }

                                        else -> {}
                                    }

                                }
                            }


                        }

                    }




                    Column(
                        Modifier
                            .fillMaxWidth()
                            .background(fileColor.copy(alpha = 0.4f))
                            .padding(8.dp)

                    ) {

                        fileMetadata?.let {
                            // File Name
                            Text(
                                text = it.originalFileName,
                                fontSize = 12.sp,
                                color = Color.White,
                                style = TextStyle(
                                    platformStyle = PlatformTextStyle(
                                        includeFontPadding = false
                                    )
                                )
                            )
                        }




                        Spacer(Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                        ) {

                            fileMetadata?.let {
                                Text(
                                    text = humanReadableBytesSize(it.fileSize),
                                    fontSize = 10.sp,
                                    color = Color.White.copy(0.7f),
                                    style = TextStyle(
                                        platformStyle = PlatformTextStyle(
                                            includeFontPadding = false
                                        )
                                    )
                                )
                            }


                            fileMetadata?.let {
                                Text(
                                    text = "•${
                                        it.fileExtension.removePrefix(".").uppercase()
                                    }",
                                    fontSize = 10.sp,
                                    color = Color.White.copy(0.7f),
                                    style = TextStyle(
                                        platformStyle = PlatformTextStyle(
                                            includeFontPadding = false
                                        )
                                    )
                                )
                            }


                        }
                    }

                }


            }


            // Timestamp
            Text(
                text = timestamp,
                color = Color(0xFFC0C0C0),
                fontSize = 10.sp,
                modifier = Modifier
                    .padding(top = 4.dp)
            )
        }
    }
}


@Composable
fun ChatOtherFileMessageItem(
    message: Message,
    mediaMetadata: MessageMediaMetadata?,
    userName: String,
    profileIUrl: String?, // Use drawable resource ID for the profile image
    viewModel: ChatViewModel
) {
    val context = LocalContext.current

    // Direct assignment for fileMetadata (no need for remember here)
    val fileMetadata = mediaMetadata
    val timestamp = viewModel.formatMessageReceived(message.timestamp)
    val isDownloaded = fileMetadata?.fileAbsolutePath?.let { File(it).exists() } ?: false


    val downloadState by viewModel.downloadStates.collectAsState()

    val downloadStatus = downloadState[message.id]

    val fileColor = when (fileMetadata?.fileMimeType) {
        // Document file types
        "application/pdf" -> Color.Red
        "application/vnd.ms-excel" -> Color(0xFF01723A)  // Excel color (Greenish)
        "application/msword" -> Color(0xFF2B7CD3)  // Word color (Blue)
        "application/vnd.ms-powerpoint" -> Color(0xFFD04423)  // PowerPoint color (Orange)
        "text/csv" -> Color(0xFF45b058)  // CSV color (Green)
        "text/plain" -> Color(0xFF737678)  // Text file color (Gray)

        // Compressed file types
        "application/zip" -> Color(0xFFFACC14)  // ZIP color (Dark Gray)
        "application/x-rar-compressed" -> Color(0xFF552C8A)  // RAR color (Reddish Brown)
        "application/x-tar" -> Color(0xFFE38001)  // TAR color (Brownish)
        "application/gzip" -> Color(0xFF53617E)  // GZ (Greenish-Blue)

        // Miscellaneous file types
        "text/html" -> Color(0xFFE0482E)  // HTML color (Blue)
        "application/json" -> Color(0xFF1BB24B)  // JSON color (Light Blue)
        "application/xml" -> Color(0xFFE44b4D)  // XML color (Dark Green)

        // Default color for unknown types
        else -> Color.LightGray
    }


    val imageRequest = ImageRequest.Builder(context)
        .data(profileIUrl)
        .placeholder(R.drawable.user_placeholder) // Your placeholder image
        .error(R.drawable.user_placeholder)
        .crossfade(true)
        .build()
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .fillMaxHeight()
        ) {

            AsyncImage(
                imageRequest,
                imageLoader = viewModel.chatUsersProfileImageLoader,
                contentDescription = "User Profile Image",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )


            // User Name
            Text(
                text = userName,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .padding(start = 8.dp),
                maxLines = 1
            )


            Box(
                modifier = Modifier
                    .background(
                        Color(0xFFECECEA),
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomEnd = 0.dp,
                            bottomStart = 16.dp
                        )
                    )
                    .padding(4.dp)
            ) {


                Column(
                    modifier = Modifier
                        .width(180.dp)
                        .clip(
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomEnd = 0.dp,
                                bottomStart = 16.dp
                            )
                        ),
                    horizontalAlignment = Alignment.Start
                ) {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .background(fileColor),
                        contentAlignment = Alignment.Center,
                    ) {

                        fileMetadata?.let {
                            // File Name
                            Text(
                                text = it.fileExtension.removePrefix(".").uppercase(),
                                fontSize = 32.sp,
                                modifier = Modifier.padding(8.dp),
                                color = Color.White
                            )
                        }




                        fileMetadata?.let { fileMetadata ->
                            // Download Icon shown only if not downloaded
                            if (!isDownloaded && (downloadStatus == null || downloadStatus == MediaDownloadState.Failed)) {


                                if (fileMetadata.fileDownloadUrl != null) {
                                    DownloadMediaButton(
                                        fileMetadata.fileSize,
                                        Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(8.dp)
                                    ) {
                                        viewModel.downloadMediaAndUpdateMessage(
                                            context.findActivity(),
                                            message.id,
                                            message.senderId,
                                            fileMetadata.fileDownloadUrl,
                                            fileMetadata.fileCachePath,
                                            fileMetadata
                                        )

                                    }
                                }
                            }

                        }

                        if (!isDownloaded) {
                            fileMetadata?.let { fileMetadata ->
                                downloadStatus?.let {
                                    when (it) {
                                        is MediaDownloadState.Started -> PreLoadingMediaButton {
                                            viewModel.cancelDownload(message.id)
                                        }

                                        is MediaDownloadState.InProgress -> DownloadingMediaButton(
                                            fileMetadata.fileSize,
                                            it.downloadedBytes
                                        ) {
                                            viewModel.cancelDownload(message.id)
                                        }

                                        else -> {}
                                    }
                                }
                            }

                        }

                    }


                    Column(
                        Modifier
                            .fillMaxWidth()
                            .background(fileColor.copy(alpha = 0.4f))
                            .padding(8.dp)

                    ) {
                        fileMetadata?.let { fileMetadata ->
                            // File Name
                            Text(
                                text = fileMetadata.originalFileName,
                                fontSize = 12.sp,
                                color = Color.White,
                                style = TextStyle(
                                    platformStyle = PlatformTextStyle(
                                        includeFontPadding = false
                                    )
                                )
                            )

                        }




                        Spacer(Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                        ) {
/*
                            Text(
                                text = "2 pages",
                                fontSize = 10.sp,
                                color = Color.White.copy(0.7f),
                                style = TextStyle(
                                    platformStyle = PlatformTextStyle(
                                        includeFontPadding = false
                                    )
                                )

                            )*/

                            fileMetadata?.let { fileMetadata ->
                                Text(
                                    text = "•${humanReadableBytesSize(fileMetadata.fileSize)}",
                                    fontSize = 10.sp,
                                    color = Color.White.copy(0.7f),
                                    style = TextStyle(
                                        platformStyle = PlatformTextStyle(
                                            includeFontPadding = false
                                        )
                                    )
                                )
                            }
                            fileMetadata?.let { fileMetadata ->
                                Text(
                                    text = "•${
                                        fileMetadata.fileExtension.removePrefix(".").uppercase()
                                    }",
                                    fontSize = 10.sp,
                                    color = Color.White.copy(0.7f),
                                    style = TextStyle(
                                        platformStyle = PlatformTextStyle(
                                            includeFontPadding = false
                                        )
                                    )
                                )
                            }

                        }
                    }


                }

            }


            // Timestamp
            Text(
                text = timestamp,
                color = Color(0xFFC0C0C0),
                fontSize = 10.sp,
                modifier = Modifier
                    .padding(top = 4.dp)
            )

        }


    }
}


@Composable
fun OverAllOtherRepliedMessageItem(
    message: MessageWithReply,
    userProfileInfo: FeedUserProfileInfo,
    viewModel: ChatViewModel,
    onRepliedMessageClicked: (Message) -> Unit
) {

    val fileMetadata = message.repliedMessageFileMeta




    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {

        message.repliedToMessage?.let {

            when (it.type) {
                ChatMessageType.TEXT -> {
                    RepliedMessageContent(
                        it.content,
                        "You",

                        viewModel.formatMessageReceived(it.timestamp)
                    ) { onRepliedMessageClicked(it) }
                }

                ChatMessageType.IMAGE -> {
                    fileMetadata?.let { fileMetadata ->
                        RepliedMessageVisualMediaContent(
                            it.content,
                            fileMetadata.fileAbsolutePath ?: fileMetadata.fileThumbPath,
                            "You",

                            viewModel.formatMessageReceived(it.timestamp)
                        ) { onRepliedMessageClicked(it) }
                    }

                }

                ChatMessageType.GIF -> {
                    fileMetadata?.let { fileMetadata ->
                        RepliedMessageVisualMediaContent(
                            it.content,
                            fileMetadata.fileAbsolutePath ?: fileMetadata.fileThumbPath,
                            "You",

                            viewModel.formatMessageReceived(it.timestamp)
                        ) { onRepliedMessageClicked(it) }
                    }

                }

                ChatMessageType.AUDIO -> {
                    fileMetadata?.let { fileMetadata ->

                        RepliedMessageContent(
                            "${it.content} ${formatTimeSeconds(fileMetadata.totalDuration / 1000f)}",
                            "You",

                            viewModel.formatMessageReceived(it.timestamp)
                        ) { onRepliedMessageClicked(it) }
                    }

                }

                ChatMessageType.VIDEO -> {

                    fileMetadata?.let { fileMetadata ->
                        RepliedMessageVideoMediaContent(
                            it.content,
                            getThumbnailBitmap(fileMetadata.thumbData),
                            fileMetadata.fileAbsolutePath,
                            "You",

                            viewModel.formatMessageReceived(it.timestamp)
                        ) { onRepliedMessageClicked(it) }
                    }


                }

                ChatMessageType.FILE -> {

                    RepliedMessageContent(
                        it.content,
                        "You",

                        viewModel.formatMessageReceived(it.timestamp)
                    ) { onRepliedMessageClicked(it) }
                }
            }

        } ?: run {
            if (message.receivedMessage.replyId != -1L) {
                RepliedMessageContent(
                    "...",
                    "${userProfileInfo.firstName} ${userProfileInfo.lastName ?: ""}",

                    ""
                ) {}
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioPlayerUI(filePath: String) {

    val context = LocalContext.current

    var isPlaying by remember { mutableStateOf(false) } // Play / Pause state
    var progress by remember { mutableFloatStateOf(0f) } // Progress of the audio
    var duration by remember { mutableLongStateOf(0L) } // Duration of the audio
    var currentTime by remember { mutableLongStateOf(0L) } // Current time of the audio

    // Initialize the MediaPlayer and set up playback
    val mediaPlayer = remember { MediaPlayer() }

    DisposableEffect(filePath) {
        mediaPlayer.setDataSource(context, Uri.parse(filePath))
        mediaPlayer.prepareAsync()
        mediaPlayer.setOnPreparedListener {
            duration = mediaPlayer.duration.toLong()
        }
        mediaPlayer.setOnCompletionListener {
            mediaPlayer.seekTo(0)
            currentTime = 0L
            isPlaying = false // Automatically pause when the audio finishes
        }

        onDispose {
            mediaPlayer.release() // Release the MediaPlayer when the composable is disposed
        }
    }

    // Update progress during playback
    LaunchedEffect(isPlaying) {
        while (isPlaying && currentTime < duration) {
            currentTime = mediaPlayer.currentPosition.toLong()
            progress = currentTime / duration.toFloat()
            delay(33) // Update approximately every 33ms for smooth updates

        }
    }

    // Handle play/pause button click
    val onPlayPauseClick = {
        if (isPlaying) {
            mediaPlayer.pause()
        } else {
            mediaPlayer.start()
        }
        isPlaying = !isPlaying
    }

    val onSeekBarValueChange = { value: Float ->
        currentTime = (value * duration).toLong()
        mediaPlayer.seekTo(currentTime.toInt()) // Convert to milliseconds
        progress = value
    }

    val thumbSize = DpSize(14.dp, 14.dp)

    val interactionSource = remember { MutableInteractionSource() }
    val trackHeight = 4.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 140.dp)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Play/Pause Button
        IconButton(onClick = onPlayPauseClick) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                Modifier.size(32.dp)
            )
        }

        // Progress Bar and Time Display
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Slider(
                value = progress,
                onValueChange = { value -> onSeekBarValueChange(value) },
                valueRange = 0f..1f, // Range from 0 to 1
                modifier =
                Modifier
                    .semantics { contentDescription = "Localized Description" }
                    .requiredSizeIn(minWidth = thumbSize.width, minHeight = trackHeight),
                thumb = {
                    val modifier =
                        Modifier
                            .size(thumbSize)
                            .shadow(1.dp, CircleShape, clip = false)
                            .indication(
                                interactionSource = interactionSource,
                                indication = ripple(bounded = false, radius = 20.dp)
                            )
                    SliderDefaults.Thumb(interactionSource = interactionSource, modifier = modifier)
                },
                onValueChangeFinished = {
                    // Optionally handle what happens when the user stops interacting with the Slider
                },
                track = {
                    val modifier = Modifier.height(trackHeight)
                    SliderDefaults.Track(
                        sliderState = it,
                        modifier = modifier,
                        thumbTrackGapSize = 0.dp,
                        trackInsideCornerSize = 0.dp,
                        drawStopIndicator = null
                    )
                }
            )


            Spacer(modifier = Modifier.height(4.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = formatTimeSeconds(currentTime / 1000f),
                )
                Text(
                    text = formatTimeSeconds(duration / 1000f),
                )
            }
        }
    }
}


@Composable
fun BoxScope.RetryMediaButton(
    modifier: Modifier = Modifier.align(Alignment.Center),
    onRetry: () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(
                Color.Black.copy(alpha = 0.4f),
                shape = CircleShape
            ) // Button background
            .clickable {
                onRetry()
            }
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {

            Text(
                "Retry", color = Color.White,
                style = LocalTextStyle.current.copy(
                    fontSize = 12.sp
                )
            )
            Spacer(modifier = Modifier.width(4.dp))

            Icon(
                imageVector = Icons.Filled.FileUpload,
                contentDescription = "Download Image",
                modifier = Modifier.size(24.dp),
                tint = Color.White
            )
        }

    }
}


@Composable
fun BoxScope.DownloadMediaButton(
    fileSize: Long = -1,
    modifier: Modifier = Modifier.align(Alignment.Center),
    onDownloadClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(
                Color.Black.copy(alpha = 0.4f),
                shape = CircleShape
            ) // Button background
            .clickable {
                onDownloadClick()
            }
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {

            if (fileSize > 0) {
                Text(
                    humanReadableBytesSize(fileSize), color = Color.White,
                    style = LocalTextStyle.current.copy(
                        fontSize = 12.sp
                    )
                )
            }

            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Filled.CloudDownload,
                contentDescription = "Download Image",
                modifier = Modifier.size(18.dp),
                tint = Color.White
            )
        }

    }
}


@Composable
fun BoxScope.PreLoadingMediaButton(onCancel: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(8.dp)
            .align(Alignment.BottomEnd)
            .clip(RoundedCornerShape(8.dp))
            .background(
                Color.Black.copy(alpha = 0.4f),
                shape = RoundedCornerShape(8.dp)
            ) // Button background
            .clickable {
                onCancel()
            }
            .padding(8.dp)
    ) {


        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box {
                CircularProgressIndicatorLegacy(
                    strokeCap = StrokeCap.Square,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(32.dp)
                )

                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Close",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(24.dp),
                    tint = Color.White
                )
            }

        }

    }
}


@Composable
fun BoxScope.DownloadingMediaButton(
    fileSize: Long,
    downloadedSize: Long,
    onDownloadCancel: () -> Unit,
) {

    // Ensure fileSize is greater than zero and clamp progress between 0f and 1f
    val progressPercentage = if (fileSize > 0) {
        // Calculate percentage and ensure it's clamped between 0 and 100
        ((downloadedSize.toFloat() / fileSize.toFloat()) * 100).toInt()
    } else {
        0
    }

    Box(
        modifier = Modifier
            .padding(8.dp)
            .align(Alignment.BottomEnd)
            .clip(RoundedCornerShape(8.dp))
            .background(
                Color.Black.copy(alpha = 0.4f),
                shape = RoundedCornerShape(8.dp)
            ) // Button background
            .clickable {
                onDownloadCancel()
            }
            .padding(8.dp)
    ) {


        Row(verticalAlignment = Alignment.CenterVertically) {

            Text(
                "${humanReadableBytesSize(downloadedSize)}/${humanReadableBytesSize(fileSize)}",
                color = Color.White,
                style = LocalTextStyle.current.copy(
                    fontSize = 12.sp
                )
            )

            Spacer(Modifier.width(4.dp))


            Box {
                CircularProgressIndicator(
                    progress = { progressPercentage / 100f },
                    gapSize = 0.dp,
                    strokeWidth = 2.dp,
                    strokeCap = StrokeCap.Square,
                    modifier = Modifier.size(32.dp)
                )

                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Download Image",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(18.dp),
                    tint = Color.White
                )
            }


        }

    }
}


@Composable
fun BoxScope.UploadingMediaButton(
    progressPercentage: Int,
    onUploadCancel: () -> Unit,
) {


    Box(
        modifier = Modifier
            .padding(8.dp)
            .align(Alignment.BottomEnd)
            .clip(RoundedCornerShape(8.dp))
            .background(
                Color.Black.copy(alpha = 0.4f),
                shape = RoundedCornerShape(8.dp)
            ) // Button background
            .clickable {
                onUploadCancel()
            }
            .padding(8.dp)
    ) {


        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box {
                CircularProgressIndicator(
                    progress = { progressPercentage / 100f },
                    gapSize = 0.dp,
                    strokeWidth = 2.dp,
                    strokeCap = StrokeCap.Square,
                    modifier = Modifier.size(40.dp)
                )

                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Download Image",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(24.dp),
                    tint = Color.White
                )
            }
        }

    }
}


@Composable
fun DecryptionSafeGuard(
    message: MessageWithReply,
    userProfileInfo: FeedUserProfileInfo,
    viewModel: ChatViewModel,
    content: @Composable () -> Unit
) {
    when (message.receivedMessage.status) {
        ChatMessageStatus.FAILED_TO_DISPLAY_REASON_DECRYPTION_FAILED -> {
            ChatOtherMessageItemError(
                "${userProfileInfo.firstName} ${userProfileInfo.lastName ?: ""}",
                userProfileInfo.profilePicUrl96By96,
                "An error caused by an unexpected issue has resulted in the display of this message.",
                viewModel.formatMessageReceived(
                    message.receivedMessage.timestamp
                )
            )

        }

        ChatMessageStatus.FAILED_TO_DISPLAY_REASON_UNKNOWN -> {
            ChatOtherMessageItemError(
                "${userProfileInfo.firstName} ${userProfileInfo.lastName ?: ""}",
                userProfileInfo.profilePicUrl96By96,
                "An error caused by an unexpected issue has resulted in the display of this message.",
                viewModel.formatMessageReceived(
                    message.receivedMessage.timestamp
                )
            )
        }

        else -> {
            content()
        }
    }
}


@Composable
fun ChatOtherMessageItemError(
    userName: String,
    profileIUrl: String?, // Use drawable resource ID for the profile image
    message: String,
    timestamp: String,
) {

    val context = LocalContext.current

    val imageRequest = ImageRequest.Builder(context)
        .data(profileIUrl)
        .placeholder(R.drawable.user_placeholder) // Your placeholder image
        .error(R.drawable.user_placeholder)
        .crossfade(true)
        .build()

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .fillMaxHeight()
        ) {

            AsyncImage(
                imageRequest,
                contentDescription = "User Profile Image",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )


            // User Name
            Text(
                text = userName,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .padding(start = 8.dp),
                maxLines = 1
            )


            ExpandableText(
                message,
                fontSize = 16.sp,
                style = LocalTextStyle.current.copy(
                    color = Color.White
                ),
                showMoreStyle = SpanStyle(color = Color.Black),
                showLessStyle = SpanStyle(color = Color.Black),
                textModifier = Modifier.fillMaxWidth(),
                modifier = Modifier
                    .background(
                        Color.Red,
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomEnd = 0.dp,
                            bottomStart = 16.dp
                        )
                    )
                    .padding(vertical = 8.dp, horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Icon(
                imageVector = Icons.Filled.Error, // First check icon
                contentDescription = "Message Delivered",
                modifier = Modifier.size(16.dp),
                tint = Color.Red
            )

            // Timestamp
            Text(
                text = timestamp,
                color = Color(0xFFC0C0C0),
                fontSize = 10.sp,
                modifier = Modifier
                    .padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun StringTextField(
    value: String,
    state: TextFieldState,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    onChooseAttachmentClicked: () -> Unit,
    // and other arguments you want to delegate
) {


    val scrollState = rememberScrollState()

    // This is effectively a rememberUpdatedState, but it combines the updated state (text) with
    // some state that is preserved across updates (selection).
    var valueWithSelection by remember {
        mutableStateOf(
            TextFieldValue(
                text = value,
                selection = TextRange(value.length),
            )
        )
    }
    valueWithSelection = valueWithSelection.copy(text = value)


    // EditText for message input
    BasicTextField(
        state = state,
//                    onValueChange = { viewModel.onMessageValueChange(it) },
        textStyle = TextStyle.Default.copy(
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = MaterialTheme.typography.bodyMedium.fontSize
        ),

        modifier = modifier.then(
            StateSyncingModifier(
                state = state,
                value = valueWithSelection,
                onValueChanged = {
                    // Don't fire the callback if only the selection/cursor changed.
                    if (it.text != valueWithSelection.text) {
                        onValueChange(it.text)
                    }
                    valueWithSelection = it
                },
                writeSelectionFromTextFieldValue = false
            )

                .background(
                    MaterialTheme.customColorScheme.searchBarColor,
                    RoundedCornerShape(20.dp)
                )
                .heightIn(min = 40.dp)
                .padding(vertical = 8.dp, horizontal = 16.dp)
        ),
        lineLimits = TextFieldLineLimits.MultiLine(maxHeightInLines = 5),
        scrollState = scrollState,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
        decorator = {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScrollWithScrollbar(
                            scrollState,
                            scrollbarConfig = ScrollBarConfig()
                        ),
                    contentAlignment = Alignment.CenterStart

                ) {
                    Box(
                        modifier = Modifier.padding(end = 8.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (state.text.isEmpty()) {
                            Text(
                                "Type message...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        it()
                    }

                }


                if (state.text.isEmpty()) {
                    IconButton(
                        onClick = onChooseAttachmentClicked, modifier =
                        Modifier
                            .size(32.dp)
                            .minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AttachFile,
                            contentDescription = "Add Attachments",
                        )
                    }

                    Spacer(Modifier.width(4.dp))

                    /*         IconButton(

                                 onClick = {}, modifier = Modifier
                                     .size(32.dp)
                                     .minimumInteractiveComponentSize()
                             ) {
                                 Icon(
                                     imageVector = Icons.Filled.PhotoCamera,
                                     contentDescription = "Add Camera Photos",
                                 )
                             }*/
                }

            }

        })


}

/**
 * Synchronizes between [TextFieldState], immutable values, and value change callbacks for
 * [BasicTextField] that may take a value+callback for state instead of taking a [TextFieldState]
 * directly. Effectively a fancy `rememberUpdatedState`.
 *
 * @param writeSelectionFromTextFieldValue If true, [update] will synchronize the selection from the
 * [TextFieldValue] to the [TextFieldState]. The text will be synchronized regardless.
 */
class StateSyncingModifier(
    private val state: TextFieldState,
    private val value: TextFieldValue,
    private val onValueChanged: (TextFieldValue) -> Unit,
    private val writeSelectionFromTextFieldValue: Boolean,
) : ModifierNodeElement<StateSyncingModifierNode>() {

    override fun create(): StateSyncingModifierNode =
        StateSyncingModifierNode(state, onValueChanged, writeSelectionFromTextFieldValue)

    override fun update(node: StateSyncingModifierNode) {
        node.update(value, onValueChanged)
    }

    override fun equals(other: Any?): Boolean {
        // Always call update, without comparing the text. Update can compare more efficiently.
        return false
    }

    override fun hashCode(): Int {
        // Avoid calculating hash from values that can change on every recomposition.
        return state.hashCode()
    }

    override fun InspectorInfo.inspectableProperties() {
        // no inspector properties
    }
}

class StateSyncingModifierNode(
    private val state: TextFieldState,
    private var onValueChanged: (TextFieldValue) -> Unit,
    private val writeSelectionFromTextFieldValue: Boolean,
) : Modifier.Node(), ObserverModifierNode, FocusEventModifierNode {

    private var isFocused = false
    private var lastValueWhileFocused: TextFieldValue? = null

    override val shouldAutoInvalidate: Boolean
        get() = false

    /**
     * Synchronizes the latest [value] to the [TextFieldState] and updates our [onValueChanged]
     * callback. Should be called from [ModifierNodeElement.update].
     */
    fun update(value: TextFieldValue, onValueChanged: (TextFieldValue) -> Unit) {
        this.onValueChanged = onValueChanged

        // Don't modify the text programmatically while an edit session is in progress.
        // WARNING: While editing, the code that holds the external state is temporarily not the
        // actual source of truth. This "stealing" of control is generally an anti-pattern. We do it
        // intentionally here because text field state is very sensitive to timing, and if a state
        // update is delivered a frame late, it breaks text input. It is very easy to accidentally
        // introduce small bits of asynchrony in real-world scenarios, e.g. with Flow-based reactive
        // architectures. The benefit of avoiding that easy pitfall outweighs the weirdness in this
        // case.
        if (!isFocused) {
            updateState(value)
        } else {
            this.lastValueWhileFocused = value
        }
    }

    override fun onAttach() {
        // Don't fire the callback on first frame.
        observeTextState(fireOnValueChanged = false)
    }

    override fun onFocusEvent(focusState: FocusState) {
        if (this.isFocused && !focusState.isFocused) {
            // Lost focus, perform deferred synchronization.
            lastValueWhileFocused?.let(::updateState)
            lastValueWhileFocused = null
        }
        this.isFocused = focusState.isFocused
    }

    /** Called by the modifier system when the [TextFieldState] has changed. */
    override fun onObservedReadsChanged() {
        observeTextState()
    }

    private fun updateState(value: TextFieldValue) {
        state.edit {
            // Ideally avoid registering a state change if the text isn't actually different.
            // Take a look at `setTextIfChanged` implementation in TextFieldBuffer
            replace(0, length, value.text)

            // The BasicTextField2(String) variant can't push a selection value, so ignore it.
            if (writeSelectionFromTextFieldValue) {
                selection = value.selection
            }
        }
    }

    private fun observeTextState(fireOnValueChanged: Boolean = true) {
        lateinit var value: TextFieldValue
        observeReads {
            value = TextFieldValue(
                state.text.toString(),
                state.selection,
                state.composition
            )
        }

        // This code is outside of the observeReads lambda so we don't observe any state reads the
        // callback happens to do.
        if (fireOnValueChanged) {
            onValueChanged(value)
        }
    }
}


@Composable
fun BoxScope.CustomWavyTypingIndicator() {
    // Constants for animation
    val duration = 500 // Total duration of the animation in milliseconds
    val delayPerItem = 150 // Delay between each dot's animation start
    val dotCount = 3 // Number of dots

    // Create a list of Animatables for the dots
    val dotOffsets = remember { List(dotCount) { Animatable(0f) } }

    //Launch a coroutine for each dot's animation
    dotOffsets.forEachIndexed { index, animatable ->
        LaunchedEffect(animatable) {
            animatable.animateTo(
                targetValue = -5f, // Move up by 10 dp
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = duration, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse,
                    initialStartOffset = StartOffset(offsetMillis = (delayPerItem * index))
                )
            )
        }
    }


    Card(
        modifier = Modifier
            .wrapContentWidth()
            .align(Alignment.BottomStart)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 16.dp,
            bottomEnd = 16.dp,
            bottomStart = 0.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .wrapContentWidth()
                .padding(8.dp)
        ) {

            // Display the dots with their respective offsets
            dotOffsets.forEach { offset ->
                Text(
                    text = "●",
                    modifier = Modifier.offset {
                        IntOffset(
                            x = 0,
                            y = offset.value.dp.roundToPx()
                        )
                    }, // Apply the vertical offset
                    color = Color.White,
                    fontSize = 16.sp,
                    lineHeight = 16.sp // Set line height to match font size
                )
            }
        }
    }

    Spacer(Modifier.height(8.dp))

}
