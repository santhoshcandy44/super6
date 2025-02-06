package com.super6.pot.api.auth.services

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Streaming
import retrofit2.http.Url


interface CommonService{

    @Headers("Accept-Encoding: *")  // Avoid compression
    @Streaming  // Allows downloading the file in chunks, not loading it all in memory
    @GET
    suspend fun downloadMedia(
        @Url imageUrl: String,
        @Header("Range") range: String? = null // Optional Range header
    ): ResponseBody


    @Headers("Accept-Encoding: *")  // Avoid compression
    @Streaming  // Allows downloading the file in chunks, not loading it all in memory
    @GET
    suspend fun downloadMediaResponse(
        @Url imageUrl: String,
        @Header("Range") range: String? = null // Optional Range header
    ): Response<ResponseBody>


}

