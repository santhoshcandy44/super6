package com.super6.pot.ui.account

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.super6.pot.R
import com.super6.pot.ui.auth.AuthActivity
import com.super6.pot.ui.theme.AppTheme
import com.super6.pot.ui.utils.SafeDrawingBox

class InvalidSessionActivity: ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                Surface {
                    SafeDrawingBox{
                        SessionExpiredScreen {
                            startActivity(
                                Intent(
                                    this@InvalidSessionActivity,
                                    AuthActivity::class.java
                                ).apply {
                                    putExtra("is_welcome", true)
                                })
                        }
                    }
                }
            }

       }

    }
}



@Composable
private fun SessionExpiredScreen(onLoginClick: () -> Unit) {

    Scaffold { contentPadding ->

        Column(
            modifier = Modifier.fillMaxSize().padding(contentPadding)
                .padding(horizontal = 16.dp).padding(top = 8.dp),
            verticalArrangement = Arrangement.Center,

            ) {


            Image(
                painterResource(R.drawable.ic_exit),
                modifier = Modifier.size(32.dp),
                contentDescription = null)


            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Account status",
                style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Your account was logged out. Please continue login to explore.",
                style = TextStyle(fontSize = 16.sp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = { onLoginClick() }) {
                Text("Continue")
            }
        }
    }

}

