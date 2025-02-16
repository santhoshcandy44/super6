package com.lts360.app.database.daos.profile

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.lts360.app.database.models.app.Board
import kotlinx.coroutines.flow.Flow


@Dao
interface BoardsDao {
    @Insert
    suspend fun insertAllBoards(profile: List<Board>)

    @Query("SELECT * FROM board ")
    suspend fun getAllBoards(): List<Board>


    @Query("SELECT * FROM board WHERE is_pinned= 1 ")
    suspend fun getPinnedBoards(): List<Board>


    @Update
    fun updateBoard(board: Board)


    @Query("SELECT * FROM  board ")
    fun getAllBoardsFlow(): Flow<List<Board>>
}

