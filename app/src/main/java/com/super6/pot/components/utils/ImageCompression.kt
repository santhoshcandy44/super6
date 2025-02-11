package com.super6.pot.components.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint

import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException


private const val maxHeight = 1280.0f
private const val maxWidth = 1280.0f

fun compressImageAsByteArray(bytes: ByteArray): ByteArray {
    var scaledBitmap: Bitmap? = null
    val options = BitmapFactory.Options().apply {
        inJustDecodeBounds = true // First pass to get image dimensions
    }

    // Decode the image bounds (without allocating memory for pixels)
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)

    var actualHeight = options.outHeight
    var actualWidth = options.outWidth

    var imgRatio = actualWidth.toFloat() / actualHeight.toFloat()
    val maxRatio = maxWidth / maxHeight

    // Resize logic
    if (actualHeight > maxHeight || actualWidth > maxWidth) {
        if (imgRatio < maxRatio) {
            imgRatio = maxHeight / actualHeight
            actualWidth = (imgRatio * actualWidth).toInt()
            actualHeight = maxHeight.toInt()
        } else if (imgRatio > maxRatio) {
            imgRatio = maxWidth / actualWidth
            actualHeight = (imgRatio * actualHeight).toInt()
            actualWidth = maxWidth.toInt()
        } else {
            actualHeight = maxHeight.toInt()
            actualWidth = maxWidth.toInt()
        }
    }

    // Calculate the sample size based on the desired size
    options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight)
    options.inJustDecodeBounds = false // Now decode the actual image

    // Decode the image to a bitmap
    val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)

    // Create a scaled bitmap
    try {
        scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.RGB_565)
    } catch (exception: OutOfMemoryError) {
        exception.printStackTrace()
    }

    val ratioX = actualWidth / options.outWidth.toFloat()
    val ratioY = actualHeight / options.outHeight.toFloat()
    val middleX = actualWidth / 2.0f
    val middleY = actualHeight / 2.0f

    val scaleMatrix = Matrix().apply {
        setScale(ratioX, ratioY, middleX, middleY)
    }

    val canvas = Canvas(scaledBitmap!!)
    canvas.setMatrix(scaleMatrix)
    canvas.drawBitmap(bmp, middleX - bmp.width / 2, middleY - bmp.height / 2, Paint(Paint.FILTER_BITMAP_FLAG))

    bmp?.recycle() // Free up memory for original bitmap

    // Handle Exif data (rotation based on orientation)
    try {
        val exif = ExifInterface(ByteArrayInputStream(bytes))
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0)
        val matrix = Matrix()
        when (orientation) {
            6 -> matrix.postRotate(90f)
            3 -> matrix.postRotate(180f)
            8 -> matrix.postRotate(270f)
        }
        scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.width, scaledBitmap.height, matrix, true)
    } catch (e: IOException) {
        e.printStackTrace()
    }

    // Write the compressed bitmap into a ByteArrayOutputStream
    val byteArrayOutputStream = ByteArrayOutputStream()
    scaledBitmap?.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)

    // Return the byte array of the compressed image
    return byteArrayOutputStream.toByteArray()
}

fun compressImageAsBitmap(bytes: ByteArray): Bitmap?{
    var scaledBitmap: Bitmap? = null
    val options = BitmapFactory.Options().apply {
        inJustDecodeBounds = true // First pass to get image dimensions
    }

    // Decode the image bounds (without allocating memory for pixels)
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)

    var actualHeight = options.outHeight
    var actualWidth = options.outWidth

    var imgRatio = actualWidth.toFloat() / actualHeight.toFloat()
    val maxRatio = maxWidth / maxHeight

    // Resize logic
    if (actualHeight > maxHeight || actualWidth > maxWidth) {
        if (imgRatio < maxRatio) {
            imgRatio = maxHeight / actualHeight
            actualWidth = (imgRatio * actualWidth).toInt()
            actualHeight = maxHeight.toInt()
        } else if (imgRatio > maxRatio) {
            imgRatio = maxWidth / actualWidth
            actualHeight = (imgRatio * actualHeight).toInt()
            actualWidth = maxWidth.toInt()
        } else {
            actualHeight = maxHeight.toInt()
            actualWidth = maxWidth.toInt()
        }
    }

    // Calculate the sample size based on the desired size
    options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight)
    options.inJustDecodeBounds = false // Now decode the actual image

    // Decode the image to a bitmap
    var bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)

    // Create a scaled bitmap
    try {
        scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.RGB_565)
    } catch (exception: OutOfMemoryError) {
        exception.printStackTrace()
    }

    val ratioX = actualWidth / options.outWidth.toFloat()
    val ratioY = actualHeight / options.outHeight.toFloat()
    val middleX = actualWidth / 2.0f
    val middleY = actualHeight / 2.0f

    val scaleMatrix = Matrix().apply {
        setScale(ratioX, ratioY, middleX, middleY)
    }

    val canvas = Canvas(scaledBitmap!!)
    canvas.setMatrix(scaleMatrix)
    canvas.drawBitmap(bmp, middleX - bmp.width / 2, middleY - bmp.height / 2, Paint(Paint.FILTER_BITMAP_FLAG))

    bmp?.recycle() // Free up memory for original bitmap

    // Handle Exif data (rotation based on orientation)
    try {
        val exif = ExifInterface(ByteArrayInputStream(bytes))
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0)
        val matrix = Matrix()
        when (orientation) {
            6 -> matrix.postRotate(90f)
            3 -> matrix.postRotate(180f)
            8 -> matrix.postRotate(270f)
        }
        scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.width, scaledBitmap.height, matrix, true)
    } catch (e: IOException) {
        e.printStackTrace()
        return null
    }

    // Return the byte array of the compressed image
    return scaledBitmap
}

private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
    val height = options.outHeight
    val width = options.outWidth
    var inSampleSize = 1

    if (height > reqHeight || width > reqWidth) {
        val heightRatio = Math.round(height.toFloat() / reqHeight.toFloat())
        val widthRatio = Math.round(width.toFloat() / reqWidth.toFloat())
        inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio
    }

    val totalPixels = width * height
    val totalReqPixelsCap = reqWidth * reqHeight * 2
    while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
        inSampleSize++
    }

    return inSampleSize
}
