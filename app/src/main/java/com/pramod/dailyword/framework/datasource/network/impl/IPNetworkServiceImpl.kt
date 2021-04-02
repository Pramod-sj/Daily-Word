package com.pramod.dailyword.framework.datasource.network.impl

import com.pramod.dailyword.business.domain.model.IPInfo
import com.pramod.dailyword.framework.datasource.network.abstraction.IPNetworkService
import com.pramod.dailyword.framework.datasource.network.mappers.IPInfoNEMapper
import com.pramod.dailyword.framework.datasource.network.service.IPService

class IPNetworkServiceImpl(
    private val ipService: IPService,
    private val ipInfoNEMapper: IPInfoNEMapper
) :
    IPNetworkService {
    override suspend fun getPublicIp(): String? {
        return ipService.getPublicIp()
    }

    override suspend fun getIPDetails(publicIp: String): IPInfo? {
        return ipService.getIPDetails(publicIp)?.let { ipInfoNEMapper.fromEntity(it) }
    }
}