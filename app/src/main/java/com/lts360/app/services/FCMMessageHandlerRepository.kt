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


    fun storeMessagePart(messageKey: String, partNumber: Int, totalParts: Int, chunkData: String) {
        sharedPreferences.edit {
            val key = "$messageKey-$partNumber"
            putString(key, chunkData)
            putInt("$messageKey-totalParts", totalParts)
            apply()
        }

    }

    fun areAllPartsReceived(messageKey: String, totalParts: Int): Boolean {
        for (i in 1..totalParts) {
            val partKey = "$messageKey-$i"
            if (sharedPreferences.getString(partKey, null) == null) {
                return false
            }
        }
        return true
    }

    fun reassembleMessage(messageKey: String, totalParts: Int): String {
        val messageBuilder = StringBuilder()

        for (i in 1..totalParts) {
            val partKey = "$messageKey-$i"
            val partData = sharedPreferences.getString(partKey, null)
            if (partData != null) {
                messageBuilder.append(partData)

                sharedPreferences.edit {
                    remove(partKey).apply()
                }
            }
        }

        sharedPreferences.edit {
            remove("$messageKey-totalParts").apply()
        }

        return messageBuilder.toString()
    }

    fun processMessage(
        applicationContext: Context,
        userId: Long,
        chunkData: String,
    ) {

        val remoteMessageData = Gson().fromJson(chunkData, JsonObject::class.java)
        val remoteMessageType = remoteMessageData.get("type").asString



        if (remoteMessageType == "general") {

            val title = remoteMessageData.get("title").asString
            val data = remoteMessageData.get("data").asString

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
        }
        else if (remoteMessageType == "new_chat_message") {
            ChatMessageHandlerWorkerHelper.enqueueFetchChatMessages(
                applicationContext as App,
                userId,
                remoteMessageType,
                remoteMessageData.toString()
            )
        } else if (remoteMessageType == "business_local_job_application") {
            val title = remoteMessageData.get("title").asString
            val data = remoteMessageData.get("data").asString
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