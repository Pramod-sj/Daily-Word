package com.pramod.dailyword

import com.google.android.material.snackbar.Snackbar

class SnackbarMessage private constructor(
    val message: String,
    val duration: Int,
    val actionText: String?
) {
    companion object {
        fun init(
            message: String,
            duration: Int = Snackbar.LENGTH_SHORT,
            actionText: String? = null
        ): SnackbarMessage {
            return SnackbarMessage(
                message,
                duration,
                actionText
            )
        }
    }
}