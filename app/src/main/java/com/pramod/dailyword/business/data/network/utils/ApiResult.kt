package com.pramod.dailyword.business.data.network.utils

import androidx.annotation.Keep

@Keep
sealed class ApiResult<out T> {

    @Keep
    data class Success<T>(val data: T) : ApiResult<T>()

    @Keep
    data class GenericError(val code: Int?, val message: String?) : ApiResult<Nothing>()

    @Keep
    data class NetworkError(val message: String?) : ApiResult<Nothing>()

}
