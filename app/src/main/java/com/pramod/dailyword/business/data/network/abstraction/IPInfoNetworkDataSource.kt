package com.pramod.dailyword.business.data.network.abstraction

import com.pramod.dailyword.business.domain.model.IPInfo

interface IPInfoNetworkDataSource {
    suspend fun getPublicIp(): String?

    suspend fun getIPDetails(publicIp: String): IPInfo?
}