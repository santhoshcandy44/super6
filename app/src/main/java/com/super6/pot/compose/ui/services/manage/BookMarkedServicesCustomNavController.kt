package com.super6.pot.compose.ui.services.manage

import android.content.Context
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.Navigator
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.DialogNavigator
import com.super6.pot.compose.ui.main.navhosts.routes.BookmarkedServices


@Composable
fun rememberBookMarkedServicesCustomBottomNavController(
    lastEntry:String?,
    vararg navigators: Navigator<out NavDestination>,
): NavHostController {
    val context = LocalContext.current
    return rememberSaveable(inputs = navigators, saver = NavControllerSaver(context, lastEntry)) {
        createNavController(context)
    }
        .apply {
            for (navigator in navigators) {
                navigatorProvider.addNavigator(navigator)
            }
        }
}

private fun createNavController(context: Context) =
    NavHostController(context).apply {
        navigatorProvider.addNavigator(ComposeNavigator())
        navigatorProvider.addNavigator(DialogNavigator())
    }

/** Saver to save and restore the NavController across config change and process death. */
private fun NavControllerSaver(context: Context, lasEntry:String?): Saver<NavHostController, *> =
    Saver<NavHostController, Bundle>(
        save = { it.saveState() },
        restore = {
            createNavController(context).apply {


                // Clean the current route by removing path and query parameters
                val cleanedRoute = lasEntry
                    ?.replace(Regex("/\\{[^}]+\\}"), "") // Remove path parameters
                    ?.replace(Regex("\\?.*"), "")?.trim() // Optionally trim whitespace

                val allowedScreens = listOf(
                    BookmarkedServices::class
                )

                // Step 2: Get the list of allowed screens' qualified names
                val allowedRoutes = allowedScreens.map { it.qualifiedName.orEmpty() }

                if (cleanedRoute in allowedRoutes) {
                    restoreState(it) // Restore state
                }


            }
        }
    )

