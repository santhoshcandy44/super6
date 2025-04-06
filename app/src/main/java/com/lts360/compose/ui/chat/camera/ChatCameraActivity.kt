package com.lts360.compose.ui.chat.camera


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.TextureView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageCapture
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource.Factory
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.extractor.DefaultExtractorsFactory
import coil3.compose.AsyncImage
import com.lts360.R
import com.lts360.components.utils.LogUtils.TAG
import com.lts360.compose.ui.common.CircularProgressIndicatorLegacy
import com.lts360.compose.ui.enterFullScreenMode
import com.lts360.compose.ui.theme.AppTheme
import com.lts360.compose.ui.utils.FormatterUtils.formatTimeSeconds
import com.lts360.compose.utils.SafeDrawingBox
import com.lts360.libs.camera.ui.CameraPreview
import com.lts360.libs.camera.ui.TorchButton
import com.lts360.libs.imagepicker.utils.redirectToAppSettings
import com.lts360.libs.media.ui.permissions.MultiplePermissionsRationaleRequestDialog
import com.lts360.libs.media.ui.permissions.PermissionRationaleRequestDialog
import com.lts360.libs.media.ui.permissions.PermissionRequestDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

class CameraVisualPickerActivityContracts {
    class TakeCameraMedia : ActivityResultContract<Unit, Uri?>() {

        override fun createIntent(context: Context, input: Unit): Intent {
            return Intent(context, ChatCameraActivity::class.java)
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
            return if (resultCode == Activity.RESULT_OK) intent?.data else null
        }
    }
}


@AndroidEntryPoint
class ChatCameraActivity : ComponentActivity() {
    @OptIn(UnstableApi::class)
    @SuppressLint("SourceLockedOrientationActivity")
    @kotlin.OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        enterFullScreenMode(this, enableSwipeUp = false)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        super.onCreate(savedInstanceState)

