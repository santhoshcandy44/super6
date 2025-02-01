package com.super6.pot.api.auth

import android.content.Context
import com.super6.pot.BuildConfig
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
object AuthClient {

    private const val BASE_URL = BuildConfig.BASE_URL

    private var appContext: Context? = null

    // Set the application context to use in interceptors
    fun init(context: Context) {

        appContext = context.applicationContext
    }


    // Create a logging interceptor for debugging
    private val interceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Initialize a cookie manager
    private val cookieHandler = CookieManager().apply {
        CookieHandler.setDefault(this)
    }

    @Volatile
    private var retrofit: Retrofit? = null


    // Create OkHttpClient with interceptor and cookie management
    private fun createClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .addInterceptor(NoInternetInterceptor(appContext!!))
            .addNetworkInterceptor(interceptor)
            .cookieJar(JavaNetCookieJar(cookieHandler))
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)

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
}
