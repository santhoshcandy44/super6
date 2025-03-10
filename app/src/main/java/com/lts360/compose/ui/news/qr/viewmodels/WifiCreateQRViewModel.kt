package com.lts360.compose.ui.news.qr.viewmodels

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lts360.compose.ui.news.qr.viewmodels.repos.QRCodeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WifiCreateQRViewModel @Inject constructor(private val qrCodeRepository: QRCodeRepository) :
    ViewModel() {

    // State for the Wi-Fi SSID, password, encryption type, and hidden flag
    private val _ssid = MutableStateFlow("")
    val ssid = _ssid.asStateFlow()

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()

    private val _encryptionType = MutableStateFlow("WPA") // Default encryption type (can be WPA, WEP, or open)
    val encryptionType = _encryptionType.asStateFlow()

    private val _isHidden = MutableStateFlow(false) // State for whether the network is hidden
    val isHidden = _isHidden.asStateFlow()

    // Bitmap for generated Wi-Fi QR code
    private val _qrCodeBitmap = MutableStateFlow<Bitmap?>(null)
    val qrCodeBitmap = _qrCodeBitmap.asStateFlow()

    // Function to update the Wi-Fi SSID
    fun updateSSID(newSsid: String) {
        _ssid.value = newSsid
    }

    // Function to update the Wi-Fi password
    fun updatePassword(newPassword: String) {
        _password.value = newPassword
    }

    // Function to update the encryption type (WPA, WEP, or open)
    fun updateEncryptionType(newEncryptionType: String) {
        _encryptionType.value = newEncryptionType
    }

    // Function to update the hidden network flag
    fun updateHiddenStatus(isHidden: Boolean) {
        _isHidden.value = isHidden
    }

    // Function to generate the Wi-Fi QR code
    fun generateWifiQrCode(
        ssid: String,
        password: String,
        encryptionType: String,
        isHidden: Boolean,
        onSuccess: () -> Unit = {},
        onError: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val wifiQrCodeData = if (isHidden) {
                // For hidden networks, specify the SSID and other info explicitly
                "WIFI:S:$ssid;T:$encryptionType;P:$password;H:true;;"
            } else {
                // For non-hidden networks
                "WIFI:S:$ssid;T:$encryptionType;P:$password;;"
            }

            qrCodeRepository.generateQRCode(wifiQrCodeData)
                ?.let {
                    _qrCodeBitmap.value = it
                    onSuccess()
                } ?: run {
                onError()
            }
        }
    }
}
