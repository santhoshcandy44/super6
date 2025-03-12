package com.lts360.test

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.camera.core.Camera
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.lts360.components.utils.LogUtils.TAG
import com.lts360.compose.ui.common.CircularProgressIndicatorLegacy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs


data class QRCodeData(
    val bounds: Rect? = null,
    val value: String? = null,
    val type: Int = -1,
    val isDetected: Boolean = false
)


fun vibrateDevice(context: Context) {
    // For API level 31 (Android 12) and above, use the VibratorManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager =
            context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        val vibrator = vibratorManager.defaultVibrator
        // Use VibrationEffect to make the device vibrate
        vibrator.vibrate(
            VibrationEffect.createOneShot(
                300,
                VibrationEffect.DEFAULT_AMPLITUDE
            )
        ) // Vibrate for 500ms
    } else {
        // For older Android versions, use the legacy Vibrator API
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    300,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            ) // Vibrate for 500ms
        } else {
            vibrator.vibrate(300) // For older devices, vibrate for 500ms (API level below 26)
        }
    }
}


@OptIn(ExperimentalGetImage::class)
@Composable
fun QRScannerCameraScreen() {
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var cameraSelector by remember { mutableStateOf(CameraSelector.DEFAULT_BACK_CAMERA) }
    var previewUseCase by remember { mutableStateOf<androidx.camera.core.Preview?>(null) }
    var imageCaptureUseCase by remember { mutableStateOf<ImageCapture?>(null) }

    val context = LocalContext.current

    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) } // For storing detected QR code bounds
    var qrCodeData by remember { mutableStateOf<QRCodeData?>(null) }
    var isQrDetected by remember { mutableStateOf(false) }

    var camera by remember { mutableStateOf<Camera?>(null) }


    fun startCapture() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProvider = cameraProviderFuture.get()

        // Create Preview use case
        previewUseCase = androidx.camera.core.Preview.Builder().build()

        // Create ImageCapture use case
        imageCaptureUseCase = ImageCapture.Builder().build()


        // Create ImageAnalysis use case for QR code detection
        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(context)) { ip ->
            processImageForQrCode(ip) { qrData, bitmap ->
                qrData?.let {
                    qrCodeData = it
                }
                imageBitmap = bitmap
            }
        }


        // Bind the use cases to the camera lifecycle
        camera = cameraProvider?.bindToLifecycle(
            ProcessLifecycleOwner.get(),
            cameraSelector,
            previewUseCase,
            imageCaptureUseCase,
            imageAnalysis
        )

    }

    // Function to stop the capture (unbind camera use cases)
    fun stopCapture() {
        // Unbind all use cases, which stops capturing
        cameraProvider?.unbindAll()
        camera = null
        // Release the camera provider if no longer needed
        cameraProvider = null
    }


    // LaunchedEffect for QR value detection and stopping capture
    LaunchedEffect(qrCodeData) {
        qrCodeData?.takeIf { !isQrDetected }?.let { nonNullQrCodeData ->
            isQrDetected = nonNullQrCodeData.isDetected

            val isDetected = nonNullQrCodeData.isDetected
            if (isDetected) {
                nonNullQrCodeData.value?.let {

                    stopCapture()  // Stop the camera capture
                    vibrateDevice(context)  // Vibrate when QR code is detected

                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                    }

                    when (nonNullQrCodeData.type) {
                        Barcode.TYPE_URL -> {
                            // Handle URL QR code
                            Log.d("QR Code", "Detected URL: $it")

                        }

                        Barcode.TYPE_TEXT -> {
                            // Handle text QR code
                            Log.d("QR Code", "Detected Text: $it")
                        }

                        Barcode.TYPE_CONTACT_INFO -> {
                            // Handle contact info (vCard)
                            Log.d("QR Code", "Detected Contact Info: $it")
                        }

                        -1 -> {

                        }

                        else -> {
                            Log.d("QR Code", "Detected unknown type: $it")
                        }
                    }


                }


            }
        }
    }


    // Setup CameraX provider
    LaunchedEffect(true) {
        startCapture()
    }

    // Camera preview and UI controls
    Box(modifier = Modifier.fillMaxSize()) {
        CameraPreview(previewUseCase = previewUseCase)
        camera?.let {
            TorchButton(it.cameraControl)
        }
        QRCodeZoomEffectOverlay(
            modifier = Modifier.zIndex(1f),
            qrCodeData?.isDetected == true
        ) // Add the overlay box

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .background(Color.Black.copy(0.5f), RoundedCornerShape(8.dp))
                .align(Alignment.BottomCenter),
            contentAlignment = Alignment.Center
        ) {

            Text(
                "Place code inside the box. Tap here to help.",
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}


// QR Code Detection
@OptIn(ExperimentalGetImage::class)
fun processImageForQrCode(
    imageProxy: ImageProxy,
    onQrCodeDetected: (QRCodeData?, Bitmap?) -> Unit
) {
    imageProxy.image?.let { image ->
        val scanner = BarcodeScanning.getClient()
        val inputImage = InputImage.fromMediaImage(image, imageProxy.imageInfo.rotationDegrees)
        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                if (barcodes.isNotEmpty()) {
                    // Get the first barcode detected (you can adjust this if needed)
                    val barcode = barcodes[0]
                    val boundingBox = barcode.boundingBox // This is the bounding box of the QR code
                    val value = barcode.rawValue // This is the bounding box of the QR code
                    val type = barcode.valueType // This is the bounding box of the QR code

                    onQrCodeDetected(
                        QRCodeData(boundingBox, value, type, true),
                        imageProxy.toBitmap()
                    )
                } else {
                    onQrCodeDetected(null, null) // No QR code detected
                }




                imageProxy.close() // Don't forget to close the image proxy
            }
            .addOnFailureListener {
                imageProxy.close() // Don't forget to close the image proxy on error
            }
    }
}


