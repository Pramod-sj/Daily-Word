package com.pramod.dailyword.db.local.dao

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pramod.dailyword.db.model.Bookmark
import com.pramod.dailyword.db.model.WordOfTheDay

@Dao
interface BookmarkDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(bookmark: Bookmark): Long

    @Query("DELETE FROM Bookmark WHERE bookmarkedWord=:word")
    suspend fun delete(word: String): Int

    @Query("SELECT * FROM WordOfTheDay as wd INNER JOIN Bookmark as f ON wd.word==f.bookmarkedWord WHERE wd.word=:word")
    suspend fun get(word: String): WordOfTheDay?

    /*@Query("SELECT * FROM WordOfTheDay as wd INNER JOIN Bookmark as f ON wd.word==f.bookmarkedWord ORDER BY f.bookmarkedAt DESC")
    fun getBookmarksPagingDataSource(): DataSource.Factory<Int, WordOfTheDay>*/

    @Query("SELECT * FROM WordOfTheDay as wd INNER JOIN Bookmark as f ON wd.word==f.bookmarkedWord ORDER BY f.bookmarkedAt DESC")
    fun getBookmarksPagingDataSource(): PagingSource<Int, WordOfTheDay>

    @Query("SELECT * FROM WordOfTheDay as wd INNER JOIN Bookmark as f ON wd.word==f.bookmarkedWord ORDER BY f.bookmarkedAt DESC")
    fun getBookmarks(): LiveData<List<WordOfTheDay>>
}