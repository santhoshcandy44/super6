package com.lts360.test

import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.camera.core.Camera
import androidx.camera.core.CameraProvider
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ProcessLifecycleOwner
import com.lts360.components.utils.LogUtils.TAG
import com.lts360.compose.ui.common.CircularProgressIndicatorLegacy
import kotlin.math.abs


import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import com.lts360.R


fun saveImageToCameraFolder(context: Context, bitmap: Bitmap, onSuccess:()-> Unit, onError:()->Unit) {
    val filename = "IMG_${System.currentTimeMillis()}.jpg"

    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, filename)
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_DCIM + "/${context.getString(
            R.string.app_name)}")
    }

    val resolver = context.contentResolver
    val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

    imageUri?.let { uri ->
        resolver.openOutputStream(uri)?.use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        }
        Toast.makeText(context, "Photo saved", Toast.LENGTH_SHORT).show()
        onSuccess()
    } ?: run {
        Toast.makeText(context, "Failed to save photo", Toast.LENGTH_SHORT).show()
        onError()
    }

}

@OptIn(ExperimentalGetImage::class)
@Composable
fun CameraXApp() {
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var cameraSelector by remember { mutableStateOf(CameraSelector.DEFAULT_BACK_CAMERA) }
    var previewUseCase by remember { mutableStateOf<androidx.camera.core.Preview?>(null) }
    var imageCaptureUseCase by remember { mutableStateOf<ImageCapture?>(null) }

    val context = LocalContext.current

    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) } // For storing detected QR code bounds

    var camera by remember { mutableStateOf<Camera?>(null) }


    // Function to stop the capture (unbind camera use cases)
    fun stopCapture(processCameraProvider:ProcessCameraProvider) {
        // Unbind all use cases, which stops capturing
        processCameraProvider.unbindAll()
        previewUseCase = null
        imageCaptureUseCase = null
        camera = null
        // Release the camera provider if no longer needed
        cameraProvider = null
    }


    fun startCapture() {
        cameraProvider?.let {
            stopCapture(it)
        }
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProvider = cameraProviderFuture.get()

        // Create Preview use case
        previewUseCase = androidx.camera.core.Preview.Builder().build()
        // Create ImageCapture use case
        imageCaptureUseCase = ImageCapture.Builder().build()

        // Bind the use cases to the camera lifecycle
        camera = cameraProvider?.bindToLifecycle(
            ProcessLifecycleOwner.get(),
            cameraSelector,
            previewUseCase,
            imageCaptureUseCase
        )

    }


    // Setup CameraX provider
    LaunchedEffect(cameraSelector) {
        startCapture()
    }

    // Camera preview and UI controls
    Box(modifier = Modifier.fillMaxSize()) {
        CameraPreview(previewUseCase = previewUseCase)
        camera?.let {
            TorchButton(it.cameraControl)
        }

        ActionControls(imageCaptureUseCase){
            cameraSelector = if(cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA){
                CameraSelector.DEFAULT_BACK_CAMERA
            }else{
                CameraSelector.DEFAULT_FRONT_CAMERA
            }
        }
    }
}






