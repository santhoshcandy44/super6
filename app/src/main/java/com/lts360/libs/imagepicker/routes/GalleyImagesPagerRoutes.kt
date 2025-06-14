package com.lts360.libs.imagepicker.routes

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed class GalleyImagesPagerRoutes: NavKey{
    @Serializable
    data object GalleyImagesPager : GalleyImagesPagerRoutes()

    @Serializable
    data class SelectedAlbumImages(val album: String) : GalleyImagesPagerRoutes()
}