package com.lts360.compose.ui.news.qr.viewmodels.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lts360.compose.ui.news.qr.models.QRCodeEntity
import com.lts360.compose.ui.news.qr.models.QRCodeType
import com.lts360.compose.ui.news.qr.viewmodels.repos.QRCodeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QRCodeViewModel (private val repository: QRCodeRepository) : ViewModel() {

    private val _qrCodes = MutableStateFlow<List<QRCodeEntity>>(emptyList())
    val qrCodes = _qrCodes.asStateFlow()

    init {
        loadQRCodeEntities()
    }

    fun loadQRCodeEntities() {
        viewModelScope.launch {
            val qrCodesList = withContext(Dispatchers.IO) {
                repository.getAllQRCodeData(QRCodeType.QRCODE)
            }
            _qrCodes.value = qrCodesList
        }
    }
}