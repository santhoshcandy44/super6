package com.lts360.app.database.daos.service

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.lts360.app.database.models.service.DraftThumbnail


@Dao
interface DraftThumbnailDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(draftPlan: DraftThumbnail)

    @Update
    suspend fun update(draftPlan: DraftThumbnail)

    @Query("DELETE FROM draft_thumbnail WHERE service_id = :serviceId")
    suspend fun deleteThumbnailByServiceId(serviceId: Long)

    @Query("SELECT image_path FROM draft_thumbnail WHERE service_id = :draftId")
    suspend fun getThumbnailPathByServiceId(draftId: Long): String?


}
