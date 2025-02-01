package com.super6.pot.ui.services.manage.viewmodels

import androidx.lifecycle.ViewModel
import com.super6.pot.ui.managers.UserSharedPreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class EditServiceInfoViewModel @Inject constructor(val repository: PublishedServiceRepository) :
    ViewModel() {



    val userId = UserSharedPreferencesManager.userId


    val selectedService = repository.selectedService


    private var errorMessage: String = ""



    init {

        selectedService.value?.let {
//            loadManageServiceInfoDetails(it)
        }
    }





    override fun onCleared() {
        super.onCleared()

      /*  _serviceTitle.value = ""
        _serviceTitleError.value = null

        _shortDescription.value = ""
        _shortDescriptionError.value = null

        _longDescriptionError.value = ""
        _longDescriptionError.value = null
        _selectedIndustry.value = -1
        _selectedIndustryError.value = null*/

    }


}