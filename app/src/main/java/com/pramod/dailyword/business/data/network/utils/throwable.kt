package com.pramod.dailyword.business.data.network.utils

import okio.IOException
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

fun handleApiException(e: Exception): Throwable {
    return when (e) {
        is SocketTimeoutException -> {
            Throwable("Timeout! Please check your internet connection or retry!")
        }
        is UnknownHostException -> {
            Throwable("You don't have a proper internet connection or server is not up")
        }
        is ConnectException -> {
            Throwable("You don't have a proper internet connection")
        }
        is IOException -> {
            Throwable("Some I/O error occurred!")
        }
        is HttpException -> {
            Throwable(e.message())
        }
        else -> {
            Throwable(
                NETWORK_ERROR_UNKNOWN
            )
        }
    }

}