        setContent {

            AppTheme {
                Surface {
                    val context = LocalContext.current

                    val cameraViewModel: ChatCameraXViewModel = hiltViewModel()
                    val capturedUri by cameraViewModel.lastCapturedUri.collectAsState()

                    /*BackHandler(capturedUri != null) {
                        cameraViewModel.updateLastCapturedUri(null)
                    }*/

                    capturedUri?.let {


                        SafeDrawingBox(isFullScreenMode = true){

                            when (cameraViewModel.isImageOrVideo(context, it)) {
                                "Image" -> {
                                    Scaffold(
                                        topBar = {
                                            TopAppBar(
                                                navigationIcon = {
                                                    IconButton(onClick = {
                                                        cameraViewModel.updateLastCapturedUri(null)
                                                    }) {
                                                        Icon(
                                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                            contentDescription = "Back Icon"
                                                        )
                                                    }
                                                },
                                                title = {
                                                    Text(
                                                        text = "",
                                                        style = MaterialTheme.typography.titleMedium
                                                    )
                                                }
                                            )
                                        },
                                        modifier = Modifier.fillMaxSize()
                                    ) { contentPadding ->

                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(contentPadding)
                                                    .weight(1f),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                AsyncImage(
                                                    it,
                                                    contentDescription = null,
                                                    contentScale = ContentScale.Fit,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                )
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 16.dp),
                                                contentAlignment = Alignment.BottomEnd
                                            ) {

                                                IconButton({
                                                    setResult(RESULT_OK, Intent().apply {
                                                        data = it
                                                    })
                                                    finish()
                                                }) {
                                                    Icon(
                                                        Icons.Filled.Check,
                                                        contentDescription = null
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                "Video" -> {
                                    val exoPlayer = cameraViewModel.exoPlayer
                                    var isPlaying by remember { mutableStateOf(false) }
                                    val textureView = remember { TextureView(context) }
                                    var currentDurationMillis by remember { mutableLongStateOf(0L) }


                                    val dimension = remember {
                                        cameraViewModel.getVideoDimensionsFromUri(
                                            context,
                                            it
                                        )
                                    }
                                    val videoWidth = dimension.first
                                    val videoHeight = dimension.second


                                    LaunchedEffect(it) {
                                        exoPlayer.setMediaSource(
                                            ProgressiveMediaSource.Factory(
                                                Factory(context),
                                                DefaultExtractorsFactory()
                                            ).createMediaSource(MediaItem.fromUri(it))
                                        )
                                        exoPlayer.prepare()
                                    }

                                    DisposableEffect(Unit) {
                                        val listener = object : Player.Listener {

                                            override fun onPlaybackStateChanged(playbackState: Int) {
                                                super.onPlaybackStateChanged(playbackState)
                                                if (playbackState == Player.STATE_IDLE) {
                                                    textureView.keepScreenOn = false
                                                }
                                                if (playbackState == Player.STATE_BUFFERING) {
                                                    textureView.keepScreenOn = true
                                                }
                                                if (playbackState == Player.STATE_READY) {
                                                    // Fetch the total duration in milliseconds
                                                    /*
                                                    totalDurationMillis = exoPlayer.duration
                                                    */

                                                    if (exoPlayer.isPlaying) {
                                                        textureView.keepScreenOn = true
                                                    } else {
                                                        textureView.keepScreenOn = false
                                                    }
                                                }



                                                if (playbackState == Player.STATE_ENDED) {
                                                    exoPlayer.stop()
                                                    currentDurationMillis = 0L
                                                    textureView.keepScreenOn = false
                                                }
                                            }

                                            override fun onIsPlayingChanged(playing: Boolean) {
                                                isPlaying = playing
                                            }

                                            override fun onPlayerError(error: PlaybackException) {
                                                super.onPlayerError(error)

                                                Toast.makeText(
                                                    context,
                                                    "Can't play, open with other",
                                                    Toast.LENGTH_SHORT
                                                )
                                                    .show()

                                                MediaScannerConnection
                                                    .scanFile(
                                                        context,
                                                        arrayOf(it.path),
                                                        null
                                                    ) { _, uri ->

                                                        val shareIntent = Intent(
                                                            Intent.ACTION_VIEW
                                                        ).apply {
                                                            setDataAndType(uri, type)
                                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                        }

                                                        if (shareIntent.resolveActivity(context.packageManager) != null) {

                                                            context.startActivity(
                                                                Intent.createChooser(
                                                                    shareIntent,
                                                                    "Open with"
                                                                )
                                                            )

                                                        } else {
                                                            Toast.makeText(
                                                                context,
                                                                "No app to play the video",
                                                                Toast.LENGTH_SHORT
                                                            )
                                                                .show()
                                                        }
                                                    }

                                            }
                                        }

                                        exoPlayer.addListener(listener)
                                        onDispose {
                                            exoPlayer.removeListener(listener)
                                            exoPlayer.stop()
                                            exoPlayer.release()
                                        }
                                    }


                                    // Control playback (Play/Pause)
                                    fun togglePlayPause() {
                                        if (isPlaying) {
                                            exoPlayer.pause()
                                        } else {

                                            if (exoPlayer.playbackState == Player.STATE_IDLE) {
                                                exoPlayer.seekTo(0)
                                                exoPlayer.prepare()
                                                exoPlayer.playWhenReady = true

                                            } else {
                                                exoPlayer.play()
                                            }
                                        }
                                    }


                                    Scaffold(
                                        topBar = {
                                            TopAppBar(
                                                navigationIcon = {
                                                    IconButton(onClick = {
                                                        cameraViewModel.updateLastCapturedUri(null)
                                                    }) {
                                                        Icon(
                                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                            contentDescription = "Back Icon"
                                                        )
                                                    }
                                                },
                                                title = {
                                                    Text(
                                                        text = "",
                                                        style = MaterialTheme.typography.titleMedium
                                                    )
                                                }
                                            )
                                        },
                                        modifier = Modifier.fillMaxSize()
                                    ) { contentPadding ->

                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(contentPadding)
                                                    .weight(1f),
                                                contentAlignment = Alignment.Center
                                            ) {


                                                if (videoWidth > 0 && videoHeight > 0) {
                                                    // Video rendering using AndroidExternalSurface
                                                    AndroidView(
                                                        modifier = Modifier
                                                            .align(Alignment.Center)
                                                            .aspectRatio(videoWidth.toFloat() / videoHeight.toFloat()),
                                                        factory = {
                                                            exoPlayer.setVideoTextureView(
                                                                textureView
                                                            )
                                                            textureView
                                                        },
                                                        update = { textureView ->
                                                            // Update logic if needed
                                                            if (textureView.isAvailable) {
                                                                // Set the video surface
                                                                exoPlayer.setVideoTextureView(
                                                                    textureView
                                                                )
                                                            }
                                                        }
                                                    )
                                                }

                                                Image(
                                                    painterResource(if (isPlaying) R.drawable.ic_video_pause else R.drawable.ic_video_play),
                                                    contentDescription = null,
                                                    modifier = Modifier
                                                        .size(80.dp)
                                                        .clickable(
                                                            interactionSource = null,
                                                            indication = null
                                                        ) {
                                                            togglePlayPause()
                                                        },

                                                    colorFilter = ColorFilter.tint(Color.White)
                                                )

                                            }

                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 16.dp),
                                                contentAlignment = Alignment.BottomEnd
                                            ) {

                                                IconButton({
                                                    setResult(RESULT_OK, Intent().apply {
                                                        data = it
                                                    })
                                                    finish()
                                                }) {
                                                    Icon(
                                                        Icons.Filled.Check,
                                                        contentDescription = null
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }


                    } ?: run {
                        SafeDrawingBox(isFullScreenMode = true) {
                            CameraXApp(cameraViewModel) {
                                this@ChatCameraActivity.finish()
                            }
                        }
                    }
                }
            }

        }
    }
}


@OptIn(ExperimentalGetImage::class)
@Composable
fun CameraXApp(cameraViewModel: ChatCameraXViewModel, onFinishActivity: () -> Unit) {


    val context = LocalContext.current

    val isCameraPermissionGranted by cameraViewModel.isCameraPermissionGranted.collectAsState()
    val isMicPermissionGranted by cameraViewModel.isMicPermissionGranted.collectAsState()

    val cameraSelector by cameraViewModel.cameraSelector.collectAsState()
    val surfaceRequest by cameraViewModel.surfaceRequest.collectAsState()
    val lastCapturedUri by cameraViewModel.lastCapturedUri.collectAsState()
    val videoCaptureUseCase by cameraViewModel.videoCaptureUseCase.collectAsState()
    val camera by cameraViewModel.camera.collectAsState()
    val flashEnabled by cameraViewModel.flashEnabled.collectAsState()
    val isTakingPhoto by cameraViewModel.isTakingPhoto.collectAsState()
    val recording by cameraViewModel.recording.collectAsState()
    val isVideoRecording by cameraViewModel.isVideoRecording.collectAsState()
    val isVideoRecordPaused by cameraViewModel.isVideoRecordPaused.collectAsState()
    val startTime by cameraViewModel.startTime.collectAsState()
    val seconds by cameraViewModel.seconds.collectAsState()
    val isBlinkingRecord by cameraViewModel.isBlinkingRecord.collectAsState()
    val currentItem by cameraViewModel.currentItem.collectAsState()

    val blinkRecordingAnimValue by animateFloatAsState(if (isBlinkingRecord) 0f else 1f)

    var micAccessRequest by remember { mutableStateOf(false) }

    LaunchedEffect(cameraSelector) {
        if (currentItem == 0) {
            val useCase = ImageCapture.Builder()
                .setResolutionSelector(
                    ResolutionSelector.Builder()
                        .setAspectRatioStrategy(
                            AspectRatioStrategy(
                                AspectRatio.RATIO_16_9,
                                AspectRatioStrategy.FALLBACK_RULE_AUTO
                            )
                        )
                        .build()
                )
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .build()
            cameraViewModel.updateImageCaptureUseCase(useCase)
            cameraViewModel.startCapture(useCase)
        } else {
            cameraViewModel.startCapture(videoCaptureUseCase)
        }
    }


    LaunchedEffect(isVideoRecording, isVideoRecordPaused) {
        if (!isVideoRecording && isVideoRecordPaused) {
            cameraViewModel.updateBlinkingRecordingStatus(false)
            return@LaunchedEffect
        }
        while (isVideoRecording && !isVideoRecordPaused) {
            cameraViewModel.updateBlinkingRecordingStatus(!isBlinkingRecord)
            delay(500)
        }
    }


// Update the duration dynamically every second
    LaunchedEffect(isVideoRecording, isVideoRecordPaused) {
        while (isVideoRecording) {
            if (!isVideoRecordPaused) {
                val elapsedSeconds = ((System.currentTimeMillis() - startTime) / 1000f).roundToInt()
                if (seconds != elapsedSeconds) {
                    cameraViewModel.updateSecondsRecorded(elapsedSeconds)
                }
            }
            delay(50) // Update every second
        }
    }

    LaunchedEffect(isMicPermissionGranted) {
        if (micAccessRequest) {
            micAccessRequest = false
        }
    }

    // Camera preview and UI controls
    Box(modifier = Modifier.fillMaxSize()) {

        if (!isCameraPermissionGranted) {
            CameraAndMediaAccess(onFinishActivity) {
                cameraViewModel.updateCameraPermission(it)
            }
        }

        if (micAccessRequest) {
            MicAccess({
                micAccessRequest = false
            }) {
                cameraViewModel.updateMicPermission(it)
            }
        }

        surfaceRequest?.let {
            CameraPreview(it)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
        ) {

            if (isVideoRecording) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        horizontalArrangement = Arrangement
                            .spacedBy(8.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)

                    ) {
                        Row(
                            modifier = Modifier
                                .wrapContentSize()
                                .alpha(blinkRecordingAnimValue),
                            horizontalArrangement = Arrangement
                                .spacedBy(4.dp, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            // Circular Red Box for the Dot
                            Spacer(
                                modifier = Modifier
                                    .size(16.dp) // Size of the circle
                                    .background(Color.Red, shape = CircleShape) // Circular shape
                            )

                            Text(
                                "Record",
                                color = Color.Red,
                                fontSize = 16.sp,
                                style = TextStyle(
                                    platformStyle = PlatformTextStyle(
                                        includeFontPadding = false
                                    )
                                )
                            )
                        }

                        Text(formatTimeSeconds(seconds.toFloat()), fontSize = 24.sp)

                    }

                }


                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {

                        Box(
                            modifier = Modifier
                                .size(50.dp) // Outer circle size (border area)
                                .border(
                                    width = 1.dp, // Border thickness
                                    color = Color.White, // Border color
                                    shape = CircleShape
                                )
                                .clip(CircleShape)
                                .clickable {

                                    cameraViewModel.takePhoto({
                                        Toast.makeText(
                                            context,
                                            "Photo saved",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }) {
                                        Toast.makeText(
                                            context,
                                            "Error capturing photo",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                        ) {


                            Image(
                                imageVector = Icons.Filled.CameraAlt,
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(Color.White),
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(40.dp),
                            )

                        }

                    }

                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp) // Outer circle size (border area)
                                .border(
                                    width = 2.dp, // Border thickness
                                    color = Color.White, // Border color
                                    shape = CircleShape
                                )
                        ) {
                            // Circular Button to capture photo with a white-filled circle and border gap
                            Spacer(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(8.dp)
                                    .size(40.dp) // Size of the circle
                                    .background(
                                        color = Color.Red,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        recording?.stop()
                                    }
                            )
                        }
                    }

                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {

                        Box(
                            modifier = Modifier
                                .size(50.dp) // Outer circle size (border area)
                                .border(
                                    width = 1.dp, // Border thickness
                                    color = Color.White, // Border color
                                    shape = CircleShape
                                )
                                .clip(CircleShape)
                                .clickable {
                                    if (isVideoRecordPaused) {
                                        recording?.resume()
                                    } else {
                                        recording?.pause()
                                    }
                                }
                        ) {

                            Image(
                                imageVector = if (isVideoRecordPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(Color.White),
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(40.dp),
                            )

                        }

                    }


                }

                Spacer(modifier = Modifier.height(64.dp))

            } else {
                Row(modifier = Modifier.fillMaxWidth()) {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        TorchButton(flashEnabled) {
                            camera?.cameraControl?.let {
                                if (flashEnabled) {
                                    it.enableTorch(false)
                                    cameraViewModel.updateIsFlashEnabled(false)
                                } else {
                                    it.enableTorch(true)
                                    cameraViewModel.updateIsFlashEnabled(true)
                                }

                                Toast.makeText(
                                    context,
                                    "Flash: ${if (flashEnabled) "On" else "Off"}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                        }
                    }


                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {


                        Box(
                            modifier = Modifier
                                .size(40.dp) // Outer circle size (border area)
                                .border(
                                    width = 1.dp, // Border thickness
                                    color = Color.White, // Border color
                                    shape = CircleShape
                                )
                                .clip(CircleShape)
                                .clickable {
                                    cameraViewModel.updateCameraSelector(
                                        if (cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA) {
                                            CameraSelector.DEFAULT_BACK_CAMERA
                                        } else {
                                            CameraSelector.DEFAULT_FRONT_CAMERA
                                        }
                                    )
                                }
                        ) {

                            Image(
                                imageVector = Icons.Filled.Cameraswitch,
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(Color.White),
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(40.dp),
                            )

                        }


                    }
                }

                ActionControlsPager(
                    isCameraPermissionGranted,
                    isTakingPhoto,
                    lastCapturedUri,
                    {
                        cameraViewModel.updateCurrentItem(it)
                    },
                    {
                        cameraViewModel.takePhoto({
                            cameraViewModel.updateLastCapturedUri(it)
                            Toast.makeText(
                                context,
                                "Photo saved",
                                Toast.LENGTH_SHORT
                            ).show()
                        }) {
                            Toast.makeText(
                                context,
                                "Error capturing photo",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    },
                    {
                        if (!isMicPermissionGranted) {
                            micAccessRequest = true
                            return@ActionControlsPager
                        }
                        cameraViewModel.startCapture(videoCaptureUseCase)
                        cameraViewModel.prepareVideoRecording {
                            cameraViewModel.updateLastCapturedUri(it)
                        }
                    },
                )

                Spacer(modifier = Modifier.height(64.dp))

            }

        }
    }
}


@Composable
private fun ActionControlsPager(
    isPermissionGranted: Boolean,
    isTakingPhoto: Boolean,
    lastCapturedUri: Uri?,
    onCurrentItemChanged: (Int) -> Unit,
    onTakePicture: () -> Unit,
    onTakeVideo: () -> Unit,
    modifier: Modifier = Modifier
) {

    val tabs = listOf("Photo", "Video")

    val pagerState = rememberPagerState(pageCount = { tabs.size })

    val localConfiguration = LocalConfiguration.current

    val density = LocalDensity.current

    val lazyRowState = rememberLazyListState()

    var itemOffsetToCenter by remember { mutableIntStateOf(0) }
    var selectedIndex by remember { mutableIntStateOf(0) }

    val centerX = with(density) { localConfiguration.screenWidthDp.dp.toPx() } / 2
    var contentPadding by remember { mutableStateOf(0.dp) }


    val scope = rememberCoroutineScope()


    LaunchedEffect(pagerState.currentPage) {
        selectedIndex = pagerState.currentPage
    }

    LaunchedEffect(selectedIndex) {
        scope.launch {
            pagerState.scrollToPage(selectedIndex)
        }
        scope.launch {
            lazyRowState.scrollToItem(selectedIndex)
        }
        onCurrentItemChanged(selectedIndex)
    }

    // Capture scroll position to manually adjust the center
    LaunchedEffect(lazyRowState) {

        snapshotFlow { lazyRowState.layoutInfo.visibleItemsInfo }
            .collect { visibleItems ->

                if (contentPadding == 0.dp) {
                    visibleItems.firstOrNull()?.let {
                        itemOffsetToCenter = it.size
                        contentPadding =
                            (localConfiguration.screenWidthDp.dp - with(density) { itemOffsetToCenter.toDp() }) / 2
                    }
                }

                val startPadding = with(density) { contentPadding.toPx() }  // Convert to px

                val closestItem = visibleItems.minByOrNull { item ->
                    abs((item.offset + startPadding) + (item.size / 2) - centerX).toInt()
                }

                closestItem?.let {
                    itemOffsetToCenter = it.size
                    contentPadding =
                        (localConfiguration.screenWidthDp.dp - with(density) { itemOffsetToCenter.toDp() }) / 2
                    selectedIndex = it.index
                }
            }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        LazyRow(
            state = lazyRowState,
            flingBehavior = rememberSnapFlingBehavior(lazyRowState),
            horizontalArrangement = Arrangement.spacedBy(16.dp), // Centers items
            contentPadding = PaddingValues(
                horizontal = contentPadding,
                vertical = 16.dp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .alpha(if (contentPadding != 0.dp && isPermissionGranted) 1f else 0f)
        ) {
            itemsIndexed(tabs) { index, item ->
                Box(
                    modifier = Modifier
                        .wrapContentSize()
                        .clickable(
                            enabled = contentPadding != 0.dp && isPermissionGranted,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            selectedIndex = index
                        }
                ) {
                    Text(
                        text = item,
                        color = if (selectedIndex == index) Color.Yellow else Color.White
                    )
                }
            }

        }


        HorizontalPager(
            state = pagerState,
            pageSize = PageSize.Fill,
            contentPadding = PaddingValues(vertical = 16.dp)
        ) { page ->


            when (page) {
                0 -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {

                            Box(
                                modifier = Modifier
                                    .size(60.dp) // Outer circle size (border area)
                                    .border(
                                        width = 1.dp, // Border thickness
                                        color = Color.White, // Border color
                                        shape = CircleShape
                                    )
                                    .clip(CircleShape)
                            ) {
                                lastCapturedUri?.let {
                                    AsyncImage(
                                        it,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }


                        }
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp) // Outer circle size (border area)
                                    .border(
                                        width = 2.dp, // Border thickness
                                        color = Color.White, // Border color
                                        shape = CircleShape
                                    )
                                    .clip(CircleShape)
                                    .clickable {
                                        onTakePicture()
                                    }

                            ) {
                                // Circular Button to capture photo with a white-filled circle and border gap
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .padding(8.dp)
                                        .size(70.dp) // Size of the circle
                                        .background(
                                            color = Color.White,
                                            shape = CircleShape
                                        )
                                        .clip(CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isTakingPhoto) {
                                        CircularProgressIndicatorLegacy(
                                            modifier = Modifier.size(30.dp), // Circle inside the button
                                            color = Color.Red,
                                            strokeWidth = 3.dp
                                        )
                                    }
                                }
                            }
                        }

                    }
                }

                else -> {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp) // Outer circle size (border area)
                                .border(
                                    width = 2.dp, // Border thickness
                                    color = Color.White, // Border color
                                    shape = CircleShape
                                )
                                .clip(CircleShape)
                                .clickable {
                                    onTakeVideo()
                                }
                        ) {
                            // Circular Button to capture photo with a white-filled circle and border gap
                            Spacer(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(8.dp)
                                    .size(70.dp) // Size of the circle
                                    .background(
                                        color = Color.Red,
                                        shape = CircleShape
                                    )
                                    .clip(CircleShape)
                            )
                        }
                    }

                }
            }
        }

    }


}

@Composable
fun CameraAndMediaAccess(onDismissRequest: () -> Unit = {}, onPermissionResult: (Boolean) -> Unit) {

    val context = LocalContext.current

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {


        val readExternalStoragePermission = android.Manifest.permission.READ_EXTERNAL_STORAGE


        val accessCamera = android.Manifest.permission.CAMERA


        val hasReadMediaPermissionGranted: () -> Boolean = {
            ContextCompat.checkSelfPermission(
                context, readExternalStoragePermission
            ) == PackageManager.PERMISSION_GRANTED
        }


        val hasAccessCameraPermissionGranted: () -> Boolean = {
            ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        }


        var allRequestPermissionGranted by remember {
            mutableStateOf(
                false
            )
        }


        var isExternalStoragePermissionRequestRationale by rememberSaveable {
            mutableStateOf(
                ActivityCompat.shouldShowRequestPermissionRationale(
                    context as Activity,
                    readExternalStoragePermission
                )
            )
        }

        var isCameraPermissionRequestRationale by rememberSaveable {
            mutableStateOf(
                ActivityCompat.shouldShowRequestPermissionRationale(
                    context as Activity,
                    accessCamera
                )
            )
        }


        var isShowingAllPermissionRequestDialogRationale by rememberSaveable { mutableStateOf(false) }
        var isShowingAllPermissionRequestDialog by rememberSaveable { mutableStateOf(false) }

        var isShowingReadMediaPermissionRequestDialogRationale by rememberSaveable {
            mutableStateOf(
                false
            )
        }
        var isShowingReadMediaPermissionRequestDialog by rememberSaveable { mutableStateOf(false) }

        var isShowingAccessCameraPermissionRequestDialogRationale by rememberSaveable {
            mutableStateOf(
                false
            )
        }
        var isShowingAccessCameraPermissionRequestDialog by rememberSaveable { mutableStateOf(false) }


        // Register the permission request callback for multiple permissions
        val requestPermissionsLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->


            val isReadMediaPermissionGranted = permissions[readExternalStoragePermission]
            val isCameraPermissionGranted = permissions[accessCamera]

            Toast.makeText(context,"${isReadMediaPermissionGranted}", Toast.LENGTH_SHORT)
                .show()

            // Update state based on the permissions result
            if (isReadMediaPermissionGranted == true
                && isCameraPermissionGranted == true
            ) {
                allRequestPermissionGranted = true

            } else {
                allRequestPermissionGranted = false
                isCameraPermissionRequestRationale =
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        context as Activity,
                        accessCamera
                    )


                isExternalStoragePermissionRequestRationale =
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        context,
                        readExternalStoragePermission
                    )

                if ((!isCameraPermissionRequestRationale && isCameraPermissionGranted == false)
                    && (isReadMediaPermissionGranted == false && !isExternalStoragePermissionRequestRationale)
                ) {
                    isShowingAllPermissionRequestDialogRationale = true
                } else if (!isCameraPermissionRequestRationale && isCameraPermissionGranted == false) {
                    isShowingAccessCameraPermissionRequestDialogRationale = true
                } else if (!isExternalStoragePermissionRequestRationale && isReadMediaPermissionGranted == false) {
                    isShowingReadMediaPermissionRequestDialogRationale = true
                } else {
                    if (isReadMediaPermissionGranted == false
                        && isCameraPermissionGranted == false
                    ) {
                        isShowingAllPermissionRequestDialog = true

                    } else if (isCameraPermissionGranted == false) {
                        isShowingAccessCameraPermissionRequestDialog = true
                    } else {
                        isShowingReadMediaPermissionRequestDialog = true
                    }
                }
            }
        }


