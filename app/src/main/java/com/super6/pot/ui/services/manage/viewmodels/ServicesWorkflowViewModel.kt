package com.super6.pot.ui.services.manage.viewmodels


import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.super6.pot.api.app.AppClient
import com.super6.pot.api.common.errors.ErrorResponse
import com.super6.pot.api.common.responses.ResponseReply
import com.super6.pot.api.models.service.EditablePlan
import com.super6.pot.api.models.service.EditablePlanFeature
import com.super6.pot.api.models.service.EditableService
import com.super6.pot.api.models.service.PlanFeature
import com.super6.pot.app.database.daos.service.DraftLocationDao
import com.super6.pot.app.database.daos.service.DraftPlanDao
import com.super6.pot.app.database.daos.service.DraftServiceDao
import com.super6.pot.app.database.daos.service.DraftThumbnailDao
import com.super6.pot.app.database.models.service.DraftImage
import com.super6.pot.app.database.models.service.toLocation
import com.super6.pot.app.database.models.service.toPlan
import com.super6.pot.app.database.models.service.toThumbnail
import com.super6.pot.app.database.models.service.DraftServiceWithDetails
import com.super6.pot.ui.services.BitmapContainer
import com.super6.pot.ui.services.BitmapContainerFactory
import com.super6.pot.ui.services.ThumbnailContainer
import com.super6.pot.ui.services.ValidatedPlan
import com.super6.pot.api.Utils.Result
import com.super6.pot.ui.chat.isValidImageDimensionsByMetaData
import com.super6.pot.ui.chat.isValidThumbnailDimensionsByMetaData
import com.super6.pot.api.Utils.mapExceptionToError
import com.super6.pot.api.app.ManageServicesApiService
import com.super6.pot.app.database.daos.service.DraftImageDao
import com.super6.pot.app.database.models.service.DraftLocation
import com.super6.pot.app.database.models.service.DraftService
import com.super6.pot.app.database.models.service.DraftPlan
import com.super6.pot.app.database.models.service.DraftThumbnail
import com.super6.pot.app.database.models.service.toImage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.FileInputStream
import java.math.BigDecimal
import javax.inject.Inject

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

    private val _draftServices = MutableStateFlow<List<EditableService>>(emptyList())
    val draftServices = _draftServices.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> get() = _refreshing



    private val _isDraftLoading = MutableStateFlow(false)
    val isDraftLoading: StateFlow<Boolean> get() = _isDraftLoading



    private val bitmapContainerFactory = BitmapContainerFactory()

    /*
        private val _isLoading = MutableStateFlow(false)
        val isLoading = _isLoading.asStateFlow()*/

    // Define MutableStateFlow for service fields
    private val _status = MutableStateFlow<String>( "")
    val status = _status.asStateFlow()

    // Define MutableStateFlow for service fields
    private val _draftId = MutableStateFlow<Long>(-1)
    val draftId = _draftId.asStateFlow()


    // Define MutableStateFlow for service fields
    private val _title = MutableStateFlow("")
    val title = _title.asStateFlow()

    private val _shortDescription = MutableStateFlow("")
    val shortDescription = _shortDescription.asStateFlow()

    private val _longDescription = MutableStateFlow("")
    val longDescription = _longDescription.asStateFlow()


    private val _selectedLocation = MutableStateFlow<DraftLocation?>(null)
    val selectedLocation = _selectedLocation.asStateFlow()

    // MutableStateFlow to manage the location bottom sheet visibility state
    private val _country = MutableStateFlow<String?>(null)
    val selectedCountry = _country.asStateFlow()

    // MutableStateFlow to manage the location bottom sheet visibility state
    private val _state = MutableStateFlow<String?>(null)
    val selectedState = _state.asStateFlow()

    // MutableStateFlow to manage the location bottom sheet visibility state
    private val _industry = MutableStateFlow(-1)
    val selectedIndustry = _industry.asStateFlow()


    // MutableStateFlow to hold the list of EditablePlans
    private val _plans = MutableStateFlow<List<ValidatedPlan>>(emptyList())
    val plans = _plans.asStateFlow()

    // Define a MutableStateFlow for the containers list
    private val _imageContainers = MutableStateFlow<List<BitmapContainer>>(emptyList())
    val imageContainers = _imageContainers.asStateFlow()


    // Define a MutableStateFlow for the containers list
    private val _thumbnailContainer = MutableStateFlow<ThumbnailContainer?>(null)
    val thumbnailContainer = _thumbnailContainer.asStateFlow()


    // Error messages
    private val _titleError = MutableStateFlow<String?>(null)
    val titleError = _titleError.asStateFlow()

    private val _shortDescriptionError = MutableStateFlow<String?>(null)
    val shortDescriptionError = _shortDescriptionError.asStateFlow()

    private val _longDescriptionError = MutableStateFlow<String?>(null)
    val longDescriptionError = _longDescriptionError.asStateFlow()

    private val _selectedLocationError = MutableStateFlow<String?>(null)
    val selectedLocationError = _selectedLocationError.asStateFlow()

    // MutableStateFlow to manage the location bottom sheet visibility state
    private val _countryError = MutableStateFlow<String?>(null)
    val selectedCountryError = _countryError.asStateFlow()

    // MutableStateFlow to manage the location bottom sheet visibility state
    private val _stateError = MutableStateFlow<String?>(null)
    val selectedStateError = _stateError.asStateFlow()


    private val _industryError = MutableStateFlow<String?>(null)
    val industryError = _industryError.asStateFlow()

    private val _plansError = MutableStateFlow<String?>(null)
    val plansError = _plansError.asStateFlow()

    private val _imageContainersError = MutableStateFlow<String?>(null)
    val imageContainersError = _imageContainersError.asStateFlow()


    // UI state
    private val _editableService = MutableStateFlow<EditableService?>(null)
    val editableService: StateFlow<EditableService?> = _editableService.asStateFlow()


    // MutableStateFlow to manage the location bottom sheet visibility state
    private val _deleteDraftDialogVisibility = MutableStateFlow(false)
    val deleteDraftDialogVisibility: StateFlow<Boolean> = _deleteDraftDialogVisibility

    private val _isPublishing = MutableStateFlow(false)
    val isPublishing = _isPublishing.asStateFlow()

    private var onCreateServiceJob: Job? = null


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


            /*     // Automatically load draft details if conditions are met
                 if (_status.value == "draft" && _draftId.value != -1L) {

                     val foundedDraftServiceWithDetails =
                         it.firstOrNull { it.draftService.id == _draftId.value }
                     foundedDraftServiceWithDetails?.let {
                         loadDraftDetails(_draftId.value, it)
                     }
                 }*/


            _draftServices.value = it.map { draftServiceDetails ->


                val draftService = draftServiceDetails.draftService
                val images = draftServiceDetails.draftImages
                val plans = draftServiceDetails.draftPlans

                EditableService(
                    draftService.id,
                    draftService.title ?: "",
                    draftService.shortDescription ?: "",
                    draftService.longDescription ?: "",
                    draftService.industry ?: -1,
                    draftService.country,
                    draftService.state,
                    "draft",
                    images.map { it.toImage() },
                    plans.map { it.toPlan() }
                )
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

        // Save values in SavedStateHandle
        savedStateHandle["status"] = status
        savedStateHandle["draftId"] = draftId

        // Load draft details if conditions are met
        if (status == "draft" && draftId != -1L) {
            loadDraftDetails(draftId)
        }
    }


    /*

        init {
            if (_status.value == "draft") {
                if (_draftId.value != -1L) {
                    loadDraftDetails(_draftId.value)
                }
            }
        }*/

    // Function to toggle or update the visibility of the location bottom sheet
    fun setDeleteDraftDialogVisibility(isVisible: Boolean) {
        _deleteDraftDialogVisibility.value = isVisible
    }

    // Optionally add functions to update fields if required
    fun updateTitle(newTitle: String) {
        _title.value = newTitle
        if (newTitle.isNotBlank()) {
            _titleError.value = null // Clear error
        }
    }


    fun updateShortDescription(newDescription: String) {
        _shortDescription.value = newDescription
        if (newDescription.isNotBlank()) {
            _shortDescriptionError.value = null // Clear error
        }
    }

    fun updateLongDescription(newDescription: String) {
        _longDescription.value = newDescription
        if (newDescription.isNotBlank()) {
            _longDescriptionError.value = null // Clear error
        }
    }

    // Optionally add functions to update fields if required
    fun updateIndustry(newIndustry: Int) {
        _industry.value = newIndustry
        if (newIndustry != -1) {
            _industryError.value = null // Clear error
        }
    }

    // Optionally add functions to update fields if required
    fun updateCountry(newCountry: String?) {
        _country.value = newCountry
        if (newCountry != null) {
            _countryError.value = null // Clear error
        }
    }


    // Optionally add functions to update fields if required
    fun updateState(newState: String?) {
        _state.value = newState
        if (newState != null) {
            _stateError.value = null // Clear error
        }
    }

    fun updateLocation(newLocation: DraftLocation) {
        _selectedLocation.value = newLocation
        _selectedLocationError.value = null // Clear error

    }


    // Function to add a new plan
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

        // Create a new list with the new plan added
        _plans.value += newPlan
        _plansError.value = null // Clear error

    }


    // Function to remove a plan
    fun removePlan(plan: EditablePlan) {
        // Create a new list excluding the removed plan
        _plans.value = _plans.value.filter { it.editablePlan.planId != plan.planId }
    }

    // Function to update a specific plan at a given index
    fun updatePlan(index: Int, updatedPlan: ValidatedPlan) {
        // Create a new list with the updated plan at the specified index
        _plans.value = _plans.value.mapIndexed { i, plan ->
            if (i == index) updatedPlan else plan
        }

    }

    // Function to update the plans
    private fun updatePlans(newPlans: List<ValidatedPlan>) {
        _plans.value = newPlans
    }


    // Function to add a new BitmapContainer
    fun addContainer(
        path: String,
        width: Int,
        height: Int,
        format: String,
        errorMessage: String? = null
    ) {
        // Create a mutable copy of the current list, add the new container, and update the state
        _imageContainers.value = _imageContainers.value.toMutableList().apply {
            add(createBitmapContainer(path, width, height, format, errorMessage))
        }
        _imageContainersError.value = null // Clear error

    }

    // Function to update a specific container
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

    // Function to remove a BitmapContainer at a specific position
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


    // Function to update a specific container
    private fun loadImageContainers(bitmapContainers: List<BitmapContainer>) {
        _imageContainers.value = bitmapContainers

    }


    private fun loadDraftDetails(
        draftId: Long,
        draftServiceWithDetails: DraftServiceWithDetails? = null
    ) {
        _isDraftLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {

            var draftServiceDetails = draftServiceWithDetails
            if (draftServiceDetails == null) {
                // Perform database query
                draftServiceDetails = draftServicesDao.getDraftServiceWithDetails(draftId)

            }


            // Extract the details
            val draftService = draftServiceDetails.draftService


            val draftImages = draftServiceDetails.draftImages
            val draftPlans = draftServiceDetails.draftPlans
            val draftLocation = draftServiceDetails.draftLocation

            val draftThumbnail = draftServiceDetails.draftThumbnail

            loadImageContainers(draftImages.map {
                val result = isValidImageDimensionsByMetaData(
                    it.width,
                    it.height,
                    it.format
                )

                val errorMessage =when {
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
                val errorMessage =when {
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


            // Create the editable service object
            val editableService = EditableService(
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

            // Update the state
            _editableService.value = editableService

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


            _isDraftLoading.value = false

        }
    }


    // Validation logic for serviceTitle
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

    // Validation logic for shortDescription
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

    // Validation logic for longDescription
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

    // Validation logic for selectedIndustry
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


    // Validation logic for plans
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

            ValidatedPlan(isValid, editablePlan) // Create a new ValidatedPlan instance
        }


        _plans.value = updatedPlans // Update all plans at once

        _plansError.value =
            if (updatedPlans.all { it.isValid }) null else "One or more plans are invalid"
        return updatedPlans.all { it.isValid }
    }


    // Validation logic for containers
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


    // Validation logic for containers
    private fun validateImageContainers(): Boolean {
        return if (_imageContainers.value.isEmpty()) {
            _imageContainersError.value = "At least one image must be added"
            false
        } else {
            _imageContainersError.value = null
            true
        }
    }

    // Validation logic for selectedLocation
    private fun validateSelectedLocation(): Boolean {
        return if (_selectedLocation.value == null) {
            _selectedLocationError.value = "Location must be selected"
            false
        } else {
            _selectedLocationError.value = null
            true
        }
    }


    // Perform full validation
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
                // Ensure the cache directory exists
                val cacheDir = File(context.cacheDir, "draft")
                if (!cacheDir.exists()) {
                    cacheDir.mkdirs()
                }

                // Process each draft image
                val cachedDraftImages = draftImages.mapNotNull { draftImage ->
                    try {
                        // Open InputStream from original file path (assuming 'path' is the original file path)
                        val inputStream =
                            context.contentResolver.openInputStream(Uri.parse(draftImage.path))
                                ?: return@mapNotNull null

                        // Create a new cache file with a unique name (based on current timestamp)
                        val cacheFile = File(
                            cacheDir,
                            "draft_images_${System.currentTimeMillis()}.jpg"
                        ) // Change extension as needed
                        val outputStream = cacheFile.outputStream()

                        // Copy the InputStream to the cache file
                        inputStream.use { input ->
                            outputStream.use { output ->
                                input.copyTo(output)
                            }
                        }

                        // Return a DraftImage object with the new cached file path
                        DraftImage(
                            serviceId = serviceId,
                            data = cacheFile.absolutePath,  // Store the cached file path
                            format = draftImage.format,
                            width = draftImage.width,
                            height = draftImage.height
                        )
                    } catch (e: Exception) {
                        // Handle any errors (e.g., file not found, I/O exception)
                        Log.e("CacheError", "Error caching image: ${draftImage.path}", e)
                        null
                    }
                }

                // Only insert into the database if there are valid cached files
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
                // Open the InputStream for the thumbnail file
                val thumbnailUri = Uri.parse(it.path)
                context.contentResolver.openInputStream(thumbnailUri)?.use { inputStream ->

                    // Create the directory for the draft images (if it doesn't exist)
                    val draftDir = File(context.cacheDir, "draft")
                    if (!draftDir.exists()) {
                        draftDir.mkdirs()
                    }

                    // Create a new file in the cache directory with the same filename
                    val outputFile = File(
                        draftDir,
                        "thumbnail_${System.currentTimeMillis()}.jpg"
                    ) // You can choose the filename


                    val outputStream = outputFile.outputStream()

                    // Copy the InputStream to the cache file
                    inputStream.use { input ->
                        outputStream.use { output ->
                            input.copyTo(output)
                        }
                    }

                    // Insert the details of the file into the database
                    draftThumbnailDao.insert(
                        DraftThumbnail(
                            serviceId = serviceId,
                            data = outputFile.absolutePath, // Path of the new file
                            format = it.format,
                            width = it.width,
                            height = it.height
                        )
                    )
                }
            }

            _draftId.value = serviceId
            _status.value = "draft"

            // Save values in SavedStateHandle
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
            draftImagesDao.deleteImagesCachePathsByServiceId(draftId).onEach {
                deleteDraftCacheFile(it)
            }
            draftPlansDao.deletePlanByServiceId(draftId)
            draftLocationDao.deleteLocationByServiceId(draftId)
            draftThumbnailDao.deleteThumbnailByServiceId(draftId)
            draftThumbnailDao.getThumbnailPathByServiceId(draftId)?.let {
                deleteDraftCacheFile(it)
            }
            draftServicesDao.update(draftService)

            if (draftImages.isNotEmpty()) {
                // Ensure the cache directory exists
                val cacheDir = File(context.cacheDir, "draft")
                if (!cacheDir.exists()) {
                    cacheDir.mkdirs()
                }

                // Process each draft image
                val cachedDraftImages = draftImages.mapNotNull { draftImage ->
                    try {

                        val inputStream = if (draftImage.path.startsWith("content://")) {
                            // Open InputStream from original file path (assuming 'path' is the original file path)
                            context.contentResolver.openInputStream(Uri.parse(draftImage.path))
                                ?: return@mapNotNull null
                        } else {
                            val file = File(draftImage.path)
                            if (file.exists()) {
                                // Return the FileInputStream if the file exists
                                FileInputStream(file)
                            } else {
                                // Return null if the file doesn't exist
                                return@mapNotNull null
                            }
                        }


                        // Create a new cache file with a unique name (based on current timestamp)
                        val cacheFile = File(
                            cacheDir,
                            "draft_images_${System.currentTimeMillis()}.jpg"
                        ) // Change extension as needed
                        // Copy the InputStream to the cache file
                        val outputStream = cacheFile.outputStream()

                        // Copy the InputStream to the cache file
                        inputStream.use { input ->
                            outputStream.use { output ->
                                input.copyTo(output)
                            }
                        }

                        // Return a DraftImage object with the new cached file path
                        DraftImage(
                            serviceId = draftId,
                            data = cacheFile.absolutePath,  // Store the cached file path
                            format = draftImage.format,
                            width = draftImage.width,
                            height = draftImage.height
                        )
                    } catch (e: Exception) {
                        // Handle any errors (e.g., file not found, I/O exception)
                        Log.e("CacheError", "Error caching image: ${draftImage.path}", e)
                        null
                    }
                }

                // Only insert into the database if there are valid cached files
                if (cachedDraftImages.isNotEmpty()) {
                    draftImagesDao.insert(cachedDraftImages)
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

                // Open the InputStream for the thumbnail file
                val thumbnailUri = Uri.parse(nonNullDraftThumbnail.path)


                val inputStream = if (nonNullDraftThumbnail.path.startsWith("content://")) {
                    // Open InputStream from original file path (assuming 'path' is the original file path)
                    context.contentResolver.openInputStream(thumbnailUri)
                } else {
                    val file = File(nonNullDraftThumbnail.path)
                    if (file.exists()) {
                        // Return the FileInputStream if the file exists
                        FileInputStream(file)
                    } else {
                        // Return null if the file doesn't exist
                        null
                    }
                }

                inputStream?.use {

                    // Create the directory for the draft images (if it doesn't exist)
                    val draftDir = File(context.cacheDir, "draft")
                    if (!draftDir.exists()) {
                        draftDir.mkdirs()
                    }

                    // Create a new file in the cache directory with the same filename
                    val outputFile = File(
                        draftDir,
                        "thumbnail_${System.currentTimeMillis()}.jpg"
                    ) // You can choose the filename
                    val outputStream = outputFile.outputStream()

                    // Copy the InputStream to the cache file
                    inputStream.use { input ->
                        outputStream.use { output ->
                            input.copyTo(output)
                        }
                    }
                    // Insert the details of the file into the database
                    draftThumbnailDao.insert(
                        DraftThumbnail(
                            serviceId = draftId,
                            data = outputFile.absolutePath, // Path of the new file
                            format = nonNullDraftThumbnail.format,
                            width = nonNullDraftThumbnail.width,
                            height = nonNullDraftThumbnail.height
                        )
                    )
                }
            }


            withContext(Dispatchers.Main) {
                onSuccess()
            }
        }
    }

    fun deleteDraft(serviceId: Long, onSuccess: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            draftServicesDao.deleteDraftById(serviceId)

            withContext(Dispatchers.Main) {
                onSuccess()
            }
        }
    }

    private fun deleteDraftCacheFile(path: String) {

        try {
            File(path).also {
                if (it.exists()) {
                    it.delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
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


    fun clearSelectedDraft() {

        onCreateServiceJob?.cancel()

        _isDraftLoading.value = false

        _draftId.value = -1
        _status.value = ""

        savedStateHandle.remove<String>("status")
        savedStateHandle.remove<Long>("draftId")

        _title.value = ""
        _shortDescription.value = ""
        _longDescription.value = ""
        _thumbnailContainer.value = null
        _imageContainers.value = emptyList()
        _industry.value=-1
        _plans.value = emptyList()
        _country.value = ""
        _state.value = ""
        _selectedLocation.value = null

        _titleError.value = null
        _shortDescriptionError.value = null
        _longDescriptionError.value = null
        _imageContainersError.value = null
        _plansError.value = null
        _industryError.value= null
        _countryError.value = null
        _stateError.value = null
        _selectedLocationError.value = null
        _editableService.value=null

    }


}
