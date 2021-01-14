package com.pramod.dailyword.db.remote

class EndPoints {
    companion object {

        const val WOTD_BASE_URL = "http://23.111.167.173/~todayswo/"
        const val WOTD_API_BASE_URL = "${WOTD_BASE_URL}ci/index.php/"
        const val WORLD_TIME_BASE_URL = "https://www.worldtimeapi.org/api/"
        const val GET_WORD_OF_THE_DAY = "Wordoftheday/getTodaysWord"
        const val GET_WORDS = "Wordoftheday/getWords"
        const val GET_RANDOM_WORD = "Wordoftheday/getRandomWord"

        const val PRIVACY_POLICY = "${WOTD_BASE_URL}etc/privacy_policy.html"
        const val TERM_AND_CONDITION = "${WOTD_BASE_URL}etc/term_and_condition.html"

        const val GET_PUBLIC_IP = "https://www.trackip.net/ip"
        const val GET_IP_DETAILS = "http://www.ip-api.com/json/{public_ip}"
    }
}