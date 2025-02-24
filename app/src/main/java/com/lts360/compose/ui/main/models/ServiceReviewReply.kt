package com.lts360.compose.ui.main.models

import com.google.gson.annotations.SerializedName

data class ServiceReviewReply(
    @SerializedName("id")
    val id: Int,

    @SerializedName("text")
    val text: String,

    @SerializedName("timestamp")
    val timestamp: Long,

    @SerializedName("user")
    val user: CommentUser,

    @SerializedName("reply_to_full_name")
    val replyToFullName: String? // Nullable since not all replies may have this field
)