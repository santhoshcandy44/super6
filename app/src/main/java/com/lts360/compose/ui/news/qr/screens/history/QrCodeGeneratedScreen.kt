package com.lts360.compose.ui.news.qr.screens.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TextFormat
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.lts360.app.database.AppDatabase
import com.lts360.compose.ui.news.qr.models.QRCodeEntity
import com.lts360.compose.ui.news.qr.viewmodels.history.QRCodeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRCodeGeneratedScreen(
    qrCodeViewModel: QRCodeViewModel = viewModel()
) {
    val generatedQrCodes by qrCodeViewModel.qrCodes.collectAsState(emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("QR Codes") },
            )
        },
        content = { padding ->
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(generatedQrCodes) { qrCode ->
                    QRCodeItem(qrCode)
                }
            }
        }
    )
}

@Composable
fun QRCodeItem(qrCode: QRCodeEntity) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Row {
            AsyncImage(
                Icons.Default.TextFormat,  // Using Coil for image loading
                contentDescription = "QR Code",
                modifier = Modifier.size(150.dp)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // Show some data of the QR Code
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Data: ${qrCode.data}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Type: ${qrCode.type}", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

