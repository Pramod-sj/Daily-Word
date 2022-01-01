package com.pramod.dailyword.framework.widget

import android.content.Context
import android.content.Intent
import com.library.audioplayer.AudioPlayer
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class WordWidgetProvider : BaseWidgetProvider() {
    private val TAG = WordWidgetProvider::class.simpleName


    @Inject
    lateinit var audioPlayer: AudioPlayer

    companion object {
        const val ACTION_PLAY_AUDIO_FROM_WIDGET =
            "com.pramod.dailyword.ui.widget.WordWidgetProvider.ACTION_PLAY_AUDIO_FROM_WIDGET"

        const val EXTRA_AUDIO_URL = "audio_url"
        const val EXTRA_BOOKMARKED_WORD = "bookmarked_word"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        Timber.i("onReceive: ${intent?.action}")
        intent?.let {
            when (it.action) {
                ACTION_PLAY_AUDIO_FROM_WIDGET -> {
                    Timber.i("onReceive: Playing")
                    it.getStringExtra(EXTRA_AUDIO_URL)?.let { audioUrl ->
                        audioPlayer.play(audioUrl)
                    }
                }
                else -> {
                }
            }
        }
    }

}