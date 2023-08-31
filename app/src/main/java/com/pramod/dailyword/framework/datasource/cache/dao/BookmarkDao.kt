package com.pramod.dailyword.framework.datasource.cache.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.pramod.dailyword.framework.datasource.cache.model.BookmarkCE

@Dao
interface BookmarkDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)

    fun insert(bookmarkCE: BookmarkCE): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(bookmarkCE: BookmarkCE): Int

    @Query("SELECT * FROM Bookmark WHERE bookmarkedWord=:word")
    fun get(word: String): BookmarkCE?

    @Query("SELECT * FROM Bookmark")
    fun getAll(): LiveData<List<BookmarkCE>?>

    @Query("DELETE FROM Bookmark WHERE bookmarkedWord=:word")
    fun delete(word: String): Int
}