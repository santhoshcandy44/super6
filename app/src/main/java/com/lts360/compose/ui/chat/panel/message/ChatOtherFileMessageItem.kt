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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import com.lts360.compose.ui.chat.panel.DownloadMediaButton
import com.lts360.compose.ui.chat.panel.DownloadingMediaButton
import com.lts360.compose.ui.chat.panel.PreLoadingMediaButton
import com.lts360.compose.ui.chat.viewmodels.ChatViewModel
import com.lts360.compose.ui.chat.viewmodels.MediaDownloadState
import com.lts360.compose.ui.utils.FormatterUtils.humanReadableBytesSize
import java.io.File

@Composable
fun ChatOtherFileMessageItem(
    message: Message,
    mediaMetadata: MessageMediaMetadata?,
    userName: String,
    profileIUrl: String?,
    viewModel: ChatViewModel
) {
    val context = LocalContext.current

    val fileMetadata = mediaMetadata
    val timestamp = viewModel.formatMessageReceived(message.timestamp)
    val isDownloaded = fileMetadata?.fileAbsolutePath?.let { File(it).exists() } == true


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
                            Text(
                                text = it.fileExtension.removePrefix(".").uppercase(),
                                fontSize = 32.sp,
                                modifier = Modifier.padding(8.dp),
                                color = Color.White
                            )
                        }



                        fileMetadata?.let { fileMetadata ->
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
