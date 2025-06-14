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
import com.lts360.api.utils.Result
import com.lts360.api.utils.mapExceptionToError
import com.lts360.api.app.ProfileSettingsService
import com.lts360.api.auth.services.CommonService
import com.lts360.app.database.daos.profile.UserProfileDao
import com.lts360.app.database.models.profile.UserProfileSettingsInfo
import com.lts360.compose.ui.managers.UserSharedPreferencesManager
import com.lts360.compose.ui.profile.repos.UserProfileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import androidx.core.net.toUri
import org.koin.android.annotation.KoinViewModel


@KoinViewModel
class ProfileSettingsViewModel(
    context: Context,
    private val userProfileDao: UserProfileDao,
    private val userProfileRepository: UserProfileRepository,
    val socketManager: SocketManager
) : ViewModel() {

    val userId = UserSharedPreferencesManager.userId

    private val contentResolver = context.contentResolver

    private val filesDir = context.filesDir

    private val _profileImageBitmap = MutableStateFlow<Bitmap?>(null)
    val profileImageBitmap = _profileImageBitmap.asStateFlow()

    private val _isProfilePicLoading = MutableStateFlow(false)
    val isProfilePicLoading = _isProfilePicLoading.asStateFlow()

    private val _profileCompletionPercentage = MutableStateFlow(0f)
    val profileCompletionPercentage = _profileCompletionPercentage.asStateFlow()

    private val _profileHealthStatus = MutableStateFlow("")
    val profileHealthStatus = _profileHealthStatus.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfileSettingsInfo?>(null)
    val userProfile = _userProfile.asStateFlow()

    private var errorMessage: String = ""


    init {
        viewModelScope.launch(Dispatchers.IO) {
            userProfileRepository.getUserProfileSettingsInfoFlow(userId)
                .collectLatest { userProfile ->
                    _userProfile.value = userProfile
                    userProfile?.let { updateUserProfileSettings(it) }
                }
        }

        viewModelScope.launch {
            userProfileRepository.fetchUserProfile(userId)
        }

    }


    private fun updateUserProfileSettings(userProfileSettingsInfo: UserProfileSettingsInfo) {
        viewModelScope.launch {
            userProfileSettingsInfo.profilePicUrl?.let { updateProfilePicUrl(it) }
            updateProfileHealthStatus(userProfileSettingsInfo)
        }
    }


    private suspend fun updateProfilePicUrl(profilePicUrl: String) {
        _profileImageBitmap.value = withContext(Dispatchers.IO) {
            try {
                BitmapFactory.decodeStream(contentResolver.openInputStream(profilePicUrl.toUri()))
            } catch (_: Exception) {
                null
            }
        }
    }


    private fun updateProfileHealthStatus(userProfileSettingsInfo: UserProfileSettingsInfo) {
        val percentage =
            userProfileRepository.calculateProfileCompletionPercentage(userProfileSettingsInfo)
        _profileCompletionPercentage.value = percentage / 100f
        _profileHealthStatus.value =  userProfileRepository.determineProfileHealthStatus(userProfileSettingsInfo, percentage)
    }


    fun onUploadProfileImage(
        userId: Long,
        imagePart: MultipartBody.Part,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
    ) {

        viewModelScope.launch {

            try {
                _isProfilePicLoading.value = true

                when (val result = uploadProfileImage(userId, imagePart)) {
                    is Result.Success -> {

                        val gsonData = Gson().fromJson(result.data.data, JsonObject::class.java)
                        val profilePicUrl = gsonData.get("profile_pic_url").asString
                        val updatedAt = gsonData.get("updated_at").asString

                        try {
                            val localFile = downloadProfileImage(profilePicUrl)
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
                    }

                }
            } catch (t: Throwable) {
                errorMessage = "Something Went Wrong"
                onError(errorMessage)
                t.printStackTrace()
            } finally {
                _isProfilePicLoading.value = false
            }
        }
    }


    private suspend fun uploadProfileImage(
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
                Result.Error(
                    Exception(
                        try {
                            Gson().fromJson(
                                response.errorBody()?.string(),
                                ErrorResponse::class.java
                            ).message
                        } catch (_: Exception) {
                            "An unknown error occurred"
                        }
                    )
                )

            }
        } catch (_: Throwable) {
            Result.Error(Exception("Failed to update profile pic"))
        }
    }


    private suspend fun downloadProfileImage(imageUrl: String): File = withContext(Dispatchers.IO) {

        val localFile = File(
            File(filesDir, "user/profile")
                .apply {
                    if (!exists()) {
                        mkdirs()
                    }
                }, "profile_pic.jpg"
        )

        CommonClient.rawInstance.create(CommonService::class.java)
            .downloadMedia(imageUrl).byteStream().use { inputStream ->
                FileOutputStream(localFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

        localFile
    }

}

