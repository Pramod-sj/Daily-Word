package com.pramod.dialyword.games.featureCard.paging

class PagingItemBuilder<UiModel : PageItemIdProvider> {

    fun build(
        items: List<UiModel>,
        appendState: LoadState,
        onRetry: () -> Unit
    ): List<PagingItem<UiModel>> {
        val result = mutableListOf<PagingItem<UiModel>>()

        items.mapTo(result) { PagingItem.Item(it) }

        when (appendState) {
            is LoadState.Loading -> {
                result.add(PagingItem.Loading(LoadType.APPEND))
            }

            is LoadState.Error -> {
                result.add(
                    PagingItem.Error(
                        throwable = appendState.throwable,
                        loadType = LoadType.APPEND,
                        retry = onRetry
                    )
                )
            }

            LoadState.Idle -> Unit
        }

        return result
    }
}