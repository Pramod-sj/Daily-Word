package com.pramod.dailyword.framework.datasource.network.mappers

import com.pramod.dailyword.business.domain.model.IPInfo
import com.pramod.dailyword.business.domain.util.EntityMapper
import com.pramod.dailyword.framework.datasource.network.model.IPInfoNE
import javax.inject.Inject

class IPInfoNEMapper @Inject constructor() : EntityMapper<IPInfoNE, IPInfo> {
    override fun fromEntity(entity: IPInfoNE): IPInfo {
        return IPInfo(
            entity.status,
            entity.country,
            entity.countryCode,
            entity.zip,
            entity.city,
            entity.regionName,
            entity.region,
        )
    }

    override fun toEntity(domain: IPInfo): IPInfoNE {
        return IPInfoNE(
            domain.status,
            domain.country,
            domain.countryCode,
            domain.zip,
            domain.city,
            domain.regionName,
            domain.region,
        )
    }
}