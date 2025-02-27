package com.lts360.compose.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.remember
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
import com.lts360.compose.ui.NoRippleInteractionSource
import com.lts360.compose.ui.main.navhosts.routes.BottomBar


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
        BottomBarItem(title = "Home", selectedIcon = R.drawable.ic_filled_home, unSelectedIcon = R.drawable.ic_outlined_home),
        BottomBarItem(title = "Chats", selectedIcon = R.drawable.ic_filled_chats, unSelectedIcon = R.drawable.ic_outlined_chats),
        BottomBarItem(title = "Notifications", selectedIcon = R.drawable.ic_filled_notification , unSelectedIcon = R.drawable.ic_outlined_notification),
        BottomBarItem(title = "More", selectedIcon = R.drawable.ic_filled_more, unSelectedIcon = R.drawable.ic_outlined_more)
    )


    val navBackStackEntry by navController.currentBackStackEntryAsState()


    val currentRoute = navBackStackEntry?.destination?.route

// Clean the current route by removing path and query parameters
    val cleanedRoute = currentRoute
        ?.replace(Regex("/\\{[^}]+\\}"), "") // Remove path parameters
        ?.replace(Regex("\\?.*"), "")?.trim() // Optionally trim whitespace


    NavigationBar(
        tonalElevation = 0.dp,
        modifier = Modifier.height(56.dp)
    ) {
        bottomBarItems.forEachIndexed { _, item ->

            val screen = when (item.title) {
                "Home" -> BottomBar.Home()
                "Chats" -> BottomBar.Chats
                "Notifications" -> BottomBar.Notifications
                "More" -> BottomBar.More
                else -> throw IllegalArgumentException("Unknown screen")
            }


            val badgeCount = when (screen) {
                BottomBar.Chats -> messageCount
                BottomBar.Notifications -> notificationCount
                else -> 0
            }

            AddItem(
                signInMethod = signInMethod,
                bottomBarItem = item,
                screen = screen,
                isSelected =if(screen is BottomBar.Home)
                    BottomBar.NestedServices::class.qualifiedName.orEmpty()==cleanedRoute
                            || screen::class.qualifiedName.orEmpty() == cleanedRoute else screen::class.qualifiedName.orEmpty() == cleanedRoute,
                navController = navController,
                badgeCount = badgeCount, // Pass badgeCount directly
                onNavigateUpWelcomeScreenSheet = onNavigateUpWelcomeScreenSheet)
        }
    }


}

@Composable
fun RowScope.AddItem(
    signInMethod: String?,
    bottomBarItem: BottomBarItem,
    screen: BottomBar,
    isSelected: Boolean,
    navController: NavHostController,
    badgeCount: Int = 0, // Add badgeCount as a parameter
    onNavigateUpWelcomeScreenSheet: () -> Unit,
) {

// Determine if this screen is currently selected


    val lifecycleOwner = LocalLifecycleOwner.current


    NavigationBarItem(
        interactionSource = remember { NoRippleInteractionSource() },
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
                    if(isSelected){
                        Icon(
                            painter = painterResource(bottomBarItem.selectedIcon),
                            contentDescription = "Navigation Icon"
                        )
                    }else{
                        Icon(
                            painter = painterResource(bottomBarItem.unSelectedIcon),
                            contentDescription = "Navigation Icon"
                        )
                    }

                }
            }
        },
        selected = if ((signInMethod != null && signInMethod == "guest") && (screen is BottomBar.Notifications || screen is BottomBar.Chats)) {
            false
        } else {
            isSelected
        },

        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.onSurface,
            unselectedIconColor = Color.Gray,
            selectedTextColor = MaterialTheme.colorScheme.onSurface,
            unselectedTextColor = Color.Gray,
            indicatorColor = Color.Transparent

        ),

        onClick = {


            if ((signInMethod != null && signInMethod == "guest") && (screen is BottomBar.Notifications || screen is BottomBar.Chats)) {
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

data class BottomBarItem(val title: String, val unSelectedIcon: Int, val selectedIcon:Int)
