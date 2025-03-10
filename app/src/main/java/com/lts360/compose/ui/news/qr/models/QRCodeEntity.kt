package com.lts360.compose.ui.news.qr.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.lts360.app.database.daos.chat.MessageDao

@TypeConverters(QRCodeTypeConverter::class)
@Entity(tableName = "qr_codes")
data class QRCodeEntity(

    val type: QRCodeDataType,                 // Type of the data, e.g., "url", "event", "contact", etc.
    val data: String,                         // QR Code data (e.g., URL, vCard data, etc.)
    val qrImageData: ByteArray?=null,
    val isFavorite: Boolean = false,           // Flag to mark QR code as a favorite
    val qrCodeType: QRCodeType = QRCodeType.QRCODE, // QR code type (qrcode or barcode)
    val generatedAt: Long = System.currentTimeMillis(), // Timestamp when QR was generated
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as QRCodeEntity

        if (type != other.type) return false
        if (qrCodeType != other.qrCodeType) return false
        if (data != other.data) return false
        if (generatedAt != other.generatedAt) return false
        if (isFavorite != other.isFavorite) return false
        if (qrImageData != null) {
            if (other.qrImageData == null) return false
            if (!qrImageData.contentEquals(other.qrImageData)) return false
        } else if (other.qrImageData != null) return false
        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + qrCodeType.hashCode()
        result = 31 * result + data.hashCode()
        result = 31 * result + generatedAt.hashCode()
        result = 31 * result + isFavorite.hashCode()
        result = 31 * result + (qrImageData?.contentHashCode() ?: 0)
        result = 31 * result + id.hashCode()
        return result
    }


}
