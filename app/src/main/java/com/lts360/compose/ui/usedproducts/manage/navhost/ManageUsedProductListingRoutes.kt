package com.lts360.compose.ui.usedproducts.manage.navhost

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed class ManageUsedProductListingRoutes: NavKey {

    @Serializable
    data object ManageUsedProductListing: ManageUsedProductListingRoutes()

    @Serializable
    data object CreateUsedProductListing: ManageUsedProductListingRoutes()

    @Serializable
    data object ManagePublishedUsedProductListing: ManageUsedProductListingRoutes()
}

