package com.lts360.api.models.service

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class PlanFeature(
    @SerializedName("feature_name")
    var featureName: String,
    @SerializedName("feature_value")
    @Serializable(with = AnySerializer::class)
    var featureValue: Any?,
)


fun PlanFeature.toEditablePlanFeature(): EditablePlanFeature {
    return EditablePlanFeature(featureName, featureValue)
}
