package com.lts360.compose.ui.onboarding.navhost

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed class OnBoardingScreen: NavKey{

    @Serializable
    data class CompleteAbout(val type:String?): OnBoardingScreen()

    @Serializable
    data object LocationAccess : OnBoardingScreen()

}
