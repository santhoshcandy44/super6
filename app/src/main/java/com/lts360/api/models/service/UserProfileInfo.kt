package com.lts360.api.models.service

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable


@Serializable
data class UserProfileInfo(
    @SerializedName("status")
    val status: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("user_id")
    val userId: Long,

    @SerializedName("is_successful")
    val isSuccessful: Boolean,

    @SerializedName("first_name")
    val firstName: String,

    @SerializedName("last_name")
    val lastName: String?,

    @SerializedName("about")
    val about: String?,

    @SerializedName("email")
    val email: String,

    @SerializedName("is_email_verified")
    val isEmailVerified: Boolean,

    @SerializedName("phone_country_code")
    val phoneCountryCode: Boolean,

    @SerializedName("phone_number")
    val phoneNumber: Boolean,

    @SerializedName("is_phone_verified")
    val isPhoneVerified: Boolean,

    @SerializedName("profile_pic_url")
    val profilePicUrl: String?,

    @SerializedName("account_type")
    val accountType: String,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String,

    @SerializedName("online")
    val online: Boolean,

    @SerializedName("offline_messages")
    val offlineMessages: List<String>,

    @SerializedName("socket_id")
    val socketId: String,

    @SerializedName("location") val location: UserLocationInfo?=null
)



@Parcelize
@Serializable
data class FeedUserProfileInfo(
    @SerializedName("user_id")
    val userId: Long,

    @SerializedName("first_name")
    val firstName: String,

    @SerializedName("last_name")
    val lastName: String?,

    @SerializedName("about")
    val about: String?,

    @SerializedName("email")
    val email: String,

    @SerializedName("is_email_verified") val isEmailVerified: Boolean,

    @SerializedName("phone_country_code")
    val phoneCountryCode: String?,

    @SerializedName("phone_number")
    val phoneNumber: String?,

    @SerializedName("is_phone_verified")
    val isPhoneVerified: Boolean,

    @SerializedName("profile_pic_url")
    val profilePicUrl: String?,

    @SerializedName("profile_pic_url_96x96")
    val profilePicUrl96By96: String?,

    @SerializedName("geo")
    val geo: String?,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("online")
    val isOnline: Boolean
):Parcelable





@Serializable
data class UserLocationInfo(

    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("geo") val geo: String,
    @SerializedName("location_type") val locationType: String,
    @SerializedName("service_id") val serviceId: Int,
    @SerializedName("updated_at") val updatedAt: String

)


