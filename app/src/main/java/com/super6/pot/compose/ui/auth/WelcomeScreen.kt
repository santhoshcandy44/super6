package com.super6.pot.compose.ui.auth

import android.widget.ImageView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.dropUnlessResumed
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import com.super6.pot.R
import com.super6.pot.compose.dropUnlessResumedV2


@Composable
fun WelcomeScreen(
    onLogInNavigate: () -> Unit,
    onSelectAccountNavigate: () -> Unit,
    onGoGuestNavigateUp: () -> Unit,
) {


    val lifecycleOwner = LocalLifecycleOwner.current

    Box(modifier = Modifier
            .fillMaxSize()) {



        AndroidView(
            factory = { ctx ->
                ImageView(ctx).apply {
                    setImageResource(R.drawable.wall) // Load .9.png
                }
            },
            modifier = Modifier.fillMaxSize()
        )


        Box(
            modifier = Modifier
                .fillMaxSize()
                ) {



            Column(
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 16.dp)
            ) {

                // Welcome Texts
                Column(

                    verticalArrangement = Arrangement.Center
                ) {


                    Text(
                        text = "Welcome to",
                        fontSize = 32.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Super6",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Make your journey better than before with Pot platform.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                // Bottom Buttons
                Row(
                    modifier = Modifier
                        .padding(top = 40.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    OutlinedButton(
                        onClick = dropUnlessResumed { onLogInNavigate()  },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White
                        )

                    ) {
                        Text(text = "Log In", color = Color.Black,
                            style = MaterialTheme.typography.bodyMedium)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Or",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = dropUnlessResumed{ onSelectAccountNavigate() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(id = R.color.colorPrimary)
                        )

                    ) {
                        Text(
                            text = "Sign Up", color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .padding(top = 16.dp),

                    ) {

                    Text(
                        modifier = Modifier.clickable {
                            dropUnlessResumedV2(lifecycleOwner){
                                onGoGuestNavigateUp()
                            }
                        },
                        text = "Go as Guest",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

            }

        }
    }
}




@Composable
fun ForceWelcomeScreen(
    onLogInNavigate: () -> Unit,
    onSelectAccountNavigate: () -> Unit,
    onCloseForceWelcomeScreen:()-> Unit
) {


    Scaffold {
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(it)) {

            Box(modifier = Modifier
                .fillMaxSize()) {

                IconButton(
                    onClick = {
                        onCloseForceWelcomeScreen()
                    },
                    modifier = Modifier.padding(16.dp) // Optional padding for spacing
                ) {
                    Icon(
                        imageVector = Icons.Default.Close, // Use the Close icon
                        contentDescription = "Close"
                    )
                }


                Column(
                    modifier = Modifier
                        .wrapContentSize()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 16.dp)
                ) {

                    // Welcome Texts
                    Column(
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Join Now",
                            fontSize = 32.sp,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Super6",
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Make your journey better than before with Super6 platform.",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }

                    // Bottom Buttons
                    Row(
                        modifier = Modifier
                            .padding(top = 40.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        OutlinedButton(
                            onClick =  {
                                onLogInNavigate() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White
                            )

                        ) {
                            Text(text = "Log In", color = Color.Black,
                                style = MaterialTheme.typography.bodyMedium)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "Or",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                onSelectAccountNavigate()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(id = R.color.colorPrimary)
                            )

                        ) {
                            Text(
                                text = "Sign Up", color = Color.White,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                }

            }
        }
    }

}