package com.pramod.dailyword.ui.bookmarked_words

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagingData
import androidx.paging.map
import com.pramod.dailyword.db.repository.BookmarkRepo
import com.pramod.dailyword.ui.BaseViewModel
import com.pramod.dailyword.ui.words.WordListUiModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FavoriteWordsViewModel(application: Application) : BaseViewModel(application) {
    private val bookmarkRepo = BookmarkRepo(application)

    val showPlaceHolderLiveData = MutableLiveData<Boolean>().apply {
        value = true
    }

    /*fun getBookmarkedWords(): LiveData<PagedList<WordOfTheDay>> = bookmarkRepo.getBookmarks()*/


    fun getFavWords(): Flow<PagingData<WordListUiModel>> {
        return bookmarkRepo.getBookmarkWords(20)
            .map { pagingData ->
                showPlaceHolderLiveData.value = false
                return@map pagingData.map {
                    return@map WordListUiModel.WordItem(0, it)
                }
            }
    }
}
