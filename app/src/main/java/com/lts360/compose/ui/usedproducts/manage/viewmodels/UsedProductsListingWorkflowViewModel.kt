package com.lts360.compose.ui.usedproducts.manage.viewmodels


import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.lts360.api.utils.Result
import com.lts360.api.utils.mapExceptionToError
import com.lts360.api.app.AppClient
import com.lts360.api.app.ManageUsedProductListingService
import com.lts360.api.common.errors.ErrorResponse
import com.lts360.api.common.responses.ResponseReply
import com.lts360.api.models.service.EditableLocation
import com.lts360.compose.ui.chat.MAX_IMAGES
import com.lts360.compose.ui.services.BitmapContainer
import com.lts360.compose.ui.services.BitmapContainerFactory
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
import org.koin.android.annotation.KoinViewModel
import java.util.Currency
import java.util.Locale

@KoinViewModel
class UsedProductsListingWorkflowViewModel(
    val applicationContext: Context,
    val savedStateHandle: SavedStateHandle,
) : ViewModel() {



    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()


    private val bitmapContainerFactory = BitmapContainerFactory()


    // Define MutableStateFlow for service fields
    private val _title = MutableStateFlow("")
    val title = _title.asStateFlow()

    private val _shortDescription = MutableStateFlow("")
    val shortDescription = _shortDescription.asStateFlow()

    private val _selectedLocation = MutableStateFlow<EditableLocation?>(null)
    val selectedLocation = _selectedLocation.asStateFlow()

    // MutableStateFlow to manage the location bottom sheet visibility state
    private val _country = MutableStateFlow<String?>(null)
    val selectedCountry = _country.asStateFlow()

    // MutableStateFlow to manage the location bottom sheet visibility state
    private val _state = MutableStateFlow<String?>(null)
    val selectedState = _state.asStateFlow()


    // MutableStateFlow to manage the location bottom sheet visibility state
    private val _price = MutableStateFlow<String>("")
    val price = _price.asStateFlow()

    val priceUnits = listOf("INR", "USD")

    // Get user's default currency from device locale
    private val userCurrency = Currency.getInstance(Locale.getDefault()).currencyCode

    private val _priceUnit= MutableStateFlow<String>(if(userCurrency in priceUnits) userCurrency else "INR")
    val priceUnit = _priceUnit.asStateFlow()

    // Define a MutableStateFlow for the containers list
    private val _imageContainers = MutableStateFlow<List<BitmapContainer>>(emptyList())
    val imageContainers = _imageContainers.asStateFlow()


    // Error messages
    private val _titleError = MutableStateFlow<String?>(null)
    val titleError = _titleError.asStateFlow()

    private val _shortDescriptionError = MutableStateFlow<String?>(null)
    val shortDescriptionError = _shortDescriptionError.asStateFlow()


    private val _priceError = MutableStateFlow<String?>(null)
    val priceError = _priceError.asStateFlow()


    private val _priceUnitError = MutableStateFlow<String?>(null)
    val priceUnitError = _priceUnitError.asStateFlow()

    private val _selectedLocationError = MutableStateFlow<String?>(null)
    val selectedLocationError = _selectedLocationError.asStateFlow()

    // MutableStateFlow to manage the location bottom sheet visibility state
    private val _countryError = MutableStateFlow<String?>(null)
    val selectedCountryError = _countryError.asStateFlow()

    // MutableStateFlow to manage the location bottom sheet visibility state
    private val _stateError = MutableStateFlow<String?>(null)
    val selectedStateError = _stateError.asStateFlow()


    private val _imageContainersError = MutableStateFlow<String?>(null)
    val imageContainersError = _imageContainersError.asStateFlow()


    private val _isPublishing = MutableStateFlow(false)
    val isPublishing = _isPublishing.asStateFlow()

    private val _isPickerLaunch = MutableStateFlow(false)
    val isPickerLaunch = _isPickerLaunch.asStateFlow()

    private val _refreshImageIndex = MutableStateFlow(-1)
    val refreshImageIndex = _refreshImageIndex.asStateFlow()

    val slots: StateFlow<Int> = _imageContainers
        .map { MAX_IMAGES - it.size }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            initialValue = MAX_IMAGES
        )


    private var onCreateServiceJob: Job? = null



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


    // Optionally add functions to update fields if required
    fun updateCountry(newCountry: String?) {
        _country.value = newCountry
        if (newCountry != null) {
            _countryError.value = null // Clear error
        }
    }


    fun updatePrice(price: String?) {
        when {
            price.isNullOrBlank() -> {
                _price.value = "" // Clear the price if empty
                _priceError.value = null // Clear error
            }
            price.toDoubleOrNull() != null -> {
                _price.value = price
                _priceError.value = null // Clear error
            }
            else -> {
                _priceError.value = "Invalid price" // Optional: Set an error for invalid input
            }
        }
    }



    // Optionally add functions to update fields if required
    fun updatePriceUnit(priceUnit: String) {
        _priceUnit.value = priceUnit
        _priceUnitError.value = null // Clear error

    }


    // Optionally add functions to update fields if required
    fun updateState(newState: String?) {
        _state.value = newState
        if (newState != null) {
            _stateError.value = null // Clear error
        }
    }

    fun updateLocation(newLocation: EditableLocation) {
        _selectedLocation.value = newLocation
        _selectedLocationError.value = null // Clear error

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


    fun setPickerLaunch(isLaunched: Boolean) {
        _isPickerLaunch.value = isLaunched
    }

    fun updateRefreshImageIndex(index: Int) {
        _refreshImageIndex.value = index
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
        }  else {
            _selectedLocationError.value = null
            true
        }
    }

    private fun validatePrice(): Boolean {
        return if (_price.value.isEmpty()) {
            _priceError.value = "Price must be selected"
            false
        } else if(_price.value.toDoubleOrNull()==null){
            _priceError.value = "Price must be 0.00 format"
            false
        }else {
            _priceError.value = null
            true
        }
    }

    private fun validatePriceUnit(): Boolean {
        return if (_priceUnit.value.isEmpty()) {
            _priceUnitError.value = "Price unit must be selected"
            false
        } else if(!priceUnits.contains(_priceUnit.value)) {
            _priceUnitError.value="Invalid unit selected"
            false
        }else {
            _priceUnitError.value = null
            true
        }
    }

    // Perform full validation
    fun validateAll(): Boolean {

        val isServiceTitleValid = validateServiceTitle()
        val isShortDescriptionValid = validateShortDescription()
        val isSelectedLocationValid = validateSelectedLocation()
        val isSelectedCountry = validateSelectedCountry()
        val isSelectedState = validateSelectedState()

        val isPriceValid = validatePrice()
        val isPriceUnitValid = validatePriceUnit()
        val isContainersValid = validateImageContainers()

        return  isServiceTitleValid && isShortDescriptionValid &&
                isSelectedLocationValid &&
                isPriceValid &&
                isPriceUnitValid &&
                 isSelectedCountry
                && isSelectedState
              && isContainersValid
    }




    fun onCreateUsedProductListing(
        productId:RequestBody,
        title: RequestBody,
        shortDescription: RequestBody,
        price: RequestBody,
        priceUnit:RequestBody,
        state: RequestBody,
        country: RequestBody,
        images: List<MultipartBody.Part>,
        location: RequestBody?,
        keepImageIds:List<RequestBody>,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
    ) {
        onCreateServiceJob = viewModelScope.launch {
            _isPublishing.value = true

            when (val result = createUsedProductListing(
                productId,
                title,
                shortDescription,
                price,
                priceUnit,
                state,
                country,
                images,
                location,
                keepImageIds
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

    private suspend fun createUsedProductListing(
        productId:RequestBody,
        title: RequestBody,
        shortDescription: RequestBody,
        price: RequestBody,
        priceUnit:RequestBody,
        state: RequestBody,
        country: RequestBody,
        images: List<MultipartBody.Part>,
        location: RequestBody? = null,
        keepImageIds: List<RequestBody>,

        ): Result<ResponseReply> {

        return try {
            val response = AppClient.instance.create(ManageUsedProductListingService::class.java)
                .createOrUpdateUsedProductListing(
                    productId,
                    title,
                    shortDescription,
                    price,
                    priceUnit,
                    country,
                    state,
                    images,
                    keepImageIds,
                    location,
                )

            if (response.isSuccessful) {
                // Handle successful response
                val responseBody = response.body()

                if (responseBody != null && responseBody.isSuccessful) {

                    Result.Success(responseBody)

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


        _title.value = ""
        _shortDescription.value = ""
        _price.value=""
        _priceUnit.value=if(userCurrency in priceUnits) userCurrency else "INR"
        _imageContainers.value = emptyList()
        _country.value = ""
        _state.value = ""
        _selectedLocation.value = null

        _titleError.value = null
        _shortDescriptionError.value = null
        _priceError.value=null
        _priceUnitError.value=null
        _imageContainersError.value = null
        _countryError.value = null
        _stateError.value = null
        _selectedLocationError.value = null

    }


}
