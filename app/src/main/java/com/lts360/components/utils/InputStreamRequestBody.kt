package com.lts360.components.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source
import okio.buffer
import java.io.File
import java.io.IOException
import java.util.Locale

class InputStreamRequestBody(
    private val context: Context,
    private val uri: Uri
) : RequestBody() {

    private val contentResolver = context.contentResolver

    override fun contentType(): MediaType? =
        getMimeType(context,uri)?.toMediaTypeOrNull()

    @Throws(IOException::class)
    override fun contentLength(): Long {
        return try {
            // For file URIs, get the file length directly
            if (uri.scheme == ContentResolver.SCHEME_FILE) {
                File(uri.path.orEmpty()).length()
            } else {
                // For content URIs, query the ContentResolver for the file size
                context.contentResolver.openFileDescriptor(uri, "r")?.statSize ?: -1
            }
        } catch (e: Exception) {
            -1 // Return -1 if we cannot determine the content length
        }
    }

    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {
        contentResolver.openInputStream(uri)?.use { inputStream ->
            val buffer = ByteArray(8 * 1024) // 8 KB buffer size
            inputStream.source().buffer().use { source ->
                var bytesRead: Int
                while (source.read(buffer).also { bytesRead = it } != -1) {
                    sink.write(buffer, 0, bytesRead) // Write chunk by chunk
                }
            }
        } ?: throw IOException("Unable to open input stream from URI: $uri")
    }

    companion object {

        fun getMimeType(context: Context, uri: Uri): String? = when (uri.scheme) {
            ContentResolver.SCHEME_CONTENT -> context.contentResolver.getType(uri)
            ContentResolver.SCHEME_FILE -> uri.toString()
                .substringAfterLast('.', "")
                .lowercase(Locale.US)
                .let { MimeTypeMap.getSingleton().getMimeTypeFromExtension(it) }
            else -> null
        }

        fun getFileName(context: Context, uri: Uri): String? {
            return when (uri.scheme) {
                ContentResolver.SCHEME_FILE -> File(uri.path.orEmpty()).name
                ContentResolver.SCHEME_CONTENT -> getCursorContent(uri, context.contentResolver)
                else -> null
            }
        }

        private fun getCursorContent(uri: Uri, contentResolver: ContentResolver): String? =
            kotlin.runCatching {
                contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME).takeIf { it != -1 }
                        ?.let { cursor.getString(it) }
                }
            }.getOrNull()
    }
}
