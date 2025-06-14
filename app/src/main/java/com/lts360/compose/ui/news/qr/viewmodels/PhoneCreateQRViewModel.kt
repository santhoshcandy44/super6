package com.lts360.compose.ui.news.qr.viewmodels
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lts360.compose.ui.news.qr.viewmodels.repos.QRCodeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class PhoneCreateQRViewModel(private val qrCodeRepository: QRCodeRepository) :
    ViewModel() {

    // Phone number input state
    private val _phoneNumber = MutableStateFlow("")
    val phoneNumber = _phoneNumber.asStateFlow()

    // Bitmap for generated QR code
    private val _qrCodePhoneBitmap = MutableStateFlow<Bitmap?>(null)
    val qrCodePhoneBitmap = _qrCodePhoneBitmap.asStateFlow()

    // Function to update phone number state
    fun updatePhoneNumber(newPhoneNumber: String) {
        _phoneNumber.value = newPhoneNumber
    }

    // Function to generate QR code for phone number
    fun generateQrCodeForPhoneNumber(
        phoneNumber: String,
        onSuccess: () -> Unit = {},
        onError: () -> Unit = {}
    ) {
        viewModelScope.launch {
            // Format the phone number in a standard format (e.g., tel:+1234567890)
            val phoneNumberString = "tel:$phoneNumber"

            qrCodeRepository.generateQRCode(phoneNumberString)
                ?.let {
                    _qrCodePhoneBitmap.value = it
                    onSuccess()
                } ?: run {
                onError()
            }
        }
    }
}
