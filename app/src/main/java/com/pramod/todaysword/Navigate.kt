package com.pramod.todaysword

class Navigate<D>(data: D?) {
    companion object {
        fun <D> init(data: D?): Navigate<D> {
            return Navigate(data)
        }
    }
}