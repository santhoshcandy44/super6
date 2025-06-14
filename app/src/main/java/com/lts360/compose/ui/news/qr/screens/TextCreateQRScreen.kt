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
import androidx.compose.material.icons.filled.FormatColorText
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import com.lts360.compose.ui.news.qr.viewmodels.TextCreateQRViewModel
import com.lts360.compose.ui.theme.customColorScheme
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextCreateQRScreen(onPopBackStack:()->Unit) {

    val textCreateQRViewModel: TextCreateQRViewModel = koinViewModel()
    val text by textCreateQRViewModel.textState.collectAsState()

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Text",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },

                navigationIcon = {
                    IconButton(onClick = dropUnlessResumed {
                        onPopBackStack()
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


                Column(modifier = Modifier.wrapContentSize()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,

                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Image(
                            Icons.Default.FormatColorText,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),

                            )
                        Text("Add text content to QR code", modifier = Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("Text", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(4.dp))
                    // Domain Input
                    BasicTextField(
                        text,
                        readOnly = false,
                        onValueChange = {
                            textCreateQRViewModel.updateText(it)
                        },
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
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Uri),
                    ) { innerTextField ->

                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart
                        ) {

                            if (text.isEmpty()) {
                                Text(
                                    "Enter content here",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                innerTextField() // Displays the actual text input field
                            }
                        }

                    }

                }


                Column(modifier = Modifier.wrapContentSize()) {
                    // Button to generate QR Code
                    Button(
                        onClick = {
                            textCreateQRViewModel.generateQrCodeForText(text, onSuccess = {
                                Toast.makeText(
                                    context,
                                    "Successfully Text QR code is generated",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }, onError = {
                                Toast.makeText(
                                    context, "Failed to generate text QR code",
                                    Toast.LENGTH_SHORT
                                ).show()

                            })
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Text("Generate QR Code")
                    }

                }
            }
        }
    )
}

