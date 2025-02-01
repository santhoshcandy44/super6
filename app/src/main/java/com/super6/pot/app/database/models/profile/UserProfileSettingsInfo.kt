package com.super6.pot.app.database.models.profile



data class UserProfileSettingsInfo(
    val first_name: String,

    val last_name: String?,

    val about: String? = null,

    val profile_pic_url: String? = null,

    val email: String,
)
