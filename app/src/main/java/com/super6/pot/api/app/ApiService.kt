package com.super6.pot.api.app

import com.super6.pot.api.auth.models.PublicKeyRequest
import com.super6.pot.api.common.responses.ResponseReply
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {


    @FormUrlEncoded
    @POST("api/app/serve/update-fcm")
    suspend fun updateFcmToken(
        @Field("fcm_token")
        fcmToken: String,
    ): Response<ResponseReply>



    @POST("api/app/serve/update-ee2ee-public-key")
    suspend fun updateE2EEPublicToken(
        @Body publicKeyRequest: PublicKeyRequest
    ): Response<ResponseReply>

    @GET("api/app/serve/get-ee2ee-public-key")
    suspend fun getE2EEPublicToken(): Response<ResponseReply>


}
