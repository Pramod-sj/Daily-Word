package com.pramod.dailyword.widget

import android.app.job.JobParameters
import android.app.job.JobService
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.pramod.dailyword.R
import com.pramod.dailyword.db.Resource
import com.pramod.dailyword.db.model.WordOfTheDay
import com.pramod.dailyword.db.repository.WOTDRepository
import com.pramod.dailyword.util.CalenderUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class WidgetDataLoadService : JobService() {
    private val TAG = WidgetDataLoadService::class.simpleName
    private lateinit var appWidgetManager: AppWidgetManager


    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "onCreate: Widget Data Load Service Created")
        appWidgetManager =
            baseContext.getSystemService(Context.APPWIDGET_SERVICE) as AppWidgetManager
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        Log.i(TAG, "onStartJob: ")
        GlobalScope.launch(Dispatchers.Main) {

            try {
                val widgetComponent = ComponentName(baseContext, WordWidgetProvider::class.java)
                val widgetComponentSmall =
                    ComponentName(baseContext, SmallWordWidgetProvider::class.java)


                val wotdRepository = WOTDRepository(baseContext)

                //show progress loader
                appWidgetManager.updateAppWidget(
                    widgetComponent,
                    WidgetViewHelper.createLoadingWidget(
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


                var localWord = wotdRepository.getJustNonLive(
                    CalenderUtil.convertCalenderToString(
                        Calendar.getInstance(Locale.US)
                    )
                )

                if (localWord == null) {

                    val resource: Resource<List<WordOfTheDay>?> =
                        wotdRepository.getWords(null, 1)

                    Log.i(TAG, "onStartJob: " + Gson().toJson(resource))

                    if (resource.status == Resource.Status.SUCCESS) {
                        if (resource.data != null && resource.data.isNotEmpty()) {
                            //storing word in local
                            wotdRepository.addWord(resource.data[0])
                            //get word with bookmark status
                            localWord = wotdRepository.getJustNonLive(
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
                        } else {
                            appWidgetManager.updateAppWidget(
                                widgetComponent,
                                WidgetViewHelper.createPlaceHolderWidget(
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
                                resource.message ?: "Something went wrong! try again."
                            )
                        )

                        //updating small widget view
                        appWidgetManager.updateAppWidget(
                            widgetComponentSmall,
                            WidgetViewHelper.createPlaceHolderWidgetSmall(
                                baseContext,
                                R.drawable.ic_round_signal_cellular_connected_no_internet_4_bar_24,
                                resource.message ?: "Something went wrong! try again."
                            )
                        )
                    }

                } else {
                    appWidgetManager.updateAppWidget(
                        widgetComponent,
                        WidgetViewHelper.createWordOfTheDayWidget(baseContext, localWord)
                    )

                    //update small widget
                    appWidgetManager.updateAppWidget(
                        widgetComponentSmall,
                        WidgetViewHelper.createWordOfTheDayWidgetSmall(baseContext, localWord)
                    )
                }

            } catch (e: Exception) {
                Log.i(TAG, "onStartJob: Exception: $e")
            } finally {
                jobFinished(params, false)
            }
        }
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        Log.i(TAG, "onStopJob: ")
        return true
    }

    override fun onDestroy() {
        Log.i(TAG, "onDestroy: Widget Data Load Service Destroyed!")
        super.onDestroy()
    }
}