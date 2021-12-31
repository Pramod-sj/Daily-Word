package com.pramod.dailyword.framework.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.paging.ExperimentalPagingApi
import com.pramod.dailyword.framework.helper.scheduleWeeklyAlarmAt12PM

@ExperimentalPagingApi
class BootReceiver : BroadcastReceiver() {


    companion object {
        val TAG = BootReceiver::class.java.simpleName
    }

    override fun onReceive(context: Context?, intent: Intent?) {

        Log.i(TAG, "onReceive: ")

        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {

            //scheduling weekly alarm after boot
            context?.scheduleWeeklyAlarmAt12PM()

        }
    }
}