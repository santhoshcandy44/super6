package com.lts360.compose.ui.profile.viewmodels

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.lts360.api.app.AppClient
import com.lts360.api.common.CommonClient
import com.lts360.api.common.errors.ErrorResponse
import com.lts360.api.common.responses.ResponseReply
import com.lts360.api.auth.managers.socket.SocketManager
import com.lts360.api.Utils.Result
import com.lts360.api.Utils.mapExceptionToError
import com.lts360.api.app.ProfileSettingsService
import com.lts360.api.auth.services.CommonService
import com.lts360.app.database.daos.profile.UserProfileDao
import com.lts360.app.database.models.profile.UserProfileSettingsInfo
import com.lts360.compose.ui.managers.UserSharedPreferencesManager
import com.lts360.compose.ui.profile.repos.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject


@HiltViewModel
class ProfileSettingsViewModel @Inject constructor(
    @ApplicationContext
    context: Context,
    private val userProfileDao: UserProfileDao,
    private val userProfileRepository: UserProfileRepository,
    val socketManager: SocketManager
) : ViewModel() {

    // Retrieve the argument from the navigation
    val userId = UserSharedPreferencesManager.userId

    private val contentResolver = context.contentResolver

    private val filesDir = context.filesDir


    private val _profileImageBitmap = MutableStateFlow<Bitmap?>(null)
    val profileImageBitmap: StateFlow<Bitmap?> = _profileImageBitmap

    private val _isProfilePicLoading = MutableStateFlow(false)
    val isProfilePicLoading: StateFlow<Boolean> = _isProfilePicLoading

    // For Progress Bar
    private val _profileCompletionPercentage = MutableStateFlow(0f)
    val profileCompletionPercentage: StateFlow<Float> get() = _profileCompletionPercentage

    private val _profileHealthStatus = MutableStateFlow("")
    val profileHealthStatus: StateFlow<String> get() = _profileHealthStatus


    private val _userProfile = MutableStateFlow<UserProfileSettingsInfo?>(null)
    val userProfile = _userProfile.asStateFlow()


    private var errorMessage: String = ""


    init {
        viewModelScope.launch(Dispatchers.IO) {
            // Fetch and update UI with initial user profile
      /*      userProfileRepository.getUserProfileSettingsInfo(userId)?.let {
                _userProfile.value=it
                updateUI(it)
            }

*/

            // Collect updates from the flow and update UI


            userProfileRepository.getUserProfileSettingsInfoFlow(userId).collectLatest { userProfile ->
                _userProfile.value=userProfile
                userProfile?.let { updateUI(it) }
            }
        }


        viewModelScope.launch{
            userProfileRepository.fetchUserProfile(userId)
        }

    }


    // Helper function to update UI components
    private suspend fun updateUI(userProfileSettingsInfo: UserProfileSettingsInfo) {
        userProfileSettingsInfo.profile_pic_url?.let { updateProfilePicUrl(it) }

        val percentage = userProfileRepository.calculateCompletionPercentage(userProfileSettingsInfo)
        val status = userProfileRepository.determineProfileHealthStatus(userProfileSettingsInfo, percentage)
        updateProfileStatus(percentage, status)
    }




    private suspend fun updateProfilePicUrl(profilePicUrl: String?) {
        _profileImageBitmap.value = withContext(Dispatchers.IO) {
            try {
                BitmapFactory.decodeStream(
                    contentResolver.openInputStream(
                        Uri.parse(
                            profilePicUrl
                        )
                    )
                )
            } catch (e: Exception) {
                null // Handle the error gracefully
            }
        }
    }


    // Update the profile status based on current user data
    private fun updateProfileStatus(percentage: Int, status: String) {
        _profileCompletionPercentage.value = percentage / 100f
        _profileHealthStatus.value = status
    }


    fun onUploadImage(
        userId: Long,
        imagePart: MultipartBody.Part,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
    ) {

        viewModelScope.launch {

            try {
                _isProfilePicLoading.value = true

                when (val result = uploadImage(userId, imagePart)) { // Call the network function
                    is Result.Success -> {

                        val gsonData = Gson().fromJson(result.data.data, JsonObject::class.java)
                        val profilePicUrl = gsonData.get("profile_pic_url").asString
                        val updatedAt = gsonData.get("updated_at").asString

                        try {
                            val localFile = downloadImage(profilePicUrl)
                            val updatedProfilePicUrl = Uri.fromFile(localFile)
                                .toString() + "?timestamp=" + System.currentTimeMillis()

                            updateProfilePicUrl(updatedProfilePicUrl)
                            userProfileDao.updateProfilePicUrl(
                                userId,
                                updatedProfilePicUrl,
                                updatedAt
                            )
                            socketManager.getSocket()
                                .emit("chat:profilePicUpdated", JSONObject().apply {
                                    put("user_id", userId)
                                })

                            onSuccess(result.data.message)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            onError("Failed to update profile pic")
                        }

                    }

                    is Result.Error -> {
                        errorMessage = mapExceptionToError(result.error).errorMessage
                        onError(errorMessage)
                        // Handle the error and update the UI accordingly
                    }

                }
            } catch (t: Throwable) {
                errorMessage = "Something Went Wrong"
                onError(errorMessage)
                t.printStackTrace()
            } finally {
                _isProfilePicLoading.value = false // Reset loading state
            }
        }
    }


    private suspend fun uploadImage(
        userId: Long,
        imagePart: MultipartBody.Part,
    ): Result<ResponseReply> {
        return try {
            val response = AppClient.instance.create(ProfileSettingsService::class.java)
                .uploadProfileImage(userId, imagePart)

            if (response.isSuccessful) {

                val body = response.body()

                if (body != null && body.isSuccessful) {
                    Result.Success(body)

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
            Result.Error(Exception("Failed to update profile pic"))
        }
    }


    // Download the image from the server and store it locally
    private suspend fun downloadImage(imageUrl: String): File = withContext(Dispatchers.IO) {

        val profileDir = File(filesDir, "user/profile")
        // Check if the directory exists, if not create it
        if (!profileDir.exists()) {
            profileDir.mkdirs() // Create the necessary directories
        }
        val localFile = File(profileDir, "profile_pic.jpg")

        // Make the network request in the background using Retrofit
        val downloadImageResponse = CommonClient.rawInstance.create(CommonService::class.java)
            .downloadMedia(imageUrl)

        // Write the downloaded image to the local file
        downloadImageResponse.byteStream().use { inputStream ->
            // Save the image as a JPEG
            FileOutputStream(localFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }

        return@withContext localFile
    }

}

