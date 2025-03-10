package com.lts360.compose.ui.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject


data class VerificationResult(val isValid:Boolean, val domain:String, val status:String,  val errorMessage:String?, val rejectedReason : String?=null)

@HiltViewModel
class DomainVerificationViewModel  @Inject constructor() : ViewModel() {

    private val _verificationResult = MutableStateFlow<VerificationResult?>(VerificationResult(true,
        "cinemapettai.com", "Rejected" ,"Your verification code is not found.", rejectedReason = "Low value content"))
    val verificationResult: StateFlow<VerificationResult?> = _verificationResult

    private val _domain = MutableStateFlow<String>(_verificationResult.value?.takeIf {
        it.isValid
    }?.domain ?: run { "" })
    val domain = _domain.asStateFlow()

    private val _domainError = MutableStateFlow<String?>(null)
    val domainError = _domainError.asStateFlow()

    private val _verificationCode = MutableStateFlow<String?>(null)
    val verificationCode: StateFlow<String?> = _verificationCode



    fun generateVerificationCode(domain: String) {
        if (domain.isBlank() && isValidDomain(domain)){
            _domainError.value = "Domain is empty or not valid domain"
            return
        }
        _domainError.value = null
        _verificationCode.value = UUID.randomUUID().toString().replace("-", "").take(20) // Shorten to 10 characters
    }

    // Verify domain by checking meta tag
    fun verifyDomain(domain: String) {
        viewModelScope.launch(Dispatchers.IO) {

        }
    }

    fun onDomainValueChange(domain: String) {
        _domain.value = domain.trim() // Trim spaces

        if (_domain.value.isNotBlank()) {
            when {
                _domain.value.length < 4 || _domain.value.length > 253 -> {
                    _domainError.value = "Domain length must be between 4 and 253 characters."
                }
                !isValidDomain(_domain.value) -> {
                    _domainError.value = "Domain must be top-level, e.g., example.com (Without http/https or www)"
                }
                else -> {
                    _domainError.value = null // No error
                }
            }
        }else{
            _domainError.value =null // No error
        }
    }



    // Function to validate a domain
    private fun isValidDomain(domain: String): Boolean {
        val domainRegex = """^((?!-)[A-Za-z0-9-]{1,63}(?<!-)\.)+[A-Za-z]{2,}$""".toRegex()
        return domain.matches(domainRegex)
    }

}
