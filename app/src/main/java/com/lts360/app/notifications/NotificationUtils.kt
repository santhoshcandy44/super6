package com.lts360.app.notifications

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
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.content.ContextCompat
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
import com.lts360.compose.ui.getCircularBitmap
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

    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        notificationManager.notify(
            NotificationIdManager.getNotificationId(),
            notificationBuilder.build()
        )
    } else {
        Log.d(TAG, "Notification permission not granted")
    }
}


suspend fun buildAndShowChatNotification(
    context: Context,
    senderId: Long,
    title: String,
    messageBody: List<Message>,
    profilePicUrl: String?,
    unreadMessageCount: Int,
) {

    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val imageLoader = ImageLoader(context)

        val bitmap = withContext(Dispatchers.IO) {
            val result = imageLoader.execute(
                ImageRequest.Builder(context)
                    .data(profilePicUrl)
                    .allowHardware(false)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .build()
            )
            result.image?.toBitmap()
        }

        val sender = Person.Builder()
            .setName(null)
            .setIcon(
                if (bitmap != null) IconCompat.createWithBitmap(getCircularBitmap(bitmap)) else IconCompat.createWithResource(
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
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .apply {
                if (unreadMessageCount > 0) {
                    setNumber(unreadMessageCount)
                }
            }
            .setOnlyAlertOnce(true)
            .setStyle(style)

        val notificationId = NotificationIdManager.getNotificationIdForChatNotification(senderId)

        NotificationManagerCompat.from(context)
            .run {
                cancel(notificationId)
                createNotificationChannelGroup(NotificationChannelGroup(CHAT_GROUP_ID, "Chats"))
                context.getSystemService(NotificationManager::class.java).createNotificationChannel(
                    NotificationChannel(
                        CHAT_CHANNEL_ID,
                        "Messages",
                        NotificationManager.IMPORTANCE_HIGH
                    ).apply {
                        description = "Notifications for new messages and chat updates"
                        group = CHAT_GROUP_ID
                    }
                )
                notify(notificationId, notificationBuilder.build())
            }

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


suspend fun buildAndShowLocalJobApplicationNotification(
    context: Context,
    data: String
) {


    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
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

        NotificationManagerCompat.from(context).notify(NotificationIdManager.getNotificationId(), notification)
    }

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