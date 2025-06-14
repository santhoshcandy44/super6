package com.lts360.compose.ui.news.qr.viewmodels

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lts360.compose.ui.news.qr.viewmodels.repos.QRCodeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EventCreateQRViewModel(private val qrCodeRepository: QRCodeRepository) :
    ViewModel() {

    private val _startDate = MutableStateFlow("")
    val startDate = _startDate.asStateFlow()

    private val _endDate = MutableStateFlow("")
    val endDate = _endDate.asStateFlow()

    private val _title = MutableStateFlow("")
    val title = _title.asStateFlow()

    private val _organizer = MutableStateFlow("")
    val organizer = _organizer.asStateFlow()

    private val _location = MutableStateFlow("")
    val location = _location.asStateFlow()

    private val _description = MutableStateFlow("")
    val description = _description.asStateFlow()

    private val _latitude = MutableStateFlow("")
    val latitude = _latitude.asStateFlow()

    private val _longitude = MutableStateFlow("")
    val longitude = _longitude.asStateFlow()

    // Bitmap for generated QR code
    private val _qrCodeEventBitmap = MutableStateFlow<Bitmap?>(null)
    val qrCodeEventBitmap = _qrCodeEventBitmap.asStateFlow()

    // Function to update start date
    fun updateStartDate(newStartDate: String) {
        _startDate.value = newStartDate
    }

    // Function to update end date
    fun updateEndDate(newEndDate: String) {
        _endDate.value = newEndDate
    }

    // Function to update title
    fun updateTitle(newTitle: String) {
        _title.value = newTitle
    }

    // Function to update organizer
    fun updateOrganizer(newOrganizer: String) {
        _organizer.value = newOrganizer
    }

    // Function to update location
    fun updateLocation(newLocation: String) {
        _location.value = newLocation
    }

    // Function to update description
    fun updateDescription(newDescription: String) {
        _description.value = newDescription
    }

    // Function to update latitude
    fun updateLatitude(newLatitude: String) {
        _latitude.value = newLatitude
    }

    // Function to update longitude
    fun updateLongitude(newLongitude: String) {
        _longitude.value = newLongitude
    }

    // Function to generate QR code for event details with location (latitude and longitude)
    fun generateQrCodeForEvent(
        startDate: String,
        endDate: String,
        title: String,
        organizer: String,
        location: String,
        description: String,
        latitude: String,
        longitude: String,
        onSuccess: () -> Unit = {},
        onError: () -> Unit = {}
    ) {
        viewModelScope.launch {
            // Format the event details as a string (e.g., "BEGIN:VEVENT\n...")
            val eventString = """
                BEGIN:VEVENT
                DTSTART:$startDate
                DTEND:$endDate
                SUMMARY:$title
                ORGANIZER:$organizer
                LOCATION:$location
                DESCRIPTION:$description
                GEO:$latitude,$longitude
                END:VEVENT
            """.trimIndent()

            qrCodeRepository.generateQRCode(eventString)
                ?.let {
                    _qrCodeEventBitmap.value = it
                    onSuccess()
                } ?: run {
                onError()
            }
        }
    }
}
