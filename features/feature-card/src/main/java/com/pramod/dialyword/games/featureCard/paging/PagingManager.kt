package com.pramod.dialyword.games.featureCard.paging

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield

class PagingManager<Data : Any, UiModel : PageItemIdProvider>(
    private val scope: CoroutineScope,
    private val config: PagingConfig = PagingConfig(),
    private val dataSource: PagingDataSource<Data>,
    mapper: (Data) -> UiModel
) {
    private val loadStateHolder = LoadStateHolder()
    private val pageTracker = PageTracker(config)
    private val itemBuilder = PagingItemBuilder<UiModel>()
    private val cacheObserver = CacheObserver(scope, dataSource, mapper)

    private var currentKey: String? = null
    private var loadJob: Job? = null

    // Expose states
    val refreshState: StateFlow<LoadState> = loadStateHolder.refreshState

    val appendState: StateFlow<LoadState> = loadStateHolder.appendState

    private val _pagingItems = MutableStateFlow<List<PagingItem<UiModel>>>(emptyList())
    val pagingItems: StateFlow<List<PagingItem<UiModel>>> = _pagingItems.asStateFlow()

    private val retryAction = { retry() }

    init {
        scope.launch {
            combine(
                cacheObserver.items,
                loadStateHolder.appendState
            ) { items, appendState ->
                withContext(Dispatchers.Default) {
                    itemBuilder.build(items, appendState, retryAction)
                }
            }.distinctUntilChanged().collect {
                _pagingItems.value = it
            }
        }
    }

    fun submitKey(key: String, forceNetwork: Boolean = true) {
        if (key == currentKey) return
        currentKey = key
        reset()
        observeCache(key)
        if (forceNetwork) {
            load(LoadType.REFRESH)
        }
    }

    fun loadNext() {
        if (!pageTracker.canLoadNext()) return
        if (loadStateHolder.isAppendingOrError()) return
        load(LoadType.APPEND)
    }

    fun retry() {
        when {
            loadStateHolder.hasAppendError() -> {
                loadStateHolder.resetAppendState()
                load(LoadType.APPEND)
            }

            loadStateHolder.hasRefreshError() -> {
                loadStateHolder.setRefreshState(LoadState.Idle)
                load(LoadType.REFRESH)
            }
        }
    }

    fun refresh(forceNetwork: Boolean = true) {
        if (refreshState.value is LoadState.Loading) return
        reset()
        observeCache(currentKey.orEmpty())
        if (forceNetwork) {
            load(LoadType.REFRESH)
        }
    }

    // ---------------------- Internals ----------------------

    private fun reset() {
        pageTracker.reset()
        loadStateHolder.resetAppendState()
        loadJob?.cancel()
        cacheObserver.cancel()
    }

    private fun observeCache(key: String) {
        cacheObserver.observe(key) { items ->
            if (loadStateHolder.refreshState.value !is LoadState.Loading) {
                pageTracker.derivePageFromCache(items.size)
            }
        }
    }

    private fun load(loadType: LoadType) {
        loadJob?.cancel()
        loadJob = scope.launch {
            loadStateHolder.setState(loadType, LoadState.Loading)

            val result = dataSource.loadPage(
                searchQuery = currentKey.orEmpty(),
                page = pageTracker.currentPage,
                pageSize = config.pageSize,
                loadType = loadType
            )

            when (result) {
                is PageResult.Error -> {
                    loadStateHolder.setState(loadType, LoadState.Error(result.throwable))
                }

                is PageResult.Success<*> -> {
                    pageTracker.onNetworkSuccess(result.isLastPage)

                    // Sync items before state change
                    cacheObserver.fetchOnce(currentKey.orEmpty())

                    // Update via cache observer's internal state
                    yield()

                    loadStateHolder.setState(loadType, LoadState.Idle)
                }
            }
        }
    }
}