package com.lts360.compose.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.lts360.compose.ui.theme.customColorScheme

@Composable
fun SafeDrawingBox(
    statusBarColor:Color = MaterialTheme.colorScheme.secondary,
    navigationBarColor:Color = MaterialTheme.customColorScheme.navigationBarColor,
    isFullScreenMode: Boolean = false,
    content: @Composable () -> Unit,
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(statusBarColor)
            .then(
                if (!isFullScreenMode) Modifier.statusBarsPadding()
                else Modifier
            )
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(navigationBarColor)
                .then(
                    if (!isFullScreenMode) Modifier.navigationBarsPadding()
                    else Modifier
                ).imePadding()
        ) {
            content()
        }
    }
}