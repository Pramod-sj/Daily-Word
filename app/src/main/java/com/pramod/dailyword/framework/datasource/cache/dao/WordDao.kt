package com.pramod.dailyword.framework.datasource.cache.dao

/*import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingSource*/
import androidx.room.*
import com.pramod.dailyword.framework.datasource.cache.model.WordCE

@Dao
interface WordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addAll(word: List<WordCE>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(wordCE: WordCE): Long

    @Query("SELECT * FROM Word WHERE date=:wordDate")
    suspend fun get(wordDate: String): WordCE?

    @Query("SELECT * FROM Word")
    suspend fun getAll(): List<WordCE>?

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(wordCE: WordCE): Int

    @Query("DELETE FROM Word WHERE word=:word")
    suspend fun delete(word: String): Int

    @Query("DELETE FROM Word")
    suspend fun deleteAll(): Int

    @Query("DELETE FROM Word WHERE word NOT IN (SELECT word FROM Word ORDER BY dateTimeInMillis DESC LIMIT :n)")
    suspend fun deleteAllExceptTop(n: Int): Int

}