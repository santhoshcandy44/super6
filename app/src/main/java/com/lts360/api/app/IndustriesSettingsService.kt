package com.lts360.api.app

import com.lts360.api.common.responses.ResponseReply
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Query

interface IndustriesSettingsService{


    @GET("api/app/serve/industries-settings/get-guest-industries")
    suspend fun getGuestIndustries(
        @Query("user_id")
        userId: Long,
    ): Response<ResponseReply>


    @GET("api/app/serve/industries-settings/get-industries")
    suspend fun getIndustries(
        @Query("user_id")
        userId: Long,
    ): Response<ResponseReply>

    @FormUrlEncoded
    @PUT("api/app/serve/industries-settings/update-industries")
    suspend fun updateIndustries(
        @Field("user_id") userId: Long, // Pass industries as a JSON string
        @Field("industries") industries: String, // Pass industries as a JSON string
    ): Response<ResponseReply>


}
