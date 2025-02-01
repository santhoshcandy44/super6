package com.super6.pot.api.auth.models

import com.google.gson.annotations.SerializedName


// Data class for the token response

data class TokenResponse(
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("refresh_token")
    val refreshToken: String
)

