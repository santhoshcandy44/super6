package com.super6.pot.app.database.models.profile

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_industry")
data class UserIndustry(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    val id: Long, // Primary key without auto-generate

    @ColumnInfo(name = "name")
    val name: String, // Industry name

    @ColumnInfo(name = "is_selected")
    var isSelected: Boolean // Indicates if the industry is selected
)
