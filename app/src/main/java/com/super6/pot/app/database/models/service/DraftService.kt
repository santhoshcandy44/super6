package com.super6.pot.app.database.models.service


import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "draft_service", indices = [Index(value = ["id"])])
data class DraftService(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "title")
    val title: String? = null,

    @ColumnInfo(name = "short_description")
    val shortDescription: String? = null,

    @ColumnInfo(name = "long_description")
    val longDescription: String? = null,

    @ColumnInfo(name = "industry")
    val industry: Int? = null,

    @ColumnInfo(name = "country")
    val country: String? = null,

    @ColumnInfo(name = "state")
    val state: String? = null,


    @ColumnInfo(name = "status")
    val status: String? = null)









