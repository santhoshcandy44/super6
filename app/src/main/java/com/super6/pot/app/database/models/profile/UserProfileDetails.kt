package com.super6.pot.app.database.models.profile

import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.serialization.Serializable


@Serializable
data class UserProfileDetails(
    @Embedded val userProfile: UserProfile,

    @Relation(
        parentColumn = "user_id",
        entityColumn = "user_id"
    )
    val userLocation: UserLocation?,

    )





