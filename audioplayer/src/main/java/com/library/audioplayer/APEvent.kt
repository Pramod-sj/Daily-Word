package com.library.audioplayer

class APEvent<T> private constructor(private val content: T) {
    private var isHandled = false

    fun getContentIfNotHandled(): T? {
        if (!isHandled) {
            isHandled = true
            return content
        }
        return null
    }

    fun peekContent(): T {
        return content
    }

    companion object {
        fun <T> init(content: T): APEvent<T> {
            return APEvent(content)
        }
    }

}