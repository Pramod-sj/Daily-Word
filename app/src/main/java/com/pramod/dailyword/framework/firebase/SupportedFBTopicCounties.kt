package com.pramod.dailyword.framework.firebase

import com.pramod.dailyword.framework.util.LookupEnum
import java.util.*

enum class SupportedFBTopicCounties {
    IN, //India
    US, //United States
    GB, //United Kingdom
    OTHERS
}

/**
 * method to check whether country code provided is supported by DailyWord
 */
fun isCountryCodeSupported(code: String): Boolean {
    return SupportedFBTopicCounties.values()
        .contains(
            LookupEnum.lookUp(
                SupportedFBTopicCounties::class.java,
                code.uppercase(Locale.US)
            )
        )
}