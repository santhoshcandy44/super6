package com.lts360.app.database.daos.profile

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.lts360.app.database.models.profile.UserProfile
import com.lts360.app.database.models.profile.UserProfileDetails
import com.lts360.app.database.models.profile.UserProfileSettingsInfo
import kotlinx.coroutines.flow.Flow


@Dao
interface UserProfileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: UserProfile)

    @Update
    suspend fun update(profile: UserProfile)

    @Query("SELECT * FROM user_profile WHERE user_id = :id")
    fun getProfileFlow(id: Long): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE user_id = :id")
    fun getProfile(id: Long): UserProfile?

    @Query("SELECT first_name FROM user_profile WHERE user_id = :id")
    fun getFirstNameFlow(id: Long): Flow<String?>

    @Query("SELECT last_name FROM user_profile WHERE user_id = :id")
    fun getLastNameFlow(id: Long): Flow<String?>

    @Query("SELECT about FROM user_profile WHERE user_id = :id")
    fun getAboutFlow(id: Long): Flow<String?>

    @Query("SELECT email FROM user_profile WHERE user_id = :id")
    fun getEmailFlow(id: Long): Flow<String?>

    @Query("SELECT * FROM user_profile WHERE user_id = :id")
    fun getUserProfileSettingsInfoFlow(id: Long): Flow<UserProfileSettingsInfo?>

    @Query("SELECT first_name, last_name, email, about, profile_pic_url FROM user_profile WHERE user_id = :id")
    fun getUserProfileSettingsInfo(id: Long): UserProfileSettingsInfo?


    @Transaction
    @Query("SELECT * FROM user_profile  WHERE user_id= :userId")
    fun getUserProfileDetailsFlow(userId: Long): Flow<UserProfileDetails?>

    @Transaction
    @Query("SELECT * FROM user_profile WHERE user_id= :userId")
    suspend fun getUserProfileDetails(userId: Long): UserProfileDetails?


    @Query("UPDATE user_profile SET first_name = :firstName, updated_at = :updatedAt WHERE user_id = :userId")
    suspend fun updateFirstName(userId: Long, firstName: String, updatedAt: String)


    @Query("UPDATE user_profile SET last_name = :lastName, updated_at = :updatedAt WHERE user_id = :userId")
    suspend fun updateLastName(userId: Long, lastName: String, updatedAt: String)


    @Query("UPDATE user_profile SET about = :about, updated_at = :updatedAt WHERE user_id = :userId")
    suspend fun updateAbout(userId: Long, about: String, updatedAt: String)

    @Query("UPDATE user_profile SET account_type = :accountType, updated_at = :updatedAt WHERE user_id = :userId")
    suspend fun updateAccountType(userId: Long, accountType: String, updatedAt: String)


    @Query("UPDATE user_profile SET email = :email, updated_at = :updatedAt WHERE user_id = :userId")
    suspend fun updateEmail(userId: Long, email: String, updatedAt: String)

    @Query("UPDATE user_profile SET profile_pic_url = :profilePicUrl, updated_at = :updatedAt WHERE user_id = :userId")
    suspend fun updateProfilePicUrl(userId: Long, profilePicUrl: String, updatedAt: String)


    @Query("SELECT COUNT(*) FROM user_profile")
    suspend fun countUsers(): Long

    @Query("SELECT * FROM user_profile")
    suspend fun all(): List<UserProfile>


}


