package com.pramod.dialyword.router.routes

/**
 * Note: This is the app location of the route.
 * Ideally, each feature should hold this route in their module.
 * Since DailyWord was not initially a multi-module project, but in the future,
 * we will be moving the route to respective modules.
 */
object CoreRoute {

    const val APP_SCHEME = "app"
    const val APP_HOST = "dailyword"

    const val APP_BASE = "$APP_SCHEME://$APP_HOST"

    const val HOME_ROUTE = "$APP_BASE${CorePath.HOME}"
    const val WORD_DETAIL_ROUTE = "$APP_BASE${CorePath.WORD_DETAIL}"
    const val WORD_LIST_ROUTE = "$APP_BASE${CorePath.WORD_LIST}"
    const val RECAP_WORDS_ROUTE = "$APP_BASE${CorePath.RECAP_WORDS}"
    const val FAVORITE_WORDS_ROUTE = "$APP_BASE${CorePath.FAVORITE_WORDS}"

    // For routes without a specific Path constant, just append the string directly
    const val SETTINGS = "$APP_BASE/settings"
    const val NOTIFICATION_CONSENT = "$APP_BASE/notification_consent"
    const val TROUBLESHOOT = "$APP_BASE/troubleshoot"
    const val ABOUT_APP = "$APP_BASE/about_app"
    const val SPLASH_SCREEN = "$APP_BASE/splash_screen"

    // Helper for parameterized routes
    fun wordDetailPath(wordId: String): String {
        return "$WORD_DETAIL_ROUTE?wordDate=$wordId"
    }
}

object CorePath {
    const val HOME = "/home"
    const val WORD_DETAIL = "/word_detail"
    const val WORD_LIST = "/word_list"
    const val RECAP_WORDS = "/recap_words"
    const val FAVORITE_WORDS = "/favorite_words"
}