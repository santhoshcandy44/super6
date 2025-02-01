package com.super6.pot.api.app

import com.super6.pot.api.common.responses.ResponseReply
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.PUT
import retrofit2.http.Part


interface ProfileSettingsService{

    @FormUrlEncoded
    @PATCH("api/serve/profile/update-first-name")
    suspend fun updateFirstName(
        @Field("user_id") userId: Long,
        @Field("first_name") firstName: String,
    ): Response<ResponseReply>


    @FormUrlEncoded
    @PATCH("api/serve/profile/update-last-name")
    suspend fun updateLastName(
        @Field("user_id") userId: Long,
        @Field("last_name") lastName: String,
    ): Response<ResponseReply>



    @FormUrlEncoded
    @PATCH("api/serve/profile/update-about")
    suspend fun updateAbout(
        @Field("user_id") userId: Long,
        @Field("about") about: String,
    ): Response<ResponseReply>


    @Multipart
    @PATCH("api/serve/profile/update-profile-pic")
    suspend fun uploadProfileImage(
        @Part("user_id") userId: Long,
        @Part profilePic: MultipartBody.Part,
    ): Response<ResponseReply>



    @FormUrlEncoded
    @PATCH("api/serve/profile/update-email")
    suspend fun changeEmailValidate(
        @Field("user_id") userId:Long,
        @Field("email") email: String?,
    ): Response<ResponseReply>


    @FormUrlEncoded
    @PATCH("api/serve/profile/update-email-verify-otp")
    suspend fun editEmailEmailVerification(
        @Field("user_id") userId: Long,
        @Field("email") email: String,
        @Field("otp") otp: String,
    ): Response<ResponseReply>


    @FormUrlEncoded
    @PUT("api/serve/profile/update-location")
    suspend fun updateUserLocation(
        @Field("user_id") userId: Long,
        @Field("latitude") lat: Double,
        @Field("longitude") long: Double,
        @Field("geo") geo: String,
        @Field("location_type") locationType: String,
    ): Response<ResponseReply>

}