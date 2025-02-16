package com.lts360.compose.ui.services.manage

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.lts360.compose.ui.auth.LoadingDialog
import com.lts360.compose.ui.chat.createImagePartForUri
import com.lts360.compose.ui.chat.getFileExtensionFromImageFormat
import com.lts360.compose.ui.chat.isValidThumbnailDimensionsFormat
import com.lts360.compose.ui.services.manage.viewmodels.PublishedServicesViewModel
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditServiceThumbnailScreen(
    navHostController: NavHostController,
    onPopBackStack: () -> Unit,
    viewModel: PublishedServicesViewModel
) {


    val userId = viewModel.userId

    val isUpdating by viewModel.isUpdating.collectAsState()

    val selectedService by viewModel.selectedService.collectAsState()


    val thumbnailContainer by viewModel.thumbnailContainer.collectAsState()


    val context = LocalContext.current

    var isPickerLaunch by remember { mutableStateOf(false) }


    val pickThumbnailImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->

        uri?.let {
            // Proceed if there are URIs to handle
            val result = isValidThumbnailDimensionsFormat(context, uri)
            val errorMessage =when {
                !result.isValidDimension -> "Invalid Dimension"
                !result.isValidFormat -> "Invalid Format"
                else -> null
            }

            viewModel.updateThumbnailContainer(
                it.toString(),
                result.width,
                result.height,
                result.format.toString(),
                errorMessage
            )
        }
        // Reset picker launch flag
        isPickerLaunch = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = onPopBackStack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back Icon"
                            )
                        }
                    },
                    title = {
                        Text(
                            text = "Edit Service Thumbnail",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                )
            },
            modifier = Modifier
                .fillMaxSize()
        ) { contentPadding ->
            // Toolbar


            Column(
                modifier = Modifier
                    .padding(contentPadding)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {

                thumbnailContainer?.let {
                    UploadServiceThumbnailContainer(
                        {
                            if (isPickerLaunch)
                                return@UploadServiceThumbnailContainer

                            isPickerLaunch = true

                            pickThumbnailImageLauncher.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        },it, it.path)
                } ?: run {
                    UploadServiceThumbnailContainer(
                        {

                            if (isPickerLaunch)
                                return@UploadServiceThumbnailContainer

                            isPickerLaunch = true

                            pickThumbnailImageLauncher.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        },
                        imageUrl = selectedService?.thumbnail?.imageUrl,
                        isPath = false
                    )
                }


                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        selectedService?.let { service ->


                            // Assuming `selectedIndex` is defined and valid
                            val userIdRequestBody =
                                userId.toString().toRequestBody("text/plain".toMediaType())

                            val imageIdRequestBody: RequestBody =
                                (selectedService?.thumbnail?.imageId ?: -1)
                                    .toString()  // Convert the integer (or null fallback) to a string
                                    .toRequestBody("text/plain".toMediaType())




                            if (viewModel.validateThumbnailContainer()) {


                                thumbnailContainer?.let {

                                    val imagePart = createImagePartForUri(
                                        context,
                                        Uri.parse(it.path),
                                        "IMAGE_THUMBNAIL_${
                                            getFileExtensionFromImageFormat(
                                                it.format
                                            )
                                        }", it.format, "thumbnail"
                                    )

                                    if (imagePart == null) {
                                        Toast.makeText(
                                            context,
                                            "Something went wrong on updating thumbnail",
                                            Toast.LENGTH_SHORT
                                        )
                                            .show()
                                        return@Button
                                    }

                                    viewModel.onUpdateServiceThumbnail(
                                        userIdRequestBody,
                                        service.serviceId,
                                        imagePart,
                                        imageIdRequestBody, {
                                            Toast.makeText(context, it, Toast.LENGTH_SHORT)
                                                .show()
                                        }
                                    ) {
                                        Toast.makeText(context, it, Toast.LENGTH_SHORT)
                                            .show()
                                    }

                                }

                            }
                        }
                    }) {
                    Text(
                        text = "Update Service Thumbnail",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }


            }


        }

        if (isUpdating) {
            LoadingDialog()
        }
    }

}