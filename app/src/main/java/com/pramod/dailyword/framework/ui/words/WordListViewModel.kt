package com.pramod.dailyword.framework.ui.words

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.pramod.dailyword.business.interactor.GetWordListInteractor
import com.pramod.dailyword.framework.firebase.FBRemoteConfig
import com.pramod.dailyword.framework.ui.common.BaseViewModel
import com.pramod.dailyword.framework.ui.common.word.WordListUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit
import javax.inject.Inject

const val PAGE_SIZE = 25

@HiltViewModel
class WordListViewModel @Inject
constructor(
    getWordListInteractor: GetWordListInteractor,
    fbRemoteConfig: FBRemoteConfig
) : BaseViewModel() {

    companion object {
        val TAG = WordListViewModel::class.simpleName
    }

    @ExperimentalPagingApi
    val wordUIModelList: LiveData<PagingData<WordListUiModel>> =
        getWordListInteractor.getWordList(
            pagingConfig = PagingConfig(
                PAGE_SIZE,
                enablePlaceholders = false
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
        }.cachedIn(viewModelScope).asLiveData(Dispatchers.IO)

}

