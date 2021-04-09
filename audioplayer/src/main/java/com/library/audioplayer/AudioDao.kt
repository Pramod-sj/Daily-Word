package com.library.audioplayer

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AudioDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(audioCE: AudioCE): Long

    @Query("SELECT * FROM Audio WHERE id=:id")
    suspend fun get(id: String): AudioCE?

}