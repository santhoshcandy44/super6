package com.lts360.api.models.service

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

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



