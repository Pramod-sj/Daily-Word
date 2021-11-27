package com.pramod.dailyword.framework.datasource.network.mappers

import com.pramod.dailyword.business.domain.model.Word
import com.pramod.dailyword.business.domain.util.EntityMapper
import com.pramod.dailyword.framework.datasource.network.model.WordNE
import com.pramod.dailyword.framework.util.CalenderUtil
import com.pramod.dailyword.framework.util.CommonUtils
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