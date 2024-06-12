package com.pramod.dailyword.framework.datasource.cache.dao

import androidx.room.*
import com.pramod.dailyword.framework.datasource.cache.model.SeenCE

@Dao
interface SeenDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun add(seenCE: SeenCE): Long

    @Update(onConflict = OnConflictStrategy.IGNORE)
    suspend fun update(seenCE: SeenCE): Int

    @Query("SELECT * FROM Seen WHERE seenWord=:word")
    suspend fun get(word: String): SeenCE?

    @Query("SELECT * FROM Seen")
    suspend fun getAll(): List<SeenCE>

    @Query("DELETE FROM Seen WHERE seenWord=:word")
    suspend fun delete(word: String): Int
}