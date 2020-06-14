package com.pramod.dailyword.util

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

    }
}