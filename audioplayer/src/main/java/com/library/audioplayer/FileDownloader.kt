package com.library.audioplayer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.net.HttpURLConnection
import java.util.concurrent.TimeUnit

class FileDownloader {
    companion object {
        private const val BUFFER_LENGTH_BYTES = 1024 * 8
        private const val HTTP_TIMEOUT = 30
    }

    private var okHttpClient = OkHttpClient().newBuilder()
        .connectTimeout(HTTP_TIMEOUT.toLong(), TimeUnit.SECONDS)
        .readTimeout(HTTP_TIMEOUT.toLong(), TimeUnit.SECONDS)
        .build()


    init {
        val okHttpBuilder = okHttpClient.newBuilder()
            .connectTimeout(HTTP_TIMEOUT.toLong(), TimeUnit.SECONDS)
            .readTimeout(HTTP_TIMEOUT.toLong(), TimeUnit.SECONDS)
        this.okHttpClient = okHttpBuilder.build()
    }

    @ExperimentalCoroutinesApi
    fun download(url: String, dest: File): Flow<FileDownloadStatus> {
        return flow {
            val request = Request.Builder().url(url).build()
            emit(FileDownloadStatus.DownloadStarted)
            val response = okHttpClient.newCall(request).execute()
            val body = response.body
            emit(FileDownloadStatus.DownloadComplete)
            val responseCode = response.code
            if (responseCode >= HttpURLConnection.HTTP_OK &&
                responseCode < HttpURLConnection.HTTP_MULT_CHOICE &&
                body != null
            ) {
                val length = body.contentLength()
                body.byteStream().apply {
                    dest.outputStream().use { fileOut ->
                        var bytesCopied = 0
                        val buffer = ByteArray(BUFFER_LENGTH_BYTES)
                        var bytes = read(buffer)
                        while (bytes >= 0) {
                            fileOut.write(buffer, 0, bytes)
                            bytesCopied += bytes
                            bytes = read(buffer)
                            emit(FileDownloadStatus.Downloading(((bytesCopied * 100) / length).toInt()))
                        }
                    }
                }
                emit(FileDownloadStatus.DownloadedFileSaved(dest.absolutePath))
            } else {
                val exception = "Error occurred when do http get $url"
                emit(FileDownloadStatus.Error(Throwable(exception)))
                throw IllegalArgumentException("Error occurred when do http get $url")
            }
        }.flowOn(Dispatchers.IO)
    }
}

