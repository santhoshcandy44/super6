package com.lts360.compose.ui.onboarding.navhost

import kotlinx.serialization.Serializable

@Serializable
sealed class OnBoardingScreen  {

    @Serializable
    data class CompleteAbout(val type:String?): OnBoardingScreen()

    @Serializable
    data object LocationAccess : OnBoardingScreen()

}