        val launchPermissions = {
            requestPermissionsLauncher.launch(
                arrayOf(accessCamera, readExternalStoragePermission)
            )
        }

        val launchCameraPermission = {
            requestPermissionsLauncher.launch(
                arrayOf(accessCamera)
            )
        }


        val launchReadMediaPermission = {
            requestPermissionsLauncher.launch(
                arrayOf(readExternalStoragePermission)
            )
        }

        LaunchedEffect(Unit) {
            launchPermissions()
        }

        LifecycleResumeEffect(Unit) {
            if (!allRequestPermissionGranted) {
                if (hasReadMediaPermissionGranted()
                    && hasAccessCameraPermissionGranted()
                ) {
                    allRequestPermissionGranted = true
                }
            }
            onPauseOrDispose {}
        }

        LaunchedEffect(allRequestPermissionGranted) {
            onPermissionResult(allRequestPermissionGranted)
        }

        if (isShowingAllPermissionRequestDialog) {

            MultiplePermissionsRationaleRequestDialog(
                Icons.Filled.CameraAlt,
                Icons.Filled.Photo,
                "Camera & Storage",
                "To take photos or video camera & storage permission is required",
                {
                    launchPermissions()
                    isShowingAllPermissionRequestDialog = false
                }, {
                    onDismissRequest()
                },
                dismissOnClickOutside = false
            )
        }

