package com.lts360.compose.ui.usedproducts.manage.navhost

/*import android.content.Context
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

@Composable
fun rememberManageUsedProductListingCustomNavController(
    lastEntry:String?,
    isSelectedUsedProductListingNull:Boolean,
    vararg navigators: Navigator<out NavDestination>,
): NavHostController {

    val context = LocalContext.current

    return rememberSaveable(inputs = navigators, saver = NavControllerSaver(context, lastEntry, isSelectedUsedProductListingNull)) {
        createNavController(context)
    }.apply {
        for (navigator in navigators) {
            navigatorProvider.addNavigator(navigator)
        }
    }

}*/


/*

private fun createNavController(context: Context) =
    NavHostController(context).apply {
        navigatorProvider.addNavigator(ComposeNavigator())
        navigatorProvider.addNavigator(DialogNavigator())
    }

private fun NavControllerSaver(context: Context, lasEntry:String?,
                               isSelectedUsedProductListingNull:Boolean): Saver<NavHostController, *> =
    Saver<NavHostController, Bundle>(
        save = { it.saveState() },
        restore = {
            createNavController(context).apply {
                val cleanedRoute = lasEntry
                    ?.replace(Regex("/\\{[^}]+\\}"), "")
                    ?.replace(Regex("\\?.*"), "")?.trim()

                var allowedScreens = listOf(
                    ManageUsedProductListingRoutes.ManageUsedProductListing::class,
                    ManageUsedProductListingRoutes.CreateUsedProductListing::class
                )


                if(cleanedRoute == ManageUsedProductListingRoutes.ManagePublishedUsedProductListing::class.qualifiedName.orEmpty()){

                    if(!isSelectedUsedProductListingNull){
                        allowedScreens = allowedScreens.toMutableList()
                            .apply {

                                add(ManageUsedProductListingRoutes.ManagePublishedUsedProductListing::class)
                            }
                    }

                }


                val allowedRoutes = allowedScreens.map { it.qualifiedName.orEmpty() }

                if (cleanedRoute in allowedRoutes) {
                    restoreState(it)
                }
            }
        }
    )
*/
