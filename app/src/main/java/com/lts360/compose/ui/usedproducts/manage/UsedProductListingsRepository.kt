package com.lts360.compose.ui.usedproducts.manage

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lts360.api.Utils.Result
import com.lts360.api.app.AppClient
import com.lts360.api.app.ManageUsedProductListingService
import com.lts360.api.common.errors.ErrorResponse
import com.lts360.api.common.responses.ResponseReply
import com.lts360.api.models.service.EditableUsedProductListing
import com.lts360.api.models.service.UsedProductListing
import com.lts360.api.models.service.toEditableUsedProductListing
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject


class UsedProductListingsRepository @Inject constructor() {

    // StateFlow for managing the list of published used product listings
    private val _publishedUsedProductListings = MutableStateFlow<List<EditableUsedProductListing>>(emptyList())
    val publishedUsedProductListings: StateFlow<List<EditableUsedProductListing>> get() = _publishedUsedProductListings

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _selectedUsedProductListing = MutableStateFlow<EditableUsedProductListing?>(null)
    val selectedUsedProductListing: StateFlow<EditableUsedProductListing?> = _selectedUsedProductListing



    fun updateProductId(productListing: EditableUsedProductListing) {
        _publishedUsedProductListings.update { list ->
            list.map { product ->
                if (product.productId == productListing.productId) {
                    productListing
                } else {
                    product
                }
            }
        }

        _selectedUsedProductListing.value?.let {
            if(it.productId==productListing.productId){
                _selectedUsedProductListing.update{
                    productListing
                }
            }
        }
    }


    // Function to update the list of published used product listings
    private fun updatePublishedUsedProductListings(newListings: List<EditableUsedProductListing>) {
        _publishedUsedProductListings.value = newListings
    }

    fun setSelectedItem(productId: Long) {
        val index = _publishedUsedProductListings.value.indexOfFirst {
            it.productId == productId
        }
        if (index != -1) {
            _selectedUsedProductListing.value = _publishedUsedProductListings.value[index]
        }
    }

    fun removeSelectedUsedProductListing(productId: Long) {
        val index = _publishedUsedProductListings.value.indexOfFirst {
            it.productId == productId
        }

        if (index != -1) {
            _publishedUsedProductListings.value = _publishedUsedProductListings.value.filter {
                it.productId != productId
            }
        }

        invalidateSelectedItem()
    }

    fun invalidateSelectedItem() {
        _selectedUsedProductListing.value = null
    }

    suspend fun onGetPublishedUsedProductListings(
        userId: Long,
        onSuccess: (items: List<EditableUsedProductListing>) -> Unit,
        onError: (Throwable) -> Unit,
    ) {



        try {
            when (val result = getPublishedUsedProductListings(userId)) { // Call the network function
                is Result.Success -> {
                    val data = Gson().fromJson(result.data.data, object : TypeToken<List<UsedProductListing>>() {}.type)
                            as List<UsedProductListing>
                    val listings = data.map { it.toEditableUsedProductListing() }

                    updatePublishedUsedProductListings(listings)
                    onSuccess(listings)
                }

                is Result.Error -> {
                    if (_publishedUsedProductListings.value.isNotEmpty()) {
                        _publishedUsedProductListings.value = emptyList()
                    }
                    onError(result.error)
                }
            }
        } catch (t: Throwable) {
            if (_publishedUsedProductListings.value.isNotEmpty()) {
                _publishedUsedProductListings.value = emptyList()
            }
            t.printStackTrace()
            onError(Exception("Something Went Wrong"))
        }
    }


    // Function to fetch used product listings from the API
    private suspend fun getPublishedUsedProductListings(userId: Long): Result<ResponseReply> {
        return try {
            val response = AppClient.instance.create(ManageUsedProductListingService::class.java)
                .getUsedProductListingsByUserId(userId)

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
