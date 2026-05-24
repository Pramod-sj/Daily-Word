package com.pramod.dailyword.framework

import com.pramod.dailyword.framework.firebase.FBRemoteConfig
import com.pramod.dailyword.network.EndpointProvider
import javax.inject.Inject

class EndpointProviderImpl @Inject constructor(
    private val remoteConfig: FBRemoteConfig
) : EndpointProvider {

    override fun getBaseUrl(): String {
        return remoteConfig.baseUrl()
    }

}