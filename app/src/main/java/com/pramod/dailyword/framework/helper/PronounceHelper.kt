package com.pramod.dailyword.framework.helper

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log

class PronounceHelper {
    companion object {
        @JvmStatic
        fun playAudio(url: String, completionCallback: (() -> Unit)? = null) {

            Log.d("AUDIO URL", url)
            try {
                val mediaPlayer = MediaPlayer()
                mediaPlayer.setDataSource("/storage/emulated/0/Android/data/com.pramod.dailyword/files/Audios/20210221_183945.mp3")
                mediaPlayer.setAudioAttributes(
                    AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                mediaPlayer.setOnCompletionListener {
                    mediaPlayer.release()
                    completionCallback?.invoke()
                }
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