        if (isShowingAllPermissionRequestDialogRationale) {

            MultiplePermissionsRationaleRequestDialog(
                Icons.Filled.CameraAlt,
                Icons.Filled.Photo,
                "Camera & Storage",
                "To take photos or video camera & storage permission is required",
                {
                    onDismissRequest()
                    redirectToAppSettings(context)
                }, {
                    onDismissRequest()
                },
                dismissOnClickOutside = false
            )
        }

        if (isShowingReadMediaPermissionRequestDialog) {

            PermissionRequestDialog(
                Icons.Filled.Photo,
                "Storage",
                "To take photos or video storage permission is required",
                {
                    launchReadMediaPermission()
                    isShowingReadMediaPermissionRequestDialog = false
                }, {
                    onDismissRequest()
                }, dismissButtonEnabled = false,
                dismissOnClickOutside = false
            )

        }

        if (isShowingReadMediaPermissionRequestDialogRationale) {


            PermissionRationaleRequestDialog(
                Icons.Filled.Photo,
                "Storage",
                "To take photos or video storage permission is required",
                {
                    onDismissRequest()
                    redirectToAppSettings(context)
                }, {
                    onDismissRequest()
                },
                dismissOnClickOutside = false
            )

        }

        if (isShowingAccessCameraPermissionRequestDialog) {
            PermissionRequestDialog(
                Icons.Filled.Camera,
                "Camera",
                "To take photos camera permission is required",
                {
                    launchCameraPermission()
                    isShowingAccessCameraPermissionRequestDialog = false
                }, {
                    onDismissRequest()
                }, dismissButtonEnabled = false,
                dismissOnClickOutside = false
            )
        }

