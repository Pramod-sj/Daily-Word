package com.pramod.dailyword

class Navigate<D>(data: D?) {
    companion object {
        fun <D> init(data: D?): Navigate<D> {
            return Navigate(data)
        }
    }
}