package com.super6.pot.ui.services.manage.viewmodels

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.super6.pot.api.app.AppClient
import com.super6.pot.api.common.errors.ErrorResponse
import com.super6.pot.api.common.responses.ResponseReply
import com.super6.pot.api.models.service.EditableImage
import com.super6.pot.api.models.service.EditableLocation
import com.super6.pot.api.models.service.EditableService
import com.super6.pot.api.models.service.Plan
import com.super6.pot.api.models.service.Service
import com.super6.pot.api.models.service.toEditablePlan
import com.super6.pot.api.models.service.toEditableService
import com.super6.pot.api.Utils.Result
import com.super6.pot.api.app.ManageServicesApiService
import com.super6.pot.utils.LogUtils.TAG
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject


class PublishedServiceRepository @Inject constructor() {


    // StateFlow for managing the list of published services
    private val _publishedServices = MutableStateFlow<List<EditableService>>(emptyList())
    val publishedServices: StateFlow<List<EditableService>> get() = _publishedServices


    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading


    private val _selectedService = MutableStateFlow<EditableService?>(null)
    val selectedService: StateFlow<EditableService?> = _selectedService


    // Function to update the list of published services
    private fun updatePublishedServices(newServices: List<EditableService>) {
        _publishedServices.value = newServices
    }



    fun setPublishedServices(items: List<EditableService>){
        _publishedServices.value=items
    }

    fun setSelectedItem(serviceId: Long) {

        val index = _publishedServices.value.indexOfFirst {
            it.serviceId == serviceId
        }

        if (index != -1) {
            _selectedService.value = _publishedServices.value[index]
        }
    }

    fun removeSelectedService(serviceId: Long) {
        val index = _publishedServices.value.indexOfFirst {
            it.serviceId == serviceId
        }

        if (index != -1) {
            _publishedServices.value = _publishedServices.value.filter {
                it.serviceId != serviceId
            }
        }
    }

    fun invalidateSelectedItem() {
       _selectedService.value=null
    }

    suspend fun getPublishedServices(
        userId: Long,
        onSuccess: (items:List<EditableService>) -> Unit,
        onError: (Throwable) -> Unit,
    ) {

        try {
            when (val result = fetchPublishedServices(userId)) { // Call the network function
                is Result.Success -> {

                    val data =  Gson().fromJson(result.data.data, object : TypeToken<List<Service>>() {}.type)
                            as List<Service>
                    val services = data.map { it.toEditableService() }

                    updatePublishedServices(services)
                    onSuccess(services)
                }

                is Result.Error -> {
                    if(_publishedServices.value.isNotEmpty()){
                        _publishedServices.value= emptyList()
                    }
                    onError(result.error)
                }
            }
        }catch (t:Throwable){
            if(_publishedServices.value.isNotEmpty()){
                _publishedServices.value= emptyList()
            }
            t.printStackTrace()
            onError(Exception("Something Went Wrong"))
        }


    }

