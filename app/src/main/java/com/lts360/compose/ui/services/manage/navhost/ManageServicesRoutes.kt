package com.lts360.compose.ui.services.manage.navhost

import kotlinx.serialization.Serializable


@Serializable
sealed class ManageServicesRoutes  {

    @Serializable
    data object ManageServices: ManageServicesRoutes()

    @Serializable
    data object CreateService

    @Serializable
    data object ManagePublishedService: ManageServicesRoutes()

    @Serializable
    data object ManagePublishedServiceInfo

    @Serializable
    data object ManagePublishedServicePlans

    @Serializable
    data object ManagePublishedServiceImages

    @Serializable
    data object ManagePublishedServiceThumbnail


    @Serializable
    data object ManagePublishedServiceLocation
}

