package com.lts360.compose.ui.auth


import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.dropUnlessResumed
import com.lts360.compose.ui.auth.viewmodels.ForgotPasswordViewModel
import com.lts360.compose.ui.main.viewmodels.ForgotPasswordProtectedViewModel
import com.lts360.compose.ui.services.manage.ErrorText
import com.lts360.compose.utils.NavigatorOutlinedCard
import com.lts360.compose.utils.NavigatorSubmitButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun ForgotPasswordScreen(
    viewModel: ForgotPasswordViewModel = hiltViewModel(),
    onNavigateUp: (String) -> Unit) {

    val context = LocalContext.current
    val isLoading by viewModel.loadingState.collectAsState()
    val email by viewModel.email.collectAsState()
    val emailError by viewModel.emailError.collectAsState()

    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    ForgotPasswordContent(isLoading = isLoading,
        email = email,
        emailError = emailError, onEmailChanged = {
            viewModel.onEmailChanged(it)
        }) {

        if (viewModel.validateEmail(email)) {

            coroutineScope.launch {
                focusManager.clearFocus()
                delay(300)
            }

            viewModel.onValidateEmailForgotPassword(email,
                onSuccess = { email ->
                    onNavigateUp(email)
                }) {
                Toast.makeText(context, it, Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreenProtected(
    onNavigateUp: (String) -> Unit,
    onPopStack: () -> Unit,
    viewModel: ForgotPasswordProtectedViewModel = hiltViewModel(),

    ) {

    val userId = viewModel.userId

    val email by viewModel.email.collectAsState()
    val context = LocalContext.current
    val isLoading by viewModel.loadingState.collectAsState()

    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    Box {

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
                            text = "Forgot Password",
                            style = MaterialTheme.typography.titleMedium,
                        )
                    })
            }
        ) { paddingValues ->

            Surface(modifier = Modifier.padding(paddingValues)) {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {

                    Text(
                        text = "Choose to Proceed",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    NavigatorOutlinedCard (
                        isLoading,
                        Modifier.fillMaxWidth(),
                        onCardClicked = {
                            scope.launch {
                                focusManager.clearFocus()
                            }
                            viewModel.onValidateEmailForgotPasswordProtected(
                                userId,
                                email,
                                onSuccess = { email, message ->
                                    onNavigateUp(email)
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

                                }) {
                                Toast.makeText(context, it, Toast.LENGTH_SHORT)
                                    .show()
                            }
                        },
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {

                            Image(Icons.Default.Email, contentDescription = null,
                                modifier = Modifier.size(56.dp),
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface))

                            Column(
                                modifier = Modifier
                                    .wrapContentHeight()
                                    .padding(horizontal = 8.dp)
                            ) {
                                Text(
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    text = "By Email")
                                Text(
                                    text = "Change password by verifying the email address of the account.")
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    text = email,
                                )

                            }
                        }
                    }
                }


            }
        }

        if (isLoading) {
            LoadingDialog()
        }
    }

}


@Composable
fun ForgotPasswordContent(
    isLoading: Boolean,
    email: String,
    emailError: String?,
    onEmailChanged: (String) -> Unit,
    onNextButtonClicked: () -> Unit,

    ) {

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        // Use Scrollable Column to mimic ScrollView
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Title Text
            Text(
                text = "Forgot Password",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Subtitle Text
            Text(
                text = "Kindly, enter your email of associated account to proceed.",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            // TextInput equivalent in Jetpack Compose using OutlinedTextField
            OutlinedTextField(
                value = email,
                onValueChange = {
                    onEmailChanged(it)
                },
                label = { Text("Username/Email") },
                modifier = Modifier
                    .fillMaxWidth(),
                singleLine = true,
                isError = emailError != null
            )


            // Display error message if there's an error
            emailError?.let {
                ErrorText(it)
            }

            Spacer(modifier = Modifier.height(16.dp))
            // Next Button


            NavigatorSubmitButton(isLoading, onNextButtonClicked = {
                onNextButtonClicked()

            }, content = {

                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White, // Change this to any color you prefer
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(ButtonDefaults.IconSize),
                    )
                } else {
                    Text(
                        "Next",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
            },Modifier.fillMaxWidth())



        }

        if (isLoading) {
            LoadingDialog()
        }
    }

}


