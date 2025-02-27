package com.lts360.test


import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.lts360.compose.ui.theme.AppTheme
import com.lts360.compose.utils.SafeDrawingBox
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class TestActivity : ComponentActivity() {

    companion object {
        init {
            System.loadLibrary("native-lib")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                Surface {
                    SafeDrawingBox {
                        QRCodeScannerScreen()
                    }
                }
            }
        }
    }
}


@Composable
fun QRCodeScannerScreen() {
    var scannedData by remember { mutableStateOf("Scan a QR Code") }

    val qrScannerLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            scannedData = result.contents // Get the scanned QR data
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = scannedData, modifier = Modifier.padding(16.dp))

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            val options = ScanOptions()
            options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            options.setPrompt("Scan a QR Code")
            options.setBeepEnabled(true)
            options.setOrientationLocked(true)
            qrScannerLauncher.launch(options)
        }) {
            Text("Scan QR Code")
        }
    }
}



@Composable
fun QRCodeScreen() {
    val qrCode = remember { generateQRCode("https://yourapp.com") }

    Box(modifier = Modifier
        .fillMaxSize()) {
        Image(
            bitmap = qrCode,
            contentDescription = "QR Code",
            modifier = Modifier
                .aspectRatio(1f)
                .padding(16.dp)
                .align(Alignment.Center)
        )
    }
}


fun generateQRCode(content: String, size: Int = 512): ImageBitmap {
    val bitMatrix: BitMatrix =
        MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, size, size)
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)

    for (x in 0 until size) {
        for (y in 0 until size) {
            bitmap.setPixel(
                x,
                y,
                if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE
            )
        }
    }

    return bitmap.asImageBitmap()
}


@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun DefaultPreview() {

    AppTheme {
        Surface {
            Card(modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp)
            ){
                Column(modifier = Modifier
                    .fillMaxWidth()
                ) {

                    // System Default Checkbox
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp).background(Color.Red),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {

                        Text("Use System Default", fontSize = 16.sp)

                        Box(Modifier
                            .wrapContentSize()){

                            Switch(
                                modifier = Modifier.size(24.dp)
                                    .background(Color.Magenta),

                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White, // Thumb color when ON
                                    checkedTrackColor = Color(0xFF00C04D), // Track background when ON
                                    uncheckedThumbColor = Color.White, // Thumb color when OFF
                                    uncheckedTrackColor = Color(0xFFBBC8D4), // Track background when OFF
                                    checkedBorderColor = Color.Transparent,
                                    uncheckedBorderColor = Color.Transparent
                                ),

                                checked = false,
                                onCheckedChange = { isChecked ->

                                }
                            )
                        }

                    }

                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Dark Mode", style = MaterialTheme.typography.bodyLarge)

                        Switch(
                            enabled = true,
                            modifier = Modifier.size(24.dp),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White, // Thumb color when ON
                                checkedTrackColor = Color(0xFF00C04D), // Track background when ON
                                uncheckedThumbColor = Color.White, // Thumb color when OFF
                                uncheckedTrackColor = Color(0xFFBBC8D4), // Track background when OFF
                                checkedBorderColor = Color.Transparent,
                                uncheckedBorderColor = Color.Transparent
                            ),

                            checked = true,
                            onCheckedChange = { isChecked ->

                            }
                        )
                    }

                }
            }


        }
    }
}

