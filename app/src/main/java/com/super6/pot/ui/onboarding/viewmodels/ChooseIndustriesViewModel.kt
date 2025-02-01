package com.super6.pot.ui.onboarding.viewmodels

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.super6.pot.api.app.AppClient
import com.super6.pot.api.common.errors.ErrorResponse
import com.super6.pot.api.common.responses.ResponseReply
import com.super6.pot.api.models.service.GuestIndustryDao
import com.super6.pot.api.models.service.Industry
import com.super6.pot.ui.managers.UserSharedPreferencesManager
import com.super6.pot.ui.onboarding.navhost.OnBoardingScreen
import com.super6.pot.ui.managers.NetworkConnectivityManager
import com.super6.pot.api.Utils.Result
import com.super6.pot.api.Utils.ResultError
import com.super6.pot.api.Utils.mapExceptionToError
import com.super6.pot.api.app.IndustriesSettingsService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ChooseIndustriesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    networkConnectivityManager: NetworkConnectivityManager
) : ViewModel() {


    private val args = savedStateHandle.toRoute<OnBoardingScreen.ChooseIndustries>()


    val connectivityManager = networkConnectivityManager


    val userId: Long = args.userId

    val type: String? = args.type


    private val _industryItems = mutableStateListOf<Industry>()

    val itemList: List<Industry> = _industryItems


    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()


    private val _isUpdating = MutableStateFlow(false)
    val isUpdating = _isUpdating.asStateFlow()


    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()


    private val _error = MutableStateFlow<ResultError?>(null)
    val error = _error.asStateFlow()

    private var errorMessage: String = ""

    init {
        onGetIndustries(
            userId,
            onSuccess = {}) {}
    }


    fun validateIndustries(): Boolean {
        return _industryItems.any { it.isSelected }
    }


    fun setRefreshing(isRefreshing: Boolean) {
        _isRefreshing.value = isRefreshing
    }

    // Function to update the error message
    fun updateError(exception:Throwable?) {
        _error.value = exception?.let {
            mapExceptionToError(it)
        }
    }


    fun onGetIndustries(
        userId: Long,
        isLoading: Boolean = true,
        isRefreshing: Boolean = false,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch {


            try {
                if (isLoading) {
                    _isLoading.value = true

                }
                if (isRefreshing) {
                    _isRefreshing.value = true

                }

                when (val result = getIndustries(userId)) { // Call the network function
                    is Result.Success -> {
                        _industryItems.clear()
                        _industryItems.addAll(Gson().fromJson(result.data.data,
                            object : TypeToken<List<Industry>>() {}.type ))
                        onSuccess()
                        updateError(null)
                    }

                    is Result.Error -> {
                        if(result.error is CancellationException){
                            return@launch
                        }
                        val error = mapExceptionToError(result.error)
                        errorMessage = error.errorMessage
                        onError(errorMessage)
                        updateError(result.error)
                        // Handle the error and update the UI accordingly
                    }

                }


            }catch (t:Throwable){
                errorMessage = "Something went wrong"
                onError(errorMessage)
                t.printStackTrace()
            }finally {

                if (isLoading) {
                    _isLoading.value = false

                }
                if (isRefreshing) {
                    _isRefreshing.value = false

                }
            }

        }


    }


    private suspend fun getIndustries(userId: Long): Result<ResponseReply> {
        return try {
            val response =
                AppClient.instance.create(IndustriesSettingsService::class.java).getIndustries(userId)
            if (response.isSuccessful) {

                val responseBody = response.body()

                if (responseBody != null && responseBody.isSuccessful) {
                    Result.Success(responseBody)
                } else {
                    val errorMessage = "Failed, try again later..."
                    Result.Error(Exception(errorMessage))
                }

            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage= try {
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


    fun onUpdateIndustries(
        userId: Long,
        selectedItems: List<Industry>,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch {
            try {
                _isUpdating.value = true
                when (val result = updateIndustries(
                    userId,
                    selectedItems)) { // Call the network function
                    is Result.Success -> {
                        _industryItems.clear()
                        _industryItems.addAll(
                            Gson().fromJson(
                                result.data.data,
                                object : TypeToken<List<Industry>>() {}.type
                            )
                        )
                        onSuccess(result.data.message)

                    }

                    is Result.Error -> {
                        errorMessage = mapExceptionToError(result.error).errorMessage
                        onError(errorMessage)
                        // Handle the error and update the UI accordingly
                    }

                }
            }catch (t:Throwable){
                errorMessage = "Something went wrong"
                onError(errorMessage)
                t.printStackTrace()
            }finally {
                _isUpdating.value = false // Reset loading state
            }

        }

    }


    private suspend fun updateIndustries(
        userId: Long,
        selectedItems: List<Industry>,
    ): Result<ResponseReply> {
        val jsonString = Gson().toJson(selectedItems)

        return try {


            val response = AppClient.instance.create(IndustriesSettingsService::class.java)
                .updateIndustries(userId,jsonString)
            if (response.isSuccessful) {

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
                    "An unknown error occurred"
                }
                Result.Error(Exception(errorMessage))
            }
        } catch (t: Throwable) {
            Result.Error(t)
        }

    }


    fun onIndustrySelectionChanged(updatedIndustry: Industry) {
        // Update the selection state
        val index = _industryItems.indexOf(updatedIndustry)
        if (index != -1) {
            _industryItems[index] = updatedIndustry.copy(isSelected = !updatedIndustry.isSelected)
        }
    }


//    fun onIndustrySelectionChanged(updatedIndustry: Industry) {
//        viewModelScope.launch {
//            // Find the index of the industry by its unique ID
//            val updatedList = _industryItems2.value.map {
//                if (it.industryId == updatedIndustry.industryId) {
//                    it.copy(isSelected = !updatedIndustry.isSelected)
//                } else {
//                    it
//                }
//            }
//            _industryItems2.value = updatedList // Update the StateFlow
//        }
//    }

}




@HiltViewModel
class GuestChooseIndustriesViewModel @Inject constructor(
    networkConnectivityManager: NetworkConnectivityManager,
    val guestIndustryDao: GuestIndustryDao) : ViewModel() {


    val connectivityManager = networkConnectivityManager


    val userId: Long = UserSharedPreferencesManager.userId

    private val _industryItems = MutableStateFlow<List<Industry>>(emptyList())
    val itemList: StateFlow<List<Industry>> = _industryItems.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()


    private val _isUpdating = MutableStateFlow(false)
    val isUpdating = _isUpdating.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()


    private val _error = MutableStateFlow<ResultError?>(null)
    val error = _error.asStateFlow()

    private var errorMessage: String = ""

    init {
        onGetGuestIndustries(
            userId,
            onSuccess = {}) {}
    }


    fun validateIndustries(): Boolean {

        return _industryItems.value.any { it.isSelected }
    }



    fun setRefreshing(isRefreshing: Boolean) {
        _isRefreshing.value = isRefreshing
    }

    // Function to update the error message
    fun updateError(exception: Throwable?) {
        _error.value = exception?.let {
            mapExceptionToError(it)
        }
    }


    fun onGetGuestIndustries(
        userId: Long,
        isLoading: Boolean = true,
        isRefreshing: Boolean = false,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch {


            try {
                if (isLoading) {
                    _isLoading.value = true

                }
                if (isRefreshing) {
                    _isRefreshing.value = true

                }

                when (val result = getGuestIndustries(userId)) { // Call the network function
                    is Result.Success -> {
                        updateError(null)
                        val allIndustries = guestIndustryDao.getAllIndustries()

                        val mappedIndustries= (Gson().fromJson(result.data.data,
                            object : TypeToken<List<Industry>>() {}.type) as List<Industry>)
                            .map { industryItem ->
                            // Find the corresponding industry from the database
                            val industryFromDb = allIndustries.find { industryItem.industryId == it.industryId }

                            if (industryFromDb == null) {
                                industryItem // No change, keep as is
                            } else {
                                industryItem.copy(isSelected = true) // Mark as selected
                            }
                        }

                        _industryItems.value=mappedIndustries
                        onSuccess()
                    }

                    is Result.Error -> {

                        if(result.error is CancellationException){
                            return@launch
                        }

                        val error = mapExceptionToError(result.error)
                        errorMessage = error.errorMessage
                        updateError(result.error)
                        onError(errorMessage)
                        // Handle the error and update the UI accordingly
                    }

                }


            }catch (t:Throwable){
                errorMessage = "Something went wrong"
                onError(errorMessage)
                t.printStackTrace()
            }finally {

                if (isLoading) {
                    _isLoading.value = false

                }
                if (isRefreshing) {
                    _isRefreshing.value = false

                }
            }
        }
    }


    private suspend fun getGuestIndustries(userId: Long): Result<ResponseReply> {
        return try {
            val response = AppClient.instance.create(IndustriesSettingsService::class.java).getGuestIndustries(userId)
            if (response.isSuccessful) {
                val responseBody = response.body()

                if (responseBody != null && responseBody.isSuccessful) {
                    Result.Success(responseBody)
                } else {
                    val errorMessage = "Failed, try again later..."
                    Result.Error(Exception(errorMessage))
                }

            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage= try {
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


    fun onUpdateIndustries(
        selectedItems: List<Industry>,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch {


            try {

                _isUpdating.value = true


                // Collect the items to insert and delete
                val itemsToInsert = selectedItems.filter { it.isSelected }
                val itemsToDelete = selectedItems.filterNot { it.isSelected }

                // Perform batch insert and delete operations
                if (itemsToInsert.isNotEmpty()) {
                    guestIndustryDao.insertIndustries(itemsToInsert)
                }

                if (itemsToDelete.isNotEmpty()) {
                    guestIndustryDao.deleteIndustries(itemsToDelete.map { it.industryId })
                }



                val mappedIndustries = _industryItems.value
                    .also {industryItems->

                        val allIndustries = guestIndustryDao.getAllIndustries()
                        // Map over _industryItems and find corresponding industry in the database
                        industryItems.map { industryItem ->
                            // Find the corresponding industry from the fetched list
                            val industryFromDb = allIndustries.find { industryItem.industryId == it.industryId }

                            if(industryFromDb==null){
                                industryItem
                            }else{
                                industryItem.copy(isSelected = true)
                            }
                        }

                    }

                _industryItems.value=mappedIndustries

                onSuccess("Industries Updated")

            }catch (t:Throwable){
                errorMessage = "Something went wrong"
                onError(errorMessage)
                t.printStackTrace()
            }finally {
                _isUpdating.value = false // Reset loading state
            }

        }

    }





    fun onIndustrySelectionChanged(updatedIndustry: Industry) {
        // Use `update` to modify the state directly
        _industryItems.update { currentList ->
            // Modify the current list by toggling the `isSelected` value of the matching industry
            currentList.map { industry ->
                if (industry.industryId == updatedIndustry.industryId) {
                    industry.copy(isSelected = !industry.isSelected) // Toggle the selection state
                } else {
                    industry // Keep other items unchanged
                }
            }
        }
    }




//    fun onIndustrySelectionChanged(updatedIndustry: Industry) {
//        viewModelScope.launch {
//            // Find the index of the industry by its unique ID
//            val updatedList = _industryItems2.value.map {
//                if (it.industryId == updatedIndustry.industryId) {
//                    it.copy(isSelected = !updatedIndustry.isSelected)
//                } else {
//                    it
//                }
//            }
//            _industryItems2.value = updatedList // Update the StateFlow
//        }
//    }

}

