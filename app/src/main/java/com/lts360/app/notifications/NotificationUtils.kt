package com.lts360.pot.database.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.widget.RemoteViews
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.IconCompat
import androidx.core.graphics.drawable.toBitmap
import coil3.ImageLoader
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.lts360.R
import com.lts360.api.models.service.FeedUserProfileInfo
import com.lts360.app.database.models.chat.Message
import com.lts360.components.isMainActivityInStack

import com.lts360.components.utils.LogUtils.TAG
import com.lts360.compose.ui.chat.ChatActivity
import com.lts360.compose.ui.getRoundedBitmap
import com.lts360.compose.ui.main.MainActivity
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
        .setContentIntent(
            PendingIntent.getActivity(
                context,
                requestID,
                Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                },
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        )
        .setAutoCancel(true)

    val notificationManager = NotificationManagerCompat.from(context)

    val name = "General"
    val descriptionText = "Notifications for general updates"
    val importance = NotificationManager.IMPORTANCE_DEFAULT
    val channel = NotificationChannel(GENERAL_CHANNEL_ID, name, importance).apply {
        description = descriptionText
    }
    val notificationManagerChannel = context.getSystemService(NotificationManager::class.java)
    notificationManagerChannel.createNotificationChannel(channel)

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


data class LocalJobApplicantNotification(
    @SerializedName("applicant_id")
    val applicantId: Long,
    @SerializedName("user")
    val user: FeedUserProfileInfo,
    @SerializedName("local_job_title")
    var localJobTitle: String
)


@RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
suspend fun buildAndShowLocalJobApplicationNotification(
    context: Context,
    data: String
) {


    val localJobApplicantNotification =
        Gson().fromJson(data, LocalJobApplicantNotification::class.java)

    val applicantName = buildString {
        append(localJobApplicantNotification.user.firstName)
        val lastName = localJobApplicantNotification.user.lastName
        if (!lastName.isNullOrBlank()) {
            append(" ")
            append(lastName)
        }
    }


    val localJobTitle = localJobApplicantNotification.localJobTitle

    val profileBitmap = localJobApplicantNotification.user.profilePicUrl?.let {
        loadBitmapFromUrl(context, it)
    }

    val manager = context.getSystemService(NotificationManager::class.java)

    val groupId = "business_group"

    val group = NotificationChannelGroup(groupId, "Business")
    manager.createNotificationChannelGroup(group)

    val channelId = "local_job_application_channel"

    val channel = NotificationChannel(
        channelId, "Local Job Application Alerts", NotificationManager.IMPORTANCE_DEFAULT
    )

    channel.group = groupId
    manager.createNotificationChannel(channel)


    val collapsedRemoteView = RemoteViews(
        context.packageName,
        R.layout.collapsed_custom_local_job_applicant_applied_notification
    ).apply {
        val baseText = "$applicantName applied for the local job."
        val spannable = SpannableString(baseText)

        val start = baseText.indexOf(applicantName)
        val end = start + applicantName.length

        spannable.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(context, R.color.text_indicator_blue)),
            start,
            end,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        setTextViewText(R.id.text_applicant_name, spannable)
        setTextViewText(R.id.text_job_title, localJobTitle)

        profileBitmap?.let {
            setImageViewBitmap(R.id.image_profile, getCircularBitmap(it))

        } ?: run {
            ContextCompat
                .getDrawable(context, R.drawable.user_placeholder)
                ?.toBitmap()
                ?.let {
                    setImageViewBitmap(R.id.image_profile, getCircularBitmap(it))
                }
        }
    }

    val expandedRemoteView = RemoteViews(
        context.packageName,
        R.layout.expanded_custom_local_job_applicant_applied_notification
    ).apply {
        val baseText = "$applicantName applied for the local job."
        val spannable = SpannableString(baseText)

        val start = baseText.indexOf(applicantName)
        val end = start + applicantName.length

        spannable.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(context, R.color.text_indicator_blue)),
            start,
            end,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        setTextViewText(R.id.text_applicant_name, spannable)
        setTextViewText(R.id.text_job_title, localJobTitle)

        ContextCompat
            .getDrawable(context, R.drawable.user_placeholder)
            ?.toBitmap()
            ?.let {
                setImageViewBitmap(R.id.image_profile, getCircularBitmap(it))
            }

    }


    val notification = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.notification_icon)
        .setCustomContentView(collapsedRemoteView)
        .setCustomBigContentView(expandedRemoteView)
        .setAutoCancel(true)
        .build()

    NotificationManagerCompat.from(context).notify(1001, notification)
}


fun getCircularBitmap(srcBitmap: Bitmap): Bitmap {
    val size = minOf(srcBitmap.width, srcBitmap.height)
    val output = createBitmap(size, size)

    val canvas = android.graphics.Canvas(output)
    val paint = android.graphics.Paint().apply {
        isAntiAlias = true
        shader = android.graphics.BitmapShader(
            srcBitmap,
            android.graphics.Shader.TileMode.CLAMP,
            android.graphics.Shader.TileMode.CLAMP
        )
    }

    val radius = size / 2f
    canvas.drawCircle(radius, radius, radius, paint)

    return output
}


suspend fun loadBitmapFromUrl(context: Context, imageUrl: String): Bitmap? {
    val loader = ImageLoader(context)

    val request = ImageRequest.Builder(context)
        .data(imageUrl)
        .allowHardware(false)
        .build()

    val result = loader.execute(request)


    return if (result is SuccessResult) {
        getCircularBitmap(result.image.toBitmap())
    } else {
        null
    }
}