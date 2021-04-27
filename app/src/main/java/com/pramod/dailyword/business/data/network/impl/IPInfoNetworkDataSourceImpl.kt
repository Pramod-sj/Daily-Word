package com.pramod.dailyword.business.data.network.impl

import com.pramod.dailyword.business.data.network.abstraction.IPInfoNetworkDataSource
import com.pramod.dailyword.business.domain.model.IPInfo
import com.pramod.dailyword.framework.datasource.network.abstraction.IPNetworkService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IPInfoNetworkDataSourceImpl @Inject constructor(
    private val ipInfoIPNetworkService: IPNetworkService
) :
    IPInfoNetworkDataSource {
    override suspend fun getPublicIp(): String? {
        return ipInfoIPNetworkService.getPublicIp()
    }

    override suspend fun getIPDetails(publicIp: String): IPInfo? {
        return ipInfoIPNetworkService.getIPDetails(publicIp)
    }
}