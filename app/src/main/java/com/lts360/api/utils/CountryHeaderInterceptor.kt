package com.lts360.api.utils

import com.lts360.api.auth.AuthClient
import com.lts360.api.auth.services.AuthService
import com.lts360.api.common.responses.ResponseReply
import com.lts360.compose.ui.settings.Country
import com.lts360.compose.ui.settings.viewmodels.RegionalSettingsRepository
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import retrofit2.Response
import java.io.IOException


class CountryHeaderInterceptor (private val regionalSettings: RegionalSettingsRepository) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val request = chain.request()

        var countryCode = regionalSettings.getCountryFromPreferences()?.code
            ?: regionalSettings.getCountryFromSim()?.code

        if (countryCode.isNullOrBlank()) {
            runBlocking {
                try {
                    val response = getIPCountry()
                    if (response.isSuccessful) {
                        val body = response.body()?.data
                        if (body != null) {

                            val json = Json {
                                ignoreUnknownKeys = true
                            }

                            val country = json.decodeFromString<Country>(body)
                            regionalSettings.saveCountryToPreferences(country.code)

                            countryCode = regionalSettings.getCountryFromPreferences()?.code
                        } else {
                            throw IOException("Unexpected behaviour")
                        }
                    } else {
                        throw IOException("Unexpected behaviour")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    throw IOException("Unexpected behaviour")
                }
            }
        }

        // Attach country code if available
        val newRequest = if (!countryCode.isNullOrBlank()) {
            request.newBuilder()
                .addHeader("X-Country-Code", countryCode!!)
                .build()
        } else {
            throw IOException("Unexpected behaviour")
        }

        return chain.proceed(newRequest)
    }

    private suspend fun getIPCountry(): Response<ResponseReply> {
        return AuthClient.instance.create(AuthService::class.java).getIPCountry()
    }
}

