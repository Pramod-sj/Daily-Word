package com.library.audioplayer

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [AudioCE::class], version = 1, exportSchema = false)
abstract class CachedAudioDatabase : RoomDatabase() {

    companion object {
        const val DATABASE_NAME = "cached_audio"

        @Volatile
        var INSTANCE: CachedAudioDatabase? = null

        fun getInstance(context: Context): CachedAudioDatabase {
            if (INSTANCE == null) {
                synchronized(CachedAudioDatabase::class) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        CachedAudioDatabase::class.java,
                        DATABASE_NAME

                    ).build()
                    return INSTANCE!!
                }
            }
            return INSTANCE!!
        }

    }

    abstract fun getAudioDao(): AudioDao
}