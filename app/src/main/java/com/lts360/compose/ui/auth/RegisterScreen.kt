package com.lts360.compose.ui.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lts360.R
import com.lts360.components.utils.openUrlInCustomTab
import com.lts360.compose.ui.auth.viewmodels.RegisterViewModel
import com.lts360.compose.ui.common.CircularProgressIndicatorLegacy
import com.lts360.compose.ui.theme.customColorScheme
import com.lts360.compose.utils.NavigatorSubmitButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel = hiltViewModel(),
    onNavigateUpRegisterEmailOtpVerification: (String, String, String, String, AccountType) -> Unit,
    onNavigateUpOnBoarding: () -> Unit,
) {


    val context = LocalContext.current

    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    var isClickable by remember { mutableStateOf(true) }

    val accountType = viewModel.accountType


    val firstName by viewModel.firstName.collectAsState()
    val lastName by viewModel.lastName.collectAsState()
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val confirmPassword by viewModel.confirmPassword.collectAsState()
    val isTermsAccepted by viewModel.isTermsAccepted.collectAsState()
    val loading by viewModel.isLoading.collectAsState()

    // Collecting error messages
    val firstNameError by viewModel.firstNameError.collectAsState()
    val lastNameError by viewModel.lastNameError.collectAsState()
    val emailError by viewModel.emailError.collectAsState()
    val passwordError by viewModel.passwordError.collectAsState()
    val confirmPasswordError by viewModel.confirmPasswordError.collectAsState()
    val termsError by viewModel.termsError.collectAsState()

    RegisterContent(
        firstName = firstName,
        lastName = lastName,
        email = email,
        password = password,
        confirmPassword = confirmPassword,
        isTermsAccepted = isTermsAccepted,
        loading = loading,
        firstNameError = firstNameError,
        lastNameError = lastNameError,
        emailError = emailError,
        passwordError = passwordError,
        confirmPasswordError = confirmPasswordError,
        termsError = termsError,
        onFirstNameChanged = { viewModel.onFirstNameChanged(it) },
        onLastNameChanged = { viewModel.onLastNameChanged(it) },
        onEmailChanged = { viewModel.onEmailChanged(it) },
        onPasswordChanged = { viewModel.onPasswordChanged(it) },
        onConfirmPasswordChanged = { viewModel.onConfirmPasswordChanged(it) },
        onTermsAcceptedChanged = { viewModel.onTermsAcceptedChanged(it) },
        onTermsAndConditionsClicked = { url ->
            openUrlInCustomTab(
                context,
                url
            )
        },
        onRegisterClicked = {

            if (viewModel.validateFields()) {
                scope.launch {
                    focusManager.clearFocus()
                    delay(300)
                }

                viewModel.onLegacyEmailSignUp(email, onSuccess = {
                    onNavigateUpRegisterEmailOtpVerification(
                        firstName,
                        lastName,
                        email,
                        password,
                        accountType
                    )
                }) {
                    Toast.makeText(context, it, Toast.LENGTH_SHORT)
                        .show()
                }
            }
        },
        onGoogleSignInClicked = {

            scope.launch {
                focusManager.clearFocus()
                isClickable = false
                delay(1000)
                viewModel.onGoogleSignUpOAuth(context,
                    onSuccess = { idToken ->
                    viewModel.onGoogleSignUp(idToken, accountType.name, onSuccess = {
                        isClickable = true
                        onNavigateUpOnBoarding()
                    }) {

                        isClickable = true

                        Toast.makeText(context, it, Toast.LENGTH_SHORT)
                            .show()
                    }

                }) {
                    viewModel.setLoading(false)
                    isClickable = true
                    Toast.makeText(context, it, Toast.LENGTH_SHORT)
                        .show()
                }
            }

        },
        isClickable = isClickable
    )
}


