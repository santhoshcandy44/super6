package com.lts360.compose.ui.chat

import android.content.Context.AUDIO_SERVICE
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.TextureView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSizeIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource.Factory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.extractor.DefaultExtractorsFactory
import com.lts360.R
import com.lts360.components.findActivity
import com.lts360.compose.ui.enterFullScreenMode
import com.lts360.compose.ui.exitFullScreenMode
import com.lts360.compose.ui.theme.AppTheme
import com.lts360.compose.ui.utils.FormatterUtils.formatTimeSeconds
import com.lts360.compose.utils.SafeDrawingBox
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

@AndroidEntryPoint
class PlayerActivity : ComponentActivity(){


    lateinit var exoPlayer:ExoPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exoPlayer = ExoPlayer.Builder(applicationContext).build()

        WindowCompat.setDecorFitsSystemWindows(window,false)
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        // Change the status bar color (example with cyan color)

        windowInsetsController.isAppearanceLightStatusBars = false // Light icons on dark background

        enterFullScreenMode(this)

        val data = intent.data
        val videoWidth = intent.getIntExtra("videoWidth",0)
        val videoHeight = intent.getIntExtra("videoHeight",0)
        val totalDuration = intent.getLongExtra("totalDuration",0L)

        if (data == null) {
            finish()
            return
        }

        setContent {
            AppTheme {

                var fullScreenMode by remember { mutableStateOf(true) }

                SafeDrawingBox(fullScreenMode) {

                    VideoPlayerScreen(data, exoPlayer, videoWidth, videoHeight, totalDuration,{
                        fullScreenMode = it
                    }){
                        this@PlayerActivity.finish()
                    }

                }
            }
        }
    }


    override fun onPause() {
        super.onPause()

        if(exoPlayer.isPlaying){
            exoPlayer.pause()
        }
    }
}




