package com.lts360.api.auth.models

import com.google.gson.annotations.SerializedName
import com.lts360.api.models.service.UserProfileInfo


data class LogInResponse(
    @SerializedName("user_id")
    val userId:Long,
    @SerializedName("access_token")
    val accessToken:String,
    @SerializedName("refresh_token")
    val refreshToken:String,
    @SerializedName("user_details")
    val userDetails: UserProfileInfo

)