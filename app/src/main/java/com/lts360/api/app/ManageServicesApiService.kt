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
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query


interface ManageServicesApiService{


    @GET("api/app/serve/services/guest-get-services")
    suspend fun guestGetServices(
        @Query("user_id") userId: Long,
        @Query("page") page: Int,
        @Query("s") query: String?,
        @Query("industries") industries: List<Int>?,// Add industries as a query parameter
        @Query("last_timestamp") lastTimestamp: String?,
        @Query("last_total_relevance") lastTotalRelevance: String?,
        @Query("latitude") latitude: Double?,
        @Query("longitude") longitude: Double?,
    ): Response<ResponseReply>


    @GET("api/app/serve/services/get-services")
    suspend fun getServices(
        @Query("user_id") userId: Long,
        @Query("page") page: Int,
        @Query("s") query: String?,
        @Query("last_timestamp") lastTimestamp: String?,
        @Query("last_total_relevance") lastTotalRelevance: String?

    ): Response<ResponseReply>

    @GET("api/app/serve/services/user-bookmark-services/{user_id}")
    suspend fun getBookmarkedServices(
        @Path("user_id") userId: Long,
    ): Response<ResponseReply>

    @GET("api/app/serve/services/get-published-services/{user_id}")
    suspend fun getServicesByUserId(
        @Path("user_id") userId: Long,
    ): Response<ResponseReply>

    @GET("api/app/serve/services/get-published-services-feed-user/{user_id}")
    suspend fun getServicesByFeedUserId(
        @Path("user_id") userId: Long,
    ): Response<ResponseReply>

    @GET("api/app/serve/services/get-published-services-feed-guest/{user_id}")
    suspend fun getServicesByFeedGuestUserId(
        @Path("user_id") userId: Long,
    ): Response<ResponseReply>


    @FormUrlEncoded
    @PATCH("api/app/serve/services/{service_id}/update-service-info")
    suspend fun updateServiceInfo(
        @Field("user_id") userId: Long,
        @Path("service_id") serviceId: Long,
        @Field("title") title: String,
        @Field("short_description") shortDescription: String,
        @Field("long_description") longDescription: String,
        @Field("industry") industry: Int,

        ): Response<ResponseReply>


    @FormUrlEncoded
    @PATCH("api/app/serve/services/{service_id}/update-service-location")
    suspend fun updateServiceLocation(
        @Field("user_id") userId: Long,
        @Path("service_id") serviceId: Long,
        @Field("latitude") latitude: Double,
        @Field("longitude") longitude: Double,
        @Field("geo") geo: String,
        @Field("location_type") locationType: String,
    ): Response<ResponseReply>



    @FormUrlEncoded
    @PATCH("api/app/serve/services/{service_id}/update-service-plans")
    suspend fun updatePlans(
        @Path("service_id") serviceId: Long,
        @Field("user_id") userId: Long,
        @Field("plans") plans: String,
    ): Response<ResponseReply>

    @DELETE("api/app/serve/services/{service_id}/delete-service-image")
    suspend fun deleteServiceImage(
        @Path("service_id") serviceId: Long,
        @Query("user_id") userId: Long,
        @Query("image_id") imageId: Int,
    ): Response<ResponseReply>


    @Multipart
    @POST("api/app/serve/services/{service_id}/upload-service-image")
    suspend fun uploadImage(
        @Path("service_id") serviceId: Long,
        @Part("user_id") userId: RequestBody,
        @Part("image_id") imageId: RequestBody,
        @Part image: MultipartBody.Part,
    ): Response<ResponseReply>


    @Multipart
    @POST("api/app/serve/services/{service_id}/update-service-image")
    suspend fun updateImage(
        @Path("service_id") serviceId: Long,
        @Part("user_id") userId: RequestBody,
        @Part("image_id") imageId: RequestBody,
        @Part image: MultipartBody.Part,
    ): Response<ResponseReply>


    @Multipart
    @POST("api/app/serve/services/create-service")
    suspend fun createService(
        @Part("title") title: RequestBody,
        @Part("long_description") longDescription: RequestBody,
        @Part("short_description") shortDescription: RequestBody,
        @Part("industry") industry: RequestBody,
        @Part("country") country: RequestBody,
        @Part("state") state: RequestBody,
        @Part thumbnail: MultipartBody.Part,
        @Part images: List<MultipartBody.Part>,
        @Part("plans") plans: RequestBody,
        @Part("location") location: RequestBody? = null): Response<ResponseReply>


    @Multipart
    @POST("api/app/serve/services/{service_id}/update-service-thumbnail")
    suspend fun updateThumbnail(
        @Path("service_id") serviceId: Long,
        @Part("user_id") userId: RequestBody,
        @Part("image_id") imageId: RequestBody,
        @Part thumbnail: MultipartBody.Part
    ): Response<ResponseReply>


    @FormUrlEncoded
    @POST("api/app/serve/services/bookmark-service")
    suspend fun bookmarkService(
        @Field("user_id") userId: Long,
        @Field("service_id") serviceId: Long,
    ): Response<ResponseReply>


    @FormUrlEncoded
    @POST("api/app/serve/services/remove-bookmark-service")
    suspend fun removeBookmarkService(
        @Field("user_id") userId: Long,
        @Field("service_id") serviceId: Long,
    ): Response<ResponseReply>


    @GET("api/app/serve/services/search-suggestions/{user_id}")
    suspend fun searchFilter(
        @Path("user_id") userId: Long,
        @Query("query") query: String = "",
    ): Response<ResponseReply>


    @GET("api/app/serve/services/guest-search-suggestions/{user_id}")
    suspend fun guestSearchFilter(
        @Path("user_id") userId: Long,
        @Query("query") query: String = "",
    ): Response<ResponseReply>


    @DELETE("api/app/serve/services/{service_id}/delete-service")
    suspend fun deleteService(
        @Path("service_id") serviceId: Long,
        @Query("user_id") userId: Long,
    ): Response<ResponseReply>

}