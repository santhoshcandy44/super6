package com.lts360.compose.ui.services.manage.viewmodels


import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.lts360.api.utils.Result
import com.lts360.api.utils.mapExceptionToError
import com.lts360.api.app.AppClient
import com.lts360.api.app.ManageServicesApiService
import com.lts360.api.common.errors.ErrorResponse
import com.lts360.api.common.responses.ResponseReply
import com.lts360.api.models.service.EditablePlan
import com.lts360.api.models.service.EditablePlanFeature
import com.lts360.api.models.service.EditableService
import com.lts360.api.models.service.PlanFeature
import com.lts360.app.database.daos.service.DraftImageDao
import com.lts360.app.database.daos.service.DraftLocationDao
import com.lts360.app.database.daos.service.DraftPlanDao
import com.lts360.app.database.daos.service.DraftServiceDao
import com.lts360.app.database.daos.service.DraftThumbnailDao
import com.lts360.app.database.models.service.DraftImage
import com.lts360.app.database.models.service.DraftLocation
import com.lts360.app.database.models.service.DraftPlan
import com.lts360.app.database.models.service.DraftService
import com.lts360.app.database.models.service.DraftServiceWithDetails
import com.lts360.app.database.models.service.DraftThumbnail
import com.lts360.app.database.models.service.toImage
import com.lts360.app.database.models.service.toLocation
import com.lts360.app.database.models.service.toPlan
import com.lts360.app.database.models.service.toThumbnail
import com.lts360.compose.ui.chat.MAX_IMAGES
import com.lts360.compose.ui.chat.isValidImageDimensionsByMetaData
import com.lts360.compose.ui.chat.isValidThumbnailDimensionsByMetaData
import com.lts360.compose.ui.services.BitmapContainer
import com.lts360.compose.ui.services.BitmapContainerFactory
import com.lts360.compose.ui.services.ThumbnailContainer
import com.lts360.compose.ui.services.ValidatedPlan
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.math.BigDecimal
import javax.inject.Inject
import androidx.core.net.toUri
import com.lts360.components.utils.LogUtils.TAG

