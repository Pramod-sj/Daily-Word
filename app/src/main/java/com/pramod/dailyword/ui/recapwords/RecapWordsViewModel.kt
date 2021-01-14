package com.pramod.dailyword.ui.recapwords

import android.app.Application
import androidx.lifecycle.Transformations
import com.pramod.dailyword.db.repository.WOTDRepository
import com.pramod.dailyword.ui.BaseViewModel

class RecapWordsViewModel(application: Application) :
    BaseViewModel(application) {
    private val wotdRepository: WOTDRepository = WOTDRepository(application)

    val words = Transformations.map(wotdRepository.recapWords(7)) { it.data }
}