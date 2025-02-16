package com.lts360.app.database.models.chat


data class ChatUserWithDetails(
    val chatUser: ChatUser,
    val lastMessage: Message?,
    val unreadCount: Int,
    val messages: List<MessageWithReply>
)