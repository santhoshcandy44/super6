package com.lts360.test


import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Pause
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import coil3.compose.AsyncImage
import com.lts360.R
import com.lts360.compose.ui.common.CircularProgressIndicatorLegacy
import com.lts360.compose.ui.enterFullScreenMode
import com.lts360.compose.ui.theme.AppTheme
import com.lts360.compose.ui.utils.FormatterUtils.formatTimeSeconds
import com.lts360.compose.utils.SafeDrawingBox
import com.lts360.libs.camera.ui.CameraPermissionRationaleDialog
import com.lts360.libs.camera.ui.CameraPermissionRequestDialog
import com.lts360.libs.camera.ui.CameraPreview
import com.lts360.libs.camera.ui.TorchButton
import com.lts360.libs.imagepicker.utils.redirectToAppSettings
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
    @kotlin.OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        enterFullScreenMode(this, enableSwipeUp = false)
        super.onCreate(savedInstanceState)

        setContent {

            AppTheme {
                Surface {
                    val cameraViewModel: ChatCameraXViewModel = hiltViewModel()
                    val capturedUri by cameraViewModel.lastCaptureUri.collectAsState()

                    capturedUri?.let {

                        SafeDrawingBox {
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
                                            Icon(Icons.Filled.Check, contentDescription = null)
                                        }
                                    }
                                }
                            }
                        }


                    } ?: run {
                        SafeDrawingBox(isFullScreenMode = true) {
                            CameraXApp(cameraViewModel)
                        }
                    }
                }
            }

        }
    }
}


@OptIn(ExperimentalGetImage::class)
@Composable
fun CameraXApp(cameraViewModel: ChatCameraXViewModel) {


    val context = LocalContext.current

    val isPermissionGranted by cameraViewModel.isPermissionGranted.collectAsState()
    val cameraSelector by cameraViewModel.cameraSelector.collectAsState()
    val surfaceRequest by cameraViewModel.surfaceRequest.collectAsState()
    val imageCaptureUseCase by cameraViewModel.imageCaptureUseCase.collectAsState()
    val lastCaptureUri by cameraViewModel.lastCaptureUri.collectAsState()
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


    LaunchedEffect(cameraSelector) {
        if (currentItem == 0) {

            val useCase = ImageCapture.Builder().build()
            cameraViewModel.updateImagePreviewUseCase(useCase)
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


    // Camera preview and UI controls
    Box(modifier = Modifier.fillMaxSize()) {

        if (!isPermissionGranted) {
            CameraAccess {
                cameraViewModel.updatePermission(it)
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
                                } else {
                                    it.enableTorch(true)
                                }

                                cameraViewModel.updateIsFlashEnabled(!flashEnabled)
                                // Toggle torch/flash state (CameraX flash support needs to be implemented)
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
                    isPermissionGranted,
                    isTakingPhoto,
                    lastCaptureUri,
                    {
                        cameraViewModel.updateCurrentItem(it)
                    },
                    {

                        imageCaptureUseCase?.let {
                            if (!isTakingPhoto) {
                                cameraViewModel.updateIsTakingPhoto(true)
                                cameraViewModel.playShutter()
                                it.takePicture(
                                    ContextCompat.getMainExecutor(context),
                                    object :
                                        ImageCapture.OnImageCapturedCallback() {
                                        override fun onCaptureSuccess(image: ImageProxy) {
                                            super.onCaptureSuccess(image)


                                            val resolver = context.contentResolver

                                            val imageUri = resolver.insert(
                                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                                ContentValues().apply {
                                                    put(
                                                        MediaStore.Images.Media.DISPLAY_NAME,
                                                        "IMG_${System.currentTimeMillis()}.jpg"
                                                    )
                                                    put(
                                                        MediaStore.Images.Media.MIME_TYPE,
                                                        "image/jpeg"
                                                    )
                                                    put(
                                                        MediaStore.Images.Media.RELATIVE_PATH,
                                                        Environment.DIRECTORY_DCIM + "/${

                                                            context.getString(R.string.app_name)
                                                        }"
                                                    )
                                                }
                                            )

                                            if (imageUri == null) {
                                                cameraViewModel.updateIsTakingPhoto(false)
                                                return
                                            }


                                            val capturedBitmap =
                                                image.toBitmap()

                                            saveImageToDCIMFolder(
                                                context,
                                                capturedBitmap,
                                                imageUri,
                                                {
                                                    cameraViewModel.updateLastCapturedUri(imageUri)
                                                    cameraViewModel.updateIsTakingPhoto(false)
                                                }) {
                                                cameraViewModel.updateIsTakingPhoto(false)
                                            }

                                        }

                                        override fun onError(exception: ImageCaptureException) {
                                            super.onError(exception)
                                            Toast.makeText(
                                                context,
                                                "Error capturing photo",
                                                Toast.LENGTH_SHORT
                                            )
                                                .show()
                                            cameraViewModel.updateIsTakingPhoto(false)

                                        }
                                    }
                                )
                            }
                        }

                    },
                    {

                        cameraViewModel.startCapture(videoCaptureUseCase)
                        cameraViewModel.prepareVideoRecording {

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
    lastCaptureUri: Uri?,
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
                                lastCaptureUri?.let {
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
                                        .clickable {
                                            onTakePicture()
                                        },
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
fun CameraAccess(onDismissRequest: () -> Unit = {}, onPermissionResult: (Boolean) -> Unit) {

    val context = LocalContext.current

    var showRationale by remember {
        mutableStateOf(
            ActivityCompat.shouldShowRequestPermissionRationale(
                context as Activity,
                android.Manifest.permission.CAMERA
            )
        )
    }


    val hasPermissionGranted: () -> Boolean = {
        ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.CAMERA
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
                android.Manifest.permission.CAMERA
            )
            if (!showRationale) {
                isShowingDialogRationale = true
            } else {
                isShowingPermissionRequestDialog = true
            }
            // Optionally, show rationale or notify the user why these permissions are required
        }
    }


    val launchCameraPermission = {
        requestPermissions.launch(
            android.Manifest.permission.CAMERA
        )
    }

    LaunchedEffect(Unit) {
        launchCameraPermission()
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

        CameraPermissionRequestDialog({
            isShowingPermissionRequestDialog = false

        }, {
            onDismissRequest()
            isShowingPermissionRequestDialog = false
        })
    }

    if (isShowingDialogRationale) {

        CameraPermissionRationaleDialog({
            onDismissRequest()
            isShowingDialogRationale = false
            redirectToAppSettings(context)
        }, {
            onDismissRequest()
            isShowingDialogRationale = false
        })
    }
}


private fun saveImageToDCIMFolder(
    context: Context,
    bitmap: Bitmap,
    out: Uri,
    onSuccess: () -> Unit,
    onError: () -> Unit
) {

    try {
        val resolver = context.contentResolver
        resolver.openOutputStream(out)?.use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        }
        Toast.makeText(context, "Photo saved", Toast.LENGTH_SHORT).show()
        onSuccess()
    } catch (e: Exception) {
        Toast.makeText(context, "Failed to save photo", Toast.LENGTH_SHORT).show()
        onError()
    }

}

