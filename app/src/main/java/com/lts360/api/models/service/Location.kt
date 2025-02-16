package com.lts360.api.models.service

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class Location(

    @SerializedName("service_id")
    val serviceId:Long,
    @SerializedName("longitude")
    val longitude: Double,
    @SerializedName("latitude")
    val latitude: Double,
    @SerializedName("geo")
    val geo:String,
    @SerializedName("location_type")
    val locationType:String
)



fun Location.toEditableLocation(): EditableLocation {
    return EditableLocation(
        serviceId,
        longitude,
        latitude,
        geo,
        locationType
    )
}


