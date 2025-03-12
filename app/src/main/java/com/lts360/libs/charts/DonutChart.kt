package com.lts360.libs.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.min

@Composable
fun DonutChart(
    values: List<Int>,
    colors: List<Color>,
    modifier: Modifier = Modifier,
    size: Dp = 200.dp,  // Default size
    strokeWidth: Float = 160f,  // This represents the outer border width
) {
    require(values.isNotEmpty() && colors.isNotEmpty()) {
        "Values and colors must not be empty."
    }
    require(values.size == colors.size) {
        "The number of values and colors must be the same. Found values: ${values.size}, colors: ${colors.size}"
    }

    // Animate the sweep angle for each arc
    val totalValue = values.sum().toFloat()
    var startAngle = 0f


    Box(
        modifier = modifier.size(size)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val width = this.size.width
            val height = this.size.height
            val drawSize = min(width, height)
            val arcSize = Size(drawSize, drawSize)

            values.forEachIndexed { index, value ->
                // Animate the sweep angle from 0 to the final value
                val sweepAngle = (value / totalValue) * 360f



                val borderInset = strokeWidth  // Move inward by half the border width

                val borderTopLeft = Offset(
                    (width - arcSize.width) / 2 + borderInset / 2,
                    (height - arcSize.height) / 2 + borderInset / 2
                )



                drawArc(
                    color = colors[index],
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,  // Use the animated sweep angle
                    useCenter = false,  // Border should not connect to center
                    size = Size(arcSize.width - strokeWidth, arcSize.height - strokeWidth),
                    topLeft = borderTopLeft,
                    style = Stroke(strokeWidth)
                )

                startAngle += sweepAngle
            }
        }
    }
}
