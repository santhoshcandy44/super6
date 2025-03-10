package com.lts360.libs.imagecrop

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lts360.components.utils.LogUtils.TAG

@Composable
fun CropView(
    cropRectSize: Dp,
    pWidth: Dp,
    pHeight: Dp,
    onCropChanged: (Rect) -> Unit,
    minSize: Dp = 100.dp
) {


    val density = LocalDensity.current

    val maxSize by rememberUpdatedState(cropRectSize)
    val parentWidth by rememberUpdatedState(pWidth)
    val parentHeight by rememberUpdatedState(pHeight)

    var cropSize by remember { mutableStateOf(cropRectSize) }


    val initialOffsetX = remember(parentWidth, parentHeight, cropRectSize, density) {
        with(density) {
            ((parentWidth.toPx() - cropRectSize.toPx()) / 2).toDp()
        }

    }

    val initialOffsetY = remember(parentWidth, parentHeight, cropRectSize, density) {
        with(density) {
            ((parentHeight.toPx() - cropRectSize.toPx()) / 2).toDp()
        }
    }


    var offsetX by remember {
        mutableStateOf(initialOffsetX)
    }

    var offsetY by remember {
        mutableStateOf(initialOffsetY)
    }


    LaunchedEffect(cropRectSize, initialOffsetX, initialOffsetY) {
        cropSize = cropRectSize
        offsetX = initialOffsetX
        offsetY = initialOffsetY
    }





    // Crop Area Box
    Box(
        modifier = Modifier
            .offset(offsetX, offsetY)
            .size(cropSize)
            .pointerInput(parentWidth, parentHeight, cropSize) {
                detectDragGestures { change, dragAmount ->
                    change.consume()

                    // Calculate new offsets
                    var newOffsetX = offsetX + dragAmount.x.toDp()
                    var newOffsetY = offsetY + dragAmount.y.toDp()

                    // Calculate max boundaries to prevent moving out of bounds
                    val maxOffsetX = (parentWidth.value - cropSize.value).dp
                    val maxOffsetY = (parentHeight.value - cropSize.value).dp

                    // Clamp the offsets within the bounds
                    newOffsetX = newOffsetX.coerceIn(0.dp, maxOffsetX)
                    newOffsetY = newOffsetY.coerceIn(0.dp, maxOffsetY)

                    // Update the offset values
                    offsetX = newOffsetX
                    offsetY = newOffsetY
                }
            }
            .onGloballyPositioned {
                with(density) {
                    val x = offsetX.toPx()
                    val y = offsetY.toPx()
                    val width = cropSize.toPx()
                    val height = cropSize.toPx()

                    onCropChanged(Rect(x, y, x + width, y + height))
                }
            }

    ) {
        // 9-Square Grid Overlay
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = size.width / 3
            for (i in 1..2) {
                // Vertical grid lines
                drawLine(
                    Color.White,
                    start = Offset(step * i, 0f),
                    end = Offset(step * i, size.height),
                    strokeWidth = 2.dp.toPx()
                )
                // Horizontal grid lines
                drawLine(
                    Color.White,
                    start = Offset(0f, step * i),
                    end = Offset(size.width, step * i),
                    strokeWidth = 2.dp.toPx()
                )
            }
        }

        // Top border resizing logic
        ResizableBorder(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(2.dp), // Resize border on the top
            onResize = { deltaX, deltaY ->
                val deltaDp = with(density) { deltaY.toDp() }

                // Compute new size ensuring constraints
                val newSize = (cropSize - deltaDp).coerceIn(minSize, maxSize)
                // Calculate the delta difference
                val deltaChange = newSize - cropSize

                val maxOffsetX = (parentWidth.value - cropSize.value).dp
                val maxOffsetY = (parentHeight.value - cropSize.value).dp

                // Check if the crop area will overflow any boundary (top, left, right, bottom)
                if (offsetX - deltaDp > maxOffsetX || offsetY - deltaDp > maxOffsetY || offsetX < 0.dp || offsetY < 0.dp) {

                    // If the right boundary is exceeded
                    if (offsetX - deltaDp > maxOffsetX) {
                        offsetX -= deltaChange
                    }

                    // If the bottom boundary is exceeded
                    if (offsetY - deltaDp > maxOffsetY) {
                        offsetY -= deltaChange
                    }

                    // If the left boundary is exceeded
                    if (offsetX < 0.dp) {
                        offsetX = 0.dp // Prevent moving beyond the left edge
                    }

                    // If the top boundary is exceeded
                    if (offsetY < 0.dp) {
                        offsetY = 0.dp // Prevent moving beyond the top edge
                    }

                } else {
                    // If no boundaries are exceeded, expand the crop area symmetrically
                    offsetX -= deltaChange / 2
                    offsetY -= deltaChange / 2
                }

                // Ensure that offsetX and offsetY are not negative
                offsetX = offsetX.coerceAtLeast(0.dp)
                offsetY = offsetY.coerceAtLeast(0.dp)

                cropSize = newSize


            }
        )


        // Bottom border resizing logic
        ResizableBorder(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(2.dp), // Resize border on the bottom
            onResize = { deltaXDp, deltaY ->
                val deltaDp = with(density) { deltaY.toDp() }

                // Compute new size ensuring constraints
                val newSize = (cropSize + deltaDp).coerceIn(minSize, maxSize)

                // Calculate the change in size
                val deltaChange = newSize - cropSize
                Log.e(TAG, "maxSize ${maxSize}")
                Log.e(TAG, "deltaDp ${deltaDp}")
                Log.e(TAG, "cropRectSize ${cropRectSize}")
                Log.e(TAG, "cropSize ${cropSize}")
                Log.e(TAG, "newSize ${newSize}")


                val maxOffsetX = (parentWidth.value - cropSize.value).dp
                val maxOffsetY = (parentHeight.value - cropSize.value).dp


                // Check if the crop area will overflow any boundary (top, left, right, bottom)
                if (offsetX + deltaDp > maxOffsetX || offsetY + deltaDp > maxOffsetY || offsetX < 0.dp || offsetY < 0.dp) {

                    // If the right boundary is exceeded
                    if (offsetX + deltaDp > maxOffsetX) {
                        offsetX -= deltaChange

                        if (offsetY - deltaDp < maxOffsetY) {
                            offsetY -= deltaChange
                        }
                        Log.e(TAG, "X exceeds on the right side")
                    }

                    // If the bottom boundary is exceeded
                    if (offsetY + deltaDp > maxOffsetY) {
                        offsetY -= deltaChange
                        if (offsetX + deltaDp < maxOffsetX) {
                            offsetX -= deltaChange / 2
                        }
                        Log.e(TAG, "Y exceeds on the bottom side")
                    }

                    // If the left boundary is exceeded
                    if (offsetX < 0.dp) {
                        offsetX = 0.dp // Prevent moving beyond the left edge
                        Log.e(TAG, "X exceeds on the left side")
                    }

                    // If the top boundary is exceeded
                    if (offsetY < 0.dp) {
                        offsetY = 0.dp // Prevent moving beyond the top edge
                        Log.e(TAG, "Y exceeds on the top side")
                    }

                } else {

                    // If no boundaries are exceeded, expand the crop area symmetrically
                    offsetX -= deltaChange / 2
                    offsetY -= deltaChange / 2
                    Log.e(TAG, "No bounds bottom")

                }

                // Ensure that offsetX and offsetY are not negative
                offsetX = offsetX.coerceAtLeast(0.dp)
                offsetY = offsetY.coerceAtLeast(0.dp)

                // Update crop size
                cropSize = newSize
            }

        )

        // Left border resizing logic
        ResizableBorder(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxHeight()
                .width(2.dp), // Resize border on the left
            onResize = { deltaX, deltaY ->

                val deltaXDp = with(density) { deltaX.toDp() }
                val deltaYDp = with(density) { deltaX.toDp() }

                // Compute new size ensuring constraints
                val newSize = (cropSize - deltaXDp).coerceIn(minSize, maxSize)

                // Calculate the delta difference
                val deltaChange = newSize - cropSize


                val maxOffsetX = (parentWidth.value - cropSize.value).dp
                val maxOffsetY = (parentHeight.value - cropSize.value).dp

                // Check if the crop area will overflow any boundary (top, left, right, bottom)
                if (offsetX - deltaXDp > maxOffsetX || offsetY - deltaXDp > maxOffsetY || offsetX < 0.dp || offsetY < 0.dp) {

                    // If the right boundary is exceeded
                    if (offsetX - deltaXDp > maxOffsetX) {
                        offsetX -= deltaChange
                        if (offsetY - deltaXDp < maxOffsetY) {
                            offsetY -= deltaChange
                            Log.e(TAG, "Right touched bounds")
                        }

                    }

                    // If the bottom boundary is exceeded
                    if (offsetY - deltaXDp > maxOffsetY) {
                        offsetY -= deltaChange
                        if (offsetX - deltaXDp < maxOffsetX) {
                            offsetX -= deltaChange
                            Log.e(TAG, "Bottom touched bounds")
                        }

                    }

                    // If the left boundary is exceeded
                    if (offsetX < 0.dp) {
                        Log.e(TAG, "Left touched bounds")
                        offsetX = 0.dp // Prevent moving beyond the left edge
                    }

                    // If the top boundary is exceeded
                    if (offsetY < 0.dp) {
                        Log.e(TAG, "Top touched bounds")
                        offsetY = 0.dp // Prevent moving beyond the top edge
                    }

                } else {

                    // If no boundaries are exceeded, expand the crop area symmetrically
                    offsetX -= deltaChange / 2
                    offsetY -= deltaChange / 2
                }

                // Ensure that offsetX and offsetY are not negative
                offsetX = offsetX.coerceAtLeast(0.dp)
                offsetY = offsetY.coerceAtLeast(0.dp)

                cropSize = newSize

            },
        )

        // Right border resizing logic
        ResizableBorder(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .width(2.dp), // Resize border on the right
            onResize = { deltaX, deltaY ->
                val deltaXDp = with(density) { deltaX.toDp() }
                val deltaYDp = with(density) { deltaX.toDp() }

                // Compute new size ensuring constraints
                val newSize = (cropSize + deltaXDp).coerceIn(minSize, maxSize)

                // Calculate the delta difference
                val deltaChange = newSize - cropSize


                val maxOffsetX = (parentWidth.value - cropSize.value).dp
                val maxOffsetY = (parentHeight.value - cropSize.value).dp

                // Check if the crop area will overflow any boundary (top, left, right, bottom)
                if (offsetX + deltaXDp > maxOffsetX || offsetY + deltaXDp > maxOffsetY || offsetX < 0.dp || offsetY < 0.dp) {

                    // If the right boundary is exceeded
                    if (offsetX + deltaXDp > maxOffsetX) {
                        offsetX -= deltaChange
                        Log.e(TAG, "Right touched bounds")
                    }

                    // If the bottom boundary is exceeded
                    if (offsetY + deltaXDp > maxOffsetY) {
                        offsetY -= deltaChange

                    }

                    // If the left boundary is exceeded
                    if (offsetX < 0.dp) {
                        Log.e(TAG, "Left touched bounds")
                        offsetX = 0.dp // Prevent moving beyond the left edge
                    }

                    // If the top boundary is exceeded
                    if (offsetY < 0.dp) {
                        Log.e(TAG, "Top touched bounds")
                        offsetY = 0.dp // Prevent moving beyond the top edge
                    }

                } else {
                    Log.e(TAG, "No bounds")

                    // If no boundaries are exceeded, expand the crop area symmetrically
                    offsetX -= deltaChange / 2
                    offsetY -= deltaChange / 2
                }

                // Ensure that offsetX and offsetY are not negative
                offsetX = offsetX.coerceAtLeast(0.dp)
                offsetY = offsetY.coerceAtLeast(0.dp)

                cropSize = newSize
            },

            )
    }
}

@Composable
fun ResizableBorder(
    modifier: Modifier,
    onResize: (Float, Float) -> Unit,

    ) {
    Box(
        modifier = modifier
            .background(Color.White) // Semi-transparent resize border
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onResize(dragAmount.x, dragAmount.y)
                }
            }
    )
}