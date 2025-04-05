package com.lts360.compose.ui.onboarding.navhost

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.lts360.compose.ui.auth.navhost.slideComposable
import com.lts360.compose.ui.main.MainActivity
import com.lts360.compose.ui.onboarding.EditProfileAboutScreen
import com.lts360.compose.ui.onboarding.LocationAccessScreen


@Composable
fun OnBoardingNavHost(
    defaultType: String = "verified_user",
    defaultStartDestination: OnBoardingScreen = OnBoardingScreen.CompleteAbout("on_board"),
) {
    val navController = rememberNavController()

    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = defaultStartDestination
    ) {

        slideComposable<OnBoardingScreen.CompleteAbout> {
            EditProfileAboutScreen(
                {
                    navController.navigate(OnBoardingScreen.LocationAccess)
                }, {
                    navController.navigate(OnBoardingScreen.LocationAccess)
                },
                { navController.popBackStack() })
        }


        slideComposable<OnBoardingScreen.LocationAccess> {
            LocationAccessScreen(defaultType) {
                context.startActivity(
                    Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    }
                )
            }
        }
    }
}

