package com.pramod.dailyword.framework.widget

import android.app.Activity
import android.content.Intent


fun Activity.refreshWidget() {
    Intent(this, DailyWordWidgetProvider::class.java).also { intent ->
        intent.action = DailyWordWidgetProvider.ACTION_SILENT_REFRESH_WIDGET
        sendBroadcast(intent)
    }
}