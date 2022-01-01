package com.pramod.dailyword.framework.ui.common.word

import android.content.Context
import com.pramod.dailyword.business.domain.model.Word
import com.pramod.dailyword.framework.prefmanagers.ThemeManager
import com.pramod.dailyword.framework.util.CalenderUtil
import com.pramod.dailyword.framework.util.CommonUtils

sealed class WordListUiModel {
    data class WordItem(val index: Long, val word: Word) : WordListUiModel() {

        fun getCardRippleColor(context: Context): Int {
            return CommonUtils.changeAlpha(getWordColor(context), 30)
        }

        fun getDateBackgroundColor(context: Context): Int {
            return CommonUtils.changeAlpha(getWordColor(context), 30)
        }

        /**
         * this method also handle condition when word color resId in db is -1
         * @return word color
         */
        fun getWordColor(context: Context): Int {
            val isDarkMode = ThemeManager.isNightModeActive(context)
            val wordColorResId = if (isDarkMode) word.wordDesaturatedColor else word.wordColor
            if (wordColorResId != -1) {
                return CommonUtils.getColor(context, wordColorResId)
            } else {
                //handling code when word color resId is null
                val cal = CalenderUtil.convertStringToCalender(
                    word.date,
                    CalenderUtil.DATE_FORMAT
                )
                val dayColor = CommonUtils.getColorBasedOnDay(cal)
                return CommonUtils.getColor(context, if (isDarkMode) dayColor[1] else dayColor[0])
            }
        }


        fun getDay(): String? {
            return CalenderUtil.getDayFromDateString(word.date, CalenderUtil.DATE_FORMAT)
        }

        fun getMonth(): String? {
            return word.date?.let {
                CalenderUtil.getMonthFromDateString(
                    it,
                    CalenderUtil.DATE_FORMAT
                )
            }
        }

    }

    data class AdItem(val adId: String) : WordListUiModel()
}
