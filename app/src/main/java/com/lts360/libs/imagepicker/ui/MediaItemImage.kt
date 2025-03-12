package com.lts360.libs.imagepicker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.lts360.compose.ui.NoRippleInteractionSource
import com.lts360.libs.imagepicker.models.ImageMediaData

@Composable
fun MediaItemImage(
    media: ImageMediaData, onClicked: () -> Unit = {},  isSingle:Boolean=true, size: Dp = 80.dp ){


    // Card to hold the media content
    Card(
        onClick = onClicked,
        interactionSource = remember { NoRippleInteractionSource() },
        modifier = Modifier
            .wrapContentSize(),  // Maintain aspect ratio
    ) {
        Box(
            modifier = Modifier.wrapContentSize(),
            contentAlignment = Alignment.Center
        ) {
            // Display the media image
            AsyncImage(
                media.uri,
                contentDescription = media.displayName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(size)
            )

            if(!isSingle){
                // Top-right corner indicator for selection status
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)  // Space from the top and right edges
                        .size(24.dp)  // Adjust size as needed
                ) {
                    // Conditional selection indicator (circle)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                color = if (media.isSelected) Color.Yellow else Color.White,  // Orange if selected, white otherwise
                                shape = CircleShape
                            )
                            .border(2.dp, Color.White, CircleShape)  // White border
                    )
                }
            }

        }
    }
}
