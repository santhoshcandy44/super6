package com.lts360.compose.ui.news.qr.navigation.routes

import kotlinx.serialization.Serializable

@Serializable
sealed class CreateQRCodeRoutes {
    @Serializable
    data object Text : CreateQRCodeRoutes()
    @Serializable
    data object Url : CreateQRCodeRoutes()
    @Serializable
    data object Wifi : CreateQRCodeRoutes()
    @Serializable
    data object ContactvCard : CreateQRCodeRoutes()
    @Serializable
    data object Email : CreateQRCodeRoutes()
    @Serializable
    data object Event : CreateQRCodeRoutes()
    @Serializable
    data object Location : CreateQRCodeRoutes()
    @Serializable
    data object Phone : CreateQRCodeRoutes()
    @Serializable
    data object Message : CreateQRCodeRoutes()
}
