package com.pramod.dailyword.framework.datasource.cache

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.pramod.dailyword.framework.datasource.cache.dao.BookmarkDao
import com.pramod.dailyword.framework.datasource.cache.dao.BookmarkedWordDao
import com.pramod.dailyword.framework.datasource.cache.dao.WordDao
import com.pramod.dailyword.framework.datasource.cache.model.BookmarkCE
import com.pramod.dailyword.framework.datasource.cache.model.WordCE
import com.pramod.dailyword.framework.datasource.cache.convertors.ListConverter

@TypeConverters(ListConverter::class)
@Database(entities = [WordCE::class, BookmarkCE::class], version = 8, exportSchema = false)
abstract class AppDB : RoomDatabase() {

    companion object {
        const val APP_DB_NAME = "WOTD_AppDb"

        @Volatile
        var INSTANCE: AppDB? = null

        fun getInstance(context: Context): AppDB {
            if (INSTANCE == null) {
                synchronized(AppDB::class) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        AppDB::class.java,
                        APP_DB_NAME

                    ).addMigrations(migration_6_7)
                        .addMigrations(migration_7_8)
                        .build()
                    return INSTANCE!!
                }
            }
            return INSTANCE!!
        }


        private object migration_1_2 : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE WordOfTheDay ADD COLUMN isSeen INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE WordOfTheDay ADD COLUMN seenAtTimeInMillis INTEGER")
            }
        }

        private object migration_2_3 : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE WordOfTheDay ADD COLUMN DID_YOU_KNOW TEXT")
            }
        }


        private object migration_5_6 : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {

                database.beginTransaction()
                database.execSQL("ALTER TABLE WordOfTheDay RENAME TO old_WordOfTheDay")
                database.execSQL("CREATE TABLE WordOfTheDay (word TEXT PRIMARY KEY NOT NULL,pronounce TEXT,pronounceAudio TEXT,meanings TEXT,didYouKnow TEXT,examples TEXT,date TEXT,dateTimeInMillis INTEGER,isSeen INTEGER DEFAULT 0 NOT NULL,seenAtTimeInMillis INTEGER,wordColor INTEGER,wordDesaturatedColor INTEGER)")
                database.execSQL("INSERT INTO WordOfTheDay SELECT word,pronounce,pronounceAudio,meanings,didYouKnow,examples,date,dateTimeInMillis,isSeen,seenAtTimeInMillis,wordColor,wordDesaturatedColor FROM old_WordOfTheDay")
                database.endTransaction()

                database.execSQL("CREATE TABLE Favorite(favoriteWord TEXT,favoriteCreatedAt LONG)")
            }
        }

        private object migration_6_7 : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE WordOfTheDay ADD COLUMN synonyms TEXT")
                database.execSQL("ALTER TABLE WordOfTheDay ADD COLUMN antonyms TEXT")
            }
        }

        private object migration_7_8 : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE Bookmark RENAME TO old_Bookmark")
                database.execSQL("CREATE TABLE Bookmark (bookmarkId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, bookmarkedWord TEXT NOT NULL, bookmarkedAt INTEGER)")
                database.execSQL("INSERT INTO Bookmark SELECT null,bookmarkedWord,bookmarkedAt FROM old_Bookmark")
                database.execSQL("DROP TABLE old_Bookmark")
            }
        }

    }


    abstract fun getWordOfTheDayDao(): WordDao

    abstract fun getBookmarkDao(): BookmarkDao

    abstract fun getBookmarkedWordDao(): BookmarkedWordDao

}