package com.lts360.compose.ui.news.qr.screens


import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.lts360.compose.ui.news.qr.viewmodels.WifiCreateQRViewModel
import com.lts360.compose.ui.services.manage.PlanItem
import com.lts360.compose.ui.theme.customColorScheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WifiCreateQRScreen(navController: NavController) {

    val viewModel: WifiCreateQRViewModel = hiltViewModel()
    val ssid by viewModel.ssid.collectAsState()
    val password by viewModel.password.collectAsState()
    val encryptionType by viewModel.encryptionType.collectAsState()
    val isHidden by viewModel.isHidden.collectAsState()
    val qrCodeBitmap by viewModel.qrCodeBitmap.collectAsState()

    val context = LocalContext.current

    // Encryption types available
    val encryptionTypes = listOf("WPA", "WEP", "Open")

    // State for controlling the dropdown expansion
    var expanded by remember { mutableStateOf(false) }

    // Scaffold with Back Button, Title, and QR Code Generation Logic
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Wi-Fi QR Code", // Set your title text here
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },

                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                }
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // SSID and Password fields
                Column(modifier = Modifier.wrapContentSize()) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Image(
                            Icons.Default.Wifi,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
                        )
                        Text("Add Wifi Info to QR code", modifier = Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(16.dp))

                    // SSID Input Field
                    Text("Wifi Name", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(4.dp))
                    BasicTextField(
                        value = ssid,
                        onValueChange = { viewModel.updateSSID(it) },
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                        singleLine = true,
                        textStyle = TextStyle.Default.copy(color = MaterialTheme.colorScheme.onSurface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.customColorScheme.searchBarColor,
                                RoundedCornerShape(4.dp)
                            )
                            .padding(8.dp)
                            .height(32.dp),
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text),
                    ) { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (ssid.isEmpty()) {
                                Text(
                                    "Enter Wifi name here",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                innerTextField() // Displays the actual text input field
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Password Input Field
                    Text("Password", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(4.dp))
                    BasicTextField(
                        value = password,
                        onValueChange = { viewModel.updatePassword(it) },
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                        singleLine = true,
                        textStyle = TextStyle.Default.copy(color = MaterialTheme.colorScheme.onSurface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.customColorScheme.searchBarColor,
                                RoundedCornerShape(4.dp)
                            )
                            .padding(8.dp)
                            .height(32.dp),
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password),
                    ) { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart

                        ) {
                            if (password.isEmpty()) {
                                Text(
                                    "Enter Password here",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                innerTextField() // Displays the actual text input field
                            }
                        }
                    }



                    Spacer(Modifier.height(16.dp))

                    // Encryption Type Dropdown
                    Column(modifier = Modifier.wrapContentSize()) {
                        Text("Select Encryption Type", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(4.dp))

                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = {
                                expanded = !expanded
                            }, // Control the expansion of the dropdown

                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(4.dp)
                                )
                        ) {
                            TextField(
                                value = encryptionType,
                                onValueChange = {},
                                label = null,
                                readOnly = true,
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                },
                                modifier = Modifier.fillMaxWidth()
                                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                encryptionTypes.forEach { type ->
                                    DropdownMenuItem(
                                        {
                                            Text(text = type)
                                        }, onClick = {
                                            viewModel.updateEncryptionType(type)
                                            expanded = false // Close the dropdown after selection
                                        })
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Hidden Wi-Fi Option (Checkbox or Dropdown)
                    Column(modifier = Modifier.wrapContentSize()) {
                        Text("Hidden Network", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(4.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text("Is it hidden?")
                            Switch(
                                checked = isHidden,
                                onCheckedChange = { viewModel.updateHiddenStatus(it) }
                            )
                        }
                    }

                }


                // Button to generate QR Code
                Button(
                    onClick = {
                        viewModel.generateWifiQrCode(
                            ssid = ssid,
                            password = password,
                            encryptionType = encryptionType,
                            isHidden = isHidden,
                            onSuccess = {
                                Toast.makeText(
                                    context,
                                    "Wi-Fi QR code generated successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            onError = {
                                Toast.makeText(
                                    context,
                                    "Failed to generate Wi-Fi QR code",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Generate Wi-Fi QR Code")
                }

            }
        }
    )
}

