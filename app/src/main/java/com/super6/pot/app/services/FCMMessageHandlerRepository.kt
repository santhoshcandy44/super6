package com.super6.pot.app.services

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.super6.pot.App
import com.super6.pot.app.database.daos.notification.NotificationDao
import com.super6.pot.app.database.models.notification.Notification
import com.super6.pot.database.services.buildAndShowGeneralNotification
import com.super6.pot.app.workers.helpers.ChatMessageHandlerWorkerHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class FCMMessageHandlerRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationDao: NotificationDao
)  {



    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("FCM_MESSAGE_PARTS", MODE_PRIVATE)



    fun storeMessagePart(messageId: String?, partNumber: Int, totalParts: Int, chunkData: String) {
        val editor = sharedPreferences.edit()
        // Save the part data using the messageId and partNumber as key
        val key = "$messageId-$partNumber"
        editor.putString(key, chunkData)
        editor.putInt("$messageId-totalParts", totalParts)
        editor.apply()
    }

    fun areAllPartsReceived(messageId: String?, totalParts: Int): Boolean {
        // Check if all parts for the message are stored
        for (i in 1..totalParts) {
            val partKey = "$messageId-$i"
            if (sharedPreferences.getString(partKey, null) == null) {
                return false // Missing part
            }
        }
        return true
    }

    fun reassembleMessage(messageId: String?, totalParts: Int): String {
        val messageBuilder = StringBuilder()

        // Collect all parts in order
        for (i in 1..totalParts) {
            val partKey = "$messageId-$i" // Create the key for each part
            val partData = sharedPreferences.getString(partKey, null) // Get the stored part data

            if (partData != null) {
                messageBuilder.append(partData) // Append to the final message

                // After using the part, remove it from SharedPreferences
                sharedPreferences.edit().remove(partKey).apply()
            }
        }

        sharedPreferences.edit().remove("$messageId-totalParts").apply()


        return messageBuilder.toString() // Return the fully reassembled message
    }

    fun processMessage(applicationContext: Context, userId:Long, chunkData: String, messageId: Long) {

        val remoteMessageData = Gson().fromJson(chunkData, JsonObject::class.java)
        val title = remoteMessageData.get("title").asString
        val data = remoteMessageData.get("data").asString
        val remoteMessageType = remoteMessageData.get("type").asString


        if (remoteMessageType == "general") {

            if (title != null && data != null) {


                val messageData = Gson().fromJson(data, JsonObject::class.java)
                val type = messageData.get("type").asString
                val message = messageData.get("message").asString


                val notification = Notification(
                    title = title,
                    message = message,
                    timestamp = System.currentTimeMillis(),
                    status = "un_read",
                    type = "general")

                notificationDao.insert(notification)


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
        }
    }


}