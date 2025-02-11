package com.super6.pot.api.auth.managers

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.firebase.auth.FirebaseAuth
import com.super6.pot.components.utils.LogUtils.TAG
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject


class TokenManager @Inject constructor(@ApplicationContext  context: Context) {


    companion object{

        private const val  SIGN_IN_METHOD= "sign_in_method"
        private const val ACCESS_TOKEN_KEY = "access_token"
        private const val REFRESH_TOKEN_KEY = "refresh_token"

    }

    private val masterKey: MasterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()


    private val encryptedSharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "encrypted_data_store",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )


    fun getSignInMethod():String{
        return encryptedSharedPreferences.getString(SIGN_IN_METHOD, "") ?: ""
    }



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

    // Get access token
    fun getAccessToken(): String {
        return encryptedSharedPreferences.getString(ACCESS_TOKEN_KEY, "") ?: ""
    }

    // Get refresh token
    fun getRefreshToken(): String {
        return encryptedSharedPreferences.getString(REFRESH_TOKEN_KEY, "") ?: ""
    }

    // Save access token
    fun saveAccessToken(token: String?) {
        encryptedSharedPreferences.edit().putString(ACCESS_TOKEN_KEY, token).apply()
    }

    // Save refresh token
    fun saveRefreshToken(token: String?) {
        encryptedSharedPreferences.edit().putString(REFRESH_TOKEN_KEY, token).apply()
    }


    // Save refresh token
    fun saveSignInMethod(method: String) {
        encryptedSharedPreferences.edit().putString(SIGN_IN_METHOD, method).apply()
    }


    // Clear tokens
    private fun cleaSignInTokens() {
        encryptedSharedPreferences.edit()
            .remove(ACCESS_TOKEN_KEY)
            .remove(REFRESH_TOKEN_KEY)
            .remove(SIGN_IN_METHOD)
            .apply()
    }


    // Logout method to clear tokens
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
