package com.pramod.dailyword.framework.ui.recap

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.pramod.dailyword.business.domain.model.Word
import com.pramod.dailyword.business.interactor.GetRecapWordsInteractor
import com.pramod.dailyword.framework.ui.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class RecapWordsViewModel @Inject constructor(
    private val getRecapWordsInteractor: GetRecapWordsInteractor
) : BaseViewModel() {

    val words: LiveData<List<Word>?> = MutableLiveData<List<Word>?>().apply {
        getRecapWordsInteractor.getRecap(7)
            .onEach {
                value = it.data
            }.launchIn(viewModelScope)
    }
}