package com.super6.pot.compose.ui.services.manage.viewmodels

import androidx.lifecycle.ViewModel
import com.super6.pot.compose.ui.managers.UserSharedPreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class EditServiceImagesViewModel @Inject constructor(val repository: PublishedServiceRepository) :
    ViewModel() {


    val userId = UserSharedPreferencesManager.userId

    val selectedService = repository.selectedService





    private var errorMessage: String = ""

    init {
//
//        selectedService.value?.let {
//            loadManageImages(it)
//        }

    }





//
//    override fun onCleared() {
//        super.onCleared()
//        _editableContainers.clear()
//    }

}
