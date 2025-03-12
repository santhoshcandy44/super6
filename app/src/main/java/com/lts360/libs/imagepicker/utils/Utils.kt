package com.lts360.libs.imagepicker.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings


fun redirectToAppSettings(context: Context) {
    // Open the app settings page where the user can enable permissions manually
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.parse("package:${context.packageName}")
    }
    context.startActivity(intent)
}

