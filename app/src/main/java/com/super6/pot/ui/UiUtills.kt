package com.super6.pot.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Shader
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Patterns
import android.view.View
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import java.io.IOException
import java.text.CharacterIterator
import java.text.NumberFormat
import java.text.StringCharacterIterator
import java.util.Currency
import java.util.Locale


fun isUriExist(context: Context, uri: Uri): Boolean {

    return try {
        context.contentResolver.openInputStream(uri)?.close()
        true
    } catch (e: Exception) {
        false
    }
}


fun humanReadableBytesSize(bytes: Long): String {
    val absB = if (bytes == Long.MIN_VALUE) Long.MAX_VALUE else Math.abs(bytes)
    if (absB < 1024) {
        return "${bytes}B"
    }
    var value = absB
    val ci: CharacterIterator = StringCharacterIterator("KMGTPE")
    var i = 40
    while (i >= 0 && absB > 0xfffccccccccccccL shr i) {
        value = value shr 10
        ci.next()
        i -= 10
    }
    value *= java.lang.Long.signum(bytes).toLong()
    return String.format(
        Locale("en", "IN"),
        "%.2f%cB",
        value / 1024.0,
        ci.current()
    ) // Explicit Locale used
}


/**
 * Get file path from URI.
 */
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


fun MediaMetadataRetriever.getMiddleVideoThumbnail(
    context: Context,
    duration: Long,
    uri: Uri
): Bitmap? {


    setDataSource(getPathFromUri(context, uri))

    // Fall back to middle of video
    // Note: METADATA_KEY_DURATION unit is in ms, not us.
    val thumbnailTimeUs: Long = duration * 1000 / 2

    val thumbnail = getFrameAtTime(thumbnailTimeUs)

    release()
    return thumbnail
}


fun MediaMetadataRetriever.getThumbnail(context: Context, uri: Uri, frameAt: Long = 0): Bitmap? {


    val openFileDescriptor = context.contentResolver.openFileDescriptor(uri, "r")
        ?: throw IOException("Failed to open file descriptor")

    setDataSource(openFileDescriptor.fileDescriptor)
    val thumbnail = getFrameAtTime(frameAt)
    openFileDescriptor.close()
    release()
    return thumbnail
}


fun MediaMetadataRetriever.getThumbnailFromPath(path: String): Bitmap? {
    setDataSource(path)
    val thumbnail = getFrameAtTime(0)
    release()
    return thumbnail
}



fun enterFullScreenMode(activity: Activity) {
    val windowInsetsController = WindowInsetsControllerCompat(
        activity.window,
        activity.window.decorView
    )

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        // For Android 11 and above, use the new API for immersive full screen
        windowInsetsController.hide(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE


    } else {
        // For lower versions, use the legacy method
        activity.window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
    }
}

fun exitFullScreenMode(activity: Activity) {
    val windowInsetsController = WindowInsetsControllerCompat(
        activity.window,
        activity.window.decorView
    )

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        windowInsetsController.show(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

    } else {
        // Show the system bars for lower versions
        activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE

    }
}


fun isValidEmail(email: String): Boolean {
    // Implement email validation logic
    return Patterns.EMAIL_ADDRESS.matcher(email).matches()
}


class PlaceholderTransformation(val placeholder: String) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        return PlaceholderFilter(text, placeholder)
    }
}

fun PlaceholderFilter(text: AnnotatedString, placeholder: String): TransformedText {

    val numberOffsetTranslator = object : OffsetMapping {
        override fun originalToTransformed(offset: Int): Int {
            return 0
        }

        override fun transformedToOriginal(offset: Int): Int {
            return 0
        }
    }

    return TransformedText(AnnotatedString(placeholder), numberOffsetTranslator)
}


fun formatCurrency(amount: Double, currencyCode: String): String {
    val locale = when (currencyCode) {
        "INR" -> Locale("en", "IN")  // India Locale for INR
        "USD" -> Locale("en", "US")  // US Locale for USD
        else -> Locale.getDefault()  // Default locale if currency code is unknown
    }

    val currency = Currency.getInstance(currencyCode)

    // Get an instance of NumberFormat for the appropriate currency
    val format = NumberFormat.getCurrencyInstance(locale)
    format.currency = currency  // Set the currency type

    return format.format(amount)
}


fun getRoundedBitmap(bitmap: Bitmap): Bitmap {
    // Ensure the bitmap is square
    val size = Math.min(bitmap.width, bitmap.height)
    val radius = size / 2f

    // Create a new bitmap with a size that can fit the rounded image
    val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)

    // Prepare the paint with a shader to render the image as a circle
    val paint = Paint()
    val shader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
    paint.shader = shader
    paint.isAntiAlias = true

    // Draw a circle on the canvas with the bitmap shader
    canvas.drawCircle(radius, radius, radius, paint)

    return output
}



fun Context.findActivity(): Activity {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    throw IllegalStateException("no activity")
}


// Global function to execute actions based on lifecycle state
fun dropUnlessResumedV2(lifecycleOwner: LifecycleOwner, block: () -> Unit) {
    // Execute the action only if the lifecycle is RESUMED
    if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
        block()
    }
}
