package com.pramod.dailyword.framework.datasource.network.abstraction

import com.pramod.dailyword.business.domain.model.IPInfo

interface IPNetworkService {

    suspend fun getPublicIp(): String?

    suspend fun getIPDetails(publicIp: String): IPInfo?
}