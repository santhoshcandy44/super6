package com.super6.pot.compose.ui.main.navhosts.routes

import kotlinx.serialization.Serializable

@Serializable
sealed class BottomBarScreen {
    @Serializable
    data class Home(
        val key: Int = 0,
        val submittedQuery: String? = null,
        val onlySearchBar: Boolean = false,
    ) : BottomBarScreen()

    @Serializable
    data class NestedHome(
        val key: Int = 0,
        val submittedQuery: String? = null,
        val onlySearchBar: Boolean = false,
    ) : BottomBarScreen()


    @Serializable
    data object Chats : BottomBarScreen()

    @Serializable
    data object Notifications : BottomBarScreen()

    @Serializable
    data object More : BottomBarScreen()
}