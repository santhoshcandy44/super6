package com.lts360.compose.ui.settings.viewmodels

import android.content.Context
import android.telephony.TelephonyManager
import androidx.core.content.edit
import com.lts360.compose.ui.settings.Country
import org.koin.core.annotation.Factory

@Factory
class RegionalSettingsRepository (val context: Context) {

    private val telephonyManager =
        context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    private val simCountryCode = telephonyManager.simCountryIso
        ?.takeIf { it.isNotBlank() }
        ?.uppercase()
        ?: telephonyManager.networkCountryIso
            ?.takeIf { it.isNotBlank() }
            ?.uppercase()


    val countries = listOf(
        Country("IN", "India"),
        Country("US", "United States"),
        Country("GB", "United Kingdom"),
        Country("CA", "Canada"),
        Country("AU", "Australia"),
        Country("DE", "Germany"),
        Country("FR", "France"),
        Country("JP", "Japan"),
        Country("CN", "China"),
        Country("BR", "Brazil"),
        Country("RU", "Russia"),
        Country("IT", "Italy"),
        Country("ES", "Spain"),
        Country("NL", "Netherlands"),
        Country("MX", "Mexico"),
        Country("KR", "South Korea"),
        Country("ZA", "South Africa"),
        Country("SA", "Saudi Arabia"),
        Country("AE", "United Arab Emirates"),
        Country("SG", "Singapore"),
        Country("SE", "Sweden"),
        Country("CH", "Switzerland"),
        Country("TR", "Turkey"),
        Country("NZ", "New Zealand"),
        Country("NO", "Norway"),
        Country("AR", "Argentina"),
        Country("BD", "Bangladesh"),
        Country("ID", "Indonesia"),
        Country("PK", "Pakistan"),
        Country("MY", "Malaysia"),
        Country("VN", "Vietnam"),
        Country("PH", "Philippines"),
        Country("TH", "Thailand"),
        Country("UA", "Ukraine"),
        Country("PL", "Poland"),
        Country("IE", "Ireland"),
        Country("BE", "Belgium"),
        Country("AT", "Austria"),
        Country("DK", "Denmark")
    )

    fun saveCountryToPreferences(isoCountryCode: String) {

        val country = countries.firstOrNull { it.code == isoCountryCode } ?: Country("IN", "India")
        val sharedPreferences = context.getSharedPreferences("user_general_preferences", Context.MODE_PRIVATE)
        sharedPreferences.edit {
            putString("selected_country_code", country.code)
        }
    }

    fun getCountryFromPreferences(): Country? {
        val sharedPreferences =
            context.getSharedPreferences("user_general_preferences", Context.MODE_PRIVATE)
        val countryCode = sharedPreferences.getString("selected_country_code", null)

        return if (countryCode != null) {
            Country(countryCode, countries.firstOrNull { it.code == countryCode }?.name ?: "None")
        } else {
            null
        }
    }


    fun getCountryFromSim(): Country? {
        return simCountryCode?.let { code ->
            val name = countries.firstOrNull { it.code == code }?.name ?: "None"
            val country = Country(code, name)
            saveCountryToPreferences(code)
            country
        }
    }


}
