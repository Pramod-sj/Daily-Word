package com.pramod.dailyword.framework.datasource.network.mappers

import com.pramod.dailyword.business.domain.model.Word
import com.pramod.dailyword.business.domain.model.WordHistory
import com.pramod.dailyword.business.domain.util.EntityMapper
import com.pramod.dailyword.business.domain.util.EntityMapperV2
import com.pramod.dailyword.framework.datasource.network.model.WordNE
import com.pramod.dailyword.framework.util.CalenderUtil
import com.pramod.dailyword.framework.util.CommonUtils
import com.pramod.dailyword.framework.util.WordHistoryParser
import javax.inject.Inject

class WordNEMapper @Inject constructor() : EntityMapper<WordNE, Word> {
    override fun fromEntity(entity: WordNE): Word {
        val cal = CalenderUtil.convertStringToCalender(
            entity.date,
            CalenderUtil.DATE_FORMAT
        )
        val dayColor = CommonUtils.getColorBasedOnDay(cal)
        return Word(
            entity.word!!,
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
        )
    }

    override fun toEntity(domain: Word): WordNE {
        TODO("Not yet implemented")
    }
}


class WordNEMapperV2 @Inject constructor() : EntityMapperV2<WordNE, Word> {

    override suspend fun fromEntity(entity: WordNE): Word {
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
            word = entity.word!!,
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

    override suspend fun toEntity(domain: Word): WordNE {
        TODO("Not yet implemented")
    }
}