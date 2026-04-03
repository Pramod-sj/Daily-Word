package com.pramod.dialyword.games.featureCard.paging

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * A utility class responsible for observing and managing cached data from a [PagingDataSource].
 * It maps raw data models to UI-ready models and exposes them via a [StateFlow].
 *
 * @param Data The type of data stored in the data source.
 * @param UiModel The type of data to be consumed by the UI.
 * @property scope The [CoroutineScope] used to launch the observation job.
 * @property dataSource The source providing access to the cached data.
 * @property mapper A transformation function to convert [Data] items into [UiModel] items.
 */
class CacheObserver<Data : Any, UiModel>(
    private val scope: CoroutineScope,
    private val dataSource: PagingDataSource<Data>,
    private val mapper: (Data) -> UiModel
) {
    private var cacheJob: Job? = null

    private val _items = MutableStateFlow<List<UiModel>>(emptyList())
    val items: StateFlow<List<UiModel>> = _items.asStateFlow()

    fun observe(
        key: String,
        onItemsReceived: (List<UiModel>) -> Unit
    ) {
        cacheJob?.cancel()
        cacheJob = scope.launch(Dispatchers.IO) {
            dataSource.observeCache(key)
                .map { cachedItems -> cachedItems.map(mapper) }
                .distinctUntilChanged()
                .collect { mappedItems ->
                    _items.value = mappedItems
                    onItemsReceived(mappedItems)
                }
        }
    }

    suspend fun fetchOnce(key: String): List<UiModel> {
        return dataSource.observeCache(key)
            .firstOrNull()
            .orEmpty()
            .map(mapper)
    }

    fun cancel() {
        cacheJob?.cancel()
    }
}