package com.super6.pot.api.app

import com.super6.pot.api.common.responses.ResponseReply
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT


interface  AccountSettingsService{

    @FormUrlEncoded
    @PATCH("api/app/serve/account-settings/update-account-type")
    suspend fun updateAccountType(
        @Field("user_id") userId: Long,
        @Field("account_type") about: String,
    ): Response<ResponseReply>


    @FormUrlEncoded
    @PUT("api/app/serve/account-settings/change-password")
    suspend fun changePassword(
        @Field("user_id") userId: Long,
        @Field("current_password") currentPassword: String, // Pass industries as a JSON string
        @Field("new_password") newPassword: String, // Pass industries as a JSON string
    ): Response<ResponseReply>


    @FormUrlEncoded
    @POST("api/app/serve/account-settings/forgot-password")
    suspend fun validateEmailForgotPassword(
        @Field("user_id") userId: Long,
        @Field("email") email: String,
    ): Response<ResponseReply>


    @FormUrlEncoded
    @POST("api/app/serve/account-settings/forgot-password-verify-otp")
    suspend fun verifyEmailForgotPassword(
        @Field("user_id") userId: Long,
        @Field("email") email: String,
        @Field("otp") otp: String,
    ): Response<ResponseReply>

    @FormUrlEncoded
    @POST("api/app/serve/account-settings/reset-password")
    suspend fun resetPassword(
        @Field("user_id") userId: Long,
        @Field("auth_token") authToken: String,  // Authorization header
        @Field("email") email: String,
        @Field("password") password: String,
    ): Response<ResponseReply>


}