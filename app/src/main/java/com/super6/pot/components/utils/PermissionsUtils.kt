package com.super6.pot.components.utils

import android.app.NotificationManager
import android.content.Context
import android.os.Build


class PermissionsUtils{

    companion object{

        fun isNotificationPermissionGranted(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // For Android 13 and above
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.areNotificationsEnabled()
            } else {
                // For devices below Android 13, notifications are granted by default
                true
            }
        }
    }
}