package com.pramod.dailyword.ui.words

import android.app.Application
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.pramod.dailyword.db.model.WordOfTheDay
import com.pramod.dailyword.db.repository.WOTDRepository
import com.pramod.dailyword.ui.BaseViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit

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

    companion object {
        val TAG = WordListViewModel::class.simpleName
    }

    private val resultResource = wordRepo.getPagingWordList()

    @ExperimentalPagingApi
    val wordUIModelList: Flow<PagingData<WordListUiModel>> =
        wordRepo.getPagingWordList(LOCAL_PAGE_SIZE).map { pagingData ->
            var index = -1L
            return@map pagingData
                .map { word ->
                    index++
                    WordListUiModel.WordItem(index, word)
                }
                .insertSeparators { wordItem: WordListUiModel.WordItem?, wordItem2: WordListUiModel.WordItem? ->
                    /* Log.i(
                         TAG,
                         "word 2:" + wordItem2?.wordOfTheDay?.date +
                                 ":" + wordItem2?.wordOfTheDay?.dateTimeInMillis +
                                 ":" + TimeUnit.DAYS.convert(
                             wordItem2?.wordOfTheDay?.dateTimeInMillis ?: 0,
                             TimeUnit.MILLISECONDS
                         )
                     )*/
                    val daysElapsed = TimeUnit.DAYS.convert(
                        wordItem2?.wordOfTheDay?.dateTimeInMillis ?: -1,
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


sealed class WordListUiModel {
    data class WordItem(val index: Long, val wordOfTheDay: WordOfTheDay) : WordListUiModel()
    data class AdItem(val adId: String) : WordListUiModel()
}