@Composable
fun CameraPreview(
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
fun QRCodeDividerRectGradientAnimOverlay(modifier: Modifier = Modifier) {

    val infiniteTransition = rememberInfiniteTransition()

    // Animate vertical movement of the scanning line
    val dividerY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    Canvas(
        modifier = modifier
            .fillMaxSize()

    ) {

        val padding = 32.dp.toPx() // Padding around the scanning area

        val width = size.width * 0.7f
        val height = size.height * 0.35f

        val rectLeft = (size.width - width) / 2
        val rectTop = (size.height - height) / 2

        val left = ((size.width + padding) - width) / 2
        val top = ((size.height + padding) - height) / 2

        val lineContainerWidth = width - padding
        val lineContainerHeight = height - padding

        val lineLength = 40.dp.toPx()
        val strokeWidth = 6.dp.toPx()  // Border thickness

        // Draw transparent overlay
        drawRect(
            color = Color.Black.copy(alpha = 0.5f),
            size = size
        )

        // Crop out the scanning area (clear rectangle)
        drawRect(
            color = Color.Transparent,
            topLeft = Offset(rectLeft, rectTop),
            size = Size(width, height),
            blendMode = BlendMode.Clear
        )

        // Draw corner lines (expanded)
        val cornerColor = Color.White

        val strokeCap = StrokeCap.Round // 🔥Use round stroke cap


        // Divider moving position (inside the scanning area)
        val dividerTop = top + (lineContainerHeight * dividerY)


        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent, // Fully transparent at the top
                    Color.Cyan.copy(alpha = 0.3f) // Cyan at the bottom with 30% opacity
                ),
                startY = top,
                endY = dividerTop
            ),
            topLeft = Offset(left, top),
            size = Size(lineContainerWidth, lineContainerHeight * dividerY)
        )

        drawLine(
            Color.Cyan,
            Offset(left, dividerTop),
            Offset(left + lineContainerWidth, dividerTop),
            2.dp.toPx(),
            strokeCap
        )

        // Top-left corner
        drawLine(
            cornerColor,
            Offset(left, top),
            Offset(left + lineLength, top),
            strokeWidth,
            strokeCap
        )

        drawLine(
            cornerColor,
            Offset(left, top),
            Offset(left, top + lineLength),
            strokeWidth,
            strokeCap
        )

        // Top-right corner
        drawLine(
            cornerColor,
            Offset(left + lineContainerWidth, top),
            Offset(left + lineContainerWidth - lineLength, top),
            strokeWidth,
            strokeCap
        )
        drawLine(
            cornerColor,
            Offset(left + lineContainerWidth, top),
            Offset(left + lineContainerWidth, top + lineLength),
            strokeWidth,
            strokeCap
        )

        // Bottom-left corner
        drawLine(
            cornerColor,
            Offset(left, top + lineContainerHeight),
            Offset(left, top + lineContainerHeight - lineLength),
            strokeWidth,
            strokeCap
        )
        drawLine(
            cornerColor,
            Offset(left, top + lineContainerHeight),
            Offset(left + lineLength, top + lineContainerHeight),
            strokeWidth,
            strokeCap
        )

        // Bottom-right corner
        drawLine(
            cornerColor,
            Offset(left + lineContainerWidth, top + lineContainerHeight),
            Offset(left + lineContainerWidth, top + lineContainerHeight - lineLength),
            strokeWidth,
            strokeCap
        )
        drawLine(
            cornerColor,
            Offset(left + lineContainerWidth, top + lineContainerHeight),
            Offset(left + lineContainerWidth - lineLength, top + lineContainerHeight),
            strokeWidth,
            strokeCap
        )
    }
}


