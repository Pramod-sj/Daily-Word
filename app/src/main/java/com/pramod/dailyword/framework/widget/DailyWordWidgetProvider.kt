package com.pramod.dailyword.framework.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetManager.*
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.core.os.BundleCompat
import androidx.core.os.bundleOf
import com.google.gson.Gson
import com.library.audioplayer.AudioPlayer
import com.pramod.dailyword.business.data.network.Status
import com.pramod.dailyword.business.domain.model.Word
import com.pramod.dailyword.business.interactor.bookmark.ToggleBookmarkInteractor
import com.pramod.dailyword.framework.ui.common.exts.goAsync
import com.pramod.dailyword.framework.ui.common.exts.safeLet
import com.pramod.dailyword.framework.ui.splash_screen.SplashScreenActivity
import com.pramod.dailyword.framework.util.safeNetworkCall
import com.pramod.dailyword.framework.widget.ClickType.*
import com.pramod.dailyword.framework.widget.pref.WidgetPreference
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber
import javax.inject.Inject


@AndroidEntryPoint
open class DailyWordWidgetProvider : AppWidgetProvider() {

    private val TAG = DailyWordWidgetProvider::class.simpleName

    @Inject
    lateinit var audioPlayer: AudioPlayer

    @Inject
    lateinit var toggleBookmarkInteractor: ToggleBookmarkInteractor

    @Inject
    lateinit var updateWidgetViewHelper: UpdateWidgetViewHelper

    @Inject
    lateinit var widgetPreference: WidgetPreference

    @Inject
    lateinit var widgetDataFetchHelper: WidgetDataFetchHelper

    @Inject
    lateinit var widgetPeriodicAlarmScheduler: WidgetPeriodicAlarmScheduler

    companion object {

        const val EXTRA_INTENT_TO_HOME_WORD_DATE = "word_date"

        const val ACTION_AUTO_UPDATE_WIDGET =
            "com.pramod.dailyword.ui.widget.DailyWordWidgetProvider.ACTION_AUTO_UPDATE_WIDGET"
        const val ACTION_TRY_AGAIN_FROM_WIDGET =
            "com.pramod.dailyword.ui.widget.DailyWordWidgetProvider.ACTION_TRY_AGAIN_FROM_WIDGET"
        const val ACTION_BOOKMARK_FROM_WIDGET =
            "com.pramod.dailyword.ui.widget.DailyWordWidgetProvider.ACTION_BOOKMARK_FROM_WIDGET"
        const val ACTION_PLAY_AUDIO_FROM_WIDGET =
            "com.pramod.dailyword.ui.widget.DailyWordWidgetProvider.ACTION_PLAY_AUDIO_FROM_WIDGET"
        const val ACTION_SCROLLABLE_WIDGET =
            "com.pramod.dailyword.ui.widget.DailyWordWidgetProvider.ACTION_SCROLLABLE_WIDGET"


        /**
         * This action will only make a network call when no word present in local,
         * it is introduce to load word from cache into widget when user open [HomeActivity]
         */
        const val ACTION_SILENT_REFRESH_WIDGET =
            "com.pramod.dailyword.ui.widget.DailyWordWidgetProvider.ACTION_SILENT_REFRESH_WIDGET"

        /**
         * This action will show random word whenever triggered
         */
        const val ACTION_RANDOM_WORD =
            "com.pramod.dailyword.ui.widget.DailyWordWidgetProvider.ACTION_RANDOM_WORD"

        const val EXTRA_AUDIO_URL = "audio_url"
        const val EXTRA_BOOKMARKED_WORD = "bookmarked_word"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        safeLet(intent, context) { i, c ->
            Timber.i("onReceive: " + i.action)
            //Toast.makeText(context, it.action, Toast.LENGTH_SHORT).show()
            when (i.action) {
                ACTION_SCROLLABLE_WIDGET -> {
                    Timber.i("onReceive: " +i.extras?.keySet()
                        ?.joinToString(", ", "{", "}") { key ->
                            "$key=${i.extras?.get(key)}"
                        })
                    val clickType = i.extras?.get(EXTRA_CLICK_TYPE) as? ClickType
                    when (clickType) {
                        PLAY_AUDIO -> {
                            c.safeNetworkCall {
                                i.getStringExtra(EXTRA_AUDIO_URL)?.let { audioUrl ->
                                    audioPlayer.play(audioUrl)
                                }
                            }
                        }

                        VIEW_FULL_WORD_DETAIL -> {
                            BundleCompat.getSerializable(
                                i.extras ?: bundleOf(),
                                EXTRA_WORD_DATA,
                                Word::class.java
                            )?.let { word ->
                                    context?.let {
                                        it.startActivity(
                                            Intent(
                                                context,
                                                SplashScreenActivity::class.java
                                            ).apply {
                                                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                putExtras(
                                                    bundleOf(EXTRA_INTENT_TO_HOME_WORD_DATE to word?.date)
                                                )
                                            })
                                    }

                                }
                        }

                        else -> Unit
                    }
                }

                ACTION_APPWIDGET_OPTIONS_CHANGED -> {
                    goAsync(Dispatchers.Main) {
                        val wordName = widgetPreference.getCurrentWordShown()
                        if (wordName != null) {
                            updateWidgetViewHelper.localFetchWordByNameAndUpdateWidgetUi(
                                wordName
                            )
                        } else {
                            widgetDataFetchHelper.runTodayWordFetchJob()
                        }
                    }

                }

                Intent.ACTION_TIME_CHANGED -> {
                    //stopping currently running job and starting again
                    widgetDataFetchHelper.stopTodayWordFetchJob()
                    widgetDataFetchHelper.runTodayWordFetchJob()
                    //cancelling existing alarm and rescheduling
                    widgetPeriodicAlarmScheduler.cancelRepeatingAlarm()
                    widgetPeriodicAlarmScheduler.setRepeatingDailyAlarmToFetch()
                }

                ACTION_TRY_AGAIN_FROM_WIDGET, ACTION_AUTO_UPDATE_WIDGET,
                ACTION_APPWIDGET_UPDATE, ACTION_APPWIDGET_ENABLED -> {
                    c.safeNetworkCall {
                        widgetDataFetchHelper.runTodayWordFetchJob()
                    }
                }

                ACTION_BOOKMARK_FROM_WIDGET -> {
                    goAsync(Dispatchers.Main) {
                        val word = i.getStringExtra(EXTRA_BOOKMARKED_WORD)
                        word?.let { bookmarked_word ->
                            toggleBookmarkInteractor.toggle(bookmarked_word).collectLatest {
                                Timber.i("toggle: " + Gson().toJson(it))
                                if (it.status != Status.LOADING) {
                                    updateWidgetViewHelper.localFetchWordByNameAndUpdateWidgetUi(
                                        bookmarked_word
                                    )
                                }
                            }
                        }
                    }
                }

                ACTION_PLAY_AUDIO_FROM_WIDGET -> {
                    Timber.i("onReceive: Playing")
                    c.safeNetworkCall {
                        i.getStringExtra(EXTRA_AUDIO_URL)?.let { audioUrl ->
                            audioPlayer.play(audioUrl)
                        }
                    }
                }

                ACTION_SILENT_REFRESH_WIDGET -> widgetDataFetchHelper.runTodayWordFetchJob(false)

                ACTION_RANDOM_WORD -> c.safeNetworkCall {
                    widgetDataFetchHelper.runRandomWordJob()
                }

                else -> Unit
            }
        }
    }

