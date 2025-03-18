package com.lts360.test

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lts360.compose.ui.utils.FormatterUtils.formatTimeSeconds
import kotlinx.coroutines.delay

@Composable
fun ChatAudioRecordDialog(
    isRecording: Boolean,
    isPaused: Boolean,
    durationInSeconds: Float,
    frequencies: List<Float>,
    onPauseButtonClicked: () -> Unit,
    onResumeButtonClicked: () -> Unit,
    onSendButtonClicked: () -> Unit,
    onDeleteButtonClicked: () -> Unit
) {

    var blinkState by rememberSaveable{ mutableStateOf(false) }

    val opacity by animateFloatAsState(if (blinkState) 0f else 1f, label = "blink")


    // Change the blink state every 500ms to create a blinking effect
    LaunchedEffect(isRecording) {
        if (!isRecording) {
            blinkState = false
        }
        // This will continuously toggle the blink state every 500ms as long as `isRecording` is true
        while (isRecording) {
            blinkState = !blinkState // Toggle blinkState between true and false
            delay(500) // 500ms delay for blinking effect
        }

    }


    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Box(
            /*    onDismissRequest = onDismissRequest,
                properties = DialogProperties(usePlatformDefaultWidth = false),*/
            modifier = Modifier.clip(RoundedCornerShape(16.dp))
        ) {
            Surface {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Image(
                        imageVector = Icons.Filled.Mic,
                        contentDescription = null,
                        modifier = Modifier
                            .size(80.dp)
                            .alpha(opacity),
                        colorFilter = ColorFilter.tint(Color(0xFFFE8B02))
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        formatTimeSeconds(durationInSeconds),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AudioFrequencyUI(
                        frequencies,
                        contentPadding = PaddingValues(vertical = 16.dp),
                        modifier = Modifier.align(Alignment.End)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .wrapContentSize()
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = 16.dp)
                    ) {

                        if (isPaused) {
                            Box(
                                modifier = Modifier
                                    .border(1.dp, Color.LightGray, CircleShape)
                                    .clip(CircleShape)
                                    .clickable {
                                        onResumeButtonClicked()
                                    }

                            ) {
                                Image(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .size(24.dp),
                                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .border(1.dp, Color.LightGray, CircleShape)
                                    .clip(CircleShape)
                                    .clickable {
                                        onPauseButtonClicked()
                                    }

                            ) {
                                Image(
                                    imageVector = Icons.Default.Pause,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .size(24.dp),
                                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .border(1.dp, Color.LightGray, CircleShape)
                                .clip(CircleShape)
                                .clickable {
                                    onDeleteButtonClicked()
                                }
                        ) {
                            Image(
                                imageVector = Icons.Default.Delete, contentDescription = null,
                                colorFilter = ColorFilter.tint(Color.Red),
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(24.dp)
                            )
                        }


                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable {
                                    onSendButtonClicked()
                                }
                        ) {
                            Image(
                                imageVector = Icons.AutoMirrored.Default.Send,
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(24.dp)
                            )
                        }
                    }
                }


            }
        }
    }

    /*
        val context = LocalContext.current
        Button({


            try {

                // Setting up constants for AudioRecord
                val sampleRate = 44100 // Sampling rate (44.1 kHz)


                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO) // Or CHANNEL_OUT_STEREO based on your use case
                            .setSampleRate(sampleRate)
                            .build()
                    )
                    .setBufferSizeInBytes(audioDataBuffer.size)
                    .build()

                // Write the audio data to AudioTrack and start playback
                audioTrack.write(audioDataBuffer, 0, audioDataBuffer.size)
                audioTrack.play()


            } catch (e: IOException) {
                e.printStackTrace()
            }


        }, shape = RoundedCornerShape(8.dp)) {
            Text("Play")
        }*/
}


