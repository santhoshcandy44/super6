package com.lts360.libs.imagecrop

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Rotate90DegreesCw
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.lts360.test.ClockWiseCircularProgressBar
import com.lts360.libs.ui.ScaleView
import kotlin.math.abs
import kotlin.math.min


@Composable
fun CropScreen(uri: Uri, croppedResult:(Bitmap?)->Unit) {


    var imageWidth by remember { mutableStateOf(0.dp) }
    var imageHeight by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current
    val context = LocalContext.current

    var inputBitmap by remember { mutableStateOf<Bitmap?>(null) }

    var outputBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var cropRect by remember { mutableStateOf<Rect?>(null) }


    var rotationAngle by remember { mutableFloatStateOf(0f) }
    var flipX by remember { mutableFloatStateOf(0f) }


    var scaleValue by remember { mutableIntStateOf(50) }
    var sliderRotationAngle by remember { mutableFloatStateOf(0f) }
    var rotationProgress by remember { mutableIntStateOf(0) }




    LaunchedEffect(uri) {
        val inputBitmapStream = context.contentResolver.openInputStream(uri)
        inputBitmapStream?.use {
            inputBitmap = BitmapFactory.decodeStream(it)
        }
    }


    LaunchedEffect(scaleValue) {
        sliderRotationAngle = (((scaleValue / 100f) * 90) - 45)
    }

    LaunchedEffect(sliderRotationAngle) {
        rotationProgress = ((sliderRotationAngle / 45f) * 100f).toInt()

    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .wrapContentSize()
            ) {
                inputBitmap?.let { nonNullInputBitmap ->

                    val bitmapWidth = nonNullInputBitmap.width
                    val bitmapHeight = nonNullInputBitmap.height

                    val bitmapWidthDp = nonNullInputBitmap.width.dp
                    val bitmapHeightDp = nonNullInputBitmap.height.dp

                    if (bitmapWidth > 0 && bitmapHeight > 0) {

                        Box(modifier = Modifier.fillMaxSize()) {

                            val isPortraitRotation =
                                rotationAngle == 90f || rotationAngle == 270f

                            val aspectRatio = if (isPortraitRotation) bitmapHeightDp / bitmapWidthDp else bitmapWidthDp / bitmapHeightDp

                            BoxWithConstraints(
                                modifier = Modifier
                                    .aspectRatio(aspectRatio)
                                    .align(Alignment.Center)
                            ) {

                                imageWidth = this.maxWidth
                                imageHeight = this.maxHeight




                                Spacer(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clipToBounds()
                                        .drawBehind {


                                            inputBitmap?.let { bitmap ->
                                                val matrix = Matrix()

                                                val boxWidth = this.size.width
                                                val boxHeight = this.size.height


                                                // Adjust dimensions based on rotation
                                                val targetWidth =
                                                    if (isPortraitRotation) boxHeight else boxWidth
                                                val targetHeight =
                                                    if (isPortraitRotation) boxWidth else boxHeight

                                                // Maintain aspect ratio
                                                val scaleFactor = min(
                                                    targetWidth / bitmap.width.toFloat(),
                                                    targetHeight / bitmap.height.toFloat()
                                                )

                                                val scaledWidth =
                                                    bitmap.width * (targetWidth / bitmap.width.toFloat())
                                                val scaledHeight =
                                                    bitmap.height * (targetHeight / bitmap.height.toFloat())


                                                // Center the image within the box
                                                val translateX = (boxWidth - scaledWidth) / 2f
                                                val translateY = (boxHeight - scaledHeight) / 2f

                                                // Apply transformations

                                                matrix.postScale(scaleFactor, scaleFactor)
                                                matrix.postTranslate(translateX, translateY)
                                                matrix.postRotate(
                                                    rotationAngle,
                                                    boxWidth / 2f,
                                                    boxHeight / 2f
                                                )
                                                matrix.postRotate(
                                                    sliderRotationAngle,
                                                    boxWidth / 2f,
                                                    boxHeight / 2f
                                                )

                                                if (flipX == 180f) {
                                                    // Step 2: Apply the flip horizontally (mirroring along X-axis)
                                                    matrix.postScale(
                                                        -1f,
                                                        1f
                                                    ) // Flip horizontally

                                                    if (rotationAngle == 90f || rotationAngle == 270f) {
                                                        matrix.postTranslate(
                                                            scaledHeight,
                                                            0f
                                                        ) // Translate by height for landscape rotation
                                                    } else {
                                                        matrix.postTranslate(
                                                            scaledWidth,
                                                            0f
                                                        ) // Translate by width for portrait
                                                    }
                                                }



                                                drawIntoCanvas { canvas ->
                                                    val paint = Paint()

                                                    canvas.nativeCanvas.save()
                                                    canvas.nativeCanvas.concat(matrix)
                                                    canvas.nativeCanvas.drawColor(ContextCompat.getColor(
                                                        context, android.R.color.white
                                                    ))
                                                    canvas.nativeCanvas.drawBitmap(
                                                        bitmap,
                                                        0f,
                                                        0f,
                                                        paint
                                                    )
                                                    canvas.nativeCanvas.restore()
                                                }
                                            }
                                        }
                                )

                                CropView(
                                    cropRectSize = minOf(imageWidth, imageHeight),
                                    pWidth = imageWidth,
                                    pHeight = imageHeight,
                                    onCropChanged = {
                                        cropRect = it
                                    },
                                )
                            }

                        }


                    }
                }
            }
        }






        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = String.format("%2d°", sliderRotationAngle.toInt()),
                    color = Color.White
                )
                ClockWiseCircularProgressBar(
                    progress = abs(rotationProgress / 100f),
                    isClockWise = rotationProgress > 0
                )

            }


            Box(modifier = Modifier.fillMaxWidth()) {
                ScaleView(
                    items = (0..100).toList(),
                    onValueChanged = {
                        scaleValue = it
                    })
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                IconButton({
                    rotationAngle = (rotationAngle + 90f) % 360f
                }) {
                    Icon(
                        Icons.Default.Rotate90DegreesCw, contentDescription = null,
                        tint = Color.White
                    )
                }

                IconButton({
                    flipX = if (flipX == 0f) {
                        180f
                    } else {
                        0f
                    }
                }) {
                    Icon(
                        Icons.Default.Flip, contentDescription = null,
                        tint = Color.White
                    )
                }

            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Done",
                    color = Color.White,
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .clickable {
                            inputBitmap?.let {
                                val originalBitmap = it

                                cropRect?.let { nonNullCropRect ->


                                    val boxWidth = imageWidth.value
                                    val boxHeight = imageHeight.value

                                    val isPortraitRotation =
                                        rotationAngle == 90f || rotationAngle == 270f

                                    // Step 1: Create an off-screen Bitmap with the same size as the box
                                    val output = Bitmap.createBitmap(
                                        boxWidth.toInt(),
                                        boxHeight.toInt(),
                                        Bitmap.Config.ARGB_8888
                                    )

                                    // Step 2: Create a Canvas that will draw on the off-screen Bitmap
                                    val canvas = Canvas(output)
                                    // Step 3: Fill the canvas with a transparent background (Optional - You can use a custom color here)
                                    canvas.drawColor(
                                        ContextCompat.getColor(
                                            context,
                                            android.R.color.white
                                        )
                                    ) // Transparent background
                                    // Step 3: Create the transformation matrix
                                    val matrix = Matrix()

                                    // Adjust dimensions based on rotation and scaling
                                    val targetWidth =
                                        if (isPortraitRotation) boxHeight else boxWidth
                                    val targetHeight =
                                        if (isPortraitRotation) boxWidth else boxHeight

                                    val scaleFactor = min(
                                        targetWidth / originalBitmap.width.toFloat(),
                                        targetHeight / originalBitmap.height.toFloat()
                                    )
                                    val scaledWidth = originalBitmap.width * scaleFactor
                                    val scaledHeight = originalBitmap.height * scaleFactor

                                    // Center the image within the bounds
                                    val translateX = (boxWidth - scaledWidth) / 2f
                                    val translateY = (boxHeight - scaledHeight) / 2f

                                    // Apply transformations: scale, translate, rotate
                                    matrix.postScale(scaleFactor, scaleFactor)
                                    matrix.postTranslate(translateX, translateY)
                                    matrix.postRotate(
                                        rotationAngle,
                                        boxWidth / 2f,
                                        boxHeight / 2f
                                    )
                                    matrix.postRotate(
                                        sliderRotationAngle,
                                        boxWidth / 2f,
                                        boxHeight / 2f
                                    )

                                    // Flip the image if necessary
                                    if (flipX == 180f) {
                                        matrix.postScale(-1f, 1f)
                                        val translateAdjustment =
                                            if (rotationAngle == 90f || rotationAngle == 270f) {
                                                scaledHeight
                                            } else {
                                                scaledWidth
                                            }
                                        matrix.postTranslate(translateAdjustment, 0f)
                                    }

                                    // Step 4: Draw the transformed image onto the canvas
                                    val paint = Paint()
                                    canvas.save()
                                    canvas.concat(matrix)  // Apply transformations
                                    canvas.drawBitmap(
                                        originalBitmap,
                                        0f,
                                        0f,
                                        paint
                                    )  // Draw the input bitmap with transformations
                                    canvas.restore()


                                    // Create a rotated and flipped bitmap
                                    val canvasBitmap = output


                                    // Step 3: Now, crop the rotated and flipped bitmap
                                    // Compute the scale factors
                                    val scaleFactorX =
                                        canvasBitmap.width.toFloat() / with(density) { imageWidth.toPx() }
                                    val scaleFactorY =
                                        canvasBitmap.height.toFloat() / with(density) { imageHeight.toPx() }

                                    // Calculate the crop coordinates
                                    var cropX = (nonNullCropRect.left * scaleFactorX).toInt()
                                    var cropY = (nonNullCropRect.top * scaleFactorY).toInt()
                                    var cropWidth =
                                        (nonNullCropRect.width * scaleFactorX).toInt()
                                    var cropHeight =
                                        (nonNullCropRect.height * scaleFactorY).toInt()

                                    // Make sure the crop area is within the bounds of the rotated and flipped image
                                    val safeCropWidth =
                                        cropWidth.coerceAtMost(canvasBitmap.width - cropX)
                                    val safeCropHeight =
                                        cropHeight.coerceAtMost(canvasBitmap.height - cropY)

                                    // Step 4: Create the final cropped bitmap from the rotated and flipped image
                                    val croppedBitmap = Bitmap.createBitmap(
                                        canvasBitmap,
                                        cropX,
                                        cropY,
                                        safeCropWidth,
                                        safeCropHeight
                                    )

                                    // Set the final cropped bitmap
                                    outputBitmap = croppedBitmap

                                    croppedResult(outputBitmap)
                                }
                            }
                        }
                )

            }
        }
    }
}

