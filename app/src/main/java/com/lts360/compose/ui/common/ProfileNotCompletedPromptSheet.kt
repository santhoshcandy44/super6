package com.lts360.compose.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.lts360.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileNotCompletedPromptSheet(
    unCompletedProfileFields: List<String>,
    onDismiss: () -> Unit,
    onProfileCompleteClick:()->Unit,
    modifier: Modifier = Modifier
) {

    val coroutineScope = rememberCoroutineScope()
    val sheetState: SheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Profile Not Completed",
                style = MaterialTheme.typography.headlineSmall)

            Spacer(modifier = Modifier.height(8.dp))

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
                                Text(text = "Email")
                            },
                            leadingContent = {
                                Image(
                                    painterResource(R.drawable.ic_info_personal_info),
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
                                Text(text = "Phone")
                            },
                            leadingContent = {
                                Image(
                                    painterResource(R.drawable.ic_info_phone),
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
                    coroutineScope.launch {
                        sheetState.hide()
                        onProfileCompleteClick()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RectangleShape
            ) {
                Text("Complete Profile", color = Color.White)
            }

            OutlinedButton(
                onClick = {
                    coroutineScope.launch {
                        sheetState.hide()
                    }
                    onDismiss()
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
