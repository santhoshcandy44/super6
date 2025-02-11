package com.super6.pot.compose.ui.profile

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.dropUnlessResumed
import com.super6.pot.compose.ui.auth.LoadingDialog
import com.super6.pot.compose.ui.profile.viewmodels.ChangePasswordViewModel
import com.super6.pot.compose.ui.services.manage.ErrorText
import com.super6.pot.compose.utils.NavigatorSubmitButton
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    onForgotPasswordNavigateUp: () -> Unit,
    onNavigatePop: () -> Unit,
    onPopStack : () -> Unit,
    viewModel: ChangePasswordViewModel = hiltViewModel()) {


    val userId = viewModel.userId

    val currentPassword by viewModel.currentPassword.collectAsState()
    val newPassword by viewModel.password.collectAsState()

    val confirmPassword by viewModel.confirmPassword.collectAsState()

    val currentPasswordError by viewModel.currentPasswordError.collectAsState()
    val passwordError by viewModel.passwordError.collectAsState()
    val confirmPasswordError by viewModel.confirmPasswordError.collectAsState()

    val loading by viewModel.isLoading.collectAsState()

    val context = LocalContext.current

    val scope = rememberCoroutineScope()

    val focusManager = LocalFocusManager.current

    Box(modifier = Modifier.fillMaxSize().imePadding()){
        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = dropUnlessResumed { onPopStack() }) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back Icon")
                        }
                    },
                    title = {
                        Text(text = "Manage Password",
                            style = MaterialTheme.typography.titleMedium)
                    }
                )

            }
        ) { contentPadding ->
            Surface(modifier = Modifier.padding(contentPadding)) {
                ChangePasswordContent(
                    currentPassword,
                    newPassword,
                    confirmPassword,
                    currentPasswordError,
                    passwordError,
                    confirmPasswordError,
                    loading,
                    {
                        viewModel.onCurrentPasswordChanged(it)
                    }, {
                        viewModel.onPasswordChanged(it)

                    }, {
                        viewModel.onConfirmPasswordChanged(it)
                    },

                    onChangePasswordClicked = {
                        if (viewModel.validatePasswords()) {
                            scope.launch {
                                focusManager.clearFocus()
                            }

                            viewModel.onChangePassword(
                                userId,
                                currentPassword, newPassword, onSuccess = {
                                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                                    onNavigatePop()
                                }) { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
                        }
                    }
                ) {
                    onForgotPasswordNavigateUp()
                }
            }

        }

        if (loading) {
            LoadingDialog()
        }
    }

}


@Composable
fun ChangePasswordContent(
    currentPassword: String,
    newPassword: String,
    confirmPassword: String,
    currentPasswordError: String?,
    passwordError: String?,
    confirmPasswordError: String?,
    loading: Boolean,
    onCurrentPasswordChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onConfirmPasswordChanged: (String) -> Unit,
    onChangePasswordClicked: () -> Unit,
    onForgotPasswordNavigateUp: () -> Unit,

    ) {


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
    ) {

        Title(text = "Change Password")

        Spacer(modifier = Modifier.height(8.dp))

        PasswordInputField(
            label = "Current Password",
            value = currentPassword,
            onValueChange = { onCurrentPasswordChanged(it) },
            isError = currentPasswordError != null,
            errorMessage = currentPasswordError
        )

        Spacer(modifier = Modifier.height(8.dp))

        PasswordInputField(
            label = "New Password",
            value = newPassword,
            onValueChange = { onPasswordChanged(it) },
            isError = passwordError != null,
            errorMessage = passwordError
        )
        Spacer(modifier = Modifier.height(8.dp))


        PasswordInputField(
            label = "Confirm Password",
            value = confirmPassword,
            onValueChange = { onConfirmPasswordChanged(it) },
            isError = confirmPasswordError != null,
            errorMessage = confirmPasswordError
        )

        Spacer(modifier = Modifier.height(16.dp))

        ChangePasswordButton(isLoading = loading) {

            onChangePasswordClicked()

        }
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Don't Remember Password?",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clickable {
                    onForgotPasswordNavigateUp()
                }
        )
    }
}


@Composable
fun Title(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun PasswordInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean,
    errorMessage: String?,
    disablePasswordToggle: Boolean = false, // New parameter to disable toggle

) {

    var passwordVisible by remember { mutableStateOf(false) }


    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        isError = isError,

        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            if (!disablePasswordToggle) { // Only show toggle if not disabled
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (passwordVisible) "Hide Password" else "Show Password"
                    )
                }
            }
        },
        modifier = modifier
            .fillMaxWidth()
    )

    if (isError) {
        errorMessage?.let {
            ErrorText(it)
        }
    }

}

@Composable
fun ChangePasswordButton(
    isLoading: Boolean,
    onClick: () -> Unit,
) {


    NavigatorSubmitButton(isLoading, onNextButtonClicked = onClick, content = {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White, // Change this to any color you prefer
                strokeWidth = 2.dp,
                modifier = Modifier.size(ButtonDefaults.IconSize),
            )

        } else {
            Text(
                text = "Change Password",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    },  modifier = Modifier
        .fillMaxWidth())
}



