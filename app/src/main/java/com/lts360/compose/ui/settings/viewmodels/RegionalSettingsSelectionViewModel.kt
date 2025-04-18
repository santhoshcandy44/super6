package com.lts360.compose.ui.settings.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import com.lts360.compose.ui.settings.Country
import com.lts360.compose.ui.settings.Language
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class RegionalSettingsSelectionViewModel @Inject constructor(
    @ApplicationContext val context: Context,
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


