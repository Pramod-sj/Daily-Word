package com.pramod.dailyword.util

import android.content.ClipboardManager
import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.google.android.material.snackbar.Snackbar

class BindingAdapter {
    companion object {

        @JvmStatic
        @BindingAdapter(value = ["copyToClipBoardText", "rootLayout"], requireAll = true)
        fun copyToClipBoardOnLongClick(
            textView: TextView,
            copyToClipBoardText: CharSequence?,
            rootLayout: View
        ) {
            textView.setOnLongClickListener {
                CommonUtils.copyToClipboard(
                    textView.context,
                    copyToClipBoardText!!,
                    ClipboardManager.OnPrimaryClipChangedListener {
                        Snackbar.make(rootLayout, "Copied to clipboard", Snackbar.LENGTH_SHORT)
                            .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
                            .show()
                    })
                return@setOnLongClickListener true
            }
        }
    }
}