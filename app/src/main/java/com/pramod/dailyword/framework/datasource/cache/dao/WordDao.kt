package com.pramod.dailyword.framework.datasource.cache.dao

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.paging.PagingSource
/*import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingSource*/
import androidx.room.*
import com.pramod.dailyword.framework.datasource.cache.model.WordCE
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addAll(word: List<WordCE>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(wordCE: WordCE): Long

    @Update
    suspend fun update(wordCE: WordCE): Int

    @Query("DELETE FROM WordOfTheDay WHERE word=:word")
    suspend fun delete(word: String): Int

    @Query("DELETE FROM WordOfTheDay")
    suspend fun deleteAll(): Int

}