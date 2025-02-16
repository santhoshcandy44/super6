package com.lts360.app.database.models.service

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.lts360.api.models.service.EditableLocation


@Entity(
    tableName = "draft_location",
    foreignKeys = [
        ForeignKey(
            entity = DraftService::class,
            parentColumns = ["id"],
            childColumns = ["service_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["service_id","latitude", "longitude"], unique = true)]

)
data class DraftLocation(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,
    @ColumnInfo(name = "service_id")
    val serviceId: Long = 0,
    @ColumnInfo(name = "longitude")
    val longitude: Double,
    @ColumnInfo(name = "latitude")
    val latitude: Double,
    @ColumnInfo(name = "geo")
    val geo: String,
    @ColumnInfo(name = "location_type")
    val locationType: String
)

fun DraftLocation.toLocation(): EditableLocation {
    return EditableLocation(
        serviceId,
        longitude,
        latitude,
        geo,
        locationType
    )
}
