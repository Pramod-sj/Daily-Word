package com.pramod.dailyword.framework.datasource.cache.mappers

import com.pramod.dailyword.business.domain.model.Word
import com.pramod.dailyword.business.domain.util.EntityMapper
import com.pramod.dailyword.framework.datasource.cache.model.BookmarkedWordCE
import com.pramod.dailyword.framework.util.CalenderUtil
import com.pramod.dailyword.framework.util.CommonUtils
import java.util.*
import javax.inject.Inject

class BookmarkedWordCEMapper @Inject constructor() : EntityMapper<BookmarkedWordCE, Word> {
    override fun fromEntity(entity: BookmarkedWordCE): Word {
        var cal: Calendar? = null
        var dayColor: List<Int>? = null
        if (entity.wordColor == -1) {
            cal = CalenderUtil.convertStringToCalender(
                entity.date,
                CalenderUtil.DATE_FORMAT
            )
            dayColor = CommonUtils.getColorBasedOnDay(cal)
        }
        return Word(
            entity.word,
            entity.pronounce,
            entity.pronounceAudio,
            entity.meanings,
            entity.didYouKnow,
            entity.attribute,
            entity.examples,
            entity.date,
            cal?.timeInMillis ?: entity.dateTimeInMillis,
            dayColor?.get(0) ?: entity.wordColor,
            dayColor?.get(1) ?: entity.wordDesaturatedColor,
            entity.otherWords,
            entity.synonyms,
            entity.antonyms,
            entity.bookmarkId,
            entity.bookmarkedAt,
            entity.bookmarkSeenAt,
            entity.seenAt != null,
            entity.seenAt,
        )
    }

    override fun toEntity(domain: Word): BookmarkedWordCE {
        return BookmarkedWordCE(
            domain.word,
            domain.pronounce,
            domain.pronounceAudio,
            domain.meanings,
            domain.didYouKnow,
            domain.attribute,
            domain.examples,
            domain.date,
            domain.dateTimeInMillis,
            domain.wordColor,
            domain.wordDesaturatedColor,
            domain.synonyms,
            domain.antonyms,
            domain.otherWords,
            domain.bookmarkedId,
            domain.word,
            domain.bookmarkedAt,
            domain.bookmarkedSeenAt,
            domain.word,
            domain.seenAtTimeInMillis,
        )
    }
}