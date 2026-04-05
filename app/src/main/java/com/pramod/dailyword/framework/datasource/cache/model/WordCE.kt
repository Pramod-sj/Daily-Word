package com.pramod.dailyword.framework.datasource.cache.model

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Word")
data class WordCE(
    @PrimaryKey
    @NonNull
    val word: String,

    val pronounce: String?,

    val pronounceAudio: String?,

    val meanings: List<String>?,

    val didYouKnow: String?,

    val attribute: String?,

    val examples: List<String>?,

    val date: String?,

    val dateTimeInMillis: Long?,

    val wordColor: Int = -1,

    val wordDesaturatedColor: Int = -1,

    val synonyms: List<String>?,

    val antonyms: List<String>?,

    val otherWords: List<String>?,

    val etymology: String? = null,

    val firstKnownUse: String? = null,

    val timeTraveler: String? = null

)
