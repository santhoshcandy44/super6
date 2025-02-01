package com.super6.pot.api.auth.services

import com.super6.pot.api.common.responses.ResponseReply
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST


interface AuthService {



    @FormUrlEncoded
    @POST("api/auth/register")
    suspend fun sendEmailVerificationOTP(
        @Field("email") email: String
    ): Response<ResponseReply>


    @FormUrlEncoded
    @POST("api/auth/register-verify-otp")
    suspend fun verifyEmail(
        @Field("otp") otp: String,
        @Field("first_name") firstName: String,
        @Field("last_name") lastName: String,
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("account_type") accountType: String,
//        @Field("country") country: String,
//        @Field("is_terms_and_conditions_accepted") isTermsAndConditionsAccepted: Boolean
    ): Response<ResponseReply>


    @FormUrlEncoded
    @POST("api/auth/google-sign-up")
    suspend fun googleSignUpRegister(
        @Field("id_token") idToken: String,
        @Field("account_type") accountType: String,
        @Field("sign_up_method") method: String,
    ): Response<ResponseReply>


    @FormUrlEncoded
    @POST("api/auth/forgot-password")
    suspend fun validateEmailForgotPassword(
        @Field("email") email: String,
    ): Response<ResponseReply>

    @FormUrlEncoded
    @POST("api/auth/forgot-password-verify-otp")
    suspend fun verifyEmailForgotPassword(
        @Field("email") email: String,
        @Field("otp") otp: String,
    ): Response<ResponseReply>

    @FormUrlEncoded
    @POST("api/auth/reset-password")
    suspend fun resetPassword(
        @Header("Authorization") authToken: String,  // Authorization header
        @Field("email") email: String,
        @Field("password") password: String,
    ): Response<ResponseReply>


    @FormUrlEncoded
    @POST("api/auth/legacy-email-login")
    suspend fun legacyEmailLogin(
        @Field("email") email: String?,
        @Field("password") password: String?
    ): Response<ResponseReply>


    @FormUrlEncoded
    @POST("api/auth/google-sign-in")
    suspend fun googleLogin(
        @Field("id_token") idToken: String?,
        @Field("sign_in_method") method: String,
    ): Response<ResponseReply>

    @POST("api/auth/refresh-token")
    suspend fun refreshToken(
        @Header("Authorization") refreshToken: String,
    ): Response<ResponseReply>


}