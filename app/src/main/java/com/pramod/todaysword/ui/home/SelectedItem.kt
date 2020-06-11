package com.pramod.todaysword.ui.home

import android.view.View

class SelectedItem<T> private constructor(
    val position: Int = -1,
    val viewId: Int = -1,
    val data: T
) {

    companion object {
        @JvmStatic
        fun <T> initWithPosition(position: Int, data: T): SelectedItem<T> {
            return SelectedItem(position = position, data = data)
        }

        @JvmStatic
        fun <T> initWithViewId(viewId: Int, data: T): SelectedItem<T> {
            return SelectedItem(viewId = viewId, data = data)
        }
    }
}