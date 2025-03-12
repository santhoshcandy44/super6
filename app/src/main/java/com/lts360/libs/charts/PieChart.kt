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
fun PieChart(
    values: List<Int>,
    colors: List<Color>,
    modifier: Modifier = Modifier,
    size: Dp = 200.dp,  // Default size
    borderColor: Color = Color.White,
    strokeWidth: Float = 160f,  // This represents the outer border width
) {
    require(values.isNotEmpty() && colors.isNotEmpty()) {
        "Values and colors must not be empty."
    }
    require(values.size == colors.size) {
        "The number of values and colors must be the same. Found values: ${values.size}, colors: ${colors.size}"
    }

    Box(
        modifier = modifier.size(size)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val width = this.size.width
            val height = this.size.height
            val totalValue = values.sum().toFloat()

            var startAngle = 0f
            var startAngleInnerArc = 0f

            val drawSize = min(width, height)
            val arcSize = Size(drawSize, drawSize)


            values.forEachIndexed { index, value ->
                val sweepAngle = (value / totalValue) * 360f

                val borderInset = strokeWidth  // Move inward by half the border width

                val topLeft = Offset(
                    (width - arcSize.width) / 2 + borderInset / 2,
                    (height - arcSize.height) / 2 + borderInset / 2
                )


                // Draw the filled arc
                drawArc(
                    color = colors[index],
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,  // This avoids filling the center
                    size = Size(arcSize.width-strokeWidth, arcSize.height-strokeWidth),
                    topLeft = topLeft
                )

                startAngle += sweepAngle
            }

            values.forEachIndexed { _, value ->
                val sweepAngle = (value / totalValue) * 360f

                val borderInset = strokeWidth  // Move inward by half the border width

                val borderTopLeft = Offset(
                    (width - arcSize.width) / 2 + borderInset / 2,
                    (height - arcSize.height) / 2 + borderInset / 2
                )


                drawArc(
                    color = borderColor,
                    startAngle = startAngleInnerArc,
                    sweepAngle = sweepAngle,
                    useCenter = false,  // Border should not connect to center
                    size = Size(arcSize.width-strokeWidth, arcSize.height-strokeWidth),
                    topLeft = borderTopLeft,
                    style = Stroke(strokeWidth)
                )

                startAngleInnerArc += sweepAngle
            }
        }
    }
}
