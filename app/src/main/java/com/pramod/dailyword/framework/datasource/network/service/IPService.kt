package com.pramod.dailyword.framework.datasource.network.service

import com.pramod.dailyword.framework.datasource.network.EndPoints
import com.pramod.dailyword.framework.datasource.network.model.IPInfoNE
import retrofit2.http.GET
import retrofit2.http.Path

interface IPService {
    @GET(EndPoints.GET_PUBLIC_IP)
    suspend fun getPublicIp(): String?

    @GET(EndPoints.GET_IP_DETAILS)
    suspend fun getIPDetails(@Path("public_ip") publicIp: String): IPInfoNE?
}