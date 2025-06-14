package com.lts360.compose.ui.profile

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.dropUnlessResumed
import com.lts360.BuildConfig
import com.lts360.R
import com.lts360.components.utils.InputStreamRequestBody
import com.lts360.components.utils.getFileNameForUri
import com.lts360.compose.transformations.PlaceholderTransformation
import com.lts360.compose.ui.common.CircularProgressIndicatorLegacy
import com.lts360.compose.ui.profile.viewmodels.ProfileSettingsViewModel
import com.lts360.compose.ui.services.manage.ErrorText
import com.lts360.libs.imagecrop.CropProfilePicActivityContracts
import com.lts360.libs.imagepicker.GalleryPagerActivityResultContracts
import com.lts360.libs.ui.ShortToast
import okhttp3.MultipartBody
import java.io.File
import java.io.InputStream


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileSettingsScreen(
    onEditFirstNameNavigateUp: () -> Unit,
    onEditLastNameNavigateUp: () -> Unit,
    onEditAboutNavigateUp: () -> Unit,
    onEditEmailNavigateUp: () -> Unit,
    onPopBakStack: () -> Unit,
    viewModel: ProfileSettingsViewModel
) {

    val userId = viewModel.userId

    val purpleGradientBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFF6200EE),
            Color(0xFF9747ff), Color(0xFFBB86FC)
        )
    )

    val userProfile by viewModel.userProfile.collectAsState()


    val profilePicBitmap by viewModel.profileImageBitmap.collectAsState()
    val profileCompletionPercentage by viewModel.profileCompletionPercentage.collectAsState()
    val healthStatus by viewModel.profileHealthStatus.collectAsState()

    val isProfilePicLoading by viewModel.isProfilePicLoading.collectAsState()

    val context = LocalContext.current

    var profilePickerState by rememberSaveable { mutableStateOf(false) }


    fun startUploadFile(uri: Uri) {
        val inputStreamRequestBody = InputStreamRequestBody(context, uri)

        val displayName = getFileNameForUri(context, uri)
            ?: throw NullPointerException("Display name is null")

        val imagePart =
            MultipartBody.Part.createFormData("profile_pic", displayName, inputStreamRequestBody)

        viewModel.onUploadProfileImage(userId, imagePart, onSuccess = {
            ShortToast(context = context, message = it)
        }) {
            ShortToast(context = context, message = it)
        }
    }

    val cropLauncher = rememberLauncherForActivityResult(
        CropProfilePicActivityContracts.ImageCropper()
    ) { uri ->
        uri?.let {
            startUploadFile(it)
        }
    }

    var isImagePickerLauncherLaunched by rememberSaveable { mutableStateOf(false) }
    val imagePickerLauncher =
        rememberLauncherForActivityResult(GalleryPagerActivityResultContracts.PickSingleImage()) { uri: Uri? ->

            uri?.let { selectedUri ->

                try {

                    val inputStream: InputStream? =
                        context.contentResolver.openInputStream(selectedUri)

                    val bitmap = inputStream?.let { BitmapFactory.decodeStream(it) }

                    if (bitmap != null) {
                        val width = bitmap.width
                        val height = bitmap.height

                        if (width >= 100 && height >= 100) {
                            if (width == height) {
                                startUploadFile(selectedUri)
                            } else {
                                cropLauncher.launch(selectedUri)
                            }
                        } else {
                            ShortToast(context, "Image is too small")
                        }
                    }


                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            isImagePickerLauncherLaunched = false
        }

    var isCameraPickerLauncherLaunched by rememberSaveable { mutableStateOf(false) }
    var cameraPickerUri: Uri? = remember { null }

    val cameraImagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()

    ) { isCaptured ->
        if (isCaptured) {
            cameraPickerUri?.let {
                startUploadFile(it)
            }
        }

        isCameraPickerLauncherLaunched = false
    }


    val progressColor = when (healthStatus) {
        "Poor" -> Color.Red
        "Weak" -> Color(0xFFFFA500)
        "Good" -> Color.Green
        else -> Color.Unspecified
    }

    val trackColor = progressColor.copy(alpha = 0.3f)

    var bottomEditPhoneSheetState by remember { mutableStateOf(false) }

    Scaffold(topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = dropUnlessResumed { onPopBakStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back Icon"
                        )
                    }
                },
                title = {
                    Text(
                        text = "Manage Profile Settings",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            )
        }) { contentPadding ->

        Box(modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(contentPadding)) {

            userProfile?.let { nonNullUserProfile ->

                val firstName = nonNullUserProfile.firstName
                val lastName = nonNullUserProfile.lastName ?: ""
                val about = nonNullUserProfile.about
                val aboutError = if (about.isNullOrEmpty()) "About is not yet updated" else null
                val email = nonNullUserProfile.email
                val phoneNumber = nonNullUserProfile.phoneNumber
                val phoneCountryCode = nonNullUserProfile.phoneCountryCode
                val phoneError = if (phoneCountryCode.isNullOrEmpty() || phoneNumber.isNullOrEmpty()) "Phone is not yet updated" else null

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    LinearProgressIndicator(
                        color = progressColor,
                        trackColor = trackColor,
                        progress = { profileCompletionPercentage },
                        gapSize = 0.dp,
                        strokeCap = StrokeCap.Square,
                        drawStopIndicator = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Profile Completion: ${(profileCompletionPercentage * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Profile Health Status: $healthStatus",
                        color = when (healthStatus) {
                            "Poor" -> Color.Red
                            "Weak" -> Color(0xFFFFA500)
                            else -> Color.Green
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(text = "Profile Picture",
                        style = MaterialTheme.typography.titleMedium)

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .align(Alignment.CenterHorizontally)
                    ) {

                        profilePicBitmap?.let { bitmap ->

                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Profile Image",
                                modifier = Modifier
                                    .size(100.dp)
                                    .border(
                                        width = 4.dp,
                                        purpleGradientBrush,
                                        shape = CircleShape
                                    )
                                    .padding(4.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )


                        } ?: run {
                            Image(
                                painter = painterResource(R.drawable.user_placeholder),
                                contentDescription = "Profile Image",
                                modifier = Modifier
                                    .size(100.dp)
                                    .border(
                                        width = 4.dp,
                                        purpleGradientBrush,
                                        shape = CircleShape
                                    )
                                    .padding(4.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }



                        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
                            IconButton(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd),

                                onClick = {
                                    profilePickerState = true
                                }) {

                                Icon(
                                    painter = painterResource(id = R.drawable.ic_edit),
                                    contentDescription = "Edit profile pic"
                                )
                            }
                        }



                        if (isProfilePicLoading) {

                            CircularProgressIndicatorLegacy(
                                modifier = Modifier.align(Alignment.Center),
                                color = MaterialTheme.colorScheme.primary
                            )

                        }

                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Personal Information",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = firstName,
                        onValueChange = { },
                        label = { Text("First Name") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = dropUnlessResumed {
                                onEditFirstNameNavigateUp()
                            }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_edit), // Edit icon as an example
                                    contentDescription = "Edit icon"
                                )
                            }
                        },

                        visualTransformation = if (firstName.isEmpty())
                            PlaceholderTransformation(" ")
                        else VisualTransformation.None,
                        modifier = Modifier
                            .fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = lastName,
                        onValueChange = {},
                        label = { Text("Last Name") },
                        readOnly = true,

                        trailingIcon = {
                            IconButton(onClick = dropUnlessResumed { onEditLastNameNavigateUp() }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_edit),
                                    contentDescription = "Edit icon"
                                )
                            }
                        },

                        visualTransformation = if (lastName.isEmpty())
                            PlaceholderTransformation(" ")
                        else VisualTransformation.None,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = about ?: "",
                        onValueChange = { },
                        label = { Text("About") },
                        readOnly = true,
                        visualTransformation = if (about.isNullOrEmpty())
                            PlaceholderTransformation(" ")
                        else VisualTransformation.None,

                        trailingIcon = {
                            IconButton(onClick = dropUnlessResumed {
                                onEditAboutNavigateUp()
                            }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_edit),
                                    contentDescription = "Edit icon"
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth(),
                        minLines = 4,
                        isError = aboutError != null
                    )

                    aboutError?.let {
                        ErrorText(it)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Contact",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { },
                        label = { Text("Email") },
                        readOnly = true,
                        visualTransformation = if (email.isEmpty())
                            PlaceholderTransformation(" ")
                        else VisualTransformation.None,
                        trailingIcon = {
                            IconButton(onClick = dropUnlessResumed { onEditEmailNavigateUp() }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_edit),
                                    contentDescription = "Edit icon"
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))


                    OutlinedTextField(
                        value = if(phoneCountryCode!=null && phoneNumber!=null)
                            "$phoneCountryCode $phoneNumber" else "",
                        onValueChange = { },
                        label = { Text("Phone") },
                        readOnly = true,
                        visualTransformation = if (email.isEmpty())
                            PlaceholderTransformation(" ")
                        else VisualTransformation.None,
                        trailingIcon = {
                            IconButton(onClick = {
                                bottomEditPhoneSheetState = true
                            }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_edit),
                                    contentDescription = "Edit icon"
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth(),
                        singleLine = true,
                        isError = phoneError != null
                    )

                    phoneError?.let {
                        ErrorText(it)
                    }

                }
            } ?: run {
                CircularProgressIndicatorLegacy(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            }


            if (bottomEditPhoneSheetState) {

                EditPhoneBottomSheet(onVerifyClick = {

                }, onDismiss = {
                    bottomEditPhoneSheetState = false
                })
            }

            TakePictureSheet(
                profilePickerState,
                {
                    if (isImagePickerLauncherLaunched)
                        return@TakePictureSheet
                    isImagePickerLauncherLaunched = true
                    imagePickerLauncher.launch(Unit)
                    profilePickerState = false
                }, {
                    if (isCameraPickerLauncherLaunched)
                        return@TakePictureSheet
                    isCameraPickerLauncherLaunched = true

                    cameraPickerUri = FileProvider.getUriForFile(
                        context, "${BuildConfig.APPLICATION_ID}.provider",
                        File(context.cacheDir, "IMG_${System.currentTimeMillis()}.jpg")
                    )
                    cameraImagePickerLauncher.launch(cameraPickerUri!!)
                    profilePickerState = false
                }, {
                    profilePickerState = false
                })


        }
    }

}
