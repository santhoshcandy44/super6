package com.lts360.app.database.models.service

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.lts360.api.models.service.EditablePlan
import com.lts360.api.models.service.EditablePlanFeature
import com.lts360.api.models.service.PlanFeature
import com.lts360.app.database.models.service.converters.Converters
import java.math.BigDecimal

@TypeConverters(Converters::class)
@Entity(
    tableName = "draft_plan",
    foreignKeys = [
        ForeignKey(
            entity = DraftService::class,
            parentColumns = ["id"],
            childColumns = ["service_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["service_id"])]  // Adding an index on service_id
)
data class DraftPlan(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "service_id")
    val serviceId: Long = 0,

    @ColumnInfo(name = "plan_name")
    val planName: String,

    @ColumnInfo(name = "plan_description")
    val planDescription: String,

    @ColumnInfo(name = "plan_price")
    val planPrice: BigDecimal,

    @ColumnInfo(name = "plan_price_unit")
    val planPriceUnit: String,

    @ColumnInfo(name = "plan_delivery_time")
    val planDeliveryTime: Int,

    @ColumnInfo(name = "plan_duration_unit")
    val planDurationUnit: String,

    @ColumnInfo(name = "plan_features")
    val planFeatures: List<PlanFeature>
)



fun DraftPlan.toPlan(): EditablePlan {
    return EditablePlan(
        planId = id, // Assuming id maps to planId in the network model
        planName = planName,
        planDescription = planDescription,
        planPrice = planPrice,
        planPriceUnit = planPriceUnit,
        planFeatures = planFeatures.map {
            EditablePlanFeature(
                it.featureName,
                it.featureValue
            )
        }, // Handle conversion
        planDeliveryTime = planDeliveryTime,
        planDurationUnit = planDurationUnit
    )
}

