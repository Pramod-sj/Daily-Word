package com.pramod.dailyword.framework.ui.common

import com.google.android.material.snackbar.Snackbar

sealed class Message {

    data class ToastMessage(
        val message: String,
        val duration: Int
    ) : Message()

    data class SnackBarMessage(
        val message: String,
        val duration: Int = Snackbar.LENGTH_SHORT,
        val animation: Int = Snackbar.ANIMATION_MODE_SLIDE,
        val action: Action? = null,
        val parentViewId: Int? = null,
        val anchorId: Int? = null
    ) : Message()

    data class DialogMessage(
        val title: String,
        val message: String,
        val positiveAction: Action? = null,
        val negativeAction: Action? = null,
        val neutralAction: Action? = null
    ) : Message()

}

data class Action(
    val name: String? = null,
    val callback: (() -> Unit)? = null
)