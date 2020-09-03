package com.pramod.dailyword.widget

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import androidx.core.os.bundleOf
import com.pramod.dailyword.Constants
import com.pramod.dailyword.R
import com.pramod.dailyword.db.model.WordOfTheDay
import com.pramod.dailyword.ui.home.HomeActivity

class WidgetViewHelper {
    companion object {

        fun createWordOfTheDayWidget(context: Context, word: WordOfTheDay?): RemoteViews {
            val views =
                RemoteViews(context.packageName, R.layout.widget_todays_word_layout)
            views.setViewVisibility(R.id.widget_placeholder, View.INVISIBLE)
            views.setViewVisibility(R.id.widget_content, View.VISIBLE)
            views.setViewVisibility(R.id.widget_progress, View.INVISIBLE)

            if (word != null) {
                views.setTextViewText(R.id.widget_txtView_word_of_the_day, word.word)
                views.setTextViewText(R.id.widget_txtView_attribute, word.attribute)
                views.setTextViewText(R.id.widget_txtView_pronounce, word.pronounce)
                if (!word.meanings.isNullOrEmpty()) {
                    views.setTextViewText(R.id.widget_txtView_meanings, word.meanings?.get(0))
                }
                views.setTextViewText(
                    R.id.widget_txtView_how_to_user_word,
                    "How to use ${word.word}"
                )
                if (!word.examples.isNullOrEmpty()) {
                    views.setTextViewText(
                        R.id.widget_txtView_how_to_user_word_desc,
                        word.examples?.get(0)
                    )
                }

                //pronounce audio pending intent
                val playAudioIntent = Intent(context, WordWidgetProvider::class.java)
                playAudioIntent.action =
                    WordWidgetProvider.ACTION_PLAY_AUDIO_FROM_WIDGET
                playAudioIntent.putExtras(
                    bundleOf(
                        Pair(
                            WordWidgetProvider.EXTRA_AUDIO_URL,
                            word.pronounceAudio
                        )
                    )
                )

                val pendingIntentPlayAudio = PendingIntent.getBroadcast(
                    context,
                    Constants.REQUEST_CODE_PENDING_INTENT_ON_WIDGET_PRONOUNCE_CLICK,
                    playAudioIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
                views.setOnClickPendingIntent(R.id.widget_img_pronounce, pendingIntentPlayAudio)
                //end

                //bookmark status
                views.setViewVisibility(R.id.widget_bookmark, View.VISIBLE)
                views.setImageViewResource(
                    R.id.widget_bookmark,
                    if (word.isBookmarked()) R.drawable.ic_round_bookmark_24
                    else R.drawable.ic_baseline_bookmark_border_24
                )
                //end

                //bookmark pending intent
                val bookmarkIntent = Intent(context, WordWidgetProvider::class.java)
                bookmarkIntent.putExtras(
                    bundleOf(
                        Pair(
                            WordWidgetProvider.EXTRA_BOOKMARKED_WORD,
                            word.word
                        )
                    )
                )
                bookmarkIntent.action =
                    WordWidgetProvider.ACTION_BOOKMARK_FROM_WIDGET

                val pendingIntentBookmark = PendingIntent.getBroadcast(
                    context,
                    Constants.REQUEST_CODE_PENDING_INTENT_ON_WIDGET_BOOKMARK,
                    bookmarkIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
                views.setOnClickPendingIntent(R.id.widget_bookmark, pendingIntentBookmark)
                //end


            } else {
                views.setViewVisibility(R.id.widget_bookmark, View.INVISIBLE)
            }


            val tryAgainIntent = Intent(context, WordWidgetProvider::class.java)
            tryAgainIntent.action =
                WordWidgetProvider.ACTION_TRY_AGAIN_FROM_WIDGET

            val pendingIntentTryAgain = PendingIntent.getBroadcast(
                context,
                Constants.REQUEST_CODE_PENDING_INTENT_ON_WIDGET_TRY_AGAIN_CLICK,
                tryAgainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            views.setOnClickPendingIntent(R.id.widget_retry, pendingIntentTryAgain)


            val pendingIntentForWidgetClick = PendingIntent.getActivity(
                context,
                Constants.REQUEST_CODE_PENDING_INTENT_ON_WIDGET_CLICK,
                Intent(context, HomeActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            views.setOnClickPendingIntent(R.id.main_linearLayout_wotd, pendingIntentForWidgetClick)

            return views
        }

        fun createPlaceHolderWidget(context: Context, resId: Int, message: String): RemoteViews {
            val views =
                RemoteViews(context.packageName, R.layout.widget_todays_word_layout)
            views.setViewVisibility(R.id.widget_content, View.INVISIBLE)
            views.setViewVisibility(R.id.widget_placeholder, View.VISIBLE)
            views.setViewVisibility(R.id.widget_progress, View.INVISIBLE)

            views.setImageViewResource(R.id.widget_placeHolder_imageView, resId)
            views.setTextViewText(R.id.widget_placeHolder_imageView_message, message)

            return views
        }

        fun createLoadingWidget(context: Context): RemoteViews {
            val views =
                RemoteViews(context.packageName, R.layout.widget_todays_word_layout)
            views.setViewVisibility(R.id.widget_content, View.INVISIBLE)
            views.setViewVisibility(R.id.widget_placeholder, View.INVISIBLE)
            views.setViewVisibility(R.id.widget_progress, View.VISIBLE)
            return views
        }

    }
}