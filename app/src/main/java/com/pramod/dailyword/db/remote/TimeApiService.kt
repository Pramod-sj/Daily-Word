package com.pramod.dailyword.db.remote

import com.pramod.dailyword.db.model.ServerTime
import retrofit2.Call
import retrofit2.http.GET

interface TimeApiService {
    @GET("ip")
    fun getTime(): Call<ServerTime>
}