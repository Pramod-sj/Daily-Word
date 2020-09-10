package com.pramod.dailyword.helper

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log

class PronounceHelper {
    companion object {
        @JvmStatic
        fun playAudio(url: String) {

            Log.d("AUDIO URL", url)
            try {
                val mediaPlayer = MediaPlayer()
                mediaPlayer.setDataSource(url)
                mediaPlayer.setAudioAttributes(
                    AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()
                )
                mediaPlayer.setOnPreparedListener {
                    it.start()
                }
                mediaPlayer.prepareAsync()

            } catch (e: Exception) {
                Log.d("AUDIO ERROR", e.toString())
            }
        }
    }
}