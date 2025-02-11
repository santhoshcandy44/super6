package com.super6.pot.compose.ui.services.manage.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import com.super6.pot.components.utils.LogUtils.TAG
import com.super6.pot.compose.ui.managers.UserSharedPreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject


@HiltViewModel
class EditServiceThumbnailViewModel @Inject constructor(val repository: PublishedServiceRepository) :
    ViewModel() {



    val userId = UserSharedPreferencesManager.userId


    val selectedService = repository.selectedService


    private val _isUpdating = MutableStateFlow(false)
    val isUpdating: StateFlow<Boolean> = _isUpdating


    private var errorMessage: String = ""





    init {
        Log.e(TAG,selectedService.value.toString())
    }






    override fun onCleared() {
        super.onCleared()


    }


}