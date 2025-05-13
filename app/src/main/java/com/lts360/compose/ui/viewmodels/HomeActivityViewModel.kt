package com.lts360.compose.ui.viewmodels

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.lts360.api.auth.managers.TokenManager
import com.lts360.app.database.daos.chat.MessageDao
import com.lts360.app.database.daos.notification.NotificationDao
import com.lts360.app.database.daos.prefs.BoardDao
import com.lts360.app.database.daos.profile.UserProfileDao
import com.lts360.app.database.models.app.Board
import com.lts360.app.workers.helpers.E2EEPublicTokenToServerWorkerHelper
import com.lts360.app.workers.helpers.SendFcmTokenWorkerHelper
import com.lts360.components.utils.LogUtils.TAG
import com.lts360.compose.ui.managers.NetworkConnectivityManager
import com.lts360.compose.ui.managers.UserSharedPreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class HomeActivityViewModel @Inject constructor(
    @ApplicationContext
    context: Context,
    val application: Application,
    val userProfileDao: UserProfileDao,
    val messageDao: MessageDao,
    val boardDao:BoardDao,
    notificationDao: NotificationDao,
    connectivityManager: NetworkConnectivityManager,
    tokenManager: TokenManager
) : ViewModel() {

    val userId = UserSharedPreferencesManager.userId

    val signInMethod = tokenManager.getSignInMethod()
    val isFeatureEnabled = tokenManager.isValidSignInMethodFeaturesEnabled()


    val isConnectedEvent = connectivityManager.isConnectedEvent

    val messageCount = messageDao.countAllUnreadMessagesFlow()

    val notificationCount: Flow<Int> = notificationDao.countAllUnreadNotifications()



    private val _boards = MutableStateFlow<List<Board>>(emptyList())
    val boards = _boards.asStateFlow()


    init {
        UserSharedPreferencesManager.initialize(context)
        viewModelScope.launch(Dispatchers.IO){
            boardDao.getAllBoardsFlow().collectLatest {
                _boards.value = it
            }
        }

    }

    init {
        UserSharedPreferencesManager.setFirstLaunchCompleted()

        if (tokenManager.isVerifiedUser()) {
            if (!UserSharedPreferencesManager.E2EEPublicTokenStatus) {
                sendE2EEEPublicTokenToServer()
            }

            if (!UserSharedPreferencesManager.fcmTokenStatus) {
                fetchAndSendFcmToken()
            }
        }
    }

    private fun fetchAndSendFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                sendTokenToServer(task.result)
            } else {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
            }
        }
    }



    fun isNotificationPermissionDismissed(): Boolean {
        return UserSharedPreferencesManager.isNotificationPermissionDismissed()
    }

    fun setNotificationPermissionDismissed() {
        UserSharedPreferencesManager.setNotificationPermissionDismissed()
    }

    private fun sendTokenToServer(fcmToken: String) {
        SendFcmTokenWorkerHelper.forceEnqueueSendFCMTokenToServerUniqueWork(application, fcmToken)
    }


    private fun sendE2EEEPublicTokenToServer() {
        E2EEPublicTokenToServerWorkerHelper.forceEnqueueSendE2EEPublicTokenToServerUniqueWork(application)
    }


}
