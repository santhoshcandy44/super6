package com.super6.pot.app.database.models.profile

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable


@Serializable
@Entity(
    tableName = "recent_locations",
    indices = [Index(value = ["latitude", "longitude"], unique = true)]
)
data class RecentLocation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val latitude: Double,
    val longitude: Double,
    val geo: String,
    val locationType: String,
    val timestamp: Long = System.currentTimeMillis())