package com.lts360.app.services


import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.lts360.app.workers.helpers.SendFcmTokenWorkerHelper
import com.lts360.compose.ui.managers.UserSharedPreferencesManager
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

        if (partNumber == 1 && totalParts == 1) {
            fcmMessageHandlerRepository.processMessage(applicationContext, userId, chunkData)
        } else {

            val messageKey = remoteMessageData["key"] ?: return

            fcmMessageHandlerRepository.storeMessagePart(
                messageKey,
                partNumber,
                totalParts,
                chunkData
            )

            if (fcmMessageHandlerRepository.areAllPartsReceived(messageKey, totalParts)) {
                val fullMessage =
                    fcmMessageHandlerRepository.reassembleMessage(messageKey, totalParts)
                fcmMessageHandlerRepository.processMessage(applicationContext, userId, fullMessage)
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


