package com.super6.pot.ui.auth.repos

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.gson.Gson
import com.super6.pot.api.Utils.Result
import com.super6.pot.api.app.AccountSettingsService
import com.super6.pot.api.app.AppClient
import com.super6.pot.api.app.ProfileSettingsService
import com.super6.pot.api.auth.AuthClient
import com.super6.pot.api.auth.managers.TokenManager
import com.super6.pot.api.auth.services.AuthService
import com.super6.pot.api.auth.services.CommonService
import com.super6.pot.api.common.CommonClient
import com.super6.pot.api.common.errors.ErrorResponse
import com.super6.pot.api.common.responses.ResponseReply
import com.super6.pot.api.models.service.UserProfileInfo
import com.super6.pot.app.database.daos.profile.UserProfileDao
import com.super6.pot.app.database.models.profile.UserLocation
import com.super6.pot.app.database.models.profile.UserProfile
import com.super6.pot.app.database.daos.profile.UserLocationDao
import com.super6.pot.ui.managers.UserSharedPreferencesManager
import com.super6.pot.utils.LogUtils.TAG
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.UUID
import javax.inject.Inject

class AuthRepository @Inject constructor(
    @ApplicationContext
    context: Context,
    private val tokenManager: TokenManager,
    val userProfileDao: UserProfileDao,
    val userLocationDao: UserLocationDao,
) {


    private val filesDir = context.filesDir


    suspend fun googleSignInOAuth(
        context: Context,
        success: (String) -> Unit,
        failure: (String) -> Unit,
    ) {
        try {
            val rawNonce = UUID.randomUUID().toString()

            val hashedNonce = try {
                val md = MessageDigest.getInstance("SHA-256")
                val bytes = rawNonce.toByteArray()
                val digest = md.digest(bytes)
                digest.fold("") { str, it -> str + "%02x".format(it) }
            } catch (e: NoSuchAlgorithmException) {
                failure("Failed to log in try again")
                return
            }

            val credentialManager = CredentialManager.create(context)
            val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId("841124425720-of6bpub4q0evdncg3ttfob98r9jtg8n6.apps.googleusercontent.com")
                .setAutoSelectEnabled(true)
                .setNonce(hashedNonce)
                .build()


            val request: GetCredentialRequest = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                request = request,
                context = context,
            )
            val credential = result.credential


            val googleIdTokenCredential = GoogleIdTokenCredential
                .createFrom(credential.data)

            val idToken = googleIdTokenCredential.idToken

            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
            FirebaseAuth.getInstance().signInWithCredential(firebaseCredential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        success(idToken)
                        // Handle successful sign-in (e.g., navigate to another activity)
                    } else {
                        failure("Failed to sign in try again")
                    }
                }


            // Process the Google ID token credential as needed
        } catch (e: GetCredentialException) {
            failure("Failed to log in try again")
        } catch (e: GoogleIdTokenParsingException) {
            failure("Failed to log in try again")
        } catch (e: IOException) {
            // Handle network issues, such as no connectivity
            failure("Network error. Please check your internet connection and try again.")
        } catch (e: Exception) {
            // Handle any other unexpected exceptions
            failure("An unexpected error occurred. Please try again.")
        }

    }


    suspend fun updateProfileIfNeeded(userProfile: UserProfileInfo) {
        val existingLocation = userProfileDao.getUserProfileDetails(userProfile.userId)
            ?.userLocation

        val existingProfile = userProfileDao.getUserProfileDetails(userProfile.userId)
            ?.userProfile


        if (existingProfile == null || existingProfile.updatedAt != userProfile.updatedAt) {
            // Check if profilePicUrl is not null and download the image
            val updatedProfilePicUrl = if (userProfile.profilePicUrl != null) {
                downloadAndSaveProfileImage(userProfile.profilePicUrl)
            } else {
                null
            }

            val profileUpdatedAt = if (updatedProfilePicUrl != null) userProfile.updatedAt else existingProfile?.updatedAt
            val userProfileData =
                createUserProfile(userProfile, updatedProfilePicUrl, profileUpdatedAt)
            userProfileDao.insert(userProfileData)

        }


        userProfile.location?.let {

            if (existingLocation == null || existingLocation.updatedAt != it.updatedAt) {
                userLocationDao.insert(
                    UserLocation(
                        userId = userProfile.userId,
                        locationType = it.locationType,
                        latitude = it.latitude,
                        longitude = it.longitude,
                        geo = it.geo,
                        updatedAt = it.updatedAt
                    )
                )
            }
        }



    }

    private fun createUserProfile(
        userProfile: UserProfileInfo,
        updatedProfilePicUrl: String?,
        profileUpdatedAt: String?,
    ): UserProfile {
        return UserProfile(
            userId = userProfile.userId,
            firstName = userProfile.firstName,
            lastName = userProfile.lastName,
            email = userProfile.email,
            profilePicUrl = updatedProfilePicUrl,
            accountType = userProfile.accountType,
            createdAt = userProfile.createdAt,
            updatedAt = profileUpdatedAt,
            about = userProfile.about
        )
    }

    private suspend fun downloadAndSaveProfileImage(imageUrl: String): String? {

        return try {
            val profileDir = File(filesDir, "user/profile")
            // Check if the directory exists, if not create it
            if (!profileDir.exists()) {
                profileDir.mkdirs() // Create the necessary directories
            }
            val localFile = File(profileDir, "profile_pic.jpg")
            // Make the network request in the background using Retrofit
            val response = CommonClient.rawInstance.create(CommonService::class.java)
                .downloadImage(imageUrl)

            // Write the downloaded image to the local file
            withContext(Dispatchers.IO) {
                response.byteStream().use { inputStream ->
                    // Save the image as a JPEG
                    FileOutputStream(localFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            }


            // Return the URI string with timestamp for cache invalidation
            Uri.fromFile(localFile).toString() + "?timestamp=" + System.currentTimeMillis()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    fun saveGoogleSignInInfo(accessToken: String, refreshToken: String) {
        tokenManager.saveSignInMethod("google")
        tokenManager.saveAccessToken(accessToken)
        tokenManager.saveRefreshToken(refreshToken)
    }


    fun saveGuestSignInInfo() {
        tokenManager.saveSignInMethod("guest")
    }


    fun saveEmailSignInInfo(accessToken: String, refreshToken: String) {
        tokenManager.saveSignInMethod("legacy_email")
        tokenManager.saveAccessToken(accessToken)
        tokenManager.saveRefreshToken(refreshToken)
    }


    fun saveUserId(userId: Long) {
        UserSharedPreferencesManager.userId = userId
    }


    suspend fun onRegisterReSendEmailVerificationOTP(
        email: String,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit,
    ) {


        when (val result = registerReSendEmailVerificationOTP(email)) { // Call the network function
            is Result.Success -> {
                onSuccess()
                // Handle success
                // Proceed to next step or navigate to OTP screen
            }

            is Result.Error -> {
                onError(result.error)
                // Handle the error and update the UI accordingly
            }
        }

    }


    suspend fun onEditEmailReSendEmailVerificationOTPValidUser(
        userId: Long,
        email: String,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit,
    ) {


        when (val result = editEmailReSendOtpVerificationValidUser(
            userId,
            email
        )) { // Call the network function
            is Result.Success -> {
                onSuccess()
                // Handle success
                // Proceed to next step or navigate to OTP screen
            }

            is Result.Error -> {
                onError(result.error)
                // Handle the error and update the UI accordingly
            }
        }

    }


    suspend fun onProtectedForgotPasswordReSendEmailOtpVerificationValidUser(
        userId: Long,
        email: String,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit,
    ) {


        when (val result = protectedForgotPasswordReSendOtpVerificationValidUser(
            userId,
            email
        )) { // Call the network function
            is Result.Success -> {
                onSuccess()
                // Handle success
                // Proceed to next step or navigate to OTP screen
            }

            is Result.Error -> {
                onError(result.error)
                // Handle the error and update the UI accordingly
            }
        }

    }


    suspend fun onForgotPasswordReSendEmailOtpVerification(
        email: String,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit,
    ) {


        when (val result = forgotPasswordReSendOtpVerification(
            email
        )) { // Call the network function
            is Result.Success -> {
                onSuccess()
                // Handle success
                // Proceed to next step or navigate to OTP screen
            }

            is Result.Error -> {
                onError(result.error)
                // Handle the error and update the UI accordingly
            }
        }

    }


    private suspend fun registerReSendEmailVerificationOTP(email: String): Result<ResponseReply> {

        return try {
            val response =
                AuthClient.instance.create(AuthService::class.java).sendEmailVerificationOTP(email)

            if (response.isSuccessful) {
                val resendOTPResponse = response.body()

                if (resendOTPResponse != null && resendOTPResponse.isSuccessful) {
                    Result.Success(resendOTPResponse)

                } else {

                    val errorMessage = "Failed, try again later..."
                    Result.Error(Exception(errorMessage))
                }


            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = try {
                    Gson().fromJson(errorBody, ErrorResponse::class.java).message
                } catch (e: Exception) {
                    "An unknown error occurred"
                }
                Result.Error(Exception(errorMessage))
            }
        } catch (t: Throwable) {

            Result.Error(t)

        }


    }


    private suspend fun forgotPasswordReSendOtpVerification(email: String): Result<ResponseReply> {

        return try {
            val response = AppClient.instance.create(AuthService::class.java)
                .validateEmailForgotPassword(email)

            if (response.isSuccessful) {
                val resendOTPResponse = response.body()

                if (resendOTPResponse != null && resendOTPResponse.isSuccessful) {
                    Result.Success(resendOTPResponse)

                } else {

                    val errorMessage = "Failed, try again later..."
                    Result.Error(Exception(errorMessage))
                }


            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = try {
                    Gson().fromJson(errorBody, ErrorResponse::class.java).message
                } catch (e: Exception) {
                    "An unknown error occurred"
                }
                Result.Error(Exception(errorMessage))
            }
        } catch (t: Throwable) {

            Result.Error(t)

        }


    }


    private suspend fun protectedForgotPasswordReSendOtpVerificationValidUser(
        userId: Long,
        email: String,
    ): Result<ResponseReply> {

        return try {
            val response = AppClient.instance.create(AccountSettingsService::class.java)
                .validateEmailForgotPassword(userId, email)

            if (response.isSuccessful) {
                val resendOTPResponse = response.body()

                if (resendOTPResponse != null && resendOTPResponse.isSuccessful) {
                    Result.Success(resendOTPResponse)

                } else {

                    val errorMessage = "Failed, try again later..."
                    Result.Error(Exception(errorMessage))
                }


            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = try {
                    Gson().fromJson(errorBody, ErrorResponse::class.java).message
                } catch (e: Exception) {
                    "An unknown error occurred"
                }
                Result.Error(Exception(errorMessage))
            }
        } catch (t: Throwable) {

            Result.Error(t)

        }


    }

    private suspend fun editEmailReSendOtpVerificationValidUser(
        userId: Long,
        email: String,
    ): Result<ResponseReply> {

        return try {
            val response = AppClient.instance.create(ProfileSettingsService::class.java)
                .changeEmailValidate(userId, email)

            if (response.isSuccessful) {
                val resendOTPResponse = response.body()

                if (resendOTPResponse != null && resendOTPResponse.isSuccessful) {
                    Result.Success(resendOTPResponse)

                } else {

                    val errorMessage = "Failed, try again later..."
                    Result.Error(Exception(errorMessage))
                }


            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = try {
                    Gson().fromJson(errorBody, ErrorResponse::class.java).message
                } catch (e: Exception) {
                    "An unknown error occurred"
                }
                Result.Error(Exception(errorMessage))
            }
        } catch (t: Throwable) {

            Result.Error(t)

        }
    }
}
