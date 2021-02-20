package com.pramod.dailyword.business.data.network.utils


class NetworkError(customMessage: String? = null) :
    Throwable(message = customMessage ?: "Network error")