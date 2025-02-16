package com.lts360.api.models.service

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.double
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class EditablePlanFeature(
    @SerializedName("feature_name")
    var featureName: String,

    @Serializable(with = AnySerializer::class)
    @SerializedName("feature_value")
    var featureValue: Any?
)


object AnySerializer : KSerializer<Any?> {

    override val descriptor: SerialDescriptor = JsonElement.serializer().descriptor

    override fun serialize(encoder: Encoder, value: Any?) {
        val jsonElement: JsonElement = when (value) {
            null -> JsonNull
            is Int -> JsonPrimitive(value)
            is String -> JsonPrimitive(value)
            is Boolean -> JsonPrimitive(value)
            is List<*> -> JsonArray(value.map { serializeListItem(it) })
            is Map<*, *> -> JsonObject(value.mapKeys { it.key.toString() }.mapValues { serializeListItem(it.value) })
            else -> JsonPrimitive(value.toString()) // Handle other objects by converting to string
        }

        encoder.encodeSerializableValue(JsonElement.serializer(), jsonElement)
    }

    override fun deserialize(decoder: Decoder): Any? {
        val jsonElement = decoder.decodeSerializableValue(JsonElement.serializer())

        return when (jsonElement) {
            is JsonPrimitive -> {
                when {
                    jsonElement.isString -> jsonElement.content
                    jsonElement.booleanOrNull != null -> jsonElement.boolean
                    jsonElement.intOrNull != null -> jsonElement.int
                    jsonElement.doubleOrNull != null -> jsonElement.double
                    else -> jsonElement.content // Fallback to string if no known primitive type
                }
            }

            is JsonArray -> {
                jsonElement.map { deserializeListItem(it) }
            }

            is JsonObject -> {
                jsonElement.mapValues { deserializeListItem(it.value) }
            }

            JsonNull -> null // Return null for JsonNull
        }
    }

    // Helper to serialize items in a list
    private fun serializeListItem(item: Any?): JsonElement {
        return when (item) {
            null -> JsonNull
            is Int -> JsonPrimitive(item)
            is String -> JsonPrimitive(item)
            is Boolean -> JsonPrimitive(item)
            else -> JsonPrimitive(item.toString()) // Serialize other objects to String
        }
    }

    // Helper to deserialize items in a list
    private fun deserializeListItem(item: JsonElement): Any? {
        return when {
            item.jsonPrimitive.isString -> item.jsonPrimitive.content
            item.jsonPrimitive.booleanOrNull != null -> item.jsonPrimitive.boolean
            item.jsonPrimitive.intOrNull != null -> item.jsonPrimitive.int
            item.jsonPrimitive.doubleOrNull != null -> item.jsonPrimitive.double
            else -> item.jsonPrimitive.content // Fallback to string if type is unknown
        }
    }
}
