package com.lts360.compose.ui.main

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.Navigator
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.DialogNavigator
import com.lts360.components.utils.LogUtils.TAG
import com.lts360.compose.ui.main.navhosts.routes.BottomBar
import com.lts360.compose.ui.main.navhosts.routes.BottomNavRoutes
import kotlin.reflect.KClass


@Composable
fun rememberCustomBottomNavController(
    lastEntry:String?,
    isSelectedServiceItemNull:Boolean,
    isSelectedServiceOwnerServiceItemNull:Boolean,
    isSelectedUsedProductListingItemNull:Boolean,
    isSelectedServiceOwnerUsedProductListingItemNull:Boolean,

    vararg navigators: Navigator<out NavDestination>,
): NavHostController {
    val context = LocalContext.current
    return rememberSaveable(inputs = navigators, saver = navControllerSaver(context, lastEntry,
        isSelectedServiceItemNull,
        isSelectedServiceOwnerServiceItemNull,
        isSelectedUsedProductListingItemNull,
        isSelectedServiceOwnerUsedProductListingItemNull)) {
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

private fun navControllerSaver(context: Context, lasEntry:String?,
                               isSelectedServiceItemNull:Boolean,
                               isSelectedServiceOwnerServiceItemNull:Boolean,
                               isSelectedUsedProductListingItemNull:Boolean,
                               isSelectedSecondsOwnerUsedProductListingItemNull:Boolean,

): Saver<NavHostController, *> =
    Saver(
        save = { it.saveState() },
        restore = {
            createNavController(context).apply {


                // Clean the current route by removing path and query parameters
                val cleanedRoute = lasEntry
                    ?.replace(Regex("/\\{[^}]+\\}"), "") // Remove path parameters
                    ?.replace(Regex("\\?.*"), "")?.trim() // Optionally trim whitespace

                val navScreens  = listOf(
                    BottomBar.Home::class,
                    BottomBar.Chats::class,
                    BottomBar.Notifications::class,
                    BottomBar.More::class,
                    BottomBar.NestedServices::class,
                    BottomBar.NestedSeconds::class

                )

                var allowedScreens: List<KClass<out BottomNavRoutes>>  = listOf()



                if(
                    cleanedRoute == BottomNavRoutes.DetailedService::class.qualifiedName.orEmpty() ||
                    cleanedRoute == BottomNavRoutes.DetailedServiceImagesSlider::class.qualifiedName.orEmpty() ||

                    cleanedRoute == BottomNavRoutes.ServiceOwnerProfile::class.qualifiedName.orEmpty() ||
                    cleanedRoute == BottomNavRoutes.DetailedServiceFeedUser::class.qualifiedName.orEmpty() ||
                    cleanedRoute == BottomNavRoutes.DetailedServiceFeedUserImagesSlider::class.qualifiedName.orEmpty() ||

                    cleanedRoute == BottomNavRoutes.DetailedSeconds::class.qualifiedName.orEmpty() ||
                    cleanedRoute == BottomNavRoutes.DetailedSecondsImagesSlider::class.qualifiedName.orEmpty() ||


                    cleanedRoute == BottomNavRoutes.SecondsOwnerProfile::class.qualifiedName.orEmpty() ||
                    cleanedRoute == BottomNavRoutes.DetailedSecondsFeedUser::class.qualifiedName.orEmpty() ||
                    cleanedRoute == BottomNavRoutes.DetailedSecondsFeedUserImagesSlider::class.qualifiedName.orEmpty()
                    ){


                    if (!isSelectedServiceItemNull) {
                        allowedScreens = allowedScreens.toMutableList()
                            .apply {
                                add(BottomNavRoutes.DetailedService::class)
                                add(BottomNavRoutes.DetailedServiceImagesSlider::class)
                                add(BottomNavRoutes.ServiceOwnerProfile::class)
                            }
                    }

                    if(!isSelectedServiceItemNull && !isSelectedServiceOwnerServiceItemNull){
                        allowedScreens = allowedScreens.toMutableList()
                            .apply {
                                add(BottomNavRoutes.DetailedServiceFeedUser::class)
                                add(BottomNavRoutes.DetailedServiceFeedUserImagesSlider::class)
                            }
                    }


                    if (!isSelectedUsedProductListingItemNull) {
                        allowedScreens = allowedScreens.toMutableList()
                            .apply {
                                add(BottomNavRoutes.DetailedSeconds::class)
                                add(BottomNavRoutes.DetailedSecondsImagesSlider::class)
                                add(BottomNavRoutes.SecondsOwnerProfile::class)
                            }
                    }

                    if(!isSelectedUsedProductListingItemNull && !isSelectedSecondsOwnerUsedProductListingItemNull){
                        allowedScreens = allowedScreens.toMutableList()
                            .apply {
                                add(BottomNavRoutes.DetailedSecondsFeedUser::class)
                                add(BottomNavRoutes.DetailedSecondsFeedUserImagesSlider::class)
                            }
                    }
                }

                val navRoutes =  navScreens.map { it.qualifiedName.orEmpty() }
                // Step 2: Get the list of allowed screens' qualified names
                val allowedRoutes = allowedScreens.map { it.qualifiedName.orEmpty() }

                if (cleanedRoute in allowedRoutes || cleanedRoute in navRoutes) {
                    restoreState(it) // Restore state
                }


            }
        }
    )

