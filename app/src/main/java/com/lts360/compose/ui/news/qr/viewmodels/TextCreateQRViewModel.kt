package com.lts360.compose.ui.news.qr.viewmodels


import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lts360.compose.ui.news.qr.models.QRCodeDataType
import com.lts360.compose.ui.news.qr.models.QRCodeType
import com.lts360.compose.ui.news.qr.models.TextData
import com.lts360.compose.ui.news.qr.viewmodels.repos.QRCodeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class TextCreateQRViewModel (private val qrCodeRepository: QRCodeRepository) :
    ViewModel() {

    private val _textState = MutableStateFlow("")
    val textState = _textState.asStateFlow()

    private val _qrCodeTextBitmap = MutableStateFlow<Bitmap?>(null)
    val qrCodeTextBitmap = _qrCodeTextBitmap.asStateFlow()

    // Function to update text state
    fun updateText(newText: String) {
        _textState.value = newText
    }


    fun generateQrCodeForText(text: String, onSuccess: () -> Unit = {}, onError: () -> Unit = {}) {
        viewModelScope.launch {
            qrCodeRepository.generateQRCode(text)
                ?.let {

                    val jsonString = Json.encodeToString(TextData(text))
                    _qrCodeTextBitmap.value = it
                    qrCodeRepository.saveQRCodeToDatabase(
                        QRCodeDataType.TEXT,
                        jsonString,
                        it,
                        QRCodeType.QRCODE
                    )

                    onSuccess()
                } ?: run {
                onError()
            }
        }
    }

}
