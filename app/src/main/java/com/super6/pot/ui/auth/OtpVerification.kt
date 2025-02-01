package com.super6.pot.ui.auth

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.KeyboardBackspace
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.dropUnlessResumed
import com.super6.pot.R
import com.super6.pot.ui.NavigatorSubmitButton
import com.super6.pot.ui.auth.viewmodels.EmailOTPVerificationViewModel
import com.super6.pot.utils.LogUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpVerificationScreen(
    viewModel: EmailOTPVerificationViewModel,
    email: String,
    isLoading: Boolean,
    onResendOtpClicked: () -> Unit,
    onVerifyClicked: (String) -> Unit,
    onPopBackStack: () -> Unit
) {

    // Collecting state for the OTP fields
    val otpFields by viewModel.otpFields.collectAsState()

    // Collecting state for the timer
    val timeLeft by viewModel.timeLeft.collectAsState()

    // Collecting state for the resend button enable/disable status
    val resendEnabled by viewModel.resendEnabled.collectAsState()


    // Collecting state for the resend button enable/disable status
    val timerVisible by viewModel.timerVisible.collectAsState()


    // Function to check if all fields are filled
    val allOtpFieldsEntered = otpFields.all { it.text.isNotEmpty() }


    val focusedIndex by viewModel.focusedIndex.collectAsState()



    Box(modifier = Modifier.fillMaxSize()) {

        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(
                            modifier = Modifier.focusProperties { canFocus = false },
                            onClick = dropUnlessResumed
                            { onPopBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back Icon"
                            )
                        }

                    },

                    title = {
                        Text(
                            text = "OTP Sent via Email",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                )
            }

        ) { innerPadding ->

            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {


                OtpVerificationContent(
                    allOtpFieldsEntered = allOtpFieldsEntered,
                    timerVisible = timerVisible,
                    resendEnabled = resendEnabled,
                    isLoading = isLoading,
                    email = email,
                    otpFields = otpFields,
                    timeLeft = timeLeft,
                    updateDigit = { value ->
                        viewModel.updateDigit(value, viewModel.focusedIndex.value) {
                            viewModel.updateFocusedIndex(it)

                        }
                    },

                    onBackSpace = {
                        val i = viewModel.focusedIndex.value
                        if (i > 0 && otpFields[i].text.isEmpty()) {
                            viewModel.updateFocusedIndex(i - 1)
                        }


                        if (otpFields[i].text.isNotEmpty()) {
                            viewModel.onUpdateOtpField("", i)

                            if (i != 0) {
                                viewModel.updateFocusedIndex(i - 1)
                            }
                        }
                    },

                    onResendOtpClicked = {
                        onResendOtpClicked()
                    },
                    {
                        onVerifyClicked(otpFields.joinToString("") { it.text })
                    },
                    focusedIndex = focusedIndex,
                    onForwardFocusedIndex = {
                        val i = viewModel.focusedIndex.value

                        if (i < otpFields.size - 1) {
                            viewModel.updateFocusedIndex(i + 1)
                        }
                    },

                    updateFocusedIndex = {
                        viewModel.updateFocusedIndex(it)

                    },
                    onBackWardFocusedIndex = {

                        val i = viewModel.focusedIndex.value

                        if (i != 0) {
                            viewModel.updateFocusedIndex(i - 1)
                        }
                    }
                )
            }


        }
        if (isLoading) {
            LoadingDialog()
        }

    }


}

