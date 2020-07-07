package com.pramod.dailyword.db.remote

sealed class NetworkResponse<out T : Any?> {

    /**
     * Api Success when code is 200
     */
    data class Success<T : Any?>(val body: T?) : NetworkResponse<T>()

    /**
     * Api Failure when code is other than 200
     */
    data class Failure(val error: Exception) : NetworkResponse<Nothing>()

    /**
     * Error like json parsing, no internet connection, etc other than api
     */
    data class UnknownError(val error: Exception) : NetworkResponse<Nothing>()
}