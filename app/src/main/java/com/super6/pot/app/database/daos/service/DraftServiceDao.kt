package com.super6.pot.app.database.daos.service

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.super6.pot.app.database.models.service.DraftService
import com.super6.pot.app.database.models.service.DraftServiceWithDetails
import kotlinx.coroutines.flow.Flow


@Dao
interface DraftServiceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(draftService: DraftService): Long

    @Transaction
    @Query("SELECT * FROM draft_service WHERE id = :serviceId")
    fun getDraftServiceWithDetails(serviceId: Long): DraftServiceWithDetails

    @Transaction
    @Query("SELECT * FROM draft_service")
    fun getAllDraftServicesWithDetails(): Flow<List<DraftServiceWithDetails>>

    @Update
    suspend fun update(draftService: DraftService)

    @Query("DELETE FROM draft_service WHERE id = :draftId")
    suspend fun deleteDraftById(draftId: Long)
}




