package com.library.audioplayer

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class AudioPlayer(
    private val context: Context
) {

    private val _audioDownloadingStatus = MutableLiveData<APEvent<Boolean>>()
    val audioDownloadingStatus: LiveData<APEvent<Boolean>>
        get() = _audioDownloadingStatus

    private val _audioDownloadError = MutableLiveData<APEvent<Throwable>?>()
    val audioDownloadError: LiveData<APEvent<Throwable>?>
        get() = _audioDownloadError

    private val _audioPlaying = MutableLiveData<APEvent<Boolean>>()
    val audioPlaying: LiveData<APEvent<Boolean>>
        get() = _audioPlaying

    private val _audioPlayingError = MutableLiveData<APEvent<Throwable>?>()
    val audioPlayingError: LiveData<APEvent<Throwable>?>
        get() = _audioPlayingError

    private var callback: CacheAudioPlayerCallback? = null

    fun setCallback(callback: CacheAudioPlayerCallback) {
        this.callback = callback
    }

    private var mediaPlayer: MediaPlayer? = null

    private val fileDownloader = FileDownloader()

    private val cachedAudioDatabase = CachedAudioDatabase.getInstance(context)


    private fun createFile(
        context: Context,
        folder: String?,
        appendName: String?,
        extension: String
    ): File? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = timeStamp + if (appendName == null) "" else "_$appendName"
        val storageDir = context.getExternalFilesDir(folder)
        if (storageDir != null) {
            return File(
                storageDir.absoluteFile,
                "$fileName.$extension"
            )
        }
        return null
    }


    fun play(audioUrl: String) {
        CoroutineScope(Dispatchers.Main).launch {
            val cachedAudioCE = cachedAudioDatabase.getAudioDao().get(audioUrl)
            if (cachedAudioCE != null) {
                //file path is present i.e. file is already downloaded
                startPlaying(cachedAudioCE.cachedAudioPath)
            } else {
                val audioDestFile = createFile(
                    context,
                    "Audios",
                    null,
                    "mp3"
                )
                //download the file and save in local
                audioDestFile?.let {
                    fileDownloader.download(audioUrl, it)
                        .collectLatest { status ->
                            when (status) {
                                is FileDownloadStatus.Downloading -> {
                                    callback?.onAudioDownloadProgress(audioUrl, status.progress)
                                }
                                is FileDownloadStatus.DownloadStarted -> {
                                    callback?.onAudioDownloadStarted(audioUrl)
                                    _audioDownloadingStatus.value = APEvent.init(true)
                                }
                                is FileDownloadStatus.DownloadComplete -> {
                                    callback?.onAudioDownloadCompleted(audioUrl)
                                    _audioDownloadingStatus.value = APEvent.init(false)
                                }
                                is FileDownloadStatus.DownloadedFileSaved -> {
                                    cachedAudioDatabase.getAudioDao().insert(
                                        AudioCE(
                                            id = audioUrl,
                                            cachedAudioPath = it.absolutePath
                                        )
                                    )
                                    callback?.onAudioSaved(audioUrl, it.absolutePath)
                                    //once file is stored in db. start playing the audio from local file
                                    startPlaying(it.absolutePath)
                                }
                                is FileDownloadStatus.Error -> {
                                    callback?.onAudioDownloadFailed(audioUrl, status.throwable)
                                    _audioDownloadingStatus.value = APEvent.init(false)
                                    _audioDownloadError.value = APEvent.init(status.throwable)
                                }
                            }
                        }
                }
            }
        }
    }

    private var isAudioPlaying = false
    private suspend fun startPlaying(uri: String) {

        withContext(Dispatchers.IO) {
            if (!isAudioPlaying) {
                _audioPlaying.postValue(APEvent.init(true))
                isAudioPlaying = true
                try {
                    mediaPlayer = MediaPlayer().apply {
                        setAudioAttributes(
                            AudioAttributes.Builder()
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build()
                        )
                    }
                    mediaPlayer?.setDataSource(uri)
                    mediaPlayer?.prepareAsync()
                    mediaPlayer?.setOnCompletionListener {
                        isAudioPlaying = false
                        _audioPlaying.postValue(APEvent.init(false))
                        mediaPlayer?.reset()
                        mediaPlayer?.release()
                        callback?.onAudioComplete(uri)
                    }
                    mediaPlayer?.setOnPreparedListener {
                        mediaPlayer?.start()
                        callback?.onAudioPlaying(uri)
                    }
                } catch (e: Exception) {
                    isAudioPlaying = false
                    _audioPlaying.postValue(APEvent.init(false))
                    _audioPlayingError.postValue(APEvent.init(e))
                    e.printStackTrace()
                }
            }
        }

    }

    fun stop() {
        mediaPlayer?.stop()
    }


    private suspend fun <T> safeCacheCall(body: suspend () -> T): T {
        return withContext(Dispatchers.IO) {
            body.invoke()
        }
    }


    abstract class CacheAudioPlayerCallback {
        open fun onAudioDownloadStarted(remoteUrl: String) {}

        open fun onAudioDownloadProgress(remoteUrl: String, progress: Int) {}

        open fun onAudioDownloadCompleted(remoteUrl: String) {}

        open fun onAudioDownloadFailed(remoteUrl: String, throwable: Throwable) {}

        open fun onAudioSaved(remoteUrl: String, localPath: String) {}

        open fun onAudioPlaying(remoteUrl: String) {}

        open fun onAudioComplete(remoteUrl: String) {}
    }


    companion object {
        private val TAG = AudioPlayer::class.java.simpleName
    }

}

