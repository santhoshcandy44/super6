package com.lts360.compose.ui.localjobs.manage

import com.google.gson.Gson
import com.lts360.api.utils.Result
import com.lts360.api.app.AppClient
import com.lts360.api.app.ManageLocalJobService
import com.lts360.api.common.errors.ErrorResponse
import com.lts360.api.common.responses.ResponseReply
import javax.inject.Inject


enum class MaritalStatus(val key: String, val value: String) {
    MARRIED("MARRIED", "Married"), UNMARRIED("UNMARRIED", "Un Married")
}

class PublishedLocalJobsRepository @Inject constructor() {

    suspend fun getLocalJobsByUserId(userId: Long): Result<ResponseReply> {
        return try {
            val response = AppClient.instance.create(ManageLocalJobService::class.java)
                .getLocalJobsByUserId(userId)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.isSuccessful) {
                    Result.Success(body)
                } else {
                    val errorMessage = "Failed, try again later..."
                    Result.Error(Exception(errorMessage))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = try {
                    Gson().fromJson(errorBody, ErrorResponse::class.java).message
                } catch (_: Exception) {
                    "An unknown error occurred"
                }
                Result.Error(Exception(errorMessage))
            }
        } catch (t: Throwable) {
            t.printStackTrace()
            Result.Error(t)
        }
    }

}
