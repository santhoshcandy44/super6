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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import com.lts360.R
import com.lts360.app.database.models.chat.Message
import com.lts360.app.database.models.chat.MessageMediaMetadata
import com.lts360.components.findActivity
import com.lts360.compose.ui.chat.panel.AudioPlayerUI
import com.lts360.compose.ui.chat.panel.DownloadMediaButton
import com.lts360.compose.ui.chat.panel.DownloadingMediaButton
import com.lts360.compose.ui.chat.panel.PreLoadingMediaButton
import com.lts360.compose.ui.chat.viewmodels.ChatViewModel
import com.lts360.compose.ui.chat.viewmodels.MediaDownloadState
import com.lts360.compose.ui.utils.FormatterUtils.humanReadableBytesSize
import java.io.File

@Composable
fun ChatOtherAudioMessageItem(
    message: Message,
    mediaMetadata: MessageMediaMetadata?,
    userName: String,
    profileIUrl: String?,
    viewModel: ChatViewModel
) {


    val context = LocalContext.current


    val fileMetadata = mediaMetadata


    val fileColor = when (fileMetadata?.fileMimeType) {
        "audio/mpeg" -> Color(0xFF4CAF50)
        "audio/wav" -> Color(0xFF2196F3)
        "audio/ogg" -> Color(0xFFFF5722)
        "audio/flac" -> Color(0xFF9C27B0)
        "audio/aac" -> Color(0xFFFF9800)
        else -> Color.LightGray
    }


    val timestamp = viewModel.formatMessageReceived(message.timestamp)

    val isDownloaded = fileMetadata?.fileAbsolutePath?.let { File(it).exists() } == true


    val downloadState by viewModel.downloadStates.collectAsState()

    val downloadStatus = downloadState[message.id]


    val imageRequest = ImageRequest.Builder(context)
        .data(profileIUrl)
        .placeholder(R.drawable.user_placeholder)
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
                        AudioPlayerUI(fileMetadata.fileAbsolutePath)
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