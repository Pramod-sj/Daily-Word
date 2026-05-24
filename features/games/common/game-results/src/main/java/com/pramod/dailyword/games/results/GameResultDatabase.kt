package com.pramod.dailyword.games.results

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [GameResultEntity::class], version = 1, exportSchema = false)
abstract class GameResultDatabase : RoomDatabase() {
    abstract fun gameResultDao(): GameResultDao
}
