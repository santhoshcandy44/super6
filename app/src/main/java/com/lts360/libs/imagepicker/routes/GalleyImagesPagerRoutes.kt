package com.lts360.libs.imagepicker.routes

import kotlinx.serialization.Serializable

@Serializable
sealed class GalleyImagesPagerRoutes {
    @Serializable
    data class GalleyImagesPager(val maxItems:Int) : GalleyImagesPagerRoutes()

    @Serializable
    data class SelectedAlbumImages(val album: String) : GalleyImagesPagerRoutes()
}