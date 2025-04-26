package com.lts360.compose.ui.localjobs.models

import com.google.gson.annotations.SerializedName
import com.lts360.api.models.service.BookMarkedItem
import com.lts360.api.models.service.EditableImage
import com.lts360.api.models.service.EditableLocation
import com.lts360.api.models.service.FeedUserProfileInfo
import com.lts360.api.models.service.Image
import com.lts360.api.models.service.Location
import com.lts360.api.models.service.toEditableImage
import com.lts360.api.models.service.toEditableLocation
import com.lts360.compose.ui.localjobs.manage.MaritalStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@SerialName("local_job")
data class LocalJob(
    @SerializedName("local_job_id")
    val localJobId: Long,

    @SerializedName("user")
    val user: FeedUserProfileInfo,

    @SerializedName("created_by")
    val createdBy: Long,

    @SerializedName("title")
    val title: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("company")
    val company: String,

    @SerializedName("age_min")
    val ageMin: Int,

    @SerializedName("age_max")
    val ageMax: Int,

    @SerializedName("marital_statuses")
    val maritalStatuses: List<String>,

    @SerializedName("salary_unit")
    val salaryUnit: String,

    @SerializedName("salary_min")
    val salaryMin: Int,

    @SerializedName("salary_max")
    val salaryMax: Int,

    @SerializedName("country")
    val country: String?,

    @SerializedName("state")
    val state: String?,

    @SerializedName("status")
    val status: String,

    @SerializedName("short_code")
    val shortCode: String,

    @SerializedName("is_bookmarked")
    var isBookmarked: Boolean,

    @SerializedName("images")
    var images: List<Image>,

    @SerializedName("location")
    var location: Location?,

    @SerializedName("distance")
    var distance: Double?,

    @SerializedName("initial_check_at")
    var initialCheckAt: String?,

    @SerializedName("total_relevance")
    var totalRelevance: String?
) : BookMarkedItem("local_job")


fun LocalJob.getMaritalStatusLabel(key: String): String {
    return MaritalStatus.entries.find { it.key == key }?.value ?: key
}


fun LocalJob.toEditableLocalJob(): EditableLocalJob {

    return EditableLocalJob(
        localJobId = localJobId,
        title = title,
        description = description,
        company = company,
        ageMin = ageMin,
        ageMax = ageMax,
        maritalStatuses = maritalStatuses,
        salaryUnit = salaryUnit,
        salaryMin = salaryMin,
        salaryMax = salaryMax,
        country = country,
        state = state,
        status = status,
        shortCode = shortCode,
        images =  images.map {
            it.toEditableImage()
        },
        location = location?.toEditableLocation()
    )

}



@Serializable
data class EditableLocalJob(
    @SerializedName("local_job_id")
    val localJobId: Long,

    @SerializedName("title")
    val title: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("company")
    val company: String,

    @SerializedName("age_min")
    val ageMin: Int,

    @SerializedName("age_max")
    val ageMax: Int,

    @SerializedName("marital_statuses")
    val maritalStatuses: List<String>,

    @SerializedName("salary_unit")
    val salaryUnit: String,

    @SerializedName("salary_min")
    val salaryMin: Int,

    @SerializedName("salary_max")
    val salaryMax: Int,

    @SerializedName("country")
    val country: String?,

    @SerializedName("state")
    val state: String?,

    @SerializedName("status")
    val status: String,

    @SerializedName("images")
    var images: List<EditableImage>,

    @SerializedName("location")
    var location: EditableLocation?=null,

    @SerializedName("short_code")
    val shortCode: String?=null)



