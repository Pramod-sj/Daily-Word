package com.pramod.dailyword.db.model

import androidx.room.Ignore
import com.google.gson.annotations.SerializedName

class ServerTime {
    @SerializedName("unixtime")
    val timeInSec: Long? = null

    @Ignore
    var timeInMillis: Long? = null
        get() {
            return timeInSec!! * 1000
        }

}
