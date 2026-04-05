package com.pramod.dailyword.business.domain.model

import java.io.Serializable

data class WordHistoryEntry(
    val partOfSpeech: String? = null, // e.g., "Noun", "Verb"
    val text: String
) : Serializable

data class WordHistory(
    val originStory: List<WordHistoryEntry>? = null,
    val bornIn: List<WordHistoryEntry>? = null,
    val throughTheAges: String? = null,

    val originStoryRawString: String? = null,
    val bornInRawString: String? = null,
    val throughTheAgesRawString: String? = null,
) : Serializable

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

    val otherWords: List<String>?,

    val synonyms: List<String>?,

    val antonyms: List<String>?,

    val bookmarkedId: Int?,

    val bookmarkedAt: Long?,

    val bookmarkedSeenAt: Long?,

    val isSeen: Boolean,

    val seenAtTimeInMillis: Long?,

    val wordHistory: WordHistory? = null

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
        if (otherWords != other.otherWords) return false
        if (wordHistory != other.wordHistory) return false
        return true
    }

    override fun hashCode(): Int {
        var result = word.hashCode()
        result = 31 * result + (pronounce?.hashCode() ?: 0)
        result = 31 * result + (pronounceAudio?.hashCode() ?: 0)
        result = 31 * result + (meanings?.hashCode() ?: 0)
        result = 31 * result + (didYouKnow?.hashCode() ?: 0)
        result = 31 * result + (attribute?.hashCode() ?: 0)
        result = 31 * result + (examples?.hashCode() ?: 0)
        result = 31 * result + (date?.hashCode() ?: 0)
        result = 31 * result + (dateTimeInMillis?.hashCode() ?: 0)
        result = 31 * result + wordColor
        result = 31 * result + wordDesaturatedColor
        result = 31 * result + (otherWords?.hashCode() ?: 0)
        result = 31 * result + (synonyms?.hashCode() ?: 0)
        result = 31 * result + (antonyms?.hashCode() ?: 0)
        result = 31 * result + (bookmarkedId ?: 0)
        result = 31 * result + (bookmarkedAt?.hashCode() ?: 0)
        result = 31 * result + (bookmarkedSeenAt?.hashCode() ?: 0)
        result = 31 * result + isSeen.hashCode()
        result = 31 * result + (seenAtTimeInMillis?.hashCode() ?: 0)
        result = 31 * result + (wordHistory?.hashCode() ?: 0)
        return result
    }
}
