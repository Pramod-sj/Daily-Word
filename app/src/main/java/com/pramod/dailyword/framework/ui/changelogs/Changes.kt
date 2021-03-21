package com.pramod.dailyword.framework.ui.changelogs

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

    fun getFormattedChanges(bullet: String): String {
        val stringBuilder = StringBuilder()
        changes.forEachIndexed { index, s ->
            stringBuilder.append("$bullet $s")
            if (index < changes.size - 1) {
                stringBuilder.append("\n")
            }
        }
        return stringBuilder.toString()
    }
}