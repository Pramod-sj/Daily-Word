package com.pramod.todaysword.ui.words

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.pramod.todaysword.db.model.Listing
import com.pramod.todaysword.db.model.NetworkState
import com.pramod.todaysword.db.model.WordOfTheDay
import com.pramod.todaysword.db.repository.WOTDRepository
import com.pramod.todaysword.db.repository.WordBoundaryCallback
import com.pramod.todaysword.db.repository.WordsRepository
import com.pramod.todaysword.ui.BaseViewModel
import java.util.concurrent.Executors

private const val PAGE_SIZE = 10

class WordListViewModel(application: Application) : BaseViewModel(application) {

    private val repo = WordsRepository(application, PAGE_SIZE, Executors.newSingleThreadExecutor())
    private val resultResource = repo.getAllWords()

    val words: LiveData<PagedList<WordOfTheDay>>
    val networkState: LiveData<NetworkState>
    val refreshState: LiveData<NetworkState>

    init {
        words = resultResource.pagedList
        networkState = resultResource.networkState
        refreshState = resultResource.refreshState
    }

    fun refresh() {
        resultResource.refresh.invoke()
    }

    fun retry() {
        resultResource.retry.invoke()
    }

}