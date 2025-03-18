package com.lts360.libs.utils

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


fun createFileDCIMExternalStorage(context: Context, folderName:String): Uri? {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())

    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_$timeStamp.jpg")
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/$folderName")
    }

    // Insert image details into MediaStore
    val contentResolver = context.contentResolver
    val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

    return uri
}