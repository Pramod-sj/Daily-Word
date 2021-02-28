package com.library.audioplayer

sealed class AudioStatus {
    data class Downloading(val progress: Int) : AudioStatus()
    object DownloadSuccess : AudioStatus()
    data class DownloadingError(val throwable: Throwable) : AudioStatus()
    object Started : AudioStatus()
    object Completed : AudioStatus()
    object InvalidStatus : AudioStatus()
}


sealed class FileDownloadStatus {
    object DownloadStarted : FileDownloadStatus()
    data class Downloading(val progress: Int) : FileDownloadStatus()
    object DownloadComplete : FileDownloadStatus()
    data class DownloadedFileSaved(val cachedFilePath: String) : FileDownloadStatus()
    data class Error(val throwable: Throwable) : FileDownloadStatus()
}
