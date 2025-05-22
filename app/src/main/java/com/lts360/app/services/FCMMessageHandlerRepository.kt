package com.lts360.app.services

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.lts360.App
import com.lts360.app.database.daos.notification.NotificationDao
import com.lts360.app.database.models.notification.Notification
import com.lts360.app.notifications.buildAndShowGeneralNotification
import com.lts360.app.workers.helpers.ChatMessageHandlerWorkerHelper
import com.lts360.app.notifications.buildAndShowLocalJobApplicationNotification
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class FCMMessageHandlerRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationDao: NotificationDao
) {


    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("FCM_MESSAGE_PARTS", MODE_PRIVATE)


    fun storeMessagePart(messageId: String?, partNumber: Int, totalParts: Int, chunkData: String) {
        sharedPreferences.edit {
            val key = "$messageId-$partNumber"
            putString(key, chunkData)
            putInt("$messageId-totalParts", totalParts)
            apply()
        }

    }

    fun areAllPartsReceived(messageId: String?, totalParts: Int): Boolean {
        for (i in 1..totalParts) {
            val partKey = "$messageId-$i"
            if (sharedPreferences.getString(partKey, null) == null) {
                return false
            }
        }
        return true
    }

    fun reassembleMessage(messageId: String?, totalParts: Int): String {
        val messageBuilder = StringBuilder()

        for (i in 1..totalParts) {
            val partKey = "$messageId-$i"
            val partData = sharedPreferences.getString(partKey, null)
            if (partData != null) {
                messageBuilder.append(partData)

                sharedPreferences.edit {
                    remove(partKey).apply()
                }
            }
        }

        sharedPreferences.edit {
            remove("$messageId-totalParts").apply()
        }

        return messageBuilder.toString()
    }

    fun processMessage(
        applicationContext: Context,
        userId: Long,
        chunkData: String,
        messageId: Long
    ) {

        val remoteMessageData = Gson().fromJson(chunkData, JsonObject::class.java)
        val title = remoteMessageData.get("title").asString
        val data = remoteMessageData.get("data").asString
        val remoteMessageType = remoteMessageData.get("type").asString

        if (remoteMessageType == "general") {
            if (title != null && data != null) {

                val messageData = Gson().fromJson(data, JsonObject::class.java)
                val message = messageData.get("message").asString

                notificationDao.insert(
                    Notification(
                        title = title,
                        message = message,
                        timestamp = System.currentTimeMillis(),
                        status = "un_read",
                        type = "general"
                    )
                )

                if (!App.isAppInForeground) {
                    buildAndShowGeneralNotification(context, title, message)
                }
            }
        } else if (remoteMessageType == "chat_message") {
            ChatMessageHandlerWorkerHelper.enqueueChatMessageProcessor(
                applicationContext as App,
                userId,
                messageId,
                title,
                data,
                remoteMessageType
            )
        } else if (remoteMessageType == "business_local_job_application") {


            notificationDao.insert(
                Notification(
                    title = title,
                    message = "",
                    timestamp = System.currentTimeMillis(),
                    status = "un_read",
                    type = "business_local_job_application",
                    data = data
                )
            )

            if (!App.isAppInForeground) {
                CoroutineScope(Dispatchers.IO).launch {
                    buildAndShowLocalJobApplicationNotification(context, data)
                }
            }
        }
    }

}