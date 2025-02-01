package com.super6.pot.api.models.service

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Contextual
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

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
