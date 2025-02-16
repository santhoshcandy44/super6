package com.lts360.compose.ui.viewmodels

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lts360.api.auth.managers.TokenManager
import com.lts360.app.database.models.profile.UserProfile
import com.lts360.compose.ui.managers.UserSharedPreferencesManager
import com.lts360.compose.ui.profile.repos.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject




@HiltViewModel
class MoreViewModel @Inject constructor(
    @ApplicationContext
    context: Context,
    private val userProfileRepository: UserProfileRepository,
    tokenManager: TokenManager
) :
    ViewModel() {


    val userId = UserSharedPreferencesManager.userId

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    val signInMethod = tokenManager.getSignInMethod()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile = _userProfile.asStateFlow()

    private val _profileImageBitmap = MutableStateFlow<Bitmap?>(null)
    val profileImageBitmap = _profileImageBitmap.asStateFlow()


    private val _profileImageDrawable = MutableStateFlow<Drawable?>(null)
    val profileImageDrawable = _profileImageDrawable.asStateFlow()


    val contentResolver: ContentResolver = context.contentResolver

    init {

        // Collect the user profile from the repository
        viewModelScope.launch(Dispatchers.IO) {
/*
            userProfileRepository.getUserProfile(userId)
                ?.let {
                    _isLoading.value=false
                    _userProfile.value = it
                    updateProfilePicUrl(it.profilePicUrl)
                }
*/
            userProfileRepository.getUserProfileFlow(userId).collectLatest { profile ->
                profile?.let {
                    _userProfile.value = profile
                    updateProfilePicUrl(profile.profilePicUrl)
                    _isLoading.value = false
                }
            }
        }


        if (tokenManager.getSignInMethod() == "guest") {

        } else {

            viewModelScope.launch {
                userProfileRepository.fetchUserProfile(userId)
            }
        }

    }


   private fun updateProfilePicUrl(profilePicUrl: String?) {
        _profileImageBitmap.value = try {
            BitmapFactory.decodeStream(contentResolver.openInputStream(Uri.parse(profilePicUrl)))
        } catch (e: Exception) {
            null // Handle the error gracefully
        }
    }

}

