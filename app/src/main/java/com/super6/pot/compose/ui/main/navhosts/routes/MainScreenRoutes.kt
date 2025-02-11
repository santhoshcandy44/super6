package com.super6.pot.compose.ui.main.navhosts.routes

import com.super6.pot.api.models.service.FeedUserProfileInfo
import com.super6.pot.app.database.models.profile.RecentLocation
import com.super6.pot.app.database.models.profile.UserProfile
import com.super6.pot.app.database.models.profile.UserProfileDetails
import com.super6.pot.compose.ui.auth.AccountType
import com.super6.pot.compose.ui.viewmodels.StateMap
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


@Serializable
sealed class MainScreen  {
    @Serializable
    data object Main : MainScreen()
}




@Serializable
data class SMSChatScreen(val address:String)


@Serializable
data object Profile

@Serializable
data class AccountAndProfileSettings(val accountType: String)


@Serializable
data class SwitchAccountType(val accountType: AccountType)


@Serializable
data class DetailedService(val key: Int)

@Serializable
data class FeedUserDetailedService(val key: Int=-1)


@Serializable
data object BookmarkedDetailedService

@Serializable
data class BookmarkedImagesSliderDetailedService(val selectedImagePosition: Int)


@Serializable
data object BookmarkedServices



@Serializable
data class ChatWindow(
    val chatId: Int,
    val recipientId: Long)


@Serializable
data class ServiceOwnerProfile(val serviceOwnerId: Long, val key: Int = -1)


@Serializable
data class ImagesSliderDetailedService(val key: Int, val selectedImagePosition: Int)


@Serializable
data class FeedUserImagesSliderDetailedService(val selectedImagePosition: Int)


@Serializable
data object PersonalSettings

@Serializable
data object ChangeProfilePassword

@Serializable
data object EditProfileFirstName

@Serializable
data object EditProfileLastName

@Serializable
data class EditProfileAbout(val type: String?)

@Serializable
data object EditProfileEmail

@Serializable
data class EditEmailOtpVerification(val email: String)







/*


@Serializable
data class HomeDetail(val key:Int=-1, val submittedQuery: String? = null, val onlySearchBar: Boolean = false)
*/

@Serializable
data class LocationChooser(val locationStatesEnabled:Boolean=true)

@Serializable
data object Districts

// Function to serialize a list of EditableService objects to a JSON string
fun serializeLocationsList(stateMap: StateMap): String {
    return Json.encodeToString(stateMap)  // The default serializer for the List type is automatically used
}

// Function to deserialize a JSON string back to a list of EditableService objects
fun deserializeLocationsList(stateMap: String): StateMap{
    return Json.decodeFromString(stateMap)
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






