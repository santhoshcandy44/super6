package com.lts360.compose.ui.localjobs.manage.viewmodels


import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.lts360.api.utils.Result
import com.lts360.api.utils.mapExceptionToError
import com.lts360.api.app.AppClient
import com.lts360.api.app.ManageLocalJobService
import com.lts360.api.common.errors.ErrorResponse
import com.lts360.api.common.responses.ResponseReply
import com.lts360.api.models.service.EditableLocation
import com.lts360.compose.ui.chat.MAX_IMAGES
import com.lts360.compose.ui.localjobs.manage.MaritalStatus
import com.lts360.compose.ui.services.BitmapContainer
import com.lts360.compose.ui.services.BitmapContainerFactory
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
class LocalJobWorkFlowViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    data class LocalJobState(
        val title: String = "",
        val description: String = "",
        val company: String = "",
        val ageMin: Int = 18,
        val ageMax: Int = 40,
        val maritalStatuses: List<SelectableMaritalStatus> = emptyList(),
        val imageContainers: List<BitmapContainer> = emptyList(),
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
        val maritalStatuses: String? = null,
        val imageContainers: String? = null,
        val salaryUnit: String? = null,
        val salaryMin: String? = null,
        val salaryMax: String? = null,
        val country: String? = null,
        val state: String? = null,
        val selectedLocation: String? = null
    )

    data class SelectableMaritalStatus(
        val status: MaritalStatus,
        var isSelected: Boolean = false
    )


    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val bitmapContainerFactory = BitmapContainerFactory()

    val maritalStatusUnits = listOf(MaritalStatus.MARRIED, MaritalStatus.UNMARRIED)

    val salaryUnits = listOf("INR", "USD")
    private val userCurrency = Currency.getInstance(Locale.getDefault()).currencyCode

    private val _localJob = MutableStateFlow<LocalJobState>(
        LocalJobState(
            maritalStatuses =
                listOf(
                    SelectableMaritalStatus(
                        MaritalStatus.MARRIED
                    ),
                    SelectableMaritalStatus(
                        MaritalStatus.UNMARRIED
                    )
                ),
            salaryUnit = if (userCurrency in salaryUnits) userCurrency else "INR"
        )
    )
    val localJob = _localJob.asStateFlow()


    private val _errors = MutableStateFlow<LocalJobErrorsState>(LocalJobErrorsState())
    val errors = _errors.asStateFlow()

    private val _isPublishing = MutableStateFlow(false)
    val isPublishing = _isPublishing.asStateFlow()

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

    fun toggleMaritalStatus(index: Int) {
        val updatedStatuses = _localJob.value.maritalStatuses.toMutableList()
        val currentStatus = updatedStatuses[index]
        updatedStatuses[index] = currentStatus.copy(isSelected = !currentStatus.isSelected)
        _localJob.value = _localJob.value.copy(maritalStatuses = updatedStatuses)
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

    fun updateLocation(newLocation: EditableLocation) {
        _localJob.value = _localJob.value.copy(selectedLocation = newLocation)
        _errors.value = _errors.value.copy(selectedLocation = null)
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
                add(createBitmapContainer(path, width, height, format, errorMessage))
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
            currentContainers[index] = currentContainers[index].copy(
                path = path,
                width = width,
                height = height,
                format = format,
                errorMessage = errorMessage
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

    private fun createBitmapContainer(
        path: String,
        width: Int,
        height: Int,
        format: String,
        errorMessage: String? = null,
    ): BitmapContainer {
        return bitmapContainerFactory.createContainer(path, width, height, format, errorMessage)
    }


    fun setPickerLaunch(isLaunched: Boolean) {
        _isPickerLaunch.value = isLaunched
    }

    fun updateRefreshImageIndex(index: Int) {
        _refreshImageIndex.value = index
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


        if (_localJob.value.ageMin < 18) {
            _errors.value = _errors.value.copy(age = "Minimum age must be at least 18")
            isValid = false
        } else if (_localJob.value.ageMin > _localJob.value.ageMax) {
            _errors.value = _errors.value.copy(age = "Minimum age cannot be greater than maximum age")
            isValid = false
        } else {
            _errors.value = _errors.value.copy(age = null)
        }

        if (_localJob.value.ageMax < _localJob.value.ageMin) {
            _errors.value = _errors.value.copy(age = "Maximum age must be greater than or equal to minimum age")
            isValid = false
        } else if (_localJob.value.ageMax > 99) {
            _errors.value = _errors.value.copy(age = "Maximum age cannot exceed 99")
            isValid = false
        } else {
            _errors.value = _errors.value.copy(age = null)
        }


        val selectedStatuses = _localJob.value.maritalStatuses
            .filter { it.isSelected }
            .map { it.status }

        if (selectedStatuses.isEmpty()) {
            _errors.value = _errors.value.copy(maritalStatuses = "At least 1 marital status must be selected")
            isValid = false
        } else if (!selectedStatuses.all { it in maritalStatusUnits }) {
            _errors.value = _errors.value.copy(maritalStatuses = "Invalid marital status selected")
            isValid = false
        } else {
            _errors.value = _errors.value.copy(maritalStatuses = null)
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


    fun onCreateLocalJob(
        localJobId: RequestBody,
        title: RequestBody,
        description: RequestBody,
        company: RequestBody,
        ageMin: RequestBody,
        ageMax: RequestBody,
        maritalStatus: List<RequestBody>,
        salaryUnit: RequestBody,
        salaryMin: RequestBody,
        salaryMax: RequestBody,
        images: List<MultipartBody.Part>,
        keepImageIds: List<RequestBody>,
        country: RequestBody,
        state: RequestBody,
        location: RequestBody? = null,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        onCreateOrUpdateJob = viewModelScope.launch {
            _isPublishing.value = true

            when (val result = createLocalJob(
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
                }

                is Result.Error -> {
                    val errorMsg = mapExceptionToError(result.error).errorMessage
                    onError(errorMsg)
                }
            }

            _isPublishing.value = false
        }
    }

    private suspend fun createLocalJob(
        localJobId: RequestBody,
        title: RequestBody,
        description: RequestBody,
        company: RequestBody,
        ageMin: RequestBody,
        ageMax: RequestBody,
        maritalStatus: List<RequestBody>,
        salaryUnit: RequestBody,
        salaryMin: RequestBody,
        salaryMax: RequestBody,
        images: List<MultipartBody.Part>,
        keepImageIds: List<RequestBody>,
        country: RequestBody,
        state: RequestBody,
        location: RequestBody? = null
    ): Result<ResponseReply> {

        return try {

            AppClient.instance.create(ManageLocalJobService::class.java)
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
                ).let {
                    if (it.isSuccessful) {
                        val responseBody = it.body()
                        if (responseBody != null && responseBody.isSuccessful) {
                            Result.Success(responseBody)
                        } else {
                            val errorMessage = "Failed, try again later..."
                            Result.Error(Exception(errorMessage))
                        }
                    } else {
                        Result.Error(
                            Exception(
                                try {
                                    Gson().fromJson(
                                        it.errorBody()?.string(),
                                        ErrorResponse::class.java
                                    ).message
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    "An unknown error occurred"
                                }
                            )
                        )
                    }
                }

        } catch (t: Throwable) {
            Result.Error(t)
        }
    }


    fun clearSelectedDraft() {
        onCreateOrUpdateJob?.cancel()
        _localJob.value = LocalJobState(
            maritalStatuses = listOf(
                SelectableMaritalStatus(
                    MaritalStatus.MARRIED
                ),
                SelectableMaritalStatus(
                    MaritalStatus.UNMARRIED
                )
            ),
            salaryUnit = if (userCurrency in salaryUnits) userCurrency else "INR"
        )
        _errors.value = LocalJobErrorsState()
    }


}
