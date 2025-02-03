package com.super6.pot.ui.services.manage.viewmodels

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.super6.pot.api.app.AppClient
import com.super6.pot.api.common.errors.ErrorResponse
import com.super6.pot.api.common.responses.ResponseReply
import com.super6.pot.api.models.service.EditableImage
import com.super6.pot.api.models.service.EditableLocation
import com.super6.pot.api.models.service.EditablePlan
import com.super6.pot.api.models.service.EditablePlanFeature
import com.super6.pot.api.models.service.EditableService
import com.super6.pot.api.models.service.Image
import com.super6.pot.api.models.service.Plan
import com.super6.pot.api.models.service.toEditableImage
import com.super6.pot.ui.services.ThumbnailContainer
import com.super6.pot.ui.services.ValidatedPlan
import com.super6.pot.ui.managers.UserSharedPreferencesManager
import com.super6.pot.utils.LogUtils.TAG
import com.super6.pot.api.Utils.Result
import com.super6.pot.api.Utils.ResultError
import com.super6.pot.api.Utils.mapExceptionToError
import com.super6.pot.api.app.ManageServicesApiService
import com.super6.pot.ui.services.manage.models.Container
import com.super6.pot.ui.services.manage.models.ContainerFactory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class PublishedServicesViewModel @Inject constructor(
    private val repository: PublishedServiceRepository,
    val savedStateHandle: SavedStateHandle,
) : ViewModel() {


    // Retrieve the argument from the navigation
    val userId: Long = UserSharedPreferencesManager.userId

    // StateFlow for managing the list of published services
    val services = repository.publishedServices

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading


    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> get() = _refreshing

    private val _resultError = MutableStateFlow<ResultError?>(null)
    val resultError = _resultError.asStateFlow()

    private var errorMessage: String = ""


    val selectedService = repository.selectedService


    // Define MutableStateFlow for service fields
    private val _serviceTitle = MutableStateFlow("")
    val serviceTitle = _serviceTitle.asStateFlow()

    private val _shortDescription = MutableStateFlow("")
    val shortDescription = _shortDescription.asStateFlow()

    private val _longDescription = MutableStateFlow("")
    val longDescription = _longDescription.asStateFlow()

    // MutableStateFlow to manage the location bottom sheet visibility state
    private val _selectedIndustry = MutableStateFlow(-1)
    val selectedIndustry = _selectedIndustry.asStateFlow()

/*
    // MutableStateFlow to manage the location bottom sheet visibility state
    private val _selectedCountry = MutableStateFlow<String?>(null)
    val selectedCountry = _selectedCountry.asStateFlow()*/


    // Define MutableStateFlow for service fields
    private val _serviceTitleError = MutableStateFlow<String?>(null)
    val serviceTitleError = _serviceTitleError.asStateFlow()

    private val _shortDescriptionError = MutableStateFlow<String?>(null)
    val shortDescriptionError = _shortDescriptionError.asStateFlow()

    private val _longDescriptionError = MutableStateFlow<String?>(null)
    val longDescriptionError = _longDescriptionError.asStateFlow()

    // MutableStateFlow to manage the location bottom sheet visibility state
    private val _selectedIndustryError = MutableStateFlow<String?>(null)
    val selectedIndustryError = _selectedIndustryError.asStateFlow()


/*
    // MutableStateFlow to manage the location bottom sheet visibility state
    private val _selectedCountryError = MutableStateFlow<String?>(null)
    val selectedCountryError = _selectedCountryError.asStateFlow()
*/


    private val _isUpdating = MutableStateFlow(false)
    val isUpdating = _isUpdating.asStateFlow()


    // MutableStateFlow to manage the location bottom sheet visibility state
    private val _editablePlans = MutableStateFlow<List<ValidatedPlan>>(emptyList())
    val editablePlans = _editablePlans.asStateFlow()

    private val _plansError = MutableStateFlow<String?>(null)
    val plansError = _plansError.asStateFlow()


    // MutableStateFlow to manage the location bottom sheet visibility state
    private val _editableLocation = MutableStateFlow<EditableLocation?>(null)
    val editableLocation = _editableLocation.asStateFlow()


    private val _selectedLocationError = MutableStateFlow<String?>(null)
    val selectedLocationError = _selectedLocationError.asStateFlow()


    // Define a MutableStateFlow for the containers list
    private val _thumbnailContainer = MutableStateFlow<ThumbnailContainer?>(null)
    val thumbnailContainer = _thumbnailContainer.asStateFlow()


    // Define StateFlow variables
    private val _refreshImageIndex = MutableStateFlow(-1)
    val refreshImageIndex = _refreshImageIndex.asStateFlow()

    private val _isPickerLaunch = MutableStateFlow(false)
    val isPickerLaunch = _isPickerLaunch.asStateFlow()

    private var _editableContainers: SnapshotStateList<Container> = mutableStateListOf()

    val editableContainers: SnapshotStateList<Container>
        get() = _editableContainers // Returns the original list


    private val containerFactory = ContainerFactory()


    init {
        _isLoading.value = true

        viewModelScope.launch {
            _resultError.value = null
            repository.getPublishedServices(userId, {
                _isLoading.value = false
            }, {
                _isLoading.value = false
                val mappedException = mapExceptionToError(it)
                _resultError.value = mappedException
                errorMessage = mappedException.errorMessage
            })
        }
    }

    fun setSelectedService(serviceId: Long) {
        repository.setSelectedItem(serviceId)
    }

   /* fun inValidateSelectedService() {
        repository.invalidateSelectedItem()
    }*/


    fun refreshPublishedServices(userId: Long) {
        _resultError.value = null
        viewModelScope.launch {
            _isLoading.value = true
            repository.getPublishedServices(userId, {
                _isLoading.value = false
            }, {
                _isLoading.value = false
                val mappedException = mapExceptionToError(it)
                _resultError.value = mappedException
                errorMessage = mapExceptionToError(it).errorMessage
            })
        }
    }


    fun removeSelectedService(serviceId: Long) {
        repository.removeSelectedService(serviceId)
    }


    fun onDeleteService(
        userId: Long,
        serviceId: Long,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
    ) {

        viewModelScope.launch {
            _isLoading.value = true
            try {
                when (val result = deleteService(userId, serviceId)) {
                    is Result.Success -> {
                        onSuccess(result.data.message)
                    }

                    is Result.Error -> {
                        errorMessage = mapExceptionToError(result.error).errorMessage
                        onError(errorMessage)
                        // Handle the error and update the UI accordingly
                    }
                }
            } catch (t: Throwable) {
                t.printStackTrace()
                errorMessage = "Something went wrong"
                onError(errorMessage)
            } finally {
                _isLoading.value = false // Reset loading state
            }

        }

    }


    private suspend fun deleteService(
        userId: Long,
        serviceId: Long,
    ): Result<ResponseReply> {


        return try {
            val response = AppClient.instance.create(ManageServicesApiService::class.java)
                .deleteService(serviceId, userId)

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
                } catch (e: Exception) {
                    "An unknown error occurred"
                }
                Result.Error(Exception(errorMessage))
            }
        } catch (t: Throwable) {
            Result.Error(t)
        }
    }


    fun loadManageServiceInfoDetails(publishedService: EditableService) {

        _serviceTitle.value = ""
        _serviceTitleError.value = null

        _shortDescription.value = ""
        _shortDescriptionError.value = null

        _longDescriptionError.value = ""
        _longDescriptionError.value = null

        _selectedIndustry.value = -1
        _selectedIndustryError.value = null

        updateServiceTitle(publishedService.title)
        updateShortDescription(publishedService.shortDescription)
        updateLongDescription(publishedService.longDescription)
        updateServiceIndustry(publishedService.industry)
/*
        updateServiceCountry(publishedService.country)
*/
    }


    // Optionally add functions to update fields if required
    fun updateServiceTitle(newTitle: String) {
        _serviceTitle.value = newTitle
    }


    fun updateShortDescription(newDescription: String) {
        _shortDescription.value = newDescription
    }

    fun updateLongDescription(newDescription: String) {
        _longDescription.value = newDescription
    }

    // Optionally add functions to update fields if required
    fun updateServiceIndustry(industry: Int) {
        _selectedIndustry.value = industry
    }

    // Optionally add functions to update fields if required
  /*  fun updateServiceCountry(country: String?) {
        _selectedCountry.value = country
    }*/


    // Validation logic for serviceTitle
    private fun validateServiceTitle(): Boolean {
        return if (_serviceTitle.value.isBlank()) {
            _serviceTitleError.value = "Service title cannot be empty"
            false
        } else if (_serviceTitle.value.length > 100) {
            _serviceTitleError.value = "Service title cannot be exceed 100 characters"
            false
        } else {
            _serviceTitleError.value = null
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
        return if (_selectedIndustry.value == -1) {
            _selectedIndustryError.value = "Industry must be selected"
            false
        } else {
            _selectedIndustryError.value = null
            true
        }
    }


/*    private fun validateSelectedCountry(): Boolean {
        return if (_selectedCountry.value == null) {
            _selectedCountryError.value = "Country must be selected"
            false
        } else {
            _selectedCountryError.value = null
            true
        }
    }*/


    // Perform full validation
    fun validateServiceInfoAll(): Boolean {

        val isServiceTitleValid = validateServiceTitle()
        val isShortDescriptionValid = validateShortDescription()
        val isLongDescriptionValid = validateLongDescription()
        val isSelectedIndustryValid = validateSelectedIndustry()
/*
        val isSelectedCountryValid = validateSelectedCountry()
*/
        return isServiceTitleValid && isShortDescriptionValid &&
                isLongDescriptionValid &&
                isSelectedIndustryValid /*&& isSelectedCountryValid*/
    }


    // Function to update service info by serviceId and add new details (title, shortDesc, longDesc)
    private fun updateServiceInfo(
        serviceId: Long,
        title: String,
        shortDescription: String,
        longDescription: String,
        industry: Int,
    ) {

        repository.updateServiceInfo(
            serviceId,
            title,
            shortDescription,
            longDescription,
            industry)

    }


    fun onUpdateServiceInfo(
        userId: Long,
        serviceId: Long,
        serviceTitle: String,
        serviceShortDescription: String,
        serviceLongDescription: String,
        serviceIndustry: Int,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch {

            try {
                _isUpdating.value = true

                when (val result = updateServiceInfo(
                    userId,
                    serviceId,
                    serviceTitle,
                    serviceShortDescription,
                    serviceLongDescription,
                    serviceIndustry,

                    )) {
                    is Result.Success -> {

                        val data = Gson().fromJson(
                            result.data.data,
                            JsonObject::class.java
                        ) as JsonObject

                        updateServiceInfo(
                            serviceId,
                            data.get("title").asString,
                            data.get("short_description").asString,
                            data.get("long_description").asString,
                            data.get("industry").asInt)


                        onSuccess(result.data.message)  // Proceed to next step or navigate to OTP screen
                    }

                    is Result.Error -> {
                        val errorMsg = mapExceptionToError(result.error).errorMessage
                        onError(errorMsg)
                    }
                }
            } catch (t: Throwable) {
                errorMessage = "Something Went Wrong"
                onError(errorMessage)
            } finally {
                _isUpdating.value = false
            }
        }


    }


    private suspend fun updateServiceInfo(
        userId: Long,
        serviceId: Long,
        serviceTitle: String,
        serviceShortDescription: String,
        serviceLongDescription: String,
        serviceIndustry: Int
        ): Result<ResponseReply> {


        return try {
            val response = AppClient.instance.create(ManageServicesApiService::class.java)
                .updateServiceInfo(
                    userId,
                    serviceId,
                    serviceTitle,
                    serviceShortDescription,
                    serviceLongDescription,
                    serviceIndustry)

            // Handle response and update the database as needed
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
                } catch (e: Exception) {
                    "An unknown error occurred"
                }
                Result.Error(Exception(errorMessage))
            }
        } catch (t: Throwable) {
            t.printStackTrace()
            Result.Error(t)
        }
    }


    fun loadManageServicePlans(publishedService: EditableService) {
        _editablePlans.value = emptyList()
        _editablePlans.value = publishedService.plans.map {
            ValidatedPlan(true, it)
        }
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
                planDeliveryTime = -1,
                planDurationUnit = ""
            )
        )

        // Create a new list with the new plan added
        _editablePlans.value += newPlan
        _plansError.value = null // Clear error

    }

    // Function to update a specific plan at a given index
    fun updatePlan(index: Int, updatedPlan: ValidatedPlan) {
        // Create a new list with the updated plan at the specified index
        _editablePlans.value = _editablePlans.value.mapIndexed { i, plan ->
            if (i == index) updatedPlan else plan
        }

    }

    // Function to remove a plan
    fun removePlan(plan: EditablePlan) {
        // Create a new list excluding the removed plan
        _editablePlans.value = _editablePlans.value.filter { it.editablePlan.planId != plan.planId }
    }


    // Validation logic for plans
    fun validatePlans(): Boolean {
        if (_editablePlans.value.isEmpty()) {
            _plansError.value = "At least one plan must be added"
            return false
        }

        val updatedPlans = _editablePlans.value.map { validatedPlan ->
            val editablePlan = validatedPlan.editablePlan
            var isValid = true

            // Perform validation
            if (editablePlan.planName.trim().isEmpty() || editablePlan.planName.length > 20) isValid =
                false
            if (editablePlan.planDescription.trim().isEmpty() || editablePlan.planDescription.length > 200) isValid =
                false
            if (editablePlan.planDurationUnit.isEmpty()) isValid = false
            if (editablePlan.planDeliveryTime == -1) isValid = false
            if (editablePlan.planPrice == BigDecimal.ZERO) isValid = false
            if (editablePlan.planPriceUnit.isEmpty()) isValid = false
            if (editablePlan.planFeatures.isEmpty()) isValid = false
            editablePlan.planFeatures.forEach { feature ->
                if (feature.featureName.trim().isEmpty() || feature.featureName.trim().length > 40 || feature.featureValue.toString().length > 10 || feature.featureValue == null) isValid =
                    false
            }
            ValidatedPlan(isValid, editablePlan) // Create a new ValidatedPlan instance
        }

        _editablePlans.value = updatedPlans // Update all plans at once
        _plansError.value = if (updatedPlans.all { it.isValid }) null else "One or more plans are invalid"

        return updatedPlans.all { it.isValid }
    }


    private fun updatePlansInfo(
        serviceId: Long,
        plans: List<Plan>,
    ) {
        repository.updatePlansInfo(serviceId, plans)
    }


    fun onUpdateServicePlans(
        userId: Long,
        serviceId: Long,
        plans: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
    ) {

        viewModelScope.launch {
            try {
                _isUpdating.value = true
                when (val result = updateServicePlans(
                    userId,
                    serviceId,
                    plans
                )) { // Call the network function
                    is Result.Success -> {
                        val updatedData = Gson().fromJson(result.data.data, object :
                            TypeToken<List<Plan>>() {}.type) as List<Plan>
                        updatePlansInfo(serviceId, updatedData)
                        onSuccess(result.data.message)
                        // Handle success
                        // Proceed to next step or navigate to OTP screen
                    }

                    is Result.Error -> {
                        errorMessage = mapExceptionToError(result.error).errorMessage
                        onError(errorMessage)
                        // Handle the error and update the UI accordingly
                    }

                }
            } catch (t: Throwable) {
                t.printStackTrace()
                errorMessage = "Something Went Wrong"
                onError(errorMessage)
            } finally {
                _isUpdating.value = false
            }
        }
    }


    private suspend fun updateServicePlans(
        userId: Long,
        serviceId: Long,
        plans: String,
    ): Result<ResponseReply> {


        return try {
            // Assume RetrofitClient has a suspend function for updateFirstName
            val response = AppClient.instance.create(ManageServicesApiService::class.java)
                .updatePlans(serviceId, userId, plans)

            // Handle response and update the database as needed
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
                } catch (e: Exception) {
                    "An unknown error occurred"
                }
                Result.Error(Exception(errorMessage))
            }
        } catch (t: Throwable) {
            Result.Error(t)
        }


    }


    fun loadManageLocationDetails(publishedService: EditableService) {
        _editableLocation.value = null
        _editableLocation.value = publishedService.location
    }

    // Validation logic for selectedLocation
    fun validateSelectedLocation(): Boolean {
        return if (_editableLocation.value == null) {
            _selectedLocationError.value = "Location must be selected"
            false
        } else {
            _selectedLocationError.value = null
            true
        }
    }


    fun updateLocation(newLocation: EditableLocation) {
        _editableLocation.value = newLocation
//        _selectedLocationError.value = null // Clear error

    }


    private fun updateLocationInfo(
        serviceId: Long,
        latitude: Double,
        longitude: Double,
        geo: String,
        locationType: String,
    ) {


        repository.updateLocationInfo(serviceId, latitude, longitude, geo, locationType)

    }


    fun onUpdateServiceLocation(
        userId: Long,
        serviceId: Long,
        editableLocation: EditableLocation,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
    ) {

        viewModelScope.launch {
            try {
                _isUpdating.value = true
                when (val result = updateServiceLocation(
                    userId,
                    serviceId,
                    editableLocation
                )) { // Call the network function
                    is Result.Success -> {

                        val data = Gson().fromJson(
                            result.data.data,
                            JsonObject::class.java
                        ) as JsonObject
                        val latitude = data.get("latitude").asDouble
                        val longitude = data.get("longitude").asDouble
                        val geo = data.get("geo").asString
                        val locationType = data.get("location_type").asString

                        updateLocationInfo(serviceId, latitude, longitude, geo, locationType)
                        onSuccess(result.data.message)
                        // Handle success
                        // Proceed to next step or navigate to OTP screen
                    }

                    is Result.Error -> {

                        errorMessage = mapExceptionToError(result.error).errorMessage
                        onError(errorMessage)
                        // Handle the error and update the UI accordingly
                    }

                }
            } catch (t: Throwable) {
                errorMessage = "Something Went Wrong"
                onError(errorMessage)
            } finally {
                _isUpdating.value = false // Reset loading state
            }
        }

    }


    private suspend fun updateServiceLocation(
        userId: Long,
        serviceId: Long,
        editableLocation: EditableLocation,
    ): Result<ResponseReply> {


        return try {
            val response = AppClient.instance.create(ManageServicesApiService::class.java)
                .updateServiceLocation(
                    userId,
                    serviceId,
                    editableLocation.latitude,
                    editableLocation.longitude,
                    editableLocation.geo,
                    editableLocation.locationType
                )

            // Handle response and update the database as needed
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
                } catch (e: Exception) {
                    "An unknown error occurred"
                }
                Result.Error(Exception(errorMessage))
            }
        } catch (t: Throwable) {
            Result.Error(t)
        }


    }


    fun updateThumbnailContainer(
        path:String,
        width: Int,
        height:Int,
        format: String,
        errorMessage: String? = null,
    ) {
        val currentContainer = _thumbnailContainer.value
        if (currentContainer != null) {
            _thumbnailContainer.value = currentContainer.copy(
                path = path,
                width = width,
                height = height,
                format = format,
                errorMessage = errorMessage
            )
        } else {

            _thumbnailContainer.value = ThumbnailContainer(
                containerId = "THUMBNAIL_CONTAINER",
                path = path,
                width = width,
                height = height,
                format = format,
                errorMessage = errorMessage)
        }
    }


    fun validateThumbnailContainer(): Boolean {
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

    fun onUpdateServiceThumbnail(
        userId: RequestBody,
        serviceId: Long,
        thumbnail: MultipartBody.Part,
        thumbnailId: RequestBody,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch {

            try {
                _isUpdating.value = true

                when (val result = updateThumbnail(
                    userId,
                    serviceId,
                    thumbnail,
                    thumbnailId
                )) {
                    is Result.Success -> {

                        val data = Gson().fromJson(
                            result.data.data,
                            JsonObject::class.java
                        ) as JsonObject

                        repository.updateServiceThumbnail(
                            serviceId,
                            data.get("image_id").asInt,
                            data.get("image_url").asString,
                            data.get("width").asInt,
                            data.get("height").asInt,
                            data.get("format").asString,
                            data.get("size").asInt
                        )

                        onSuccess(result.data.message)  // Proceed to next step or navigate to OTP screen
                    }

                    is Result.Error -> {
                        val errorMsg = mapExceptionToError(result.error).errorMessage
                        onError(errorMsg)
                    }
                }
            } catch (t: Throwable) {
                errorMessage = "Something Went Wrong"
                onError(errorMessage)
            } finally {
                _isUpdating.value = false
            }
        }


    }


    private suspend fun updateThumbnail(
        userId: RequestBody,
        serviceId: Long,
        thumbnail: MultipartBody.Part,
        thumbnailId: RequestBody,

        ): Result<ResponseReply> {


        return try {
            val response = AppClient.instance.create(ManageServicesApiService::class.java)
                .updateThumbnail(
                    serviceId,
                    userId,
                    thumbnailId,
                    thumbnail
                )

            // Handle response and update the database as needed
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
                } catch (e: Exception) {
                    "An unknown error occurred"
                }
                Result.Error(Exception(errorMessage))
            }
        } catch (t: Throwable) {
            t.printStackTrace()
            Result.Error(t)
        }

    }


    // Example function to update state
    fun setRefreshImageIndex(index: Int) {
        viewModelScope.launch {
            _refreshImageIndex.value=index
        }
    }

    fun togglePickerLaunch() {
        viewModelScope.launch {
            _isPickerLaunch.emit(!_isPickerLaunch.value)
        }
    }

    fun loadManageImages(publishedService: EditableService) {

        _editableContainers.clear()
        _editableContainers.addAll(
            publishedService.images.map {
                containerFactory.createContainer(it)
            }
        )
    }


    fun onImageValidationFailedUpdate(containerId: String,previewImageUri: Uri?=null, errorMessage: String?=null) {
        val index = _editableContainers.indexOfFirst { it.containerId == containerId }

        // Check if index is valid
        if (index != -1) {
            // Update the container safely
            _editableContainers = _editableContainers.apply {
                this[index] = this[index].copy(
                    error = errorMessage,
                    isLoading = false,
                    previewUri = previewImageUri ?: this[index].previewUri
                    )
            }
        }
    }


    fun setImageLoadingState(containerId: String) {
        // Find the index of the container based on containerId
        val originalIndex = _editableContainers.indexOfFirst { it.containerId == containerId }

        // Check if the index is valid
        if (originalIndex != -1) {
            // Update the loading state of the specific container
            _editableContainers[originalIndex] = _editableContainers[originalIndex].copy(
                isLoading = true,
                error = null
            )
        }
    }


    fun updateImageContainerOngoingRequest(containerId: String, ongoingRequestJob: Job?, previewImageUri: Uri?) {
        // Find the index of the container based on containerId
        val index = _editableContainers.indexOfFirst { it.containerId == containerId }

        // Check if the index is valid
        if (index != -1) {
            // Update the ongoingRequest property of the specific container
            _editableContainers[index] = _editableContainers[index].copy(
                isLoading = true,
                ongoingRequest = ongoingRequestJob,
                error = null,
                previewUri = previewImageUri ?: _editableContainers[index].previewUri
            )
        }

    }


    fun createLoadingItem(previewUri:Uri): Container = containerFactory.createContainer(null, true,previewUri = previewUri)


    fun addLoadingItem(container: Container) {
        _editableContainers.add(container)
    }


    fun updateContainerImage(
        containerId: String,
        imageId: Int,
        imageUrl: String?,
        width: Int,
        height: Int,
        size: Int,
        format: String,
    ) {
        // Find the index of the container based on containerId
        val index = _editableContainers.indexOfFirst { it.containerId == containerId }

        // Check if the index is valid
        if (index != -1) {

            Log.e(TAG, "Container added")

            // Update the specific container with a new EditableImage
            _editableContainers[index] = _editableContainers[index].copy(
                image = EditableImage(
                    imageId = imageId,
                    imageUrl = imageUrl,
                    width = width,
                    height = height,
                    size = size,
                    format = format
                ),
                isLoading = false // Set isLoading to false
            )

        }
    }


    fun updateContainerState(
        containerId: String,
        message: String?,
    ) {
        // Find the index of the container based on containerId
        val index = _editableContainers.indexOfFirst { it.containerId == containerId }

        // Check if the index is valid
        if (index != -1) {
            // Update the specific container's properties
            _editableContainers[index] = _editableContainers[index].copy(
                error = message,
                isLoading = false,
                ongoingRequest = null
            )

        }
    }


    // Function to update a container's isRemoving state by its ID
    fun updateContainerIsRemoving(containerId: String) {
        // Find the index of the container to update
        val selectedItemIndex = _editableContainers.indexOfFirst {
            it.containerId == containerId
        }

        // Update the container if found
        if (selectedItemIndex != -1) {
            _editableContainers[selectedItemIndex] = _editableContainers[selectedItemIndex].copy(
                isRemoving = true
            )
        }
    }


    // Function to update and remove a container
    fun updateAndRemoveContainer(containerId: String) {
        // Find the index of the container to update
        val selectedItemIndex = _editableContainers.indexOfFirst {
            it.containerId == containerId
        }
        // Proceed only if the container is found
        if (selectedItemIndex != -1) {
            // Log the imageId if it exists
            // Remove the container from the list
            _editableContainers.removeAt(selectedItemIndex)
        }
    }


    fun handleFailureAndUpdateContainer(containerId: String, error: String?) {


        // Find the index of the container
        val selectedItemIndex = _editableContainers.indexOfFirst {
            it.containerId == containerId
        }

        // Check if the container exists in the list
        if (selectedItemIndex != -1) {
            // Update the container at the found index
            _editableContainers[selectedItemIndex] = _editableContainers[selectedItemIndex].copy(
                error = error,
                isLoading = false,
                isRemoving = false
            )
        }
    }


    fun updateOrAddImage(
        serviceId: Long,
        imageId: Int,
        imageUrl: String?,
        width: Int,
        height: Int,
        size: Int,
        format: String,
    ) {

        repository.updateOrAddImage(serviceId, imageId, imageUrl, width, height, size, format)
    }


    fun removeImageFromSelectedService(serviceId: Long, imageId: Int) {
        repository.removeImageFromSelectedService(serviceId, imageId)
    }


    fun onDeleteImage(
        userId: Long,
        serviceId: Long,
        imageId: Int,
        onSuccess: (String) -> Unit,
        onError: (String?) -> Unit,
    ) {
        viewModelScope.launch {
            try {
                when (val result = deleteImage(userId, serviceId, imageId)) {

                    is Result.Success -> {
                        onSuccess(result.data.message)
                    }

                    is Result.Error -> {
                        errorMessage = mapExceptionToError(result.error).errorMessage
                        onError(errorMessage)
                    }

                }
            } catch (t: Throwable) {
                onError(errorMessage)
                errorMessage = "Something went wrong"
            }
        }
    }


    private suspend fun deleteImage(
        userId: Long,
        serviceId: Long,
        imageId: Int,
    ): Result<ResponseReply> {


        return try {
            val response = AppClient.instance.create(ManageServicesApiService::class.java)
                .deleteServiceImage(serviceId, userId, imageId)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.isSuccessful) {
                    Result.Success(body)
                } else {
                    val errorMessage = "Failed"
                    Result.Error(Exception(errorMessage))

                }
            } else {

                val errorBody = response.errorBody()?.string()
                val errorMessage = try {
                    Gson().fromJson(errorBody, ErrorResponse::class.java).message
                } catch (e: Exception) {
                    "An unknown error occurred"
                }
                Result.Error(Exception(errorMessage))

            }


        } catch (t: Throwable) {
            t.printStackTrace()
            Result.Error(t)
        }
    }


    fun onUploadImage(
        userId: Long,
        serviceId: Long,
        imageId: Int,
        imagePart: MultipartBody.Part,
        initiate: (Job?) -> Unit,
        onSuccess: (EditableImage) -> Unit,
        onError: (String?) -> Unit,
    ) {

        viewModelScope.launch {


            // Assuming `selectedIndex` is defined and valid
            val userIdRequestBody = userId.toString().toRequestBody("text/plain".toMediaType())
            val imageIdRequestBody = imageId.toString().toRequestBody("text/plain".toMediaType())

            try {
                initiate(coroutineContext[Job]!!)

                when (val result =
                    uploadImage(userIdRequestBody, serviceId, imageIdRequestBody, imagePart)) {
                    is Result.Success -> {
                        onSuccess(Gson().fromJson(result.data.data, Image::class.java)
                            .toEditableImage())
                    }

                    is Result.Error -> {
                        errorMessage = mapExceptionToError(result.error).errorMessage
                        onError(errorMessage)
                    }

                }
            } catch (t: Throwable) {
                errorMessage = "Something went wrong"
                onError(errorMessage)
            }
        }

    }


    fun onUpdateImage(
        userId: Long,
        serviceId: Long,
        imageId: Int,
        imagePart: MultipartBody.Part,
        initiate: (Job?) -> Unit,
        onSuccess: (EditableImage) -> Unit,
        onError: (String?) -> Unit,
    ) {

        viewModelScope.launch {


            // Assuming `selectedIndex` is defined and valid
            val userIdRequestBody = userId.toString().toRequestBody("text/plain".toMediaType())
            val imageIdRequestBody = imageId.toString().toRequestBody("text/plain".toMediaType())

            try {
                initiate(coroutineContext[Job]!!)

                when (val result =
                    updateImage(userIdRequestBody, serviceId, imageIdRequestBody, imagePart)) {
                    is Result.Success -> {
                        val resultData = Gson().fromJson(result.data.data, Image::class.java)
                            .toEditableImage()


                        onSuccess(resultData)
                    }

                    is Result.Error -> {
                        errorMessage = mapExceptionToError(result.error).errorMessage
                        onError(errorMessage)
                    }

                }
            } catch (t: Throwable) {
                errorMessage = "Something went wrong"
                onError(errorMessage)
            }
        }

    }


    // Define a function to make the request
    private suspend fun uploadImage(
        userIdRequestBody: RequestBody,
        serviceId: Long,
        imageIdRequestBody: RequestBody,
        imagePart: MultipartBody.Part,
    ): Result<ResponseReply> {
        // Use withContext to switch to the IO dispatcher for network operations


        return try {

            // Make the network request
            val response = AppClient.instance.create(ManageServicesApiService::class.java)
                .uploadImage(serviceId, userIdRequestBody, imageIdRequestBody, imagePart)


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
                } catch (e: Exception) {
                    "An unknown error occurred"
                }
                Result.Error(Exception(errorMessage))
            }
        } catch (t: Throwable) {
            t.printStackTrace()
            Result.Error(t)
        }

    }


    // Define a function to make the request
    private suspend fun updateImage(
        userIdRequestBody: RequestBody,
        serviceId: Long,
        imageIdRequestBody: RequestBody,
        imagePart: MultipartBody.Part,
    ): Result<ResponseReply> {
        // Use withContext to switch to the IO dispatcher for network operations


        return try {

            // Make the network request
            val response = AppClient.instance.create(ManageServicesApiService::class.java)
                .updateImage(serviceId, userIdRequestBody, imageIdRequestBody, imagePart)


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
                } catch (e: Exception) {
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
