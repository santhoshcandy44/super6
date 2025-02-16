package com.lts360.compose.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.lts360.R
import com.lts360.compose.dropUnlessResumedV2
import com.lts360.compose.ui.main.navhosts.routes.BottomBarScreen


@Composable
fun BottomBar(
    navController: NavHostController,
    signInMethod: String?,
//              scrollBehavior:BottomAppBarScrollBehavior,
    messageCount: Int,
    notificationCount: Int,
    onNavigateUpWelcomeScreenSheet: () -> Unit,
) {


    val bottomBarItems = listOf(
        BottomBarItem(title = "Home", icon = R.drawable.ic_home),
        BottomBarItem(title = "Chats", icon = R.drawable.ic_chats),
        BottomBarItem(title = "Notifications", icon = R.drawable.ic_notifications),
        BottomBarItem(title = "More", icon = R.drawable.ic_more)
    )


    // Setting up the scroll behavior


    /*    BottomAppBar(
            scrollBehavior = scrollBehavior
        ){

        }*/


    val navBackStackEntry by navController.currentBackStackEntryAsState()


    val currentRoute = navBackStackEntry?.destination?.route

// Clean the current route by removing path and query parameters
    val cleanedRoute = currentRoute
        ?.replace(Regex("/\\{[^}]+\\}"), "") // Remove path parameters
        ?.replace(Regex("\\?.*"), "")?.trim() // Optionally trim whitespace


    NavigationBar(
        tonalElevation = 3.dp,
        containerColor = Color.Transparent,
    ) {
        bottomBarItems.forEachIndexed { _, item ->

            val screen = when (item.title) {
                "Home" -> BottomBarScreen.Home()
                "Chats" -> BottomBarScreen.Chats
                "Notifications" -> BottomBarScreen.Notifications
                "More" -> BottomBarScreen.More
                else -> throw IllegalArgumentException("Unknown screen")
            }


            val badgeCount = when (screen) {
                BottomBarScreen.Chats -> messageCount
                BottomBarScreen.Notifications -> notificationCount
                else -> 0
            }

            AddItem(
                signInMethod = signInMethod,
                bottomBarItem = item,
                screen = screen,
                isSelected =if(screen is BottomBarScreen.Home)
                    BottomBarScreen.NestedHome::class.qualifiedName.orEmpty()==cleanedRoute
                            || screen::class.qualifiedName.orEmpty() == cleanedRoute else screen::class.qualifiedName.orEmpty() == cleanedRoute,
                navController = navController,
                badgeCount = badgeCount, // Pass badgeCount directly
                onNavigateUpWelcomeScreenSheet = onNavigateUpWelcomeScreenSheet,

                )
        }
    }


}

@Composable
fun RowScope.AddItem(
    signInMethod: String?,
    bottomBarItem: BottomBarItem,
    screen: BottomBarScreen,
    isSelected: Boolean,
    navController: NavHostController,
    badgeCount: Int = 0, // Add badgeCount as a parameter
    onNavigateUpWelcomeScreenSheet: () -> Unit,
) {

// Determine if this screen is currently selected


    val lifecycleOwner = LocalLifecycleOwner.current


    NavigationBarItem(
        icon = {

            Box(modifier = Modifier.padding(4.dp)) {
                BadgedBox(
                    badge = {
                        if (badgeCount > 0) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White
                            ) { Text(badgeCount.toString()) }
                        }
                    }
                ) {
                    Icon(
                        painter = painterResource(bottomBarItem.icon),
                        contentDescription = "Navigation Icon"
                    )
                }
            }
        },
        selected = if ((signInMethod != null && signInMethod == "guest") && (screen is BottomBarScreen.Notifications || screen is BottomBarScreen.Chats)) {
            false
        } else {
            isSelected
        },

        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
            unselectedIconColor = Color.Gray,
            selectedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
            unselectedTextColor = Color.Gray
        ),

        onClick = {
            if ((signInMethod != null && signInMethod == "guest") && (screen is BottomBarScreen.Notifications || screen is BottomBarScreen.Chats)) {
                onNavigateUpWelcomeScreenSheet()
            } else {

                dropUnlessResumedV2(lifecycleOwner) {
                    navController.navigate(screen) {
                        launchSingleTop = true
                        restoreState = true // Restore previous state
                        // Optionally pop up to a specific destination if needed
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true // Save the state of the destination
                        }
                    }
                }

            }

        }
    )
}

data class BottomBarItem(val title: String, val icon: Int)
