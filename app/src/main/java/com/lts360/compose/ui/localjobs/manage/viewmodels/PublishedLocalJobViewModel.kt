package com.lts360.compose.ui.localjobs.manage.viewmodels

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lts360.api.utils.Result
import com.lts360.api.utils.ResultError
import com.lts360.api.utils.mapExceptionToError
import com.lts360.api.app.AppClient
import com.lts360.api.app.ManageLocalJobService
import com.lts360.api.common.errors.ErrorResponse
import com.lts360.api.common.responses.ResponseReply
import com.lts360.api.models.service.EditableLocation
import com.lts360.compose.ui.chat.MAX_IMAGES
import com.lts360.compose.ui.localjobs.manage.MaritalStatus
import com.lts360.compose.ui.localjobs.manage.PublishedLocalJobsRepository
import com.lts360.compose.ui.localjobs.manage.repos.LocalJobApplicantsPageSource
import com.lts360.compose.ui.localjobs.models.EditableLocalJob
import com.lts360.compose.ui.localjobs.models.LocalJob
import com.lts360.compose.ui.localjobs.models.toEditableLocalJob
import com.lts360.compose.ui.managers.NetworkConnectivityManager
import com.lts360.compose.ui.managers.UserSharedPreferencesManager
import com.lts360.compose.ui.profile.repos.UserProfileRepository
import com.lts360.compose.ui.services.manage.models.CombinedContainer
import com.lts360.compose.ui.services.manage.models.CombinedContainerFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Currency
import java.util.Locale
import kotlin.collections.map

