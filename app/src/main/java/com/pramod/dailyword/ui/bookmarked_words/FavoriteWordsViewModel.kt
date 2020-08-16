package com.pramod.dailyword.ui.bookmarked_words

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagedList
import com.pramod.dailyword.db.model.WordOfTheDay
import com.pramod.dailyword.db.repository.BookmarkRepo
import com.pramod.dailyword.ui.BaseViewModel

class FavoriteWordsViewModel(application: Application) : BaseViewModel(application) {
    private val bookmarkRepo = BookmarkRepo(application)

    val showPlaceHolderLiveData = MutableLiveData<Boolean>().apply {
        value = true
    }

    fun getBookmarkedWords(): LiveData<PagedList<WordOfTheDay>> = bookmarkRepo.getBookmarks()
}