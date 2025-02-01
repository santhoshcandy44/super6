package com.super6.pot.api.common.errors

import com.google.gson.annotations.SerializedName



data class ErrorResponse(
    @SerializedName("isSuccessful") val isSuccessful: Boolean,
    @SerializedName("data") val data: ResponseData,
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String
)


data class ResponseData(
    @SerializedName("status") val status: String,
    @SerializedName("statusCode") val statusCode: Int,
    @SerializedName("error") val error: ErrorDetails,
    @SerializedName("requestId") val requestId: String,
    @SerializedName("documentation_url") val documentationUrl: String
)



data class ErrorDetails(
    @SerializedName("code") val code: String,
    @SerializedName("message") val message: String,
    @SerializedName("details") val details: String? = null,
    @SerializedName("timestamp") val timestamp: String,
    @SerializedName("path") val path: String
)