@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayerScreen(
    videoUri: Uri,
    exoPlayer: ExoPlayer,
    videoWidth:Int,
    videoHeight:Int,
    totalDurationMillis:Long,
    onFullScreenModeChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onPopBackStack: () -> Unit
) {



    // Get context and initialize ExoPlayer
    val context = LocalContext.current

    val textureView = remember { TextureView(context) }

    var isPlaying by remember { mutableStateOf(false) }
    var currentDurationMillis by remember { mutableLongStateOf(0L) }

    val thumbSize = DpSize(14.dp, 14.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val trackHeight = 4.dp

    // Control playback (Play/Pause)
    fun togglePlayPause() {
        if (isPlaying) {
            exoPlayer.pause()
        } else {

            if(exoPlayer.playbackState==Player.STATE_IDLE){
                exoPlayer.seekTo(0)
                exoPlayer.prepare()
                exoPlayer.playWhenReady = true

            }else{
                exoPlayer.play()
            }
        }
    }

    // Seek forward/backward
    fun seekForward() {
        val newPos = (exoPlayer.currentPosition + 10000).coerceAtMost(totalDurationMillis)
        exoPlayer.seekTo(newPos)
    }

    fun seekBackward() {
        val newPos = (exoPlayer.currentPosition - 10000).coerceAtLeast(0)
        exoPlayer.seekTo(newPos)
    }



    var progress by remember { mutableFloatStateOf(0f) } // Progress bar value between 0f and 1f
//


    var onValueChange by remember { mutableStateOf(false) }

    LaunchedEffect(Unit){

        val audioManager=context.getSystemService(
            AUDIO_SERVICE
        ) as AudioManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val result= audioManager.requestAudioFocus(
                AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_UNKNOWN)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build())
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener {
                        when(it){
                            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT->{
                                exoPlayer.pause()
                            }
                            AudioManager.AUDIOFOCUS_GAIN->{
                                exoPlayer.play()
                            }
                            AudioManager.AUDIOFOCUS_LOSS->{
                                exoPlayer.pause()
                            }
                        }
                    }.build())

            if(result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
            {
                exoPlayer.play()
            }
        } else {
            val result= audioManager.requestAudioFocus(
                { focusChange ->

                    when(focusChange){
                        AudioManager.AUDIOFOCUS_LOSS_TRANSIENT->{
                            exoPlayer.pause()
                        }
                        AudioManager.AUDIOFOCUS_GAIN->{
                            exoPlayer.play()

                        }
                        AudioManager.AUDIOFOCUS_LOSS->{
                            exoPlayer.pause()

                        }
                    }

                },
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
            if(result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
            {
                exoPlayer.play()
            }
        }


        /*     MediaMetadataRetriever()
                .apply {
                    setDataSource(context,videoUri)
                    withContext(Dispatchers.IO){
                        videoWidth= getFrameAtTime(0)?.width ?:0
                        videoHeight= getFrameAtTime(0)?.height ?:0
                        release()
                    }
                }
    */
    }


    DisposableEffect(Unit) {
        val listener = object : Player.Listener {

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                if(playbackState==Player.STATE_IDLE){
                    textureView.keepScreenOn=false
                }
                if(playbackState==Player.STATE_BUFFERING){
                    textureView.keepScreenOn=true
                }
                if (playbackState == Player.STATE_READY) {
                    // Fetch the total duration in milliseconds
                    /*
                    totalDurationMillis = exoPlayer.duration
                    */

                    if(exoPlayer.isPlaying){
                        textureView.keepScreenOn=true
                    }else{
                        textureView.keepScreenOn=false
                    }
                }



                if (playbackState == Player.STATE_ENDED) {
                    exoPlayer.stop()
                    currentDurationMillis = 0L
                    textureView.keepScreenOn=false
                }
            }

            override fun onIsPlayingChanged(isPlaying_: Boolean) {
                isPlaying = isPlaying_
            }

            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)

                Toast.makeText(context,"Can't play, open with other",Toast.LENGTH_SHORT)
                    .show()

                MediaScannerConnection
                    .scanFile(
                        context,
                        arrayOf(videoUri.path),
                        null
                    ) { _, uri ->

                        val shareIntent = Intent(
                            Intent.ACTION_VIEW
                        ).apply {
                            setDataAndType(uri, type)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }

                        if(shareIntent.resolveActivity(context.packageManager)!=null){

                            context.startActivity(Intent.createChooser(shareIntent, "Open with"))

                        }else{
                            Toast.makeText(context,"No app to play the video",Toast.LENGTH_SHORT)
                                .show()
                        }
                    }

            }
        }


        exoPlayer.addListener(listener)
        onDispose {
            exitFullScreenMode(context.findActivity())
            onFullScreenModeChange(false)
            exoPlayer.removeListener(listener)
            exoPlayer.stop()
            exoPlayer.release()
        }
    }

    LaunchedEffect(videoUri) {
        val mediaItem = MediaItem.fromUri(videoUri)

        exoPlayer.setMediaSource(ProgressiveMediaSource.Factory(
            Factory(context),
            DefaultExtractorsFactory()).createMediaSource(mediaItem))
        exoPlayer.prepare()
    }


    // Update current position and progress when playing
    if (isPlaying) {
        LaunchedEffect(Unit) {
            while (true) {
                currentDurationMillis = exoPlayer.currentPosition
                if(!onValueChange){
                    progress = currentDurationMillis / totalDurationMillis.toFloat()
                }
                delay(33) // Update approximately every 33ms for smooth updates
            }
        }
    }

    val onSeekBarValueChange = { value: Float ->
        currentDurationMillis = (progress * totalDurationMillis).toLong()
        progress = value
    }

    var controlsVisible by remember { mutableStateOf(true) }

    // Function to hide controls immediately
    fun hideControls() {
        enterFullScreenMode(context.findActivity())
        onFullScreenModeChange(true)
        controlsVisible = false  // Hide controls
    }


    // Handler and runnable to hide controls after 10 seconds of inactivity
    val handler = remember { Handler(Looper.getMainLooper()) }
    val runnable = remember {
        Runnable {
            hideControls()
        }
    }

    // Function to show controls and reset the inactivity timer
    fun showControls() {
        controlsVisible = true  // Show controls immediately
        handler.removeCallbacks(runnable) // Reset previous timer
        handler.postDelayed(runnable, 5000) // Hide controls after 10 seconds of inactivity
    }


    LaunchedEffect(onValueChange){

        if(onValueChange){
            handler.removeCallbacks(runnable) // Reset previous timer
        }else{
            handler.postDelayed(runnable, 5000) // Hide controls after 10 seconds of inactivity
        }
    }

    LaunchedEffect(Unit) {
        showControls()  // Initially show controls
    }

    // Composable layout
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        // If controls are visible, hide them
                        if (controlsVisible) {
                            hideControls()  // Hide controls immediately
                        } else {
                            onFullScreenModeChange(false)
                            exitFullScreenMode(context.findActivity())
                            showControls()  // Show controls and reset the timer
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {


        if(videoWidth>0 && videoHeight>0){
            // Video rendering using AndroidExternalSurface
            AndroidView(
                modifier = Modifier
                    .align(Alignment.Center)
                    .aspectRatio(videoWidth.toFloat()/videoHeight.toFloat()),
                factory = { context ->


                    exoPlayer.setVideoTextureView(textureView)

                    textureView
                },
                update = { textureView ->
                    // Update logic if needed
                    if (textureView.isAvailable) {
                        // Set the video surface
                        exoPlayer.setVideoTextureView(textureView)
                    }
                }
            )
        }



        Column(modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.TopCenter)
            .systemBarsPadding()
        ) {

            // Animated TopAppBar
            AnimatedVisibility(
                visible = controlsVisible,
                enter = fadeIn(
                    animationSpec = tween(durationMillis = 100) // Set your desired fade-in duration here
                ),
                exit = fadeOut(
                    animationSpec = tween(durationMillis = 500) // Set your desired fade-out duration here
                )
            ) {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = onPopBackStack) {
                            Icon(
                                Icons.AutoMirrored.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    title = {
                        Text("Player", style = MaterialTheme.typography.titleMedium)
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent,
                        titleContentColor = Color.White,
                        actionIconContentColor = Color.White)
                )
            }

        }

        Column(modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .systemBarsPadding()) {

            AnimatedVisibility(
                visible = controlsVisible,
                enter = fadeIn(
                    animationSpec = tween(durationMillis = 100) // Set your desired fade-in duration here
                ),
                exit = fadeOut(
                    animationSpec = tween(durationMillis = 500) // Set your desired fade-out duration here
                )
            ) {

                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            color = Color.White,
                            text = formatTimeSeconds(currentDurationMillis / 1000f),
                        )

                        Slider(
                            value = progress,
                            onValueChange = {
                                onValueChange=true
                                onSeekBarValueChange(it)
                            },
                            valueRange = 0f..1f, // Range from 0 to 1
                            modifier = Modifier
                                .semantics { contentDescription = "Localized Description" }
                                .requiredSizeIn(minWidth = thumbSize.width, minHeight = trackHeight)
                                .weight(1f),
                            thumb = {
                                SliderDefaults.Thumb(
                                    interactionSource = interactionSource, modifier = Modifier
                                        .size(thumbSize)
                                        .shadow(1.dp, CircleShape, clip = false)
                                        .indication(
                                            interactionSource = interactionSource,
                                            indication = ripple(bounded = false, radius = 20.dp)
                                        )
                                )
                            },

                            onValueChangeFinished = {
                                //Calculate the current time in milliseconds (based on the SeekBar value)
                                currentDurationMillis = (progress * totalDurationMillis).toLong()
                                exoPlayer.seekTo(currentDurationMillis)
                                onValueChange=false

                            },
                            track = {
                                SliderDefaults.Track(
                                    sliderState = it,
                                    modifier = Modifier.height(trackHeight),
                                    thumbTrackGapSize = 0.dp,
                                    trackInsideCornerSize = 0.dp,
                                    drawStopIndicator = null,
                                )
                            }
                        )

                        Text(
                            color = Color.White,
                            text = formatTimeSeconds(totalDurationMillis / 1000f),
                        )
                    }

                    // Video Controls (Play/Pause, Forward, Backward)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Backward Button
                        IconButton(onClick = { seekBackward() }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_video_backward),
                                contentDescription = "Rewind 10s",
                                tint = Color.White
                            )
                        }

                        // Play/Pause Button
                        IconButton(onClick = { togglePlayPause() }) {
                            Icon(
                                painter = if (isPlaying) painterResource(R.drawable.ic_video_pause) else painterResource(
                                    R.drawable.ic_video_play
                                ),
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = Color.White
                            )
                        }

                        // Forward Button
                        IconButton(onClick = { seekForward() }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_video_forward),
                                contentDescription = "Forward 10s",
                                tint = Color.White
                            )
                        }
                    }
                }

            }

        }
    }
}