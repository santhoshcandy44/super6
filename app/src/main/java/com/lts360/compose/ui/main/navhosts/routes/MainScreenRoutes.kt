package com.lts360.compose.ui.main.navhosts.routes

import androidx.navigation3.runtime.NavKey
import com.lts360.app.database.models.profile.RecentLocation
import com.lts360.compose.ui.auth.AccountType
import com.lts360.compose.ui.main.State
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

sealed class MainRoutes : NavKey{

    @Serializable
    data object Main : MainRoutes()

    @Serializable
    data class ChatWindow(
        val chatId: Int,
        val recipientId: Long) : MainRoutes()

    @Serializable
    data object ChooseIndustries : MainRoutes()

    @Serializable
    data object GuestChooseIndustries  : MainRoutes()

}


sealed class BottomNavRoutes: NavKey {

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

sealed class  BookMarkRoutes: NavKey{

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

sealed class AccountAndProfileSettingsRoutes: NavKey{

    @Serializable
    data class SwitchAccountType(val accountType: AccountType): AccountAndProfileSettingsRoutes()

    @Serializable
    data class AccountAndProfileSettings(val accountType: String) : AccountAndProfileSettingsRoutes()

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

sealed class LocationSetUpRoutes: NavKey {
    @Serializable
    data class LocationChooser(val locationStatesEnabled:Boolean=true):LocationSetUpRoutes()

    @Serializable
    data object Districts:LocationSetUpRoutes()
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






