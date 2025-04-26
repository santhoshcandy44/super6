package com.lts360.app.database.models.app

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lts360.compose.ui.main.prefs.viewmodels.BoardPref


@Entity(tableName = "boards")
data class Board(
    @ColumnInfo("board_id")
    @PrimaryKey
    val boardId: Int,
    @ColumnInfo("board_name")
    val boardName: String,
    @ColumnInfo("board_label")
    val boardLabel: String,
    @ColumnInfo("display_order")
    val displayOrder: Int
)


fun Board.toBoardPref(isSelected: Boolean): BoardPref{

    return BoardPref(
        boardId = boardId,
        boardName = boardName,
        boardLabel = boardLabel,
        displayOrder = displayOrder,
        isSelected = isSelected
    )
}

