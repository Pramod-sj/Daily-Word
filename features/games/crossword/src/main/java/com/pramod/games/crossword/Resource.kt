package com.pramod.games.crossword

internal class Resource<T>(
    val data: T? = null,
    val status: Status,
    val error: Throwable? = null,
) {
    companion object {
        fun <T> success(data: T): Resource<T?> = Resource(data = data, status = Status.SUCCESS)

        fun <T> error(
            throwable: Throwable,
            data: T? = null,
        ): Resource<T?> = Resource(error = throwable, status = Status.ERROR, data = data)
    }
}

internal enum class Status {
    SUCCESS,
    ERROR,
}
