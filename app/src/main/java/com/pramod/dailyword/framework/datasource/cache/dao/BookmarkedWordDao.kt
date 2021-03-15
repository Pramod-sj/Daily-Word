package com.pramod.dailyword.framework.datasource.cache.dao

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import com.pramod.dailyword.framework.datasource.cache.model.BookmarkedWordCE
import kotlinx.coroutines.flow.Flow


const val query_get_all_word =
    "SELECT * FROM Word LEFT JOIN Bookmark ON Word.word==Bookmark.bookmarkedWord LEFT JOIN Seen ON Word.word==Seen.seenWord"

const val query_get_only_bookmarked =
    "SELECT * FROM Word INNER JOIN Bookmark ON Word.word==Bookmark.bookmarkedWord LEFT JOIN Seen ON Word.word==Seen.seenWord"

@Dao
interface BookmarkedWordDao {

    @Query("$query_get_all_word WHERE date=:date")
    fun getWordByDate(date: String): LiveData<BookmarkedWordCE?>

    @Query("$query_get_all_word WHERE date=:date")
    suspend fun getWordByDateNonLive(date: String): BookmarkedWordCE?

    @Query("$query_get_all_word WHERE date=:date")
    fun getWordByDateAsFlow(date: String): Flow<BookmarkedWordCE?>

    @Query("$query_get_all_word WHERE Word.word=:word")
    fun getWordByName(word: String): LiveData<BookmarkedWordCE?>

    @Query("$query_get_all_word WHERE Word.word=:word")
    fun getWordByNameFlow(word: String): Flow<BookmarkedWordCE?>

    @Query("$query_get_all_word ORDER BY dateTimeInMillis DESC LIMIT 1 OFFSET 0")
    fun getTopOneWord(): Flow<BookmarkedWordCE?>

    @Query("$query_get_all_word ORDER BY dateTimeInMillis DESC LIMIT :count OFFSET 1")
    fun getFewExceptTopOneWord(count: Int): LiveData<List<BookmarkedWordCE>?>

    @Query("$query_get_all_word ORDER BY dateTimeInMillis DESC LIMIT :count")
    fun getFewWordsFromTop(count: Int): LiveData<List<BookmarkedWordCE>?>

    @Query("$query_get_all_word ORDER BY dateTimeInMillis DESC LIMIT :count")
    fun getFewWordsFromTopAsFlow(count: Int): Flow<List<BookmarkedWordCE>?>

    @Query("$query_get_all_word WHERE :tillDate <= dateTimeInMillis ORDER BY dateTimeInMillis DESC LIMIT :count")
    fun getFewWordsTill(tillDate: Long, count: Int): LiveData<List<BookmarkedWordCE>?>

    @Query("$query_get_all_word WHERE :tillDate <= dateTimeInMillis AND dateTimeInMillis<=:fromDate ORDER BY dateTimeInMillis DESC LIMIT :count")
    fun getFewWordsTillAsFlow(
        fromDate: Long,
        tillDate: Long,
        count: Int
    ): Flow<List<BookmarkedWordCE>?>

    @Query("$query_get_all_word ORDER BY dateTimeInMillis DESC")
    fun getWordsPagingSource(): PagingSource<Int, BookmarkedWordCE>

    @Query("$query_get_all_word ORDER BY dateTimeInMillis DESC")
    fun getWordsDataSource(): DataSource.Factory<Int, BookmarkedWordCE>

    @Query("$query_get_all_word WHERE date!=:date")
    fun getAllExcept(date: String): LiveData<List<BookmarkedWordCE>?>

    @Query("$query_get_all_word WHERE date!=:date LIMIT :count")
    fun getFewExcept(date: String, count: Int): LiveData<List<BookmarkedWordCE>?>

    //nonLiveFunction
    @Query("$query_get_all_word WHERE date=:date")
    suspend fun getWordNonLive(date: String): BookmarkedWordCE?

    @Query("$query_get_all_word WHERE word=:word")
    suspend fun getWordByNameNonLive(word: String): BookmarkedWordCE?

    @Query("$query_get_all_word ORDER BY dateTimeInMillis ASC LIMIT 1 OFFSET 0")
    suspend fun getJustTopOneWordNonLive(): BookmarkedWordCE?


    @Query("$query_get_only_bookmarked ORDER BY Bookmark.bookmarkedAt DESC")
    fun getBookmarksPagingDataSource(): PagingSource<Int, BookmarkedWordCE>


}