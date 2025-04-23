package com.lts360.compose.ui.localjobs.manage

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lts360.api.utils.Result
import com.lts360.api.app.AppClient
import com.lts360.api.app.ManageLocalJobService
import com.lts360.api.common.errors.ErrorResponse
import com.lts360.api.common.responses.ResponseReply
import com.lts360.compose.ui.localjobs.models.EditableLocalJob
import com.lts360.compose.ui.localjobs.models.LocalJob
import com.lts360.compose.ui.localjobs.models.toEditableLocalJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject


class LocalJobsRepository @Inject constructor() {

    private val _publishedLocalJobs = MutableStateFlow<List<EditableLocalJob>>(emptyList())
    val publishedLocalJobs = _publishedLocalJobs.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _selectedLocalJob = MutableStateFlow<EditableLocalJob?>(null)
    val selectedLocalJob = _selectedLocalJob.asStateFlow()



    fun updateLocalJobId(item: EditableLocalJob) {
        _publishedLocalJobs.update { list ->
            list.map {
                if (it.localJobId == item.localJobId) {
                    item
                } else {
                    it
                }
            }
        }

        _selectedLocalJob.value?.let {
            if(it.localJobId == item.localJobId){
                _selectedLocalJob.update{
                    item
                }
            }
        }
    }


    private fun updatePublishedUsedProductListings(items: List<EditableLocalJob>) {
        _publishedLocalJobs.value = items
    }

    fun setSelectedItem(localJobId: Long) {
        val index = _publishedLocalJobs.value.indexOfFirst {
            it.localJobId == localJobId
        }
        if (index != -1) {
            _selectedLocalJob.value = _publishedLocalJobs.value[index]
        }
    }

    fun removeSelectedLocalJob(localJobId: Long) {
        val index = _publishedLocalJobs.value.indexOfFirst {
            it.localJobId == localJobId
        }

        if (index != -1) {
            _publishedLocalJobs.value = _publishedLocalJobs.value.filter {
                it.localJobId != localJobId
            }
        }

        invalidateSelectedItem()
    }

    fun invalidateSelectedItem() {
        _selectedLocalJob.value = null
    }

    suspend fun onGetPublishedLocalJobs(
        userId: Long,
        onSuccess: (items: List<EditableLocalJob>) -> Unit,
        onError: (Throwable) -> Unit,
    ) {

        try {
            when (val result = getLocalJobsByUserId(userId)) {
                is Result.Success -> {
                    val data = Gson().fromJson(result.data.data, object : TypeToken<List<LocalJob>>() {}.type)
                            as List<LocalJob>
                    val listings = data.map { it.toEditableLocalJob() }

                    updatePublishedUsedProductListings(listings)
                    onSuccess(listings)
                }

                is Result.Error -> {
                    if (_publishedLocalJobs.value.isNotEmpty()) {
                        _publishedLocalJobs.value = emptyList()
                    }
                    onError(result.error)
                }
            }
        } catch (t: Throwable) {
            if (_publishedLocalJobs.value.isNotEmpty()) {
                _publishedLocalJobs.value = emptyList()
            }
            t.printStackTrace()
            onError(Exception("Something Went Wrong"))
        }
    }


    private suspend fun getLocalJobsByUserId(userId: Long): Result<ResponseReply> {
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
