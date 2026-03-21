package com.pramod.dailyword.framework

import com.pramod.dailyword.framework.firebase.FBRemoteConfig
import com.pramod.dailyword.games.results.GameEndpointProvider
import javax.inject.Inject

class GameEndpointProviderImpl @Inject constructor(
    private val remoteConfig: FBRemoteConfig
) : GameEndpointProvider {
    override fun getBaseUrl(): String {
        return remoteConfig.baseUrl()
    }
}