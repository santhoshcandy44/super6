package com.lts360.compose.ui.localjobs.manage.navhost

import kotlinx.serialization.Serializable

@Serializable
sealed class ManageLocalJobRoutes  {

    @Serializable
    data object ManageLocalJob: ManageLocalJobRoutes()

    @Serializable
    data object CreateLocalJob: ManageLocalJobRoutes()

    @Serializable
    data object ManagePublishedLocalJob: ManageLocalJobRoutes()

}

