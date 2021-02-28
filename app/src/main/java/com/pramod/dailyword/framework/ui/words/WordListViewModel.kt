package com.pramod.dailyword.framework.ui.words

import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.pramod.dailyword.business.interactor.GetWordListInteractor
import com.pramod.dailyword.framework.ui.common.BaseViewModel
import com.pramod.dailyword.framework.ui.common.word.WordListUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit
import javax.inject.Inject

const val LOCAL_PAGE_SIZE = 20
const val NETWORK_PAGE_SIZE = 20

@HiltViewModel
class WordListViewModel
@Inject constructor(
    getWordListInteractor: GetWordListInteractor
) : BaseViewModel() {

    companion object {
        val TAG = WordListViewModel::class.simpleName
    }

    @ExperimentalPagingApi
    val wordUIModelList: Flow<PagingData<WordListUiModel>> =
        getWordListInteractor.getWordList(pagingConfig = PagingConfig(20))
            .map { pagingData ->
                return@map pagingData
                    .map { word ->
                        WordListUiModel.WordItem(0, word)
                    }
                    .insertSeparators { wordItem: WordListUiModel.WordItem?, wordItem2: WordListUiModel.WordItem? ->
                        val daysElapsed = TimeUnit.DAYS.convert(
                            wordItem2?.word?.dateTimeInMillis ?: -1,
                            TimeUnit.MILLISECONDS
                        )

                        return@insertSeparators if (daysElapsed % 6 == 0L) {
                            WordListUiModel.AdItem("")
                        } else {
                            null
                        }
                    }
            }.cachedIn(viewModelScope)

}

