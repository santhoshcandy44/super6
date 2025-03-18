package com.lts360.libs.visualpicker.ui

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lts360.components.utils.isUriExist
import com.lts360.compose.ui.NoRippleInteractionSource
import com.lts360.compose.ui.utils.FormatterUtils.formatTimeSeconds
import com.lts360.compose.ui.utils.getMiddleVideoThumbnail
import com.lts360.libs.imagepicker.models.ImageMediaData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


@Composable
fun MediaItemVideo(
    media: ImageMediaData,
    onClicked: () -> Unit = {},
    isSingle: Boolean = true,
    size: Dp = 80.dp
) {
    val context = LocalContext.current


    var thumbnail by remember {
        mutableStateOf<Bitmap?>(
            VisualsThumbnailsCache.getBitmap(media.uri.toString())
        )
    }
    var isLoading by remember { mutableStateOf(false) }  // Flag to track if the thumbnail is loading


    LaunchedEffect(media) {

        val mediaUri = media.uri

        if (isUriExist(context, mediaUri) && media.duration != -1L && thumbnail == null) {
            // Use MediaMetadataRetriever to fetch the middle thumbnail in a background thread
            try {
                isLoading = true  // Start loading
                thumbnail = withContext(Dispatchers.IO) {
                    // Retrieve the middle frame thumbnail or an equivalent
                    MediaMetadataRetriever().getMiddleVideoThumbnail(
                        context,
                        media.duration,
                        mediaUri
                    ).also {
                        VisualsThumbnailsCache.putBitmap(mediaUri.toString(), it)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false  // Thumbnail loaded or failed
            }
        }

    }

    // Card with adaptive size, aspect ratio ensures items are proportional
    Card(
        onClick = onClicked,
        interactionSource = remember { NoRippleInteractionSource() },
        modifier = Modifier
            .fillMaxWidth()  // Fill available width
            .wrapContentSize(),  // Ensure a square aspect ratio
    ) {
        Box(
            modifier = Modifier.wrapContentSize(),
            contentAlignment = Alignment.Center
        ) {
            // Show gray placeholder while loading
            if (isLoading) {
                Box(
                    modifier = Modifier.size(size),
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
                        modifier = Modifier.size(size)
                    )
                } ?: run {
                    // Gray placeholder background
                    Box(
                        modifier = Modifier
                            .size(size)
                            .background(Color.Gray.copy(alpha = 0.3f))  // Semi-transparent gray
                    )
                }

            }

            if (!isSingle) {
                // Top-right corner indicator for selection status
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)  // Space from the top and right edges
                        .size(24.dp)  // Adjust size as needed
                ) {
                    // Conditional selection indicator (circle)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                color = if (media.isSelected) Color.Yellow else Color.White,  // Orange if selected, white otherwise
                                shape = CircleShape
                            )
                            .border(2.dp, Color.White, CircleShape)  // White border
                    )
                }
            }

            if (media.duration != -1L) {
                Text(
                    formatTimeSeconds(media.duration / 1000f),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
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

