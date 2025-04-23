package com.lts360.compose.ui.usedproducts.manage.viewmodels

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.lts360.api.utils.Result
import com.lts360.api.utils.ResultError
import com.lts360.api.utils.mapExceptionToError
import com.lts360.api.app.AppClient
import com.lts360.api.app.ManageUsedProductListingService
import com.lts360.api.common.errors.ErrorResponse
import com.lts360.api.common.responses.ResponseReply
import com.lts360.api.models.service.EditableLocation
import com.lts360.api.models.service.EditableUsedProductListing
import com.lts360.compose.ui.chat.MAX_IMAGES
import com.lts360.compose.ui.managers.UserSharedPreferencesManager
import com.lts360.compose.ui.services.manage.models.CombinedContainer
import com.lts360.compose.ui.services.manage.models.CombinedContainerFactory
import com.lts360.compose.ui.usedproducts.manage.UsedProductListingsRepository
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
class PublishedUsedProductsListingViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    val savedStateHandle: SavedStateHandle,
    private val repository: UsedProductListingsRepository
) : ViewModel() {


    val userId: Long = UserSharedPreferencesManager.userId

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()


    private val _refreshing = MutableStateFlow(false)
    val refreshing = _refreshing.asStateFlow()


    private val _title = MutableStateFlow("")
    val title = _title.asStateFlow()

    private val _shortDescription = MutableStateFlow("")
    val shortDescription = _shortDescription.asStateFlow()


    private val _price = MutableStateFlow("")
    val price = _price.asStateFlow()

    val priceUnits = listOf("INR", "USD")

    private val userCurrency = Currency.getInstance(Locale.getDefault()).currencyCode

    private val _priceUnit = MutableStateFlow<String>(if (userCurrency in priceUnits) userCurrency else "INR")
    val priceUnit = _priceUnit.asStateFlow()

    private val _priceUnitError = MutableStateFlow<String?>(null)
    val priceUnitError = _priceUnitError.asStateFlow()


    private val _selectedLocation = MutableStateFlow<EditableLocation?>(null)
    val selectedLocation = _selectedLocation.asStateFlow()

    private val _country = MutableStateFlow<String?>(null)
    val selectedCountry = _country.asStateFlow()

    private val _state = MutableStateFlow<String?>(null)
    val selectedState = _state.asStateFlow()

    private val _imageContainers = MutableStateFlow<List<CombinedContainer>>(emptyList())
    val imageContainers = _imageContainers.asStateFlow()

    private val _titleError = MutableStateFlow<String?>(null)
    val titleError = _titleError.asStateFlow()

    private val _shortDescriptionError = MutableStateFlow<String?>(null)
    val shortDescriptionError = _shortDescriptionError.asStateFlow()

    private val _priceError = MutableStateFlow<String?>(null)
    val priceError = _priceError.asStateFlow()


    private val _selectedLocationError = MutableStateFlow<String?>(null)
    val selectedLocationError = _selectedLocationError.asStateFlow()

    private val _countryError = MutableStateFlow<String?>(null)
    val selectedCountryError = _countryError.asStateFlow()

    private val _stateError = MutableStateFlow<String?>(null)
    val selectedStateError = _stateError.asStateFlow()


    private val _imageContainersError = MutableStateFlow<String?>(null)
    val imageContainersError = _imageContainersError.asStateFlow()


    private val _resultError = MutableStateFlow<ResultError?>(null)
    val resultError = _resultError.asStateFlow()

    private var errorMessage: String = ""

    val publishedUsedProductListings = repository.publishedUsedProductListings
    val selectedUsedProductListing = repository.selectedUsedProductListing

    private var onCreateSecondsJob: Job? = null


    private val _isUpdating = MutableStateFlow(false)
    val isUpdating = _isUpdating.asStateFlow()

    private val _isDeleting = MutableStateFlow(false)
    val isDeleting = _isDeleting.asStateFlow()


    private val containerFactory = CombinedContainerFactory()


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

        _isLoading.value = true

        viewModelScope.launch {
            _resultError.value = null
            repository.onGetPublishedUsedProductListings(userId, {
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

    fun refreshPublishedSeconds(userId: Long) {
        _resultError.value = null
        viewModelScope.launch {
            _isLoading.value = true
            repository.onGetPublishedUsedProductListings(userId, {
                _isLoading.value = false
            }, {
                _isLoading.value = false
                val mappedException = mapExceptionToError(it)
                _resultError.value = mappedException
                errorMessage = mapExceptionToError(it).errorMessage
            })
        }
    }

    fun isSelectedUsedProductListingNull(): Boolean {
        return selectedUsedProductListing.value == null
    }

    fun setSelectedSeconds(secondsId: Long) {
        repository.setSelectedItem(secondsId)
        selectedUsedProductListing.value?.let {
            loadManageProductInfoDetails(it)
        }
    }

    fun removeSelectedSeconds(secondsId: Long) {
        repository.removeSelectedUsedProductListing(secondsId)
    }




    private fun loadManageProductInfoDetails(publishedSeconds: EditableUsedProductListing) {
        // Reset all values before initializing
        _title.value = ""
        _titleError.value = null

        _shortDescription.value = ""
        _shortDescriptionError.value = null

        _country.value = ""
        _state.value = ""
        _selectedLocation.value = null
        _price.value = ""

        // Clear previous images if applicable
        _imageContainers.value = emptyList()

        updateTitle(publishedSeconds.name)
        updateShortDescription(publishedSeconds.description)
        updateCountry(publishedSeconds.country)
        updateState(publishedSeconds.state)
        updateLocation(publishedSeconds.location)
        updatePrice(publishedSeconds.price.toString())
        updatePriceUnit(publishedSeconds.priceUnit)

        loadImageContainers(publishedSeconds.images.map {
            containerFactory.createCombinedContainerForEditableImage(it)
        })
    }

    fun onDeleteSeconds(
        userId: Long,
        secondsId: Long,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
    ) {

        viewModelScope.launch {
            _isDeleting.value = true
            try {
                when (val result = deleteSeconds(userId, secondsId)) {
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
                _isDeleting.value = false // Reset loading state
            }

        }

    }


    private suspend fun deleteSeconds(
        userId: Long,
        secondsId: Long,
    ): Result<ResponseReply> {


        return try {
            val response = AppClient.instance.create(ManageUsedProductListingService::class.java)
                .deleteUsedProductListing(secondsId, userId)

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
    fun updatePriceUnit(priceUnit: String) {
        _priceUnit.value = priceUnit
        _priceUnitError.value = null // Clear error
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


    fun updatePrice(newPrice: String?) {
        when {
            newPrice.isNullOrBlank() -> {
                _price.value = "" // Clear the price if empty
                _priceError.value = null // Clear error
            }

            newPrice.toDoubleOrNull() != null -> {
                _price.value = newPrice
                _priceError.value = null // Clear error
            }

            else -> {
                _priceError.value = "Invalid price" // Optional: Set an error for invalid input
            }
        }
    }


    fun updateLocation(newLocation: EditableLocation?) {
        _selectedLocation.value = newLocation
        _selectedLocationError.value = null

    }


    private fun loadImageContainers(bitmapContainers: List<CombinedContainer>) {
        _imageContainers.value = bitmapContainers
    }


    fun addContainer(
        path: String,
        width: Int,
        height: Int,
        format: String,
        errorMessage: String? = null
    ) {
        _imageContainers.value = _imageContainers.value.toMutableList().apply {
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
        val currentContainers = _imageContainers.value
        if (index in currentContainers.indices) {
            _imageContainers.value = currentContainers.mapIndexed { i, container ->
                if (i == index) {
                    containerFactory.createCombinedContainerForBitmap(
                        path,
                        width,
                        height,
                        format,
                        errorMessage
                    )
                } else container
            }
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


    private fun validateTitle(): Boolean {
        return if (_title.value.isBlank()) {
            _titleError.value = "Title cannot be empty"
            false
        } else if (_shortDescription.value.length > 100) {
            _title.value = "Seconds title cannot be exceed 100 characters"
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
            _shortDescription.value = "Seconds short description cannot be exceed 250 characters"
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

    private fun validatePrice(): Boolean {
        return if (_price.value.isEmpty()) {
            _price.value = "Price must be selected"
            false
        } else if (_price.value.toDoubleOrNull() == null) {
            _price.value = "Price must be 0.00 format"
            false
        } else {
            _priceError.value = null
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


    private fun validatePriceUnit(): Boolean {
        return if (_priceUnit.value.isEmpty()) {
            _priceUnitError.value = "Price unit must be selected"
            false
        } else if (!priceUnits.contains(_priceUnit.value)) {
            _priceUnitError.value = "Invalid unit selected"
            false
        } else {
            _priceUnitError.value = null
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

        val isTitleValid = validateTitle()
        val isShortDescriptionValid = validateShortDescription()
        val isSelectedLocationValid = validateSelectedLocation()
        val isSelectedCountry = validateSelectedCountry()
        val isSelectedState = validateSelectedState()
        val isValidPrice = validatePrice()
        val isPriceUnitValid = validatePriceUnit()
        val isContainersValid = validateImageContainers()

        return isTitleValid && isShortDescriptionValid &&
                isSelectedLocationValid &&
                isSelectedCountry
                && isSelectedState
                && isContainersValid
                && isValidPrice && isPriceUnitValid
    }


    fun onUpdateUsedProductListing(
        productId: RequestBody,
        title: RequestBody,
        shortDescription: RequestBody,
        price: RequestBody,
        priceUnit: RequestBody,
        state: RequestBody,
        country: RequestBody,
        images: List<MultipartBody.Part>,
        location: RequestBody?,
        keepImageIds: RequestBody,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
    ) {
        onCreateSecondsJob = viewModelScope.launch {
            _isUpdating.value = true

            when (val result = createOrUpdateUsedProductListing(
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
                    val editableUsedProductListing =
                        Gson().fromJson(result.data.data, EditableUsedProductListing::class.java)
                    repository.updateProductId(editableUsedProductListing)
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
        productId: RequestBody,
        title: RequestBody,
        shortDescription: RequestBody,
        price: RequestBody,
        priceUnit: RequestBody,
        state: RequestBody,
        country: RequestBody,
        images: List<MultipartBody.Part>,
        location: RequestBody? = null,
        keepImageIds: RequestBody,
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
