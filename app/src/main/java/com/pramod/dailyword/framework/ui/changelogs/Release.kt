package com.pramod.dailyword.framework.ui.changelogs

import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.BulletSpan
import androidx.annotation.Keep
import androidx.core.text.toSpannable
import com.google.gson.annotations.SerializedName

@Keep
data class Release(
    @SerializedName("version_code")
    val versionCode: Long,
    @SerializedName("version_name")
    val versionName: String,
    @SerializedName("release_name")
    val releaseName: String,
    @SerializedName("release_date")
    val date: String,
    @SerializedName("changes")
    val changes: List<String>,
    @SerializedName("is_force_update")
    val isForceUpdate: Boolean
) {

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