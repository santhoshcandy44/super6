package com.super6.pot.compose.ui.chat.viewmodels.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.super6.pot.App
import com.super6.pot.app.database.daos.chat.ChatUserDao
import com.super6.pot.app.database.daos.chat.MessageDao
import com.super6.pot.app.database.daos.chat.MessageMediaMetaDataDao
import com.super6.pot.compose.ui.chat.repos.ChatUserRepository
import com.super6.pot.compose.ui.chat.viewmodels.ChatActivityViewModel


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