package com.pramod.dailyword.framework.datasource.cache.model

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Seen")
data class SeenCE(
    @PrimaryKey
    @NonNull
    val seenWord: String,
    val seenAt: Long
)
