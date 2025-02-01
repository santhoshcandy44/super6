package com.super6.pot.api.common.responses

import com.google.gson.annotations.SerializedName


data class ResponseReply(
    @SerializedName("isSuccessful") val isSuccessful: Boolean,
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data:String="",
)
