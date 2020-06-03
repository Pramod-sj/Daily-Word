package com.pramod.todaysword.util

import com.pramod.todaysword.db.remote.WOTDApiService
import com.pramod.todaysword.db.remote.EndPoints
import com.pramod.todaysword.db.remote.TimeApiService
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.Executors

class NetworkUtils {
    companion object {
        fun getWOTDApiService(): WOTDApiService {
            val client = Retrofit.Builder()
                .baseUrl(EndPoints.WOTD_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            return client.create(WOTDApiService::class.java)
        }

        fun getServerTimeApiService(): TimeApiService {
            val client = Retrofit.Builder()
                .baseUrl(EndPoints.WORLD_TIME_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

            return client.create(TimeApiService::class.java)
        }

    }
}