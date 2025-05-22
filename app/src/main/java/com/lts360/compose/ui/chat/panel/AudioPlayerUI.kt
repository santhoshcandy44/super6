package com.lts360.compose.ui.chat.panel

import android.media.MediaPlayer
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSizeIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.lts360.compose.ui.utils.FormatterUtils.formatTimeSeconds
import kotlinx.coroutines.delay


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioPlayerUI(filePath: String) {

    val context = LocalContext.current

    var isPlaying by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    var duration by remember { mutableLongStateOf(0L) }
    var currentTime by remember { mutableLongStateOf(0L) }

    val mediaPlayer = remember { MediaPlayer() }

    DisposableEffect(filePath) {
        mediaPlayer.setDataSource(context, filePath.toUri())
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