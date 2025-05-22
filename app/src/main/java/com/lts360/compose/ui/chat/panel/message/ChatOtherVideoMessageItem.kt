package com.lts360.compose.ui.chat.panel.message

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import com.lts360.R
import com.lts360.app.database.models.chat.Message
import com.lts360.app.database.models.chat.MessageMediaMetadata
import com.lts360.app.database.models.chat.ThumbnailLoader.getThumbnailBitmap
import com.lts360.components.findActivity
import com.lts360.compose.ui.chat.panel.DownloadMediaButton
import com.lts360.compose.ui.chat.panel.DownloadingMediaButton
import com.lts360.compose.ui.chat.panel.PreLoadingMediaButton
import com.lts360.compose.ui.chat.viewmodels.ChatViewModel
import com.lts360.compose.ui.chat.viewmodels.MediaDownloadState
import com.lts360.compose.ui.utils.getThumbnailFromPath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun ChatOtherVideoMessageItem(
    message: Message,
    mediaMetadata: MessageMediaMetadata?,
    userName: String,
    profileIUrl: String?,
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


    val timestamp = viewModel.formatMessageReceived(message.timestamp)

    val isDownloaded = mediaMetadata?.fileAbsolutePath?.let { File(it).exists() } == true


    val downloadState by viewModel.downloadStates.collectAsState()

    val downloadStatus = downloadState[message.id]


    val imageRequest = ImageRequest.Builder(context)
        .data(profileIUrl)
        .placeholder(R.drawable.user_placeholder)
        .error(R.drawable.user_placeholder)
        .crossfade(true)
        .build()


    LaunchedEffect(mediaMetadata) {
        mediaMetadata?.fileAbsolutePath?.let {
            if (File(it).exists()) {
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
                                        it.toUri(),
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