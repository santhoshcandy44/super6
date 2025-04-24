package com.lts360.compose.ui.localjobs.manage.viewmodels

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.lts360.api.utils.Result
import com.lts360.api.utils.ResultError
import com.lts360.api.utils.mapExceptionToError
import com.lts360.api.app.AppClient
import com.lts360.api.app.ManageLocalJobService
import com.lts360.api.common.errors.ErrorResponse
import com.lts360.api.common.responses.ResponseReply
import com.lts360.api.models.service.EditableLocation
import com.lts360.compose.ui.chat.MAX_IMAGES
import com.lts360.compose.ui.localjobs.manage.LocalJobsRepository
import com.lts360.compose.ui.localjobs.manage.MaritalStatus
import com.lts360.compose.ui.localjobs.models.EditableLocalJob
import com.lts360.compose.ui.managers.UserSharedPreferencesManager
import com.lts360.compose.ui.services.manage.models.CombinedContainer
import com.lts360.compose.ui.services.manage.models.CombinedContainerFactory
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.util.Currency
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class PublishedLocalJobViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    val savedStateHandle: SavedStateHandle,
    private val repository: LocalJobsRepository
) : ViewModel() {

    val userId: Long = UserSharedPreferencesManager.userId


    data class LocalJobState(
        val localJobId: Long =-1,
        val title: String = "",
        val description: String = "",
        val company: String = "",
        val ageMin: Int = 18,
        val ageMax: Int = 40,
        val maritalStatus: MaritalStatus = MaritalStatus.UNMARRIED,
        val imageContainers: List<CombinedContainer> = emptyList(),
        val salaryUnit: String = "",
        val salaryMin: Int = -1,
        val salaryMax: Int = -1,
        val country: String = "",
        val state: String = "",
        val selectedLocation: EditableLocation? = null
    )


    data class LocalJobErrorsState(
        val title: String? = null,
        val description: String? = null,
        val company: String? = null,
        val age: String? = null,
        val maritalStatus: String? = null,
        val imageContainers: String? = null,
        val salaryUnit: String? = null,
        val salaryMin: String? = null,
        val salaryMax: String? = null,
        val country: String? = null,
        val state: String? = null,
        val selectedLocation: String? = null
    )


    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()



    val maritalStatusUnits = listOf(MaritalStatus.MARRIED, MaritalStatus.UNMARRIED)

    val salaryUnits = listOf("INR", "USD")
    private val userCurrency = Currency.getInstance(Locale.getDefault()).currencyCode

    private val _localJob = MutableStateFlow<LocalJobState>(
        LocalJobState(
            maritalStatus = maritalStatusUnits[1],
            salaryUnit = if (userCurrency in salaryUnits) userCurrency else "INR"
        )
    )

    val localJob = _localJob.asStateFlow()


    private val _errors = MutableStateFlow<LocalJobErrorsState>(LocalJobErrorsState())
    val errors = _errors.asStateFlow()

    private val _isPickerLaunch = MutableStateFlow(false)
    val isPickerLaunch = _isPickerLaunch.asStateFlow()

    private val _refreshImageIndex = MutableStateFlow(-1)
    val refreshImageIndex = _refreshImageIndex.asStateFlow()


    val slots: StateFlow<Int> = _localJob
        .map { localJob ->
            MAX_IMAGES - localJob.imageContainers.size
        }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            initialValue = MAX_IMAGES
        )


    private var onCreateOrUpdateJob: Job? = null


    private var errorMessage: String = ""

    val publishedLocalJobs = repository.publishedLocalJobs
    val selectedLocalJob = repository.selectedLocalJob

    private val _resultError = MutableStateFlow<ResultError?>(null)
    val resultError = _resultError.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _isUpdating = MutableStateFlow(false)
    val isUpdating = _isUpdating.asStateFlow()

    private val _isDeleting = MutableStateFlow(false)
    val isDeleting = _isDeleting.asStateFlow()


    private val containerFactory = CombinedContainerFactory()

    init {
        _isLoading.value = true
        viewModelScope.launch {
            repository.onGetPublishedLocalJobs(userId, {
                _isLoading.value = false
            }, {
                _isLoading.value = false
                val mappedException = mapExceptionToError(it)
                _resultError.value = mappedException
                errorMessage = mappedException.errorMessage
            })
        }
    }

    fun setPickerLaunch(isLaunched: Boolean) {
        _isPickerLaunch.value = isLaunched
    }

    fun updateRefreshImageIndex(index: Int) {
        _refreshImageIndex.value = index
    }

    fun refreshPublishedItems(userId: Long) {
        _resultError.value = null
        _isRefreshing.value = true
        viewModelScope.launch {
            repository.onGetPublishedLocalJobs(userId, {
                _isRefreshing.value = false
            }, {
                _isRefreshing.value = false
                val mappedException = mapExceptionToError(it)
                _resultError.value = mappedException
                errorMessage = mapExceptionToError(it).errorMessage
            })
        }
    }

    fun isSelectedLocalJobNull(): Boolean {
        return selectedLocalJob.value == null
    }

    fun setSelectedItem(itemId: Long) {
        repository.setSelectedItem(itemId)
        selectedLocalJob.value?.let {
            loadItemDetails(it)
        }
    }

    fun removeSelectedItem(itemId: Long) {
        repository.removeSelectedItem(itemId)
    }


    private fun loadItemDetails(item: EditableLocalJob) {

        _errors.value = LocalJobErrorsState()
        _localJob.value = _localJob.value.copy(localJobId = item.localJobId)
        updateTitle(item.title)
        updateDescription(item.description)
        updateCompany(item.company)
        updateAgeMin(item.ageMin)
        updateAgeMax(item.ageMax)
        updateMaritalStatus(maritalStatusUnits.find {
            it.key == item.maritalStatus
        }?: MaritalStatus.UNMARRIED)
        updateSalaryUnit(item.salaryUnit)
        updateSalaryMin(item.salaryMin)
        updateSalaryMax(item.salaryMax)

        updateCountry(item.country ?: "")
        updateState(item.state?: "")
        updateLocation(item.location)

        loadImageContainers(item.images.map {
            containerFactory.createCombinedContainerForEditableImage(it)
        })
    }

    fun onDeleteItem(
        userId: Long,
        itemId: Long,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
    ) {

        viewModelScope.launch {
            _isDeleting.value = true
            try {
                when (val result = deleteLocalJob(userId, itemId)) {
                    is Result.Success -> {
                        onSuccess(result.data.message)
                    }

                    is Result.Error -> {
                        errorMessage = mapExceptionToError(result.error).errorMessage
                        onError(errorMessage)
                    }
                }
            } catch (t: Throwable) {
                t.printStackTrace()
                errorMessage = "Something went wrong"
                onError(errorMessage)
            } finally {
                _isDeleting.value = false // Reset loading state
            }

        }

    }


    private suspend fun deleteLocalJob(
        userId: Long,
        localJobId: Long,
    ): Result<ResponseReply> {


        return try {
            val response = AppClient.instance.create(ManageLocalJobService::class.java)
                .deleteLocalJob(localJobId, userId)

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
            Result.Error(t)
        }
    }


    fun updateTitle(newTitle: String) {
        _localJob.value = _localJob.value.copy(title = newTitle)
        if (newTitle.isNotBlank()) {
            _errors.value = _errors.value.copy(title = null)
        }
    }

    fun updateDescription(newDescription: String) {
        _localJob.value = _localJob.value.copy(description = newDescription)
        if (newDescription.isNotBlank()) {
            _errors.value = _errors.value.copy(description = null)
        }
    }

    fun updateCompany(company: String) {
        _localJob.value = _localJob.value.copy(company = company)
        if (company.isNotBlank()) {
            _errors.value = _errors.value.copy(company = null)
        }
    }

    fun updateAgeMin(ageMin: Int) {
        _localJob.value = _localJob.value.copy(ageMin = ageMin)
        _errors.value = _errors.value.copy(age = null)
    }

    fun updateAgeMax(ageMax: Int) {
        _localJob.value = _localJob.value.copy(ageMax = ageMax)
        _errors.value = _errors.value.copy(age = null)
    }

    fun updateMaritalStatus(maritalStatus: MaritalStatus) {
        _localJob.value = _localJob.value.copy(
            maritalStatus = maritalStatus)
        _errors.value = _errors.value.copy(maritalStatus = null)
    }

    fun updateSalaryUnit(salaryUnit: String) {
        _localJob.value = _localJob.value.copy(salaryUnit = salaryUnit)
        _errors.value = _errors.value.copy(salaryUnit = null)
    }

    fun updateSalaryMin(salaryMin: Int) {
        _localJob.value = _localJob.value.copy(salaryMin = salaryMin)
        _errors.value = _errors.value.copy(salaryMin = null)
    }

    fun updateSalaryMax(salaryMax: Int) {
        _localJob.value = _localJob.value.copy(salaryMax = salaryMax)
        _errors.value = _errors.value.copy(salaryMax = null)
    }

    fun updateCountry(newCountry: String) {
        _localJob.value = _localJob.value.copy(country = newCountry)
        if (newCountry.isBlank()) {
            _errors.value = _errors.value.copy(country = null)
        }
    }

    fun updateState(newState: String) {
        _localJob.value = _localJob.value.copy(state = newState)
        if (newState.isBlank()) {
            _errors.value = _errors.value.copy(state = null)
        }
    }

    fun updateLocation(newLocation: EditableLocation?) {
        _localJob.value = _localJob.value.copy(selectedLocation = newLocation)
        _errors.value = _errors.value.copy(selectedLocation = null)
    }


    private fun loadImageContainers(bitmapContainers: List<CombinedContainer>) {
        _localJob.value = _localJob.value.copy(
            imageContainers = bitmapContainers
        )
    }


    fun addContainer(
        path: String,
        width: Int,
        height: Int,
        format: String,
        errorMessage: String? = null
    ) {
        _localJob.value = _localJob.value.copy(
            imageContainers = _localJob.value.imageContainers.toMutableList().apply {
                add(
                    containerFactory.createCombinedContainerForBitmap(
                        path,
                        width,
                        height,
                        format,
                        errorMessage
                    )
                )
            }
        )
        _errors.value = _errors.value.copy(imageContainers = null)

    }


    fun updateContainer(
        index: Int,
        path: String,
        width: Int,
        height: Int,
        format: String,
        errorMessage: String? = null,
    ) {

        val currentContainers = _localJob.value.imageContainers.toMutableList()
        if (index in currentContainers.indices) {
            currentContainers[index] = containerFactory.createCombinedContainerForBitmap(
                path,
                width,
                height,
                format,
                errorMessage
            )

            _localJob.value = _localJob.value.copy(
                imageContainers = currentContainers
            )
        }
    }

    fun removeContainer(index: Int) {
        val updatedContainers = _localJob.value.imageContainers.toMutableList()
        if (index in updatedContainers.indices) {
            updatedContainers.removeAt(index)
            _localJob.value = _localJob.value.copy(
                imageContainers = updatedContainers
            )
        }
    }

    fun validateAll(): Boolean {
        var isValid = true

        if (_localJob.value.title.isBlank()) {
            _errors.value = _errors.value.copy(title = "Service title cannot be empty")
            isValid = false
        } else if (_localJob.value.title.length > 100) {
            _errors.value =
                _errors.value.copy(title = "Service title cannot exceed 100 characters")
            isValid = false
        } else {
            _errors.value = _errors.value.copy(title = null)
        }

        if (_localJob.value.description.isBlank()) {
            _errors.value = _errors.value.copy(description = "Short description cannot be empty")
            isValid = false
        } else if (_localJob.value.description.length > 250) {
            _errors.value =
                _errors.value.copy(description = "Service short description cannot exceed 250 characters")
            isValid = false
        } else {
            _errors.value = _errors.value.copy(description = null)
        }

        if (_localJob.value.company.isBlank()) {
            _errors.value = _errors.value.copy(company = "Company cannot be empty")
            isValid = false
        } else if (_localJob.value.company.length > 250) {
            _errors.value =
                _errors.value.copy(company = "Company cannot exceed 250 characters")
            isValid = false
        } else {
            _errors.value = _errors.value.copy(company = null)
        }


        if (_localJob.value.maritalStatus !in maritalStatusUnits) {
            _errors.value = _errors.value.copy(maritalStatus = "Marital Status must be selected")
            isValid = false
        } else {
            _errors.value = _errors.value.copy(maritalStatus = null)
        }

        if (_localJob.value.salaryUnit.isEmpty()) {
            _errors.value = _errors.value.copy(salaryUnit = "Salary unit must be selected")
            isValid = false
        } else if (!salaryUnits.contains(_localJob.value.salaryUnit)) {
            _errors.value = _errors.value.copy(salaryUnit = "Invalid unit selected")
            isValid = false
        } else {
            _errors.value = _errors.value.copy(salaryUnit = null)
        }

        if (_localJob.value.country.isBlank()) {
            _errors.value = _errors.value.copy(country = "Country must be selected")
            isValid = false
        } else {
            _errors.value = _errors.value.copy(country = null)
        }

        if (_localJob.value.state.isBlank()) {
            _errors.value = _errors.value.copy(state = "State must be selected")
            isValid = false
        } else {
            _errors.value = _errors.value.copy(state = null)
        }

        if (_localJob.value.imageContainers.isEmpty()) {
            _errors.value =
                _errors.value.copy(imageContainers = "At least one image must be added")
            isValid = false
        } else {
            _errors.value = _errors.value.copy(imageContainers = null)
        }

        if (_localJob.value.selectedLocation == null) {
            _errors.value = _errors.value.copy(selectedLocation = "Location must be selected")
            isValid = false
        } else {
            _errors.value = _errors.value.copy(selectedLocation = null)
        }

        return isValid
    }


    fun onUpdateLocalJob(
        localJobId: RequestBody,
        title: RequestBody,
        description: RequestBody,
        company: RequestBody,
        ageMin: RequestBody,
        ageMax: RequestBody,
        maritalStatus: RequestBody,
        salaryUnit: RequestBody,
        salaryMin: RequestBody,
        salaryMax: RequestBody,
        images: List<MultipartBody.Part>,
        keepImageIds: RequestBody,
        country: RequestBody,
        state: RequestBody,
        location: RequestBody? = null,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        onCreateOrUpdateJob = viewModelScope.launch {
            _isUpdating.value = true

            when (val result = createOrUpdateUsedProductListing(
                localJobId,
                title,
                description,
                company,
                ageMin,
                ageMax,
                maritalStatus,
                salaryUnit,
                salaryMin,
                salaryMax,
                images,
                keepImageIds,
                country,
                state,
                location,
            )) {
                is Result.Success -> {
                    onSuccess(result.data.message)

                    repository.updateLocalJobId(
                        Gson().fromJson(
                            result.data.data,
                            EditableLocalJob::class.java
                        )
                    )
                }

                is Result.Error -> {
                    val errorMsg = mapExceptionToError(result.error).errorMessage
                    onError(errorMsg)
                }
            }

            _isUpdating.value = false
        }
    }

    private suspend fun createOrUpdateUsedProductListing(
        localJobId: RequestBody,
        title: RequestBody,
        description: RequestBody,
        company: RequestBody,
        ageMin: RequestBody,
        ageMax: RequestBody,
        maritalStatus: RequestBody,
        salaryUnit: RequestBody,
        salaryMin: RequestBody,
        salaryMax: RequestBody,
        images: List<MultipartBody.Part>,
        keepImageIds: RequestBody,
        country: RequestBody,
        state: RequestBody,
        location: RequestBody? = null
    ): Result<ResponseReply> {

        return try {
            val response = AppClient.instance.create(ManageLocalJobService::class.java)
                .createOrUpdateLocalJob(
                    localJobId,
                    title,
                    description,
                    company,
                    ageMin,
                    ageMax,
                    maritalStatus,
                    salaryUnit,
                    salaryMin,
                    salaryMax,
                    images,
                    keepImageIds,
                    country,
                    state,
                    location
                )

            if (response.isSuccessful) {
                // Handle successful response
                val loginResponse = response.body()

                if (loginResponse != null && loginResponse.isSuccessful) {

                    Result.Success(loginResponse)

                } else {
                    val errorMessage = "Failed, try again later..."
                    Result.Error(Exception(errorMessage))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = try {
                    Gson().fromJson(errorBody, ErrorResponse::class.java).message
                } catch (e: Exception) {
                    e.printStackTrace()
                    "An unknown error occurred"
                }

                Result.Error(Exception(errorMessage))


            }
        } catch (t: Throwable) {

            Result.Error(t)

        }
    }


}
