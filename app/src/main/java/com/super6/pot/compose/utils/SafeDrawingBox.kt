package com.super6.pot.compose.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.super6.pot.compose.ui.theme.customColorScheme

@Composable
fun SafeDrawingBox(content: @Composable () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.secondary)
            .statusBarsPadding()
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.customColorScheme.navigationBarColor)
                .navigationBarsPadding()
        ) {
            content()
        }
    }
}
