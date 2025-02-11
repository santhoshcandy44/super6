package com.super6.pot.compose.ui.main.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.super6.pot.R

@Composable
fun NoInternetScreen(modifier: Modifier = Modifier, tryAgain: () -> Unit) {

    Column(
        modifier = modifier.padding(16.dp), // Padding for the Column,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Image
        Image(
            painter = painterResource(R.drawable.offline), // Replace with your drawable resource
            contentDescription = "No internet",
            modifier = Modifier.sizeIn(
                maxWidth = 200.dp,
                maxHeight = 200.dp
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Title Text
        Text(
            text = "Internet Connection Lost",
            fontSize = 16.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Description Text
        Text(
            text = "Please check your internet connection and try again to proceed.",
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Retry Button
        Button(
            onClick = {
                tryAgain()
            },
            modifier = Modifier.align(Alignment.CenterHorizontally),

            ) {
            Text(
                "Try again", style = MaterialTheme.typography.bodyLarge
            )
        }
    }

}

