package com.super6.pot.app.database


import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import com.super6.pot.api.models.service.GuestIndustryDao
import com.super6.pot.api.models.service.Industry
import com.super6.pot.app.CrashlyticsLogger
import com.super6.pot.app.database.daos.chat.ChatUserDao
import com.super6.pot.app.database.daos.chat.MessageDao
import com.super6.pot.app.database.daos.chat.MessageMediaMetaDataDao
import com.super6.pot.app.database.daos.chat.MessageProcessingDataDao
import com.super6.pot.app.database.daos.notification.NotificationDao
import com.super6.pot.app.database.daos.profile.BoardsDao
import com.super6.pot.app.database.daos.profile.RecentLocationDao
import com.super6.pot.app.database.daos.profile.UserLocationDao
import com.super6.pot.app.database.daos.profile.UserProfileDao
import com.super6.pot.app.database.daos.service.DraftImageDao
import com.super6.pot.app.database.daos.service.DraftLocationDao
import com.super6.pot.app.database.daos.service.DraftPlanDao
import com.super6.pot.app.database.daos.service.DraftServiceDao
import com.super6.pot.app.database.daos.service.DraftThumbnailDao
import com.super6.pot.app.database.models.app.Board
import com.super6.pot.app.database.models.chat.ChatUser
import com.super6.pot.app.database.models.chat.Message
import com.super6.pot.app.database.models.chat.MessageMediaMetadata
import com.super6.pot.app.database.models.chat.MessageProcessingData
import com.super6.pot.app.database.models.notification.Notification
import com.super6.pot.app.database.models.profile.RecentLocation
import com.super6.pot.app.database.models.profile.UserIndustry
import com.super6.pot.app.database.models.profile.UserLocation
import com.super6.pot.app.database.models.profile.UserProfile
import com.super6.pot.app.database.models.service.DraftImage
import com.super6.pot.app.database.models.service.DraftLocation
import com.super6.pot.app.database.models.service.DraftPlan
import com.super6.pot.app.database.models.service.DraftService
import com.super6.pot.app.database.models.service.DraftThumbnail
import com.super6.pot.app.hilt.modules.DatabaseModule.clearDatabaseInstance
import java.io.File
import java.io.IOException


@Database(
    entities = [
        Board::class,
        UserProfile::class,
        UserLocation::class,
        ChatUser::class,
        Message::class,
        UserIndustry::class,
        RecentLocation::class,
        Notification::class,
        DraftService::class,
        DraftPlan::class,
        DraftImage::class,
        DraftLocation::class,
        DraftThumbnail::class,
        Industry::class,
        MessageMediaMetadata::class,
        MessageProcessingData::class,
    ], version = 79,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {


    abstract fun boardsDao(): BoardsDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun userLocationDao(): UserLocationDao
    abstract fun messageDao(): MessageDao
    abstract fun messageFileMetadataDao(): MessageMediaMetaDataDao
    abstract fun messageFileProcessingDao(): MessageProcessingDataDao
    abstract fun chatUserDao(): ChatUserDao
    abstract fun notificationDao(): NotificationDao
    abstract fun draftServicesDao(): DraftServiceDao
    abstract fun draftImageDao(): DraftImageDao
    abstract fun draftPlanDao(): DraftPlanDao
    abstract fun draftLocationDao(): DraftLocationDao
    abstract fun draftThumbnailDao(): DraftThumbnailDao
    abstract fun recentLocationDao(): RecentLocationDao
    abstract fun guestIndustryDao(): GuestIndustryDao


   fun backupDatabase(context: Context) {

        // Path to the original Room database file
        val dbFile = context.getDatabasePath("app_database")

        // Backup location in the external storage
        val backupFile = File(context.getExternalFilesDir("databases"), "backup_app_database.db")

        try {
            // Delete the original database file after successful backup
            if (dbFile.exists()) {
                // Copy the database to the backup file (overwrite if it exists)
                dbFile.copyTo(backupFile, overwrite = true)
                clearAllData()
                close()
                context.deleteDatabase("app_database")
                clearDatabaseInstance()
            }

        } catch (e: IOException) {
            e.printStackTrace()
            CrashlyticsLogger.recordException(
                e,
                mapOf(
                    "backup_error" to "true",
                    "error_message" to (e.message ?: "Caused while backing up database")
                )
            )
        }
    }

    private fun clearAllData() {
        clearAllTables()
    }
}
