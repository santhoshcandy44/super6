package com.super6.pot.app.services


import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.super6.pot.ui.managers.UserSharedPreferencesManager
import com.super6.pot.app.workers.helpers.SendFcmTokenWorkerHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FirebaseMessagingService : FirebaseMessagingService() {


    @Inject
    lateinit var fcmMessageHandlerRepository: FCMMessageHandlerRepository


    val userId = UserSharedPreferencesManager.userId



    override fun onMessageReceived(remoteMessage: RemoteMessage) {


        val remoteMessageData = remoteMessage.data

        val partNumber = remoteMessageData["partNumber"]?.toInt() ?: return
        val totalParts = remoteMessageData["totalParts"]?.toInt() ?: return
        val chunkData = remoteMessageData["data"] ?: return
        val messageId = remoteMessageData["messageId"]


        if (partNumber == 1 && totalParts == 1) {
            fcmMessageHandlerRepository.processMessage(applicationContext, userId, chunkData,messageId?.toLong()?:-1)
        }else{
            // Store the part data
            fcmMessageHandlerRepository.storeMessagePart(messageId, partNumber, totalParts, chunkData)

            // Check if all parts are received
            if (fcmMessageHandlerRepository.areAllPartsReceived(messageId, totalParts)) {
                val fullMessage = fcmMessageHandlerRepository.reassembleMessage(messageId, totalParts)
                fcmMessageHandlerRepository.processMessage(applicationContext, userId, fullMessage,messageId?.toLong()?:-1)
            }
        }
    }




    override fun onNewToken(token: String) {
        super.onNewToken(token)
        if (userId != -1L) {
            SendFcmTokenWorkerHelper.cancelSendFCMTokenToServerUniqueWork(application)
            SendFcmTokenWorkerHelper.enqueueSendFCMTokenToServerWork(application, token)
        }
    }
}