package com.pramod.dailyword.framework.ui.worddetails

import android.util.Log
import androidx.lifecycle.*
import com.library.audioplayer.AudioPlayer
import com.pramod.dailyword.business.data.network.Resource
import com.pramod.dailyword.business.data.network.Status
import com.pramod.dailyword.business.domain.model.Word
import com.pramod.dailyword.business.interactor.GetRandomWordInteractor
import com.pramod.dailyword.business.interactor.GetWordDetailsByDateInteractor
import com.pramod.dailyword.business.interactor.bookmark.ToggleBookmarkInteractor
import com.pramod.dailyword.framework.ui.common.BaseViewModel
import com.pramod.dailyword.framework.ui.common.Message
import com.pramod.dailyword.framework.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class WordDetailedViewModel @Inject constructor(
    stateHandle: SavedStateHandle,
    private val getWordDetailsByDateInteractor: GetWordDetailsByDateInteractor,
    private val getRandomWordInteractor: GetRandomWordInteractor,
    private val toggleBookmarkInteractor: ToggleBookmarkInteractor,
    val audioPlayer: AudioPlayer
) : BaseViewModel() {

    private val wordDate = stateHandle.get<String>("WORD_DATE")


    companion object {
        val TAG = WordDetailedViewModel::class.simpleName
    }

    //this will be false when user open this activity first
    //when user manually pull to refresh this will be change to true
    private var shouldForceRefresh = wordDate?.let { false } ?: true

    private val refreshEvent = MutableLiveData<Unit>().apply {
        value = Unit
    }

    val loadingLiveData = MutableLiveData<Boolean>()

    var navigator: WordDetailNavigator? = null

    fun refresh() {
        shouldForceRefresh = true
        refreshEvent.value = Unit
    }

    private val _word = MutableLiveData<Word?>()

    val word: LiveData<Word?>
        get() = _word

    init {

        val wordResource: Flow<Resource<Word?>> = refreshEvent.switchMap {
            return@switchMap if (wordDate == null) {
                getRandomWordInteractor.getRandomWord()
            } else {
                getWordDetailsByDateInteractor.getWordDetailsByDate(wordDate, true)
            }.asLiveData(Dispatchers.IO)
        }.asFlow()

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

    fun navigateToMerriamWebster(): LiveData<Event<String>> = navigateToMerriamWebster

    fun bookmark() {
        val word = word.value
        word?.let {
            viewModelScope.launch {
                toggleBookmarkInteractor.toggle(
                    it.word
                ).collectLatest {

                }
            }
        }
    }

}