@Composable
fun QRCodeDividerAnimOverlay(modifier: Modifier = Modifier) {

    val infiniteTransition = rememberInfiniteTransition()

    // Animate vertical movement of the scanning line
    val dividerY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    Canvas(
        modifier = modifier
            .fillMaxSize()

    ) {

        val padding = 32.dp.toPx() // Padding around the scanning area

        val width = size.width * 0.7f
        val height = size.height * 0.35f

        val rectLeft = (size.width - width) / 2
        val rectTop = (size.height - height) / 2

        val left = ((size.width + padding) - width) / 2
        val top = ((size.height + padding) - height) / 2

        val lineContainerWidth = width - padding
        val lineContainerHeight = height - padding

        val lineLength = 40.dp.toPx()
        val strokeWidth = 6.dp.toPx()  // Border thickness

        // Draw transparent overlay
        drawRect(
            color = Color.Black.copy(alpha = 0.5f),
            size = size
        )

        // Crop out the scanning area (clear rectangle)
        drawRect(
            color = Color.Transparent,
            topLeft = Offset(rectLeft, rectTop),
            size = Size(width, height),
            blendMode = BlendMode.Clear
        )

        // Draw corner lines (expanded)
        val cornerColor = Color.White

        val strokeCap = StrokeCap.Round // 🔥Use round stroke cap


        val movingHorizontalPadding = 16.dp.toPx() // Padding around the scanning area

        // Divider moving position (inside the scanning area)
        val dividerTop =
            top + movingHorizontalPadding + lineLength + ((lineContainerHeight - ((lineLength * 2) + (movingHorizontalPadding * 2))) * dividerY)

        val movingHorizontalStrokeWidth = 3.dp.toPx()  // Border thickness


        // 🔥 Moving horizontal divider line
        drawLine(
            color = Color.Cyan,
            start = Offset(left, dividerTop),
            end = Offset(left + lineContainerWidth, dividerTop),
            strokeWidth = movingHorizontalStrokeWidth,
            cap = strokeCap
        )


        // Top-left corner
        drawLine(
            cornerColor,
            Offset(left, top),
            Offset(left + lineLength, top),
            strokeWidth,
            strokeCap
        )

        drawLine(
            cornerColor,
            Offset(left, top),
            Offset(left, top + lineLength),
            strokeWidth,
            strokeCap
        )

        // Top-right corner
        drawLine(
            cornerColor,
            Offset(left + lineContainerWidth, top),
            Offset(left + lineContainerWidth - lineLength, top),
            strokeWidth,
            strokeCap
        )
        drawLine(
            cornerColor,
            Offset(left + lineContainerWidth, top),
            Offset(left + lineContainerWidth, top + lineLength),
            strokeWidth,
            strokeCap
        )

        // Bottom-left corner
        drawLine(
            cornerColor,
            Offset(left, top + lineContainerHeight),
            Offset(left, top + lineContainerHeight - lineLength),
            strokeWidth,
            strokeCap
        )
        drawLine(
            cornerColor,
            Offset(left, top + lineContainerHeight),
            Offset(left + lineLength, top + lineContainerHeight),
            strokeWidth,
            strokeCap
        )

        // Bottom-right corner
        drawLine(
            cornerColor,
            Offset(left + lineContainerWidth, top + lineContainerHeight),
            Offset(left + lineContainerWidth, top + lineContainerHeight - lineLength),
            strokeWidth,
            strokeCap
        )
        drawLine(
            cornerColor,
            Offset(left + lineContainerWidth, top + lineContainerHeight),
            Offset(left + lineContainerWidth - lineLength, top + lineContainerHeight),
            strokeWidth,
            strokeCap
        )
    }
}