        if (isShowingAccessCameraPermissionRequestDialogRationale) {

            PermissionRationaleRequestDialog(
                Icons.Filled.Camera,
                "Camera",
                "To take photos camera permission is required",
                {
                    onDismissRequest()
                    redirectToAppSettings(context)
                }, {
                    onDismissRequest()
                },
                dismissOnClickOutside = false
            )

        }

    } else {


        val readMediaImages = android.Manifest.permission.READ_MEDIA_IMAGES

        val readMediaVideo = android.Manifest.permission.READ_MEDIA_VIDEO

        val accessCamera = android.Manifest.permission.CAMERA


        val hasReadMediaImagesPermissionGranted: () -> Boolean = {
            ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        }

        val hasReadMediaVideoPermissionGranted: () -> Boolean = {
            ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.READ_MEDIA_VIDEO
            ) == PackageManager.PERMISSION_GRANTED
        }


        val hasAccessCameraPermissionGranted: () -> Boolean = {
            ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        }


        var allRequestPermissionGranted by remember {
            mutableStateOf(
                false
            )
        }


        var isCameraPermissionRequestRationale by rememberSaveable {
            mutableStateOf(
                ActivityCompat.shouldShowRequestPermissionRationale(
                    context as Activity,
                    accessCamera
                )
            )
        }


        var isReadImagesPermissionRequestRationale by rememberSaveable {
            mutableStateOf(
                ActivityCompat.shouldShowRequestPermissionRationale(
                    context as Activity,
                    readMediaImages
                )
            )
        }


        var isReadVideoPermissionRequestRationale by rememberSaveable {
            mutableStateOf(
                ActivityCompat.shouldShowRequestPermissionRationale(
                    context as Activity,
                    readMediaVideo
                )
            )
        }

        var isShowingAllPermissionRequestDialogRationale by rememberSaveable { mutableStateOf(false) }
        var isShowingAllPermissionRequestDialog by rememberSaveable { mutableStateOf(false) }


        var isShowingReadMediaPermissionRequestDialogRationale by rememberSaveable {
            mutableStateOf(
                false
            )
        }

        var isShowingReadMediaPermissionRequestDialog by rememberSaveable { mutableStateOf(false) }


        var isShowingAccessCameraPermissionRequestDialogRationale by rememberSaveable {
            mutableStateOf(
                false
            )
        }
        var isShowingAccessCameraPermissionRequestDialog by rememberSaveable { mutableStateOf(false) }


        // Register the permission request callback for multiple permissions
        val requestPermissionsLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->


            val isReadMediaImagesPermissionGranted = permissions[readMediaImages]
            val isReadMediaVideoPermissionGranted = permissions[readMediaVideo]

            val isReadMediaPermissionGranted =
                isReadMediaImagesPermissionGranted == true && isReadMediaVideoPermissionGranted == true

            val isCameraPermissionGranted = permissions[accessCamera] == true


            // Update state based on the permissions result
            if (isReadMediaPermissionGranted && isCameraPermissionGranted
            ) {
                allRequestPermissionGranted = true

            } else {
                allRequestPermissionGranted = false
                isCameraPermissionRequestRationale =
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        context as Activity,
                        accessCamera
                    )


                isReadImagesPermissionRequestRationale =
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        context,
                        readMediaImages
                    )
                isReadVideoPermissionRequestRationale =
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        context,
                        readMediaVideo
                    )

                if ((!isCameraPermissionRequestRationale && !isCameraPermissionGranted)
                    && (!isReadImagesPermissionRequestRationale
                            && !isReadVideoPermissionRequestRationale
                            && !isReadMediaPermissionGranted)
                ) {
                    isShowingAllPermissionRequestDialogRationale = true
                } else if (!isCameraPermissionRequestRationale && !isCameraPermissionGranted) {
                    isShowingAccessCameraPermissionRequestDialogRationale = true

                } else if ((!isReadImagesPermissionRequestRationale
                            || !isReadVideoPermissionRequestRationale)
                    && !isReadMediaPermissionGranted
                ) {

                    isShowingReadMediaPermissionRequestDialogRationale = true
                } else {
                    if (!isReadMediaPermissionGranted && !isCameraPermissionGranted) {
                        isShowingAllPermissionRequestDialog = true

                    } else if (!isCameraPermissionGranted) {
                        isShowingAccessCameraPermissionRequestDialog = true
                    } else {
                        isShowingReadMediaPermissionRequestDialog = true
                    }
                }
            }

        }


        val launchCameraPermission = {
            requestPermissionsLauncher.launch(
                arrayOf(accessCamera)
            )
        }


        val launchReadMediaPermission = {
            requestPermissionsLauncher.launch(
                arrayOf(readMediaImages, readMediaVideo)
            )
        }

        val launchPermissions = {
            requestPermissionsLauncher.launch(
                arrayOf(accessCamera, readMediaImages, readMediaVideo)
            )
        }

        LaunchedEffect(Unit) {
            launchPermissions()
        }

        LifecycleResumeEffect(Unit) {
            if (!allRequestPermissionGranted) {
                if (hasReadMediaImagesPermissionGranted()
                    && hasReadMediaVideoPermissionGranted()
                    && hasAccessCameraPermissionGranted()
                ) {
                    allRequestPermissionGranted = true
                }
            }
            onPauseOrDispose {}
        }

        LaunchedEffect(allRequestPermissionGranted) {
            onPermissionResult(allRequestPermissionGranted)
        }

        if (isShowingAllPermissionRequestDialog) {

            MultiplePermissionsRationaleRequestDialog(
                Icons.Filled.CameraAlt,
                Icons.Filled.Photo,
                "Camera & Photo and Video",
                "To take photos or video camera & Photo and Video permission is required",
                {
                    launchPermissions()
                    isShowingAllPermissionRequestDialog = false
                }, {
                    onDismissRequest()
                },
                dismissOnClickOutside = false
            )

        }

        if (isShowingAllPermissionRequestDialogRationale) {

            MultiplePermissionsRationaleRequestDialog(
                Icons.Filled.CameraAlt,
                Icons.Filled.Photo,
                "Camera & Photo and Video",
                "To take photos or video camera & Photo and Video permission is required",
                {
                    onDismissRequest()
                    redirectToAppSettings(context)
                }, {
                    onDismissRequest()
                },
                dismissOnClickOutside = false
            )
        }

        if (isShowingReadMediaPermissionRequestDialog) {
            PermissionRequestDialog(
                Icons.Filled.Photo,
                "Photos and Video",
                "To take photos or video photos and video permission is required",
                {
                    launchReadMediaPermission()
                    isShowingReadMediaPermissionRequestDialog = false
                }, {
                    onDismissRequest()
                }, dismissButtonEnabled = false,
                dismissOnClickOutside = false
            )
        }


        if (isShowingAccessCameraPermissionRequestDialog) {

            PermissionRequestDialog(
                Icons.Filled.Camera,
                "Camera",
                "To take photos camera permission is required",
                {
                    launchCameraPermission()
                    isShowingAccessCameraPermissionRequestDialog = false
                }, {
                    onDismissRequest()
                }, dismissButtonEnabled = false,
                dismissOnClickOutside = false
            )
        }

        if (isShowingAccessCameraPermissionRequestDialogRationale) {

            PermissionRationaleRequestDialog(
                Icons.Filled.Camera,
                "Camera",
                "To take photos camera permission is required",
                {
                    onDismissRequest()
                    redirectToAppSettings(context)
                }, {
                    onDismissRequest()
                },
                dismissOnClickOutside = false
            )
        }
    }

}


