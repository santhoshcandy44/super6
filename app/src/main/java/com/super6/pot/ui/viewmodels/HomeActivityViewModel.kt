package com.super6.pot.ui.viewmodels

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.super6.pot.api.auth.managers.TokenManager
import com.super6.pot.api.models.service.GuestIndustryDao
import com.super6.pot.app.database.daos.chat.MessageDao
import com.super6.pot.app.database.daos.notification.NotificationDao
import com.super6.pot.app.database.daos.profile.UserProfileDao
import com.super6.pot.ui.managers.UserSharedPreferencesManager
import com.super6.pot.app.workers.helpers.E2EEPublicTokenToServerWorkerHelper
import com.super6.pot.app.workers.helpers.SendFcmTokenWorkerHelper
import com.super6.pot.ui.managers.NetworkConnectivityManager
import com.super6.pot.utils.LogUtils.TAG
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class HomeActivityViewModel @Inject constructor(
    @ApplicationContext
    context: Context,
    val application: Application,
    val userProfileDao: UserProfileDao,
    val messageDao: MessageDao,
    notificationDao: NotificationDao,
    connectivityManager: NetworkConnectivityManager,
    tokenManager: TokenManager,
    guestIndustryDao: GuestIndustryDao,
) : ViewModel() {


    val userId = UserSharedPreferencesManager.userId

    val signInMethod = tokenManager.getSignInMethod()
    val isFeatureEnabled = tokenManager.isValidSignInMethodFeaturesEnabled()


    val isConnectedEvent = connectivityManager.isConnectedEvent

    val messageCount = messageDao.countAllUnreadMessagesFlow()

    val notificationCount: Flow<Int> = notificationDao.countAllUnreadNotifications()


    private val _selectedIndustriesCount = MutableStateFlow<Int>(-1)
    val selectedIndustriesCount = _selectedIndustriesCount.asStateFlow()


    private val _bottomNavVisibility = MutableStateFlow(true)
    val bottomNavVisibility = _bottomNavVisibility.asStateFlow()



    init {

        UserSharedPreferencesManager.initialize(context)

        if (signInMethod == "guest") {
            viewModelScope.launch {
                // Update the StateFlow
                _selectedIndustriesCount.value = guestIndustryDao.countSelectedIndustries()
            }
        }


    }


    init {

        UserSharedPreferencesManager.setFirstLaunchCompleted()


        if(!UserSharedPreferencesManager.E2EEPublicTokenStatus &&  tokenManager.getSignInMethod() != "guest"){
            sendE2EEEPublicTokenToServer()
        }

        if (!UserSharedPreferencesManager.fcmTokenStatus && tokenManager.getSignInMethod() != "guest") {

            // After successful registration, get the FCM token
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val fcmToken = task.result
                    // Send the FCM token to your server after registration is complete
                    sendTokenToServer(fcmToken)
                } else {
                    Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                    // Handle the failure case
                }
            }

        }

    }

    fun updateBottomNavVisibility(value: Boolean){
        viewModelScope.launch {
            _bottomNavVisibility.value = value
        }
    }

    fun isIndustriesSheetDismissed(): Boolean {
        return UserSharedPreferencesManager.isIndustriesSheetDismissed()
    }


    fun setIndustriesSheetDismissed() {
        UserSharedPreferencesManager.setIndustriesSheetDismissed()
    }

    fun isNotificationPermissionDismissed(): Boolean {
        return UserSharedPreferencesManager.isNotificationPermissionDismissed()
    }

    fun setNotificationPermissionDismissed() {
        UserSharedPreferencesManager.setNotificationPermissionDismissed()
    }


    private fun sendTokenToServer(fcmToken: String) {
        SendFcmTokenWorkerHelper.cancelSendFCMTokenToServerUniqueWork(application)
        SendFcmTokenWorkerHelper.enqueueSendFCMTokenToServerWork(application, fcmToken)
    }



    private fun sendE2EEEPublicTokenToServer() {
        E2EEPublicTokenToServerWorkerHelper.cancelSendE2EEPublicTokenToServerUniqueWork(application)
        E2EEPublicTokenToServerWorkerHelper.enqueueSendE2EEPublicKey(application)
    }


}

