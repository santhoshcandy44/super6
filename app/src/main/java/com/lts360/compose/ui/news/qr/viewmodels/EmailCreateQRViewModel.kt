package com.lts360.compose.ui.news.qr.viewmodels

import javax.inject.Inject
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lts360.compose.ui.news.qr.viewmodels.repos.QRCodeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EmailCreateQRViewModel(private val qrCodeRepository: QRCodeRepository) :
    ViewModel() {

    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    // Title input state (for "mailto" QR code)
    private val _subject = MutableStateFlow("")
    val subject = _subject.asStateFlow()

    // Content (body) input state for email
    private val _content = MutableStateFlow("")
    val content = _content.asStateFlow()

    // Bitmap for generated QR code
    private val _qrCodeEmailBitmap = MutableStateFlow<Bitmap?>(null)
    val qrCodeEmailBitmap = _qrCodeEmailBitmap.asStateFlow()

    // Function to update email state
    fun updateEmail(newEmail: String) {
        _email.value = newEmail
    }

    // Function to update title state
    fun updateTitle(newTitle: String) {
        _subject.value = newTitle
    }

    // Function to update content (body) state
    fun updateContent(newContent: String) {
        _content.value = newContent
    }

    // Function to generate QR code for email
    fun generateQrCodeForEmail(
        email: String,
        title: String,
        content: String,
        onSuccess: () -> Unit = {},
        onError: () -> Unit = {}
    ) {
        viewModelScope.launch {
            // Format the email in a standard "mailto" format with subject and body
            val mailtoUri = "mailto:$email?subject=${title.encodeUrl()}&body=${content.encodeUrl()}"

            qrCodeRepository.generateQRCode(mailtoUri)
                ?.let {
                    _qrCodeEmailBitmap.value = it
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
