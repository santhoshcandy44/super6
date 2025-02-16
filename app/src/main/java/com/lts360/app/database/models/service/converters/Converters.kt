package com.lts360.app.database.models.service.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lts360.api.models.service.FeedUserProfileInfo
import com.lts360.api.models.service.PlanFeature
import java.lang.reflect.Type
import java.math.BigDecimal


class Converters {

    private val gson = Gson()


    @TypeConverter
    fun fromBigDecimal(value: BigDecimal?): String? {
        return value?.toString()
    }

    @TypeConverter
    fun toBigDecimal(value: String?): BigDecimal? {
        return value?.let { BigDecimal(it) }
    }

    @TypeConverter
    fun fromUserProfileInfo(userProfile: FeedUserProfileInfo): String {
        return  gson.toJson(userProfile)
    }

    @TypeConverter
    fun toUserProfileInfo(userProfileString: String): FeedUserProfileInfo {
        return userProfileString.let {
            val type = object : TypeToken<FeedUserProfileInfo>() {}.type
            gson.fromJson(it, type)

        }
    }


    @TypeConverter
    fun fromPlanFeatureList(list: List<PlanFeature>?): String? {
        return gson.toJson(list)
    }

    @TypeConverter
    fun toPlanFeatureList(value: String?): List<PlanFeature>? {
        val listType: Type = object : TypeToken<List<PlanFeature>>() {}.type
        return gson.fromJson(value, listType)
    }


}



