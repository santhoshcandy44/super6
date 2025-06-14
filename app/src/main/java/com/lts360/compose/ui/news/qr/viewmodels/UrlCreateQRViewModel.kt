package com.lts360.compose.ui.news.qr.viewmodels

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lts360.compose.ui.news.qr.viewmodels.repos.QRCodeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class UrlCreateQRViewModel(private val qrCodeRepository: QRCodeRepository) :
    ViewModel() {


    private val _title = MutableStateFlow("")
    val title = _title.asStateFlow()


    // URL input state (for URL encoding)
    private val _url = MutableStateFlow("")
    val url = _url.asStateFlow()

    // Bitmap for generated QR code
    private val _qrCodeTextBitmap = MutableStateFlow<Bitmap?>(null)
    val qrCodeTextBitmap = _qrCodeTextBitmap.asStateFlow()

    // Function to update the title
    fun updateTitle(newTitle: String) {
        _title.value = newTitle
    }

    // Function to update URL state
    fun updateURL(newUrl: String) {
        _url.value = newUrl
    }

    // Function to generate QR code for URL
    fun generateQrCodeForUrl( title:String, url: String, onSuccess: () -> Unit = {}, onError: () -> Unit = {}) {
        viewModelScope.launch {
            qrCodeRepository.generateQRCode(url)
                ?.let {
                    _qrCodeTextBitmap.value = it
                    onSuccess()
                } ?: run {
                onError()
            }
        }
    }
}
