package com.lts360.compose.ui.main.navhosts.routes

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed class BottomBar : NavKey{

    @Serializable
    data class Home(
        val key: Int = 0,
        val submittedQuery: String? = null,
        val onlySearchBar: Boolean = false,
    ) : BottomBar()

    @Serializable
    data class NestedServices(
        val key: Int = 0,
        val submittedQuery: String? = null,
        val onlySearchBar: Boolean = false,
    ) : BottomBar()

    @Serializable
    data class NestedSeconds(
        val key: Int = 0,
        val submittedQuery: String? = null,
        val onlySearchBar: Boolean = false,
    ) : BottomBar()

    @Serializable
    data class NestedLocalJobs(
        val key: Int = 0,
        val submittedQuery: String? = null,
        val onlySearchBar: Boolean = false,
    ) : BottomBar()


    @Serializable
    data object Chats : BottomBar()

    @Serializable
    data object Notifications : BottomBar()

    @Serializable
    data object More : BottomBar()
}