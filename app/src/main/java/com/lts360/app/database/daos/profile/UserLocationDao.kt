package com.lts360.app.database.daos.profile

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lts360.app.database.models.profile.UserLocation


@Dao
interface UserLocationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: UserLocation)

    @Query("SELECT * FROM user_location WHERE user_id==:userId ")
    suspend fun getLocation(userId:Long): UserLocation?


//    @Query("UPDATE user_location SET location_type= :locationType, latitude = :latitude, longitude = :longitude, geo = :geo WHERE user_id = :userId")
//    suspend fun updateUserLocation(
//        userId: Int,
//        locationType: String,
//        latitude: Double,
//        longitude: Double,
//        geo: String,
//    )

}
