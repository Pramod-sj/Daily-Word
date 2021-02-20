package com.pramod.dailyword.business.data.network.utils

import android.util.Log
import com.pramod.dailyword.BuildConfig
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

const val NETWORK_ERROR_UNKNOWN = "Network unknown error"
const val NETWORK_ERROR_TIMEOUT = "Network timeout"
const val NETWORK_ERROR = "Please connect to internet"
const val NETWORK_NULL_DATA = "No data found"

suspend fun <T> safeApiCall(
    dispatcher: CoroutineDispatcher,
    apiCall: suspend () -> T?
): ApiResult<T?> {
    return withContext(dispatcher) {
        try {
            if (BuildConfig.DEBUG) {
                //mocking purpose
                Thread.sleep(1000)
            }
            ApiResult.Success(apiCall.invoke())
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
            when (throwable) {
                is SocketTimeoutException -> {
                    ApiResult.NetworkError
                }
                is IOException -> {
                    ApiResult.NetworkError
                }
                is HttpException -> {
                    val code = throwable.code()
                    val errorResponse = convertErrorBody(throwable)
                    Log.i("ApiExtension", "safeApiCall: $errorResponse ")
                    ApiResult.GenericError(
                        code,
                        errorResponse
                    )
                }
                else -> {
                    ApiResult.GenericError(
                        null,
                        NETWORK_ERROR_UNKNOWN
                    )
                }
            }
        }
    }
}


private fun convertErrorBody(throwable: HttpException): String? {
    return try {
        throwable.toString()
    } catch (exception: Exception) {
        "Unknown"
    }
}
