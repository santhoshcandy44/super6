package com.lts360


import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.Configuration
import androidx.work.WorkManager
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.gif.AnimatedImageDecoder
import coil3.request.CachePolicy
import coil3.request.allowHardware
import com.lts360.api.app.AppClient
import com.lts360.api.auth.AuthClient
import com.lts360.api.auth.managers.TokenManager
import com.lts360.api.auth.managers.socket.SocketManager
import com.lts360.api.common.CommonClient
import com.lts360.app.AppExceptionHandler
import com.lts360.app.database.AppDatabase
import com.lts360.app.di.AppModule
import com.lts360.app.di.DatabaseModule
import com.lts360.app.di.databaseModule
import com.lts360.components.isAuthActivityInStack
import com.lts360.compose.ui.auth.AuthActivity
import com.lts360.compose.ui.main.MainActivity
import com.lts360.compose.ui.managers.NetworkConnectivityManager
import com.lts360.compose.ui.managers.UserSharedPreferencesManager
import com.lts360.compose.ui.settings.viewmodels.RegionalSettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.factory.KoinWorkerFactory
import org.koin.core.context.startKoin
import org.koin.ksp.generated.defaultModule
import org.koin.ksp.generated.module

class App : Application(), Configuration.Provider, SingletonImageLoader.Factory {

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setWorkerFactory(KoinWorkerFactory())
            .setDefaultProcessName(BuildConfig.APPLICATION_ID)
            .build()

    private val tokenManager: TokenManager by inject()
    private val regionalSettingsRepository: RegionalSettingsRepository by inject()
    private val appDatabase: AppDatabase by inject()
    private val socketManager: SocketManager by inject()
    private val networkConnectivityManager: NetworkConnectivityManager by inject()

    private var activityCount = 0
    private var job: Job? = null

    companion object {
        var isAppInForeground = false
    }

    var isInitialStart = true

    fun isAppInForeground(): Boolean {
        return isAppInForeground
    }

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(applicationContext)
            modules(defaultModule, databaseModule, DatabaseModule().module, AppModule().module)
        }
        if(BuildConfig.DEBUG){
            val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
            Thread.setDefaultUncaughtExceptionHandler(AppExceptionHandler(applicationContext, defaultHandler))
        }
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
        AppClient.init(this, tokenManager ,regionalSettingsRepository)
        /*        if (!Places.isInitialized()) {
                    Places.initialize(applicationContext, "<YOUR_GOOGLE_API_KEY>")
                }*/
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityResumed(activity: Activity) {}

            override fun onActivityPaused(activity: Activity) {}

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                isAppInForeground = true

                activityCount++

                if (activity is MainActivity && !activity.isChangingConfigurations) {

                    val isFeatureEnabled = tokenManager.isValidSignInMethodFeaturesEnabled()

                    if (isFeatureEnabled) {
                        CoroutineScope(Dispatchers.IO).launch{
                            socketManager.initSocket()
                        }
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
                activityCount--
                if (activityCount == 0) {
                    if (activity is MainActivity && !activity.isChangingConfigurations) {
                        if (tokenManager.isValidSignInMethod()) {
                            if (tokenManager.isValidSignInMethodFeaturesEnabled()) {
                                socketManager.destroySocket()
                                job?.cancel()
                                job = null
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


    fun reconnectSocket() {
        CoroutineScope(Dispatchers.IO).launch{
            socketManager.reconnect()
        }
    }

    fun destroySocketSocket() {
        socketManager.destroySocket()
    }

    suspend fun setIsInvalidSession(value: Boolean) {
        withContext(Dispatchers.Main) {

            val userId = UserSharedPreferencesManager.userId
            if(userId==-1L){
                return@withContext
            }

            withContext(Dispatchers.IO) {
                appDatabase.backupDatabase(applicationContext)
            }

            UserSharedPreferencesManager.isInvalidSession = value
            if (value) {
                WorkManager.getInstance(applicationContext).cancelAllWork()
                socketManager.destroySocket()

                networkConnectivityManager.unregisterNetworkCallback()
                job?.cancel() // Cancel the coroutine job
                job = null // Clear the job reference
                UserSharedPreferencesManager.clear()
                getSharedPreferences("FCM_MESSAGE_PARTS", MODE_PRIVATE)
                    .edit{ clear() }


                AppClient.clear()

                startActivity(Intent(applicationContext, AuthActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or if (!isAuthActivityInStack(applicationContext)) {
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
                    } else {
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                    }
                })

            }
        }
    }

    suspend fun logout(method: String) {

        withContext(Dispatchers.Main) {

            val userId = UserSharedPreferencesManager.userId

            if(userId==-1L){
                return@withContext
            }

            withContext(Dispatchers.IO) {
                appDatabase.backupDatabase(applicationContext)
            }

            WorkManager.getInstance(applicationContext).cancelAllWork()
            socketManager.destroySocket()
            networkConnectivityManager.unregisterNetworkCallback()
            job?.cancel() // Cancel the coroutine job
            job = null // Clear the job reference
            UserSharedPreferencesManager.clear()
            getSharedPreferences("FCM_MESSAGE_PARTS", MODE_PRIVATE)
                .edit{ clear() }
            tokenManager.logout(method)

            AppClient.clear()

            startActivity(Intent(applicationContext, AuthActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or if (!isAuthActivityInStack(applicationContext)) {
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
                } else {
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
            })

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
                add(AnimatedImageDecoder.Factory()) // Use ImageDecoder for Android P and above
            }
            .build()
    }


}


