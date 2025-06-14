package com.lts360.compose.ui.localjobs.manage.navhost

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable


sealed class ManageLocalJobRoutes : NavKey{

    @Serializable
    data object ManageLocalJob: ManageLocalJobRoutes()

    @Serializable
    data object CreateLocalJob: ManageLocalJobRoutes()

    @Serializable
    data object ManagePublishedLocalJob: ManageLocalJobRoutes()

    @Serializable
    data object ViewApplicantsPublishedLocalJob: ManageLocalJobRoutes()

}

