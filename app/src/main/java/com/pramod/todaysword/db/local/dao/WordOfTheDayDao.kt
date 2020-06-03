package com.pramod.todaysword.db.local.dao

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.*
import com.pramod.todaysword.db.model.WordOfTheDay

@Dao
interface WordOfTheDayDao {

    @Query("SELECT * FROM WordOfTheDay WHERE date=:date")
    fun getJust(date: String): LiveData<WordOfTheDay?>

    @Query("SELECT * FROM WordOfTheDay ORDER BY dateTimeInMillis DESC LIMIT 1 OFFSET 0")
    fun getJustTopOne(): LiveData<WordOfTheDay?>

    @Query("SELECT * FROM WordOfTheDay ORDER BY dateTimeInMillis DESC LIMIT :count OFFSET 1")
    fun getFewExceptTopOne(count: Int): LiveData<List<WordOfTheDay>?>

    @Query("SELECT * FROM WordOfTheDay ORDER BY dateTimeInMillis DESC")
    fun getAll(): DataSource.Factory<Int,WordOfTheDay>

    @Query("SELECT * FROM WordOfTheDay WHERE date!=:date")
    fun getAllExcept(date: String): LiveData<List<WordOfTheDay>?>

    @Query("SELECT * FROM WordOfTheDay WHERE date!=:date LIMIT :count")
    fun getFewExcept(date: String, count: Int): LiveData<List<WordOfTheDay>?>

    //nonLiveFunction
    @Query("SELECT * FROM WordOfTheDay WHERE date=:date")
    fun getJustNonLive(date: String): WordOfTheDay?

    @Query("SELECT * FROM WordOfTheDay ORDER BY dateTimeInMillis DESC LIMIT 1 OFFSET 0")
    fun getJustTopOneNonLive(): WordOfTheDay?


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addAll(word: List<WordOfTheDay>): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun add(wordOfTheDay: WordOfTheDay): Long

    @Update
    fun update(wordOfTheDay: WordOfTheDay): Int

    @Query("DELETE FROM WordOfTheDay WHERE word=:word COLLATE NOCASE")
    fun delete(word: String)

    @Query("DELETE FROM WordOfTheDay")
    fun deleteAll()

}