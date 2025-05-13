package com.lts360.compose.ui.bookmarks

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
import com.lts360.compose.ui.main.navhosts.routes.BookMarkRoutes
import kotlin.reflect.KClass


@Composable
fun rememberBookMarksCustomNavController(
    lastEntry: String?,
    isSelectedBookmarkNull: Boolean,
    isSelectedItemSecondsOwnerProfileNull:Boolean,
    isSelectedItemServiceOwnerProfileNull:Boolean,

    vararg navigators: Navigator<out NavDestination>,
): NavHostController {
    val context = LocalContext.current
    return rememberSaveable(
        inputs = navigators,
        saver = NavControllerSaver(context, lastEntry, isSelectedBookmarkNull,
            isSelectedItemSecondsOwnerProfileNull,
            isSelectedItemServiceOwnerProfileNull)
    ) {
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
private fun NavControllerSaver(
    context: Context, lasEntry: String?,
    isSelectedBookmarkNull: Boolean,
    isSelectedItemSecondsOwnerProfileNull:Boolean,
    isSelectedItemServiceOwnerProfileNull:Boolean,


    ): Saver<NavHostController, *> =
    Saver<NavHostController, Bundle>(
        save = { it.saveState() },
        restore = {
            createNavController(context).apply {


                // Clean the current route by removing path and query parameters
                val cleanedRoute = lasEntry
                    ?.replace(Regex("/\\{[^}]+\\}"), "") // Remove path parameters
                    ?.replace(Regex("\\?.*"), "")?.trim() // Optionally trim whitespace

                var allowedScreens: List<KClass<out BookMarkRoutes>> = listOf(
                    BookMarkRoutes.BookmarkedServices::class,
                )



                if (cleanedRoute == BookMarkRoutes.BookmarkedDetailedService::class.qualifiedName.orEmpty() ||
                    cleanedRoute == BookMarkRoutes.BookmarkedDetailedUsedProductListing::class.qualifiedName.orEmpty() ||
                    cleanedRoute == BookMarkRoutes.BookmarkedDetailedServiceImagesSlider::class.qualifiedName.orEmpty() ||
                    cleanedRoute == BookMarkRoutes.BookmarkedDetailedUsedProductListingImagesSlider::class.qualifiedName.orEmpty() ||



                    cleanedRoute == BookMarkRoutes.ServiceOwnerProfile::class.qualifiedName.orEmpty() ||
                    cleanedRoute == BookMarkRoutes.DetailedServiceFeedUser::class.qualifiedName.orEmpty() ||
                    cleanedRoute == BookMarkRoutes.DetailedServiceFeedUserImagesSlider::class.qualifiedName.orEmpty() ||

                    cleanedRoute == BookMarkRoutes.SecondsOwnerProfile::class.qualifiedName.orEmpty() ||
                    cleanedRoute == BookMarkRoutes.DetailedSecondsFeedUser::class.qualifiedName.orEmpty() ||
                    cleanedRoute == BookMarkRoutes.DetailedSecondsFeedUserImagesSlider::class.qualifiedName.orEmpty()



                        ) {

                    if (!isSelectedBookmarkNull) {
                        allowedScreens = allowedScreens.toMutableList()
                            .apply {
                                add(BookMarkRoutes.BookmarkedDetailedService::class)
                                add(BookMarkRoutes.BookmarkedDetailedUsedProductListing::class)
                                add(BookMarkRoutes.BookmarkedDetailedServiceImagesSlider::class)
                                add(BookMarkRoutes.BookmarkedDetailedUsedProductListingImagesSlider::class)
                                add(BookMarkRoutes.ServiceOwnerProfile::class)
                                add(BookMarkRoutes.SecondsOwnerProfile::class)
                            }
                    }

                    if(!isSelectedBookmarkNull && !isSelectedItemServiceOwnerProfileNull){
                        allowedScreens = allowedScreens.toMutableList()
                            .apply {
                                add(BookMarkRoutes.DetailedServiceFeedUser::class)
                                add(BookMarkRoutes.DetailedServiceFeedUserImagesSlider::class)
                            }
                    }

                    if(!isSelectedBookmarkNull && !isSelectedItemSecondsOwnerProfileNull){
                        allowedScreens = allowedScreens.toMutableList()
                            .apply {
                                add(BookMarkRoutes.DetailedSecondsFeedUser::class)
                                add(BookMarkRoutes.DetailedSecondsFeedUserImagesSlider::class)
                            }
                    }



                }


                // Step 2: Get the list of allowed screens' qualified names
                val allowedRoutes = allowedScreens.map { it.qualifiedName.orEmpty() }


                if (cleanedRoute in allowedRoutes) {
                    restoreState(it) // Restore state
                }


            }
        }
    )

