package com.super6.pot.compose.ui.onboarding.navhost

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.super6.pot.R
import com.super6.pot.compose.ui.auth.navhost.slideComposable
import com.super6.pot.compose.ui.main.MainActivity
import com.super6.pot.compose.ui.onboarding.ChooseIndustryScreen
import com.super6.pot.compose.ui.onboarding.EditProfileAboutScreen
import com.super6.pot.compose.ui.onboarding.GuestChooseIndustryScreen
import com.super6.pot.compose.ui.onboarding.LocationAccessScreen



@Composable
fun OnBoardingNavHost(
    defaultType: String = "verified_user",
    defaultStartDestination: OnBoardingScreen = OnBoardingScreen.CompleteAbout("on_board"),
) {
    val navController = rememberNavController()

    val context = LocalContext.current

    // Define the AnimatedNavHost
    NavHost(
        navController = navController,
        startDestination = defaultStartDestination
    ) {
        // Entry Screen
        slideComposable<OnBoardingScreen.CompleteAbout> {

            EditProfileAboutScreen({ userId, type ->
                navController.navigate(OnBoardingScreen.LocationAccess)
            }, {}, {}, {navController.popBackStack()})
        }

        // Choose Account Type Screen
        slideComposable<OnBoardingScreen.ChooseIndustries> {

            ChooseIndustryScreen({
                context.startActivity(
                    Intent(context, MainActivity::class.java).apply {
                        flags =
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    },
                    ActivityOptions.makeCustomAnimation(
                        context,
                        R.anim.slide_in_right,
                        R.anim.slide_out_left
                    ).toBundle()
                )
                (context as Activity).finishAffinity()

            }, {
                navController.popBackStack()
            })
        }


        // Choose Account Type Screen
        slideComposable<OnBoardingScreen.GuestChooseIndustries> {

            GuestChooseIndustryScreen( {
                context.startActivity(
                    Intent(context, MainActivity::class.java).apply {
                        flags =
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    },
                    ActivityOptions.makeCustomAnimation(
                        context,
                        R.anim.slide_in_right,
                        R.anim.slide_out_left
                    ).toBundle()
                )
                (context as Activity).finishAffinity()

            }, {navController.popBackStack()})
        }


        // Register Screen
        slideComposable<OnBoardingScreen.LocationAccess> {
            LocationAccessScreen(defaultType,
                { userId, type ->
                    if (defaultType == "guest") {
                        navController.navigate(OnBoardingScreen.GuestChooseIndustries)
                    } else {
                        navController.navigate(
                            OnBoardingScreen.ChooseIndustries(
                                userId,
                                "on_board"
                            )
                        )
                    }
                })
        }
    }
}

