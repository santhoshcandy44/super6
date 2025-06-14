package com.lts360.api.app

import com.lts360.api.common.responses.ResponseReply
import com.lts360.test.JobFilters
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApplicantProfileApiService {

    @GET("api/app/serve/jobs/applicant-profile/{user_id}")
    suspend fun getApplicantProfile(
        @Path("user_id") userId: Long
    ): Response<ResponseReply>

    @Multipart
    @POST("api/app/serve/jobs/update-applicant-profile")
    suspend fun updateProfessionalInfo(
        @Part("applicantProfessionalInfo") jobProfessionalInfo: RequestBody, // Send JobProfessionalInfo as JSON string
        @Part profilePic: MultipartBody.Part? = null  // Profile pic as a multipart file (optional)
    ): Response<ResponseReply>

    @POST("api/app/serve/jobs/update-applicant-education")
    suspend fun updateEducationInfo(
        @Body applicantEducationInfo: RequestBody, // Send JobProfessionalInfo as JSON string
    ): Response<ResponseReply>

    @POST("api/app/serve/jobs/update-applicant-experience")
    suspend fun updateExperienceInfo(
        @Body applicantExperienceInfo: RequestBody, // Send JobProfessionalInfo as JSON string
    ): Response<ResponseReply>


    @POST("api/app/serve/jobs/update-applicant-no-experience")
    suspend fun updateNoExperienceInfo(): Response<ResponseReply>

    @POST("api/app/serve/jobs/update-applicant-skill")
    suspend fun updateSkillInfo(
        @Body applicantExperienceInfo: RequestBody, // Send JobProfessionalInfo as JSON string
    ): Response<ResponseReply>


    @POST("api/app/serve/jobs/update-applicant-language")
    suspend fun updateLanguageInfo(
        @Body applicantLanguageInfo: RequestBody, // Send JobProfessionalInfo as JSON string
    ): Response<ResponseReply>


    @Multipart
    @POST("api/app/serve/jobs/update-applicant-resume")
    suspend fun updateResumeInfo(
        @Part("applicantResumeInfo") applicantResumeInfo: RequestBody, // Send JobProfessionalInfo as JSON string
        @Part resume: MultipartBody.Part
    ): Response<ResponseReply>

    @Multipart
    @POST("api/app/serve/jobs/update-applicant-certificate")
    suspend fun updateCertificateInfo(
        @Part("applicantCertificateInfo") applicantResumeInfo: RequestBody, // Send JobProfessionalInfo as JSON string
        @Part certificates: List<MultipartBody.Part>
    ): Response<ResponseReply>

}


interface JobPostingsApiService {

    @GET("api/app/serve/jobs/get-job-listings")
    suspend fun getJobListings(
        @Query("user_id") userId: Long,
        @Query("page") page: Int,
        @Query("s") query: String?,
        @Query("last_timestamp") lastTimestamp: String?,
        @Query("last_total_relevance") lastTotalRelevance: String?,
        @Query("work_modes") workModes: String?=null,
        @Query("salary_min") salaryMin: Int?=-1,
        @Query("salary_max") salaryMax: Int?=-1
    ): Response<ResponseReply>

}

