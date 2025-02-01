package com.super6.pot.ui.manage.services.viewmodels

import androidx.lifecycle.ViewModel
import com.super6.pot.ui.managers.UserSharedPreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject


@HiltViewModel
class EditServiceLocationViewModel @Inject constructor(val repository:PublishedServiceRepository):
    ViewModel(){



    val userId = UserSharedPreferencesManager.userId

    val selectedService = repository.selectedService




    private val _isUpdating = MutableStateFlow(false)
    val isUpdating: StateFlow<Boolean> = _isUpdating


    private var errorMessage: String = ""



  /*  init {
       selectedService.value?.let {
           loadManageLocationDetails(it)
       }
    }




    override fun onCleared() {
        super.onCleared()
        _editableLocation.value = null

    }*/

}
