package com.pramod.dialyword.games.featureCard

import com.pramod.dialyword.games.featureCard.paging.InitialPage
import com.pramod.dialyword.games.featureCard.paging.LoadType
import com.pramod.dialyword.games.featureCard.paging.PageResult
import com.pramod.dialyword.games.featureCard.paging.PagingDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import javax.inject.Inject


internal class GameListingPagingDataSource @Inject constructor(
    private val gamesApiService: GamesApiService,
) : PagingDataSource<FeatureCard> {

    private val _initialPage = MutableStateFlow<InitialPage?>(null)

    override val initialPage: StateFlow<InitialPage?> = _initialPage.asStateFlow()
    private val featureCards = MutableStateFlow<List<FeatureCard>>(emptyList())

    override fun observeCache(searchQuery: String): Flow<List<FeatureCard>> {
        return featureCards
    }

    override suspend fun loadPage(
        searchQuery: String,
        page: Int,
        pageSize: Int,
        loadType: LoadType
    ): PageResult<FeatureCard> = withContext(Dispatchers.IO) {

        val response = gamesApiService.getAllFeatures(page, pageSize)

        val body = response.body()

        if (response.isSuccessful && body != null) {

            if (page == 1) {
                _initialPage.value = body
            }

            if (loadType == LoadType.REFRESH) {
                featureCards.value = body.featureCards
            } else {
                featureCards.update {
                    it.toMutableList().apply {
                        addAll(body.featureCards)
                    }
                }
            }
            PageResult.Success(
                items = body.featureCards,
                isLastPage = body.featureCards.size < pageSize
            )
        } else {
            PageResult.Error(Throwable(response.errorBody()?.string()))
        }
    }

}