@Composable
fun AudioFrequencyUI(
    frequenciesList: List<Float>,
    waveWidth: Dp = 2.dp,
    waveHeight: Dp = 40.dp,
    upperStrokeWidth: Float = 4f,
    lowerStrokeWidth: Float = 4f,
    upperWaveColor: Color = Color(0xFFB900B3),
    lowerWaveColor: Color = Color(0xFFB900B3),
    gapSize: Dp = 4.dp,
    minMagnitude: Float = 10f,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    noiseFilter: Float = 0.9f,
    modifier: Modifier = Modifier
) {

    require(noiseFilter in 0.0f..1.0f) { "Noise filter level must be between 0.0f and 1.0f" }

    val frequencies = remember { mutableStateListOf<Float>() }

    val listState = rememberLazyListState()


    val density = LocalDensity.current

    // This will track the number of visible items
    var visibleItems by remember { mutableIntStateOf(0) }
    var availableWidth by remember { mutableFloatStateOf(0f) }  // To store the available width

    LaunchedEffect(frequencies.size) {
        if (frequencies.isNotEmpty()) {
            listState.scrollToItem(frequencies.size - 1)
        }
    }
    // Calculate the number of visible items based on the available width and item width
    LaunchedEffect(availableWidth, waveWidth, gapSize) {
        if (availableWidth > 0) {
            visibleItems = with(density) {
                ((availableWidth + gapSize.toPx()) / (waveWidth.toPx() + gapSize.toPx())).toInt()
            }
        }
    }



    LaunchedEffect(frequenciesList) {
        frequencies.clear()
        frequencies.addAll(frequenciesList.takeLast(visibleItems).reversed())
    }


    LazyRow(
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(gapSize, Alignment.End),
        modifier = modifier
            .fillMaxWidth()
            .height(waveHeight)
            .onGloballyPositioned { layoutCoordinates ->
                availableWidth = layoutCoordinates.size.width.toFloat()
            },
        contentPadding = contentPadding,
        reverseLayout = true
    ) {

        itemsIndexed(frequencies, key = { index, _ -> index }) { _, magnitude ->


            Box(
                modifier = Modifier
                    .width(waveWidth) // The width of the box
                    .height(waveHeight)
            ) {
/*
                val maxMagnitude = frequencies.maxOrNull() ?: 1 // Default to 1 if the list is empty
*/

                val maxMagnitude = (waveHeight / 2).toPx() ?: 1 // Default to 1 if the list is empty

                Spacer(modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {


                        // Normalize the magnitude value to the height of the box
                        var scaledMagnitude = magnitude / maxMagnitude.toFloat() * size.height / 2
                        scaledMagnitude =
                            if (scaledMagnitude.isNaN()) minMagnitude else scaledMagnitude.coerceAtLeast(
                                minMagnitude
                            )
                        scaledMagnitude = if (noiseFilter == 1f) {
                            scaledMagnitude
                        } else {
                            (scaledMagnitude - (scaledMagnitude * noiseFilter))
                        }

                        scaledMagnitude = scaledMagnitude.coerceAtMost(waveHeight.toPx() / 2)

                        scaledMagnitude = if (scaledMagnitude > minMagnitude) {
                            scaledMagnitude
                        } else {
                            minMagnitude
                        }


                        // Get the center of the box
                        val halfHeight = size.height / 2


                        // Draw the upper line (positive part of the waveform)
                        drawLine(
                            color = upperWaveColor, // Line color
                            start = Offset(
                                size.width / 2,
                                halfHeight
                            ), // Start from the middle (upper half)
                            end = Offset(
                                size.width / 2,
                                halfHeight - scaledMagnitude
                            ), // Go up based on scaled magnitude
                            strokeWidth = upperStrokeWidth
                        )

                        // Draw the lower line (negative part of the waveform)
                        drawLine(
                            color = lowerWaveColor, // Line color
                            start = Offset(
                                size.width / 2,
                                halfHeight
                            ), // Start from the middle (lower half)
                            end = Offset(
                                size.width / 2,
                                halfHeight + scaledMagnitude
                            ), // Go down based on scaled magnitude
                            strokeWidth = lowerStrokeWidth
                        )
                    })
            }
        }
    }

}

