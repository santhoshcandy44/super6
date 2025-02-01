package com.super6.pot.api.models.service

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class Plan(

    @SerializedName("plan_id")
    val planId: Int,

    @SerializedName("plan_name")
    var planName: String,

    @SerializedName("plan_description")
    val planDescription: String,

    @Serializable(with = BigDecimalSerializer::class)
    @SerializedName("plan_price")
    val planPrice: BigDecimal,

    @SerializedName("price_unit")
    val planPriceUnit: String,

    @SerializedName("plan_features")
    var planFeatures: List<PlanFeature>,

    @SerializedName("plan_delivery_time")
    val planDeliveryTime: Int,

    @SerializedName("duration_unit")
    val planDurationUnit: String
)


fun Plan.toEditablePlan(): EditablePlan {
    return EditablePlan(
        planId = planId, // Assuming id maps to planId in the network model
        planName = planName,
        planDescription = planDescription,
        planPrice = planPrice,
        planPriceUnit = planPriceUnit,
        planFeatures = planFeatures.map { it.toEditablePlanFeature() }, // Handle conversion
        planDeliveryTime = planDeliveryTime,
        planDurationUnit = planDurationUnit
    )
}



