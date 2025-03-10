package com.lts360.compose.ui.news.qr.viewmodels.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lts360.compose.ui.news.qr.models.QRCodeEntity
import com.lts360.compose.ui.news.qr.models.QRCodeType
import com.lts360.compose.ui.news.qr.viewmodels.repos.QRCodeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class QRCodeViewModel @Inject constructor(private val repository: QRCodeRepository) : ViewModel() {

    // MutableStateFlow to hold QR codes
    private val _qrCodes = MutableStateFlow<List<QRCodeEntity>>(emptyList())
    val qrCodes = _qrCodes.asStateFlow()

    init {
        loadQRCodeEntities()
    }
    // Function to load QR codes from the repository
    fun loadQRCodeEntities() {
        // This could be done in a coroutine scope, usually through a viewModelScope
        viewModelScope.launch {
            // Fetch QR codes in the IO dispatcher
            val qrCodesList = withContext(Dispatchers.IO) {
                repository.getAllQRCodeData(QRCodeType.QRCODE)
            }
            // Update the state with the fetched list
            _qrCodes.value = qrCodesList
        }
    }
}
