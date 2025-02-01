package com.super6.pot.api.models.service

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.math.BigDecimal

@Serializable
data class EditablePlan(
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
    var planFeatures: List<EditablePlanFeature>,

    @SerializedName("plan_delivery_time")
    val planDeliveryTime: Int,

    @SerializedName("duration_unit")
    val planDurationUnit: String,
)

object BigDecimalSerializer : KSerializer<BigDecimal> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("BigDecimal", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: BigDecimal) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): BigDecimal {
        return BigDecimal(decoder.decodeString())
    }
}
