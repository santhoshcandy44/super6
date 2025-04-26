package com.lts360.app.database.daos.prefs

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.lts360.app.database.models.app.Board
import com.lts360.compose.ui.main.prefs.viewmodels.BoardPref
import kotlinx.coroutines.flow.Flow

@Dao
interface BoardDao {

    @Query("DELETE FROM boards")
    suspend fun deleteAllBoards()

    @Query("DELETE FROM boards WHERE board_id IN (:boardIds)")
    suspend fun deleteBoards(boardIds: List<Int>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllBoards(boards:List<Board>)

    @Query("SELECT * FROM boards ORDER BY display_order ASC")
    fun getAllBoardsFlow(): Flow<List<Board>>

    @Query("SELECT * FROM boards ORDER BY display_order ASC")
    fun getAllBoards(): List<Board>

    @Transaction
    suspend fun clearAndInsertSelectedBoards(allBoards: List<BoardPref>) {
        deleteAllBoards()
        insertAllBoards(
            allBoards.filter { it.isSelected }.map {
                Board(
                    boardId = it.boardId,
                    boardName = it.boardName,
                    boardLabel = it.boardLabel,
                    displayOrder = it.displayOrder
                )
            }
        )
    }

}