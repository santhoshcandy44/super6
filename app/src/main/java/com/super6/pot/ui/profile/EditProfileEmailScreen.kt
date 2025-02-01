package com.super6.pot.ui.profile

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.super6.pot.R
import com.super6.pot.ui.PlaceholderTransformation
import com.super6.pot.ui.auth.LoadingDialog
import com.super6.pot.ui.profile.viewmodels.EditEmailViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileEmailScreen(
    onEditEmailOtpVerificationNavigateUp:(String)->Unit,
    onPopBackStack:() -> Unit,
    viewModel: EditEmailViewModel = hiltViewModel()

    ) {


    val context = LocalContext.current


    val email by viewModel.email.collectAsState()
    val isLoadingState by viewModel.isLoading.collectAsState()
    val emailError by viewModel.emailError.collectAsState()


    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()


    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Scaffold(

            topBar = {
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Edit Email",
                                style  = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                )
            }

        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {


                OutlinedTextField(
                    isError = emailError != null,
                    value = email,
                    onValueChange = { viewModel.onEmailChanged(it) },
                    label = { Text("Email") },
                    modifier = Modifier
                        .fillMaxWidth(),
                    maxLines = 1,
                    singleLine = true,
                    visualTransformation = if (email.isEmpty())
                        PlaceholderTransformation(" ")
                    else VisualTransformation.None,
                    textStyle = TextStyle(fontSize = 14.sp)
                )

                // Display error message if there's an error
                emailError?.let {

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

                Spacer(Modifier.height(16.dp))

                Button(
                    enabled = !isLoadingState,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.colorPrimary),
                        disabledContainerColor = colorResource(id = R.color.colorPrimary)

                    ),
                    onClick = {

                        if (viewModel.validateEmail(email)) {

                            scope.launch {
                                focusManager.clearFocus()
                                delay(300)

                            }
                            viewModel.onChangeEmailValidate(
                                viewModel.userId,
                                email,
                                onSuccess = {
                                    Toast.makeText(context, it, Toast.LENGTH_SHORT)
                                        .show()
                                    onEditEmailOtpVerificationNavigateUp(email)
                                }) {
                                Toast.makeText(context, it, Toast.LENGTH_SHORT)
                                    .show()
                            }

                        }

                    },
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    if (isLoadingState) {
                        CircularProgressIndicator(
                            color = Color.White, // Change this to any color you prefer
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(ButtonDefaults.IconSize),
                        )

                    } else {
                        Text(
                            text = "Update Email",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }


                }

            }



        }


        if (isLoadingState) {
            LoadingDialog()
        }

    }
}
