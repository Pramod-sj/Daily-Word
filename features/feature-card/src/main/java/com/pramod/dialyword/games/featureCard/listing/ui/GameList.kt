package com.pramod.dialyword.games.featureCard.listing.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pramod.dialyword.games.featureCard.listing.model.GameCard
import com.pramod.dialyword.games.featureCard.paging.PagingItem

@Composable
internal fun GameList(
    games: List<PagingItem<GameCard>>,
    onGameClick: (gameCard: GameCard) -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        itemsIndexed(
            items = games,
            key = { _, pagingItem -> pagingItem.id },
        ) { index, pagingItem ->

            when (pagingItem) {
                is PagingItem.Error -> {

                }

                is PagingItem.Item<GameCard> -> {

                    GameCard(
                        game = pagingItem.data,
                        onClick = { onGameClick(pagingItem.data) },
                    )

                    if (index < games.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 62.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                        )
                    }
                }

                is PagingItem.Loading -> {

                }
            }

        }
    }
}