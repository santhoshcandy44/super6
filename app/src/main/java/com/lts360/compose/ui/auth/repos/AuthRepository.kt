package com.lts360.compose.ui.auth.repos

import android.content.Context
import android.net.Uri
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.gson.Gson
import com.lts360.BuildConfig
import com.lts360.api.utils.Result
import com.lts360.api.app.AccountSettingsService
import com.lts360.api.app.AppClient
import com.lts360.api.app.ProfileSettingsService
import com.lts360.api.auth.AuthClient
import com.lts360.api.auth.managers.TokenManager
import com.lts360.api.auth.services.AuthService
import com.lts360.api.auth.services.CommonService
import com.lts360.api.common.CommonClient
import com.lts360.api.common.errors.ErrorResponse
import com.lts360.api.common.responses.ResponseReply
import com.lts360.api.models.service.UserProfileInfo
import com.lts360.app.database.daos.prefs.BoardDao
import com.lts360.app.database.daos.profile.UserLocationDao
import com.lts360.app.database.daos.profile.UserProfileDao
import com.lts360.app.database.models.profile.UserLocation
import com.lts360.app.database.models.profile.UserProfile
import com.lts360.compose.ui.managers.UserSharedPreferencesManager
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
    val boardDao: BoardDao
) {

    private val filesDir = context.filesDir

    suspend fun googleSignInOAuth(
        context: Context,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
    ) {
        try {
            val rawNonce = UUID.randomUUID().toString()

            val hashedNonce = try {
                val md = MessageDigest.getInstance("SHA-256")
                val bytes = rawNonce.toByteArray()
                val digest = md.digest(bytes)
                digest.fold("") { str, it -> str + "%02x".format(it) }
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
                onError("Failed to log in try again")
                return
            }

            val credentialManager = CredentialManager.create(context)


            val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(BuildConfig.GOOGLE_SIGN_IN_OAUTH_WEB_CLIENT_ID)
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
            onSuccess(idToken)
        } catch (e: GetCredentialException) {
            e.printStackTrace()
            onError("Failed to log in try again")
        } catch (e: GoogleIdTokenParsingException) {
            e.printStackTrace()
            onError("Failed to log in try again")
        } catch (_: IOException) {
            onError("Network error. Please check your internet connection and try again.")
        } catch (_: Exception) {
            onError("An unexpected error occurred. Please try again.")
        }

    }

    suspend fun updateProfileIfNeeded(userProfile: UserProfileInfo) {
        val existingLocation = userProfileDao.getUserProfileDetails(userProfile.userId)
            ?.userLocation

        val existingProfile = userProfileDao.getUserProfileDetails(userProfile.userId)
            ?.userProfile


        if (existingProfile == null || existingProfile.updatedAt != userProfile.updatedAt) {
            val updatedProfilePicUrl = userProfile.profilePicUrl?.let {
                downloadAndSaveProfileImage(it)
            }

            val userProfileData = UserProfile(
                userId = userProfile.userId,
                firstName = userProfile.firstName,
                lastName = userProfile.lastName,
                email = userProfile.email,
                isEmailVerified = userProfile.isEmailVerified,
                phoneCountryCode = userProfile.phoneCountryCode,
                phoneNumber = userProfile.phoneNumber,
                isPhoneVerified = userProfile.isPhoneVerified,
                profilePicUrl = updatedProfilePicUrl,
                accountType = userProfile.accountType,
                createdAt = userProfile.createdAt,
                updatedAt = if (updatedProfilePicUrl != null) userProfile.updatedAt else existingProfile?.updatedAt,
                about = userProfile.about
            )

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

    private suspend fun downloadAndSaveProfileImage(imageUrl: String): String? {

        return try {
            val profileDir = File(filesDir, "user/profile")
            if (!profileDir.exists()) {
                profileDir.mkdirs()
            }
            val localFile = File(profileDir, "profile_pic.jpg")
            val response = CommonClient.rawInstance.create(CommonService::class.java)
                .downloadMedia(imageUrl)

            withContext(Dispatchers.IO) {
                response.byteStream().use { inputStream ->
                    FileOutputStream(localFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            }

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
                } catch (_: Exception) {
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
                } catch (_: Exception) {
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
                } catch (_: Exception) {
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
                } catch (_: Exception) {
                    "An unknown error occurred"
                }
                Result.Error(Exception(errorMessage))
            }
        } catch (t: Throwable) {

            Result.Error(t)

        }
    }
}
