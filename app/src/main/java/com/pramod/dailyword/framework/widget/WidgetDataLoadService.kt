package com.pramod.dailyword.framework.widget

import android.app.job.JobParameters
import android.app.job.JobService
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import androidx.paging.ExperimentalPagingApi
import com.google.gson.Gson
import com.pramod.dailyword.R
import com.pramod.dailyword.business.data.cache.abstraction.BookmarkedWordCacheDataSource
import com.pramod.dailyword.business.data.cache.abstraction.WordCacheDataSource
import com.pramod.dailyword.business.data.network.Resource
import com.pramod.dailyword.business.data.network.Status
import com.pramod.dailyword.business.data.network.abstraction.WordNetworkDataSource
import com.pramod.dailyword.business.data.network.utils.ApiResponseHandler
import com.pramod.dailyword.business.data.network.utils.safeApiCall
import com.pramod.dailyword.business.domain.model.Word
import com.pramod.dailyword.framework.datasource.network.model.api.ApiResponse
import com.pramod.dailyword.framework.util.CalenderUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class WidgetDataLoadService : JobService() {
    private val TAG = WidgetDataLoadService::class.simpleName
    private lateinit var appWidgetManager: AppWidgetManager

    @Inject
    lateinit var wordCacheDataSource: WordCacheDataSource

    @OptIn(ExperimentalPagingApi::class)
    @Inject
    lateinit var bookmarkedWordCacheDataSource: BookmarkedWordCacheDataSource

    @Inject
    lateinit var wordNetworkDataSource: WordNetworkDataSource

    override fun onCreate() {
        super.onCreate()
        Timber.i( "onCreate: Widget Data Load Service Created")
        appWidgetManager =
            baseContext.getSystemService(Context.APPWIDGET_SERVICE) as AppWidgetManager
    }

    @OptIn(ExperimentalPagingApi::class)
    override fun onStartJob(params: JobParameters?): Boolean {
        Timber.i( "onStartJob: ")
        CoroutineScope(Dispatchers.Main).launch {

            try {
                val widgetComponent = ComponentName(baseContext, WordWidgetProvider::class.java)
                val widgetComponentMedium =
                    ComponentName(baseContext, MediumWordWidgetProvider::class.java)
                val widgetComponentSmall =
                    ComponentName(baseContext, SmallWordWidgetProvider::class.java)


                //show progress loader
                appWidgetManager.updateAppWidget(
                    widgetComponent,
                    WidgetViewHelper.createLoadingWidget(
                        baseContext
                    )
                )

                //show progress loader for medium widget
                appWidgetManager.updateAppWidget(
                    widgetComponentMedium,
                    WidgetViewHelper.createLoadingWidgetMedium(
                        baseContext
                    )
                )

                //show progress loader for small widget
                appWidgetManager.updateAppWidget(
                    widgetComponentSmall,
                    WidgetViewHelper.createLoadingWidgetSmall(
                        baseContext
                    )
                )


                var localWord = bookmarkedWordCacheDataSource.getWordNonLive(
                    CalenderUtil.convertCalenderToString(
                        Calendar.getInstance(Locale.US)
                    )
                )

                if (localWord == null) {

                    val apiResponse = safeApiCall(Dispatchers.IO) {
                        wordNetworkDataSource.getWords(null, 1)
                    }

                    val resource = object :
                        ApiResponseHandler<ApiResponse<List<Word>>, List<Word>>(apiResponse) {
                        override suspend fun handleSuccess(resultObj: ApiResponse<List<Word>>): Resource<List<Word>?> {
                            return if (resultObj.code == "200") {
                                Resource.success(resultObj.data)
                            } else {
                                Resource.error(Throwable(resultObj.message), null)
                            }
                        }

                    }.getResult()

                    Timber.i( "onStartJob: " + Gson().toJson(resource))

                    if (resource.status == Status.SUCCESS) {
                        if (resource.data != null && resource.data.isNotEmpty()) {
                            //storing word in local
                            wordCacheDataSource.addAll(resource.data)
                            //get word with bookmark status
                            localWord = bookmarkedWordCacheDataSource.getWordNonLive(
                                resource.data[0].date ?: CalenderUtil.convertCalenderToString(
                                    Calendar.getInstance(Locale.US)
                                )
                            )

                            appWidgetManager.updateAppWidget(
                                widgetComponent,
                                WidgetViewHelper.createWordOfTheDayWidget(baseContext, localWord)
                            )

                            //updating small widget view
                            appWidgetManager.updateAppWidget(
                                widgetComponentSmall,
                                WidgetViewHelper.createWordOfTheDayWidgetSmall(
                                    baseContext,
                                    localWord
                                )
                            )

                            //updating medium widget view
                            appWidgetManager.updateAppWidget(
                                widgetComponentMedium,
                                WidgetViewHelper.createWordOfTheDayWidgetMedium(
                                    baseContext,
                                    localWord
                                )
                            )
                        } else {
                            appWidgetManager.updateAppWidget(
                                widgetComponent,
                                WidgetViewHelper.createPlaceHolderWidget(
                                    baseContext,
                                    R.drawable.ic_vocabulary,
                                    "No word of the day found!"
                                )
                            )


                            //updating medium widget view
                            appWidgetManager.updateAppWidget(
                                widgetComponentMedium,
                                WidgetViewHelper.createPlaceHolderWidgetMedium(
                                    baseContext,
                                    R.drawable.ic_vocabulary,
                                    "No word of the day found!"
                                )
                            )

                            //updating small widget view
                            appWidgetManager.updateAppWidget(
                                widgetComponentSmall,
                                WidgetViewHelper.createPlaceHolderWidgetSmall(
                                    baseContext,
                                    R.drawable.ic_vocabulary,
                                    "No word of the day found!"
                                )
                            )
                        }
                    } else {
                        appWidgetManager.updateAppWidget(
                            widgetComponent,
                            WidgetViewHelper.createPlaceHolderWidget(
                                baseContext,
                                R.drawable.ic_round_signal_cellular_connected_no_internet_4_bar_24,
                                resource.error?.message ?: "Something went wrong! try again."
                            )
                        )

                        //updating medium widget view
                        appWidgetManager.updateAppWidget(
                            widgetComponentMedium,
                            WidgetViewHelper.createPlaceHolderWidgetMedium(
                                baseContext,
                                R.drawable.ic_round_signal_cellular_connected_no_internet_4_bar_24,
                                resource.error?.message ?: "Something went wrong! try again."
                            )
                        )

                        //updating small widget view
                        appWidgetManager.updateAppWidget(
                            widgetComponentSmall,
                            WidgetViewHelper.createPlaceHolderWidgetSmall(
                                baseContext,
                                R.drawable.ic_round_signal_cellular_connected_no_internet_4_bar_24,
                                resource.error?.message ?: "Something went wrong! try again."
                            )
                        )
                    }

                } else {
                    appWidgetManager.updateAppWidget(
                        widgetComponent,
                        WidgetViewHelper.createWordOfTheDayWidget(baseContext, localWord)
                    )
                    //update medium widget
                    appWidgetManager.updateAppWidget(
                        widgetComponentMedium,
                        WidgetViewHelper.createWordOfTheDayWidgetMedium(baseContext, localWord)
                    )

                    //update small widget
                    appWidgetManager.updateAppWidget(
                        widgetComponentSmall,
                        WidgetViewHelper.createWordOfTheDayWidgetSmall(baseContext, localWord)
                    )
                }

            } catch (e: Exception) {
                Timber.i( "onStartJob: Exception: $e")
            } finally {
                jobFinished(params, false)
            }

        }
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        Timber.i( "onStopJob: ")
        return false
    }

    override fun onDestroy() {
        Timber.i( "onDestroy: Widget Data Load Service Destroyed!")
        super.onDestroy()
    }
}