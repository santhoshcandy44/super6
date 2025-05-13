package com.lts360.compose.ui.services.manage

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
import com.lts360.compose.ui.services.manage.navhost.ManageServicesRoutes
import com.lts360.compose.ui.usedproducts.manage.navhost.ManageUsedProductListingRoutes


@Composable
public fun rememberManageServicesCustomNavController(
    lastEntry:String?,
    isSelectedServiceNull:Boolean,
    vararg navigators: Navigator<out NavDestination>,
): NavHostController {
    val context = LocalContext.current
    return rememberSaveable(inputs = navigators, saver = NavControllerSaver(context, lastEntry, isSelectedServiceNull)) {
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
private fun NavControllerSaver(context: Context, lasEntry:String?, isSelectedServiceNull:Boolean): Saver<NavHostController, *> =
    Saver<NavHostController, Bundle>(
        save = { it.saveState() },
        restore = {
            createNavController(context).apply {


                // Clean the current route by removing path and query parameters
                val cleanedRoute = lasEntry
                    ?.replace(Regex("/\\{[^}]+\\}"), "") // Remove path parameters
                    ?.replace(Regex("\\?.*"), "")?.trim() // Optionally trim whitespace

                var allowedScreens = listOf(
                    ManageServicesRoutes.ManageServices::class,
                    ManageServicesRoutes.CreateService::class,
                )

                if(cleanedRoute == ManageServicesRoutes.ManagePublishedService::class.qualifiedName.orEmpty()){

                    if(!isSelectedServiceNull){
                        allowedScreens = allowedScreens.toMutableList()
                            .apply {
                                add(ManageServicesRoutes.ManagePublishedService::class)
                            }
                    }

                }



                val allowedRoutes = allowedScreens.map { it.qualifiedName.orEmpty() }

                if (cleanedRoute in allowedRoutes) {
                    restoreState(it) // Restore state
                }


            }
        }
    )

