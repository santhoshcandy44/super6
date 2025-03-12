package com.lts360.libs.imagepicker.ui

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.lts360.components.utils.isUriExist
import com.lts360.compose.ui.chat.formatTimeSeconds
import com.lts360.compose.ui.utils.getMiddleVideoThumbnail
import com.lts360.libs.imagepicker.models.ImageMediaData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


@Composable
fun MediaItemVideo(media: ImageMediaData) {
    val context = LocalContext.current


    var thumbnail by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }  // Flag to track if the thumbnail is loading


    LaunchedEffect(media.uri) {
        // Check if URI exists and retrieve the thumbnail in a background thread
        media.uri.let {
            if (isUriExist(context, it)) {
                // Use MediaMetadataRetriever to fetch the middle thumbnail in a background thread
                if (media.duration != -1L) {
                    try {
                        isLoading = true  // Start loading
                        thumbnail = withContext(Dispatchers.Default) {
                            // Retrieve the middle frame thumbnail or an equivalent
                            MediaMetadataRetriever().getMiddleVideoThumbnail(
                                context,
                                media.duration,
                                it
                            )
                        }
                    } catch (_: Exception) {
                        // Handle error if thumbnail creation fails
                    } finally {
                        isLoading = false  // Thumbnail loaded or failed
                    }
                }
            }
        }
    }

    // Card with adaptive size, aspect ratio ensures items are proportional
    Card(
        modifier = Modifier
            .fillMaxWidth()  // Fill available width
            .aspectRatio(1f),  // Ensure a square aspect ratio
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Show gray placeholder while loading
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    // Gray placeholder background
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Gray.copy(alpha = 0.3f))  // Semi-transparent gray
                    )
                }
            } else {
                // Show the video thumbnail once loaded
                thumbnail?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = media.displayName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } ?: run {
                    // Gray placeholder background
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Gray.copy(alpha = 0.3f))  // Semi-transparent gray
                    )
                }
            }

            if (media.duration != -1L) {
                Text(
                    formatTimeSeconds(media.duration / 1000f),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(4.dp)
                        .background(
                            Color.Black.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(4.dp)
                        .align(Alignment.BottomEnd)

                )

            }
        }
    }
}

