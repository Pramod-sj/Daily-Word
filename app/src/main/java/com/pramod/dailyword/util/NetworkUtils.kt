package com.pramod.dailyword.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import com.pramod.dailyword.BuildConfig
import com.pramod.dailyword.db.remote.IPService
import com.pramod.dailyword.db.remote.TimeApiService
import com.pramod.dailyword.db.remote.WOTDApiService
import com.pramod.dailyword.firebase.FBRemoteConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

class NetworkUtils {
    companion object {
        val TAG = NetworkUtils::class.java.simpleName

        fun getWOTDApiService(): WOTDApiService {
            val client = Retrofit.Builder()
                .baseUrl(FBRemoteConfig.getInstance().baseUrl())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            return client.create(WOTDApiService::class.java)
        }

        fun getServerTimeApiService(): TimeApiService {
            val client = Retrofit.Builder()
                .baseUrl(BuildConfig.WORLD_TIME_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return client.create(TimeApiService::class.java)
        }

        fun getIPService(): IPService {
            val client = Retrofit.Builder()
                .baseUrl(BuildConfig.API_BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            return client.create(IPService::class.java)
        }

        fun isNetworkActive(context: Context): Boolean {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val capabilities =
                    connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                if (capabilities != null) {
                    when {
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                            return true
                        }
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                            return true
                        }
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                            return true
                        }
                    }
                } else {
                    return false;
                }
            } else {
                val activeNetworkInfo = connectivityManager.activeNetworkInfo
                return activeNetworkInfo != null && activeNetworkInfo.isConnected
            }
            return false
        }


        fun isVPNActive(context: Context): Boolean {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val capabilities =
                    connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                if (capabilities != null) {
                    return when {
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> true
                        else -> false
                    }
                }
                return false
            } else {
                val networks = connectivityManager.allNetworkInfo
                for (n in networks) {
                    if (n.isConnectedOrConnecting && n.type == ConnectivityManager.TYPE_VPN) {
                        return true
                    }
                }
                return false
            }

        }
    }
}