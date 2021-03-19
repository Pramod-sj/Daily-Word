package com.pramod.dailyword.business.domain.model

import java.io.Serializable

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

    val wordColor: Int = -1,

    val wordDesaturatedColor: Int = -1,

    val synonyms: List<String>?,

    val antonyms: List<String>?,

    val bookmarkedId: Int?,

    val bookmarkedAt: Long?,

    val bookmarkedSeenAt: Long?,

    val isSeen: Boolean,

    val seenAtTimeInMillis: Long?


) : Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Word

        if (word != other.word) return false
        if (pronounce != other.pronounce) return false
        if (pronounceAudio != other.pronounceAudio) return false
        if (meanings != other.meanings) return false
        if (didYouKnow != other.didYouKnow) return false
        if (attribute != other.attribute) return false
        if (examples != other.examples) return false
        if (date != other.date) return false
        if (dateTimeInMillis != other.dateTimeInMillis) return false
        if (wordColor != other.wordColor) return false
        if (wordDesaturatedColor != other.wordDesaturatedColor) return false
        if (synonyms != other.synonyms) return false
        if (antonyms != other.antonyms) return false
        if (bookmarkedId != other.bookmarkedId) return false
        if (bookmarkedAt != other.bookmarkedAt) return false
        if (bookmarkedSeenAt != other.bookmarkedSeenAt) return false
        if (isSeen != other.isSeen) return false
        if (seenAtTimeInMillis != other.seenAtTimeInMillis) return false

        return true
    }
}
