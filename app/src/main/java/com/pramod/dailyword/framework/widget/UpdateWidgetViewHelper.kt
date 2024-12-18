package com.pramod.dailyword.framework.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.google.gson.Gson
import com.pramod.dailyword.R
import com.pramod.dailyword.business.data.cache.abstraction.BookmarkedWordCacheDataSource
import com.pramod.dailyword.business.data.network.Status
import com.pramod.dailyword.business.interactor.GetRandomWordInteractor
import com.pramod.dailyword.business.interactor.GetWordsInteractor
import com.pramod.dailyword.framework.util.NetworkUtils
import com.pramod.dailyword.framework.widget.pref.WidgetPreference
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateWidgetViewHelper @Inject constructor(
    private val bookmarkedWordCacheDataSource: BookmarkedWordCacheDataSource,
    private val widgetPreference: WidgetPreference,
    private val appWidgetManager: AppWidgetManager,
    @ApplicationContext private val context: Context,
    private val getWordsInteractor: GetWordsInteractor,
    private val getRandomWordInteractor: GetRandomWordInteractor,
    private val widgetViewHelper: WidgetViewHelper
) {

    suspend fun localFetchWordByNameAndUpdateWidgetUi(wordName: String) {
        val widgetSize = widgetPreference.getWidgetSize() ?: return

        val widgetComponent = ComponentName(context, DailyWordWidgetProvider::class.java)

        try {
            val word = bookmarkedWordCacheDataSource.getWordByNameNonLive(wordName)
            appWidgetManager.updateAppWidget(
                widgetComponent,
                widgetViewHelper.getRemoteViews(
                    word,
                    widgetSize.width,
                    widgetSize.height
                )
            )
        } catch (e: Exception) {
            Timber.i("onStartJob: Exception: $e")
            appWidgetManager.updateAppWidget(
                widgetComponent,
                widgetViewHelper.getResponsiveErrorRemoteView(
                    R.drawable.ic_info_outline_black_24dp,
                    context.resources.getString(R.string.widget_unable_to_fetch),//"Unable to fetch the word, try opening the app.",
                    widgetSize.width,
                    widgetSize.height
                )
            )
        } finally {
        }
    }

    suspend fun fetchRandomWordAndUpdateWidgetUi() {
        val widgetSize = widgetPreference.getWidgetSize() ?: return

        val widgetComponent = ComponentName(context, DailyWordWidgetProvider::class.java)

        try {

            if (!NetworkUtils.isNetworkActive(context)) {
                appWidgetManager.updateAppWidget(
                    widgetComponent,
                    widgetViewHelper.getResponsiveErrorRemoteView(
                        R.drawable.ic_round_signal_cellular_connected_no_internet_4_bar_24,
                        context.resources.getString(R.string.widget_please_check_internet),//"Please check your internet",
                        width = widgetSize.width,
                        height = widgetSize.height
                    )
                )
                return
            }

            //show progress loader
            appWidgetManager.updateAppWidget(
                widgetComponent, widgetViewHelper.getResponsiveLoadingRemoteView(
                    widgetSize.width, widgetSize.height
                )
            )

            val latestWordResource =
                getRandomWordInteractor.getRandomWord().firstOrNull { it.status != Status.LOADING }

            Timber.i("onStartJob: " + Gson().toJson(latestWordResource))

            if (latestWordResource?.status == Status.SUCCESS && latestWordResource.data != null) {
                val word = latestWordResource.data

                widgetPreference.setCurrentWordShown(word.word) //updating current showing word

                appWidgetManager.updateAppWidget(
                    widgetComponent,
                    widgetViewHelper.getRemoteViews(
                        word,
                        widgetSize.width,
                        widgetSize.height
                    )
                )

                Handler(Looper.myLooper()!!).postDelayed({
                    appWidgetManager.notifyAppWidgetViewDataChanged(
                        appWidgetManager.getAppWidgetIds(widgetComponent),
                        R.id.list_scrollable_content
                    )
                }, 50)

            } else {
                appWidgetManager.updateAppWidget(
                    widgetComponent,
                    widgetViewHelper.getResponsiveErrorRemoteView(
                        R.drawable.ic_round_signal_cellular_connected_no_internet_4_bar_24,
                        latestWordResource?.error?.message
                            ?: context.resources.getString(R.string.widget_something_went_wrong),//"Something went wrong! try again.",
                        width = widgetSize.width,
                        height = widgetSize.height
                    )
                )
            }
        } catch (e: Exception) {
            Timber.i("onStartJob: Exception: $e")
            appWidgetManager.updateAppWidget(
                widgetComponent,
                widgetViewHelper.getResponsiveErrorRemoteView(
                    R.drawable.ic_info_outline_black_24dp,
                    context.resources.getString(R.string.widget_unable_to_fetch),//"Unable to fetch the word, try opening the app.",
                    widgetSize.width,
                    widgetSize.height
                )
            )
        } finally {
        }

    }

    suspend fun fetchTodayWordAndUpdateWidgetUi(shouldCallApi: Boolean) {

        val widgetSize = widgetPreference.getWidgetSize() ?: return

        val widgetComponent = ComponentName(context, DailyWordWidgetProvider::class.java)

        try {

            val topWord = withContext(Dispatchers.IO) {
                bookmarkedWordCacheDataSource.getTopOneWord().firstOrNull()
            }

            if (topWord == null || shouldCallApi) {

                if (!NetworkUtils.isNetworkActive(context)) {
                    appWidgetManager.updateAppWidget(
                        widgetComponent,
                        widgetViewHelper.getResponsiveErrorRemoteView(
                            R.drawable.ic_round_signal_cellular_connected_no_internet_4_bar_24,
                            context.resources.getString(R.string.widget_please_check_internet), //"Please check your internet",
                            width = widgetSize.width,
                            height = widgetSize.height
                        )
                    )
                    return
                }

                //show progress loader
                appWidgetManager.updateAppWidget(
                    widgetComponent,
                    widgetViewHelper.getResponsiveLoadingRemoteView(
                        widgetSize.width,
                        widgetSize.height
                    )
                )

                Timber.i("onStartJob: Calling api")

                val latestWordResource = withContext(Dispatchers.IO) {
                    getWordsInteractor.getWords(1, true)
                        .firstOrNull { it.status != Status.LOADING }
                }

                Timber.i("onStartJob: " + Gson().toJson(latestWordResource))

                if (latestWordResource?.status == Status.SUCCESS
                    && latestWordResource.data?.isEmpty() == false
                ) {
                    val word = latestWordResource.data.first()

                    Timber.i("WORD:" + word.word)

                    widgetPreference.setCurrentWordShown(word.word) //updating current showing word

                    appWidgetManager.updateAppWidget(
                        widgetComponent,
                        widgetViewHelper.getRemoteViews(
                            word,
                            widgetSize.width,
                            widgetSize.height
                        )
                    )

                    Handler(Looper.myLooper()!!).postDelayed({
                        appWidgetManager.notifyAppWidgetViewDataChanged(
                            appWidgetManager.getAppWidgetIds(widgetComponent),
                            R.id.list_scrollable_content
                        )
                    }, 50)
                } else {
                    appWidgetManager.updateAppWidget(
                        widgetComponent,
                        widgetViewHelper.getResponsiveErrorRemoteView(
                            R.drawable.ic_round_signal_cellular_connected_no_internet_4_bar_24,
                            latestWordResource?.error?.message
                                ?: context.resources.getString(R.string.widget_something_went_wrong),//"Something went wrong! try again.",
                            width = widgetSize.width,
                            height = widgetSize.height
                        )
                    )
                }
            } else {
                appWidgetManager.updateAppWidget(
                    widgetComponent,
                    widgetViewHelper.getRemoteViews(
                        topWord,
                        widgetSize.width,
                        widgetSize.height
                    )
                )
            }
        } catch (e: Exception) {
            Timber.i("onStartJob: Exception: $e")
            appWidgetManager.updateAppWidget(
                widgetComponent,
                widgetViewHelper.getResponsiveErrorRemoteView(
                    R.drawable.ic_info_outline_black_24dp,
                    context.resources.getString(R.string.widget_unable_to_fetch),//"Unable to fetch the word, try opening the app.",
                    widgetSize.width,
                    widgetSize.height
                )
            )
        } finally {
        }
    }
}