package com.lts360.app.database.models.service

import androidx.room.Embedded
import androidx.room.Relation


data class DraftServiceWithDetails(
    @Embedded val draftService: DraftService,

    @Relation(
        parentColumn = "id",
        entityColumn = "service_id"
    )
    val draftImages: List<DraftImage>,

    @Relation(
        parentColumn = "id",
        entityColumn = "service_id"
    )
    val draftThumbnail: DraftThumbnail?,


    @Relation(
        parentColumn = "id",
        entityColumn = "service_id"
    )
    val draftPlans: List<DraftPlan>,

    @Relation(
        parentColumn = "id",
        entityColumn = "service_id"
    )
    val draftLocation: DraftLocation?


)

