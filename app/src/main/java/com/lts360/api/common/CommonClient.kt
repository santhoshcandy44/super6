package com.lts360.api.common

import android.content.Context
import com.lts360.BuildConfig
import com.lts360.api.utils.NoInternetInterceptor
import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.CookieHandler
import java.net.CookieManager
import java.util.concurrent.TimeUnit


object CommonClient {
    private const val BASE_URL = BuildConfig.BASE_URL


    private var appContext: Context? = null

    // Set the application context to use in interceptors
    fun init(context: Context) {
        appContext = context.applicationContext
    }


    // Create a logging interceptor for debugging
    private val interceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.HEADERS
    }

    // Initialize a cookie manager
    private val cookieHandler = CookieManager().apply {
        CookieHandler.setDefault(this)
    }

    @Volatile
    private var retrofit: Retrofit? = null

    @Volatile
    private var rawRetrofit: Retrofit? = null


    // Create OkHttpClient with interceptor and cookie management
    private fun createClient(): OkHttpClient {

        val builder = OkHttpClient.Builder()
            .addInterceptor(NoInternetInterceptor(appContext!!))
            .addNetworkInterceptor(interceptor)
            .cookieJar(JavaNetCookieJar(cookieHandler))
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)


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
    private fun getRawRetrofitInstance(): Retrofit {
        if (rawRetrofit == null) {
            synchronized(this) {
                if (rawRetrofit == null) {
                    rawRetrofit = Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .client(createClient())
                        .build()
                }
            }
        }
        return rawRetrofit!!
    }

    // Access the Retrofit instance
    val instance: Retrofit
        get() = getRetrofitInstance()

    val rawInstance: Retrofit
        get() = getRawRetrofitInstance()


    fun clear() {
        retrofit = null // Clear the retrofit instance
    }

}