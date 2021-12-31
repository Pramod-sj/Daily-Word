package com.pramod.dailyword.framework.prefmanagers

import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import com.google.gson.Gson
import com.pramod.dailyword.business.data.cache.abstraction.BookmarkedWordCacheDataSource
import com.pramod.dailyword.business.interactor.bookmark.GetAllBookmarks
import com.pramod.dailyword.framework.ui.common.exts.isSunday
import com.pramod.dailyword.framework.util.CalenderUtil
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeScreenBadgeManager @Inject constructor(
    @ApplicationContext private val base: Context,
    private val bookmarkedWordCacheDataSource: BookmarkedWordCacheDataSource,
    private val getAllBookmarks: GetAllBookmarks,
    private val prefManager: PrefManager
) : BasePreferenceManager(PREF_NAME, base) {

    fun updatedRandomWordReadOn() {
        sPrefManager.edit().putString(
            KEY_LAST_RANDOM_WORD_READ_ON,
            CalenderUtil.convertCalenderToString(
                Calendar.getInstance(), CalenderUtil.DATE_FORMAT
            )
        ).apply()
    }

    /**
     * This method also handle hide badge setting
     */
    fun showBadgeOnRandomWord(): LiveData<Boolean> {
        val liveData = SPrefStringLiveData(sPrefManager, KEY_LAST_RANDOM_WORD_READ_ON, null)
        return prefManager.getHideBadgeLiveData().switchMap { hideBadge ->
            if (!hideBadge) {
                return@switchMap liveData.map {
                    return@map CalenderUtil.convertCalenderToString(
                        Calendar.getInstance(),
                        CalenderUtil.DATE_FORMAT
                    ) != it
                }
            } else
                MutableLiveData<Boolean>().apply {
                    value = false
                }
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

    /**
     * This method also handle hide badge setting
     */
    fun showBadgeOnRecap(): LiveData<Boolean> {
        val liveData =
            SPrefStringLiveData(
                sPrefManager,
                KEY_LAST_DATE_SHOWN_RECAP_BADGE_ONLY_SUNDAYS,
                null
            )
        return prefManager.getHideBadgeLiveData().switchMap { hideBadge ->
            if (!hideBadge) {
                return@switchMap liveData.map {
                    val lastDateTimeInMillis =
                        CalenderUtil.convertStringToCalender(
                            it,
                            CalenderUtil.DATE_FORMAT
                        )?.timeInMillis ?: 0
                    val todayTimeInMillis = CalenderUtil.getCalendarInstance(true).timeInMillis

                    if (Calendar.getInstance()
                            .isSunday() && lastDateTimeInMillis < todayTimeInMillis
                    ) {
                        return@map true
                    }
                    return@map false
                }
            } else
                MutableLiveData<Boolean>().apply {
                    value = false
                }
        }
    }

    /**
     * call this method to observe KEY_IS_NEW_BOOKMARKS_ADDED sPrefManager value
     *  This method also handle hide badge setting
     */
    fun showBadgeOnBookmark(): LiveData<Boolean> {
        return prefManager.getHideBadgeLiveData().switchMap { hideBadge ->
            if (!hideBadge) {
                return@switchMap getAllBookmarks.getBookmarks()
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
                    .asLiveData(Dispatchers.IO)
            } else
                MutableLiveData<Boolean>().apply {
                    value = false
                }
        }
    }

    /**
     * This method also handle hide badge setting
     */
    fun showBadgeOnWordList(): LiveData<Boolean> {
        val liveData = bookmarkedWordCacheDataSource.getFewWordsFromTop(7)
        return prefManager.getHideBadgeLiveData().switchMap { hideBadge ->
            if (!hideBadge) {
                return@switchMap liveData.map {
                    it?.let { wordList ->

                        for (word in wordList) {
                            if (!word.isSeen) {
                                return@map true
                            }
                        }
                    }
                    return@map false
                }
            }
            return@switchMap MutableLiveData<Boolean>().apply {
                value = false
            }
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