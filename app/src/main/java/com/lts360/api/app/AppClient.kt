package com.lts360.api.app

import com.lts360.App
import com.lts360.BuildConfig
import com.lts360.api.auth.AuthInterceptor
import com.lts360.api.auth.managers.TokenManager
import com.lts360.api.utils.CountryHeaderInterceptor
import com.lts360.api.utils.NoInternetInterceptor
import com.lts360.compose.ui.settings.viewmodels.RegionalSettingsRepository
import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.CookieHandler
import java.net.CookieManager
import java.util.concurrent.TimeUnit

object AppClient {

    private const val BASE_URL = BuildConfig.BASE_URL



    @Volatile
    private var retrofit: Retrofit? = null

    @Volatile
    private var mediaDownloadRetrofit: Retrofit? = null

    private lateinit var tokenManager: TokenManager
    private lateinit var regionalSettingsRepository: RegionalSettingsRepository
    private lateinit var appContext: App

    fun init(app: App, tokenManager: TokenManager, repository: RegionalSettingsRepository) {
        appContext = app
        AppClient.tokenManager =tokenManager
        regionalSettingsRepository = repository
    }

    private var interceptor: AuthInterceptor?=null

    // Create OkHttpClient with interceptor and cookie management
    private fun createClient(): OkHttpClient {
        interceptor = AuthInterceptor(appContext, tokenManager)
        val builder = OkHttpClient.Builder()
            .cookieJar(JavaNetCookieJar(CookieManager().apply {
                CookieHandler.setDefault(this)
            }
            ))
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(NoInternetInterceptor(appContext.applicationContext))
            .addInterceptor(interceptor!!)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }).addInterceptor(CountryHeaderInterceptor(regionalSettingsRepository))

        return builder.build()
    }

    // Create OkHttpClient with interceptor and cookie management
    private fun createMediaDownloadClient(): OkHttpClient {
        interceptor = AuthInterceptor(appContext, tokenManager)
        val builder = OkHttpClient.Builder()
            .cookieJar(JavaNetCookieJar(CookieManager().apply {
                CookieHandler.setDefault(this)
            }
            ))
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(NoInternetInterceptor(appContext.applicationContext))
            .addInterceptor(interceptor!!)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.HEADERS
            })

        return builder.build()
    }



    // Get or create the Retrofit instance
    private fun getRetrofitInstance(): Retrofit {
        if (retrofit == null) {
            synchronized(this) {
                if (retrofit == null) {
                    retrofit = Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .client(createClient())
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
                }
            }
        }
        return retrofit!!
    }

    // Get or create the Retrofit instance
    private fun getMediaDownloadRetrofitInstance(): Retrofit {
        if (mediaDownloadRetrofit == null) {
            synchronized(this) {
                if (mediaDownloadRetrofit == null) {
                    mediaDownloadRetrofit = Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .client(createMediaDownloadClient())
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
                }
            }
        }
        return mediaDownloadRetrofit!!
    }

    // Access the Retrofit instance
    val instance: Retrofit
        get() = getRetrofitInstance()

    val mediaDownloadInstance: Retrofit
        get() = getMediaDownloadRetrofitInstance()

    fun clear() {
        interceptor?.clearJobs()
        retrofit = null // Clear the retrofit instance
        mediaDownloadRetrofit = null // Clear the retrofit instance
    }

}

