package com.pramod.dailyword.framework.widget

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.view.View
import android.widget.RemoteViews
import androidx.core.os.bundleOf
import com.pramod.dailyword.Constants
import com.pramod.dailyword.R
import com.pramod.dailyword.business.domain.model.Word
import com.pramod.dailyword.framework.helper.safeImmutableFlag
import com.pramod.dailyword.framework.ui.splash_screen.SplashScreenActivity
import com.pramod.dailyword.framework.util.convertNumberRangeToAnotherRange
import com.pramod.dailyword.framework.widget.pref.Controls
import com.pramod.dailyword.framework.widget.pref.WidgetPreference
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.ceil

fun RemoteViews.applyControlVisibility(widgetPreference: WidgetPreference): RemoteViews {

    val visibleControls = widgetPreference.getVisibleWidgetControls()

    if (visibleControls.any { it == Controls.BOOKMARK.label }) {
        setViewVisibility(R.id.widget_bookmark, View.VISIBLE)
    } else {
        setViewVisibility(R.id.widget_bookmark, View.GONE)
    }
    if (visibleControls.any { it == Controls.RANDOM_WORD.label }) {
        setViewVisibility(R.id.widget_random_word, View.VISIBLE)
    } else {
        setViewVisibility(R.id.widget_random_word, View.GONE)
    }

    return this
}

