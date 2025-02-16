package com.lts360.compose.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.lts360.compose.ui.theme.customColorScheme


@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    shimmerBackgroundContainerColor:Color=MaterialTheme.customColorScheme.shimmerContainer,
    shimmerColor:Color=MaterialTheme.customColorScheme.shimmerColor,
    content: @Composable () -> Unit = {
        Box(modifier = Modifier.fillMaxSize())
    },
){
    Box(modifier = modifier.background(shimmerBackgroundContainerColor)){
       Box(
           modifier = Modifier.shimmerLoadingAnimation(defaultShimmerBackgroundColor=shimmerBackgroundContainerColor, defaultShimmerColor = shimmerColor)
       ) {
           content()
       }
    }
}

// Extension function for shimmer loading animation
@Composable
fun Modifier.shimmerLoadingAnimation(
    widthOfShadowBrush: Int = 500,
    angleOfAxisY: Float = 270f,
    durationMillis: Int = 1000,
    defaultShimmerBackgroundColor: Color = MaterialTheme.customColorScheme.shimmerContainer,
    defaultShimmerColor:Color=MaterialTheme.customColorScheme.shimmerColor
): Modifier {
    return composed {

        val shimmerColors = listOf(defaultShimmerBackgroundColor,
            defaultShimmerColor.copy(alpha = 0.1f),
            defaultShimmerColor.copy(alpha = 0.3f),
            defaultShimmerColor.copy(alpha = 0.1f))


        val transition = rememberInfiniteTransition(label = "")

        val translateAnimation = transition.animateFloat(
            initialValue = 0f,
            targetValue = (durationMillis + widthOfShadowBrush).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = durationMillis,
                    easing = LinearEasing,
                ),
                repeatMode = RepeatMode.Restart,
            ),
            label = "Shimmer loading animation",
        )


        this.background(
            brush = Brush.linearGradient(
                colors = shimmerColors,
                start = Offset(x = translateAnimation.value - widthOfShadowBrush, y = 0.0f),
                end = Offset(x = translateAnimation.value, y = angleOfAxisY),
            ),
        )
    }
}


@Composable
fun Modifier.shimmerLoadingAnimationGray(
    widthOfShadowBrush: Int = 500,
    angleOfAxisY: Float = 270f,
    durationMillis: Int = 1000,
): Modifier {
    return composed {

        val shimmerColors = listOf(
            Color.LightGray.copy(alpha = 0.3f),
            Color.LightGray.copy(alpha = 0.5f),
            Color.LightGray.copy(alpha = 1.0f),
            Color.LightGray.copy(alpha = 0.5f),
            Color.LightGray.copy(alpha = 0.3f),
        )

        val transition = rememberInfiniteTransition(label = "")

        val translateAnimation = transition.animateFloat(
            initialValue = 0f,
            targetValue = (durationMillis + widthOfShadowBrush).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = durationMillis,
                    easing = LinearEasing,
                ),
                repeatMode = RepeatMode.Restart,
            ),
            label = "Shimmer loading animation",
        )

        this.background(
            brush = Brush.linearGradient(
                colors = shimmerColors,
                start = Offset(x = translateAnimation.value - widthOfShadowBrush, y = 0.0f),
                end = Offset(x = translateAnimation.value, y = angleOfAxisY),
            ),
        )
    }
}



