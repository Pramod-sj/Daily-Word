package com.pramod.todaysword.ui.home

import android.view.View

class SelectedItem<T> private constructor(val position: Int, val data: T) {

    companion object {
        @JvmStatic
        fun <T> init(position: Int, data: T): SelectedItem<T> {
            return SelectedItem(position, data)
        }
    }
}