@Composable
private fun OtpVerificationContent(
    allOtpFieldsEntered: Boolean,
    timerVisible: Boolean,
    resendEnabled: Boolean,
    isLoading: Boolean,
    email: String,
    otpFields: List<TextFieldValue>,
    timeLeft: String,
    updateDigit: (String) -> Unit,
    onBackSpace: () -> Unit,
    onResendOtpClicked: () -> Unit,
    onVerifyClicked: () -> Unit,
    focusedIndex: Int,
    updateFocusedIndex: (Int) -> Unit,
    onForwardFocusedIndex: () -> Unit,
    onBackWardFocusedIndex: () -> Unit,
) {


    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .weight(1f) // Take up available space above the bottom box
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp)
                .padding(horizontal = 16.dp)
        ) {


            Row(
                modifier = Modifier.fillMaxWidth(), // Fill available width
                verticalAlignment = Alignment.CenterVertically // Align items vertically in the center
            ) {
                // Image
                Image(
                    painter = painterResource(id = R.drawable.email_otp_verification),
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape) // Make the image rounded
                )

                Spacer(modifier = Modifier.width(8.dp)) // Use width instead of height to space horizontally

                // Title with weight
                Text(
                    text = "Email Verification",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .weight(1f) // Take remaining space in the row
                        .padding(vertical = 4.dp),
                    textAlign = TextAlign.Start // Align text to the start
                )
            }

            Spacer(modifier = Modifier.height(16.dp))


            OutlinedCard(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
                shape = RectangleShape,
                modifier = Modifier
                    .fillMaxWidth()

            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    OtpEmailVerification(email)

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(
                            2.dp,
                            Alignment.CenterHorizontally
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {


                        otpFields.forEachIndexed { i, _ ->
                            Box(
                                contentAlignment = Alignment.Center, // Center the text inside the box
                                modifier = Modifier
                                    .sizeIn(
                                        maxWidth = 40.dp,
                                        maxHeight = 40.dp
                                    ) // Size of the box, adjust based on your design
                                    .weight(1f, fill = false)
                                    .aspectRatio(1f)
                                    .graphicsLayer {

                                        scaleX = if (focusedIndex == i) 1.10f else 1f
                                        scaleY = if (focusedIndex == i) 1.10f else 1f

                                    }

                                    .border(
                                        BorderStroke(
                                            2.dp,
                                            if (focusedIndex == i) MaterialTheme.colorScheme.primary else Color.LightGray
                                        ),
                                        shape = RoundedCornerShape(4.dp) // Optional: Rounded corners
                                    )
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember {
                                            MutableInteractionSource()
                                        }
                                    ) {
                                        // Handle click event to focus the box
                                        updateFocusedIndex(i)
                                    }
                            ) {


                                // Display the OTP digit, or show a placeholder if it's empty
                                Text(
                                    text = otpFields[i].text.ifEmpty { "_" }, // Placeholder when empty
                                    style = TextStyle(
                                        textAlign = TextAlign.Center,
                                        fontSize = 24.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }


                            // Optional spacing between the input fields
                            if (i < otpFields.size - 1) {
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                        }

                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Verify Button


                    NavigatorSubmitButton(
                        isLoading || !allOtpFieldsEntered,
                        onNextButtonClicked = {
                            onVerifyClicked()
                        },

                        content = {
                            if (isLoading && allOtpFieldsEntered) {

                                CircularProgressIndicator(
                                    color = Color.White, // Change this to any color you prefer
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(ButtonDefaults.IconSize),
                                )

                            } else {
                                Text(
                                    "Verify",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (allOtpFieldsEntered) Color.White else Color.LightGray
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusProperties { canFocus = false },
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Timer and Resend OTP Section
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {

                        if (timerVisible) {
                            Row(

                                modifier = Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(8.dp) // Add rounded corners with a radius of 8dp
                                    )
                                    .padding(8.dp), // Optional padding for spacing inside the box
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center // Align the content horizontally in the center
                            ) {

                                // Combine both texts into a single spannable string
                                Text(
                                    text = buildAnnotatedString {
                                        append("Request new OTP after ")
                                        withStyle(style = SpanStyle(color = Color.Red)) {
                                            append(timeLeft) // Timer value
                                        }
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier
                                        .padding(4.dp), // Optional padding for spacing inside the box
                                )
                            }
                        }

                        if (resendEnabled) {

                            Row(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .focusProperties {
                                        canFocus = false
                                    }, // Optional padding for spacing inside the box
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center // Align the content horizontally in the center
                            ) {


                                Text(
                                    text = "Resend OTP",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier
                                        .clickable {
                                            onResendOtpClicked()
                                        }
                                        .padding(4.dp), // Optional padding for spacing inside the box
                                )
                            }
                        }
                    }
                }
            }


        }


        Box(
            modifier = Modifier
                .fillMaxWidth() // Fill the width
                .background(Color.Blue)
                .wrapContentHeight() // Wrap content height


        ) {


            CustomNumberInput(onDigitClick = {

                updateDigit(it)

            }, onBackspaceClick = {
                onBackSpace()
            }, onForwardClick = {

                onForwardFocusedIndex()

            }) {
                onBackWardFocusedIndex()
            }

        }

    }
}


@Composable
private fun OtpEmailVerification(emailAddress: String) {

    // Create an AnnotatedString for the styled text
    val styledMessage = buildAnnotatedString {
        // Add the initial part of the message
        append("OTP sent to ")

        // Apply color style to the email address
        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
            append(emailAddress)
        }

        // Add the remaining part of the message
        append(" email. Don't forget to check all mails.")
    }

    // Display the styled text in a Text composable
    Text(
        text = styledMessage,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    )
}


@Composable
fun CustomNumberInput(
    onDigitClick: (String) -> Unit,   // Callback for digit button click
    onBackspaceClick: () -> Unit,      // Callback for backspace button click
    onForwardClick: () -> Unit,          // Callback for forward button click
    onBackwardClick: () -> Unit,          // Callback for forward button click

) {


    val focusRequester = remember {
        FocusRequester()
    }

    LaunchedEffect(true) {
        focusRequester.requestFocus()
    }

    Surface {
        Column(

            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .onKeyEvent { keyEvent ->

                    Log.d(LogUtils.TAG, "Key event $keyEvent")

                    if (keyEvent.type == KeyEventType.KeyDown) {
                        when (keyEvent.key) {
                            Key.DirectionLeft -> {
                                Log.d(LogUtils.TAG, "onLeft")
                                onBackwardClick()
                                true
                            }

                            Key.DirectionRight,
                                -> {
                                onForwardClick()
                                Log.d(LogUtils.TAG, "onRight")

                                true
                            }

                            Key.Backspace,
                                -> {
                                Log.d(LogUtils.TAG, "onBackSpace")
                                onBackspaceClick()
                                true
                            }

                            Key.Zero -> {
                                onDigitClick("0")
                                true
                            }

                            Key.One -> {
                                onDigitClick("1")
                                true
                            }

                            Key.Two -> {
                                onDigitClick("2")
                                true
                            }

                            Key.Three -> {
                                onDigitClick("3")
                                true
                            }

                            Key.Four -> {
                                onDigitClick("4")
                                true
                            }

                            Key.Five -> {
                                onDigitClick("5")
                                true
                            }

                            Key.Six -> {
                                onDigitClick("6")
                                true
                            }

                            Key.Seven -> {
                                onDigitClick("7")
                                true
                            }

                            Key.Eight -> {
                                onDigitClick("8")
                                true
                            }

                            Key.Nine -> {
                                onDigitClick("9")
                                true
                            }

                            else -> false
                        }
                    } else {
                        false
                    }

                }
                .focusable(),

            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    // Create number buttons 1-9 and 0
                    val numberButtons =
                        listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "", "0", "")

                    // Create rows of buttons
                    for (row in numberButtons.chunked(3)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                        ) {
                            row.forEach { digit ->

                                Button(
                                    modifier = Modifier
                                        .weight(1f)
                                        .focusProperties { canFocus = false },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    onClick = {
                                        if (digit.isNotEmpty()) { // Ensure digit is not empty
                                            onDigitClick(digit) // Call the digit click callback
                                        }
                                    }
                                ) {
                                    Text(digit)
                                }
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                ) {
                    // Backspace button
                    Button(
                        modifier = Modifier.focusProperties { canFocus = false },

                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        onClick = {
                            onBackspaceClick() // Call the backspace click callback
                        }
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardBackspace,
                            contentDescription = "Backspace"
                        )
                    }

                    // Forward button
                    Button(
                        modifier = Modifier.focusProperties { canFocus = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        onClick = {
                            onForwardClick() // Call the forward click callback
                        }
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Forward"
                        )
                    }

                    // Backward button
                    Button(
                        modifier = Modifier.focusProperties { canFocus = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        onClick = {
                            onBackwardClick() // Call the forward click callback
                        }
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Forward"
                        )
                    }

                }
            }
        }

    }


}


@Composable
fun CustomPinInput(
    onDigitClick: (String) -> Unit,   // Callback for digit button click
    onBackspaceClick: () -> Unit,      // Callback for backspace button click
    onForwardClick: () -> Unit,          // Callback for forward button click
    onBackwardClick: () -> Unit,          // Callback for forward button click

) {


    val focusRequester = remember {
        FocusRequester()
    }

    LaunchedEffect(true) {
        focusRequester.requestFocus()
    }

    Surface {
        Column(

            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .onKeyEvent { keyEvent ->


                    if (keyEvent.type == KeyEventType.KeyDown) {
                        when (keyEvent.key) {
                            Key.DirectionLeft -> {
                                Log.d(LogUtils.TAG, "onLeft")
                                onBackwardClick()
                                true
                            }

                            Key.DirectionRight,
                                -> {
                                onForwardClick()
                                Log.d(LogUtils.TAG, "onRight")

                                true
                            }

                            Key.Backspace,
                                -> {
                                Log.d(LogUtils.TAG, "onBackSpace")
                                onBackspaceClick()
                                true
                            }

                            Key.Zero -> {
                                onDigitClick("0")
                                true
                            }

                            Key.One -> {
                                onDigitClick("1")
                                true
                            }

                            Key.Two -> {
                                onDigitClick("2")
                                true
                            }

                            Key.Three -> {
                                onDigitClick("3")
                                true
                            }

                            Key.Four -> {
                                onDigitClick("4")
                                true
                            }

                            Key.Five -> {
                                onDigitClick("5")
                                true
                            }

                            Key.Six -> {
                                onDigitClick("6")
                                true
                            }

                            Key.Seven -> {
                                onDigitClick("7")
                                true
                            }

                            Key.Eight -> {
                                onDigitClick("8")
                                true
                            }

                            Key.Nine -> {
                                onDigitClick("9")
                                true
                            }

                            else -> false
                        }
                    } else {
                        false
                    }

                }
                .focusable(),

            horizontalAlignment = Alignment.CenterHorizontally
        ) {


            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)

            ) {
                // Create number buttons 1-9 and 0
                val numberButtons =
                    listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "", "0", "BackSpace")

                // Create rows of buttons
                for (row in numberButtons.chunked(3)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 16.dp)
                    ) {
                        row.forEach { digit ->


                            if (digit.isEmpty()) {
                                Box(modifier = Modifier.weight(1f))
                            } else {

                                if (digit == "BackSpace") {



                                    Button(
                                        shape = RectangleShape,
                                        modifier = Modifier
                                        .weight(1f)
                                            .focusProperties { canFocus = false }
                                            .indication(MutableInteractionSource(), null),

                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.surface
                                        ),
                                        onClick = {
                                            onBackspaceClick() // Call the backspace click callback
                                        },
                                        interactionSource = remember { NoRippleInteractionSource() }

                                    ) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.Backspace,
                                            contentDescription = "Backspace"
                                        )
                                    }
                                } else if (digit.all { it.isDigit() }) {

                                    Button(
                                        shape = RectangleShape,
                                        modifier = Modifier
                                            .weight(1f)
                                            .focusProperties { canFocus = false }
                                            .indication(MutableInteractionSource(), null),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.surface
                                        ),
                                        onClick = {
                                            if (digit.isNotEmpty() && digit.all { it.isDigit() }) { // Ensure digit is not empty
                                                onDigitClick(digit) // Call the digit click callback
                                            }
                                        },
                                        interactionSource = remember { NoRippleInteractionSource() }
                                    ) {
                                        Text(digit, fontSize = 34.sp)
                                    }
                                }
                            }

                        }
                    }
                }
            }

        }

    }

}


class NoRippleInteractionSource : MutableInteractionSource {

    override val interactions: Flow<Interaction> = emptyFlow()

    override suspend fun emit(interaction: Interaction) {}

    override fun tryEmit(interaction: Interaction) = true
}