package com.lts360.libs.camera.ui

import androidx.camera.compose.CameraXViewfinder
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun CameraPreview(
    surfaceRequest: androidx.camera.core.SurfaceRequest
) {

    CameraXViewfinder(
        surfaceRequest = surfaceRequest,
        modifier = Modifier.fillMaxSize()
    )
}