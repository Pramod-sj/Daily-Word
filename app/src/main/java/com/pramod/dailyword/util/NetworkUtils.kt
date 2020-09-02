package com.pramod.dailyword.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import com.pramod.dailyword.db.remote.WOTDApiService
import com.pramod.dailyword.db.remote.EndPoints
import com.pramod.dailyword.db.remote.TimeApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class NetworkUtils {
    companion object {
        fun getWOTDApiService(): WOTDApiService {
            val client = Retrofit.Builder()
                .baseUrl(EndPoints.WOTD_API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            return client.create(WOTDApiService::class.java)
        }

        fun getServerTimeApiService(): TimeApiService {
            val client = Retrofit.Builder()
                .baseUrl(EndPoints.WORLD_TIME_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return client.create(TimeApiService::class.java)
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
    }
}