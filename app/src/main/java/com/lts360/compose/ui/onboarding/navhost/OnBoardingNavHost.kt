package com.lts360.compose.ui.onboarding.navhost

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import com.lts360.compose.ui.main.MainActivity
import com.lts360.compose.ui.onboarding.EditProfileAboutScreen
import com.lts360.compose.ui.onboarding.LocationAccessScreen

@Composable
fun OnBoardingNavHost(
    defaultType: String = "verified_user",
    defaultStartDestination: OnBoardingScreen = OnBoardingScreen.CompleteAbout("on_board"),
) {
    val backStacks = rememberNavBackStack(defaultStartDestination)

    val context = LocalContext.current

    NavDisplay(
        backStack = backStacks,
        entryDecorators = listOf(
            rememberSceneSetupNavEntryDecorator(),
            rememberSavedStateNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {
            entry<OnBoardingScreen.CompleteAbout> {
                EditProfileAboutScreen(
                    {
                        backStacks.add(OnBoardingScreen.LocationAccess)
                    }, {
                        backStacks.add(OnBoardingScreen.LocationAccess)
                    },
                    { backStacks.removeLastOrNull() })
            }

            entry<OnBoardingScreen.LocationAccess> {
                LocationAccessScreen(defaultType) {
                    context.startActivity(
                        Intent(context, MainActivity::class.java).apply {
                            flags =
                                Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        }
                    )
                }
            }
        }
    )

}

