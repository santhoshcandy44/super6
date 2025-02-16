package com.lts360.compose.ui.chat

import android.content.ContentResolver
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.math.abs


const val MIN_WIDTH = 150
const val MIN_HEIGHT = 150
const val MAX_WIDTH = 4000
const val MAX_HEIGHT = 2416
const val MAX_IMAGES = 8

data class ImageValidationResult(
    val width: Int,
    val height: Int,
    val format: ImageFormats,
    val isValidDimension: Boolean,
    val isValidFormat: Boolean)

fun isValidImageDimensions(context: Context, uri: Uri): ImageValidationResult {
    val format = getImageFormatFromUri(context.contentResolver, uri)

    val isValidFormat: Boolean = format != ImageFormats.UNKNOWN
    var inputStream: InputStream? = null
    var isValidDimension = false
    var width = 0
    var height = 0

    try {
        // Open the input stream
        inputStream = context.contentResolver.openInputStream(uri)

        inputStream?.let {

            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }

            // Decode the image to get the bounds
            BitmapFactory.decodeStream(it, null, options)

            // Get width and height from the options
            width = options.outWidth
            height = options.outHeight


            // Check if dimensions are within the valid range
            isValidDimension = (width in MIN_WIDTH..MAX_WIDTH) && (height in MIN_HEIGHT..MAX_HEIGHT)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        // Close the input stream to avoid resource leaks
        inputStream?.close()
    }

    // Return the validity status, width, height, and format as a data class
    return ImageValidationResult(width, height, format, isValidDimension, isValidFormat)
}


fun isValidThumbnailDimensionsFormat(context: Context, uri: Uri): ImageValidationResult {


    var inputStream: InputStream? = null
    var isValidDimension = false

    val format = getImageFormatFromUri(context.contentResolver, uri)

    val isValidFormat: Boolean = format != ImageFormats.UNKNOWN

    // Define minimum and maximum dimensions for a valid thumbnail (16:9 aspect ratio)
    val MIN_WIDTH = 1280 // Example minimum width
    val MIN_HEIGHT = 720  // Example minimum height
    val MAX_WIDTH = 3840  // Example maximum width
    val MAX_HEIGHT = 2160  // Example maximum height

    val tolerance = 0.01  // 1% tolerance
    val desiredRatio = 16f / 9f
    var width = 0
    var height = 0


    try {
        // Open the input stream
        inputStream = context.contentResolver.openInputStream(uri)

        inputStream?.let {

            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }

            // Decode the image to get the bounds
            BitmapFactory.decodeStream(it, null, options)

            // Get width and height from the options
            width = options.outWidth
            height = options.outHeight

            // Check if dimensions are within the valid range and have a 16:9 aspect ratio
            val aspectRatio = width.toFloat() / height.toFloat()
            // Check if dimensions are within the valid range and have a 16:9 aspect ratio
            isValidDimension = (width in MIN_WIDTH..MAX_WIDTH) &&
                    (height in MIN_HEIGHT..MAX_HEIGHT) &&
                    abs(aspectRatio - desiredRatio) < tolerance
        }

    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        // Close the input stream to avoid resource leaks
        inputStream?.close()
    }

    // Return the validity status, the bitmap (if any), and the format
    return ImageValidationResult(width, height, format, isValidDimension, isValidFormat)
}



fun isValidImageDimensionsByMetaData(width:Int, height: Int, format: String):  Pair<Boolean, Boolean> {
    val imageFormat = getImageFormatFromString(format)
    val isValidFormat: Boolean = imageFormat != ImageFormats.UNKNOWN

    var isValidDimension = false

    try {
        isValidDimension = (width in MIN_WIDTH..MAX_WIDTH) && (height in MIN_HEIGHT..MAX_HEIGHT)
    } catch (e: Exception) {
        e.printStackTrace()
    }

    // Return the validity status, width, height, and format as a data class
    return Pair(isValidDimension, isValidFormat)
}


