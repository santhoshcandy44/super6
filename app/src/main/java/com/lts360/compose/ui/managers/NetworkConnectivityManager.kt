package com.lts360.compose.ui.managers

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Handler
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkConnectivityManager @Inject constructor(@ApplicationContext context: Context) {



    private var _isConnectedEvent = MutableSharedFlow<Boolean>(1)
    val isConnectedEvent = _isConnectedEvent.asSharedFlow()


    var isConnectedInternet = false

    private var isFirstConnection = true

    private var networkCapabilities: NetworkCapabilities? = null

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager


    private val networkCallback = object : ConnectivityManager.NetworkCallback() {


        override fun onAvailable(network: Network) {
            networkCapabilities = connectivityManager.getNetworkCapabilities(network)

            networkCapabilities?.let {
                val isConnected = isInternetConnected(it)

                // If either Wi-Fi or Mobile Data is connected, post true
                if (isConnected) {
                    isConnectedInternet = true
                    if (isFirstConnection) {
                        isFirstConnection = false
                        return
                    } else {
                        CoroutineScope(Dispatchers.Main).launch{
                            _isConnectedEvent.emit(true)
                        }
                    }
                } else {
                    isConnectedInternet = false
                    CoroutineScope(Dispatchers.Main).launch{
                        _isConnectedEvent.emit(false)
                    }
                    isFirstConnection = false
                }

            } ?: run {

                isConnectedInternet = false
                CoroutineScope(Dispatchers.Main).launch {
                    _isConnectedEvent.emit(false)
                }
                isFirstConnection = false
            }
        }

        override fun onLost(network: Network) {
            isConnectedInternet = false
            CoroutineScope(Dispatchers.Main).launch{
                _isConnectedEvent.emit(false)
            }

        }


    }

    // Register network callback to listen for connectivity changes
   private val networkRequest = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .build()

    private var isNetworkCallbackRegistered = false


    init {


        val activeNetwork = connectivityManager.activeNetwork
        networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)

        if (networkCapabilities == null) {
            isConnectedInternet = false
            CoroutineScope(Dispatchers.Main).launch {
                _isConnectedEvent.emit(false)
            }
            isFirstConnection = false
        }
    }



    fun registerNetworkCallback(){
        if(isNetworkCallbackRegistered){
            return
        }
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
        isNetworkCallbackRegistered=true
    }

    fun unregisterNetworkCallback(){
        if(isNetworkCallbackRegistered){
            connectivityManager.unregisterNetworkCallback(networkCallback)
            isNetworkCallbackRegistered=false
        }
        // Reset state variables
        _isConnectedEvent.resetReplayCache()  // Reset the SharedFlow cache
        isConnectedInternet = false
        isFirstConnection = true
        networkCapabilities = null
    }


    private fun isInternetConnected(networkCapabilities: NetworkCapabilities): Boolean {
        return (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET))
    }


    fun checkForSeconds(handler: Handler, statusCallBack: (STATUS) -> Unit, milliseconds: Long) {

        var elapsedTime = 0

        val checkNetworkRunnable = object : Runnable {
            override fun run() {

                val isConnectedValue =  isConnectedInternet

                if (isConnectedValue) {
                    // Network is connected, perform action
                    statusCallBack(STATUS.STATUS_CONNECTED)
                } else {
                    statusCallBack(STATUS.STATUS_NOT_CONNECTED_INITIALLY)
                    // Network is not connected
                    elapsedTime += 1000

                    if (elapsedTime < milliseconds) {
                        // Check again in 1 second
                        handler.postDelayed(this, 1000)
                    } else {
                        statusCallBack(STATUS.STATUS_NOT_CONNECTED_ON_COMPLETED_JOB)
                    }
                }
            }
        }

        handler.post(checkNetworkRunnable)

    }


    enum class STATUS {
        STATUS_CONNECTED,
        STATUS_NOT_CONNECTED_INITIALLY,
        STATUS_NOT_CONNECTED_ON_COMPLETED_JOB
    }

}
