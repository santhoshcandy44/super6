package com.lts360.libs.camera.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp


@Composable
fun TorchButton(flashEnabled: Boolean, onFlashToggle:()-> Unit) {



    Box(
        modifier = Modifier
            .size(40.dp) // Outer circle size (border area)
            .border(
                width = 1.dp, // Border thickness
                color = Color.White, // Border color
                shape = CircleShape
            )
            .clip(CircleShape)
            .clickable {
                onFlashToggle()
            }
    ) {


        Image(
            imageVector =  if (flashEnabled) Icons.Filled.FlashOff else Icons.Filled.FlashOn,
            contentDescription = null,
            colorFilter = ColorFilter.tint(Color.White),
            modifier = Modifier
                .padding(8.dp)
                .size(40.dp),
        )

    }

}
