package com.lts360.compose.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lts360.compose.ui.theme.customColorScheme

@Composable
fun SafeDrawingBox(
    fullScreenMode: Boolean = false,
    content: @Composable () -> Unit
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.secondary)
            .then(
                if (!fullScreenMode) Modifier.statusBarsPadding()
                else Modifier
            )
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.customColorScheme.navigationBarColor)
                .then(
                    if (!fullScreenMode) Modifier.navigationBarsPadding()
                    else Modifier
                )
        ) {
            content()
        }
    }
}