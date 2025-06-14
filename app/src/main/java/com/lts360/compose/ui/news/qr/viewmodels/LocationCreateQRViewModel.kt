package com.lts360.compose.ui.news.qr.viewmodels

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lts360.compose.ui.news.qr.viewmodels.repos.QRCodeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LocationCreateQRViewModel(private val qrCodeRepository: QRCodeRepository) :
    ViewModel() {

    private val _latitude = MutableStateFlow("")
    val latitude = _latitude.asStateFlow()

    private val _longitude = MutableStateFlow("")
    val longitude = _longitude.asStateFlow()

    private val _qrCodeLocationBitmap = MutableStateFlow<Bitmap?>(null)
    val qrCodeLocationBitmap = _qrCodeLocationBitmap.asStateFlow()

    fun updateLatitude(newLatitude: String) {
        _latitude.value = newLatitude
    }

    // Function to update longitude
    fun updateLongitude(newLongitude: String) {
        _longitude.value = newLongitude
    }

    // Function to generate QR code for location (latitude and longitude)
    fun generateQrCodeForLocation(
        latitude: String,
        longitude: String,
        onSuccess: () -> Unit = {},
        onError: () -> Unit = {}
    ) {
        viewModelScope.launch {
            // Format the location as a string (e.g., "geo:<latitude>,<longitude>")
            val locationString = "geo:$latitude,$longitude"

            qrCodeRepository.generateQRCode(locationString)
                ?.let {
                    _qrCodeLocationBitmap.value = it
                    onSuccess()
                } ?: run {
                onError()
            }
        }
    }
}
