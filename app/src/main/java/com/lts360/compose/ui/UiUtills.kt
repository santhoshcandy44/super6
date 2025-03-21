package com.lts360.compose.ui

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Shader
import android.os.Build
import android.view.View
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import java.util.Currency
import java.util.concurrent.TimeUnit


class NoRippleInteractionSource : MutableInteractionSource {

    override val interactions: Flow<Interaction> = emptyFlow()

    override suspend fun emit(interaction: Interaction) {}

    override fun tryEmit(interaction: Interaction) = true
}

fun enterFullScreenMode(activity: Activity, enableSwipeUp: Boolean = true) {
    WindowInsetsControllerCompat(
        activity.window,
        activity.window.decorView
    ).apply {

        hide(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
        if (enableSwipeUp) {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}

fun exitFullScreenMode(activity: Activity) {
    WindowInsetsControllerCompat(
        activity.window,
        activity.window.decorView
    ).apply {
        show(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
        systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
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


fun serviceReviewsFormatTimestamp(timestamp: Long): String {
    val currentTime = System.currentTimeMillis()
    val difference = currentTime - timestamp

    val seconds = TimeUnit.MILLISECONDS.toSeconds(difference)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(difference)
    val hours = TimeUnit.MILLISECONDS.toHours(difference)
    val days = TimeUnit.MILLISECONDS.toDays(difference)
    val years = days / 365

    return when {
        seconds < 60 -> "Just now"
        minutes < 60 -> "$minutes minute${if (minutes > 1) "s" else ""} ago"
        hours < 24 -> "$hours hour${if (hours > 1) "s" else ""} ago"
        days < 365 -> "$days day${if (days > 1) "s" else ""} ago"
        else -> "$years year${if (years > 1) "s" else ""} ago"
    }
}


fun getCurrencySymbol(priceUnit: String): String {
    return try {
        Currency.getInstance(priceUnit).symbol
    } catch (e: IllegalArgumentException) {
        priceUnit // Return the unit itself if invalid (fallback)
    }
}


