package com.super6.pot.app.database.daos.service

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.super6.pot.app.database.models.service.DraftPlan


@Dao
interface DraftPlanDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(draftPlan: List<DraftPlan>)

    @Update
    suspend fun update(draftPlan:List<DraftPlan>)

    @Query("DELETE FROM draft_plan WHERE service_id = :serviceId")
    suspend fun deletePlanByServiceId(serviceId: Long)

}
