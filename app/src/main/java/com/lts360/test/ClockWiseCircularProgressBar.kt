package com.lts360.test

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


@Composable
fun ClockWiseCircularProgressBar(
    progress: Float, // Progress should be a float between 0f and 1f
    modifier: Modifier = Modifier,
    radius: Dp = 32.dp, // Radius of the circular progress bar
    strokeWidth: Dp = 4.dp, // Stroke width of the progress bar
    maxProgress: Float = 1f, // Default max progress (100%)
    circleColor: Color = Color.Gray,
    progressColor: Color = Color.White,
    isClockWise: Boolean = false,
    startAngle: Float = 270f
) {

    require(progress in 0f..maxProgress) { "Progress must be between 0f and $maxProgress, but was $progress" }

    val size = radius.toPx() * 2

    val angle = if (isClockWise) 360f * (progress / maxProgress) else -360f * (progress / maxProgress)

    Box(
        modifier = modifier
            .size(radius * 2)
            .clip(CircleShape)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = circleColor,
                radius = size / 2,
                style = Stroke(width = strokeWidth.toPx())
            )

            drawArc(
                color = progressColor,
                startAngle = startAngle,
                sweepAngle = angle,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx())
            )
        }
    }
}