package com.pramod.dailyword.framework.ui.words

import androidx.lifecycle.*
import androidx.paging.*
import com.pramod.dailyword.business.domain.model.Word
import com.pramod.dailyword.business.interactor.GetWordPagingInteractor
import com.pramod.dailyword.business.interactor.bookmark.ToggleBookmarkInteractor
import com.pramod.dailyword.framework.firebase.FBRemoteConfig
import com.pramod.dailyword.framework.ui.common.BaseViewModel
import com.pramod.dailyword.framework.ui.common.word.WordListUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

const val PAGE_SIZE = 25

@HiltViewModel
class WordListViewModel @Inject constructor(
    private val getWordListInteractor: GetWordPagingInteractor,
    private var toggleBookmarkInteractor: ToggleBookmarkInteractor,
    fbRemoteConfig: FBRemoteConfig
) : BaseViewModel() {

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

                        return@insertSeparators if (fbRemoteConfig.isAdsEnabled() && daysElapsed % 6 == 0L) {
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

