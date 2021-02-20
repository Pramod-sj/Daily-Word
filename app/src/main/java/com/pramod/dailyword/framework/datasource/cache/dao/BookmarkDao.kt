package com.pramod.dailyword.framework.datasource.cache.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pramod.dailyword.framework.datasource.cache.model.BookmarkCE

@Dao
interface BookmarkDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(bookmarkCE: BookmarkCE): Long

    @Query("DELETE FROM Bookmark WHERE bookmarkedWord=:word")
    suspend fun delete(word: String): Int
}