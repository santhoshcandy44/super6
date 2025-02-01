package com.super6.pot.api.models.service

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


object EditableServiceSerializer{
    // Function to serialize a list of EditableService objects to a JSON string
    fun serializeEditableServiceList(serviceList: List<EditableService>): String {
        return Json.encodeToString(serviceList)  // The default serializer for the List type is automatically used
    }

    // Function to deserialize a JSON string back to a list of EditableService objects
    fun deserializeEditableServiceList(serviceListString: String): List<EditableService> {
        return Json.decodeFromString(serviceListString)
    }

    // Function to serialize a list of EditableService objects to a JSON string
    fun serializeEditableService(editableService: EditableService): String {
        return Json.encodeToString(editableService)  // The default serializer for the List type is automatically used
    }

    // Function to deserialize a JSON string back to a list of EditableService objects
    fun deserializeEditableService(editableServiceString: String): EditableService{
        return Json.decodeFromString(editableServiceString)
    }

}



@Serializable
data class EditableService(
    @SerializedName("service_id")
    val serviceId: Long,

    @SerializedName("title")
    val title: String,

    @SerializedName("short_description")
    val shortDescription: String,

    @SerializedName("long_description")
    val longDescription: String,

    @SerializedName("industry")
    val industry: Int,

    @SerializedName("country")
    val country: String?,


    @SerializedName("state")
    val state: String?,


    @SerializedName("status")
    val status: String,

    @SerializedName("images")
    var images: List<EditableImage>,

    @SerializedName("plans")
    var plans: List<EditablePlan>,

    @SerializedName("location")
    var location: EditableLocation?=null,

    @SerializedName("thumbnail_url")
    val imageUrl: String? = null,

    @SerializedName("thumbnail")
    val thumbnail: EditableThumbnailImage?=null,

    @SerializedName("short_code")
    val shortCode: String?=null)

