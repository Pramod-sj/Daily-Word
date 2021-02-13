package com.pramod.dailyword.db.model

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Bookmark {
    @PrimaryKey(autoGenerate = true)
    @NonNull
    var bookmarkId: Int? = null

    var bookmarkedWord: String? = null

    var bookmarkedAt: Long? = null
}