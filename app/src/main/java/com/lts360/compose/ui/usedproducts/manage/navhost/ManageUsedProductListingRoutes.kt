package com.lts360.compose.ui.usedproducts.manage.navhost

import kotlinx.serialization.Serializable

@Serializable
sealed class ManageUsedProductListingRoutes  {

    @Serializable
    data object ManageUsedProductListing: ManageUsedProductListingRoutes()

    @Serializable
    data object CreateUsedProductListing

    @Serializable
    data object ManagePublishedUsedProductListing: ManageUsedProductListingRoutes()


}

