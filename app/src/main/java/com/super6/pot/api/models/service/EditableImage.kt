package com.super6.pot.api.models.service

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class EditableImage(
    @SerializedName("image_id")
    val imageId: Int,

    @SerializedName("image_url")
    val imageUrl: String? = null,

    @SerializedName("width")
    val width: Int,

    @SerializedName("height")
    val height: Int,

    @SerializedName("size")
    val size: Int,


    @SerializedName("format")
    val format: String,

    @SerializedName("image_path")
    val imageData: String?=null
)



@Serializable
data class EditableThumbnailImage(
    @SerializedName("image_id")
    val imageId: Int,

    @SerializedName("image_url")
    val imageUrl: String? = null,

    @SerializedName("width")
    val width: Int,

    @SerializedName("height")
    val height: Int,

    @SerializedName("size")
    val size: Int,


    @SerializedName("format")
    val format: String,

    @SerializedName("image_path")
    val imageData: String? = null
)



