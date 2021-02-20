package com.pramod.dailyword.business.data.cache.utils


class CacheResult<T>(
    val data: T? = null,
    val error: Throwable? = null
) {
    companion object {

        fun <T> success(data: T?): CacheResult<T?> {
            return CacheResult(data = data)
        }

        fun <T> error(error: Throwable): CacheResult<T?> {
            return CacheResult(error = error)
        }

    }


}
