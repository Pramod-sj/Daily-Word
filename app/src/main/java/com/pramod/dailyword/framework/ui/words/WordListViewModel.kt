package com.pramod.dailyword.framework.ui.words

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import com.pramod.dailyword.business.domain.model.Word
import com.pramod.dailyword.business.interactor.GetWordPagingInteractor
import com.pramod.dailyword.business.interactor.bookmark.ToggleBookmarkInteractor
import com.pramod.dailyword.framework.ui.common.BaseViewModel
import com.pramod.dailyword.framework.ui.common.word.WordListUiModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

const val PAGE_SIZE = 25

class WordListViewModel @AssistedInject constructor(
    private val getWordListInteractor: GetWordPagingInteractor,
    private val toggleBookmarkInteractor: ToggleBookmarkInteractor,
    @Assisted private val isBannerAdsEnabled: Boolean,
) : BaseViewModel() {

    @AssistedFactory
    interface Factory {

        fun create(isBannerAdsEnabled: Boolean): WordListViewModel

    }

    companion object {
        val TAG = WordListViewModel::class.simpleName
    }

    private val _searchQuery = MutableStateFlow("")

    fun setSearchQuery(query: String) {
        _searchQuery.tryEmit(query)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val wordUIModelList: Flow<PagingData<WordListUiModel>> =
        _searchQuery.flatMapLatest { query ->
            getWordListInteractor.getWordList(
                search = query,
                pagingConfig = PagingConfig(
                    pageSize = PAGE_SIZE
                )
            ).map { pagingData ->
                return@map pagingData
                    .map { word ->
                        WordListUiModel.WordItem(0, word)
                    }
                    .insertSeparators { wordItem: WordListUiModel.WordItem?, wordItem2: WordListUiModel.WordItem? ->

                        if (wordItem == null) {
                            // we're at the end of the list
                            return@insertSeparators null
                        }

                        if (wordItem2 == null) {
                            // we're at the beginning of the list
                            return@insertSeparators null
                        }

                        val daysElapsed = TimeUnit.DAYS.convert(
                            wordItem2.word.dateTimeInMillis ?: -1,
                            TimeUnit.MILLISECONDS
                        )

                        return@insertSeparators if (isBannerAdsEnabled && daysElapsed % 6 == 0L) {
                            WordListUiModel.AdItem("")
                        } else {
                            null
                        }
                    }
            }.cachedIn(viewModelScope)
        }


    fun toggleBookmark(word: Word) {
        viewModelScope.launch {
            toggleBookmarkInteractor.toggle(word.word)
                .collectLatest {

                }
        }
    }

}

class WordListViewModelFactory(
    private val assistedFactory: WordListViewModel.Factory,
    private val isBannerAdsEnabled: Boolean
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WordListViewModel::class.java)) {
            return assistedFactory.create(isBannerAdsEnabled) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}