package com.lts360.api.models.service

import com.google.gson.annotations.SerializedName


// Model for the user's location
data class UserLocation(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("geo") val geo: String,
    @SerializedName("location_type") val locationType: String,
    @SerializedName("service_id") val serviceId: Int,
    @SerializedName("updated_at") val updatedAt: String

)

