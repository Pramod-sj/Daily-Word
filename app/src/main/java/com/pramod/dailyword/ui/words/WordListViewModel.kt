package com.pramod.dailyword.ui.words

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagedList
import com.pramod.dailyword.db.model.NetworkState
import com.pramod.dailyword.db.model.WordOfTheDay
import com.pramod.dailyword.db.repository.WOTDRepository
import com.pramod.dailyword.ui.BaseViewModel

const val LOCAL_PAGE_SIZE = 20
const val NETWORK_PAGE_SIZE = 20

class WordListViewModel(application: Application) : BaseViewModel(application) {

    private val wordRepo = WOTDRepository(application)
/*

    @ExperimentalPagingApi
    val wordPager = wordRepo.getAllWords(LOCAL_PAGE_SIZE)

*/

    private val resultResource = wordRepo.getAllWords()
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