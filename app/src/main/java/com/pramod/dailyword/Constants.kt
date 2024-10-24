package com.pramod.dailyword

import java.util.TimeZone

class Constants {
    companion object {
        const val REQUEST_CODE_PENDING_INTENT_ON_WIDGET_CLICK = 200
        const val REQUEST_CODE_PENDING_INTENT_ON_WIDGET_PRONOUNCE_CLICK = 201
        const val REQUEST_CODE_PENDING_INTENT_ON_WIDGET_TRY_AGAIN_CLICK = 202
        const val REQUEST_CODE_PENDING_INTENT_ALARM = 203
        const val REQUEST_CODE_PENDING_INTENT_ON_WIDGET_BOOKMARK = 204
        const val REQUEST_CODE_PENDING_INTENT_ON_SHOW_RANDOM = 206

        const val JOB_ID_FETCH_DATA_FOR_WIDGET = 205
        const val JOB_ID_FETCH_RANDOM_WORD_FOR_WIDGET = 207

        const val COLOR_ALPHA = 30
        const val COLOR_ALPHA_DONATE_ITEM = 10

        //in-app update request codes
        const val APP_UPDATE_FLEX_REQUEST_CODE = 1
        const val APP_UPDATE_IMMEDIATE_REQUEST_CODE = 2

        val DEFAULT_TIME_ZONE = TimeZone.getTimeZone("Asia/Kolkata")

    }
}