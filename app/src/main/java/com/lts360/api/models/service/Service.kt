package com.lts360.api.models.service

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


open class BookMarkedItem(val type:String)

@SerialName("service")
data class Service(
    @SerializedName("service_id")
    val serviceId: Long,

    @SerializedName("user")
    val user: FeedUserProfileInfo,

    @SerializedName("created_services")
    val createdServices: List<Service>?,

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


    @SerializedName("comments_count")
    var commentsCount: Int,
    ) : BookMarkedItem("service")

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
        thumbnail = thumbnail?.toEditableThumbnail(),
        images = images.map { it.toEditableImage() },
        plans = plans.map { it.toEditablePlan() },
        location = location?.toEditableLocation(),

        )
}


@SerialName("used_product_listing")
data class UsedProductListing(
    @SerializedName("product_id")
    val productId: Long,

    @SerializedName("user")
    val user: FeedUserProfileInfo,

    @SerializedName("created_used_product_listings")
    val createdUsedProductListings: List<UsedProductListing>?,

    @SerializedName("created_by")
    val createdBy: Long,

    @SerializedName("name")
    val name: String,

    @SerializedName("description")
    val description: String,

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


    @SerializedName("images")
    var images: List<Image>,

    @SerializedName("price")
    var price: Double,


    @SerializedName("price_unit")
    var priceUnit: String,


    @SerializedName("location")
    var location: Location?,

    @SerializedName("distance")
    var distance: Double?,

    @SerializedName("initial_check_at")
    var initialCheckAt: String?,

    @SerializedName("total_relevance")
    var totalRelevance: String?
) : BookMarkedItem("used_product_listing")


fun UsedProductListing.toEditableUsedProductListing(): EditableUsedProductListing {


    return EditableUsedProductListing(
        productId = productId, // Map to the network's serviceId
        name = name,
        description = description,
        country = country,
        state = state,
        status = status,
        shortCode = shortCode,
        images = images.map { it.toEditableImage() },
        location = location?.toEditableLocation(),
        price = price,
        priceUnit = priceUnit
    )
}