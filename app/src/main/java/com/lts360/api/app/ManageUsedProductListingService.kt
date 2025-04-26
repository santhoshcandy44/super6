package com.lts360.api.app

import com.lts360.api.common.responses.ResponseReply
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query


interface ManageUsedProductListingService{

    @GET("api/app/serve/used-product-listings/guest-get-used-product-listings")
    suspend fun guestGetUsedProductListings(
        @Query("user_id") userId: Long,
        @Query("page") page: Int,
        @Query("s") query: String?,
        @Query("last_timestamp") lastTimestamp: String?,
        @Query("last_total_relevance") lastTotalRelevance: String?,
        @Query("latitude") latitude: Double?,
        @Query("longitude") longitude: Double?,
    ): Response<ResponseReply>


    @GET("api/app/serve/used-product-listings/get-used-product-listings")
    suspend fun getUsedProductListings(
        @Query("user_id") userId: Long,
        @Query("page") page: Int,
        @Query("s") query: String?,
        @Query("last_timestamp") lastTimestamp: String?,
        @Query("last_total_relevance") lastTotalRelevance: String?
    ): Response<ResponseReply>

    @GET("api/app/serve/used-product-listings/get-published-used-product-listings/{user_id}")
    suspend fun getUsedProductListingsByUserId(
        @Path("user_id") userId: Long,
    ): Response<ResponseReply>


    @Multipart
    @POST("api/app/serve/used-product-listings/create-or-update-used-product-listing")
    suspend fun createOrUpdateUsedProductListing(
        @Part("product_id") productId: RequestBody,
        @Part("name") name: RequestBody,
        @Part("description") description: RequestBody,
        @Part("price") price: RequestBody,
        @Part("price_unit") priceUnit: RequestBody,
        @Part("country") country: RequestBody,
        @Part("state") state: RequestBody,
        @Part images: List<MultipartBody.Part>,
        @Part("keep_image_ids[]") keepImageIds: List<@JvmSuppressWildcards RequestBody>,
        @Part("location") location: RequestBody? = null
    ): Response<ResponseReply>



    @FormUrlEncoded
    @POST("api/app/serve/used-product-listings/bookmark-used-product-listing")
    suspend fun bookmarkUsedProductListing(
        @Field("user_id") userId: Long,
        @Field("product_id") serviceId: Long,
    ): Response<ResponseReply>


    @FormUrlEncoded
    @POST("api/app/serve/used-product-listings/remove-bookmark-used-product-listing")
    suspend fun removeBookmarkUsedProductListing(
        @Field("user_id") userId: Long,
        @Field("product_id") serviceId: Long,
    ): Response<ResponseReply>


    @GET("api/app/serve/used-product-listings/search-used-product-listing-suggestions/{user_id}")
    suspend fun searchFilterUsedProductListing(
        @Path("user_id") userId: Long,
        @Query("query") query: String = "",
    ): Response<ResponseReply>


    @GET("api/app/serve/used-product-listings/guest-used-product-listing-search-suggestions/{user_id}")
    suspend fun guestSearchFilterUsedProductListing(
        @Path("user_id") userId: Long,
        @Query("query") query: String = "",
    ): Response<ResponseReply>


    @DELETE("api/app/serve/used-product-listings/{product_id}/delete-used-product-listing")
    suspend fun deleteUsedProductListing(
        @Path("product_id") serviceId: Long,
        @Query("user_id") userId: Long,
    ): Response<ResponseReply>

}

