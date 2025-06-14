package com.lts360.app.database


import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import com.lts360.api.models.service.GuestIndustryDao
import com.lts360.api.models.service.Industry
import com.lts360.app.CrashlyticsLogger
import com.lts360.app.database.daos.chat.ChatUserDao
import com.lts360.app.database.daos.chat.MessageDao
import com.lts360.app.database.daos.chat.MessageMediaMetaDataDao
import com.lts360.app.database.daos.chat.MessageProcessingDataDao
import com.lts360.app.database.daos.notification.NotificationDao
import com.lts360.app.database.daos.prefs.BoardDao
import com.lts360.app.database.daos.profile.RecentLocationDao
import com.lts360.app.database.daos.profile.UserLocationDao
import com.lts360.app.database.daos.profile.UserProfileDao
import com.lts360.app.database.daos.service.DraftImageDao
import com.lts360.app.database.daos.service.DraftLocationDao
import com.lts360.app.database.daos.service.DraftPlanDao
import com.lts360.app.database.daos.service.DraftServiceDao
import com.lts360.app.database.daos.service.DraftThumbnailDao
import com.lts360.app.database.models.app.Board
import com.lts360.app.database.models.chat.ChatUser
import com.lts360.app.database.models.chat.Message
import com.lts360.app.database.models.chat.MessageMediaMetadata
import com.lts360.app.database.models.chat.MessageProcessingData
import com.lts360.app.database.models.notification.Notification
import com.lts360.app.database.models.profile.RecentLocation
import com.lts360.app.database.models.profile.UserIndustry
import com.lts360.app.database.models.profile.UserLocation
import com.lts360.app.database.models.profile.UserProfile
import com.lts360.app.database.models.service.DraftImage
import com.lts360.app.database.models.service.DraftLocation
import com.lts360.app.database.models.service.DraftPlan
import com.lts360.app.database.models.service.DraftService
import com.lts360.app.database.models.service.DraftThumbnail
import com.lts360.compose.ui.news.qr.daos.QRCodeDao
import com.lts360.compose.ui.news.qr.models.QRCodeEntity
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
        QRCodeEntity::class
    ], version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun boardDao(): BoardDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun userLocationDao(): UserLocationDao
    abstract fun messageDao(): MessageDao
    abstract fun messageFileMetadataDao(): MessageMediaMetaDataDao
    abstract fun messageFileProcessingDao(): MessageProcessingDataDao
    abstract fun chatUserDao(): ChatUserDao
    abstract fun notificationDao(): NotificationDao
    abstract fun draftServiceDao(): DraftServiceDao
    abstract fun draftImageDao(): DraftImageDao
    abstract fun draftPlanDao(): DraftPlanDao
    abstract fun draftLocationDao(): DraftLocationDao
    abstract fun draftThumbnailDao(): DraftThumbnailDao
    abstract fun recentLocationDao(): RecentLocationDao
    abstract fun guestIndustryDao(): GuestIndustryDao
    abstract fun qrCodeDao(): QRCodeDao

    suspend fun backupDatabase(context: Context) {

        // Path to the original Room database file
        val dbFile = context.getDatabasePath("app_database")

        // Backup location in the external storage
        val backupFile = File(context.getExternalFilesDir("databases"), "backup_app_database.db")

        try {
            // Delete the original database file after successful backup
            if (dbFile.exists()) {
                // Copy the database to the backup file (overwrite if it exists)
                dbFile.copyTo(backupFile, overwrite = true)
            }

            formatAndCleanDatabase(context)

        } catch (e: IOException) {
            e.printStackTrace()
            CrashlyticsLogger.recordException(
                e, mapOf(
                    "backup_error" to "true",
                    "error_message" to (e.message ?: "Caused while backing up database")
                )
            )
        }
    }


    private suspend fun formatAndCleanDatabase(context: Context) {
        clearAllData()
        /*        close()
                context.deleteDatabase("app_database")*/
    }

    private suspend fun clearAllData() {
        clearAllTables()
    }
}
