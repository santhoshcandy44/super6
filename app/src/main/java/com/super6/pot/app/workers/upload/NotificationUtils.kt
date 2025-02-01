package com.super6.pot.app.workers.upload

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat



private const val  UPLOAD_CHANNEL_ID = "upload_channel"


// Creating Notification Channel (for devices running Android 8.0 or higher)
fun createUploadNotificationChannel(notificationManager:NotificationManager) {
    val channel = NotificationChannel(
        UPLOAD_CHANNEL_ID,
        "File Upload",
        NotificationManager.IMPORTANCE_LOW
    ).apply {
        description = "Notifications for file upload progress"
    }
    notificationManager.createNotificationChannel(channel)
}

// Function to send or update the notification with the current progress
fun sendUploadNotification(
    context:Context,
    notificationManager:NotificationManager,
    notificationId: Int,
    progress: Int,
    message: String = "Uploading...",
) {
    val notification = NotificationCompat.Builder(context, UPLOAD_CHANNEL_ID)
        .setContentTitle("File Upload")
        .setContentText("$message $progress%")
        .setStyle(NotificationCompat.BigTextStyle().bigText("$message $progress%"))
        .setSmallIcon(android.R.drawable.stat_sys_upload)  // Replace with your icon
        .setProgress(100, progress, false)
        .setPriority(NotificationCompat.PRIORITY_LOW)  // Set priority to low
        .setShowWhen(false)
        .setSound(null)  // No sound
        .setVibrate(longArrayOf(0L))  // Disable vibration
        .setOngoing(true)  // Keep the notification while uploading
        .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)  // Immediate behavior
        .setAutoCancel(false) // Auto-cancel once the upload is complete
        .build()

    notificationManager.notify(notificationId, notification)
}
