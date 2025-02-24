package com.lts360.compose.ui.main.models

import com.google.gson.annotations.SerializedName

data class ServiceReview(
    @SerializedName("id")
    val id: Int,

    @SerializedName("text")
    val text: String,

    @SerializedName("timestamp")
    val timestamp: Long,

    @SerializedName("user")
    val user: CommentUser
)