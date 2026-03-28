package com.pramod.dailyword.network

class Resource<T>(
    val data: T? = null,
    val status: Status,
    val error: Throwable? = null,
) {
    companion object {
        fun <T> success(data: T): Resource<T?> = Resource(data = data, status = Status.SUCCESS)

        fun <T> loading(data: T? = null): Resource<T?> =
            Resource(data = data, status = Status.LOADING)

        fun <T> error(
            throwable: Throwable,
            data: T? = null,
        ): Resource<T?> = Resource(error = throwable, status = Status.ERROR, data = data)
    }
}

enum class Status {
    LOADING,
    SUCCESS,
    ERROR,
}
