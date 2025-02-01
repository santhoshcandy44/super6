package com.super6.pot.ui.profile.repos

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.gson.Gson
import com.super6.pot.api.app.AppClient
import com.super6.pot.api.app.ProfileService
import com.super6.pot.api.auth.services.CommonService
import com.super6.pot.api.common.CommonClient
import com.super6.pot.api.common.errors.ErrorResponse
import com.super6.pot.api.models.service.UserProfileInfo
import com.super6.pot.app.database.daos.profile.UserProfileDao
import com.super6.pot.app.database.models.profile.UserLocation
import com.super6.pot.app.database.models.profile.UserProfile
import com.super6.pot.app.database.models.profile.UserProfileSettingsInfo
import com.super6.pot.app.database.daos.profile.UserLocationDao
import com.super6.pot.utils.LogUtils.TAG
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
    // Method to fetch user profile
    suspend fun fetchUserProfile(userId: Long) {

        try {
            val response = AppClient.instance.create(ProfileService::class.java)
                .getUserProfile(userId)

            if(response.isSuccessful){
                val body = response.body()

                if (body != null && response.isSuccessful) {


                    val data = Gson().fromJson(body.data, UserProfileInfo::class.java)



                    val existingLocation = userProfileDao.getUserProfileDetails(userId)
                        ?.userLocation

                    val existingProfile = userProfileDao.getUserProfileDetails(userId)
                        ?.userProfile

                    val profilePicUrl = existingProfile?.profilePicUrl

                    val uri = profilePicUrl?.let { Uri.parse(it) }
                    // Get the actual file path without query parameters
                    val pathWithoutParams = uri?.path // This will return the path without removing valid '?' in filenames


                    if (existingProfile == null ||
                        pathWithoutParams.isNullOrEmpty() ||
                        !File(pathWithoutParams).exists() ||
                        existingProfile.updatedAt != data.updatedAt){

                        val imageUrl = data.profilePicUrl
                        val profileDir = File(getFilesDir(), "user/profile")
                        // Check if the directory exists, if not create it
                        if (!profileDir.exists()) {
                            profileDir.mkdirs() // Create the necessary directories
                        }
                        val localFile = File(profileDir, "profile_pic.jpg")
                        var updatedProfilePicUrl: String? = null


                        try {

                            if(imageUrl==null){
                                throw IOException("Profile pic url is null")
                            }

                            // Make the network request in the background using Retrofit
                            val downloadImageResponse = CommonClient.rawInstance.create(
                                CommonService::class.java)
                                .downloadImage(imageUrl)

                            // Write the downloaded image to the local file
                            withContext(Dispatchers.IO) {
                                downloadImageResponse.byteStream().use { inputStream ->
                                    // Save the image as a JPEG
                                    FileOutputStream(localFile).use { outputStream ->
                                        inputStream.copyTo(outputStream)
                                    }
                                }
                            }
                            // Return the URI string with timestamp for cache invalidation
                            updatedProfilePicUrl= Uri.fromFile(localFile).toString() + "?timestamp=" + System.currentTimeMillis()


                        } catch (e: Exception) {
                            Log.e(TAG, "Error downloading/saving image: ${e.message}")
                            e.printStackTrace()
                        }


                        val userProfileData = UserProfile(
                            userId = userId,
                            firstName = data.firstName,
                            lastName = data.lastName,
                            email = data.email,
                            profilePicUrl = updatedProfilePicUrl,
                            accountType = data.accountType,
                            createdAt = data.createdAt,
                            updatedAt = if (updatedProfilePicUrl != null) data.updatedAt else existingProfile?.updatedAt,
                            about = data.about
                        )
                        if(existingProfile!=null)
                            userProfileDao.update(userProfileData)
                        else
                            userProfileDao.insert(userProfileData)

                    }

                    data.location?.let {

                        if (existingLocation == null || existingLocation.updatedAt !=it.updatedAt) {

                            userLocationDao.insert(
                                UserLocation(
                                userId=userId,
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
            }else{

                val errorBody = response.errorBody()?.string()
                val errorMessage = try {
                    Gson().fromJson(errorBody, ErrorResponse::class.java).message
                } catch (e: Exception) {
                    "An unknown error occurred"
                }

                throw Exception(errorMessage)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    // Method to get user profile as LiveData from database
    fun getUserProfileFlow(id: Long): Flow<UserProfile?> {
        return userProfileDao.getProfileFlow(id)
    }

    // Method to get user profile as LiveData from database
    fun getUserProfile(id: Long): UserProfile? {
        return userProfileDao.getProfile(id)
    }

    // Method to get user profile as LiveData from database
    fun getUserProfileSettingsInfoFlow(id: Long): Flow<UserProfileSettingsInfo?> {
        return userProfileDao.getUserProfileSettingsInfoFlow(id)
    }


    // Method to get user profile as LiveData from database
    fun getUserProfileSettingsInfo(id: Long): UserProfileSettingsInfo? {
        return userProfileDao.getUserProfileSettingsInfo(id)
    }


    // Calculate completion percentage based on profile fields
    fun calculateCompletionPercentage(profile: UserProfileSettingsInfo): Int {
        var fieldsCompleted = 0
        val totalFields = 4 // Total number of fields to check

        if (profile.first_name.isNotEmpty()) fieldsCompleted++
//        if (profile.lastName?.isNotEmpty()) fieldsCompleted++
        if (!profile.profile_pic_url.isNullOrEmpty()) fieldsCompleted++

        if (profile.email.isNotEmpty()) fieldsCompleted++


        if (!profile.about.isNullOrEmpty()) fieldsCompleted++

        return (fieldsCompleted * 100) / totalFields
    }

    // Determine the profile health status based on fields and percentage
    fun determineProfileHealthStatus(profile: UserProfileSettingsInfo, percentage: Int): String {
        return when {
            profile.email.isEmpty() -> "Poor" // Email is required
            percentage == 100 -> "Good" // All fields completed
            else -> "Weak" // Some fields are missing
        }
    }


    private fun getFilesDir(): File {
        return context.filesDir
    }

}
