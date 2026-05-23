package com.copa.alerta2026.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [MatchEntity::class, FavoriteTeamEntity::class, ChatMessageEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun matchDao(): MatchDao
    abstract fun favoriteTeamDao(): FavoriteTeamDao
    abstract fun chatMessageDao(): ChatMessageDao
}
