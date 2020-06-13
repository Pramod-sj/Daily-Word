package com.pramod.dailyword.db.remote

class EndPoints {
    companion object {
        const val WOTD_BASE_URL = "http://8f54c163445d.ngrok.io/ci/index.php/"
        const val WORLD_TIME_BASE_URL = "https://www.worldtimeapi.org/api/"
        const val GET_WORD_OF_THE_DAY = "Wordoftheday/getTodaysWord"
        const val GET_WORDS = "Wordoftheday/getWords"
    }
}