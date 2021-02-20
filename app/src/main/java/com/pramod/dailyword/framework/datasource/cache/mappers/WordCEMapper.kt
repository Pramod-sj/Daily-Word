package com.pramod.dailyword.framework.datasource.cache.mappers

import com.pramod.dailyword.business.domain.model.Word
import com.pramod.dailyword.business.domain.util.EntityMapper
import com.pramod.dailyword.framework.datasource.cache.model.WordCE
import com.pramod.dailyword.framework.util.CalenderUtil
import com.pramod.dailyword.framework.util.CommonUtils
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
            false,
            null,
            dayColor[0],
            dayColor[1],
            entity.synonyms,
            entity.antonyms,
            -1,
            -1,
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
            domain.isSeen,
            domain.seenAtTimeInMillis,
            domain.wordColor,
            domain.wordDesaturatedColor,
            domain.synonyms,
            domain.antonyms
        )
    }
}