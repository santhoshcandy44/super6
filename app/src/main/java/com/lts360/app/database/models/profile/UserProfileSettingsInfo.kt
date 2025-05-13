package com.lts360.app.database.models.profile

import androidx.room.ColumnInfo


data class UserProfileSettingsInfo(
    @ColumnInfo(name = "first_name")
    val firstName: String,
    @ColumnInfo(name = "last_name")
    val lastName: String?,
    @ColumnInfo(name = "about")
    val about: String?,
    @ColumnInfo(name = "profile_pic_url")
    val profilePicUrl: String?,
    @ColumnInfo(name = "email")
    val email: String,
    @ColumnInfo(name = "phone_country_code")
    val phoneCountryCode: String?,
    @ColumnInfo(name = "phone_number")
    val phoneNumber: String?
)
