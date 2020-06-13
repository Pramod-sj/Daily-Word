package com.pramod.dailyword.ui.words

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.pramod.dailyword.db.model.NetworkState
import com.pramod.dailyword.db.model.WordOfTheDay
import com.pramod.dailyword.db.repository.PaginationWordRepository
import com.pramod.dailyword.ui.BaseViewModel
import java.util.concurrent.Executors

private const val PAGE_SIZE = 20

class WordListViewModel(application: Application) : BaseViewModel(application) {

    private val repo = PaginationWordRepository(application, PAGE_SIZE, Executors.newSingleThreadExecutor())
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