package com.lts360.compose.ui.profile

import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
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
import com.lts360.components.utils.LogUtils.TAG
import com.lts360.compose.transformations.PlaceholderTransformation
import com.lts360.compose.ui.common.CircularProgressIndicatorLegacy
import com.lts360.compose.ui.profile.viewmodels.ProfileSettingsViewModel
import com.lts360.compose.ui.services.manage.ErrorText
import com.lts360.libs.imagecrop.CropProfilePicActivityContracts
import com.lts360.libs.imagepicker.GalleryPagerActivityResultContracts
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
    onPopStack: () -> Unit,
    viewModel: ProfileSettingsViewModel
) {

    val userId = viewModel.userId

    val purpleGradientBrush = Brush.linearGradient(colors = listOf(Color(0xFF6200EE),
        Color(0xFF9747ff), Color(0xFFBB86FC)))

    val userProfile by viewModel.userProfile.collectAsState()


    val profilePicBitmap by viewModel.profileImageBitmap.collectAsState()
    val profileCompletionPercentage by viewModel.profileCompletionPercentage.collectAsState()
    val healthStatus by viewModel.profileHealthStatus.collectAsState()

    val isProfilePicLoading by viewModel.isProfilePicLoading.collectAsState()

    val context = LocalContext.current

    var profilePickerState by rememberSaveable { mutableStateOf(false) }


    fun startUploadFile(uri: Uri) {
        // Convert the URI to a file and upload the image
        val inputStreamRequestBody = InputStreamRequestBody(context, uri)

        val resolver = context.contentResolver
        val cursor = resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)

        val displayName = cursor?.use {
            if (it.moveToFirst()) {
                it.getString(it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
            } else {
                null
            }
        }

        if (displayName == null) {
            throw NullPointerException("Display name is null")
        }

        val imagePart =
            MultipartBody.Part.createFormData("profile_pic", displayName, inputStreamRequestBody)

        viewModel.onUploadImage(userId, imagePart, onSuccess = {
            Toast.makeText(context, it, Toast.LENGTH_SHORT)
                .show()
        }) {
            Toast.makeText(context, it, Toast.LENGTH_SHORT)
                .show()
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

                    // Step 1: Get InputStream from URI safely
                    val inputStream: InputStream? =
                        context.contentResolver.openInputStream(selectedUri)

                    // Step 2: Decode InputStream to Bitmap
                    val bitmap = inputStream?.let { BitmapFactory.decodeStream(it) }

                    if (bitmap != null) {
                        // Step 3: Get image width and height
                        val width = bitmap.width
                        val height = bitmap.height

                        // Step 4: Check if the image meets the minimum size (100px by 100px)
                        if (width >= 100 && height >= 100) {
                            // Step 5: Check the aspect ratio (1:1)
                            if (width == height) {

                                startUploadFile(selectedUri)

                            } else {
                                cropLauncher.launch(selectedUri)
                            }
                        } else {

                            Toast.makeText(context, "Image is too small", Toast.LENGTH_SHORT)
                                .show()
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


// Determine progress color based on health status
    val progressColor = when (healthStatus) {
        "Poor" -> Color.Red
        "Weak" -> Color(0xFFFFA500) // Orange color
        "Good" -> Color.Green // Good status
        else -> Color.Unspecified
    }

// Calculate track color as an alpha version of the original color
    val trackColor = progressColor.copy(alpha = 0.3f) // Adjust alpha value as needed

    Scaffold(

        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = dropUnlessResumed { onPopStack() }) {
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
        }
    ) { contentPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(contentPadding)
        ) {

            userProfile?.let { nonNullUserProfile ->

                val firstName = nonNullUserProfile.first_name
                val lastName = nonNullUserProfile.last_name ?: ""
                val about = nonNullUserProfile.about
                val aboutError = if (about.isNullOrEmpty()) "About is not yet updated" else null
                val email = nonNullUserProfile.email

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // Progress Bar and Profile Status
                    LinearProgressIndicator(
                        color = progressColor, // Use the determined color
                        trackColor = trackColor, // Set the track color to the alpha version
                        progress = { profileCompletionPercentage },
                        gapSize = 0.dp,
                        strokeCap = StrokeCap.Square,
                        drawStopIndicator = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Displaying Profile Completion Percentage
                    Text(
                        text = "Profile Completion: ${(profileCompletionPercentage * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // Health Status Text and Color
                    Text(
                        text = "Profile Health Status: $healthStatus",
                        color = when (healthStatus) {
                            "Poor" -> Color.Red
                            "Weak" -> Color(0xFFFFA500) // Orange color
                            else -> Color.Green
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Section Headers
                    Text(
                        text = "Profile Image",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Profile Image with Edit Icon and Progress Indicator
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .align(Alignment.CenterHorizontally)
                    ) {

                        // Display the image if available, else display a placeholder
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
                            )// Crop the image to fit the circle


                        } ?: run {
                            // Placeholder if image is not yet loaded

                            // Use Image composable to display the drawable
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
                                contentScale = ContentScale.Crop // Crop the image to fit the circle
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

                    // Personal Information Section
                    Text(
                        text = "Personal Information",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // First Name Input
                    OutlinedTextField(
                        value = firstName, // Bind this from your ViewModel
                        onValueChange = { },
                        label = { Text("First Name") },
                        readOnly = true, // Makes it non-editable
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

                    // Last Name Input
                    OutlinedTextField(
                        value = lastName, // Bind this from your ViewModel
                        onValueChange = {},
                        label = { Text("Last Name") },
                        readOnly = true, // Makes it non-editable

                        trailingIcon = {
                            IconButton(onClick = dropUnlessResumed { onEditLastNameNavigateUp() }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_edit), // Edit icon as an example
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

                    // About Input
                    OutlinedTextField(
                        value = about ?: "", // Bind this from your ViewModel
                        onValueChange = { },
                        label = { Text("About") },
                        readOnly = true, // Makes it non-editable
                        visualTransformation = if (about.isNullOrEmpty())
                            PlaceholderTransformation(" ")
                        else VisualTransformation.None,

                        trailingIcon = {
                            IconButton(onClick = dropUnlessResumed {
                                onEditAboutNavigateUp()
                            }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_edit), // Edit icon as an example
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

                    // Contact Information Section
                    Text(
                        text = "Contact",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Email Input
                    OutlinedTextField(
                        value = email, // Bind this from your ViewModel
                        onValueChange = { },
                        label = { Text("Email") },
                        readOnly = true, // Makes it non-editable
                        visualTransformation = if (email.isEmpty())
                            PlaceholderTransformation(" ")
                        else VisualTransformation.None,
                        trailingIcon = {
                            IconButton(onClick = dropUnlessResumed { onEditEmailNavigateUp() }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_edit), // Edit icon as an example
                                    contentDescription = "Edit icon"
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth(),
                        singleLine = true
                    )

                }
            } ?: run {
                CircularProgressIndicatorLegacy(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
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
