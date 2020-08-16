package com.pramod.dailyword.db.model

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
class Bookmark {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "bookmarkedWord")
    var bookmarkedWord: String? = null

    var bookmarkedAt: Long? = null
}