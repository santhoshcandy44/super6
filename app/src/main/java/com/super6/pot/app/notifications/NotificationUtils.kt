package com.super6.pot.database.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.IconCompat
import coil3.ImageLoader
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap
import com.super6.pot.R
import com.super6.pot.app.database.models.chat.Message
import com.super6.pot.components.isMainActivityInStack

import com.super6.pot.components.utils.LogUtils.TAG
import com.super6.pot.compose.ui.chat.ChatActivity
import com.super6.pot.compose.ui.getRoundedBitmap
import com.super6.pot.compose.ui.main.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


private const val GENERAL_CHANNEL_ID = "general_channel"
private const val CHAT_GROUP_ID = "chat_group"
private const val CHAT_CHANNEL_ID = "chat_channel"


fun buildAndShowGeneralNotification(context: Context, title: String?, messageBody: String?) {

    val requestID = System.currentTimeMillis().toInt()

    val notificationBuilder = NotificationCompat.Builder(context, GENERAL_CHANNEL_ID)
        .setSmallIcon(R.drawable.notification_icon)
        .setColor(ContextCompat.getColor(context, R.color.notification_color))
        .setContentTitle(title)
        .setContentText(messageBody)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setContentIntent(PendingIntent.getActivity(
            context,
            requestID,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        ))
        .setAutoCancel(true)

    val notificationManager = NotificationManagerCompat.from(context)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "General"
        val descriptionText = "Notifications for general updates"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(GENERAL_CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManagerChannel = context.getSystemService(NotificationManager::class.java)
        notificationManagerChannel.createNotificationChannel(channel)
    }

    // Check if the permission to post notifications is granted
    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        // Permission is granted, so post the notification
        notificationManager.notify(0, notificationBuilder.build())
    } else {
        // Permission is not granted, handle this case
        Log.d(TAG, "Notification permission not granted")
        // Optionally request permission or notify the user
    }
}


suspend fun buildAndShowChatNotification(
    context: Context,
    notificationId: Int,
    senderId: Long,
    title: String,
    messageBody: List<Message>,
    profilePicUrl: String?,
    unreadMessageCount: Int,
) {
    val intent = Intent(context, MainActivity::class.java)
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

    /*        val pendingIntent = PendingIntent.getActivity(this, 0, intent,PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE )


            val replyLabel = "Enter your reply here"
            val remoteInput = androidx.core.app.RemoteInput.Builder("KEY_REPLY")
                .setLabel(replyLabel)
                .build()

            // Create an intent for the reply action
            val replyIntent = Intent(applicationContext, ReplyReceiver::class.java)

            val replyPendingIntent = PendingIntent.getBroadcast(
                applicationContext,
                0,
                replyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

            )

            val replyAction = NotificationCompat.Action.Builder(
                android.R.drawable.ic_menu_send,
                "Reply",
                replyPendingIntent
            ).addRemoteInput(remoteInput)
                .build()*/


    val imageLoader = ImageLoader(context)

    val request = ImageRequest.Builder(context)
        .data(profilePicUrl)
        .allowHardware(false)
        .diskCachePolicy(CachePolicy.ENABLED) // Enable disk caching (default)
        .memoryCachePolicy(CachePolicy.ENABLED) // Enable memory caching (default)
        .build()

    // Start a coroutine to load the image asynchronously
    withContext(Dispatchers.IO) {
        val result = imageLoader.execute(request)

        if (result is SuccessResult) {
            val bitmap = result.image.toBitmap()
            buildChatNotification(
                context,
                notificationId,
                senderId,
                bitmap,
                title,
                messageBody,
                unreadMessageCount
            )
        } else {
            buildChatNotification(
                context,
                notificationId,
                senderId,
                null,
                title,
                messageBody,
                unreadMessageCount
            )
        }
    }

}




private fun buildChatNotification(
    context: Context,
    notificationId: Int,
    senderId: Long,
    bitmap: Bitmap?,
    title: String,
    messageBody: List<Message>,
    unreadMessageCount: Int,
) {
    val sender = Person.Builder()
        .setName(null)
        .setIcon(
            if (bitmap != null) IconCompat.createWithBitmap(getRoundedBitmap(bitmap)) else IconCompat.createWithResource(
                context,
                R.drawable.user_placeholder
            )
        )
        .build()

    val receiver = Person.Builder()
        .setName("You")
        .build()

    val style = NotificationCompat.MessagingStyle(receiver).also {
        it.setConversationTitle(title)
        messageBody.forEach { message ->
            it.addMessage(message.content, System.currentTimeMillis(), sender)
        }
    }

    val requestID = System.currentTimeMillis().toInt()


    val notificationBuilder = NotificationCompat.Builder(context, CHAT_CHANNEL_ID)
        .setContentIntent(

            if (isMainActivityInStack(context))
                PendingIntent.getActivity(
                    context,
                    requestID,
                    Intent(context, ChatActivity::class.java).apply {
                        putExtra("sender_id", senderId)
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    },
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            else PendingIntent.getActivity(
                context,
                requestID,
                Intent(context, MainActivity::class.java).apply {
                    putExtra("sender_id", senderId)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                },
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        )
        .setSmallIcon(R.drawable.notification_icon)
        .setColor(
            ContextCompat.getColor(
                context,
                R.color.notification_color
            )
        )
        .setAutoCancel(true)  // Automatically dismiss the notification when clicked
        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .apply {
            if (unreadMessageCount > 0) {
                setNumber(unreadMessageCount)
            }
        }
        .setNumber(100)
        .setStyle(style)

    val notificationManager = NotificationManagerCompat.from(context)
    notificationManager.cancel(notificationId)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "Messages"
        val descriptionText = "Notifications for new messages and chat updates"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val notificationManagerChannel = context.getSystemService(NotificationManager::class.java)
        val chatChannelGroup = NotificationChannelGroup(CHAT_GROUP_ID, "Chats")
        notificationManager.createNotificationChannelGroup(chatChannelGroup)
        notificationManagerChannel.createNotificationChannel(
            NotificationChannel(
                CHAT_CHANNEL_ID,
                name,
                importance
            ).apply {
                description = descriptionText
                group = CHAT_GROUP_ID
            }
        )
    }

    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        notificationManager.notify(notificationId, notificationBuilder.build())
    } else {
        Log.d(TAG, "Notification permission not granted.")
    }
}