package com.super6.pot.api.app

import com.super6.pot.App
import com.super6.pot.BuildConfig
import com.super6.pot.api.auth.AuthInterceptor
import com.super6.pot.api.auth.managers.TokenManager
import com.super6.pot.api.Utils.NoInternetInterceptor
import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.CookieHandler
import java.net.CookieManager
import java.util.concurrent.TimeUnit


// Retrofit client with OkHttp and token refresh logic
object AppClient {

    private const val BASE_URL = BuildConfig.BASE_URL

    // Create a logging interceptor for debugging
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    // Initialize a cookie manager
    private val cookieHandler = CookieManager().apply {
        CookieHandler.setDefault(this)
    }

    @Volatile
    private var retrofit: Retrofit? = null


    private lateinit var tokenManager: TokenManager
    private lateinit var appContext: App

    fun init(app: App, tokenManager: TokenManager) {
        appContext = app
        AppClient.tokenManager =tokenManager
    }

    private var interceptor: AuthInterceptor?=null



    // Create OkHttpClient with interceptor and cookie management
    private fun createClient(): OkHttpClient {
        interceptor = AuthInterceptor(appContext, tokenManager)
        val builder = OkHttpClient.Builder()
            .cookieJar(JavaNetCookieJar(cookieHandler))
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(NoInternetInterceptor(appContext.applicationContext))
            .addInterceptor(interceptor!!)
            .addInterceptor(loggingInterceptor)

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

    // Access the Retrofit instance
    val instance: Retrofit
        get() = getRetrofitInstance()

    fun clear() {
        interceptor?.clearJobs()
        retrofit = null // Clear the retrofit instance
    }

}



