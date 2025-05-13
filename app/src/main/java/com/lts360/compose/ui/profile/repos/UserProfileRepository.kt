package com.lts360.compose.ui.profile.repos

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import com.google.gson.Gson
import com.lts360.api.app.AppClient
import com.lts360.api.app.ProfileService
import com.lts360.api.auth.services.CommonService
import com.lts360.api.common.CommonClient
import com.lts360.api.common.errors.ErrorResponse
import com.lts360.api.models.service.UserProfileInfo
import com.lts360.app.database.daos.profile.UserLocationDao
import com.lts360.app.database.daos.profile.UserProfileDao
import com.lts360.app.database.models.profile.UserLocation
import com.lts360.app.database.models.profile.UserProfile
import com.lts360.app.database.models.profile.UserProfileSettingsInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject

class UserProfileRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userProfileDao: UserProfileDao,
    private val userLocationDao: UserLocationDao
) {


    fun getUserProfileFlow(userId: Long): Flow<UserProfile?> {
        return userProfileDao.getProfileFlow(userId)
    }

    fun getUserProfileSettingsInfoFlow(userId: Long): Flow<UserProfileSettingsInfo?> {
        return userProfileDao.getUserProfileSettingsInfoFlow(userId)
    }

    fun calculateProfileCompletionPercentage(userProfileSettingsInfo: UserProfileSettingsInfo): Int {
        var fieldsCompleted = 0
        val totalFields = 4

        if (userProfileSettingsInfo.firstName.isNotEmpty()) fieldsCompleted++
        if (!userProfileSettingsInfo.profilePicUrl.isNullOrEmpty()) fieldsCompleted++
        if (userProfileSettingsInfo.email.isNotEmpty()) fieldsCompleted++
        /*
                if (userProfileSettingsInfo.phone.isNullOrEmpty()) fieldsCompleted++
        */
        if (!userProfileSettingsInfo.about.isNullOrEmpty()) fieldsCompleted++

        return (fieldsCompleted * 100) / totalFields
    }

    fun isProfileCompleted(userProfileSettingsInfo: UserProfileSettingsInfo) =
        calculateProfileCompletionPercentage(userProfileSettingsInfo)


    fun unCompletedProfileFields(userProfileSettingsInfo: UserProfileSettingsInfo): List<String> {
        val uncompletedFields = mutableListOf<String>()
        if (userProfileSettingsInfo.email.isNotEmpty()) {
            uncompletedFields.add("EMAIL")
        }
        if (userProfileSettingsInfo.phoneCountryCode.isNullOrEmpty()
            || userProfileSettingsInfo.phoneNumber.isNullOrEmpty()) {
            uncompletedFields.add("PHONE")
        }
        return uncompletedFields
    }

    fun determineProfileHealthStatus(profile: UserProfileSettingsInfo, percentage: Int): String {
        return when {
            profile.email.isEmpty() -> "Poor"
            percentage == 100 -> "Good"
            else -> "Weak"
        }
    }

    suspend fun fetchUserProfile(userId: Long) {
        try {
            AppClient.instance.create(ProfileService::class.java)
                .getUserProfile(userId)
                .let {
                    if (it.isSuccessful) {
                        val body = it.body()

                        if (body != null && it.isSuccessful) {

                            val data = Gson().fromJson(body.data, UserProfileInfo::class.java)

                            val existingLocation = userProfileDao.getUserProfileDetails(userId)
                                ?.userLocation

                            val existingProfile = userProfileDao.getUserProfileDetails(userId)
                                ?.userProfile

                            val profilePicUrl = existingProfile?.profilePicUrl

                            val uri = profilePicUrl?.toUri()
                            val pathWithoutParams = uri?.path

                            if (existingProfile == null ||
                                pathWithoutParams.isNullOrEmpty() ||
                                !File(pathWithoutParams).exists() ||
                                existingProfile.updatedAt != data.updatedAt
                            ) {

                                val imageUrl = data.profilePicUrl

                                val localFile = File(
                                    File(context.filesDir, "user/profile")
                                        .apply {
                                            if (!exists()) {
                                                mkdirs()
                                            }
                                        }, "profile_pic.jpg"
                                )
                                var updatedProfilePicUrl: String? = null

                                try {

                                    if (imageUrl == null) {
                                        throw IOException("Profile pic url is null")
                                    }

                                    val downloadImageResponse =
                                        CommonClient.rawInstance.create(CommonService::class.java)
                                            .downloadMedia(imageUrl)

                                    withContext(Dispatchers.IO) {
                                        downloadImageResponse.byteStream().use { inputStream ->
                                            FileOutputStream(localFile).use { outputStream ->
                                                inputStream.copyTo(outputStream)
                                            }
                                        }
                                    }
                                    updatedProfilePicUrl = Uri.fromFile(localFile)
                                        .toString() + "?timestamp=" + System.currentTimeMillis()

                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }

                                val userProfileData = UserProfile(
                                    userId = userId,
                                    firstName = data.firstName,
                                    lastName = data.lastName,
                                    email = data.email,
                                    isEmailVerified = data.isEmailVerified,
                                    phoneCountryCode = data.phoneCountryCode,
                                    phoneNumber = data.phoneNumber,
                                    isPhoneVerified = data.isPhoneVerified,
                                    profilePicUrl = updatedProfilePicUrl,
                                    accountType = data.accountType,
                                    createdAt = data.createdAt,
                                    updatedAt = if (updatedProfilePicUrl != null) data.updatedAt else existingProfile?.updatedAt,
                                    about = data.about
                                )

                                existingProfile?.let {
                                    userProfileDao.update(userProfileData)
                                } ?: run {
                                    userProfileDao.insert(userProfileData)
                                }

                            }

                            data.location?.let {

                                if (existingLocation == null || existingLocation.updatedAt != it.updatedAt) {

                                    userLocationDao.insert(
                                        UserLocation(
                                            userId = userId,
                                            locationType = it.locationType,
                                            latitude = it.latitude,
                                            longitude = it.longitude,
                                            geo = it.geo,
                                            updatedAt = it.updatedAt
                                        )
                                    )
                                }
                            }

                        } else {
                            throw Exception("Failed, try again later...")
                        }
                    } else {
                        throw Exception(
                            try {
                                Gson().fromJson(
                                    it.errorBody()?.string(),
                                    ErrorResponse::class.java
                                ).message
                            } catch (_: Exception) {
                                "An unknown error occurred"
                            }
                        )
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}
