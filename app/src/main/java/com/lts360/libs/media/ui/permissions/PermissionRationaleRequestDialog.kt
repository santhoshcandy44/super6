package com.lts360.libs.media.ui.permissions

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionRationaleRequestDialog(
    icon: ImageVector,
    permissionLabel:String,
    permissionDescription:String,
    onAllowPermissionClicked: () -> Unit,
    onDismissRequest: () -> Unit,
    iconSize: Dp = 80.dp,
    dismissButtonEnabled: Boolean = false,
    dismissOnClickOutside:Boolean = true
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        BasicAlertDialog(
            onDismissRequest = onDismissRequest,
            properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = dismissOnClickOutside),
            modifier = Modifier.clip(RoundedCornerShape(16.dp))
        ) {
            Surface {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Image(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier
                            .size(iconSize),
                        colorFilter = ColorFilter.tint(Color(0xFF9394f0))
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "$permissionLabel Permission Required",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center

                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        permissionDescription,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onAllowPermissionClicked,
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE8B02)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            Text("Settings")
                        }

                        if (dismissButtonEnabled) {
                            OutlinedButton(
                                onClick = onDismissRequest,
                                shape = CircleShape,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            ) {
                                Text("Dismiss")
                            }
                        }
                    }

                }
            }
        }
    }
}