class PublishedLocalJobViewModel(
    val context: Context,
    val savedStateHandle: SavedStateHandle,
    private val repository: PublishedLocalJobsRepository,
    networkConnectivityManager: NetworkConnectivityManager,
    userProfileRepository: UserProfileRepository
) : ViewModel() {

    val userId: Long = UserSharedPreferencesManager.userId
    val isProfileCompletedFlow = userProfileRepository.isProfileCompletedFlow(userId)
    val unCompletedProfileFieldsFlow = userProfileRepository.unCompletedProfileFieldsFlow(userId)

    data class LocalJobState(
        val localJobId: Long = -1,
        val title: String = "",
        val description: String = "",
        val company: String = "",
        val ageMin: Int = 18,
        val ageMax: Int = 40,
        val maritalStatuses: List<SelectableMaritalStatus> = emptyList(),
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


    val connectivityManager = networkConnectivityManager

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


    private var onUpdateLocalJob: Job? = null

    private var errorMessage: String = ""

    private val _publishedLocalJobs = MutableStateFlow<List<EditableLocalJob>>(emptyList())
    val publishedLocalJobs = _publishedLocalJobs.asStateFlow()

    private val _selectedItem = MutableStateFlow<EditableLocalJob?>(null)
    val selectedItem = _selectedItem.asStateFlow()

    private val _pageSource = LocalJobApplicantsPageSource(pageSize = 30)
    val pageSource: LocalJobApplicantsPageSource get() = _pageSource


    private val _resultError = MutableStateFlow<ResultError?>(null)
    val resultError = _resultError.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _isUpdating = MutableStateFlow(false)
    val isUpdating = _isUpdating.asStateFlow()

    private val _isDeleting = MutableStateFlow(false)
    val isDeleting = _isDeleting.asStateFlow()


    private val containerFactory = CombinedContainerFactory()

    init {
        _isLoading.value = true
        onGetPublishedLocalJobs(userId, {
            _isLoading.value = false
        }, {
            _isLoading.value = false
            val mappedException = mapExceptionToError(it)
            _resultError.value = mappedException
            errorMessage = mappedException.errorMessage
        })
    }

    fun setPickerLaunch(isLaunched: Boolean) {
        _isPickerLaunch.value = isLaunched
    }

    fun updateRefreshImageIndex(index: Int) {
        _refreshImageIndex.value = index
    }

    private fun updatePublishedLocalJobs(items: List<EditableLocalJob>) {
        _publishedLocalJobs.value = items
    }


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

        _selectedItem.value?.let {
            if (it.localJobId == item.localJobId) {
                _selectedItem.update {
                    item
                }
            }
        }
    }

    fun refreshPublishedItems(userId: Long) {
        _resultError.value = null
        _isRefreshing.value = true
        onGetPublishedLocalJobs(userId, {
            _isRefreshing.value = false
        }, {
            _isRefreshing.value = false
            val mappedException = mapExceptionToError(it)
            _resultError.value = mappedException
            errorMessage = mapExceptionToError(it).errorMessage
        })
    }

    fun isSelectedLocalJobNull(): Boolean {
        return _selectedItem.value == null
    }

    fun setSelectedItemAndLoadApplicants(itemId: Long) {
        val index = _publishedLocalJobs.value.indexOfFirst {
            it.localJobId == itemId
        }
        if (index != -1) {
            _selectedItem.value = _publishedLocalJobs.value[index]
        }
        onNextPageLocalJobApplicants(itemId)
    }

    fun setSelectedItem(itemId: Long) {
        val index = _publishedLocalJobs.value.indexOfFirst {
            it.localJobId == itemId
        }
        if (index != -1) {
            _selectedItem.value = _publishedLocalJobs.value[index]
        }
        _selectedItem.value?.let {
            loadItemDetails(it)
        }
    }

    fun removeSelectedItem(itemId: Long) {

        val index = _publishedLocalJobs.value.indexOfFirst {
            it.localJobId == itemId
        }

        if (index != -1) {
            _publishedLocalJobs.value = _publishedLocalJobs.value.filter {
                it.localJobId != itemId
            }
        }

        _selectedItem.value = null
    }

    private fun createNewPageSource(): LocalJobApplicantsPageSource {
        return LocalJobApplicantsPageSource(pageSize = 30)
    }


    private fun loadItemDetails(item: EditableLocalJob) {

        _errors.value = LocalJobErrorsState()
        _localJob.value = _localJob.value.copy(localJobId = item.localJobId)
        updateTitle(item.title)
        updateDescription(item.description)
        updateCompany(item.company)
        updateAgeMin(item.ageMin)
        updateAgeMax(item.ageMax)
        updateSelectedMaritalStatus(item.maritalStatuses.mapNotNull { key ->
            maritalStatusUnits.find { it.key == key }
        })
        updateSalaryUnit(item.salaryUnit)
        updateSalaryMin(item.salaryMin)
        updateSalaryMax(item.salaryMax)

        updateCountry(item.country ?: "")
        updateState(item.state ?: "")
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

    fun updateSelectedMaritalStatus(maritalStatuses: List<MaritalStatus>) {
        val updatedStatuses = _localJob.value.maritalStatuses.map { selectableStatus ->
            val isSelected = maritalStatuses.contains(selectableStatus.status)
            selectableStatus.copy(isSelected = isSelected)
        }
        _localJob.value = _localJob.value.copy(maritalStatuses = updatedStatuses)
        _errors.value = _errors.value.copy(maritalStatuses = null)
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


        if (_localJob.value.ageMin < 18) {
            _errors.value = _errors.value.copy(age = "Minimum age must be at least 18")
            isValid = false
        } else if (_localJob.value.salaryMax != -1 && _localJob.value.ageMin > _localJob.value.ageMax) {
            _errors.value =
                _errors.value.copy(age = "Minimum age cannot be greater than maximum age")
            isValid = false
        } else {
            _errors.value = _errors.value.copy(age = null)
        }

        if (_localJob.value.ageMax < _localJob.value.ageMin) {
            _errors.value =
                _errors.value.copy(age = "Maximum age must be greater than or equal to minimum age")
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
            _errors.value =
                _errors.value.copy(maritalStatuses = "At least 1 marital status must be selected")
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

        if (_localJob.value.salaryMin < 0) {
            _errors.value = _errors.value.copy(salaryMin = "Minimum salary must be set")
            isValid = false
        } else if (_localJob.value.salaryMax != -1 && _localJob.value.salaryMin > _localJob.value.salaryMax) {
            _errors.value =
                _errors.value.copy(salaryMin = "Minimum salary cannot be greater than maximum salary")
            isValid = false
        } else {
            _errors.value = _errors.value.copy(salaryMin = null)
        }

        if (_localJob.value.salaryMax != -1 && _localJob.value.salaryMax < _localJob.value.salaryMin) {
            _errors.value =
                _errors.value.copy(salaryMax = "Maximum salary must be greater than or equal to minimum salary")
            isValid = false
        } else {
            _errors.value = _errors.value.copy(salaryMax = null)
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


    fun onGetPublishedLocalJobs(
        userId: Long,
        onSuccess: (items: List<EditableLocalJob>) -> Unit,
        onError: (Throwable) -> Unit,
    ) {

        viewModelScope.launch {
            try {
                when (val result = repository.getLocalJobsByUserId(userId)) {
                    is Result.Success -> {
                        val data = Gson().fromJson(
                            result.data.data,
                            object : TypeToken<List<LocalJob>>() {}.type
                        )
                                as List<LocalJob>
                        val listings = data.map { it.toEditableLocalJob() }

                        updatePublishedLocalJobs(listings)
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
    }


    fun onUpdateLocalJob(
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
        onUpdateLocalJob = viewModelScope.launch {
            _isUpdating.value = true

            when (val result = updateLocalJob(
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

                    updateLocalJobId(
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


    fun onNextPageLocalJobApplicants(userId: Long) {
        viewModelScope.launch {
            _pageSource.nextPage(userId)
        }
    }

    fun onUpdateLastLoadedApplicantItemPosition(lastLoadedItemPosition: Int) {
        _pageSource.updateLastLoadedItemPosition(lastLoadedItemPosition)
    }

    fun onRefreshApplicants(localJobId: Long) {
        viewModelScope.launch {
            _pageSource.refresh(localJobId)
        }
    }

    fun onRetryGetApplicants(localJobId: Long) {
        viewModelScope.launch {
            _pageSource.retry(localJobId)
        }
    }


    fun onMarkAsReviewedLocalJob(
        userId: Long,
        localJobId: Long,
        applicantId: Long,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            _pageSource.markAsReviewedLocalJob(userId, localJobId, applicantId, onSuccess, onError)
        }
    }

    fun onUnmarkAsReviewedLocalJob(
        userId: Long, localJobId: Long, applicantId: Long,
        onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            _pageSource.unmarkAsReviewedLocalJob(
                userId,
                localJobId,
                applicantId,
                onSuccess,
                onError
            )
        }
    }


    private suspend fun updateLocalJob(
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


    fun formatZonedDateTimeFormat(zonedDateTime: String): String {
        val parsedDateTime = ZonedDateTime.parse(zonedDateTime)
        val formatted = parsedDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        return formatted
    }
}



















