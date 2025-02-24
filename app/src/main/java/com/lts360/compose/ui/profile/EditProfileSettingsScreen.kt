package com.lts360.compose.ui.profile

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
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
import androidx.lifecycle.compose.dropUnlessResumed
import com.lts360.R
import com.lts360.compose.transformations.PlaceholderTransformation
import com.lts360.compose.ui.common.CircularProgressIndicatorLegacy
import com.lts360.compose.ui.profile.viewmodels.ProfileSettingsViewModel
import com.lts360.compose.ui.services.manage.ErrorText
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileSettingsScreen(
    onEditFirstNameNavigateUp: () -> Unit,
    onEditLastNameNavigateUp: () -> Unit,
    onEditAboutNavigateUp: () -> Unit,
    onEditEmailNavigateUp: () -> Unit,
    onPopStack:()-> Unit,
    viewModel: ProfileSettingsViewModel
) {

    val userId = viewModel.userId

    val purpleGradientBrush = Brush.linearGradient(
        colors = listOf(Color(0xFF6200EE), Color(0xFF9747ff), Color(0xFFBB86FC))
    )
    val userProfile by viewModel.userProfile.collectAsState()


    val profilePicBitmap by viewModel.profileImageBitmap.collectAsState()
    val profileCompletionPercentage by viewModel.profileCompletionPercentage.collectAsState()
    val healthStatus by viewModel.profileHealthStatus.collectAsState()

    val isProfilePicLoading by viewModel.isProfilePicLoading.collectAsState()

    val context = LocalContext.current


    var isPickerLaunched by rememberSaveable { mutableStateOf(false) }

    //Register the image picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            // Convert the URI to a file and upload the image
            val imageFile = File(getRealPathFromURI(context, selectedUri))
            val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            val imagePart =
                MultipartBody.Part.createFormData("profile_pic", imageFile.name, requestFile)
            viewModel.onUploadImage(userId, imagePart, onSuccess = {
                Toast.makeText(context, it, Toast.LENGTH_SHORT)
                    .show()
            }) {
                Toast.makeText(context, it, Toast.LENGTH_SHORT)
                    .show()
            }

        }

        isPickerLaunched = false

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
            // AppBarLayout equivalent using TopAppBar in Compose


            userProfile?.let {  nonNullUserProfile ->

                val firstName = nonNullUserProfile.first_name
                val lastName = nonNullUserProfile.last_name ?:""
                val about = nonNullUserProfile.about
                val aboutError = if(about.isNullOrEmpty()) "About is not yet updated" else null
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
                                    if (isPickerLaunched)
                                        return@IconButton
                                    isPickerLaunched = true
                                    imagePickerLauncher.launch("image/*")

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
                                color = MaterialTheme.colorScheme.primary)

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
            }?:run {
                CircularProgressIndicatorLegacy(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary)
            }




        }
    }

}


private fun getRealPathFromURI(context: Context, uri: Uri): String {
    val projection = arrayOf(MediaStore.Images.Media.DATA)
    val cursor = context.contentResolver.query(uri, projection, null, null, null)
    if (cursor != null) {
        val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        val filePath = cursor.getString(columnIndex)
        cursor.close()
        return filePath
    }
    return uri.path ?: ""
}
