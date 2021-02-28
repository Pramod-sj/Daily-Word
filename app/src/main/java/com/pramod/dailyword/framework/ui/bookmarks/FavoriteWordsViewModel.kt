package com.pramod.dailyword.framework.ui.bookmarks

import androidx.lifecycle.MutableLiveData
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.paging.map
import com.pramod.dailyword.business.interactor.GetBookmarkedWordList
import com.pramod.dailyword.framework.ui.common.BaseViewModel
import com.pramod.dailyword.framework.ui.common.word.WordListUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class FavoriteWordsViewModel @Inject constructor(
    private val getBookmarkedWordList: GetBookmarkedWordList
) : BaseViewModel() {

    val showPlaceHolderLiveData = MutableLiveData<Boolean>().apply {
        value = true
    }

    @ExperimentalPagingApi
    fun getFavWords(): Flow<PagingData<WordListUiModel>> {
        return getBookmarkedWordList.getBookmarkedWordList(20)
            .map { pagingData ->
                return@map pagingData
                    .map {
                        return@map WordListUiModel.WordItem(0, it)
                    }
            }
    }
}
