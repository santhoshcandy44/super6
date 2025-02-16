package com.lts360.compose.ui.chat.viewmodels.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.lts360.App
import com.lts360.app.database.daos.chat.ChatUserDao
import com.lts360.app.database.daos.chat.MessageDao
import com.lts360.app.database.daos.chat.MessageMediaMetaDataDao
import com.lts360.compose.ui.chat.repos.ChatUserRepository
import com.lts360.compose.ui.chat.viewmodels.ChatActivityViewModel


class ChatActivityViewModelFactory(
    val application: App,
    private val chatUserDao: ChatUserDao,
    private val messageDao: MessageDao,
    private val senderId: Long,
    private val messageMediaMetaDataDao: MessageMediaMetaDataDao,

    ) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatActivityViewModel::class.java)) {

            return ChatActivityViewModel(
                application,
                messageDao,
                chatUserDao,
                senderId,
                ChatUserRepository(application.applicationContext, messageDao, chatUserDao, messageMediaMetaDataDao)
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }

}