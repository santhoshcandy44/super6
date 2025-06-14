package com.lts360.compose.ui.services.manage.navhost

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed class ManageServicesRoutes : NavKey{

    @Serializable
    data object ManageServices: ManageServicesRoutes()

    @Serializable
    data object CreateService: ManageServicesRoutes()

    @Serializable
    data object ManagePublishedService: ManageServicesRoutes()

    @Serializable
    data object ManagePublishedServiceInfo: ManageServicesRoutes()

    @Serializable
    data object ManagePublishedServicePlans: ManageServicesRoutes()

    @Serializable
    data object ManagePublishedServiceImages: ManageServicesRoutes()

    @Serializable
    data object ManagePublishedServiceThumbnail: ManageServicesRoutes()

    @Serializable
    data object ManagePublishedServiceLocation: ManageServicesRoutes()
}

