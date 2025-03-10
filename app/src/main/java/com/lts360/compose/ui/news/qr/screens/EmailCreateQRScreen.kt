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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation.NavController
import com.lts360.compose.ui.news.qr.viewmodels.EmailCreateQRViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailCreateQRScreen(navController: NavController) {

    val viewModel: EmailCreateQRViewModel = hiltViewModel()
    val email by viewModel.email.collectAsState()
    val subject by viewModel.subject.collectAsState()
    val content by viewModel.content.collectAsState()

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Email",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = dropUnlessResumed {
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
                Column(modifier = Modifier.wrapContentSize()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            Icons.Default.Email,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface))
                        Text("Add email to QR code", modifier = Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("Recipient Email", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(4.dp))
                    BasicTextField(
                        email,
                        onValueChange = { viewModel.updateEmail(it) },
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                        singleLine = true,
                        textStyle = TextStyle.Default.copy(color = MaterialTheme.colorScheme.onSurface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(4.dp)
                            )
                            .padding(8.dp)
                            .height(32.dp),
                    ) { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (email.isEmpty()) {
                                Text("Enter recipient email", style = MaterialTheme.typography.bodySmall)
                            }
                            innerTextField()
                        }
                    }
                    Spacer(Modifier.height(16.dp))

                    Text("Subject", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(4.dp))
                    BasicTextField(
                        subject,
                        onValueChange = { viewModel.updateTitle(it) },
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                        singleLine = true,
                        textStyle = TextStyle.Default.copy(color = MaterialTheme.colorScheme.onSurface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(4.dp)
                            )
                            .padding(8.dp)
                            .height(32.dp),
                    ) { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (subject.isEmpty()) {
                                Text("Enter subject", style = MaterialTheme.typography.bodySmall)
                            }
                            innerTextField()
                        }
                    }
                    Spacer(Modifier.height(16.dp))

                    Text("Email Content", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(4.dp))
                    BasicTextField(
                        content,
                        onValueChange = { viewModel.updateContent(it) },
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                        textStyle = TextStyle.Default.copy(color = MaterialTheme.colorScheme.onSurface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(4.dp)
                            )
                            .padding(8.dp),
                    ) { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.TopStart
                        ) {
                            if (content.isEmpty()) {
                                Text("Enter email content", style = MaterialTheme.typography.bodySmall)
                            }
                            innerTextField()
                        }
                    }
                }

                Button(
                    onClick = {
                        viewModel.generateQrCodeForEmail(email, subject, content, onSuccess = {
                            Toast.makeText(
                                context,
                                "Successfully generated email QR code",
                                Toast.LENGTH_SHORT
                            ).show()
                        }, onError = {
                            Toast.makeText(
                                context, "Failed to generate email QR code",
                                Toast.LENGTH_SHORT
                            ).show()
                        })
                    },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Generate QR Code")
                }
            }
        }
    )
}