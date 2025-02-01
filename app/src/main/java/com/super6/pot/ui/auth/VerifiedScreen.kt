package com.super6.pot.ui.auth


import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.super6.pot.R


@Composable
fun VerifiedScreen(
    onGoToHomeClicked: () -> Unit,
) {
    // Container for the entire layout
    Surface(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ){
            // Center the content
            Column(
                modifier = Modifier
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Image (verified icon)
                Image(
                    painter = painterResource(id = R.drawable.verified), // Use your verified drawable here
                    contentDescription = "Verified Icon",
                    modifier = Modifier
                        .size(100.dp) // Adjust size based on your image
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Title Text
                Text(
                    text = "Account Verified",
                    fontSize = 32.sp,
                    color = Color(0xFFFFA500), // Use orange-dark color
                    modifier = Modifier.padding(8.dp)
                )

                // Subtitle Text
                Text(
                    text = "Your Account has verified successfully. Go to home and browse.",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(8.dp)
                )
            }

            // Go to Home Button at the bottom
            Button(
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                onClick = onGoToHomeClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(vertical = 16.dp)
            ) {
                Text(text = "Go to home",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .padding(horizontal = 16.dp) // Horizontal padding for the text
                    )
            }
        }
    }
}

