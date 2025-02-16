package com.lts360.compose.ui.auth

import android.widget.Toast
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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lts360.compose.ui.auth.viewmodels.ResetPasswordViewModel
import com.lts360.compose.ui.managers.UserSharedPreferencesManager
import com.lts360.compose.ui.services.manage.ErrorText
import com.lts360.compose.utils.NavigatorSubmitButton
import kotlinx.coroutines.launch


@Composable
fun ResetPasswordScreen(
    viewModel: ResetPasswordViewModel = hiltViewModel(),
    onNavigateDown: () -> Unit,
) {

    val context = LocalContext.current

    val accessToken = viewModel.accessToken
    val email = viewModel.email

    val password by viewModel.password.collectAsState()
    val confirmPassword by viewModel.confirmPassword.collectAsState()

    val passwordError by viewModel.passwordError.collectAsState()
    val confirmPasswordError by viewModel.confirmPasswordError.collectAsState()

    val isLoading by viewModel.isLoading.collectAsState()

    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()


    ResetPasswordContent(
        isLoading = isLoading,
        password = password,
        confirmPassword = confirmPassword,
        passwordError = passwordError,
        confirmPasswordError = confirmPasswordError,
        onPasswordChanged = { viewModel.onPasswordChanged(it) },
        onConfirmPasswordChanged = { viewModel.onConfirmPasswordChanged(it) }
    ) {

        if (viewModel.validatePasswords()) {
            scope.launch {
                focusManager.clearFocus()
            }

            viewModel.onResetPassword(
                accessToken = accessToken,
                email = email, password, onSuccess = {

                    onNavigateDown()

                }) {
                Toast.makeText(context, it, Toast.LENGTH_SHORT)
                    .show()
            }
        }

    }

}


@Composable
fun ResetPasswordScreenProtected(
    viewModel: ResetPasswordViewModel = hiltViewModel(),
    onNavigateDown: () -> Unit,
) {

    val context = LocalContext.current

    val userId = UserSharedPreferencesManager.userId

    val accessToken = viewModel.accessToken
    val email = viewModel.email

    val password by viewModel.password.collectAsState()
    val confirmPassword by viewModel.confirmPassword.collectAsState()

    val passwordError by viewModel.passwordError.collectAsState()
    val confirmPasswordError by viewModel.confirmPasswordError.collectAsState()

    val isLoading by viewModel.isLoading.collectAsState()

    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()


    ResetPasswordContent(
        isLoading = isLoading,
        password = password,
        confirmPassword = confirmPassword,
        passwordError = passwordError,
        confirmPasswordError = confirmPasswordError,
        onPasswordChanged = { viewModel.onPasswordChanged(it) },
        onConfirmPasswordChanged = { viewModel.onConfirmPasswordChanged(it) }
    ) {

        if (viewModel.validatePasswords()) {
            scope.launch {
                focusManager.clearFocus()
            }

            viewModel.onResetPasswordProtected(
                userId,
                accessToken = accessToken,
                email = email, password, onSuccess = {
                    Toast.makeText(context, it, Toast.LENGTH_SHORT)
                        .show()
                    onNavigateDown()

                }) {
                Toast.makeText(context, it, Toast.LENGTH_SHORT)
                    .show()
            }
        }

    }

}


@Composable
private fun ResetPasswordContent(
    isLoading: Boolean,
    password: String,
    confirmPassword: String,
    passwordError: String?,
    confirmPasswordError: String?,
    onPasswordChanged: (String) -> Unit,
    onConfirmPasswordChanged: (String) -> Unit,
    onRestClicked: () -> Unit,
) {


    // State for the password and confirm password visibility
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }


    // Vertical scrollable content

    Surface {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
                .imePadding()
        ) {
            // "Reset Password" Title
            Text(
                text = "Reset Password",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            // "Now, create new password..." Text
            Text(
                text = "Now, create new password to set.",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))

            // Password Input
            OutlinedTextField(
                value = password,
                onValueChange = { onPasswordChanged(it) },
                label = { Text("Password") },
                isError = passwordError != null,

                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide Password" else "Show Password",
                            tint = if (confirmPasswordError != null)
                                MaterialTheme.colorScheme.error else {
                                MaterialTheme.colorScheme.onSurface // Color when there is an error
                            }
                        )
                    }
                },

                modifier = Modifier
                    .fillMaxWidth()
            )

            passwordError?.let {
                ErrorText(it)
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Confirm Password Input
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { onConfirmPasswordChanged(it) },
                label = { Text("Confirm Password") },
                isError = confirmPasswordError != null,
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            imageVector = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (confirmPasswordVisible) "Hide Confirm Password" else "Show Confirm Password",

                            tint = if (confirmPasswordError != null)
                                MaterialTheme.colorScheme.error else {
                                MaterialTheme.colorScheme.onSurface // Color when there is an error
                            }


                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
            )

            confirmPasswordError?.let {
                ErrorText(it)
            }

            Spacer(modifier = Modifier.height(16.dp))


            NavigatorSubmitButton(isLoading, onNextButtonClicked = {
                onRestClicked()

            }, content = {

                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White, // Change this to any color you prefer
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(ButtonDefaults.IconSize),
                    )

                } else {
                    Text(
                        text = "Reset Password",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .padding(horizontal = 16.dp) // Horizontal padding for the text
                    )
                }

            },
                modifier = Modifier
                    .fillMaxWidth()
                )

        }
    }
}