@Singleton
class WidgetViewHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val widgetPreference: WidgetPreference
) {

    /**
     * Returns number of cells needed for given size of the widget.
     *
     * @param size Widget size in dp.
     * @return Size in number of cells.
     */
    private fun getCellsForSize(size: Int): Int {
        return ceil(((size + 30) / 70).toDouble()).toInt()
    }


    private fun isTablet(context: Context): Boolean {
        return ((context.resources.configuration.screenLayout
                and Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE)
    }

    fun getRemoteViews(
        word: Word?,
        width: Int,
        height: Int
    ): RemoteViews {
        val rowCell = getCellsForSize(height)
        val colCell = getCellsForSize(width)

        Timber.i("Col:%s ; Rows:%s", colCell, rowCell)
        return (if (isTablet(context)) {
            if (rowCell in 1 until 3 || colCell in 1 until 4) {
                createWordOfTheDayWidgetMedium(context, word)
            } else {
                createWordOfTheDayWidget(context, word)
            }
        } else {
            if (rowCell in 1 until 2 || colCell in 1 until 3) {
                createWordOfTheDayWidgetSmall(context, word)
            } else if (rowCell in 3 until 4 || colCell in 3 until 4) {
                createWordOfTheDayWidgetMedium(context, word)
            } else {
                createWordOfTheDayWidget(context, word)
            }
        }).apply {
            //setting alpha of widget widget
            setInt(
                R.id.iv_body_bg, "setImageAlpha", convertNumberRangeToAnotherRange(
                    oldValue = widgetPreference.getWidgetBodyAlpha().toFloat(),
                    oldRange = 0 to 100,
                    newRange = 0 to 255
                )
            )
            setInt(
                R.id.iv_bg, "setImageAlpha",
                convertNumberRangeToAnotherRange(
                    oldValue = widgetPreference.getWidgetBackgroundAlpha().toFloat(),
                    oldRange = 0 to 100,
                    newRange = 0 to 255
                )
            )
            applyControlVisibility(widgetPreference)
        }

    }

    fun getResponsiveLoadingRemoteView(
        width: Int,
        height: Int
    ): RemoteViews {
        val rowCell = getCellsForSize(height)
        val colCell = getCellsForSize(width)
        return (if (isTablet(context)) {
            if (rowCell in 1 until 3 || colCell in 1 until 4) {
                createLoadingWidgetMedium(context)
            } else {
                createLoadingWidget(context)
            }
        } else {
            if (rowCell in 1 until 2 || colCell in 1 until 3) {
                createLoadingWidgetSmall(context)
            } else if (rowCell in 3 until 4 || colCell in 3 until 4) {
                createLoadingWidgetMedium(context)
            } else {
                createLoadingWidget(context)
            }
        }).apply {
            //setting alpha of widget widget
            setInt(
                R.id.iv_body_bg, "setImageAlpha", convertNumberRangeToAnotherRange(
                    oldValue = widgetPreference.getWidgetBodyAlpha().toFloat(),
                    oldRange = 0 to 100,
                    newRange = 0 to 255
                )
            )
            setInt(
                R.id.iv_bg, "setImageAlpha",
                convertNumberRangeToAnotherRange(
                    oldValue = widgetPreference.getWidgetBackgroundAlpha().toFloat(),
                    oldRange = 0 to 100,
                    newRange = 0 to 255
                )
            )
            applyControlVisibility(widgetPreference)
        }
    }


    fun getResponsiveErrorRemoteView(
        resId: Int, message: String, width: Int, height: Int
    ): RemoteViews {
        val rowCell = getCellsForSize(height)
        val colCell = getCellsForSize(width)
        return (if (isTablet(context)) {
            if (rowCell in 1 until 3 || colCell in 1 until 4) {
                createPlaceHolderWidgetMedium(context, resId, message)
            } else {
                createPlaceHolderWidget(context, resId, message)
            }
        } else {
            if (rowCell in 1 until 2 || colCell in 1 until 3) {
                createPlaceHolderWidgetSmall(context, resId, message)
            } else if (rowCell in 3 until 4 || colCell in 3 until 4) {
                createPlaceHolderWidgetMedium(context, resId, message)
            } else {
                createPlaceHolderWidget(context, resId, message)
            }
        })
            .apply {
                //setting alpha of widget widget
                setInt(
                    R.id.iv_body_bg, "setImageAlpha", convertNumberRangeToAnotherRange(
                        oldValue = widgetPreference.getWidgetBodyAlpha().toFloat(),
                        oldRange = 0 to 100,
                        newRange = 0 to 255
                    )
                )
                setInt(
                    R.id.iv_bg, "setImageAlpha",
                    convertNumberRangeToAnotherRange(
                        oldValue = widgetPreference.getWidgetBackgroundAlpha().toFloat(),
                        oldRange = 0 to 100,
                        newRange = 0 to 255
                    )
                )
                applyControlVisibility(widgetPreference)
            }
    }


    private fun createWordOfTheDayWidget(context: Context, word: Word?): RemoteViews {
        val views =
            RemoteViews(context.packageName, R.layout.widget_word_layout_revamp)
        views.setViewVisibility(R.id.widget_placeholder, View.INVISIBLE)
        views.setViewVisibility(R.id.widget_content, View.VISIBLE)
        views.setViewVisibility(R.id.widget_progress, View.INVISIBLE)

        if (word != null) {
            views.setTextViewText(R.id.widget_txtView_word_of_the_day, word.word)
            views.setTextViewText(R.id.widget_txtView_attribute, word.attribute)
            views.setTextViewText(R.id.widget_txtView_pronounce, word.pronounce)
            if (!word.meanings.isNullOrEmpty()) {
                views.setTextViewText(R.id.widget_txtView_meanings, word.meanings.get(0))
            }
            views.setTextViewText(
                R.id.widget_txtView_how_to_user_word,
                "How to use ${word.word}"
            )
            if (!word.examples.isNullOrEmpty()) {
                views.setTextViewText(
                    R.id.widget_txtView_how_to_user_word_desc,
                    word.examples.get(0)
                )
            }
            //pronounce audio pending intent
            val playAudioIntent = Intent(context, DailyWordWidgetProvider::class.java)
            playAudioIntent.action =
                DailyWordWidgetProvider.ACTION_PLAY_AUDIO_FROM_WIDGET
            playAudioIntent.putExtras(
                bundleOf(
                    Pair(
                        DailyWordWidgetProvider.EXTRA_AUDIO_URL,
                        word.pronounceAudio
                    )
                )
            )

            val pendingIntentPlayAudio = PendingIntent.getBroadcast(
                context,
                Constants.REQUEST_CODE_PENDING_INTENT_ON_WIDGET_PRONOUNCE_CLICK,
                playAudioIntent,
                safeImmutableFlag(PendingIntent.FLAG_UPDATE_CURRENT)
            )
            views.setOnClickPendingIntent(R.id.widget_img_pronounce, pendingIntentPlayAudio)
            //end

            //bookmark status
            views.setViewVisibility(R.id.widget_bookmark, View.VISIBLE)
            views.setImageViewResource(
                R.id.widget_bookmark,
                if (word.bookmarkedId != null) R.drawable.ic_round_bookmark_24
                else R.drawable.ic_baseline_bookmark_border_24
            )
            //end

            //bookmark pending intent
            val bookmarkIntent = Intent(context, DailyWordWidgetProvider::class.java)
            bookmarkIntent.putExtras(
                bundleOf(
                    Pair(
                        DailyWordWidgetProvider.EXTRA_BOOKMARKED_WORD,
                        word.word
                    )
                )
            )
            bookmarkIntent.action =
                DailyWordWidgetProvider.ACTION_BOOKMARK_FROM_WIDGET


            val pendingIntentBookmark = PendingIntent.getBroadcast(
                context,
                Constants.REQUEST_CODE_PENDING_INTENT_ON_WIDGET_BOOKMARK,
                bookmarkIntent,
                safeImmutableFlag(PendingIntent.FLAG_UPDATE_CURRENT)
            )
            views.setOnClickPendingIntent(R.id.widget_bookmark, pendingIntentBookmark)
            //end


            //bookmark pending intent
            val randomWordIntent = Intent(context, DailyWordWidgetProvider::class.java)
            randomWordIntent.putExtras(
                bundleOf(
                    Pair(
                        DailyWordWidgetProvider.EXTRA_BOOKMARKED_WORD,
                        word.word
                    )
                )
            )
            randomWordIntent.action =
                DailyWordWidgetProvider.ACTION_RANDOM_WORD


            val pendingIntentRandomWord = PendingIntent.getBroadcast(
                context,
                Constants.REQUEST_CODE_PENDING_INTENT_ON_SHOW_RANDOM,
                randomWordIntent,
                safeImmutableFlag(PendingIntent.FLAG_UPDATE_CURRENT)
            )
            views.setOnClickPendingIntent(R.id.widget_random_word, pendingIntentRandomWord)
            //end


        } else {
            views.setViewVisibility(R.id.widget_bookmark, View.INVISIBLE)
        }


        val tryAgainIntent = Intent(context, DailyWordWidgetProvider::class.java)
        tryAgainIntent.action =
            DailyWordWidgetProvider.ACTION_TRY_AGAIN_FROM_WIDGET

        val pendingIntentTryAgain = PendingIntent.getBroadcast(
            context,
            Constants.REQUEST_CODE_PENDING_INTENT_ON_WIDGET_TRY_AGAIN_CLICK,
            tryAgainIntent,
            safeImmutableFlag(PendingIntent.FLAG_UPDATE_CURRENT)
        )
        views.setOnClickPendingIntent(R.id.widget_retry, pendingIntentTryAgain)


        val pendingIntentForWidgetClick = PendingIntent.getActivity(
            context,
            Constants.REQUEST_CODE_PENDING_INTENT_ON_WIDGET_CLICK,
            Intent(context, SplashScreenActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                putExtras(
                    bundleOf(DailyWordWidgetProvider.EXTRA_INTENT_TO_HOME_WORD_DATE to word?.date)
                )
            },
            safeImmutableFlag(PendingIntent.FLAG_CANCEL_CURRENT)
        )
        views.setOnClickPendingIntent(R.id.main_linearLayout_wotd, pendingIntentForWidgetClick)

        return views
    }

    private fun createPlaceHolderWidget(
        context: Context,
        resId: Int,
        message: String
    ): RemoteViews {
        val views =
            RemoteViews(context.packageName, R.layout.widget_word_layout_revamp)
        views.setViewVisibility(R.id.widget_content, View.INVISIBLE)
        views.setViewVisibility(R.id.widget_placeholder, View.VISIBLE)
        views.setViewVisibility(R.id.widget_progress, View.INVISIBLE)

        views.setImageViewResource(R.id.widget_placeHolder_imageView, resId)
        views.setTextViewText(R.id.widget_placeHolder_imageView_message, message)

        return views
    }

    private fun createLoadingWidget(context: Context): RemoteViews {
        val views =
            RemoteViews(context.packageName, R.layout.widget_word_layout_revamp)
        views.setViewVisibility(R.id.widget_content, View.INVISIBLE)
        views.setViewVisibility(R.id.widget_placeholder, View.INVISIBLE)
        views.setViewVisibility(R.id.widget_progress, View.VISIBLE)
        return views
    }


    private fun createWordOfTheDayWidgetMedium(context: Context, word: Word?): RemoteViews {
        val views =
            RemoteViews(context.packageName, R.layout.widget_word_layout_medium_revamp)
        views.setViewVisibility(R.id.widget_placeholder, View.INVISIBLE)
        views.setViewVisibility(R.id.widget_content, View.VISIBLE)
        views.setViewVisibility(R.id.widget_progress, View.INVISIBLE)

        if (word != null) {

            views.setTextViewText(R.id.widget_txtView_word_of_the_day, word.word)
            views.setTextViewText(R.id.widget_txtView_attribute, word.attribute)
            views.setTextViewText(R.id.widget_txtView_pronounce, word.pronounce)

            if (!word.meanings.isNullOrEmpty()) {
                views.setTextViewText(R.id.widget_txtView_meanings, word.meanings.get(0))
            }

            //bookmark status
            views.setViewVisibility(R.id.widget_bookmark, View.VISIBLE)
            views.setImageViewResource(
                R.id.widget_bookmark,
                if (word.bookmarkedId != null) R.drawable.ic_round_bookmark_24
                else R.drawable.ic_baseline_bookmark_border_24
            )
            //end

            //bookmark pending intent
            val bookmarkIntent = Intent(context, DailyWordWidgetProvider::class.java)
            bookmarkIntent.putExtras(
                bundleOf(
                    Pair(
                        DailyWordWidgetProvider.EXTRA_BOOKMARKED_WORD,
                        word.word
                    )
                )
            )
            bookmarkIntent.action =
                DailyWordWidgetProvider.ACTION_BOOKMARK_FROM_WIDGET

            val pendingIntentBookmark = PendingIntent.getBroadcast(
                context,
                Constants.REQUEST_CODE_PENDING_INTENT_ON_WIDGET_BOOKMARK,
                bookmarkIntent,
                safeImmutableFlag(PendingIntent.FLAG_UPDATE_CURRENT)
            )
            views.setOnClickPendingIntent(R.id.widget_bookmark, pendingIntentBookmark)
            //end

            //pronounce audio pending intent
            val playAudioIntent = Intent(context, DailyWordWidgetProvider::class.java)
            playAudioIntent.action =
                DailyWordWidgetProvider.ACTION_PLAY_AUDIO_FROM_WIDGET
            playAudioIntent.putExtras(
                bundleOf(
                    Pair(
                        DailyWordWidgetProvider.EXTRA_AUDIO_URL,
                        word.pronounceAudio
                    )
                )
            )

            val pendingIntentPlayAudio = PendingIntent.getBroadcast(
                context,
                Constants.REQUEST_CODE_PENDING_INTENT_ON_WIDGET_PRONOUNCE_CLICK,
                playAudioIntent,
                safeImmutableFlag(PendingIntent.FLAG_UPDATE_CURRENT)
            )
            views.setOnClickPendingIntent(R.id.widget_img_pronounce, pendingIntentPlayAudio)
            //end

        } else {
            views.setViewVisibility(R.id.widget_bookmark, View.INVISIBLE)
        }


        val tryAgainIntent = Intent(context, DailyWordWidgetProvider::class.java)
        tryAgainIntent.action =
            DailyWordWidgetProvider.ACTION_TRY_AGAIN_FROM_WIDGET

        val pendingIntentTryAgain = PendingIntent.getBroadcast(
            context,
            Constants.REQUEST_CODE_PENDING_INTENT_ON_WIDGET_TRY_AGAIN_CLICK,
            tryAgainIntent,
            safeImmutableFlag(PendingIntent.FLAG_UPDATE_CURRENT)
        )
        views.setOnClickPendingIntent(R.id.widget_retry, pendingIntentTryAgain)

        val pendingIntentForWidgetClick = PendingIntent.getActivity(
            context,
            Constants.REQUEST_CODE_PENDING_INTENT_ON_WIDGET_CLICK,
            Intent(context, SplashScreenActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                putExtras(
                    bundleOf(DailyWordWidgetProvider.EXTRA_INTENT_TO_HOME_WORD_DATE to word?.date)
                )
            },
            safeImmutableFlag(PendingIntent.FLAG_CANCEL_CURRENT)
        )
        views.setOnClickPendingIntent(R.id.main_linearLayout_wotd, pendingIntentForWidgetClick)

        return views
    }

    private fun createPlaceHolderWidgetMedium(
        context: Context,
        resId: Int,
        message: String
    ): RemoteViews {
        val views =
            RemoteViews(context.packageName, R.layout.widget_word_layout_medium_revamp)
        views.setViewVisibility(R.id.widget_content, View.INVISIBLE)
        views.setViewVisibility(R.id.widget_placeholder, View.VISIBLE)
        views.setViewVisibility(R.id.widget_progress, View.INVISIBLE)

        views.setImageViewResource(R.id.widget_placeHolder_imageView, resId)
        views.setTextViewText(R.id.widget_placeHolder_imageView_message, message)

        return views
    }

    private fun createLoadingWidgetMedium(context: Context): RemoteViews {
        val views =
            RemoteViews(context.packageName, R.layout.widget_word_layout_medium_revamp)
        views.setViewVisibility(R.id.widget_content, View.INVISIBLE)
        views.setViewVisibility(R.id.widget_placeholder, View.INVISIBLE)
        views.setViewVisibility(R.id.widget_progress, View.VISIBLE)
        return views
    }


    private fun createWordOfTheDayWidgetSmall(context: Context, word: Word?): RemoteViews {
        val views =
            RemoteViews(context.packageName, R.layout.widget_word_layout_small_revamp)
        views.setViewVisibility(R.id.widget_placeholder, View.INVISIBLE)
        views.setViewVisibility(R.id.widget_content, View.VISIBLE)
        views.setViewVisibility(R.id.widget_progress, View.INVISIBLE)

        if (word != null) {

            views.setTextViewText(R.id.widget_txtView_word_of_the_day, word.word)
            views.setTextViewText(R.id.widget_txtView_attribute, word.attribute)
            views.setTextViewText(R.id.widget_txtView_pronounce, word.pronounce)

        } else {
            views.setViewVisibility(R.id.widget_bookmark, View.INVISIBLE)
        }

        val tryAgainIntent = Intent(context, DailyWordWidgetProvider::class.java)
        tryAgainIntent.action =
            DailyWordWidgetProvider.ACTION_TRY_AGAIN_FROM_WIDGET

        val pendingIntentTryAgain = PendingIntent.getBroadcast(
            context,
            Constants.REQUEST_CODE_PENDING_INTENT_ON_WIDGET_TRY_AGAIN_CLICK,
            tryAgainIntent, safeImmutableFlag(PendingIntent.FLAG_UPDATE_CURRENT)
        )
        views.setOnClickPendingIntent(R.id.widget_retry, pendingIntentTryAgain)


        val pendingIntentForWidgetClick = PendingIntent.getActivity(
            context,
            Constants.REQUEST_CODE_PENDING_INTENT_ON_WIDGET_CLICK,
            Intent(context, SplashScreenActivity::class.java)
                .apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    putExtras(
                        bundleOf(DailyWordWidgetProvider.EXTRA_INTENT_TO_HOME_WORD_DATE to word?.date)
                    )
                },
            safeImmutableFlag(PendingIntent.FLAG_CANCEL_CURRENT)
        )
        views.setOnClickPendingIntent(R.id.main_linearLayout_wotd, pendingIntentForWidgetClick)

        return views
    }


    private fun createPlaceHolderWidgetSmall(
        context: Context,
        resId: Int,
        message: String
    ): RemoteViews {
        val views =
            RemoteViews(context.packageName, R.layout.widget_word_layout_small_revamp)
        views.setViewVisibility(R.id.widget_content, View.INVISIBLE)
        views.setViewVisibility(R.id.widget_placeholder, View.VISIBLE)
        views.setViewVisibility(R.id.widget_progress, View.INVISIBLE)

        views.setImageViewResource(R.id.widget_placeHolder_imageView, resId)
        views.setTextViewText(R.id.widget_placeHolder_imageView_message, message)

        return views
    }

    private fun createLoadingWidgetSmall(context: Context): RemoteViews {
        val views =
            RemoteViews(context.packageName, R.layout.widget_word_layout_small_revamp)
        views.setViewVisibility(R.id.widget_content, View.INVISIBLE)
        views.setViewVisibility(R.id.widget_placeholder, View.INVISIBLE)
        views.setViewVisibility(R.id.widget_progress, View.VISIBLE)
        return views
    }

}