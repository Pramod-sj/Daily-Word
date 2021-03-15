package com.pramod.dailyword.framework.datasource.cache.model

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Bookmark")
data class BookmarkCE(
    @PrimaryKey(autoGenerate = true)
    @NonNull
    val bookmarkId: Int?,

    val bookmarkedWord: String?,

    val bookmarkedAt: Long?,

    val bookmarkSeenAt: Long?
)