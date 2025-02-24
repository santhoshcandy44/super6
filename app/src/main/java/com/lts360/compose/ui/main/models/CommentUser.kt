package com.lts360.compose.ui.main.models

import com.google.gson.annotations.SerializedName


data class CommentUser(
    @SerializedName("full_name")
    val fullName: String,

    @SerializedName("profile_pic_url")
    val profilePicUrl: String,

    @SerializedName("user_id")
    val userId: Long
)
