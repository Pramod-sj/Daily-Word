package com.pramod.dailyword.framework.datasource.cache.mappers

import com.pramod.dailyword.business.domain.model.Word
import com.pramod.dailyword.business.domain.model.WordHistory
import com.pramod.dailyword.business.domain.util.EntityMapper
import com.pramod.dailyword.business.domain.util.EntityMapperV2
import com.pramod.dailyword.framework.datasource.cache.model.WordCE
import com.pramod.dailyword.framework.util.CalenderUtil
import com.pramod.dailyword.framework.util.CommonUtils
import com.pramod.dailyword.framework.util.WordHistoryParser
import javax.inject.Inject

class WordCEMapper @Inject constructor() : EntityMapper<WordCE, Word> {
    override fun fromEntity(entity: WordCE): Word {
        val cal = CalenderUtil.convertStringToCalender(
            entity.date,
            CalenderUtil.DATE_FORMAT
        )
        val dayColor = CommonUtils.getColorBasedOnDay(cal)
        return Word(
            entity.word,
            entity.pronounce,
            entity.pronounceAudio,
            entity.meanings,
            entity.didYouKnow,
            entity.attribute,
            entity.examples,
            entity.date,
            cal?.timeInMillis,
            dayColor[0],
            dayColor[1],
            entity.otherWords,
            entity.synonyms,
            entity.antonyms,
            null,
            null,
            null,
            false,
            null,
            null
        )
    }

    override fun toEntity(domain: Word): WordCE {
        return WordCE(
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
            etymology = null,
            firstKnownUse = null,
            timeTraveler = null
        )
    }
}


class WordCEMapperV2 @Inject constructor() : EntityMapperV2<WordCE, Word> {

    override suspend fun fromEntity(entity: WordCE): Word {
        val cal = CalenderUtil.convertStringToCalender(
            entity.date,
            CalenderUtil.DATE_FORMAT
        )
        val dayColor = CommonUtils.getColorBasedOnDay(cal)

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
            dateTimeInMillis = cal?.timeInMillis,
            wordColor = dayColor[0],
            wordDesaturatedColor = dayColor[1],
            otherWords = entity.otherWords,
            synonyms = entity.synonyms,
            antonyms = entity.antonyms,
            bookmarkedId = null,
            bookmarkedAt = null,
            bookmarkedSeenAt = null,
            isSeen = false,
            seenAtTimeInMillis = null,
            wordHistory = wordHistory
        )
    }

    override suspend fun toEntity(domain: Word): WordCE {
        return WordCE(
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
            etymology = domain.wordHistory?.originStoryRawString,
            firstKnownUse = domain.wordHistory?.bornInRawString,
            timeTraveler = domain.wordHistory?.throughTheAgesRawString
        )
    }
}
