package com.super6.pot


import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.hilt.work.HiltWorkerFactory
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.Configuration
import androidx.work.WorkManager
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.gif.AnimatedImageDecoder
import coil3.gif.GifDecoder
import coil3.imageLoader
import coil3.request.CachePolicy
import coil3.request.allowHardware
import com.super6.pot.api.app.AppClient
import com.super6.pot.api.common.CommonClient
import com.super6.pot.api.auth.AuthClient
import com.super6.pot.api.auth.managers.TokenManager
import com.super6.pot.ui.auth.AuthActivity
import com.super6.pot.ui.main.MainActivity
import com.super6.pot.api.auth.managers.socket.SocketManager
import com.super6.pot.app.AppExceptionHandler
import com.super6.pot.app.database.AppDatabase
import com.super6.pot.ui.managers.UserSharedPreferencesManager
import com.super6.pot.ui.managers.NetworkConnectivityManager
import com.super6.pot.utils.LogUtils.TAG
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okio.Path.Companion.toOkioPath
import java.io.File
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), Configuration.Provider, SingletonImageLoader.Factory {


    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setWorkerFactory(workerFactory)
            .setDefaultProcessName(BuildConfig.APPLICATION_ID)
            .build()

    // TokenManager or AuthManager to handle token logic
    @Inject
    lateinit var tokenManager: TokenManager

    @Inject
    lateinit var appDatabase: AppDatabase

    @Inject
    lateinit var socketManager: SocketManager

    @Inject
    lateinit var networkConnectivityManager: NetworkConnectivityManager


    private var activityCount = 0

    private var job: Job? = null


    companion object {
        var isAppInForeground = false

    }

    var isInitialStart = true


    // Returns whether the app is in the foreground
    fun isAppInForeground(): Boolean {
        return isAppInForeground
    }


    lateinit var chatUsersImageLoader: ImageLoader
        private set

    override fun onCreate() {
        super.onCreate()


        if(BuildConfig.DEBUG){
            // Set the global exception handler
            val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
            Thread.setDefaultUncaughtExceptionHandler(AppExceptionHandler(applicationContext, defaultHandler))
        }


        chatUsersImageLoader =  ImageLoader.Builder(this)
            .diskCache {
                DiskCache.Builder()
                    .directory(File(filesDir, "chat_users_profile_image_files").toOkioPath())
                    .maxSizePercent(0.02)
                    .build()
            }
            .diskCachePolicy(CachePolicy.ENABLED)  // Enable disk cache
            .memoryCachePolicy(CachePolicy.ENABLED)  // Enable memory cache
            .allowHardware(false)
            .components {
                // Add appropriate decoder based on the Android version
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    add(AnimatedImageDecoder.Factory()) // Use ImageDecoder for Android P and above
                } else {
                    add(GifDecoder.Factory()) // Use GifDecoder for versions below Android P
                }
            }
            .build()


        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {

            override fun onStart(owner: LifecycleOwner) {
                super.onStart(owner)
                isAppInForeground = true

                if (!isInitialStart) {
                    if (tokenManager.isValidSignInMethodFeaturesEnabled()) {
                        reconnectSocket()
                    }
                }
                isInitialStart = false
            }

            override fun onStop(owner: LifecycleOwner) {
                super.onStop(owner)

                isAppInForeground = false
                (applicationContext as App).destroySocketSocket()
            }
        })

        UserSharedPreferencesManager.initialize(applicationContext)
        AuthClient.init(applicationContext)
        CommonClient.init(applicationContext)
        AppClient.init(this, tokenManager)
        /*        if (!Places.isInitialized()) {
                    Places.initialize(applicationContext, "<YOUR_GOOGLE_API_KEY>")
                }*/
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {


            override fun onActivityResumed(activity: Activity) {}

            override fun onActivityPaused(activity: Activity) {}

            // Implement other lifecycle methods as needed
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                isAppInForeground = true

                activityCount++ // Increment when an activity is created

                if (activity is MainActivity && !activity.isChangingConfigurations) {

                    val isFeatureEnabled = tokenManager.isValidSignInMethodFeaturesEnabled()

                    if (isFeatureEnabled) {
                        socketManager.getSocket()
                    }
                }



                if (activity is MainActivity && !activity.isChangingConfigurations) {


                    if (tokenManager.isValidSignInMethod()) {

                        networkConnectivityManager.registerNetworkCallback()

                        if (tokenManager.isValidSignInMethodFeaturesEnabled()) {
                            job = CoroutineScope(Dispatchers.Main).launch {
                                networkConnectivityManager.isConnectedEvent.collectLatest { connectionState ->
                                    if (connectionState && isAppInForeground()) {
                                        reconnectSocket()
                                    }
                                }
                            }
                        }
                    }
                }
            }

            override fun onActivityStarted(activity: Activity) {}

            override fun onActivityStopped(activity: Activity) {}

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}


            override fun onActivityDestroyed(activity: Activity) {
                activityCount-- // Decrement when an activity is destroyed


                if (activityCount == 0) {
                    if (activity is MainActivity && !activity.isChangingConfigurations) {
                        if (tokenManager.isValidSignInMethod()) {
                            if (tokenManager.isValidSignInMethodFeaturesEnabled()) {
                                socketManager.destroySocket()
                                job?.cancel() // Cancel the coroutine job
                                job = null // Clear the job reference
                            }
                            networkConnectivityManager.unregisterNetworkCallback()
                        }
                        AppClient.clear()
                        isAppInForeground = false
                    }

                }

            }
        })
    }


    // Function to clear the memory cache manually
    fun clearMemoryCache(context: Context) {
        val imageLoader = context.imageLoader
        // Clear the memory cache
        imageLoader.memoryCache?.clear()
    }


    fun reconnectSocket() {
        socketManager.reconnect()
    }


    fun destroySocketSocket() {
        socketManager.destroySocket()
    }


    suspend fun setIsInvalidSession(value: Boolean) {
        withContext(Dispatchers.Main) {
            UserSharedPreferencesManager.isInvalidSession = value
            if (value) {
                WorkManager.getInstance(applicationContext).cancelAllWork()
                socketManager.destroySocket()
                networkConnectivityManager.unregisterNetworkCallback()
                job?.cancel() // Cancel the coroutine job
                job = null // Clear the job reference
                AppClient.clear()
                val intent = Intent(applicationContext, AuthActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
            } else {
                UserSharedPreferencesManager.clear()
                getSharedPreferences("FCM_MESSAGE_PARTS", MODE_PRIVATE)
                    .edit().clear().apply()
                withContext(Dispatchers.IO) {
                    appDatabase.backupDatabase(applicationContext)
                }
            }
        }
    }

    suspend fun logout(method: String) {


        // Navigate to AuthActivity
        withContext(Dispatchers.Main) {
            WorkManager.getInstance(applicationContext).cancelAllWork()
            socketManager.destroySocket()
            networkConnectivityManager.unregisterNetworkCallback()
            job?.cancel() // Cancel the coroutine job
            job = null // Clear the job reference
            UserSharedPreferencesManager.clear()
            getSharedPreferences("FCM_MESSAGE_PARTS", MODE_PRIVATE)
                .edit().clear().apply()

            tokenManager.logout(method)
            withContext(Dispatchers.IO) {
                appDatabase.backupDatabase(applicationContext)
            }
            AppClient.clear()
            val intent = Intent(applicationContext, AuthActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

            startActivity(intent)

        }

    }

    override fun newImageLoader(context: PlatformContext): ImageLoader {

        // Configure the custom ImageLoader
        return ImageLoader.Builder(context)
            .diskCachePolicy(CachePolicy.ENABLED)  // Enable disk cache
            .memoryCachePolicy(CachePolicy.ENABLED)  // Enable memory cache
            .allowHardware(false)
            .components {
                // Add appropriate decoder based on the Android version
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    add(AnimatedImageDecoder.Factory()) // Use ImageDecoder for Android P and above
                } else {
                    add(GifDecoder.Factory()) // Use GifDecoder for versions below Android P
                }
            }
            .build()
    }


}


