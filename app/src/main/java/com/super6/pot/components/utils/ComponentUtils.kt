package com.super6.pot.components.utils

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore

fun isUriExist(context: Context, uri: Uri): Boolean {

    return try {
        context.contentResolver.openInputStream(uri)?.close()
        true
    } catch (e: Exception) {
        false
    }
}

fun getPathFromUri(context: Context, uri: Uri): String? {
    var filePath: String? = null
    val cursor: Cursor? =
        context.contentResolver.query(uri, arrayOf(MediaStore.Video.Media.DATA), null, null, null)

    cursor?.use {
        val columnIndex = it.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
        if (it.moveToFirst()) {
            // If the cursor is not empty, get the file path from the column
            filePath = it.getString(columnIndex)
        }
    }

    // Return the file path or null if not found
    return filePath
}
