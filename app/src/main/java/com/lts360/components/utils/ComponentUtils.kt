package com.lts360.components.utils

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns

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


fun getFileNameForUri(context: Context, uri: Uri): String? {
    val resolver: ContentResolver = context.contentResolver
    val cursor = resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)

    return cursor?.use {
        if (it.moveToFirst()) {
            it.getString(it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
        } else {
            null
        }
    }
}



fun getFileSizeForUri(context: Context, uri: Uri): Long {
    val resolver: ContentResolver = context.contentResolver
    val cursor = resolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)

    return cursor?.use {
        if (it.moveToFirst()) {
            it.getLong(it.getColumnIndexOrThrow(OpenableColumns.SIZE))
        } else {
            0L
        }
    } ?: 0L
}

