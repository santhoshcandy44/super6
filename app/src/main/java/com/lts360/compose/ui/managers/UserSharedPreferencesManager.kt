package com.lts360.compose.ui.managers

import android.content.Context
import android.content.SharedPreferences


object UserSharedPreferencesManager {

    private const val PREFS_NAME = "user"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_IS_FIRST_LAUNCH = "isFirstLaunch"
    private const val KEY_IS_NOTIFICATION_PERMISSION_DISMISSED = "npd"
    private const val KEY_IS_INDUSTRIES_SHEET_DISMISSED = "is"

    private const val KEY_FCM_TOKEN = "is_fcm_token_updated" // Added for FCM token

    private const val IS_INVALID_SESSION = "is_invalid_session" // Added for FCM token


    private const val KEY_E2EEE_PUBLIC_TOKEN = "is_e2ee_public_token_updated" // Added for FCM token
    private const val KEY_E2EEE_KEY_VERSION = "e2ee_key_version" // Added for FCM token

    private lateinit var sharedPreferences: SharedPreferences

    fun initialize(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    var userId: Long
        get() = sharedPreferences.getLong(KEY_USER_ID, -1)
        set(value) = sharedPreferences.edit().putLong(KEY_USER_ID, value).apply()

    fun removeUserId() {
        sharedPreferences.edit().remove(KEY_USER_ID).apply()
    }

    var fcmTokenStatus: Boolean
        get() = sharedPreferences.getBoolean(KEY_FCM_TOKEN, false)
        set(value) = sharedPreferences.edit().putBoolean(KEY_FCM_TOKEN, value).apply()



    var isInvalidSession: Boolean
        get() = sharedPreferences.getBoolean(IS_INVALID_SESSION, false)
        set(value) = sharedPreferences.edit().putBoolean(IS_INVALID_SESSION, value).apply()


    var E2EEPublicTokenStatus: Boolean
        get() = sharedPreferences.getBoolean(KEY_E2EEE_PUBLIC_TOKEN, false)
        set(value) = sharedPreferences.edit().putBoolean(KEY_E2EEE_PUBLIC_TOKEN, value).apply()



    var E2EELatestKeyVersion: Long
        get() = sharedPreferences.getLong(KEY_E2EEE_KEY_VERSION, -1L)
        set(value) = sharedPreferences.edit().putLong(KEY_E2EEE_KEY_VERSION,value).apply()




    fun removeE2EEKeyVersion() {
        sharedPreferences.edit().remove(KEY_E2EEE_PUBLIC_TOKEN).apply()
    }

    fun removeE2EEPublicToken() {
        sharedPreferences.edit().remove(KEY_E2EEE_PUBLIC_TOKEN).apply()
    }

    fun removeFcmToken() {
        sharedPreferences.edit().remove(KEY_FCM_TOKEN).apply()
    }


    fun setFirstLaunchCompleted() {
        sharedPreferences.edit()
            .putBoolean(KEY_IS_FIRST_LAUNCH, false)
            .apply()
    }
    fun isFirstLaunch(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_FIRST_LAUNCH, true)
    }

    fun setNotificationPermissionDismissed() {
        sharedPreferences.edit()
            .putBoolean(KEY_IS_NOTIFICATION_PERMISSION_DISMISSED, true)
            .apply()
    }

    fun isNotificationPermissionDismissed():Boolean {
        return sharedPreferences.getBoolean(KEY_IS_NOTIFICATION_PERMISSION_DISMISSED, false)

    }


    fun setIndustriesSheetDismissed() {
        sharedPreferences.edit()
            .putBoolean(KEY_IS_INDUSTRIES_SHEET_DISMISSED, true)
            .apply()
    }

    fun isIndustriesSheetDismissed():Boolean {
        return sharedPreferences.getBoolean(KEY_IS_INDUSTRIES_SHEET_DISMISSED, false)

    }

    fun clear() {
        sharedPreferences.edit().clear().apply()
    }


}