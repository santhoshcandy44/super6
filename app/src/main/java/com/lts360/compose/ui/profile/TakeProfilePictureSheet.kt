package com.lts360.compose.ui.profile

import android.app.Activity
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TakePictureSheet(
    sheetState: Boolean,
    onGallerySelected: () -> Unit,
    onCameraSelected: () -> Unit,
    onDismissRequest: () -> Unit
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


    var isShowingDialogRationale by rememberSaveable { mutableStateOf(false) }
    var isShowingPermissionRequestDialog by rememberSaveable { mutableStateOf(false) }


    // Register the permission request callback for multiple permissions
    val requestPermissions = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isPermissionGranted ->

        // Update state based on the permissions result
        if (isPermissionGranted) {
            setPermissionsGranted(true)
        } else {
            setPermissionsGranted(false)
            showRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                context as Activity,
                android.Manifest.permission.CAMERA
            )
            if (!showRationale) {
                isShowingDialogRationale = true
            } else {
                isShowingPermissionRequestDialog = true
            }
            // Optionally, show rationale or notify the user why these permissions are required
        }
    }





    if (sheetState) {
        Box(modifier = Modifier.fillMaxSize()) {


            ModalBottomSheet(
                contentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .padding(16.dp)
                    .safeDrawingPadding(),
                onDismissRequest = {
                    onDismissRequest()
                },
                sheetState = rememberModalBottomSheetState(),
                dragHandle = null,
                shape = RoundedCornerShape(16.dp)
            ) {
                // Sheet content

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "Choose",
                        style = LocalTextStyle.current.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {

                            Column {

                                Box(
                                    modifier = Modifier
                                        .background(Color(0XFFFDF4F5), CircleShape)
                                        .clip(CircleShape)
                                        .clickable {
                                            onGallerySelected()
                                        },

                                    ) {
                                    Image(
                                        imageVector = Icons.Filled.Photo,
                                        contentDescription = null,
                                        colorFilter = ColorFilter.tint(
                                            Color(
                                                0xFF8B5DFF
                                            )
                                        ),
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .size(40.dp)

                                    )


                                }
                                Spacer(Modifier.height(8.dp))
                                Text("Gallery")
                            }

                        }

                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0XFFFDF4F5), CircleShape)
                                        .clip(CircleShape)
                                        .clickable {
                                            onDismissRequest()
                                            if (permissionsGranted) {
                                                onCameraSelected()
                                            } else {

                                                if (!showRationale) {
                                                    isShowingDialogRationale = true
                                                } else {
                                                    // Request both permissions at once
                                                    requestPermissions.launch(
                                                        android.Manifest.permission.CAMERA
                                                    )
                                                }
                                            }
                                        },

                                    ) {
                                    Image(
                                        imageVector = Icons.Filled.Camera,
                                        contentDescription = null,
                                        colorFilter = ColorFilter.tint(Color.Red),
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .size(40.dp)
                                    )

                                }


                                Spacer(Modifier.height(8.dp))
                                Text("Camera")
                            }


                        }

                    }


                }

            }
        }
    }

}
