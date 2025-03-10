package com.lts360.compose.ui.news.qr.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.lts360.compose.ui.news.qr.models.QRCodeDataType
import com.lts360.compose.ui.news.qr.models.QRCodeEntity
import com.lts360.compose.ui.news.qr.models.QRCodeType
import com.lts360.compose.ui.news.qr.models.QRCodeTypeConverter

@TypeConverters(QRCodeTypeConverter::class)
@Dao
interface QRCodeDao {

    // Insert a new QR code into the database
    @Insert
    suspend fun insertQRCode(qrCode: QRCodeEntity)

    // Retrieve all QR codes
    @Query("SELECT * FROM qr_codes")
    suspend fun getAllQRCodeData(): List<QRCodeEntity>

    @Query("SELECT * FROM qr_codes WHERE qrCodeType = :qrCodeType")
    suspend fun getQRCodeByType(qrCodeType: QRCodeType): List<QRCodeEntity>

    // Retrieve QR codes based on their data type (e.g., URL, Event, etc.)
    @Query("SELECT * FROM qr_codes WHERE type = :qrCodeDataType")
    suspend fun getQRCodeByDataType(qrCodeDataType: QRCodeDataType): List<QRCodeEntity>

    // Retrieve favorite QR codes
    @Query("SELECT * FROM qr_codes WHERE isFavorite = 1")
    suspend fun getFavoriteQRCodeData(): List<QRCodeEntity>

    // Update the favorite status of a QR code
    @Query("UPDATE qr_codes SET isFavorite = :isFavorite WHERE id = :qrCodeId")
    suspend fun updateFavoriteStatus(qrCodeId: Long, isFavorite: Boolean)
}
