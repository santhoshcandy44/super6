package com.super6.pot.compose.ui

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




