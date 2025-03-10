package com.lts360.compose.ui.news.qr.models

import androidx.room.TypeConverter

// Convert QRCodeType enum to String and vice versa
class QRCodeTypeConverter {

    @TypeConverter
    fun fromQRCodeType(type: QRCodeType): String {
        return type.name
    }

    @TypeConverter
    fun toQRCodeType(type: String): QRCodeType {
        return QRCodeType.valueOf(type)
    }

    @TypeConverter
    fun fromQRCodeDataType(type: QRCodeDataType): String {
        return type.name
    }

    @TypeConverter
    fun toQRCodeDataType(type: String): QRCodeDataType {
        return QRCodeDataType.valueOf(type)
    }

}
