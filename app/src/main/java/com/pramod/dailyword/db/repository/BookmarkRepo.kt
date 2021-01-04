package com.pramod.dailyword.db.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.pramod.dailyword.db.local.AppDB
import com.pramod.dailyword.db.model.Bookmark
import com.pramod.dailyword.db.model.WordOfTheDay
import com.pramod.dailyword.ui.words.LOCAL_PAGE_SIZE
import java.util.*

class BookmarkRepo(private val context: Context) {
    private val localDb: AppDB = AppDB.getInstance(context)
    private val dao = localDb.getBookmarkDao()

    fun getBookmarks(): LiveData<PagedList<WordOfTheDay>> {
        return LivePagedListBuilder(
            localDb.getBookmarkDao().getBookmarksPagingDataSource()
            , PagedList.Config.Builder()
                .setPageSize(LOCAL_PAGE_SIZE)
                .setEnablePlaceholders(false)
                .build()
        ).build()
    }

    suspend fun bookmarkToggle(word: String): Boolean {
        val data = dao.get(word)
        return if (data == null) {
            addToBookmark(word) > -1
        } else {
            removeFromBookmark(word) > 0
        }
    }

    suspend fun addToBookmark(word: String): Long {
        val bookmark = Bookmark()
        bookmark.bookmarkedWord = word
        bookmark.bookmarkedAt = Calendar.getInstance(Locale.US).timeInMillis
        return dao.insert(bookmark)
    }

    suspend fun removeFromBookmark(word: String): Int {
        return dao.delete(word)
    }
}