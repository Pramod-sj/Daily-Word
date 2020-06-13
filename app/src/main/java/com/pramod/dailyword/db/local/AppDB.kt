package com.pramod.dailyword.db.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.pramod.dailyword.db.local.dao.WordOfTheDayDao
import com.pramod.dailyword.db.model.WordOfTheDay
import com.pramod.dailyword.util.ListConverter

@TypeConverters(ListConverter::class)
@Database(entities = [WordOfTheDay::class], version = 3, exportSchema = false)
abstract class AppDB : RoomDatabase() {

    companion object {
        private const val APP_DB_NAME = "WOTD_AppDb"

        @Volatile
        var INSTANCE: AppDB? = null

        fun getInstance(context: Context): AppDB? {
            if (INSTANCE == null) {
                synchronized(AppDB::class) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        AppDB::class.java,
                        APP_DB_NAME

                    ).fallbackToDestructiveMigration()/*.addMigrations(migration_1_2)
                        .addCallback(object : Callback() {
                            override fun onCreate(db: SupportSQLiteDatabase) {
                                super.onCreate(db)
                            }
                        })
                     */   .build()
                    return INSTANCE
                }
            }
            return INSTANCE
        }



        private object migration_1_2 : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE WordOfTheDay ADD COLUMN isSeen INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE WordOfTheDay ADD COLUMN seenAtTimeInMillis INTEGER")
            }
        }
    }


    abstract fun getWordOfTheDayDao(): WordOfTheDayDao

}