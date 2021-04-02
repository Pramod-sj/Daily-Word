package com.pramod.dailyword.framework.ui.changelogs

import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.BulletSpan
import androidx.core.text.toSpannable
import com.google.gson.annotations.SerializedName

class Changes {

    var version: String? = null

    @SerializedName("release_name")
    var releaseName: String? = null

    var date: String? = null

    var changes: List<String> = ArrayList()

    fun getHtmlFormattedChanges(): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append("<li>")
        for (change in changes) {
            stringBuilder.append("<ul>$change</ul>")
        }
        stringBuilder.append("</li>")
        return stringBuilder.toString()
    }

    fun getFormattedChanges(): Spannable {
        val spannableStringBuilder = SpannableStringBuilder()
        changes.forEachIndexed { index, s ->
            spannableStringBuilder.append(
                s, BulletSpan(15), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            if (index < changes.size - 1) {
                spannableStringBuilder.appendLine()
            }
        }
        return spannableStringBuilder.toSpannable()
    }
}