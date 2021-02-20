package com.pramod.dailyword.framework.widget

import android.content.Context
import android.content.Intent
import android.util.Log
import com.pramod.dailyword.framework.helper.PronounceHelper

class WordWidgetProvider : BaseWidgetProvider() {
    private val TAG = WordWidgetProvider::class.simpleName

    companion object {
        const val ACTION_PLAY_AUDIO_FROM_WIDGET =
            "com.pramod.dailyword.ui.widget.WordWidgetProvider.ACTION_PLAY_AUDIO_FROM_WIDGET"

        const val EXTRA_AUDIO_URL = "audio_url"
        const val EXTRA_BOOKMARKED_WORD = "bookmarked_word"
    }

    private var isWordPronounced = true
    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        Log.i(TAG, "onReceive: ${intent?.action}")
        intent?.let {
            when (it.action) {
                ACTION_PLAY_AUDIO_FROM_WIDGET -> {
                    if (isWordPronounced) {
                        Log.i(TAG, "onReceive: Playing")
                        it.getStringExtra(EXTRA_AUDIO_URL)?.let { audioUrl ->
                            isWordPronounced = false
                            PronounceHelper.playAudio(audioUrl) {
                                isWordPronounced = true
                            }
                        }
                    }
                }
                else -> {
                }
            }
        }
    }

}