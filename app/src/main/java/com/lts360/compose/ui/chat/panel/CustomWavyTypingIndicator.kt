package com.lts360.compose.ui.chat.panel

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun BoxScope.CustomWavyTypingIndicator() {
    val duration = 500
    val delayPerItem = 150
    val dotCount = 3

    val dotOffsets = remember { List(dotCount) { Animatable(0f) } }

    dotOffsets.forEachIndexed { index, animatable ->
        LaunchedEffect(animatable) {
            animatable.animateTo(
                targetValue = -5f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = duration, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse,
                    initialStartOffset = StartOffset(offsetMillis = (delayPerItem * index))
                )
            )
        }
    }


    Card(
        modifier = Modifier
            .wrapContentWidth()
            .align(Alignment.BottomStart)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 16.dp,
            bottomEnd = 16.dp,
            bottomStart = 0.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .wrapContentWidth()
                .padding(8.dp)
        ) {

            dotOffsets.forEach { offset ->
                Text(
                    text = "●",
                    modifier = Modifier.offset {
                        IntOffset(
                            x = 0,
                            y = offset.value.dp.roundToPx()
                        )
                    },
                    color = Color.White,
                    fontSize = 16.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }

    Spacer(Modifier.height(8.dp))

}