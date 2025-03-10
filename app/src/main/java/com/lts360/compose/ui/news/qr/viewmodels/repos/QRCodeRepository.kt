package com.lts360.compose.ui.news.qr.viewmodels.repos

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import androidx.room.Query
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.lts360.app.database.AppDatabase
import com.lts360.components.utils.LogUtils.TAG
import com.lts360.compose.ui.news.qr.models.QRCodeDataType
import com.lts360.compose.ui.news.qr.models.QRCodeEntity
import com.lts360.compose.ui.news.qr.models.QRCodeType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject


class QRCodeRepository @Inject constructor(private val appDatabase: AppDatabase) {

    // Function to generate QR code based on input
    suspend fun generateQRCode(data: String): Bitmap? {
        return withContext(Dispatchers.IO) {
            if (data.isEmpty()) {
                // Provide minimal data instead of an empty string
                return@withContext generateQRCode(" ")  // Pass a space to avoid empty string error
            }
            try {
                val size = 512
                val hints = mapOf(EncodeHintType.MARGIN to 0)
                val bitMatrix: BitMatrix = MultiFormatWriter().encode(
                    data, BarcodeFormat.QR_CODE, size, size, hints
                )
                val width = bitMatrix.width
                val height = bitMatrix.height
                val pixels = IntArray(width * height)
                for (y in 0 until height) {
                    for (x in 0 until width) {
                        pixels[y * width + x] =
                            if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE
                    }
                }
                val qrCodeBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                qrCodeBitmap.setPixels(pixels, 0, width, 0, 0, width, height)

                qrCodeBitmap
            } catch (e: Exception) {
                Log.e(TAG, "Error generating QR code", e)
                null
            }
        }
    }

    // Save QR code to the database with QRCodeType and QRCodeDataType
    suspend fun saveQRCodeToDatabase(
        type: QRCodeDataType,
        data: String,
        qrCodeBitmap: Bitmap,
        qrCodeType: QRCodeType = QRCodeType.QRCODE,
        isFavorite:Boolean=false
    ) {
        withContext(Dispatchers.IO) {
            val byteArray = bitmapToByteArray(qrCodeBitmap)
            val qrCodeEntity = QRCodeEntity(
                type = type,
                data = data,
                qrCodeType = qrCodeType,
                qrImageData = byteArray,
                isFavorite = isFavorite,  // Default favorite status
                generatedAt = System.currentTimeMillis()
            )
            appDatabase.qrCodeDao().insertQRCode(qrCodeEntity)
        }
    }

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        return stream.toByteArray()
    }


    suspend fun insertQRCodeData(qrCodeEntity: QRCodeEntity) {
        return withContext(Dispatchers.IO) {
            appDatabase.qrCodeDao().insertQRCode(qrCodeEntity)
        }
    }

    suspend fun getAllQRCodeData(qrCodeType: QRCodeType): List<QRCodeEntity> {
        return withContext(Dispatchers.IO) {
            appDatabase.qrCodeDao().getAllQRCodeData()
        }
    }

    suspend fun getQRCodeByType(qrCodeType: QRCodeType): List<QRCodeEntity> {
        return withContext(Dispatchers.IO) {
            appDatabase.qrCodeDao().getQRCodeByType(qrCodeType)
        }
    }

    suspend fun getQRCodeByDataType(qrCodeDataType: QRCodeDataType): List<QRCodeEntity> {
        return withContext(Dispatchers.IO) {
            appDatabase.qrCodeDao().getQRCodeByDataType(qrCodeDataType)
        }
    }

    suspend fun getFavoriteQRCodeData(): List<QRCodeEntity> {
        return withContext(Dispatchers.IO) {
            appDatabase.qrCodeDao().getFavoriteQRCodeData()
        }
    }

    suspend fun updateQRCodeFavoriteStatus(id: Long, isFavorite: Boolean) {
        return withContext(Dispatchers.IO) {
            appDatabase.qrCodeDao().updateFavoriteStatus(id, isFavorite)
        }
    }

}