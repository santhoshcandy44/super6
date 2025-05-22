package com.lts360.compose.ui.chat.panel

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.lts360.compose.ui.common.CircularProgressIndicatorLegacy

@Composable
fun BoxScope.PreLoadingMediaButton(onCancel: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(8.dp)
            .align(Alignment.BottomEnd)
            .clip(RoundedCornerShape(8.dp))
            .background(
                Color.Black.copy(alpha = 0.4f),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable {
                onCancel()
            }
            .padding(8.dp)
    ) {


        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box {
                CircularProgressIndicatorLegacy(
                    strokeCap = StrokeCap.Square,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(32.dp)
                )

                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Close",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(24.dp),
                    tint = Color.White
                )
            }

        }

    }
}