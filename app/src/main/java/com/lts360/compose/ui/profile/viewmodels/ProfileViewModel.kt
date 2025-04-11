package com.lts360.compose.ui.profile.viewmodels

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lts360.app.database.models.profile.UserProfile
import com.lts360.compose.ui.main.navhosts.routes.UserProfileSerializer
import com.lts360.compose.ui.managers.UserSharedPreferencesManager
import com.lts360.compose.ui.profile.repos.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import androidx.core.net.toUri


@HiltViewModel
class ProfileViewModel @Inject constructor(
    @ApplicationContext
    context: Context,
    private val userProfileRepository: UserProfileRepository,
    val savedStateHandle: SavedStateHandle,
) : ViewModel() {


    val userId = UserSharedPreferencesManager.userId


    private val contentResolver: ContentResolver = context.contentResolver

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> get() = _userProfile


    private val _profileImageBitmap = MutableStateFlow<Bitmap?>(null)
    val profileImageBitmap: StateFlow<Bitmap?> = _profileImageBitmap

    init {

        val savedUserProfile = savedStateHandle.get<String>("userProfile")
            ?.let { UserProfileSerializer.deserializeUserProfile(it) }

        if (savedUserProfile != null) {
            _userProfile.value = savedUserProfile
            viewModelScope.launch {
                savedUserProfile.profilePicUrl?.let {
                    updateProfilePicUrl(it)
                }
            }
        } else {

            viewModelScope.launch(Dispatchers.IO){
                userProfileRepository.getUserProfileFlow(userId).collectLatest { userProfile ->
                    _userProfile.value = userProfile
                    userProfile?.let {
                        userProfile.profilePicUrl?.let {
                            updateProfilePicUrl(it)
                        }
                        savedStateHandle["userProfile"] = UserProfileSerializer.serializeUserProfile(userProfile)
                    }

                }

            }

            viewModelScope.launch {
                userProfileRepository.fetchUserProfile(userId)
            }
        }

    }


    private suspend fun updateProfilePicUrl(profilePicUrl: String) {

        _profileImageBitmap.value = withContext(Dispatchers.IO) {
            try {
                BitmapFactory.decodeStream(contentResolver.openInputStream(profilePicUrl.toUri()))
            } catch (e: Exception) {
                null
            }
        }
    }


}

