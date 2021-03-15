package com.pramod.dailyword.framework.ui.bookmarks

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.paging.map
import com.pramod.dailyword.business.data.network.Status
import com.pramod.dailyword.business.domain.model.Bookmark
import com.pramod.dailyword.business.domain.model.Word
import com.pramod.dailyword.business.interactor.GetBookmarkedWordList
import com.pramod.dailyword.business.interactor.bookmark.AddBookmarkInteractor
import com.pramod.dailyword.business.interactor.bookmark.RemoveBookmarkInteractor
import com.pramod.dailyword.framework.ui.common.Action
import com.pramod.dailyword.framework.ui.common.BaseViewModel
import com.pramod.dailyword.framework.ui.common.Message
import com.pramod.dailyword.framework.ui.common.word.WordListUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoriteWordsViewModel @Inject constructor(
    private val getBookmarkedWordList: GetBookmarkedWordList,
    private val addBookmarkInteractor: AddBookmarkInteractor,
    private val removeBookmarkInteractor: RemoveBookmarkInteractor
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

    fun removeBookmark(word: Word) {
        viewModelScope.launch {
            removeBookmarkInteractor.removeBookmark(word.word)
                .collect {
                    if (it.status != Status.LOADING) {
                        if (it.status == Status.SUCCESS) {
                            setMessage(
                                Message.SnackBarMessage(
                                    message = "Removed ${word.word} from bookmarks",
                                    action = Action(
                                        "Undo"
                                    ) {
                                        viewModelScope.launch {
                                            addBookmarkInteractor.addBookmark(
                                                Bookmark(
                                                    word.bookmarkedId,
                                                    word.word,
                                                    word.bookmarkedAt,
                                                    word.bookmarkedSeenAt
                                                )
                                            ).collect {

                                            }

                                        }
                                    }
                                )
                            )
                        } else {
                            setMessage(
                                Message.SnackBarMessage(
                                    it.error?.message ?: "Unable to remove bookmark"
                                )
                            )
                        }
                    }
                }
        }
    }
}
