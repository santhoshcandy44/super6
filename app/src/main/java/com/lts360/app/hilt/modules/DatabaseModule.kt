package com.lts360.app.hilt.modules

import android.content.Context
import androidx.room.Room
import com.lts360.api.models.service.GuestIndustryDao
import com.lts360.app.database.AppDatabase
import com.lts360.app.database.daos.chat.ChatUserDao
import com.lts360.app.database.daos.chat.MessageDao
import com.lts360.app.database.daos.chat.MessageMediaMetaDataDao
import com.lts360.app.database.daos.chat.MessageProcessingDataDao
import com.lts360.app.database.daos.notification.NotificationDao
import com.lts360.app.database.daos.profile.BoardsDao
import com.lts360.app.database.daos.profile.RecentLocationDao
import com.lts360.app.database.daos.profile.UserLocationDao
import com.lts360.app.database.daos.profile.UserProfileDao
import com.lts360.app.database.daos.service.DraftImageDao
import com.lts360.app.database.daos.service.DraftLocationDao
import com.lts360.app.database.daos.service.DraftPlanDao
import com.lts360.app.database.daos.service.DraftServiceDao
import com.lts360.app.database.daos.service.DraftThumbnailDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {


    /*  .addMigrations(MIGRATION_54_55,
            MIGRATION_55_56,
            MIGRATION_56_57
            )*/

    @Volatile
    var DATABASE_INSTANCE: AppDatabase? = null



    @Provides
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        val dbFile = context.getDatabasePath("app_database")
        return DATABASE_INSTANCE.takeIf {
            dbFile.exists()
        } ?: synchronized(this) {

            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "app_database"
            ).apply {
                createFromAsset("database/external_database.db")
                // Enable multi-instance invalidation (optional)
                enableMultiInstanceInvalidation()

            }.build().also {
                DATABASE_INSTANCE = it
            }
        }

    }


    @Provides
    fun provideBoardsDao(database: AppDatabase): BoardsDao {
        return database.boardsDao()
    }

    @Provides
    fun provideGuestIndustryDao(database: AppDatabase): GuestIndustryDao {
        return database.guestIndustryDao()
    }


    @Provides
    fun provideDraftServiceDao(database: AppDatabase): DraftServiceDao {
        return database.draftServicesDao()
    }


    @Provides
    fun provideDraftImageDao(database: AppDatabase): DraftImageDao {
        return database.draftImageDao()
    }


    @Provides
    fun provideDraftPlanDao(database: AppDatabase): DraftPlanDao {
        return database.draftPlanDao()
    }

    @Provides
    fun provideDraftLocationDao(database: AppDatabase): DraftLocationDao {
        return database.draftLocationDao()
    }


    @Provides
    fun provideDraftThumbnailDao(database: AppDatabase): DraftThumbnailDao {
        return database.draftThumbnailDao()
    }

    @Provides
    fun provideMessageDao(database: AppDatabase): MessageDao {
        return database.messageDao()
    }

    @Provides
    fun provideMessageFileMetaDataDao(database: AppDatabase): MessageMediaMetaDataDao {
        return database.messageFileMetadataDao()
    }

    @Provides
    fun provideMessageFileProcessingDao(database: AppDatabase): MessageProcessingDataDao {
        return database.messageFileProcessingDao()
    }


    @Provides
    fun provideUserProfileDao(database: AppDatabase): UserProfileDao {
        return database.userProfileDao()

    }

    @Provides
    fun provideUserLocationDao(database: AppDatabase): UserLocationDao {
        return database.userLocationDao()
    }

    @Provides
    fun provideRecentLocationDao(database: AppDatabase): RecentLocationDao {
        return database.recentLocationDao()
    }


    @Provides
    fun provideChatUserDao(database: AppDatabase): ChatUserDao {
        return database.chatUserDao()
    }

    @Provides
    fun provideNotificationDao(database: AppDatabase): NotificationDao {
        return database.notificationDao()
    }

}
