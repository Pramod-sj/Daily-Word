package com.pramod.dialyword.games.featureCard.paging

interface PageItemIdProvider {

    fun getPagingItemId(): String

}

sealed class PagingItem<out Data : PageItemIdProvider>(val id: String) {

    data class Item<out Data : PageItemIdProvider>(
        val data: Data,
    ) : PagingItem<Data>(data.getPagingItemId())

    data class Loading(
        val loadType: LoadType
    ) : PagingItem<Nothing>("loading")

    data class Error(
        val throwable: Throwable,
        val loadType: LoadType,
        val retry: () -> Unit
    ) : PagingItem<Nothing>("error")

}
