package com.lts360.compose.ui.localjobs.models

import com.google.gson.annotations.SerializedName
import com.lts360.api.models.service.FeedUserProfileInfo

data class LocalJobApplicant(
    @SerializedName("applicant_id")
    val applicantId: Long,
    @SerializedName("applied_at")
    val appliedAt: String,
    @SerializedName("is_reviewed")
    val isReviewed: Boolean,
    @SerializedName("user")
    val user: FeedUserProfileInfo,
    @SerializedName("initial_check_at")
    var initialCheckAt: String?
)







