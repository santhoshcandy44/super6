package com.lts360.app.database.daos.service

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lts360.app.database.models.service.DraftImage


@Dao
interface DraftImageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(draftImage: List<DraftImage>)

    @Query("DELETE FROM draft_image WHERE service_id = :serviceId")
    suspend fun deleteImagesByServiceId(serviceId: Long)

    @Query("SELECT image_path FROM draft_image WHERE service_id = :serviceId")
    suspend fun getImagesCachePathsByServiceId(serviceId: Long) : List<String>

}
