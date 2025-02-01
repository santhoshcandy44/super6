package com.super6.pot.app.database.models.app

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "board")
data class Board(
    @ColumnInfo("board_id")
    @PrimaryKey(autoGenerate = true)
    val boardId: Long,
    @ColumnInfo("board_name")
    val boardName: String,
    @ColumnInfo("is_pinned")
    val isPinned:Boolean)

