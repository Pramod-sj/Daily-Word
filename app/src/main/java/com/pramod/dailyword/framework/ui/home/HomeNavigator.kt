package com.pramod.dailyword.framework.ui.home

import android.view.View
import com.pramod.dailyword.business.domain.model.Word

interface HomeNavigator {
    fun copyToClipboard(word: Word?)
    fun readMore(v: View?, word: Word?)
    fun learnAll(v: View?)
    fun gotoBookmark(v: View?)
    fun gotoRecap(v: View?)
    fun gotoRandomWord(v: View?)
}