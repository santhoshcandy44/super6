package com.lts360.api.prefs

import com.lts360.api.common.responses.ResponseReply
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Query


interface BoardPreferenceApiService {

    @GET("api/app/serve/boards-settings/get-guest-boards")
    suspend fun getGuestBoards(
        @Query("user_id")
        userId: Long,
    ): Response<ResponseReply>


    @GET("api/app/serve/boards-settings/get-boards")
    suspend fun getBoards(
        @Query("user_id")
        userId: Long,
    ): Response<ResponseReply>

    @FormUrlEncoded
    @PUT("api/app/serve/boards-settings/update-boards")
    suspend fun updateBoards(
        @Field("user_id") userId: Long,
        @Field("boards") industries: String,
    ): Response<ResponseReply>

}