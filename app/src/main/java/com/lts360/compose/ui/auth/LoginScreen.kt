package com.lts360.compose.ui.auth

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.lts360.R
import com.lts360.compose.dropUnlessResumedV2
import com.lts360.compose.ui.auth.viewmodels.LogInViewModel
import com.lts360.compose.ui.main.MainActivity
import com.lts360.compose.ui.theme.icons
import com.lts360.compose.utils.NavigatorSubmitButton
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginScreen(
    viewModel: LogInViewModel = koinViewModel(),
    onNavigateUpForgotPassword: () -> Unit,
    onNavigateUpCreateAccount: () -> Unit,
) {

    val lifecycleOwner = LocalLifecycleOwner.current


    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()


    val emailError by viewModel.emailError.collectAsState()
    val passwordError by viewModel.passwordError.collectAsState()

    val isLoading by viewModel.isLoading.collectAsState()
    val context = LocalContext.current


    var isClickable by remember { mutableStateOf(true) }


    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()



    Box(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        // Scrollable Column equivalent to ScrollView
        Column(

            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .align(Alignment.Center)
                .padding(top = 16.dp)
                .padding(horizontal = 16.dp),
        ) {


            LongInTextFieldWithLabel(
                value = email,
                onValueChange = { viewModel.onEmailChanged(it) },
                keyboardOptions = KeyboardOptions.Default.copy(
                    autoCorrectEnabled = false,
                    keyboardType = KeyboardType.Email
                ),
                isError = emailError != null,
                errorMessage = emailError
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Password Input
            LongInPasswordFieldWithLabel(
                value = password,
                onValueChange = { viewModel.onPasswordChanged(it) },
                isError = passwordError != null,
                errorMessage = passwordError
            )
            Spacer(modifier = Modifier.height(8.dp))


            NavigatorSubmitButton(isLoading, onNextButtonClicked = {
                if (viewModel.validateFields()) {

                    coroutineScope.launch {
                        focusManager.clearFocus()
                    }

                    viewModel.onLegacyEmailLogin(email, password, {
                        context.startActivity(
                            Intent(context, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                            },
                            ActivityOptions.makeCustomAnimation(
                                context,
                                R.anim.slide_in_right,
                                R.anim.slide_out_left
                            ).toBundle()
                        )
                        (context as Activity).finishAffinity()

                    }) {
                        Toast.makeText(context, it, Toast.LENGTH_SHORT)
                            .show()
                    }

                }

            }, content = {

                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White, // Change this to any color you prefer
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(ButtonDefaults.IconSize),
                    )

                } else {
                    Text(
                        "Sign in",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
            },Modifier.fillMaxWidth().padding(vertical = 4.dp))




            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // Forgot password TextView
                Text(
                    text = "Forgot Password?",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .clickable {
                            dropUnlessResumedV2(lifecycleOwner){
                                focusManager.clearFocus()
                                onNavigateUpForgotPassword()
                            }
                        },
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // "Don't have an account?" with "Create Account" TextView
                Row(
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Don't have an account?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Create Account",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.clickable {
                            dropUnlessResumedV2(lifecycleOwner){
                                onNavigateUpCreateAccount()
                            }
                        }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    style = MaterialTheme.typography.bodyMedium,
                    text = "Or",
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Google Sign In Button (Image)
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {


                GoogleButton(
                    isClickable = isClickable,
                    onClicked = {
                        coroutineScope.launch {
                            focusManager.clearFocus()
                        }
                        isClickable = false
                        viewModel.onGoogleSignInOAuth(context,
                            onSuccess = { idToken ->
                                viewModel.onGoogleSignIn(idToken, onSuccess = {

                                    isClickable = true

                                    context.startActivity(
                                        Intent(context, MainActivity::class.java),
                                        ActivityOptions.makeCustomAnimation(
                                            context,
                                            R.anim.slide_in_right,
                                            R.anim.slide_out_left
                                        ).toBundle()
                                    )
                                    (context as Activity).finishAffinity()

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
                    })

            }
            Spacer(modifier = Modifier.height(8.dp))
        }


        if (isLoading) {
            LoadingDialog()
        }
    }

}




/*@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTextField(
    text: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true,
    enabled: Boolean = true,
    isError: Boolean = false,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    BasicTextField(
        value = text,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp), // Adjust height as needed
        singleLine = singleLine,
        interactionSource = interactionSource,
        keyboardOptions = keyboardOptions, // Pass keyboard options
        textStyle = TextStyle.Default.copy(color = MaterialTheme.colorScheme.onSurface),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),

        ) { innerTextField ->
        OutlinedTextFieldDefaults.DecorationBox(
            value = text,
            innerTextField = innerTextField,
            enabled = enabled,
            singleLine = singleLine,
            visualTransformation = VisualTransformation.None,
            interactionSource = interactionSource,
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_login_user),
                    contentDescription = "User Icon",
                    tint = MaterialTheme.colorScheme.onSurface // Use the appropriate tint color
                )
            },
            label = {
                Text(label) // Label for the TextField
            },
            contentPadding = TextFieldDefaults.contentPaddingWithoutLabel(
                top = 0.dp,
                bottom = 0.dp
            ),
            isError = isError // Handle error state
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomPasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isPasswordVisible: Boolean,
    onTogglePasswordVisibility: () -> Unit,
    isError: Boolean = false,
    modifier: Modifier = Modifier
) {
    var interactionSource = remember { MutableInteractionSource() }

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        interactionSource = interactionSource,
        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation()
    ) { innerTextField ->
        OutlinedTextFieldDefaults.DecorationBox(
            value = value,
            innerTextField = innerTextField,
            enabled = true,
            singleLine = true,
            visualTransformation = VisualTransformation.None,
            interactionSource = interactionSource,
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_login_password),
                    contentDescription = "Password Icon",
                    tint = MaterialTheme.colorScheme.onSurface // Use the appropriate tint color
                )
            },
            label = { Text(label) },
            isError = isError,
            trailingIcon = {
                IconButton(onClick = onTogglePasswordVisibility) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                        tint = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            contentPadding = TextFieldDefaults.textFieldWithoutLabelPadding(
                top = 0.dp,
                bottom = 0.dp
            )
        )
    }
}*/




@Composable
private fun LongInTextFieldWithLabel(
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean,
    errorMessage: String?,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {

    // Username TextInputLayout
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Email") },
        keyboardOptions = keyboardOptions,

        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        leadingIcon = {
            Icon(
                painter = painterResource(MaterialTheme.icons.logInUser),
                contentDescription = "User Icon",
                tint = MaterialTheme.colorScheme.onSurface // Use the appropriate tint color
            )
        },
        singleLine = true,
        isError = isError // You can customize error handling
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
private fun LongInPasswordFieldWithLabel(
    value: String,
    onValueChange: (String) -> Unit,
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
        leadingIcon = {
            Icon(
                painter = painterResource(MaterialTheme.icons.logInPassword),
                contentDescription = "User Icon",
                tint = MaterialTheme.colorScheme.onSurface // Use the appropriate tint color
            )
        },
        label = { Text("Password") },
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
                    // Use the appropriate tint color
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

@Composable
fun GoogleButton(
    modifier: Modifier = Modifier,
    text: String = "Sign In with Google",
    icon: Int = R.drawable.ic_google_logo,
    shape: Shape = MaterialTheme.shapes.medium,
    borderColor: Color = Color.LightGray,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    onClicked: () -> Unit,
    isClickable: Boolean = false,
) {

    Surface(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable(enabled = isClickable) {
                if (isClickable) {
                    onClicked()
                }
            },
        shape = shape,
        border = BorderStroke(width = 1.dp, color = borderColor),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier
                .padding(
                    start = 12.dp,
                    end = 16.dp,
                    top = 12.dp,
                    bottom = 12.dp
                )
                .animateContentSize(
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = LinearOutSlowInEasing
                    )
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = "Google Button",
                tint = Color.Unspecified
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

        }
    }
}