@Composable
fun BoxScope.ActionControls(
    imageCaptureUseCase: ImageCapture?,
    onFlipCamera:()-> Unit,
) {
    val context = LocalContext.current
    var isTakingPhoto by remember { mutableStateOf(false) }


    // List of tabs
    val tabs = listOf("Photo", "Video", "Portrait", "Night Mode")

    // State for Pager and ScrollableTabRow
    val pagerState = rememberPagerState(pageCount = { tabs.size })

    // Get a coroutine scope to launch scrolling actions
    val scope = rememberCoroutineScope()



    Column(
        modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
    ) {


        val localConfiguration = LocalConfiguration.current

        val density = LocalDensity.current

        val lazyRowState = rememberLazyListState()

        var itemOffsetToCenter by remember { mutableIntStateOf(0) }
        var selectedIndex by remember { mutableIntStateOf(-1) }

        val centerX = with(density) { localConfiguration.screenWidthDp.dp.toPx() } / 2  // Convert dp to px properly

        var contentPadding by remember { mutableStateOf(0.dp) }

        // Capture scroll position to manually adjust the center
        LaunchedEffect(lazyRowState) {
            snapshotFlow { lazyRowState.layoutInfo.visibleItemsInfo }
                .collect { visibleItems ->

                    val startPadding = with(density) { contentPadding.toPx() }  // Convert to px

                    val closestItem = visibleItems.minByOrNull { item ->
                        abs((item.offset + startPadding) + (item.size / 2) - centerX).toInt()
                    }

                    closestItem?.let {
                        localConfiguration.screenWidthDp.dp
                        itemOffsetToCenter =  it.size
                        contentPadding = (localConfiguration.screenWidthDp.dp - with(density){ itemOffsetToCenter.toDp() }) / 2
                        selectedIndex = it.index
                    }
                }
        }



        LazyRow(
            state = lazyRowState,
            flingBehavior = rememberSnapFlingBehavior(lazyRowState),
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp), // Centers items
            contentPadding = PaddingValues(
                horizontal =  contentPadding,
                vertical = 16.dp
            )
        ) {

            itemsIndexed(tabs) { index, item ->
                Box(modifier = Modifier
                    .wrapContentSize()
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
                        horizontalArrangement = Arrangement.Center,
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
                            ) {

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
                                            if (!isTakingPhoto) {
                                                isTakingPhoto = true

                                                imageCaptureUseCase?.takePicture(
                                                    ContextCompat.getMainExecutor(context),
                                                    object : ImageCapture.OnImageCapturedCallback() {
                                                        override fun onCaptureSuccess(image: ImageProxy) {
                                                            super.onCaptureSuccess(image)

                                                            val capturedBitmap = image.toBitmap()

                                                            saveImageToCameraFolder(context, capturedBitmap,{
                                                                isTakingPhoto = false
                                                            }){
                                                                isTakingPhoto = false
                                                            }

                                                        }

                                                        override fun onError(exception: ImageCaptureException) {
                                                            super.onError(exception)
                                                            Toast.makeText(context, "Error capturing photo", Toast.LENGTH_SHORT)
                                                                .show()
                                                            isTakingPhoto = false
                                                        }
                                                    }
                                                )
                                            }
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


                        Box(
                            modifier = Modifier.weight(1f),
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
                                    .padding(8.dp)
                            ) {

                                IconButton(onFlipCamera) {
                                    Icon(Icons.Filled.Cameraswitch, contentDescription = null, tint = Color.White)
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
                        ) {
                            // Circular Button to capture photo with a white-filled circle and border gap
                            Box(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(8.dp)
                                    .size(70.dp) // Size of the circle
                                    .background(
                                        color = Color.Red,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        if (!isTakingPhoto) {
                                            isTakingPhoto = true
                                            imageCaptureUseCase?.takePicture(
                                                ContextCompat.getMainExecutor(context),
                                                object : ImageCapture.OnImageCapturedCallback() {
                                                    override fun onCaptureSuccess(image: ImageProxy) {
                                                        super.onCaptureSuccess(image)
                                                        Toast
                                                            .makeText(
                                                                context,
                                                                "Photo captured",
                                                                Toast.LENGTH_SHORT
                                                            )
                                                            .show()
                                                        isTakingPhoto = false
                                                    }

                                                    override fun onError(exception: ImageCaptureException) {
                                                        super.onError(exception)
                                                        Toast
                                                            .makeText(
                                                                context,
                                                                "Error capturing photo",
                                                                Toast.LENGTH_SHORT
                                                            )
                                                            .show()
                                                        isTakingPhoto = false
                                                    }
                                                }
                                            )
                                        }
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
        }

    }


}
