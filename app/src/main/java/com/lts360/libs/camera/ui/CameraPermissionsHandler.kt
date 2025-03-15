package com.lts360.libs.camera.ui

import android.app.Activity
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.lts360.libs.imagepicker.utils.redirectToAppSettings

@Composable
fun CameraPermissionHandler(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {

    val context = LocalContext.current

    var showRationale by remember {
        mutableStateOf(
            ActivityCompat.shouldShowRequestPermissionRationale(
                context as Activity,
                android.Manifest.permission.CAMERA
            )
        )
    }


    // State to handle permission request
    var isInitial by remember {
        mutableStateOf(
            ActivityCompat.shouldShowRequestPermissionRationale(
                context as Activity,
                android.Manifest.permission.CAMERA
            )
        )
    }


    val hasPermissionGranted: () -> Boolean = {
        ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    // State to handle permission request
    val (permissionsGranted, setPermissionsGranted) = remember {
        mutableStateOf(
            hasPermissionGranted()
        )
    }


    // Register the permission request callback for multiple permissions
    val requestPermissions = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraPermissionGranted = permissions[android.Manifest.permission.CAMERA] == true

        // Update state based on the permissions result
        if (cameraPermissionGranted) {
            setPermissionsGranted(true)
        } else {
            setPermissionsGranted(false)
            isInitial = false
            showRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                context as Activity,
                android.Manifest.permission.CAMERA
            )
            // Optionally, show rationale or notify the user why these permissions are required
        }
    }


    LaunchedEffect(Unit) {
        if (!permissionsGranted) {
            // Request both permissions at once
            requestPermissions.launch(
                arrayOf(
                    android.Manifest.permission.CAMERA
                )
            )
        }
    }

    LifecycleResumeEffect(Unit) {
        if (!permissionsGranted) {
            if (hasPermissionGranted()) {
                setPermissionsGranted(true)
            }
        }
        onPauseOrDispose {

        }
    }


    // Column to show the UI for requesting permissions or displaying the gallery
    Box(modifier = modifier.fillMaxSize()) {

        // If permissions are granted, show gallery or perform tasks
        if (permissionsGranted) {
            // Load the gallery once permissions are granted
            content()
        } else {
            Column(
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.Center)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                if (!showRationale) {
                    // If permissions are not granted, show a button to request them
                    Text(
                        "Camera Permission is required.",
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(8.dp))

                    Button(

                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(
                                0xFFFE8B02,
                            )
                        ),
                        onClick = {
                            redirectToAppSettings(context)
                        }) {
                        Text("Allow Permission", color = Color.White)
                    }
                } else {


                    if (!isInitial) {
                        // If permissions are not granted, show a button to request them
                        Text(
                            "To take picture permission is required.",
                            color = Color.White, textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(8.dp))

                        Button(

                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(
                                    0xFFFE8B02,
                                )
                            ),
                            onClick = {
                                // Request both permissions at once
                                requestPermissions.launch(
                                    arrayOf(android.Manifest.permission.CAMERA)
                                )
                            }) {
                            Text("Allow Permission", color = Color.White)
                        }
                    }
                }
            }
        }
    }

}