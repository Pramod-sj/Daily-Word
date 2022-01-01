package com.pramod.dailyword.framework.helper

import android.content.Context
import com.pramod.dailyword.business.data.network.abstraction.IPInfoNetworkDataSource
import com.pramod.dailyword.business.domain.model.IPInfo
import com.pramod.dailyword.framework.util.CommonUtils
import com.pramod.dailyword.framework.util.NetworkUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
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
                    Timber.i( "subscribeToCountry: $e")
                }

                Timber.i( "IP: ${publicIP}")

                val isVPNActive = NetworkUtils.isVPNActive(context)
                Timber.i( "subscribeToCountry: isVPNActive: $isVPNActive")

                if (publicIP != null && !isVPNActive) {
                    try {
                        val ipInfo: IPInfo? = ipInfoNetworkDataSource.getIPDetails(publicIP)
                        if (ipInfo != null && "success" == ipInfo.status) {
                            ipInfo.countryCode
                        } else {
                            //getting code from telephone manager
                            CommonUtils.getCountryCodeFromTelephoneManager(context)
                        }
                    } catch (e: Exception) {
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