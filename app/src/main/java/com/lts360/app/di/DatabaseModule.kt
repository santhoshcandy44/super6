package com.lts360.app.di

import android.content.Context
import androidx.room.Room
import com.lts360.api.models.service.GuestIndustryDao
import com.lts360.app.database.AppDatabase
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
import org.koin.android.ext.koin.androidApplication
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import org.koin.dsl.module

val databaseModule = module {
    single { provideDatabase(androidApplication()) }
}

private var DATABASE_INSTANCE: AppDatabase? = null

private fun provideDatabase(context: Context): AppDatabase {
    val dbFile = context.getDatabasePath("app_database")
    return DATABASE_INSTANCE.takeIf {
        dbFile.exists()
    } ?: synchronized(context) {
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "app_database"
        ).apply {
            createFromAsset("database/external_database.db")
            enableMultiInstanceInvalidation()
        }.build().also {
            DATABASE_INSTANCE = it
        }
    }
}

@Module
@ComponentScan
class DatabaseModule{

    @Single
    fun provideAppDatabase(context: Context): AppDatabase {
        return provideDatabase(context)
    }

    @Single
    fun provideUserProfileDao(appDatabase: AppDatabase): UserProfileDao {
        return appDatabase.userProfileDao()
    }

    @Single
    fun provideUserLocationDao(appDatabase: AppDatabase): UserLocationDao {
        return appDatabase.userLocationDao()
    }

    @Single
    fun provideBoardDao(appDatabase: AppDatabase): BoardDao {
        return appDatabase.boardDao()
    }

    @Single
    fun provideMessageDao(appDatabase: AppDatabase): MessageDao {
        return appDatabase.messageDao()
    }

    @Single
    fun provideNotificationDao(appDatabase: AppDatabase): NotificationDao {
        return appDatabase.notificationDao()
    }

    @Single
    fun provideGuestIndustryDao(appDatabase: AppDatabase): GuestIndustryDao {
        return appDatabase.guestIndustryDao()
    }

    @Single
    fun provideDraftServicesDao(appDatabase: AppDatabase): DraftServiceDao {
        return appDatabase.draftServiceDao()
    }
    @Single
    fun provideDraftImageDao(appDatabase: AppDatabase): DraftImageDao {
        return appDatabase.draftImageDao()
    }

    @Single
    fun provideDraftPlanDao(appDatabase: AppDatabase): DraftPlanDao {
        return appDatabase.draftPlanDao()
    }

    @Single
    fun provideDraftLocationDao(appDatabase: AppDatabase): DraftLocationDao {
        return appDatabase.draftLocationDao()
    }

    @Single
    fun provideDraftThumbnailDao(appDatabase: AppDatabase): DraftThumbnailDao {
        return appDatabase.draftThumbnailDao()
    }

    @Single
    fun provideMessageFileMetadataDao(appDatabase: AppDatabase): MessageMediaMetaDataDao {
        return appDatabase.messageFileMetadataDao()
    }

    @Single
    fun provideMessageFileProcessingDao(appDatabase: AppDatabase): MessageProcessingDataDao {
        return appDatabase.messageFileProcessingDao()
    }

    @Single
    fun provideRecentLocationDao(appDatabase: AppDatabase): RecentLocationDao {
        return appDatabase.recentLocationDao()
    }

    @Single
    fun provideChatUserDao(appDatabase: AppDatabase): ChatUserDao {
        return appDatabase.chatUserDao()
    }
}