package com.super6.pot.ui.main.models

import com.google.gson.Gson
import com.super6.pot.api.app.AppClient
import com.super6.pot.api.common.errors.ErrorResponse
import com.super6.pot.api.common.responses.ResponseReply
import com.super6.pot.app.database.daos.profile.UserLocationDao
import com.super6.pot.api.Utils.Result
import com.super6.pot.api.app.ProfileSettingsService
import com.super6.pot.app.database.daos.profile.RecentLocationDao
import com.super6.pot.app.database.models.profile.RecentLocation
import com.super6.pot.app.database.models.profile.UserLocation
import javax.inject.Inject



class LocationRepository @Inject constructor(
    val userLocationDao: UserLocationDao,
    private val recentLocationDao: RecentLocationDao,
) {


    suspend fun insertUserLocation(userLocation: UserLocation) {
        userLocationDao.insert(userLocation)
    }

    suspend fun insertRecentLocation(recentLocation: RecentLocation) {
        recentLocationDao.insert(recentLocation)
    }



    suspend fun onGuestSaveLocationCoordinates(
        userId: Long,
        lat: Double,
        lon: Double,
        type: String,
        geo: String,
    ) {
        userLocationDao.insert(
            UserLocation(
                userId,
                type,
                lat,
                lon,
                geo,
                System.currentTimeMillis().toString()
            )
        )

        recentLocationDao.insert(
            RecentLocation(
                latitude = lat,
                longitude = lon,
                locationType = type,
                geo = geo
            )
        )
    }


    suspend fun saveLocationCoordinates(
        userId: Long,
        lat: Double,
        lon: Double,
        type: String,
        geo: String,
    ): Result<ResponseReply> {
        return try {
            // API call to save location
            val response = AppClient.instance.create(ProfileSettingsService::class.java)
                .updateUserLocation(userId, lat, lon, geo, type)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.isSuccessful) {

                    Result.Success(body)


                } else {
                    val errorMessage = "Failed, try again later..."
                    Result.Error(Exception(errorMessage))
                }

            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = try {
                    Gson().fromJson(errorBody, ErrorResponse::class.java).message
                } catch (e: Exception) {
                    "An unknown error occurred"
                }
                Result.Error(Exception(errorMessage))

            }
        } catch (t: Throwable) {

            Result.Error(t)

        }
    }


}
