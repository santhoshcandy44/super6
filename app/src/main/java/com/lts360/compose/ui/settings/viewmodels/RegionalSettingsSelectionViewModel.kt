package com.lts360.compose.ui.settings.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import com.lts360.compose.ui.settings.Country
import com.lts360.compose.ui.settings.Language
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.android.annotation.KoinViewModel
import javax.inject.Inject

@KoinViewModel
class RegionalSettingsSelectionViewModel @Inject constructor(
    val applicationContext: Context,
    private val regionalSettingsSelectionRepository: RegionalSettingsRepository
) : ViewModel() {

    val allLanguages = listOf(
        Language("en", "English"),
        Language("hi", "Hindi"),
        Language("bn", "Bengali"),
        Language("te", "Telugu"),
        Language("mr", "Marathi"),
        Language("ta", "Tamil"),
        Language("ur", "Urdu"),
        Language("gu", "Gujarati"),
        Language("kn", "Kannada"),
        Language("or", "Odia"),
        Language("pa", "Punjabi"),
        Language("as", "Assamese"),
        Language("ml", "Malayalam")
    )

    private val _selectedLanguage = MutableStateFlow<Language?>(null)
    val selectedLanguage = _selectedLanguage.asStateFlow()

    val allCountries = regionalSettingsSelectionRepository.countries

    private val _selectedCountry = MutableStateFlow<Country?>(null)
    val selectedCountry = _selectedCountry.asStateFlow()

    init {
        _selectedCountry.value = regionalSettingsSelectionRepository.getCountryFromPreferences()
    }

    fun selectLanguage(language: Language) {
        _selectedLanguage.value = language
    }

    fun selectCountry(country: Country) {
        _selectedCountry.value = country
        regionalSettingsSelectionRepository.saveCountryToPreferences(country.code)
    }

}


