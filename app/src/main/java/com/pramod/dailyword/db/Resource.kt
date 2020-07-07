package com.pramod.dailyword.db

import com.pramod.dailyword.db.model.WordOfTheDay

class Resource<T> private constructor(
    val status: Status,
    val data: T?,
    val message: String?
) {

    enum class Status {
        SUCCESS, ERROR, LOADING
    }

    enum class ErrorType {
        UNKNOWN, NO_INTERNET
    }

    companion object {
        @JvmStatic
        fun <T> success(data: T): Resource<T> {
            return Resource(
                Status.SUCCESS,
                data,
                null
            )
        }

        @JvmStatic
        fun <T> error(msg: String?, data: T?): Resource<T?> {
            return Resource(
                Status.ERROR,
                data,
                msg
            )
        }

        @JvmStatic
        fun <T> loading(data: T?): Resource<T?> {
            return Resource(
                Status.LOADING,
                data,
                null
            )
        }
    }

}