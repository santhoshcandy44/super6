package com.lts360.compose.ui.main

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.Navigator
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.DialogNavigator
import com.lts360.compose.ui.main.navhosts.routes.BottomBar


@Composable
fun rememberCustomBottomNavController(
    lastEntry:String?,
    vararg navigators: Navigator<out NavDestination>,
): NavHostController {
    val context = LocalContext.current
    return rememberSaveable(inputs = navigators, saver = navControllerSaver(context, lastEntry)) {
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

private fun navControllerSaver(context: Context, lasEntry:String?): Saver<NavHostController, *> =
    Saver(
        save = { it.saveState() },
        restore = {
            createNavController(context).apply {


                // Clean the current route by removing path and query parameters
                val cleanedRoute = lasEntry
                    ?.replace(Regex("/\\{[^}]+\\}"), "") // Remove path parameters
                    ?.replace(Regex("\\?.*"), "")?.trim() // Optionally trim whitespace

                val allowedScreens = listOf(
                    BottomBar.Home::class,
                    BottomBar.Chats::class,
                    BottomBar.Notifications::class,
                    BottomBar.More::class,
                    BottomBar.NestedServices::class
                )

                // Step 2: Get the list of allowed screens' qualified names
                val allowedRoutes = allowedScreens.map { it.qualifiedName.orEmpty() }

                if (cleanedRoute in allowedRoutes) {
                    restoreState(it) // Restore state
                }


            }
        }
    )

