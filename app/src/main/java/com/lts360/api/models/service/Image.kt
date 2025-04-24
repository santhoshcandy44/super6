package com.lts360.api.models.service

import com.google.gson.annotations.SerializedName
import java.io.Serializable

@kotlinx.serialization.Serializable
data class Image(
    @SerializedName("image_id")
    val imageId: Int,
    @SerializedName("image_url")
    val imageUrl: String,
    @SerializedName("width")
    val width: Int,
    @SerializedName("height")
    val height: Int,
    @SerializedName("size")
    val size: Int,
    @SerializedName("format")
    val format: String) : Serializable


fun Image.toEditableImage(): EditableImage {
    return EditableImage(
        imageId = imageId,
        width = width,
        height = height,
        size = size,
        format = format,
        imageUrl = imageUrl,
        imageData = null
    )
}






@kotlinx.serialization.Serializable
data class Thumbnail(
    @SerializedName("id")
    val imageId: Int,
    @SerializedName("url")
    val imageUrl: String,
    @SerializedName("width")
    val width: Int,
    @SerializedName("height")
    val height: Int,
    @SerializedName("size")
    val size: Int,
    @SerializedName("format")
    val format: String) : Serializable


fun Thumbnail.toEditableThumbnail(): EditableThumbnailImage {
    return EditableThumbnailImage(
        imageId = imageId, // Assuming id maps to imageId in the network model
        width = width, // Set default or appropriate value
        height = height, // Set default or appropriate value
        size = size, // Set default or appropriate value
        format = format, // Set default or appropriate value
        imageUrl = imageUrl,
        imageData = null

    )
}
