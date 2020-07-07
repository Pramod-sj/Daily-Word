package com.pramod.dailyword.db.remote

import com.pramod.dailyword.db.Resource
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

fun <T> handleApiSuccess(data: T): Resource<T?> = Resource.success(data)


fun <T> handleApiFailure(data: T?, message: String?): Resource<T?> =
    Resource.error(message ?: "No message from server", data)


fun <T> handleNetworkException(data: T?, e: Exception): Resource<T?> {
    val errorMessage = when (e) {
        is UnknownHostException -> "You don't have a proper internet connection or server is not up"
        is ConnectException -> "You don't have a proper internet connection"
        is SocketTimeoutException -> "Timeout! Please check your internet connection or retry!"
        else -> e.message
    }
    return Resource.error(errorMessage, data)
}