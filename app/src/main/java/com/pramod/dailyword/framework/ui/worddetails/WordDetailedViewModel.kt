package com.pramod.dailyword.framework.ui.worddetails

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.pramod.dailyword.business.data.network.Resource
import com.pramod.dailyword.business.data.network.Status
import com.pramod.dailyword.business.domain.model.Bookmark
import com.pramod.dailyword.business.domain.model.Word
import com.pramod.dailyword.business.interactor.GetRandomWordInteractor
import com.pramod.dailyword.business.interactor.GetWordDetailsByDateInteractor
import com.pramod.dailyword.business.interactor.bookmark.AddBookmarkInteractor
import com.pramod.dailyword.business.interactor.bookmark.RemoveBookmarkInteractor
import com.pramod.dailyword.framework.ui.common.BaseViewModel
import com.pramod.dailyword.framework.ui.common.Message
import com.pramod.dailyword.framework.helper.PronounceHelper
import com.pramod.dailyword.framework.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.util.*
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class WordDetailedViewModel @Inject constructor(
    stateHandle: SavedStateHandle,
    private val getWordDetailsByDateInteractor: GetWordDetailsByDateInteractor,
    private val getRandomWordInteractor: GetRandomWordInteractor,
    private val addBookmarkInteractorr: AddBookmarkInteractor,
    private val removeBookmarkInteractor: RemoveBookmarkInteractor
) : BaseViewModel() {

    private val wordDate = stateHandle.get<String>("WORD_DATE")

    companion object {
        val TAG = WordDetailedViewModel::class.simpleName
    }

    //this will be false when user open this activity first
    //when user manually pull to refresh this will be change to true
    private var shouldForceRefresh = wordDate?.let { false } ?: true

    private val refreshEvent = MutableStateFlow(true)

    val loadingLiveData = MutableLiveData<Boolean>()

    var navigator: WordDetailNavigator? = null

    fun refresh() {
        shouldForceRefresh = true
        refreshEvent.value = true
    }

    private val _word = MutableLiveData<Word?>()

    val word: LiveData<Word?>
        get() = _word

    init {

        val wordResource: Flow<Resource<Word?>> = refreshEvent.transformLatest {
            if (refreshEvent.value) {
                emitAll(
                    if (wordDate == null) {
                        getRandomWordInteractor.getRandomWord()
                    } else {
                        getWordDetailsByDateInteractor.getWordDetailsByDate(wordDate, true)
                    }
                )
            }
        }

        wordResource
            .onEach {
                loadingLiveData.value = it.status == Status.LOADING
                if (it.status == Status.ERROR) {
                    setMessage(
                        Message.SnackBarMessage(
                            it.error?.message ?: "Something went wrong!!"
                        )
                    )
                }
                it.data?.let { word ->
                    _word.value = word
                }
            }.launchIn(viewModelScope)

    }

    private val showTitle = MutableLiveData<Boolean>().apply {
        value = false
    }

    private val navigateToMerriamWebster = MutableLiveData<Event<String>>()


    fun navigateToWordMW(url: String) {
        navigateToMerriamWebster.value = Event.init(url)
    }

    fun setTitleVisibility(show: Boolean) {
        showTitle.value = show
    }

    fun showTitle(): LiveData<Boolean> = showTitle

    private var _isAudioPronouncing = MutableLiveData<Boolean>().apply {
        value = false
    }

    val isAudioPronouncing: LiveData<Boolean>
        get() = _isAudioPronouncing

    fun pronounceWord(url: String) {
        Log.d("AUDIO URL", url)
        if (_isAudioPronouncing.value == false) {
            _isAudioPronouncing.value = true
            PronounceHelper.playAudio(url) {
                _isAudioPronouncing.value = false
            }
        }
    }

    fun navigateToMerriamWebster(): LiveData<Event<String>> = navigateToMerriamWebster

    fun bookmark() {
        val word = word.value
        word?.let {
            if (word.bookmarkedId == null) {
                addBookmarkInteractorr.addBookmark(
                    Bookmark(
                        bookmarkId = null,
                        bookmarkedWord = it.word,
                        bookmarkedAt = Calendar.getInstance().timeInMillis
                    )
                )
            } else {
                removeBookmarkInteractor.removeBookmark(wordName = it.word!!)
            }
        }
    }

}