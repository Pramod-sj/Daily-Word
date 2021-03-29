package com.pramod.dailyword.framework.prefmanagers

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import com.google.gson.Gson
import com.pramod.dailyword.business.data.cache.abstraction.BookmarkedWordCacheDataSource
import com.pramod.dailyword.business.interactor.bookmark.GetAllBookmarks
import com.pramod.dailyword.framework.ui.common.exts.isSunday
import com.pramod.dailyword.framework.util.CalenderUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import java.util.*

class HomeScreenBadgeManager(
    private val base: Context,
    private val bookmarkedWordCacheDataSource: BookmarkedWordCacheDataSource,
    private val getAllBookmarks: GetAllBookmarks
) : BasePreferenceManager(PREF_NAME, base) {

    fun updatedRandomWordReadOn() {
        sPrefManager.edit().putString(
            KEY_LAST_RANDOM_WORD_READ_ON,
            CalenderUtil.convertCalenderToString(
                Calendar.getInstance(), CalenderUtil.DATE_FORMAT
            )
        ).apply()
    }

    fun showBadgeOnRandomWord(): LiveData<Boolean> {
        val liveData = SPrefStringLiveData(sPrefManager, KEY_LAST_RANDOM_WORD_READ_ON, null)
        return liveData.map {
            return@map CalenderUtil.convertCalenderToString(
                Calendar.getInstance(),
                CalenderUtil.DATE_FORMAT
            ) != it
        }
    }

    fun updateLastRecapOpenedOn() {
        sPrefManager.edit().putString(
            KEY_LAST_DATE_SHOWN_RECAP_BADGE_ONLY_SUNDAYS,
            CalenderUtil.convertCalenderToString(
                Calendar.getInstance(), CalenderUtil.DATE_FORMAT
            )
        ).apply()
    }

    fun showBadgeOnRecap(): LiveData<Boolean> {
        val liveData =
            SPrefStringLiveData(
                sPrefManager,
                KEY_LAST_DATE_SHOWN_RECAP_BADGE_ONLY_SUNDAYS,
                null
            )
        return liveData.map {
            val lastDateTimeInMillis =
                CalenderUtil.convertStringToCalender(
                    it,
                    CalenderUtil.DATE_FORMAT
                )?.timeInMillis ?: 0
            val todayTimeInMillis = CalenderUtil.getCalendarInstance(true).timeInMillis

            Log.i(
                TAG,
                "showBadgeOnRecap: $lastDateTimeInMillis $todayTimeInMillis ${
                    Calendar.getInstance().isSunday()
                }"
            )

            if (Calendar.getInstance().isSunday() && lastDateTimeInMillis < todayTimeInMillis) {
                return@map true
            }
            return@map false

        }
    }


    /**
     * call this method to observe KEY_IS_NEW_BOOKMARKS_ADDED sPrefManager value
     */
    fun showBadgeOnBookmark(): LiveData<Boolean> {
        return getAllBookmarks.getBookmarks()
            .map {
                Log.i(TAG, "showBadgeOnBookmark: " + Gson().toJson(it))
                return@map it.data.let { bookmarkList ->
                    if (bookmarkList != null) {
                        for (b in bookmarkList) {
                            if (b.bookmarkSeenAt == null) {
                                return@let true
                            }
                        }
                    }
                    return@let false
                }
            }
            .onEach {
                Log.i(TAG, "showBadgeOnBookmark: " + it)
            }.asLiveData(Dispatchers.IO)
    }


    fun showBadgeOnWordList(): LiveData<Boolean> {
        val liveData = bookmarkedWordCacheDataSource.getFewWordsFromTop(7)
        return liveData.map {

            it?.let { wordList ->

                for (word in wordList) {
                    if (!word.isSeen) {
                        return@map true;
                    }
                }
            }
            return@map false;
        }
    }

    companion object {
        val TAG = HomeScreenBadgeManager::class.java.simpleName

        const val PREF_NAME = "home_screen_badge_pref"

        /**
         * this key will store date dd-MMM-yy of last date of reading a random word
         */
        private const val KEY_LAST_RANDOM_WORD_READ_ON = "last_random_word_read_on"

        /**
         * this key will help to identify whether badge was shown on sunday
         * this will only return true on sunday until user didn't open recap page
         */
        private const val KEY_LAST_DATE_SHOWN_RECAP_BADGE_ONLY_SUNDAYS =
            "last_date_shown_recap_badge_only_sundays"

    }
}