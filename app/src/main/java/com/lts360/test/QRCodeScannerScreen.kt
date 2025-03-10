package com.lts360.test

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.MultiFormatWriter
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.BitMatrix
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

@Composable
fun QRCodeScreen() {
    val qrCode = remember { generateQRCode("https://yourapp.com") }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
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

    val hints = mapOf(
        EncodeHintType.MARGIN to 0,
        EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H, // High error correction
        EncodeHintType.CHARACTER_SET to "UTF-8" // Proper encoding for special characters
    ) // 👈 Set margin to 0


    val bitMatrix: BitMatrix =
        MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints)
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


    val width = bitmap.width
    val height = bitmap.height
    val intArray = IntArray(width * height)
    bitmap.getPixels(intArray, 0, width, 0, 0, width, height)

    val source = RGBLuminanceSource(width, height, intArray)
    val binaryBitmap = BinaryBitmap(HybridBinarizer(source))

    try {
        val reader = MultiFormatReader()
        val result = reader.decode(binaryBitmap)

        throw Exception(result.text)
    } catch (e: Exception) {
        e.printStackTrace()
        null // QR code not detected
    }

    return bitmap.asImageBitmap()
}


fun generateBarcode(
    content: String,
    format: BarcodeFormat,
    width: Int = 600,
    height: Int = 300
): ImageBitmap {
    val bitMatrix: BitMatrix = MultiFormatWriter().encode(content, format, width, height)
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

    for (x in 0 until width) {
        for (y in 0 until height) {
            bitmap.setPixel(
                x,
                y,
                if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE
            )
        }
    }

    return bitmap.asImageBitmap()
}