    override fun onEnabled(context: Context?) {
        super.onEnabled(context)
        Timber.i("onEnabled: ")
        widgetPeriodicAlarmScheduler.setRepeatingDailyAlarmToFetch()
    }

    override fun onDisabled(context: Context?) {
        Timber.i("onDisabled: ")
        widgetDataFetchHelper.stopTodayWordFetchJob()
        widgetPeriodicAlarmScheduler.cancelRepeatingAlarm()
        widgetPreference.removeAll()
        super.onDisabled(context)
    }

    override fun onUpdate(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetIds: IntArray?
    ) {
        /*goAsync(Dispatchers.Main) {
            val wordName = widgetPreference.getCurrentWordShown()
            if (wordName != null) {
                updateWidgetViewHelper.localFetchWordByNameAndUpdateWidgetUi(
                    wordName
                )
            } else {
                widgetDataFetchHelper.runTodayWordFetchJob()
            }
        }*/
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        safeLet(
            context,
            appWidgetIds?.firstOrNull()
                ?.let { appWidgetManager?.getAppWidgetOptions(it) }) { nonNullContext, bundle ->
            val widgetSize = getWidgetWidthAndHeight(nonNullContext, bundle)
            widgetPreference.setWidgetSize(widgetSize) //saving current widget size in shared pref
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        Timber.i("onAppWidgetOptionsChanged: ")
        context?.let {
            val widgetSize = getWidgetWidthAndHeight(context, newOptions)
            widgetPreference.setWidgetSize(widgetSize) //saving current widget size in shared pref
        }
    }

    private fun getWidgetWidthAndHeight(
        context: Context,
        newOptions: Bundle?
    ): WidgetPreference.WidgetSize {
        val width: Int
        val height: Int
        if (context.resources.configuration.orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            || context.resources.configuration.orientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
        ) {
            width = newOptions?.getInt(OPTION_APPWIDGET_MIN_WIDTH) ?: 0
            height = newOptions?.getInt(OPTION_APPWIDGET_MAX_HEIGHT) ?: 0
        } else {
            width = newOptions?.getInt(OPTION_APPWIDGET_MAX_WIDTH) ?: 0
            height = newOptions?.getInt(OPTION_APPWIDGET_MIN_HEIGHT) ?: 0
        }
        return WidgetPreference.WidgetSize(width, height)
    }

}