    // Function to fetch services from the API
    private suspend fun fetchPublishedServices(userId: Long): Result<ResponseReply> {


        return try {
            val response = AppClient.instance.create(ManageServicesApiService::class.java)
                .getServicesByUserId(userId)

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








    // Function to update service info by serviceId and add new details (title, shortDesc, longDesc)
    fun updateServiceInfo(
        serviceId: Long,
        title: String,
        shortDescription: String,
        longDescription: String,
        industry: Int,) {

        _publishedServices.update { currentServices ->
            // Create a new list with the updated service
            currentServices.map { service ->
                if (service.serviceId == serviceId) {
                    // Update the service with the provided details
                    val updated = service.copy(
                        title = title,
                        shortDescription = shortDescription ,
                        longDescription = longDescription,
                        industry = industry,
                        country=null
                    )
                    updated  // Replace the service with the updated version
                } else {
                    service  // Keep the existing service unchanged
                }
            }
        }

        _selectedService.value= _selectedService.value!!.copy(
            title = title,
            shortDescription = shortDescription,
            longDescription = longDescription,
            industry = industry,
            country=null)

    }



    fun updateServiceThumbnail(
        serviceId: Long,
        imageId: Int,
        imageUrl: String,
        imageWidth: Int,
        imageHeight: Int,
        format: String,
        size:Int
    ) {

        _publishedServices.update { currentServices ->
            // Create a new list with the updated service
            currentServices.map { service ->
                if (service.serviceId == serviceId) {
                    // Update the service with the provided details
                    val updated = service.copy(

                        thumbnail = service.thumbnail?.copy(
                            imageId=imageId,
                            imageUrl = imageUrl,
                            width = imageWidth,
                            height = imageHeight,
                            format = format,
                            size= size

                        )
                    )
                    updated  // Replace the service with the updated version
                } else {
                    service  // Keep the existing service unchanged
                }
            }
        }

        _selectedService.value= _selectedService.value!!.copy(
            thumbnail = _selectedService.value!!.thumbnail?.copy(
                imageId = imageId,
                imageUrl = imageUrl,
                width = imageWidth,
                height = imageHeight,
                format = format,
                size= size
            ))

    }


    fun updateOrAddImage(
        serviceId: Long,
        imageId: Int,
        imageUrl: String?,
        width: Int,
        height: Int,
        size: Int,
        format: String
    ) {

        Log.e(TAG,"${imageId}")
        // Create the new EditableImage
        val newImage = EditableImage(
            imageId = imageId,
            imageUrl = imageUrl,
            width = width,
            height = height,
            size = size,
            format = format
        )

        // Update the published services with the new or updated image
        _publishedServices.update { currentServices ->
            currentServices.map { service ->
                if (service.serviceId == serviceId) {
                    val updatedImages = service.images.toMutableList().apply {
                        val index = indexOfFirst { it.imageId == imageId }
                        if (index != -1) {
                            // Update existing image
                            this[index] = newImage
                        } else {
                            Log.e(TAG,"Image added")

                            // Add new image
                            add(newImage)
                        }
                    }

                    service.copy(images = updatedImages) // Replace the images list
                } else {
                    service // Keep the existing service unchanged
                }
            }
        }

        // Update or add the Image in the mutable list for the selected service
        _selectedService.value?.let { service ->
            val updatedImages = service.images.toMutableList().apply {
                val index = indexOfFirst { it.imageId == imageId }
                if (index != -1) {
                    // Update existing image
                    this[index] = newImage
                } else {
                    Log.e(TAG,"Image added 1")
                    // Add new image
                    add(newImage)
                }
            }

            // Update the list in editableService and persist the changes
            _selectedService.value = service.copy(images = updatedImages)
        }
    }




    fun removeImageFromSelectedService(serviceId:Long,imageId: Int) {


        // Update the published services to remove the image from the specified service
        _publishedServices.update { currentServices ->
            currentServices.map { service ->
                if (service.serviceId == serviceId) {
                    // Remove the specified image from the service
                    val updatedImages = service.images.filter { it.imageId != imageId }
                    service.copy(images = updatedImages) // Replace the images list
                } else {
                    service // Keep the existing service unchanged
                }
            }
        }

        // Safely access _selectedService.value
        _selectedService.value?.let { currentService ->


            // Create a new copy of the current service, with updated images
            val updatedService = currentService.copy(
                images = currentService.images.filter { it.imageId != imageId }
            )


            // Update _selectedService.value with the new updated service
            _selectedService.value = updatedService
        }
    }



   fun updateLocationInfo(
        serviceId: Long,
        latitude: Double,
        longitude: Double,
        geo: String,
        locationType: String
    ) {

        _publishedServices.update { currentServices ->
            // Create a new list with the updated service
            currentServices.map { service ->
                if (service.serviceId == serviceId) {
                    // Update the service with the provided details
                    val updated = service.copy(
                        location = EditableLocation(
                            serviceId=serviceId,
                            latitude = latitude,
                            longitude = longitude,
                            geo = geo,
                            locationType = locationType
                        ))
                    updated  // Replace the service with the updated version
                } else {
                    service  // Keep the existing service unchanged
                }
            }
        }

        _selectedService.value= _selectedService.value!!.copy(
            location = EditableLocation(
                serviceId=serviceId,
                latitude = latitude,
                longitude = longitude,
                geo = geo,
                locationType = locationType
            ))

    }




    fun updatePlansInfo(
        serviceId: Long,
        plans:List<Plan>
    ) {

        _publishedServices.update { currentServices ->
            // Create a new list with the updated service
            currentServices.map { service ->
                if (service.serviceId == serviceId) {
                    // Update the service with the provided details
                    val updated = service.copy(plans=plans.map { it.toEditablePlan() })
                    updated  // Replace the service with the updated version
                } else {
                    service  // Keep the existing service unchanged
                }
            }
        }

        _selectedService.value= _selectedService.value!!.copy(
            plans=plans.map { it.toEditablePlan() })

    }


}



