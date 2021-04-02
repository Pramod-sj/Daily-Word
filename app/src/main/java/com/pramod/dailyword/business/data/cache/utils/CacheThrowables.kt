package com.pramod.dailyword.business.data.cache.utils


const val CACHE_TIMEOUT = 3000L
const val CACHE_TIMEOUT_ERROR_MESSAGE = "Cache timeout"
const val CACHE_UNKNOWN_ERROR_MESSAGE = "Something went wrong while fetching from cache"

class CacheTimeoutError(customMessage: String? = null) :
    Throwable(message = customMessage ?: CACHE_TIMEOUT_ERROR_MESSAGE)

class CacheUnknownError(customMessage: String? = null) :
    Throwable(message = customMessage ?: CACHE_UNKNOWN_ERROR_MESSAGE)

