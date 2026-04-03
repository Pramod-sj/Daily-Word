package com.pramod.dialyword.games.featureCard.paging

/**
 * Manages the state of pagination, including tracking the current page index,
 * determining if the last page has been reached, and synchronizing state between
 * cached data and network updates.
 *
 * @property config The configuration settings for pagination, defining page sizes and initial indices.
 */
class PageTracker(private val config: PagingConfig) {

    var currentPage: Int = config.initialPage
        private set

    var isLastPage: Boolean = false
        private set

    private var pageDerivedFromCache = false
    var hasNetworkLoadedOnce = false
        private set

    fun reset() {
        currentPage = config.initialPage
        isLastPage = false
        pageDerivedFromCache = false
        hasNetworkLoadedOnce = false
    }

    fun onNetworkSuccess(isLast: Boolean) {
        hasNetworkLoadedOnce = true
        isLastPage = isLast
        currentPage++
    }

    fun derivePageFromCache(itemCount: Int): Boolean {
        if (pageDerivedFromCache || hasNetworkLoadedOnce || itemCount == 0) {
            return false
        }
        val pagesLoaded = (itemCount + config.pageSize - 1) / config.pageSize
        currentPage = pagesLoaded + config.initialPage
        pageDerivedFromCache = true
        return true
    }

    fun canLoadNext(): Boolean = !isLastPage
}