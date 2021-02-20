package com.pramod.dailyword.framework.datasource.cache.model

data class BookmarkedWordCE(
    var word: String,

    var pronounce: String?,

    var pronounceAudio: String?,

    var meanings: List<String>?,

    var didYouKnow: String?,

    var attribute: String?,

    var examples: List<String>?,

    var date: String?,

    var dateTimeInMillis: Long?,

    var isSeen: Boolean,

    var seenAtTimeInMillis: Long?,

    var wordColor: Int = -1,

    var wordDesaturatedColor: Int = -1,

    var synonyms: List<String>?,

    var antonyms: List<String>?,

    var bookmarkId: Int?,

    var bookmarkedWord: String?,

    var bookmarkedAt: Long?
)
