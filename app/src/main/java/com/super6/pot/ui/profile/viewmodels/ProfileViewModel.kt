package com.super6.pot.ui.profile.viewmodels

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.super6.pot.app.database.models.profile.UserProfile
import com.super6.pot.ui.main.navhosts.routes.UserProfileSerializer
import com.super6.pot.ui.managers.UserSharedPreferencesManager
import com.super6.pot.ui.profile.repos.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


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
                updateProfilePicUrl(savedUserProfile.profilePicUrl)
            }
        } else {
            // Collect the user profile from the repository
            viewModelScope.launch(Dispatchers.IO){
                userProfileRepository.getUserProfileFlow(userId).collectLatest { userProfile ->
                    _userProfile.value = userProfile
                    userProfile?.let {
                        updateProfilePicUrl(userProfile.profilePicUrl)
                        savedStateHandle["userProfile"] = UserProfileSerializer.serializeUserProfile(userProfile)
                    }

                }

            }

            viewModelScope.launch {
                userProfileRepository.fetchUserProfile(userId)
            }
        }

    }


    private suspend fun updateProfilePicUrl(profilePicUrl: String?) {

        _profileImageBitmap.value = withContext(Dispatchers.IO) {
            // Delay for 1 minute (60,000 milliseconds)
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


}

