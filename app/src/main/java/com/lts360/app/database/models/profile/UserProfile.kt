package com.lts360.app.database.models.profile

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "user_profile",
    indices = [Index(value = ["user_id"], unique = true)]
)
data class UserProfile(

    @PrimaryKey
    @ColumnInfo(name = "user_id")
    val userId: Long,

    @ColumnInfo(name = "first_name")
    val firstName: String,

    @ColumnInfo(name = "last_name")
    val lastName: String?,

    @ColumnInfo(name = "about")
    val about: String? = null,

    @ColumnInfo(name = "profile_pic_url")
    val profilePicUrl: String? = null,

    @ColumnInfo(name = "email")
    val email: String,

    @ColumnInfo(name = "account_type")
    val accountType: String,

    @ColumnInfo(name = "created_at")
    val createdAt: String,

    @ColumnInfo(name = "updated_at")
    val updatedAt: String?
)