@Composable
fun QRCodeZoomEffectOverlay(modifier: Modifier = Modifier, stopAnimation: Boolean) {

    val scaleInfiniteTransition = rememberInfiniteTransition()

// 🔥 Animate both scale factor and dp-based expansion together
    val scalePair by scaleInfiniteTransition.animateValue(
        initialValue = Pair(0.95f, 0.dp.toPx()),  // Shrinks slightly, no extra padding initially
        targetValue = Pair(1.05f, 30.dp.toPx()),  // Expands slightly, increases padding
        typeConverter = TwoWayConverter(
            convertToVector = { pair -> AnimationVector2D(pair.first, pair.second) },
            convertFromVector = { vector -> Pair(vector.v1, vector.v2) }
        ),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing), // Smooth animation
            repeatMode = RepeatMode.Reverse // Auto-reverse for infinite effect
        ), label = ""
    )

    // Extract values
    val scale: Float
    val scaleDp: Float

    if (stopAnimation) {
        // Set default value when QR code is detected
        scale = 1f
        scaleDp = 0f
    } else {
        // Use animated values when QR code is not detected
        scale = scalePair.first
        scaleDp = scalePair.second
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()

    ) {

        val padding = 32.dp.toPx() // Padding around the scanning area

        val width = size.width * 0.7f * scale
        val height = size.height * 0.35f * scale

        val rectLeft = (size.width - width) / 2
        val rectTop = (size.height - height) / 2

        val left = ((size.width + padding) - width) / 2
        val top = ((size.height + padding) - height) / 2

        val lineContainerWidth = width - padding
        val lineContainerHeight = height - padding

        val lineLength = 40.dp.toPx() + scaleDp // Small extended corner lines
        val strokeWidth = 6.dp.toPx()  // Border thickness

        // Draw transparent overlay
        drawRect(
            color = Color.Black.copy(alpha = 0.5f),
            size = size
        )

        // Crop out the scanning area (clear rectangle)
        drawRect(
            color = Color.Transparent,
            topLeft = Offset(rectLeft, rectTop),
            size = Size(width, height),
            blendMode = BlendMode.Clear
        )

        // Draw corner lines (expanded)
        val cornerColor = Color.White

        val strokeCap = StrokeCap.Round // 🔥Use round stroke cap


        // Top-left corner
        drawLine(
            cornerColor,
            Offset(left, top),
            Offset(left + lineLength, top),
            strokeWidth,
            strokeCap
        )

        drawLine(
            cornerColor,
            Offset(left, top),
            Offset(left, top + lineLength),
            strokeWidth,
            strokeCap
        )

        // Top-right corner
        drawLine(
            cornerColor,
            Offset(left + lineContainerWidth, top),
            Offset(left + lineContainerWidth - lineLength, top),
            strokeWidth,
            strokeCap
        )
        drawLine(
            cornerColor,
            Offset(left + lineContainerWidth, top),
            Offset(left + lineContainerWidth, top + lineLength),
            strokeWidth,
            strokeCap
        )

        // Bottom-left corner
        drawLine(
            cornerColor,
            Offset(left, top + lineContainerHeight),
            Offset(left, top + lineContainerHeight - lineLength),
            strokeWidth,
            strokeCap
        )
        drawLine(
            cornerColor,
            Offset(left, top + lineContainerHeight),
            Offset(left + lineLength, top + lineContainerHeight),
            strokeWidth,
            strokeCap
        )

        // Bottom-right corner
        drawLine(
            cornerColor,
            Offset(left + lineContainerWidth, top + lineContainerHeight),
            Offset(left + lineContainerWidth, top + lineContainerHeight - lineLength),
            strokeWidth,
            strokeCap
        )
        drawLine(
            cornerColor,
            Offset(left + lineContainerWidth, top + lineContainerHeight),
            Offset(left + lineContainerWidth - lineLength, top + lineContainerHeight),
            strokeWidth,
            strokeCap
        )
    }
}


@Composable
fun BoxScope.TorchButton(cameraControl: CameraControl) {
    var flashEnabled by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    IconButton(
        onClick = {
            if (flashEnabled) {
                cameraControl.enableTorch(false)
            } else {
                cameraControl.enableTorch(true)
            }

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
