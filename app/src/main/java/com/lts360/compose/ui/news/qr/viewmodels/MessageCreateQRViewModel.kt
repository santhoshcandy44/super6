package com.lts360.compose.ui.news.qr.viewmodels

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lts360.compose.ui.news.qr.viewmodels.repos.QRCodeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MessageCreateQRViewModel(private val qrCodeRepository: QRCodeRepository) :
    ViewModel() {

    private val _phoneNumber = MutableStateFlow("")
    val phoneNumber = _phoneNumber.asStateFlow()

    private val _subject = MutableStateFlow("")
    val subject = _subject.asStateFlow()

    // Message (body) input state for SMS
    private val _message = MutableStateFlow("")
    val message = _message.asStateFlow()

    // Bitmap for generated QR code
    private val _qrCodeSmsBitmap = MutableStateFlow<Bitmap?>(null)
    val qrCodeSmsBitmap = _qrCodeSmsBitmap.asStateFlow()

    // Function to update phone number state
    fun updatePhoneNumber(newPhoneNumber: String) {
        _phoneNumber.value = newPhoneNumber
    }

    // Function to update subject state (if applicable)
    fun updateSubject(newSubject: String) {
        _subject.value = newSubject
    }

    // Function to update message (body) state
    fun updateMessage(newMessage: String) {
        _message.value = newMessage
    }

    // Function to generate QR code for SMS
    fun generateQrCodeForSms(
        phoneNumber: String,
        subject: String,
        message: String,
        onSuccess: () -> Unit = {},
        onError: () -> Unit = {}
    ) {
        viewModelScope.launch {
            // Format the SMS in a standard format (sms:phoneNumber?body=message)
            val smsUri = "sms:$phoneNumber?body=${message.encodeUrl()}"

            qrCodeRepository.generateQRCode(smsUri)
                ?.let {
                    _qrCodeSmsBitmap.value = it
                    onSuccess()
                } ?: run {
                onError()
            }
        }
    }

    // Extension function to properly encode URL parameters
    private fun String.encodeUrl(): String {
        return java.net.URLEncoder.encode(this, "UTF-8")
    }
}
