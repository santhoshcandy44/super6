package com.lts360.compose.ui.main.navhosts.routes

import com.lts360.api.models.service.FeedUserProfileInfo
import com.lts360.app.database.models.profile.RecentLocation
import com.lts360.app.database.models.profile.UserProfile
import com.lts360.compose.ui.auth.AccountType
import com.lts360.compose.ui.main.State
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
sealed class MainRoutes  {
    @Serializable
    data object Main : MainRoutes()

    @Serializable
    data class ChatWindow(
        val chatId: Int,
        val recipientId: Long)

    @Serializable
    data object ChooseIndustries : MainRoutes()

    @Serializable
    data object GuestChooseIndustries  : MainRoutes()

}

@Serializable
sealed class BottomNavRoutes {

    @Serializable
    data class DetailedService(val key: Int) : BottomNavRoutes()

    @Serializable
    data class DetailedServiceImagesSlider(val key: Int, val selectedImagePosition: Int) : BottomNavRoutes()

    @Serializable
    data class ServiceOwnerProfile(val serviceOwnerId: Long, val key: Int = -1) : BottomNavRoutes()

    @Serializable
    data class DetailedServiceFeedUser(val key: Int=-1) : BottomNavRoutes()

    @Serializable
    data class DetailedServiceFeedUserImagesSlider(val selectedImagePosition: Int) : BottomNavRoutes()


    @Serializable
    data class DetailedSeconds(val key: Int) : BottomNavRoutes()


    @Serializable
    data class SecondsOwnerProfile(val serviceOwnerId: Long, val key: Int = -1) : BottomNavRoutes()

    @Serializable
    data class  DetailedSecondsFeedUser(val key: Int=-1) : BottomNavRoutes()

    @Serializable
    data class DetailedSecondsImagesSlider(val key: Int, val selectedImagePosition: Int) : BottomNavRoutes()

    @Serializable
    data class DetailedSecondsFeedUserImagesSlider(val selectedImagePosition: Int) : BottomNavRoutes()

    @Serializable
    data class DetailedLocalJob(val key: Int) : BottomNavRoutes()
    @Serializable
    data class DetailedLocalJobsImagesSlider(val key: Int, val selectedImagePosition: Int) : BottomNavRoutes()
}

@Serializable
sealed class  BookMarkRoutes{
    @Serializable
    data object BookmarkedDetailedService : BookMarkRoutes()

    @Serializable
    data object BookmarkedDetailedUsedProductListing : BookMarkRoutes()

    @Serializable
    data object BookmarkedDetailedLocalJob : BookMarkRoutes()

    @Serializable
    data class BookmarkedDetailedServiceImagesSlider(val selectedImagePosition: Int) : BookMarkRoutes()

    @Serializable
    data class BookmarkedDetailedUsedProductListingImagesSlider(val selectedImagePosition: Int) : BookMarkRoutes()


    @Serializable
    data class BookmarkedDetailedLocalJobImagesSlider(val selectedImagePosition: Int) : BookMarkRoutes()

    @Serializable
    data object BookmarkedServices : BookMarkRoutes()

    @Serializable
    data class ServiceOwnerProfile(val serviceOwnerId: Long, val key: Int = -1) : BookMarkRoutes()

    @Serializable
    data class DetailedServiceFeedUser(val key: Int=-1) : BookMarkRoutes()

    @Serializable
    data class DetailedServiceFeedUserImagesSlider(val selectedImagePosition: Int): BookMarkRoutes()


    @Serializable
    data class SecondsOwnerProfile(val serviceOwnerId: Long, val key: Int = -1) : BookMarkRoutes()

    @Serializable
    data class  DetailedSecondsFeedUser(val key: Int=-1) : BookMarkRoutes()

    @Serializable
    data class DetailedSecondsFeedUserImagesSlider(val selectedImagePosition: Int): BookMarkRoutes()
}

@Serializable
sealed class AccountAndProfileSettingsRoutes{

    @Serializable
    data class SwitchAccountType(val accountType: AccountType)

    @Serializable
    data class AccountAndProfileSettings(val accountType: String)

    @Serializable
    data object PersonalSettings : AccountAndProfileSettingsRoutes()

    @Serializable
    data object ChangeAccountPassword : AccountAndProfileSettingsRoutes()

    @Serializable
    data object EditProfileFirstName : AccountAndProfileSettingsRoutes()

    @Serializable
    data object EditProfileLastName : AccountAndProfileSettingsRoutes()

    @Serializable
    data class EditProfileAbout(val type: String?) : AccountAndProfileSettingsRoutes()

    @Serializable
    data object EditProfileEmail : AccountAndProfileSettingsRoutes()

    @Serializable
    data class EditEmailOtpVerification(val email: String) : AccountAndProfileSettingsRoutes()

}

@Serializable
sealed class LocationSetUpRoutes{
    @Serializable
    data class LocationChooser(val locationStatesEnabled:Boolean=true)

    @Serializable
    data object Districts
}



object StateSerializer{
    fun serializeLocationsList(stateMap:  Map<String, State>): String {
        return Json.encodeToString(stateMap)
    }

    fun deserializeLocationsList(stateMap: String):  Map<String, State> {
        return Json.decodeFromString(stateMap)
    }
}


object RecentLocationSerializer{
    fun serializeLocationsList(recentLocation: List<RecentLocation>): String {
        return Json.encodeToString(recentLocation)
    }

    fun deserializeLocationsList(recentLocation: String): List<RecentLocation>{
        return Json.decodeFromString(recentLocation)
    }
}

object UserProfileSerializer{

    fun serializeUserProfile(userProfile: UserProfile): String {
        return Json.encodeToString(userProfile)
    }

    fun deserializeUserProfile(userProfileString: String): UserProfile {
        return Json.decodeFromString(userProfileString)
    }

    fun serializeFeedUserProfileInfo(feedUserProfileInfo: FeedUserProfileInfo): String {
        return Json.encodeToString(feedUserProfileInfo)
    }

    fun deserializeFeedUserProfile(feedUserProfileString: String): FeedUserProfileInfo {
        return Json.decodeFromString(feedUserProfileString)
    }

}






