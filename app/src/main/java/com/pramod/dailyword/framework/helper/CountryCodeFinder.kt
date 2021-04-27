package com.pramod.dailyword.framework.helper

import android.content.Context
import android.util.Log
import com.pramod.dailyword.business.data.network.abstraction.IPInfoNetworkDataSource
import com.pramod.dailyword.business.domain.model.IPInfo
import com.pramod.dailyword.framework.firebase.FBTopicSubscriber
import com.pramod.dailyword.framework.util.CommonUtils
import com.pramod.dailyword.framework.util.NetworkUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CountryCodeFinder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val ipInfoNetworkDataSource: IPInfoNetworkDataSource
) {
    suspend fun getCountryCode(): String? {
        return withContext(Dispatchers.IO) {
            //two letters
            return@withContext if (NetworkUtils.isNetworkActive(context)) {
                var publicIP: String? = null
                try {
                    publicIP = ipInfoNetworkDataSource.getPublicIp()
                } catch (e: Exception) {
                    Log.i(TAG, "subscribeToCountry: $e")
                }

                Log.i(TAG, "IP: ${publicIP}")

                val isVPNActive = NetworkUtils.isVPNActive(context)
                Log.i(TAG, "subscribeToCountry: isVPNActive: $isVPNActive")

                if (publicIP != null && !isVPNActive) {
                    val ipInfo: IPInfo? = ipInfoNetworkDataSource.getIPDetails(publicIP)
                    if (ipInfo != null && "success" == ipInfo.status) {
                        ipInfo.countryCode
                    } else {
                        //getting code from telephone manager
                        CommonUtils.getCountryCodeFromTelephoneManager(context)
                    }
                } else {
                    //getting code from telephone manager
                    CommonUtils.getCountryCodeFromTelephoneManager(context)

                }
            } else {
                //getting code from telephone manager
                CommonUtils.getCountryCodeFromTelephoneManager(context)
            }
        }
    }

    companion object {
        val TAG = CountryCodeFinder::class.java.simpleName
    }

}