@Composable
private fun RegisterContent(
    firstName: String,
    lastName: String,
    email: String,
    password: String,
    confirmPassword: String,
    isTermsAccepted: Boolean,
    loading: Boolean,
    firstNameError: String?,
    lastNameError: String?,
    emailError: String?,
    passwordError: String?,
    confirmPasswordError: String?,
    termsError: String?,
    onFirstNameChanged: (String) -> Unit,
    onLastNameChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onConfirmPasswordChanged: (String) -> Unit,
    onTermsAcceptedChanged: (Boolean) -> Unit,
    onTermsAndConditionsClicked: (String) -> Unit,
    onRegisterClicked: () -> Unit,
    onGoogleSignInClicked: () -> Unit,
    isClickable: Boolean,
) {

    val context = LocalContext.current


    Surface(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "Sign Up",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // First Name Input
            TextFieldWithLabel(
                value = firstName,
                onValueChange = { onFirstNameChanged(it) },
                label = "First Name",
                isError = firstNameError != null,
                errorMessage = firstNameError
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Last Name Input
            TextFieldWithLabel(
                value = lastName,
                onValueChange = { onLastNameChanged(it) },
                label = "Last Name",
                isError = lastNameError != null,
                errorMessage = lastNameError
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Email Input
            TextFieldWithLabel(
                value = email,
                onValueChange = { onEmailChanged(it) },
                label = "Email",
                keyboardOptions = KeyboardOptions.Default.copy(
                    autoCorrectEnabled = false,
                    keyboardType = KeyboardType.Email
                ),
                isError = emailError != null,
                errorMessage = emailError
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Password Input
            PasswordFieldWithLabel(
                value = password,
                onValueChange = { onPasswordChanged(it) },
                label = "Password",
                isError = passwordError != null,
                errorMessage = passwordError
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Confirm Password Input
            PasswordFieldWithLabel(
                value = confirmPassword,
                onValueChange = { onConfirmPasswordChanged(it) },
                label = "Confirm Password",
                isError = confirmPasswordError != null,
                errorMessage = confirmPasswordError
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Terms and Conditions Checkbox

            Column {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = isTermsAccepted,
                        onCheckedChange = { onTermsAcceptedChanged(it) },

                        )
                    Text(
                        "I agree to the terms and conditions",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Display error message if there's an error
                termsError?.let {

                    Text(
                        text = it,
                        color = Color.Red,
                        modifier = Modifier.padding(
                            horizontal = 16.dp,
                            vertical = 4.dp
                        ) // Adjust padding as needed
                    )

                }

            }

            Spacer(modifier = Modifier.height(8.dp))

            // agreement
            Text(buildAnnotatedString {
                append("By checking agree to ")

                withLink(
                    LinkAnnotation.Url(
                        url = "https://saket.me/compose-custom-text-spans/",
                        styles = TextLinkStyles(
                            style = SpanStyle(
                                color = MaterialTheme.customColorScheme.linkColor,
                                textDecoration = TextDecoration.None
                            )
                        )

                    ) {

                        onTermsAndConditionsClicked(context.getString(R.string.terms_and_conditions))
                    }
                ) {
                    append("Terms and Conditions")
                }
            }, style = MaterialTheme.typography.bodyMedium)


            Spacer(modifier = Modifier.height(16.dp))


            NavigatorSubmitButton(loading, onNextButtonClicked = {
                onRegisterClicked()
            }, content = {
                if (loading) {
                    CircularProgressIndicator(
                        color = Color.White, // Change this to any color you prefer
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(ButtonDefaults.IconSize),
                    )

                } else {
                    Text(
                        text = "Register",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

            },Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(8.dp))

            // Or Text
            Text(
                text = "Or",
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(8.dp))


            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Google Sign Up Button


                GoogleButton(
                    text = "Sign Up with Google",
                    isClickable = isClickable,
                    onClicked = {
                        onGoogleSignInClicked()
                    })

            }
        }


     /*   if (loading) {
            LoadingDialog()
        }*/
    }

}


@Composable
fun LoadingDialog() {

    // Overlay background to dim the content behind the dialog
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(interactionSource = remember {
                MutableInteractionSource()
            }, indication = null) {} // Consume clicks
            .background(Color.Black.copy(alpha = 0.5f)) // Semi-transparent background
            .wrapContentSize(Alignment.Center) // Center the dialog
    ) {
        // Dialog content
        Box(
            modifier = Modifier
                .wrapContentSize()
                .background(Color.White, shape = RoundedCornerShape(8.dp)) // Rounded corners
                .padding(16.dp) // Padding for the dialog
        ) {
            CircularProgressIndicatorLegacy(
                modifier = Modifier
                    .padding(8.dp) // Padding around the indicator
            )
        }
    }
}



@Composable
fun TextFieldWithLabel(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isError: Boolean,
    errorMessage: String?,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    OutlinedTextField(
        isError = isError,
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        maxLines = 1,
        keyboardOptions = keyboardOptions
    )

    // Display error message if there's an error
    if (isError && errorMessage != null) {
        Text(
            text = errorMessage,
            color = Color.Red,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(start = 16.dp, top = 4.dp) // Adjust padding as needed
        )
    }
}


@Composable
fun PasswordFieldWithLabel(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isError: Boolean,
    errorMessage: String?,
) {
    // State for password visibility
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        keyboardOptions = KeyboardOptions.Default.copy(
            autoCorrectEnabled = false,
            capitalization = KeyboardCapitalization.None,
            // Optional: set imeAction if needed
            imeAction = ImeAction.Done
        ),
        label = { Text(label) },
        isError = isError,
        modifier = Modifier.fillMaxWidth(),
        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        singleLine = true,
        trailingIcon = {
            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                Icon(

                    imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = if (isPasswordVisible) "Hide password" else "Show password",

                    tint = if (errorMessage != null)
                        MaterialTheme.colorScheme.error else {
                        MaterialTheme.colorScheme.onSurface // Color when there is an error
                    }

                )
            }
        }
    )

    // Display error message if there's an error
    if (isError && errorMessage != null) {
        Text(
            text = errorMessage,
            color = Color.Red,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(start = 16.dp, top = 4.dp) // Adjust padding as needed
        )
    }
}
