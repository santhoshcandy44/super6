package com.lts360.api.models.service

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import com.google.gson.annotations.SerializedName

@Entity(tableName = "guest_industries") // Table name is 'industries'
data class Industry(
    @PrimaryKey(autoGenerate = true) // Auto-generate the primary key if needed
    @SerializedName("industry_id")
    @ColumnInfo(name = "industry_id")
    val industryId: Int,
    @ColumnInfo(name = "industry_name")
    @SerializedName("industry_name")
    val industryName: String,
    @ColumnInfo(name = "description")
    @SerializedName("description")
    val description: String,
//    @SerializedName("created_at")
//    val createdAt: String,
    @ColumnInfo(name = "is_selected")
    @SerializedName("is_selected")
    var isSelected: Boolean
)



@Dao
interface GuestIndustryDao {

/*    // Insert a new industry into the database
    @Insert(onConflict = OnConflictStrategy.REPLACE) // Replace existing data if there's a conflict
    suspend fun insertIndustry(industry: Industry)*/

    // Insert multiple industries
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIndustries(industries: List<Industry>)

/*    // Update the 'isSelected' status for an industry
    @Update
    suspend fun updateIndustry(industry: Industry)*/

    // Get all industries
    @Query("SELECT * FROM guest_industries")
    suspend fun getAllIndustries(): List<Industry>

    // Get selected industries only
    @Query("SELECT * FROM guest_industries WHERE is_selected = 1")
    suspend fun getSelectedIndustries(): List<Industry>

    @Query("SELECT COUNT(*) FROM guest_industries WHERE is_selected = 1")
    suspend fun countSelectedIndustries(): Int

    // Get industry by id
/*    @Query("SELECT * FROM guest_industries WHERE industry_id = :industryId")
    suspend fun getIndustryById(industryId: Int): Industry?*/

    // Delete industry by industryId
/*    @Query("DELETE FROM guest_industries WHERE industry_id = :industryId")
    suspend fun deleteIndustry(industryId: Int)*/

    @Query("DELETE FROM guest_industries WHERE industry_id IN (:industryIds)")
    suspend fun deleteIndustries(industryIds: List<Int>)

}

