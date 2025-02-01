package com.super6.pot.app.database.models.chat

import androidx.room.ColumnInfo


data class UnreadCount(
    @ColumnInfo(name = "chat_id")
    val chatId: Int,
    val unreadCount: Int
)