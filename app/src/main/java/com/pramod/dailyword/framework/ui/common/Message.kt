package com.pramod.dailyword.framework.ui.common

import android.widget.Toast
import androidx.annotation.Keep
import com.google.android.material.snackbar.Snackbar

@Keep
sealed class Message {

    var isShown: Boolean = false

    @Keep
    data class ToastMessage(
        val message: String,
        val duration: Int = Toast.LENGTH_SHORT
    ) : Message()

    @Keep
    data class SnackBarMessage(
        val message: String,
        val duration: Int = Snackbar.LENGTH_SHORT,
        val animation: Int = Snackbar.ANIMATION_MODE_SLIDE,
        val action: Action? = null,
        val parentViewId: Int? = null,
        val anchorId: Int? = null
    ) : Message()

    @Keep
    data class DialogMessage(
        val title: String,
        val message: String,
        val positiveAction: Action? = null,
        val negativeAction: Action? = null,
        val neutralAction: Action? = null
    ) : Message()

}

@Keep
data class Action(
    val name: String? = null,
    val callback: (() -> Unit)? = null
)