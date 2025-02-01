package com.super6.pot.app.database.models.profile

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "user_location",
    foreignKeys = [
        ForeignKey(
            entity = UserProfile::class,
            parentColumns = ["user_id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class UserLocation(

    @PrimaryKey
    @ColumnInfo(name = "user_id")
    val userId: Long,
    @ColumnInfo(name = "location_type")
    val locationType: String?=null,
    @ColumnInfo(name = "latitude")
    val latitude: Double?=null,
    @ColumnInfo(name = "longitude")
    val longitude: Double?=null,
    @ColumnInfo(name = "geo")
    val geo: String?=null,
    @ColumnInfo(name = "updated_at")
    val updatedAt: String)