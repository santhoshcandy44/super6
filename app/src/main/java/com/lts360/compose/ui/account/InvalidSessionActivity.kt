package com.lts360.compose.ui.account

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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
import com.lts360.R
import com.lts360.compose.ui.auth.AuthActivity
import com.lts360.compose.ui.theme.AppTheme
import com.lts360.compose.ui.theme.icons
import com.lts360.compose.utils.SafeDrawingBox

class InvalidSessionActivity: ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
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
                painterResource(MaterialTheme.icons.exit),
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

