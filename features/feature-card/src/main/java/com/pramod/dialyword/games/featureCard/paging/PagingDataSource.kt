package com.pramod.dialyword.games.featureCard.paging

import com.pramod.dialyword.games.featureCard.PagingMeta
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Interface defining a data source for paginated data retrieval.
 *
 * This interface bridges local caching and remote network fetching to provide
 * a consistent paging mechanism.
 *
 * @param Data The type of data items handled by this data source.
a list of items of type [Data].
.
 * @param page The index of the page to be retrieved.
 * @param pageSize The maximum number of items to be returned in the page.
 * @param loadType The type of load operation being performed (e.g., initial, prepend, or append).
 * @return A [PageResult] containing the list of items and pagination metadata.
 */
interface PagingDataSource<Data : Any> {

    val initialPage: StateFlow<InitialPage?>

    /** Reactive cache */

    fun observeCache(searchQuery: String): Flow<List<Data>>

    /** Network fetch */
    suspend fun loadPage(
        searchQuery: String,
        page: Int,
        pageSize: Int,
        loadType: LoadType
    ): PageResult<Data>

}

interface InitialPage

sealed interface PageResult<out Data : Any> {

    data class Success<out Data : Any>(
        val items: List<Data>,
        val isLastPage: Boolean
    ) : PageResult<Data>


    data class Error(
        val throwable: Throwable
    ) : PageResult<Nothing>


}
