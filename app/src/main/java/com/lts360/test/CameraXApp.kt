package com.lts360.test

import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ProcessLifecycleOwner
import com.lts360.compose.ui.common.CircularProgressIndicatorLegacy
import kotlinx.coroutines.launch


@Composable
fun CameraXApp() {
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var cameraSelector by remember { mutableStateOf(CameraSelector.DEFAULT_BACK_CAMERA) }
    var previewUseCase by remember { mutableStateOf<androidx.camera.core.Preview?>(null) }
    var imageCaptureUseCase by remember { mutableStateOf<ImageCapture?>(null) }

    val context = LocalContext.current

    // Setup CameraX provider
    LaunchedEffect(true) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProvider = cameraProviderFuture.get()

        // Create Preview use case
        previewUseCase = androidx.camera.core.Preview.Builder().build()

        // Create ImageCapture use case
        imageCaptureUseCase = ImageCapture.Builder().build()

        // Bind the use cases to the camera lifecycle
        cameraProvider?.bindToLifecycle(
            ProcessLifecycleOwner.get(),
            cameraSelector,
            previewUseCase,
            imageCaptureUseCase
        )
    }

    // Camera preview and UI controls
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        CameraPreview(cameraProvider = cameraProvider, previewUseCase = previewUseCase)
        TorchButton()
        ActionControls(imageCaptureUseCase = imageCaptureUseCase)
    }
}


@Composable
fun CameraPreview(
    cameraProvider: ProcessCameraProvider?,
    previewUseCase: androidx.camera.core.Preview?
) {
    val context = LocalContext.current
    val previewView = remember { PreviewView(context) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        previewUseCase?.surfaceProvider = previewView.surfaceProvider
    }
}



@Composable
fun BoxScope.TorchButton() {
    var flashEnabled by remember { mutableStateOf(false) }
    val context = LocalContext.current

    IconButton(
        onClick = {
            flashEnabled = !flashEnabled
            // Toggle torch/flash state (CameraX flash support needs to be implemented)
            Toast.makeText(
                context,
                "Flash: ${if (flashEnabled) "On" else "Off"}",
                Toast.LENGTH_SHORT
            ).show()
        },
        modifier = Modifier
            .padding(8.dp)
            .align(Alignment.TopStart)
    ) {
        Icon(
            imageVector = if (flashEnabled) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
            contentDescription = "Toggle Flash",
            tint = Color.White
        )
    }
}

@Composable
fun BoxScope.ActionControls(
    imageCaptureUseCase: ImageCapture?
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
            .padding(16.dp)
    ) {

        ScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            edgePadding = 0.dp,
            containerColor = Color.Transparent,
            divider = {},
            modifier = Modifier
                .fillMaxWidth()
                .offset(), // Here, we bind scrollState to the tab row
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                    height = 2.dp,
                    color = Color.Yellow
                )
            }

        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    selectedContentColor = Color.Yellow,
                    unselectedContentColor = Color.White,

                    onClick = {
                        // Launch a coroutine to animate the scroll to the position
                        scope.launch {
                            pagerState.animateScrollToPage(index)

                        }
                    },
                    text = {
                        Text(text = title)
                    }
                )
            }
        }


        // HorizontalPager for the swipeable content
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
                                                    object :
                                                        ImageCapture.OnImageCapturedCallback() {
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

                                IconButton({}) {
                                    Icon(Icons.Filled.Cameraswitch, contentDescription = null)
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
