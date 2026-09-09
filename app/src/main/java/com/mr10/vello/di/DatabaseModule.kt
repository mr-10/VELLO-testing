package com.mr10.vello.di

import android.content.Context
import androidx.room.Room
import com.mr10.vello.data.local.db.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "vello.db"
        ).build()
    }

    @Provides
    fun provideUserDao(db: AppDatabase) = db.userDao()

    @Provides
    fun provideMessageDao(db: AppDatabase) = db.messageDao()

    @Provides
    fun provideConversationDao(db: AppDatabase) = db.conversationDao()

    @Provides
    fun provideCallDao(db: AppDatabase) = db.callDao()

    @Provides
    fun provideConnectionDao(db: AppDatabase) = db.connectionDao()
}
