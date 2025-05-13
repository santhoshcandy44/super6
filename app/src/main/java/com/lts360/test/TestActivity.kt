package com.lts360.test


import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.content.res.Configuration
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.RemoteViews
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.lts360.R
import com.lts360.compose.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.graphics.drawable.toBitmap
import com.lts360.compose.ui.main.NotificationScreen
import com.lts360.pot.database.services.getCircularBitmap


@AndroidEntryPoint
class TestActivity : ComponentActivity() {

    companion object {
        init {
            System.loadLibrary("native-lib")
        }
    }




    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.expanded_custom_local_job_applicant_applied_notification)


        /*  setContent {
              AppTheme {
                  Surface {
                      SafeDrawingBox {
                          *//*   JobSearchScreenWithSmartDropdowns(onSearchClick = { role, location ->
                               println("Searching for $role jobs in $location")
                           }, onPopUp = {

                           })*//*


                    }
                }
            }
        }*/
    }




    @Composable
    fun add() {

        Scaffold(topBar = {


        }) { paddingValues ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(top = 16.dp)
                        .padding(horizontal = 16.dp)

                ) {

                }
            }


            /*
                        ProfileNotCompletedPromptSheet(onDismiss = {})
            */
        }
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ProfileNotCompletedPromptSheet(
        onDismiss: () -> Unit,
        modifier: Modifier = Modifier,
        sheetState: SheetState = rememberModalBottomSheetState()
    ) {

        var unCompletedProfileFields = listOf("EMAIL", "PHONE")

        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            dragHandle = null,
            modifier = modifier
        ) {
            Column(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Profile Not Completed",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(8.dp)
                    )

                    Text(text = "Complete below required profile fields to proceed.")

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color.Red)
                        ) {
                            Text("${unCompletedProfileFields.size}", color = Color.White)
                        }

                        Text(text = "${if (unCompletedProfileFields.size == 1) "Field" else "Fields"} Need to be Complete")

                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    unCompletedProfileFields.forEach {
                        when (it) {
                            "EMAIL" -> {
                                ListItem(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    headlineContent = {
                                        Text(text = "Email", fontWeight = FontWeight.Bold)
                                    },
                                    leadingContent = {
                                        Image(
                                            painterResource(R.drawable.ic_verify_phone),
                                            contentDescription = null,
                                            modifier = Modifier.size(48.dp)
                                        )
                                    },
                                )

                            }

                            "PHONE" -> {
                                ListItem(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    headlineContent = {
                                        Text(text = "Phone", fontWeight = FontWeight.Bold)
                                    },
                                    leadingContent = {
                                        Image(
                                            painterResource(R.drawable.ic_verify_phone),
                                            contentDescription = null,
                                            modifier = Modifier.size(48.dp)
                                        )
                                    },
                                )

                            }
                        }
                    }



                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {

                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(
                                0xFF25D366
                            )
                        ),
                        modifier = Modifier
                            .fillMaxWidth(),
                        shape = RectangleShape
                    ) {
                        Text("Complete Profile", color = Color.White)
                    }

                    OutlinedButton(
                        onClick = {

                        },
                        modifier = Modifier
                            .fillMaxWidth(),
                        shape = RectangleShape
                    ) {
                        Text("Cancel")
                    }


                }
            }
        }

    }


    @Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
    @Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
    @Composable
    fun DefaultPreview() {

        AppTheme {
            Surface {

            }
        }

    }
}




