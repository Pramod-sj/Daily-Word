package com.pramod.dailyword.ui.words

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.pramod.dailyword.db.model.NetworkState
import com.pramod.dailyword.db.model.WordOfTheDay
import com.pramod.dailyword.db.repository.WOTDRepository
import com.pramod.dailyword.ui.BaseViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

const val LOCAL_PAGE_SIZE = 20
const val NETWORK_PAGE_SIZE = 20

class WordListViewModel(application: Application) : BaseViewModel(application) {

    private val wordRepo = WOTDRepository(application)

   /* @ExperimentalPagingApi
    val wordPager: Flow<PagingData<WordListUiModel>> = wordRepo.getAllWords(LOCAL_PAGE_SIZE)
        .map { pagingData -> pagingData.map { WordListUiModel.WordItem(it) } }
        .map {
            it.insertSeparators { wordItem: WordListUiModel.WordItem?, wordItem1: WordListUiModel.WordItem? ->
                WordListUiModel.AdItem(1, "")
            }

        }.cachedIn(viewModelScope)
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


/*
sealed class WordListUiModel {
    data class WordItem(val wordOfTheDay: WordOfTheDay) : WordListUiModel()
    data class AdItem(val adIndex: Int, val adId: String) : WordListUiModel()
}*/