fun isValidThumbnailDimensionsByMetaData(width:Int, height: Int, format: String): Pair<Boolean, Boolean> {

    val imageFormat = getImageFormatFromString(format)
    val isValidFormat: Boolean = imageFormat != ImageFormats.UNKNOWN

    var isValidDimension = false

    // Define minimum and maximum dimensions for a valid thumbnail (16:9 aspect ratio)
    val MIN_WIDTH = 1280 // Example minimum width
    val MIN_HEIGHT = 720  // Example minimum height
    val MAX_WIDTH = 3840  // Example maximum width
    val MAX_HEIGHT = 2160  // Example maximum height

    val tolerance = 0.01  // 1% tolerance
    val desiredRatio = 16f / 9f


    try {
        // Check if dimensions are within the valid range and have a 16:9 aspect ratio
        val aspectRatio = width.toFloat() / height.toFloat()
        // Check if dimensions are within the valid range and have a 16:9 aspect ratio
        isValidDimension = (width in MIN_WIDTH..MAX_WIDTH) &&
                (height in MIN_HEIGHT..MAX_HEIGHT) &&
                Math.abs(aspectRatio - desiredRatio) < tolerance
    } catch (e: Exception) {
        e.printStackTrace()
    }

    // Return the validity status, the bitmap (if any), and the format
    return Pair(isValidDimension, isValidFormat)
}




fun getImageFormatFromUri(contentResolver: ContentResolver, uri: Uri): ImageFormats {
    val mimeType = contentResolver.getType(uri) ?: return ImageFormats.UNKNOWN
    return getImageFormatFromString(mimeType)
}

fun getImageFormatFromString(mimeType: String): ImageFormats {
    return when (mimeType) {
        "image/jpg", "image/jpeg" -> ImageFormats.JPEG
        "image/png" -> ImageFormats.PNG
        else -> ImageFormats.UNKNOWN
    }
}

sealed class ImageFormats {
    object JPEG : ImageFormats() {

        override fun toString(): String {
            return "image/jpeg"
        }

    }

    object PNG : ImageFormats() {
        override fun toString(): String {
            return "image/png"
        }

    }
    object UNKNOWN : ImageFormats() {

        override fun toString(): String {
            return "Unknown"
        }
    }
}


fun createImagePartForSingleUri(
    context: Context,
    uri: Uri,
    isSingle: Boolean,
    onFileCreated: (File) -> Unit,
): MultipartBody.Part {
    return createImagePart(context, uri, isSingle, onFileCreated)
}

fun createImagePartForUri(
    context: Context,
    uri: Uri,
    fileName: String,
    format: String,
    name: String
): MultipartBody.Part? {
    return context.contentResolver.openInputStream(uri)?.use { inputStream ->
        MultipartBody.Part.createFormData(
            name,
            fileName,
            inputStream.readBytes().toRequestBody(format.toMediaTypeOrNull())
        )
    }
}


val getFileExtensionFromImageFormat: (String) -> String = { format ->
    when (getImageFormatFromString(format)) {
        ImageFormats.PNG -> ".png"
        ImageFormats.JPEG -> ".jpg"
        else -> throw IllegalArgumentException("Unsupported image format")
    }
}


fun createImagePart(
    context: Context,
    uri: Uri,
    isSingle: Boolean,
    onFileCreated: (File) -> Unit,
): MultipartBody.Part {
    val file = uriToFile(context, uri) { createdFile ->
        onFileCreated(createdFile)
    }


    return if (isSingle) {
        val mediaType = "image/*".toMediaType()
        val requestFile = file.asRequestBody(mediaType)
        MultipartBody.Part.createFormData("image", file.name, requestFile)
    } else {
        val mediaType = "image/*".toMediaType()
        val requestFile = file.asRequestBody(mediaType)
        MultipartBody.Part.createFormData("images[]", file.name, requestFile)
    }
}


private fun uriToFile(context: Context, uri: Uri, onFileCreated: (File) -> Unit): File {
    val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
    val file = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
    val outputStream = FileOutputStream(file)

    inputStream?.copyTo(outputStream)
    inputStream?.close()
    outputStream.close()

    onFileCreated(file)

    return file
}

