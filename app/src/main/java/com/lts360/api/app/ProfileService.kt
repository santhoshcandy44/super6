package com.lts360.api.app

import com.lts360.api.common.responses.ResponseReply
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path


interface  ProfileService{

    @GET("api/serve/profile/{user_id}")
    suspend fun getUserProfile(
        @Path("user_id") userId: Long,
    ): Response<ResponseReply>



    @FormUrlEncoded
    @POST("api/serve/profile/logout")
    suspend fun logout(
        @Field("user_id")
        userId: Long,
    ): Response<ResponseReply>

}