@HiltViewModel
class ServicesWorkflowViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    private val draftServicesDao: DraftServiceDao,
    private val draftLocationDao: DraftLocationDao,
    private val draftPlansDao: DraftPlanDao,
    private val draftImagesDao: DraftImageDao,
    private val draftThumbnailDao: DraftThumbnailDao,
    val savedStateHandle: SavedStateHandle,
) : ViewModel() {


    private val _lastEntry = MutableStateFlow<String?>(null)
    val lastEntry = _lastEntry.asStateFlow()

    private val _draftServices = MutableStateFlow<List<EditableService>>(emptyList())
    val draftServices = _draftServices.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()


    private val _refreshing = MutableStateFlow(false)
    val refreshing = _refreshing.asStateFlow()


    private val bitmapContainerFactory = BitmapContainerFactory()


    private val _status = MutableStateFlow("")
    val status = _status.asStateFlow()

    private val _draftId = MutableStateFlow<Long>(-1)
    val draftId = _draftId.asStateFlow()

    private val _title = MutableStateFlow("")
    val title = _title.asStateFlow()

    private val _shortDescription = MutableStateFlow("")
    val shortDescription = _shortDescription.asStateFlow()

    private val _longDescription = MutableStateFlow("")
    val longDescription = _longDescription.asStateFlow()


    private val _selectedLocation = MutableStateFlow<DraftLocation?>(null)
    val selectedLocation = _selectedLocation.asStateFlow()

    private val _country = MutableStateFlow<String?>(null)
    val selectedCountry = _country.asStateFlow()

    private val _state = MutableStateFlow<String?>(null)
    val selectedState = _state.asStateFlow()

    private val _industry = MutableStateFlow(-1)
    val selectedIndustry = _industry.asStateFlow()


    private val _plans = MutableStateFlow<List<ValidatedPlan>>(emptyList())
    val plans = _plans.asStateFlow()

    private val _imageContainers = MutableStateFlow<List<BitmapContainer>>(emptyList())
    val imageContainers = _imageContainers.asStateFlow()


    private val _thumbnailContainer = MutableStateFlow<ThumbnailContainer?>(null)
    val thumbnailContainer = _thumbnailContainer.asStateFlow()

    private val _titleError = MutableStateFlow<String?>(null)
    val titleError = _titleError.asStateFlow()

    private val _shortDescriptionError = MutableStateFlow<String?>(null)
    val shortDescriptionError = _shortDescriptionError.asStateFlow()

    private val _longDescriptionError = MutableStateFlow<String?>(null)
    val longDescriptionError = _longDescriptionError.asStateFlow()

    private val _selectedLocationError = MutableStateFlow<String?>(null)
    val selectedLocationError = _selectedLocationError.asStateFlow()

    private val _countryError = MutableStateFlow<String?>(null)
    val selectedCountryError = _countryError.asStateFlow()

    private val _stateError = MutableStateFlow<String?>(null)
    val selectedStateError = _stateError.asStateFlow()


    private val _industryError = MutableStateFlow<String?>(null)
    val industryError = _industryError.asStateFlow()

    private val _plansError = MutableStateFlow<String?>(null)
    val plansError = _plansError.asStateFlow()

    private val _imageContainersError = MutableStateFlow<String?>(null)
    val imageContainersError = _imageContainersError.asStateFlow()


    private val _deleteDraftDialogVisibility = MutableStateFlow(false)
    val deleteDraftDialogVisibility: StateFlow<Boolean> = _deleteDraftDialogVisibility

    private val _isPublishing = MutableStateFlow(false)
    val isPublishing = _isPublishing.asStateFlow()

    private var onCreateServiceJob: Job? = null


    val slots: StateFlow<Int> = _imageContainers
        .map { MAX_IMAGES - it.size }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            initialValue = MAX_IMAGES
        )


    private val _isPickerLaunch = MutableStateFlow(false)
    val isPickerLaunch = _isPickerLaunch.asStateFlow()

    private val _refreshImageIndex = MutableStateFlow(-1)
    val refreshImageIndex = _refreshImageIndex.asStateFlow()


    init {

        val savedStatus = savedStateHandle.get<String>("status") ?: "default_status"
        val savedDraftId = savedStateHandle.get<Long>("draftId") ?: -1L

        _status.value = savedStatus
        _draftId.value = savedDraftId

        if (_status.value == "draft" && _draftId.value != -1L) {
            loadDraftDetails(_draftId.value)
        }

        viewModelScope.launch {
            loadDraftServices()
        }

    }

    fun updateLastEntry(lastEntry: String?) {
        _lastEntry.value = lastEntry
    }

    fun setPickerLaunch(isLaunched: Boolean) {
        _isPickerLaunch.value = isLaunched
    }

    fun updateRefreshImageIndex(index: Int) {
        _refreshImageIndex.value = index
    }


    private suspend fun loadDraftServices(
        isLoading: Boolean = true,
        isRefreshing: Boolean = false
    ) {
        if (isLoading) {
            _isLoading.value = true
        }

        if (isRefreshing) {
            _refreshing.value = true
        }

        draftServicesDao.getAllDraftServicesWithDetails().collectLatest {

            _draftServices.value = it.map { draftServiceDetails ->

                val draftService = draftServiceDetails.draftService
                val draftImages = draftServiceDetails.draftImages
                val draftThumbnail = draftServiceDetails.draftThumbnail
                val draftPlans = draftServiceDetails.draftPlans

                Log.e(
                    TAG, "RECEIVER ${
                        EditableService(
                            serviceId = draftService.id,
                            title = draftService.title ?: "",
                            shortDescription = draftService.shortDescription ?: "",
                            longDescription = draftService.longDescription ?: "",
                            industry = draftService.industry ?: -1,
                            status = "draft",
                            images = draftImages.map { image -> image.toImage() },
                            plans = draftPlans.map { plan -> plan.toPlan() },
                            location = _selectedLocation.value?.toLocation(),
                            thumbnail = draftThumbnail?.toThumbnail(),
                            country = draftService.country,
                            state = draftService.state
                        )
                    }"
                )
                EditableService(
                    serviceId = draftService.id,
                    title = draftService.title ?: "",
                    shortDescription = draftService.shortDescription ?: "",
                    longDescription = draftService.longDescription ?: "",
                    industry = draftService.industry ?: -1,
                    status = "draft",
                    images = draftImages.map { image -> image.toImage() },
                    plans = draftPlans.map { plan -> plan.toPlan() },
                    location = _selectedLocation.value?.toLocation(),
                    thumbnail = draftThumbnail?.toThumbnail(),
                    country = draftService.country,
                    state = draftService.state
                )
            }

            if (_status.value == "draft" && _draftId.value != -1L) {
                loadDraftDetails(_draftId.value)
            }

            if (isLoading) {
                _isLoading.value = false
            }

            if (isRefreshing) {
                _refreshing.value = false
            }

        }

    }


    fun onRefresh() {
        viewModelScope.launch {
            loadDraftServices(isLoading = false, isRefreshing = true)
        }
    }


    fun updateDraftInfoAndLoadDraftDetails(status: String, draftId: Long) {
        _status.value = status
        _draftId.value = draftId

        savedStateHandle["status"] = status
        savedStateHandle["draftId"] = draftId

        if (status == "draft" && draftId != -1L) {
            loadDraftDetails(draftId)
        }
    }

    fun setDeleteDraftDialogVisibility(isVisible: Boolean) {
        _deleteDraftDialogVisibility.value = isVisible
    }

    fun updateTitle(newTitle: String) {
        _title.value = newTitle
        if (newTitle.isNotBlank()) {
            _titleError.value = null
        }
    }


    fun updateShortDescription(newDescription: String) {
        _shortDescription.value = newDescription
        if (newDescription.isNotBlank()) {
            _shortDescriptionError.value = null
        }
    }

    fun updateLongDescription(newDescription: String) {
        _longDescription.value = newDescription
        if (newDescription.isNotBlank()) {
            _longDescriptionError.value = null
        }
    }

    fun updateIndustry(newIndustry: Int) {
        _industry.value = newIndustry
        if (newIndustry != -1) {
            _industryError.value = null
        }
    }

    fun updateCountry(newCountry: String?) {
        _country.value = newCountry
        if (newCountry != null) {
            _countryError.value = null
        }
    }


    fun updateState(newState: String?) {
        _state.value = newState
        if (newState != null) {
            _stateError.value = null
        }
    }

    fun updateLocation(newLocation: DraftLocation) {
        _selectedLocation.value = newLocation
        _selectedLocationError.value = null

    }


    fun addPlan() {
        val newPlan = ValidatedPlan(
            true,
            EditablePlan(
                planId = -1,
                planName = "",
                planDescription = "",
                planPrice = BigDecimal.ZERO,
                planPriceUnit = "",
                planFeatures = listOf(
                    EditablePlanFeature(featureName = "", featureValue = null)
                ),
                planDurationUnit = "",
                planDeliveryTime = -1
            )
        )

        _plans.value += newPlan
        _plansError.value = null

    }


    fun removePlan(plan: EditablePlan) {
        _plans.value = _plans.value.filter { it.editablePlan.planId != plan.planId }
    }

    fun updatePlan(index: Int, updatedPlan: ValidatedPlan) {
        _plans.value = _plans.value.mapIndexed { i, plan ->
            if (i == index) updatedPlan else plan
        }

    }

    private fun updatePlans(newPlans: List<ValidatedPlan>) {
        _plans.value = newPlans
    }


    fun addContainer(
        path: String,
        width: Int,
        height: Int,
        format: String,
        errorMessage: String? = null
    ) {
        _imageContainers.value = _imageContainers.value.toMutableList().apply {
            add(createBitmapContainer(path, width, height, format, errorMessage))
        }
        _imageContainersError.value = null

    }

    fun updateContainer(
        index: Int,
        path: String,
        width: Int,
        height: Int,
        format: String,
        errorMessage: String? = null,
    ) {
        val currentContainers = _imageContainers.value.toMutableList()
        if (index in currentContainers.indices) {
            currentContainers[index] = currentContainers[index].copy(
                path = path,
                width = width,
                height = height,
                format = format,
                errorMessage = errorMessage
            )
            _imageContainers.value = currentContainers
        }
    }

    fun removeContainer(index: Int) {
        val updatedContainers = _imageContainers.value.toMutableList()
        if (index in updatedContainers.indices) {
            updatedContainers.removeAt(index)
            _imageContainers.value = updatedContainers
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

    fun updateThumbnailContainer(
        path: String,
        width: Int,
        height: Int,
        format: String,
        errorMessage: String? = null,
    ) {

        val currentContainer = _thumbnailContainer.value

        if (currentContainer != null) {
            _thumbnailContainer.value = currentContainer.copy(
                path = path,
                format = format,
                width = width,
                height = height,
                errorMessage = errorMessage
            )
        } else {

            _thumbnailContainer.value = ThumbnailContainer(
                containerId = "THUMBNAIL_CONTAINER",
                path = path,
                format = format,
                width = width,
                height = height,
                errorMessage = errorMessage
            )
        }
    }


    private fun loadImageContainers(bitmapContainers: List<BitmapContainer>) {
        _imageContainers.value = bitmapContainers
    }


    private fun loadDraftDetails(
        draftId: Long,
        draftServiceWithDetails: DraftServiceWithDetails? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {

            val draftServiceDetails = draftServiceWithDetails ?: draftServicesDao.getDraftServiceWithDetails(draftId) ?: return@launch

            val draftService = draftServiceDetails.draftService
            val draftImages = draftServiceDetails.draftImages
            val draftPlans = draftServiceDetails.draftPlans
            val draftLocation = draftServiceDetails.draftLocation
            val draftThumbnail = draftServiceDetails.draftThumbnail


            updateTitle(draftService.title ?: "")
            updateShortDescription(draftService.shortDescription ?: "")
            updateLongDescription(draftService.longDescription ?: "")
            draftService.industry?.let {
                updateIndustry(it)
            }
            draftService.country?.let {
                updateCountry(it)
            }
            draftService.state?.let {
                updateState(it)
            }
            updatePlans(draftPlans.map { plan -> ValidatedPlan(true, plan.toPlan()) })
            draftLocation?.let {
                updateLocation(it)
            }

            loadImageContainers(draftImages.map {
                val result = isValidImageDimensionsByMetaData(
                    it.width,
                    it.height,
                    it.format
                )
                val errorMessage = when {
                    !result.first -> "Invalid Dimension"
                    !result.second -> "Invalid Format"
                    else -> null
                }

                createBitmapContainer(
                    it.data,
                    it.width,
                    it.height,
                    it.format,
                    errorMessage
                )
            })

            draftThumbnail?.let { nonNullDraftThumbnail ->

                val result = isValidThumbnailDimensionsByMetaData(
                    nonNullDraftThumbnail.width,
                    nonNullDraftThumbnail.height,
                    nonNullDraftThumbnail.format
                )
                val errorMessage = when {
                    !result.first -> "Invalid Dimension"
                    !result.second -> "Invalid Format"
                    else -> null
                }

                updateThumbnailContainer(
                    nonNullDraftThumbnail.data,
                    nonNullDraftThumbnail.width,
                    nonNullDraftThumbnail.height,
                    nonNullDraftThumbnail.format,
                    errorMessage
                )
            }

        }
    }


    fun clearSelectedDraft() {

        onCreateServiceJob?.cancel()

        _draftId.value = -1
        _status.value = ""

        savedStateHandle.remove<String>("status")
        savedStateHandle.remove<Long>("draftId")

        _title.value = ""
        _shortDescription.value = ""
        _longDescription.value = ""
        _thumbnailContainer.value = null
        _imageContainers.value = emptyList()
        _industry.value = -1
        _plans.value = emptyList()
        _country.value = ""
        _state.value = ""
        _selectedLocation.value = null

        _titleError.value = null
        _shortDescriptionError.value = null
        _longDescriptionError.value = null
        _imageContainersError.value = null
        _plansError.value = null
        _industryError.value = null
        _countryError.value = null
        _stateError.value = null
        _selectedLocationError.value = null

    }


    private fun validateServiceTitle(): Boolean {
        return if (_title.value.isBlank()) {
            _titleError.value = "Service title cannot be empty"
            false
        } else if (_shortDescription.value.length > 100) {
            _title.value = "Service title cannot be exceed 100 characters"
            false
        } else {
            _titleError.value = null
            true
        }
    }

    private fun validateShortDescription(): Boolean {
        return if (_shortDescription.value.isBlank()) {
            _shortDescriptionError.value = "Short description cannot be empty"
            false
        } else if (_shortDescription.value.length > 250) {
            _shortDescription.value = "Service short description cannot be exceed 250 characters"
            false
        } else {
            _shortDescriptionError.value = null
            true
        }
    }

    private fun validateLongDescription(): Boolean {
        return if (_longDescription.value.isBlank()) {
            _longDescriptionError.value = "Long description cannot be empty"
            false
        } else if (_longDescription.value.length > 5000) {
            _longDescription.value = "Service long description cannot be exceed 5000 characters"
            false
        } else {
            _longDescriptionError.value = null
            true
        }
    }

    private fun validateSelectedIndustry(): Boolean {
        return if (_industry.value == -1) {
            _industryError.value = "Industry must be selected"
            false
        } else {
            _industryError.value = null
            true
        }
    }


    private fun validateSelectedCountry(): Boolean {
        return if (_country.value == null) {
            _countryError.value = "Country must be selected"
            false
        } else {
            _countryError.value = null
            true
        }

    }


    private fun validateSelectedState(): Boolean {
        return if (_state.value == null) {
            _stateError.value = "State must be selected"
            false
        } else {
            _stateError.value = null
            true
        }

    }


    private fun validatePlans(): Boolean {
        if (_plans.value.isEmpty()) {
            _plansError.value = "At least one plan must be added"
            return false
        }

        val updatedPlans = _plans.value.map { validatedPlan ->
            val editablePlan = validatedPlan.editablePlan
            var isValid = true

            // Perform validation
            if (editablePlan.planName.isEmpty() || editablePlan.planName.length > 20) isValid =
                false
            if (editablePlan.planDescription.isEmpty() || editablePlan.planDescription.length > 200) isValid =
                false
            if (editablePlan.planDurationUnit.isEmpty()) isValid = false
            if (editablePlan.planDeliveryTime == -1) isValid = false
            if (editablePlan.planPrice == BigDecimal.ZERO) isValid = false
            if (editablePlan.planPriceUnit.isEmpty()) isValid = false
            if (editablePlan.planFeatures.isEmpty()) isValid = false
            editablePlan.planFeatures.forEach { feature ->
                if (feature.featureName.isEmpty() || feature.featureName.length > 40 || feature.featureValue.toString().length > 10 || feature.featureValue == null) isValid =
                    false
            }

            ValidatedPlan(isValid, editablePlan)
        }


        _plans.value = updatedPlans

        _plansError.value =
            if (updatedPlans.all { it.isValid }) null else "One or more plans are invalid"
        return updatedPlans.all { it.isValid }
    }


    private fun validateThumbnailContainer(): Boolean {

        return if (_thumbnailContainer.value != null && _thumbnailContainer.value?.errorMessage == null) {
            true
        } else {
            _thumbnailContainer.value = _thumbnailContainer.value?.copy(
                errorMessage = if (_thumbnailContainer.value?.errorMessage == null)
                    "Select thumbnail image" else _thumbnailContainer.value?.errorMessage
            )
            false
        }
    }


    private fun validateImageContainers(): Boolean {
        return if (_imageContainers.value.isEmpty()) {
            _imageContainersError.value = "At least one image must be added"
            false
        } else {
            _imageContainersError.value = null
            true
        }
    }

    private fun validateSelectedLocation(): Boolean {
        return if (_selectedLocation.value == null) {
            _selectedLocationError.value = "Location must be selected"
            false
        } else {
            _selectedLocationError.value = null
            true
        }
    }


    fun validateAll(): Boolean {

        val isServiceTitleValid = validateServiceTitle()
        val isShortDescriptionValid = validateShortDescription()
        val isLongDescriptionValid = validateLongDescription()
        val isSelectedLocationValid = validateSelectedLocation()
        val isSelectedIndustryValid = validateSelectedIndustry()
        val isSelectedCountry = validateSelectedCountry()
        val isSelectedState = validateSelectedState()

        val isPlansValid = validatePlans()
        val isContainersValid = validateImageContainers()
        val isThumbnailContainerValid = validateThumbnailContainer()

        return isThumbnailContainerValid && isServiceTitleValid && isShortDescriptionValid &&
                isLongDescriptionValid && isSelectedLocationValid &&
                isSelectedIndustryValid && isSelectedCountry
                && isSelectedState
                && isPlansValid && isContainersValid
    }


    fun draft(
        draftService: DraftService,
        draftImages: List<BitmapContainer>,
        draftPlans: List<EditablePlan>,
        draftLocation: DraftLocation?,
        draftThumbnail: ThumbnailContainer?,
        onSuccess: () -> Unit,
    ) {
        viewModelScope.launch(Dispatchers.IO) {

            val serviceId = draftServicesDao.insert(draftService)


            if (draftImages.isNotEmpty()) {
                val cacheDir = File(context.filesDir, "draft")
                if (!cacheDir.exists()) {
                    cacheDir.mkdirs()
                }

                val cachedDraftImages = draftImages.mapNotNull { draftImage ->
                    try {
                        val inputStream =
                            context.contentResolver.openInputStream(draftImage.path.toUri())
                                ?: return@mapNotNull null

                        val outputFile = File(
                            cacheDir,
                            "draft_image_${System.currentTimeMillis()}.jpg"
                        )
                        val outputStream = outputFile.outputStream()

                        inputStream.use { input ->
                            outputStream.use { output ->
                                input.copyTo(output)
                            }
                        }

                        DraftImage(
                            serviceId = serviceId,
                            data = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.provider",
                                outputFile
                            ).toString(),
                            format = draftImage.format,
                            width = draftImage.width,
                            height = draftImage.height
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                }

                if (cachedDraftImages.isNotEmpty()) {
                    draftImagesDao.insert(cachedDraftImages)
                }
            }


            if (draftPlans.isNotEmpty()) {
                draftPlansDao.insert(
                    draftPlans.map { editablePlan ->
                        DraftPlan(
                            serviceId = serviceId,
                            planName = editablePlan.planName,
                            planDescription = editablePlan.planDescription,
                            planPrice = editablePlan.planPrice,
                            planPriceUnit = editablePlan.planPriceUnit,
                            planDeliveryTime = editablePlan.planDeliveryTime,
                            planFeatures = editablePlan.planFeatures.map {
                                PlanFeature(it.featureName, it.featureValue)
                            },
                            planDurationUnit = editablePlan.planDurationUnit
                        )
                    }
                )
            }


            draftLocation?.let {
                draftLocationDao.insert(
                    draftLocation.copy(
                        serviceId = serviceId
                    )
                )
            }

            draftThumbnail?.let {
                val thumbnailUri = it.path.toUri()
                context.contentResolver.openInputStream(thumbnailUri)?.use { inputStream ->

                    val draftDir = File(context.filesDir, "draft")
                    if (!draftDir.exists()) {
                        draftDir.mkdirs()
                    }

                    val outputFile = File(
                        draftDir,
                        "thumbnail_${System.currentTimeMillis()}.jpg"
                    )


                    val outputStream = outputFile.outputStream()

                    inputStream.use { input ->
                        outputStream.use { output ->
                            input.copyTo(output)
                        }
                    }

                    draftThumbnailDao.insert(
                        DraftThumbnail(
                            serviceId = serviceId,
                            data = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.provider",
                                outputFile
                            ).toString(),
                            format = it.format,
                            width = it.width,
                            height = it.height
                        )
                    )
                }
            }

            _draftId.value = serviceId
            _status.value = "draft"

            savedStateHandle["draftId"] = serviceId
            savedStateHandle["status"] = "draft"

            withContext(Dispatchers.Main) {
                onSuccess()
            }
        }
    }


    fun updateDraft(
        draftId: Long,
        draftService: DraftService,
        draftImages: List<BitmapContainer>,
        draftPlans: List<EditablePlan>,
        draftLocation: DraftLocation?,
        draftThumbnail: ThumbnailContainer?,
        onSuccess: () -> Unit,
    ) {
        viewModelScope.launch(Dispatchers.IO) {


            draftImagesDao.deleteImagesByServiceId(draftId)
            draftPlansDao.deletePlanByServiceId(draftId)
            draftLocationDao.deleteLocationByServiceId(draftId)
            draftThumbnailDao.deleteThumbnailByServiceId(draftId)

            draftServicesDao.update(draftService)

            if (draftImages.isNotEmpty()) {

                val cacheDir = File(context.filesDir, "draft")
                if (!cacheDir.exists()) {
                    cacheDir.mkdirs()
                }

                val cachedDraftImages = draftImages.mapNotNull { draftImage ->
                    try {

                        val inputStream =
                            context.contentResolver.openInputStream(draftImage.path.toUri())
                                ?: return@mapNotNull null

                        val outputFile = File(
                            cacheDir,
                            "draft_image_${System.currentTimeMillis()}.jpg"
                        )

                        inputStream.use { input ->
                            outputFile.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }

                        DraftImage(
                            serviceId = draftId,
                            data = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.provider",
                                outputFile
                            ).toString(),
                            format = draftImage.format,
                            width = draftImage.width,
                            height = draftImage.height
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                }

                if (cachedDraftImages.isNotEmpty()) {
                    draftImagesDao.insert(cachedDraftImages)
                }

                draftImages.forEach {
                    deleteDraftCacheFile(it.path.toUri())
                }
            }

            if (draftPlans.isNotEmpty()) {
                draftPlansDao.insert(
                    draftPlans.map { editablePlan ->
                        DraftPlan(
                            serviceId = draftId,
                            planName = editablePlan.planName,
                            planDescription = editablePlan.planDescription,
                            planPrice = editablePlan.planPrice,
                            planPriceUnit = editablePlan.planPriceUnit,
                            planDeliveryTime = editablePlan.planDeliveryTime,
                            planFeatures = editablePlan.planFeatures.map {
                                PlanFeature(it.featureName, it.featureValue)
                            },
                            planDurationUnit = editablePlan.planDurationUnit
                        )
                    }
                )
            }

            draftLocation?.let {
                draftLocationDao.insert(
                    draftLocation.copy(
                        serviceId = draftId
                    )
                )
            }

            draftThumbnail?.let { nonNullDraftThumbnail ->

                context.contentResolver.openInputStream(nonNullDraftThumbnail.path.toUri())
                    ?.use { input ->

                        val draftDir = File(context.filesDir, "draft")

                        if (!draftDir.exists()) {
                            draftDir.mkdirs()
                        }

                        val outputFile = File(
                            draftDir,
                            "thumbnail_${System.currentTimeMillis()}.jpg"
                        )

                        outputFile.outputStream().use { output ->
                            input.copyTo(output)
                        }

                        draftThumbnailDao.insert(
                            DraftThumbnail(
                                serviceId = draftId,
                                data = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.provider",
                                    outputFile
                                ).toString(),
                                format = nonNullDraftThumbnail.format,
                                width = nonNullDraftThumbnail.width,
                                height = nonNullDraftThumbnail.height
                            )
                        )
                    }

                deleteDraftCacheFile(nonNullDraftThumbnail.path.toUri())

            }

            withContext(Dispatchers.Main) {
                onSuccess()
            }
        }
    }

    fun deleteDraft(serviceId: Long, onSuccess: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            draftImagesDao.getImagesCachePathsByServiceId(serviceId)
                .forEach {
                    deleteDraftCacheFile(it.toUri())
                }
            draftThumbnailDao.getThumbnailPathByServiceId(serviceId)?.let {
                deleteDraftCacheFile(it.toUri())
            }
            draftServicesDao.deleteDraftById(serviceId)

            withContext(Dispatchers.Main) {
                onSuccess()
            }
        }
    }

    private fun deleteDraftCacheFile(contentUri: Uri) {

        try {
            val rowsDeleted = context.contentResolver.delete(contentUri, null, null)
            if (rowsDeleted > 0) {
                Log.d("Delete", "Content URI deleted successfully")
            } else {
                Log.d("Delete", "No rows deleted. Possibly not found or no permission.")
            }
        } catch (e: SecurityException) {
            Log.e("Delete", "Security exception when deleting: ${e.message}")
        } catch (e: Exception) {
            Log.e("Delete", "General error deleting content URI: ${e.message}")
        }
    }


    fun onCreateService(
        title: RequestBody,
        longDescription: RequestBody,
        shortDescription: RequestBody,
        industry: RequestBody,
        country: RequestBody,
        state: RequestBody,
        thumbnail: MultipartBody.Part,
        images: List<MultipartBody.Part>,
        plans: RequestBody,
        location: RequestBody?,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
    ) {
        onCreateServiceJob = viewModelScope.launch {
            _isPublishing.value = true

            when (val result = createService(
                title,
                longDescription,
                shortDescription,
                industry,
                country,
                state,
                thumbnail,
                images,
                plans,
                location
            )) {
                is Result.Success -> {
                    deleteDraft(_draftId.value) {}
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

    private suspend fun createService(
        title: RequestBody,
        longDescription: RequestBody,
        shortDescription: RequestBody,
        industry: RequestBody,
        country: RequestBody,
        state: RequestBody,
        thumbnail: MultipartBody.Part,
        images: List<MultipartBody.Part>,
        plans: RequestBody,
        location: RequestBody? = null,
    ): Result<ResponseReply> {

        return try {
            val response = AppClient.instance.create(ManageServicesApiService::class.java)
                .createService(
                    title,
                    longDescription,
                    shortDescription,
                    industry,
                    country,
                    state,
                    thumbnail,
                    images,
                    plans,
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