@Composable
fun MicAccess(onDismissRequest: () -> Unit = {}, onPermissionResult: (Boolean) -> Unit) {

    val context = LocalContext.current

    var showRationale by remember {
        mutableStateOf(
            ActivityCompat.shouldShowRequestPermissionRationale(
                context as Activity,
                android.Manifest.permission.RECORD_AUDIO
            )
        )
    }


    val hasPermissionGranted: () -> Boolean = {
        ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    // State to handle permission request
    var permissionsGranted by remember {
        mutableStateOf(
            hasPermissionGranted()
        )
    }


    var isShowingDialogRationale by rememberSaveable { mutableStateOf(false) }
    var isShowingPermissionRequestDialog by rememberSaveable { mutableStateOf(false) }


    // Register the permission request callback for multiple permissions
    val requestPermissions = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isPermissionGranted ->

        // Update state based on the permissions result
        if (isPermissionGranted) {
            permissionsGranted = true
        } else {
            permissionsGranted = false
            showRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                context as Activity,
                android.Manifest.permission.RECORD_AUDIO
            )
            if (!showRationale) {
                isShowingDialogRationale = true
            } else {
                isShowingPermissionRequestDialog = true
            }
            // Optionally, show rationale or notify the user why these permissions are required
        }
    }


    val launchPermission = {
        requestPermissions.launch(
            android.Manifest.permission.RECORD_AUDIO
        )
    }

    LaunchedEffect(Unit) {
        launchPermission()
    }

    LifecycleResumeEffect(Unit) {
        if (!permissionsGranted) {
            if (hasPermissionGranted()) {
                permissionsGranted = true
            }
        }
        onPauseOrDispose {}
    }

    LaunchedEffect(permissionsGranted) {
        onPermissionResult(permissionsGranted)
    }

    if (isShowingPermissionRequestDialog) {

        PermissionRequestDialog(
            Icons.Filled.Mic,
            "Mic",
            "To record voice mic permission is required",
            {
                launchPermission()
                isShowingPermissionRequestDialog = false
            },
            {
                isShowingPermissionRequestDialog = false
                onDismissRequest()
            }, dismissButtonEnabled = true
        )
    }

    if (isShowingDialogRationale) {
        PermissionRationaleRequestDialog(
            Icons.Filled.Mic,
            "Mic",
            "To record voice mic permission is required",
            {
                onDismissRequest()
                redirectToAppSettings(context)
            },
            {
                isShowingPermissionRequestDialog = false
                onDismissRequest()
            },
            dismissButtonEnabled = true
        )

    }
}


