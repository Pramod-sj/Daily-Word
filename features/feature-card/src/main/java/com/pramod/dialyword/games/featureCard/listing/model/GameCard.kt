package com.pramod.dialyword.games.featureCard.listing.model

import androidx.compose.runtime.Stable
import com.pramod.dialyword.games.featureCard.paging.PageItemIdProvider

@Stable
data class GameCard(
    val id: String,
    val title: String,
    val subtitle: String,
    val status: String? = null,
    val datePublished: String,  // display-ready e.g. "Mar 29"
    val gameState: GameState, // null = not played, "04:21" = done
    val gameRoute: String
) : PageItemIdProvider {

    override fun getPagingItemId(): String = id

}

@Stable
sealed interface GameState {

    data object NotStarted : GameState

    data class Completed(val completionTime: String, val score: Int) : GameState

}