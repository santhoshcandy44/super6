package com.super6.pot.app.database.daos.service

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.super6.pot.app.database.models.service.DraftLocation


@Dao
interface DraftLocationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(draftPlan: DraftLocation)

    @Update
    suspend fun update(draftPlan: DraftLocation)

    @Query("DELETE FROM draft_location WHERE service_id = :serviceId")
    suspend fun deleteLocationByServiceId(serviceId: Long)

}
