package com.super6.pot.app.database.daos.profile

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.super6.pot.app.database.models.profile.RecentLocation
import kotlinx.coroutines.flow.Flow


@Dao
interface RecentLocationDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(location: RecentLocation)

    @Query("SELECT * FROM recent_locations ORDER BY timestamp DESC")
    fun getAllLocations(): Flow<List<RecentLocation>>
}
