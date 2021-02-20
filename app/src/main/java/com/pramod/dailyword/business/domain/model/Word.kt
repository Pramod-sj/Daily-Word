package com.pramod.dailyword.business.domain.model

data class Word(
    val word: String,

    val pronounce: String?,

    val pronounceAudio: String?,

    val meanings: List<String>?,

    val didYouKnow: String?,

    val attribute: String?,

    val examples: List<String>?,

    val date: String?,

    val dateTimeInMillis: Long?,

    val isSeen: Boolean,

    val seenAtTimeInMillis: Long?,

    val wordColor: Int = -1,

    val wordDesaturatedColor: Int = -1,

    val synonyms: List<String>?,

    val antonyms: List<String>?,

    val bookmarkedId: Int?,

    val bookmarkedAt: Long?
)
