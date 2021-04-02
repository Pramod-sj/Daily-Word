package com.pramod.dailyword.framework.datasource.cache.dao

import androidx.room.*
import com.pramod.dailyword.framework.datasource.cache.model.SeenCE

@Dao
interface SeenDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun add(seenCE: SeenCE): Long

    @Update(onConflict = OnConflictStrategy.IGNORE)
    fun update(seenCE: SeenCE): Int

    @Query("SELECT * FROM Seen WHERE seenWord=:word")
    fun get(word: String): SeenCE?

    @Query("SELECT * FROM Seen")
    fun getAll(): List<SeenCE>

    @Query("DELETE FROM Seen WHERE seenWord=:word")
    fun delete(word: String): Int
}