package com.pramod.dailyword.db.remote

import com.pramod.dailyword.BuildConfig
import com.pramod.dailyword.db.model.IPInfo
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Url

interface IPService {
    @GET(BuildConfig.GET_PUBLIC_IP)
    suspend fun getPublicIp(): String?

    @GET(BuildConfig.GET_IP_DETAILS)
    suspend fun getIPDetails(@Path("public_ip") publicIp: String): IPInfo?
}