package com.pramod.games.crossword.network

import android.util.Log
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

const val ERROR_MESSAGE_SOCKET_TIMEOUT_EXCEPTION =
    "Timeout! Please check your internet connection or retry!"
const val ERROR_MESSAGE_UNKNOWN_HOST_EXCEPTION =
    "You don't have a proper internet connection or server is not up"
const val ERROR_MESSAGE_CONNECT_EXCEPTION = "You don't have a proper internet connection"
const val ERROR_MESSAGE_IO_EXCEPTION = "Some I/O error occurred!"

const val NETWORK_ERROR_UNKNOWN = "Unknown network error!"
const val NETWORK_ERROR_TIMEOUT = "Network timeout"
const val NETWORK_ERROR = "Please connect to internet"
const val NETWORK_NULL_DATA = "No data found"

suspend fun <T : Any> safeApiCall(
    dispatcher: CoroutineDispatcher,
    apiCall: suspend () -> T?,
): ApiResult<T?> =
    withContext(dispatcher) {
        try {
            ApiResult.Success(apiCall.invoke())
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
            when (throwable) {
                is SocketTimeoutException -> {
                    ApiResult.NetworkError("Timeout! Please check your internet connection or retry!")
                }

                is UnknownHostException -> {
                    ApiResult.NetworkError("You don't have a proper internet connection or server is not up")
                }

                is ConnectException -> {
                    ApiResult.NetworkError("You don't have a proper internet connection")
                }

                is IOException -> {
                    ApiResult.NetworkError("Some I/O error occurred!")
                }

                is HttpException -> {
                    val code = throwable.code()
                    val errorResponse = convertErrorBody(throwable)
                    Log.i("ApiExtension", "safeApiCall: $errorResponse ")
                    ApiResult.GenericError(
                        code,
                        errorResponse,
                    )
                }

                else -> {
                    ApiResult.GenericError(
                        null,
                        NETWORK_ERROR_UNKNOWN,
                    )
                }
            }
        }
    }

private fun convertErrorBody(throwable: HttpException): String? =
    try {
        throwable.toString()
    } catch (exception: Exception) {
        "Unknown"
    }
