package com.lts360.api.app

import com.lts360.api.common.responses.ResponseReply
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApplicantProfileApiService {

    @Multipart
    @POST("api/app/serve/jobs/applicant-profile")
    suspend fun updateJobProfessionalInfo(
        @Part("jobProfessionalInfo") jobProfessionalInfo: RequestBody, // Send JobProfessionalInfo as JSON string
        @Part profilePic: MultipartBody.Part? = null  // Profile pic as a multipart file (optional)
    ): Response<ResponseReply>
}
