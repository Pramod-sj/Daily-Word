package com.pramod.dailyword.framework.datasource.cache.mappers

import com.pramod.dailyword.business.domain.model.Seen
import com.pramod.dailyword.business.domain.util.EntityMapper
import com.pramod.dailyword.framework.datasource.cache.model.SeenCE
import javax.inject.Inject

class SeenCEMapper @Inject constructor(): EntityMapper<SeenCE, Seen> {
    override fun fromEntity(entity: SeenCE): Seen {
        return Seen(
            word = entity.seenWord,
            seenAt = entity.seenAt
        )
    }

    override fun toEntity(domain: Seen): SeenCE {
        return SeenCE(
            seenWord = domain.word,
            seenAt = domain.seenAt
        )
    }
}