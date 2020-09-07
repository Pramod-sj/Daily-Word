package com.pramod.dailyword.db.model

import com.google.gson.annotations.SerializedName
import java.lang.StringBuilder

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

    fun getFormattedChanges(bullet: String): String {
        val stringBuilder = StringBuilder()
        for (change in changes) {
            stringBuilder.append("$bullet $change\n")
        }
        return stringBuilder.toString()
    }
}