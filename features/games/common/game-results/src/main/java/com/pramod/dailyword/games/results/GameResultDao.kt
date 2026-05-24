package com.pramod.dailyword.games.results

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameResultDao {
    @Query("SELECT * FROM game_results WHERE puzzleId = :puzzleId")
    fun getResultById(puzzleId: String): Flow<GameResultEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(result: GameResultEntity)

    @Query("SELECT * FROM game_results")
    fun getAllResults(): Flow<List<GameResultEntity>>
}
