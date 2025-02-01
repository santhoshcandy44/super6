package com.super6.pot.app.database.models.chat


data class ChatUserWithDetails(
    val chatUser: ChatUser,
    val lastMessage: Message?,
    val unreadCount: Int,
    val messages: List<MessageWithReply>
)