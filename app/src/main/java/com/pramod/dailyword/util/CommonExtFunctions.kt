package com.pramod.dailyword.util;

import android.text.Selection
import android.text.Spannable
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView

fun TextView.showLinks(vararg clickableTextLinks: Pair<String, View.OnClickListener>) {
    val spannableString = SpannableString(this.text)
    for (i in clickableTextLinks) {
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                Selection.setSelection((widget as TextView).text as Spannable, 0)
                widget.invalidate()
                i.second.onClick(widget)
            }
        }

        val startIndex = this.text.toString().indexOf(i.first)
        spannableString.setSpan(
            clickableSpan,
            startIndex,
            startIndex + i.first.length,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
    this.movementMethod = LinkMovementMethod.getInstance()
    this.setText(spannableString, TextView.BufferType.SPANNABLE)
}
