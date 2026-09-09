package com.mr10.vello.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mr10.vello.data.local.dao.*
import com.mr10.vello.data.local.entities.*

@Database(
    entities = [
        UserEntity::class,
        MessageEntity::class,
        ConversationEntity::class,
        CallEntity::class,
        UserConnectionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun messageDao(): MessageDao
    abstract fun conversationDao(): ConversationDao
    abstract fun callDao(): CallDao
    abstract fun connectionDao(): ConnectionDao
}
