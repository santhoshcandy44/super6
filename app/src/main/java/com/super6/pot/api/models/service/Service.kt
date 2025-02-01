package com.super6.pot.api.models.service

import android.util.Log
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.super6.pot.utils.LogUtils.TAG
import kotlinx.serialization.Serializable


@Serializable
data class Service(
    @SerializedName("service_id")
    val serviceId: Long,

    @SerializedName("user")
    val user: FeedUserProfileInfo,

    @SerializedName("created_services")
    val createdServices:List<Service>?,

    @SerializedName("created_by")
    val createdBy: Long,

    @SerializedName("title")
    val title: String,

    @SerializedName("short_description")
    val shortDescription: String,

    @SerializedName("long_description")
    val longDescription: String,

    @SerializedName("industry")
    val industry: Int,

    @SerializedName("country")
    val country: String,


    @SerializedName("state")
    val state: String,

    @SerializedName("status")
    val status: String,

    @SerializedName("short_code")
    val shortCode: String,

    @SerializedName("is_bookmarked")
    var isBookmarked: Boolean,

    @SerializedName("thumbnail")
    var thumbnail: Thumbnail?,

    @SerializedName("images")
    var images: List<Image>,

    @SerializedName("plans")
    var plans: List<Plan>,

    @SerializedName("industries_count")
    var industriesCount: Int,

    @SerializedName("location")
    var location: Location?,

    @SerializedName("distance")
    var distance: Double?,

    @SerializedName("initial_check_at")
    var initialCheckAt: String?,

    @SerializedName("total_relevance")
    var totalRelevance: String?,

    )

fun Service.toEditableService(): EditableService {


    return EditableService(
        serviceId = serviceId, // Map to the network's serviceId
        title = title,
        shortDescription = shortDescription,
        longDescription = longDescription,
        industry = industry,
        country = country,
        state = state,
        status = "published",
        shortCode = shortCode,
        thumbnail =thumbnail?.toEditableThumbnail(),
        images = images.map { it.toEditableImage() },
        plans = plans.map { it.toEditablePlan() },
        location=location?.toEditableLocation(),

    )
}

