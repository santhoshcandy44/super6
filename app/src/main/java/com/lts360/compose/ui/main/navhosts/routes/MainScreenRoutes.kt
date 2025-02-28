package com.lts360.compose.ui.main.navhosts.routes

import com.lts360.api.models.service.FeedUserProfileInfo
import com.lts360.app.database.models.profile.RecentLocation
import com.lts360.app.database.models.profile.UserProfile
import com.lts360.app.database.models.profile.UserProfileDetails
import com.lts360.compose.ui.auth.AccountType
import com.lts360.compose.ui.viewmodels.StateMap
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
    data class SMSChatScreen(val address:String)
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

}

@Serializable
sealed class  BookMarkRoutes{
    @Serializable
    data object BookmarkedDetailedService : BookMarkRoutes()

    @Serializable
    data object BookmarkedDetailedUsedProductListing : BookMarkRoutes()

    @Serializable
    data class BookmarkedDetailedServiceImagesSlider(val selectedImagePosition: Int) : BookMarkRoutes()

    @Serializable
    data class BookmarkedDetailedUsedProductListingImagesSlider(val selectedImagePosition: Int) : BookMarkRoutes()

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
    data object Profile

    @Serializable
    data class SwitchAccountType(val accountType: AccountType)

    @Serializable
    data class AccountAndProfileSettings(val accountType: String)

    @Serializable
    data object PersonalSettings : AccountAndProfileSettingsRoutes()

    @Serializable
    data object ChangeProfilePassword : AccountAndProfileSettingsRoutes()

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



object StateMapSerializer{
    // Function to serialize a list of EditableService objects to a JSON string
    fun serializeLocationsList(stateMap: StateMap): String {
        return Json.encodeToString(stateMap)  // The default serializer for the List type is automatically used
    }

    // Function to deserialize a JSON string back to a list of EditableService objects
    fun deserializeLocationsList(stateMap: String): StateMap {
        return Json.decodeFromString(stateMap)
    }
}


object RecentLocationSerializer{
    // Function to serialize a list of EditableService objects to a JSON string
    fun serializeLocationsList(recentLocation: List<RecentLocation>): String {
        return Json.encodeToString(recentLocation)  // The default serializer for the List type is automatically used
    }

    // Function to deserialize a JSON string back to a list of EditableService objects
    fun deserializeLocationsList(recentLocation: String): List<RecentLocation>{
        return Json.decodeFromString(recentLocation)
    }
}

object UserProfileSerializer{


    fun serializeUserProfileDetails(userProfileDetails: UserProfileDetails): String {
        return Json.encodeToString(userProfileDetails)  // The default serializer for the List type is automatically used
    }

    // Function to deserialize a JSON string back to a list of EditableService objects
    fun deserializeUserProfileDetails(userProfileDetailsString: String): UserProfileDetails {
        return Json.decodeFromString(userProfileDetailsString)
    }

    fun serializeUserProfile(userProfile: UserProfile): String {
        return Json.encodeToString(userProfile)  // The default serializer for the List type is automatically used
    }

    // Function to deserialize a JSON string back to a list of EditableService objects
    fun deserializeUserProfile(userProfileString: String): UserProfile {
        return Json.decodeFromString(userProfileString)
    }

    fun serializeFeedUserProfileInfo(feedUserProfileInfo: FeedUserProfileInfo): String {
        return Json.encodeToString(feedUserProfileInfo)  // The default serializer for the List type is automatically used
    }

    // Function to deserialize a JSON string back to a list of EditableService objects
    fun deserializeFeedUserProfile(feedUserProfileString: String): FeedUserProfileInfo {
        return Json.decodeFromString(feedUserProfileString)
    }

}






