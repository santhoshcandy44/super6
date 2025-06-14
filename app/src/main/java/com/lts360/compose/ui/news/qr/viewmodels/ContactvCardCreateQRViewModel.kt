package com.lts360.compose.ui.news.qr.viewmodels

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lts360.compose.ui.news.qr.models.QRCodeEntity
import com.lts360.compose.ui.news.qr.viewmodels.repos.QRCodeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ContactCreateQRViewModel(private val qrCodeRepository: QRCodeRepository) :
    ViewModel() {

    private val _firstName = MutableStateFlow("")
    val firstName = _firstName.asStateFlow()

    private val _lastName = MutableStateFlow("")
    val lastName = _lastName.asStateFlow()

    private val _mobile = MutableStateFlow("")
    val mobile = _mobile.asStateFlow()

    private val _phone = MutableStateFlow("")
    val phone = _phone.asStateFlow()

    private val _fax = MutableStateFlow("")
    val fax = _fax.asStateFlow()

    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    private val _company = MutableStateFlow("")
    val company = _company.asStateFlow()

    private val _job = MutableStateFlow("")
    val job = _job.asStateFlow()

    private val _street = MutableStateFlow("")
    val street = _street.asStateFlow()

    private val _city = MutableStateFlow("")
    val city = _city.asStateFlow()

    private val _zip = MutableStateFlow("")
    val zip = _zip.asStateFlow()

    private val _state = MutableStateFlow("")
    val state = _state.asStateFlow()

    private val _country = MutableStateFlow("")
    val country = _country.asStateFlow()

    private val _website = MutableStateFlow("")
    val website = _website.asStateFlow()

    // Bitmap for generated contact QR code
    private val _qrCodeBitmap = MutableStateFlow<Bitmap?>(null)
    val qrCodeBitmap = _qrCodeBitmap.asStateFlow()

    // Function to update the fields
    fun updateFirstName(newFirstName: String) {
        _firstName.value = newFirstName
    }

    fun updateLastName(newLastName: String) {
        _lastName.value = newLastName
    }

    fun updateMobile(newMobile: String) {
        _mobile.value = newMobile
    }

    fun updatePhone(newPhone: String) {
        _phone.value = newPhone
    }

    fun updateFax(newFax: String) {
        _fax.value = newFax
    }

    fun updateEmail(newEmail: String) {
        _email.value = newEmail
    }

    fun updateCompany(newCompany: String) {
        _company.value = newCompany
    }

    fun updateJob(newJob: String) {
        _job.value = newJob
    }

    fun updateStreet(newStreet: String) {
        _street.value = newStreet
    }

    fun updateCity(newCity: String) {
        _city.value = newCity
    }

    fun updateZip(newZip: String) {
        _zip.value = newZip
    }

    fun updateState(newState: String) {
        _state.value = newState
    }

    fun updateCountry(newCountry: String) {
        _country.value = newCountry
    }

    fun updateWebsite(newWebsite: String) {
        _website.value = newWebsite
    }

    // Function to generate the vCard QR code
    fun generateContactQrCode(
        firstName: String,
        lastName: String,
        mobile: String,
        phone: String,
        fax: String,
        email: String,
        company: String,
        job: String,
        street: String,
        city: String,
        zip: String,
        state: String,
        country: String,
        website: String,
        onSuccess: () -> Unit = {},
        onError: () -> Unit = {}
    ) {
        viewModelScope.launch {
            // Create the vCard format for contact
            val contactQrCodeData = """
                BEGIN:VCARD
                VERSION:3.0
                FN:$firstName $lastName
                TEL:$mobile
                TEL:$phone
                TEL:$fax
                EMAIL:$email
                ORG:$company
                TITLE:$job
                ADR:$street;$city;$state;$zip;$country
                URL:$website
                END:VCARD
            """.trimIndent()

            qrCodeRepository.generateQRCode(contactQrCodeData)
                ?.let {
                    _qrCodeBitmap.value = it
                    onSuccess()
                } ?: run {
                onError()
            }
        }
    }
}
