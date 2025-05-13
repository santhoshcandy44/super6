package com.lts360.compose.ui.managers

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.lts360.compose.ui.managers.UserSharedPreferencesManager.sharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton


object UserSharedPreferencesManager {

    private const val PREFS_NAME = "user"

    private const val KEY_USER_ID = "user_id"

    private const val KEY_FCM_TOKEN = "is_fcm_token_updated"

    private const val IS_INVALID_SESSION = "is_invalid_session"

    private const val KEY_E2EEE_PUBLIC_TOKEN = "is_e2ee_public_token_updated"
    private const val KEY_E2EEE_KEY_VERSION = "e2ee_key_version"

    private const val KEY_IS_FIRST_LAUNCH = "isFirstLaunch"

    private const val KEY_IS_NOTIFICATION_PERMISSION_DISMISSED = "npd"

    private lateinit var sharedPreferences: SharedPreferences


    fun initialize(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    var userId: Long
        get() = sharedPreferences.getLong(KEY_USER_ID, -1)
        set(value) = sharedPreferences.edit { putLong(KEY_USER_ID, value) }

    var fcmTokenStatus: Boolean
        get() = sharedPreferences.getBoolean(KEY_FCM_TOKEN, false)
        set(value) = sharedPreferences.edit { putBoolean(KEY_FCM_TOKEN, value) }

    var isInvalidSession: Boolean
        get() = sharedPreferences.getBoolean(IS_INVALID_SESSION, false)
        set(value) = sharedPreferences.edit { putBoolean(IS_INVALID_SESSION, value) }

    var E2EEPublicTokenStatus: Boolean
        get() = sharedPreferences.getBoolean(KEY_E2EEE_PUBLIC_TOKEN, false)
        set(value) = sharedPreferences.edit { putBoolean(KEY_E2EEE_PUBLIC_TOKEN, value) }

    var E2EELatestKeyVersion: Long
        get() = sharedPreferences.getLong(KEY_E2EEE_KEY_VERSION, -1L)
        set(value) = sharedPreferences.edit { putLong(KEY_E2EEE_KEY_VERSION, value) }

    fun removeE2EEKeyVersion() {
        sharedPreferences.edit { remove(KEY_E2EEE_PUBLIC_TOKEN) }
    }

    fun removeE2EEPublicToken() {
        sharedPreferences.edit { remove(KEY_E2EEE_PUBLIC_TOKEN) }
    }

    fun removeFcmToken() {
        sharedPreferences.edit { remove(KEY_FCM_TOKEN) }
    }

    fun setFirstLaunchCompleted() {
        sharedPreferences.edit {
            putBoolean(KEY_IS_FIRST_LAUNCH, false)
        }
    }

    fun isFirstLaunch(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_FIRST_LAUNCH, true)
    }

    fun setNotificationPermissionDismissed() {
        sharedPreferences.edit {
            putBoolean(KEY_IS_NOTIFICATION_PERMISSION_DISMISSED, true)
        }
    }

    fun isNotificationPermissionDismissed(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_NOTIFICATION_PERMISSION_DISMISSED, false)

    }

    fun clear() {
        sharedPreferences.edit { clear() }
    }


}




private val Context.userGeneralDataStore by preferencesDataStore(name = "user_general_preferences")

@Singleton
class UserGeneralPreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private val KEY_LOCAL_JOB_ATTENTION_PROMPT_VISIBILITY = booleanPreferencesKey("local_job_attention_prompt_visibility")
    }

    val isDontAskAgainChecked: Flow<Boolean> = context.userGeneralDataStore.data
        .map { preferences ->
            preferences[KEY_LOCAL_JOB_ATTENTION_PROMPT_VISIBILITY] == true
        }

    suspend fun setLocalJobPersonalInfoPromptIsDontAskAgainChecked(value: Boolean) {
        context.userGeneralDataStore.edit { preferences ->
            preferences[KEY_LOCAL_JOB_ATTENTION_PROMPT_VISIBILITY] = value
        }
    }
}