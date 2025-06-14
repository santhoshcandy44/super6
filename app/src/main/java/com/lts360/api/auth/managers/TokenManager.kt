package com.lts360.api.auth.managers

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.firebase.auth.FirebaseAuth
import com.lts360.components.utils.LogUtils.TAG
import androidx.core.content.edit
import org.koin.core.annotation.Factory

@Factory
class TokenManager (applicationContext: Context) {

    companion object{
        private const val  SIGN_IN_METHOD= "sign_in_method"
        private const val ACCESS_TOKEN_KEY = "access_token"
        private const val REFRESH_TOKEN_KEY = "refresh_token"
    }

    private val masterKey: MasterKey = MasterKey.Builder(applicationContext)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val encryptedSharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        applicationContext,
        "encrypted_data_store",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )




    fun getSignInMethod():String{
        return encryptedSharedPreferences.getString(SIGN_IN_METHOD, "") ?: ""
    }

    fun isGuest() = getSignInMethod() == "guest"

    fun isVerifiedUser():Boolean{
        val signInMethod = getSignInMethod()
        return  signInMethod=="google" || signInMethod=="legacy_email"
    }


    fun isValidSignInMethodFeaturesEnabled():Boolean{
        val signInMethod = getSignInMethod()
        return  signInMethod=="google" || signInMethod=="legacy_email"
    }

    fun isValidSignInMethod():Boolean{
        val signInMethod =getSignInMethod()
        return signInMethod=="google" || signInMethod=="legacy_email" || signInMethod== "guest"
    }

    fun getAccessToken(): String {
        return encryptedSharedPreferences.getString(ACCESS_TOKEN_KEY, "") ?: ""
    }

    fun getRefreshToken(): String {
        return encryptedSharedPreferences.getString(REFRESH_TOKEN_KEY, "") ?: ""
    }

    fun saveAccessToken(token: String?) {
        encryptedSharedPreferences.edit { putString(ACCESS_TOKEN_KEY, token) }
    }

    fun saveRefreshToken(token: String?) {
        encryptedSharedPreferences.edit { putString(REFRESH_TOKEN_KEY, token) }
    }


    fun saveSignInMethod(method: String) {
        encryptedSharedPreferences.edit { putString(SIGN_IN_METHOD, method) }
    }


    private fun cleaSignInTokens() {
        encryptedSharedPreferences.edit {
            remove(ACCESS_TOKEN_KEY)
                .remove(REFRESH_TOKEN_KEY)
                .remove(SIGN_IN_METHOD)
        }
    }


    fun logout(method: String) {
        when(method){
            "google"->{
                val firebaseAuth = FirebaseAuth.getInstance()
                firebaseAuth.signOut() // Sign out from Firebase
                cleaSignInTokens()
                Log.d(TAG, "User has been logged out")
            }
            "legacy_email"->{
                cleaSignInTokens()
                Log.d(TAG, "User has been logged out")
            }
            "guest"->{
                Log.d(TAG, "Guest user has been logged out")
            }
            else ->{
                Log.d(TAG, "Unknown user has been logged out")
            }
        }
    }
}
