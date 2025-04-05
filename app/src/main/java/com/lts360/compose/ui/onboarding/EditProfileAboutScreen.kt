package com.lts360.compose.ui.onboarding

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.dropUnlessResumed
import com.lts360.R
import com.lts360.compose.transformations.PlaceholderTransformation
import com.lts360.compose.ui.auth.LoadingDialog
import com.lts360.compose.ui.onboarding.viewmodels.EditProfileAboutViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun EditProfileAboutScreen(
    onPopBackStack: () -> Unit,
    onUpdateSuccess: () -> Unit={},
    onSkipNowNavigateUp: () -> Unit={},
    viewModel: EditProfileAboutViewModel = hiltViewModel()

) {
    val userId = viewModel.userId
    val type = viewModel.type

    val context = LocalContext.current

    val about by viewModel.about.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val aboutError by viewModel.aboutError.collectAsState()


    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()


    EditProfileAboutContent(
        type = type,
        about = about,
        aboutError = aboutError,
        onAboutChanged = { viewModel.onAboutChanged(it) },
        isLoading = isLoading,
        onAboutChangeClicked = {


            if (viewModel.validateAbout()) {
                scope.launch {
                    focusManager.clearFocus()
                    delay(300)
                }
                viewModel.onUpdateAbout(userId, about, onSuccess = {

                    Toast.makeText(
                        context,
                        it,
                        Toast.LENGTH_SHORT
                    ).show()
                    onUpdateSuccess()

                }) {
                    Toast.makeText(context, it, Toast.LENGTH_SHORT)
                        .show()
                }

            }

        },
        {
            onSkipNowNavigateUp()
        },
        onPopBackStack
    )

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileAboutContent(
    type: String?,
    about: String,
    aboutError: String?,
    onAboutChanged: (String) -> Unit,
    isLoading: Boolean,
    onAboutChangeClicked: () -> Unit,
    onSkipNowNavigateUp: () -> Unit,
    onPopBackStack: () -> Unit
) {


    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()

    ) {

        Scaffold(

            topBar = {

                if (type == "complete_about") {
                    TopAppBar(
                        navigationIcon = {
                            IconButton(onClick = dropUnlessResumed { onPopBackStack() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back Icon"
                                )
                            }
                        },
                        title = {
                            Text(
                                text = "Edit About",
                                style = MaterialTheme.typography.titleMedium
                            )

                        }
                    )
                }
            }

        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .verticalScroll(rememberScrollState())
            ) {


                if (type != "complete_about") {
                    Text(
                        text = "Complete About",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                }


                OutlinedTextField(
                    isError = aboutError != null || about.length > 160,
                    value = about,
                    onValueChange = { onAboutChanged(it) },
                    label = { Text("About") },
                    modifier = Modifier
                        .fillMaxWidth(),
                    visualTransformation = if (about.isEmpty())
                        PlaceholderTransformation(" ")
                    else VisualTransformation.None,
                    minLines = 8,
                    textStyle = TextStyle(fontSize = 14.sp)
                )

                if (aboutError != null || about.length > 160) {
                    Text(
                        text = "Limit: ${about.length}/${160}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(
                            horizontal = 16.dp,
                            vertical = 4.dp
                        ) // Adjust padding as needed
                    )
                }

                // Display error message if there's an error
                aboutError?.let {

                    Text(
                        text = it,
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(
                            horizontal = 16.dp,
                            vertical = 4.dp
                        ) // Adjust padding as needed
                    )
                }


                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    enabled = !isLoading,

                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.colorPrimary),
                        disabledContainerColor = colorResource(id = R.color.colorPrimary)

                    ),
                    onClick = {
                        onAboutChangeClicked()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White, // Change this to any color you prefer
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(ButtonDefaults.IconSize),
                        )

                    } else {
                        Text(
                            text = "Update About",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .padding(horizontal = 8.dp) // Horizontal padding for the text
                        )
                    }


                }

                if (type != "complete_about") {
                    Spacer(modifier = Modifier.height(16.dp))


                    Row(
                        modifier = Modifier.fillMaxWidth(), // Fills the available space, but no vertical centering
                        verticalAlignment = Alignment.CenterVertically,// Centers all children horizontally
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = "Skip now",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .clickable {
                                    onSkipNowNavigateUp()
                                })
                    }


                }

            }


        }

        if (isLoading) {
            LoadingDialog()
        }

    }

}

