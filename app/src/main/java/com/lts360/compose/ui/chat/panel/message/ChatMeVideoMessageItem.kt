package com.lts360.compose.ui.chat.panel.message

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkQuery
import com.lts360.R
import com.lts360.app.database.models.chat.ChatMessageStatus
import com.lts360.app.database.models.chat.Message
import com.lts360.app.database.models.chat.MessageMediaMetadata
import com.lts360.app.database.models.chat.ThumbnailLoader.getThumbnailBitmap
import com.lts360.components.utils.isUriExist
import com.lts360.compose.ui.chat.panel.PreLoadingMediaButton
import com.lts360.compose.ui.chat.panel.RetryMediaButton
import com.lts360.compose.ui.chat.panel.UploadingMediaButton
import com.lts360.compose.ui.chat.viewmodels.ChatViewModel
import com.lts360.compose.ui.chat.viewmodels.FileUploadState
import com.lts360.compose.ui.chat.viewmodels.deserializeFileUploadState
import com.lts360.compose.ui.utils.getThumbnail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
            if (isUriExist(context, it.toUri())) {
                // Using a coroutine to load the thumbnail in the background
                thumbnail = withContext(Dispatchers.IO) {
                    // Create the video thumbnail in background thread
                    MediaMetadataRetriever().getThumbnail(context, it.toUri())
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

                        if (isUriExist(context, nonNullFileAbsPath.toUri())) {

                            mediaMetadata.let {
                                onNavigateVideoPlayer(
                                    nonNullFileAbsPath.toUri(),
                                    it.width,
                                    it.height,
                                    it.totalDuration
                                )
                            }

                        } else {
                            Toast.makeText(context, "Media is not exist", Toast.LENGTH_SHORT)
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

        when (message.status) {
            ChatMessageStatus.SENDING,
            ChatMessageStatus.QUEUED,
            ChatMessageStatus.QUEUED_MEDIA,
            ChatMessageStatus.QUEUED_MEDIA_RETRY,
                -> {
                Icon(
                    painter = painterResource(R.drawable.ic_message_pending),
                    contentDescription = "Sending",
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.Start)
                )
            }

            ChatMessageStatus.SENT -> {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Message Sent",
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.Start)
                )
            }

            ChatMessageStatus.DELIVERED -> {
                Box(
                    modifier = Modifier
                        .align(Alignment.Start)

                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Message Delivered",
                        modifier = Modifier.size(16.dp)
                    )
                    Icon(
                        imageVector = Icons.Filled.Check,
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

        Text(
            text = timestamp,
            color = Color(0xFFC0C0C0),
            fontSize = 10.sp,
            modifier = Modifier
                .padding(top = 4.dp)
        )
    }


}
