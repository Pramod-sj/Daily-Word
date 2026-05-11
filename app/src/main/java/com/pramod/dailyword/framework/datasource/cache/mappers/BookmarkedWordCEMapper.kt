package com.pramod.dailyword.framework.datasource.cache.mappers

import com.pramod.dailyword.business.domain.model.Word
import com.pramod.dailyword.business.domain.model.WordHistory
import com.pramod.dailyword.business.domain.util.EntityMapper
import com.pramod.dailyword.business.domain.util.EntityMapperV2
import com.pramod.dailyword.framework.datasource.cache.model.BookmarkedWordCE
import com.pramod.dailyword.framework.util.CalenderUtil
import com.pramod.dailyword.framework.util.CommonUtils
import com.pramod.dailyword.framework.util.WordHistoryParser
import java.util.Calendar
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


class BookmarkedWordCEMapperV2 @Inject constructor() : EntityMapperV2<BookmarkedWordCE, Word> {

    override suspend fun fromEntity(entity: BookmarkedWordCE): Word {
        var cal: Calendar? = null
        var dayColor: List<Int>? = null
        if (entity.wordColor == -1) {
            cal = CalenderUtil.convertStringToCalender(
                entity.date,
                CalenderUtil.DATE_FORMAT
            )
            dayColor = CommonUtils.getColorBasedOnDay(cal)
        }
        val wordHistory =
            if (!entity.etymology.isNullOrEmpty() ||
                !entity.firstKnownUse.isNullOrEmpty() ||
                !entity.timeTraveler.isNullOrEmpty()
            ) {

                WordHistory(
                    originStory = WordHistoryParser.parse(entity.etymology),
                    bornIn = WordHistoryParser.parse(entity.firstKnownUse),
                    throughTheAges = entity.timeTraveler,

                    originStoryRawString = entity.etymology,
                    bornInRawString = entity.firstKnownUse,
                    throughTheAgesRawString = entity.timeTraveler
                )
            } else null
        return Word(
            word = entity.word,
            pronounce = entity.pronounce,
            pronounceAudio = entity.pronounceAudio,
            meanings = entity.meanings,
            didYouKnow = entity.didYouKnow,
            attribute = entity.attribute,
            examples = entity.examples,
            date = entity.date,
            dateTimeInMillis = cal?.timeInMillis ?: entity.dateTimeInMillis,
            wordColor = dayColor?.get(0) ?: entity.wordColor,
            wordDesaturatedColor = dayColor?.get(1) ?: entity.wordDesaturatedColor,
            otherWords = entity.otherWords,
            synonyms = entity.synonyms,
            antonyms = entity.antonyms,
            bookmarkedId = entity.bookmarkId,
            bookmarkedAt = entity.bookmarkedAt,
            bookmarkedSeenAt = entity.bookmarkSeenAt,
            isSeen = entity.seenAt != null,
            seenAtTimeInMillis = entity.seenAt,
            wordHistory = wordHistory
        )
    }

    override suspend fun toEntity(domain: Word): BookmarkedWordCE {
        return BookmarkedWordCE(
            word = domain.word,
            pronounce = domain.pronounce,
            pronounceAudio = domain.pronounceAudio,
            meanings = domain.meanings,
            didYouKnow = domain.didYouKnow,
            attribute = domain.attribute,
            examples = domain.examples,
            date = domain.date,
            dateTimeInMillis = domain.dateTimeInMillis,
            wordColor = domain.wordColor,
            wordDesaturatedColor = domain.wordDesaturatedColor,
            synonyms = domain.synonyms,
            antonyms = domain.antonyms,
            otherWords = domain.otherWords,
            bookmarkId = domain.bookmarkedId,
            bookmarkedWord = domain.word,
            bookmarkedAt = domain.bookmarkedAt,
            bookmarkSeenAt = domain.bookmarkedSeenAt,
            seenWord = domain.word,
            seenAt = domain.seenAtTimeInMillis,
            etymology = domain.wordHistory?.originStoryRawString,
            firstKnownUse = domain.wordHistory?.bornInRawString,
            timeTraveler = domain.wordHistory?.throughTheAgesRawString
        )
    }
}


