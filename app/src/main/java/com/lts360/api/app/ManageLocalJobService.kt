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


interface ManageLocalJobService {

    @GET("api/app/serve/local-jobs/guest-get-local-jobs")
    suspend fun guestGetLocalJobs(
        @Query("user_id") userId: Long,
        @Query("page") page: Int,
        @Query("s") query: String?,
        @Query("last_timestamp") lastTimestamp: String?,
        @Query("last_total_relevance") lastTotalRelevance: String?,
        @Query("latitude") latitude: Double?,
        @Query("longitude") longitude: Double?,
    ): Response<ResponseReply>


    @GET("api/app/serve/local-jobs/get-local-jobs")
    suspend fun getLocalJobs(
        @Query("user_id") userId: Long,
        @Query("s") query: String?,
        @Query("page") page: Int,
        @Query("last_timestamp") lastTimestamp: String?,
        @Query("last_total_relevance") lastTotalRelevance: String?
    ): Response<ResponseReply>

    @GET("api/app/serve/local-jobs/get-published-local-jobs/{user_id}")
    suspend fun getLocalJobsByUserId(
        @Path("user_id") userId: Long,
    ): Response<ResponseReply>

    @GET("api/app/serve/local-jobs/get-local-job-applicants/{local_job_id}")
    suspend fun getLocalJobApplicantsByLocalJobId(
        @Path("local_job_id") localJobId: Long,
        @Query("page") page: Int,
        @Query("last_timestamp") lastTimestamp: String?
    ): Response<ResponseReply>

    @Multipart
    @POST("api/app/serve/local-jobs/create-or-update-local-job")
    suspend fun createOrUpdateLocalJob(
        @Part("local_job_id") localJobId: RequestBody,
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody,
        @Part("company") company: RequestBody,
        @Part("age_min") ageMin: RequestBody,
        @Part("age_max") ageMax: RequestBody,
        @Part("marital_statuses[]") maritalStatus: List<@JvmSuppressWildcards RequestBody>,
        @Part("salary_unit") salaryUnit: RequestBody,
        @Part("salary_min") salaryMin: RequestBody,
        @Part("salary_max") salaryMax: RequestBody,
        @Part images: List<MultipartBody.Part>,
        @Part("keep_image_ids[]") keepImageIds: List<@JvmSuppressWildcards RequestBody>,
        @Part("country") country: RequestBody,
        @Part("state") state: RequestBody,
        @Part("location") location: RequestBody? = null
    ): Response<ResponseReply>

    @FormUrlEncoded
    @POST("api/app/serve/local-jobs/bookmark-local-job")
    suspend fun bookmarkLocalJob(
        @Field("user_id") userId: Long,
        @Field("local_job_id") localJobId: Long,
    ): Response<ResponseReply>

    @FormUrlEncoded
    @POST("api/app/serve/local-jobs/mark-as-reviewed-local-job")
    suspend fun markAsReviewedLocalJob(
        @Field("user_id") userId: Long,
        @Field("local_job_id") localJobId: Long,
        @Field("applicant_id") applicantId: Long,
        ): Response<ResponseReply>

    @FormUrlEncoded
    @POST("api/app/serve/local-jobs/unmark-reviewed-local-job")
    suspend fun unmarkAsReviewedLocalJob(
        @Field("user_id") userId: Long,
        @Field("local_job_id") localJobId: Long,
        @Field("applicant_id") applicantId: Long,
    ): Response<ResponseReply>

    @FormUrlEncoded
    @POST("api/app/serve/local-jobs/apply-local-job")
    suspend fun applyLocalJob(
        @Field("user_id") userId: Long,
        @Field("local_job_id") localJobId: Long,
    ): Response<ResponseReply>


    @FormUrlEncoded
    @POST("api/app/serve/local-jobs/remove-bookmark-local-job")
    suspend fun removeBookmarkLocalJob(
        @Field("user_id") userId: Long,
        @Field("local_job_id") localJobId: Long,
    ): Response<ResponseReply>


    @GET("api/app/serve/local-jobs/search-local-job-suggestions/{user_id}")
    suspend fun searchFilterLocalJob(
        @Path("user_id") userId: Long,
        @Query("query") query: String = "",
    ): Response<ResponseReply>


    @GET("api/app/serve/local-jobs/guest-search-local-job-suggestions/{user_id}")
    suspend fun guestSearchFilterLocalJob(
        @Path("user_id") userId: Long,
        @Query("query") query: String = "",
    ): Response<ResponseReply>


    @DELETE("api/app/serve/local-jobs/{product_id}/delete-local-job")
    suspend fun deleteLocalJob(
        @Path("local_job_id") serviceId: Long,
        @Query("user_id") userId: Long,
    ): Response<ResponseReply>

}

