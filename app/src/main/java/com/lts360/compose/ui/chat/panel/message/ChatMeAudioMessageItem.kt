package com.lts360.compose.ui.chat.panel.message

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkQuery
import com.lts360.R
import com.lts360.app.database.models.chat.ChatMessageStatus
import com.lts360.app.database.models.chat.Message
import com.lts360.app.database.models.chat.MessageMediaMetadata
import com.lts360.compose.ui.chat.panel.AudioPlayerUI
import com.lts360.compose.ui.chat.panel.PreLoadingMediaButton
import com.lts360.compose.ui.chat.panel.RetryMediaButton
import com.lts360.compose.ui.chat.panel.UploadingMediaButton
import com.lts360.compose.ui.chat.viewmodels.ChatViewModel
import com.lts360.compose.ui.chat.viewmodels.FileUploadState
import com.lts360.compose.ui.chat.viewmodels.deserializeFileUploadState
import com.lts360.compose.ui.utils.FormatterUtils.humanReadableBytesSize

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
}