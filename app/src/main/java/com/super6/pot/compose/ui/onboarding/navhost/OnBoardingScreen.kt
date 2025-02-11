package com.super6.pot.compose.ui.onboarding.navhost

import kotlinx.serialization.Serializable

@Serializable
sealed class OnBoardingScreen  {

    @Serializable
    data class CompleteAbout(val type:String?): OnBoardingScreen()

    @Serializable
    data object LocationAccess : OnBoardingScreen()

    @Serializable
    data class ChooseIndustries(val userId: Long, val type:String?) : OnBoardingScreen()

    @Serializable
    data object GuestChooseIndustries  : OnBoardingScreen()


}
