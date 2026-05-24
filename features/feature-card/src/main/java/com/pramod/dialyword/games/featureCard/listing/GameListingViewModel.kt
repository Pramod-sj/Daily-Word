package com.pramod.dialyword.games.featureCard.listing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pramod.dailyword.games.results.GameResultRepository
import com.pramod.dialyword.games.featureCard.FeatureCard
import com.pramod.dialyword.games.featureCard.FeatureCardResponse
import com.pramod.dialyword.games.featureCard.listing.model.GameCard
import com.pramod.dialyword.games.featureCard.listing.model.GameFilter
import com.pramod.dialyword.games.featureCard.listing.model.GameState
import com.pramod.dialyword.games.featureCard.paging.LoadState
import com.pramod.dialyword.games.featureCard.paging.PagingConfig
import com.pramod.dialyword.games.featureCard.paging.PagingDataSource
import com.pramod.dialyword.games.featureCard.paging.PagingItem
import com.pramod.dialyword.games.featureCard.paging.PagingManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
internal class GameListingViewModel @Inject constructor(
    gameListingPagingDataSource: PagingDataSource<FeatureCard>,
    gameResultRepository: GameResultRepository,
) : ViewModel() {

    companion object {
        private const val TAG = "GameListingViewModel"
    }

    private val _filter = MutableStateFlow(GameFilter.ALL)

    val filter: StateFlow<GameFilter> = _filter.asStateFlow()

    private val pagingManager = PagingManager(
        scope = viewModelScope,
        config = PagingConfig(pageSize = 20),
        dataSource = gameListingPagingDataSource,
        mapper = { featureCard ->
            GameCard(
                id = featureCard.gameId.orEmpty(),
                title = featureCard.content?.title.orEmpty(),
                subtitle = featureCard.content?.subtitle.orEmpty(),
                datePublished = featureCard.content?.overlineText.orEmpty(),
                gameState = GameState.NotStarted, //always init with not started
                gameRoute = featureCard.action?.routeUri.orEmpty(),
                status = featureCard.status
            )
        }
    )

    fun Long.formatAsTime(): String {
        val totalSeconds = this / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        val tenths = (this % 1000) / 100
        return when {
            minutes > 0 -> "${minutes}m ${seconds.toString().padStart(2, '0')}s"
            totalSeconds > 0 -> "${seconds}.${tenths}s"
            else -> "0.${tenths}s"
        }
    }

    val games: StateFlow<List<PagingItem<GameCard>>> = combine(
        pagingManager.pagingItems,
        _filter,
        gameResultRepository.getAllResults(),
    ) { pagingItems, filter, allResults ->

        // Build a lookup map once — O(1) per item instead of O(n) find()
        val resultMap = allResults.associateBy { it.puzzleId }

        pagingItems.mapNotNull { pageItem ->
            when (pageItem) {
                is PagingItem.Error,
                is PagingItem.Loading -> {
                    // Keep loading/error indicators only for ALL filter
                    if (filter == GameFilter.ALL) pageItem else null
                }

                is PagingItem.Item<GameCard> -> {
                    val gameState = resultMap[pageItem.data.id]
                        ?.let {
                            GameState.Completed(
                                it.completionTimeMillis.formatAsTime(),
                                it.score
                            )
                        }
                        ?: GameState.NotStarted

                    val matchesFilter = when (filter) {
                        GameFilter.ALL -> true
                        GameFilter.COMPLETED -> gameState is GameState.Completed
                        GameFilter.NOT_PLAYED -> gameState is GameState.NotStarted
                    }

                    if (matchesFilter) pageItem.copy(data = pageItem.data.copy(gameState = gameState))
                    else null
                }
            }
        }
    }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    val refreshState: StateFlow<LoadState> = pagingManager.refreshState

    val appendState: StateFlow<LoadState> = pagingManager.appendState

    val initialPage = gameListingPagingDataSource.initialPage.map {
        it as? FeatureCardResponse
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val completedCount = gameResultRepository.getAllResults()
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)


    init {
        pagingManager.refresh()
    }

    fun setFilter(filter: GameFilter) {
        _filter.value = filter
    }

    fun retry() {
        pagingManager.retry()
    }

    fun refresh() {
        pagingManager.refresh(true)
    }

    fun loadNext() {
        pagingManager.loadNext()
    }

    override fun onCleared() {
        super.onCleared()
    }

}