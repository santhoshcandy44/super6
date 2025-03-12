package com.lts360.libs.visualpicker.routes

import kotlinx.serialization.Serializable

@Serializable
sealed class GalleyImagesPagerRoutes {
    @Serializable
    data object GalleyImagesPager : GalleyImagesPagerRoutes()

    @Serializable
    data class SelectedAlbumImages(val album: String) : GalleyImagesPagerRoutes()
}