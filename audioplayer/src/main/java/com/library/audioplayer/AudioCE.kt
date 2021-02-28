package com.library.audioplayer

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Audio")
data class AudioCE(
    @NonNull
    @PrimaryKey
    val id: String,
    val cachedAudioPath: String
)