package com.pramod.dialyword.games.featureCard.listing.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pramod.dialyword.games.featureCard.CrosswordTheme
import com.pramod.dialyword.games.featureCard.listing.model.GameCard
import com.pramod.dialyword.games.featureCard.listing.model.GameState

// ── All states in one panel ───────────────────────────────────────────────────
@Preview(name = "All States", showBackground = true, widthDp = 380)
@Preview(
    name = "All States — Dark",
    showBackground = true,
    widthDp = 380,
    uiMode = UI_MODE_NIGHT_YES
)
@Composable
private fun GameCardAllStatesPreview() {
    val games = listOf(
        GameCard(
            "1", "Daily #142", "Mar 29",
            gameState = GameState.NotStarted,
            datePublished = "23 Mar",
            gameRoute = ""
        ),
        GameCard(
            id = "2",
            title = "Daily #141",
            subtitle = "Mar 28",
            gameState = GameState.Completed("04:21", 95),
            datePublished = "23 Mar",
            gameRoute = "",
            status = "New"
        ),
        GameCard(
            id = "3",
            title = "Weekend Special",
            subtitle = "Mar 27",
            gameState = GameState.Completed("11:03", 62),
            datePublished = "23 Mar",
            gameRoute = ""
        ),
        GameCard(
            id = "4",
            title = "Themed: Bollywood",
            subtitle = "Mar 22",
            gameState = GameState.Completed("09:47", 38),
            datePublished = "23 Mar",
            gameRoute = ""
        ),
    )
    CrosswordTheme {
        Surface {
            Column {
                games.forEachIndexed { i, game ->
                    GameCard(game = game, onClick = {})
                    if (i < games.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 62.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                        )
                    }
                }
            }
        }
    }
}


@Composable
internal fun GameCard(
    game: GameCard,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        elevation = CardDefaults.elevatedCardElevation(0.dp),
        shape = RectangleShape,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {

            // ── Title + meta ─────────────────────────────────────────
            Column(modifier = Modifier.weight(1f)) {
                Row {

                    Text(
                        text = game.title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Spacer(modifier = Modifier.size(4.dp))

                    game.status?.let {

                        val color = MaterialTheme.colorScheme.primary

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = color.copy(alpha = 0.10f),
                            border = BorderStroke(1.dp, color.copy(alpha = 0.30f)),
                        ) {
                            Text(
                                text = game.status,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    letterSpacing = 0.3.sp,
                                ),
                                color = color,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = game.datePublished,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // ── Right: fixed-width status block ──────────────────────
            // Width is fixed so the chevron never shifts between states
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.wrapContentWidth(),
            ) {
                // Status — fixed width box so chevron is always at same X
                Box(
                    modifier = Modifier.width(56.dp),
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    when (val state = game.gameState) {
                        is GameState.Completed -> {
                            Column(
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(3.dp),
                            ) {
                                ScoreChip(score = state.score)
                                Text(
                                    text = state.completionTime,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Medium,
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        GameState.NotStarted -> {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .border(
                                        width = 1.5.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant,
                                        shape = CircleShape,
                                    )
                            )
                        }
                    }
                }

                // Chevron — always at same position
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Composable
private fun ScoreChip(score: Int) {
    val color = scoreColor(score)
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.30f)),
    ) {
        Text(
            text = "${score}pts",
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp,
                letterSpacing = 0.3.sp,
            ),
            color = color,
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
        )
    }
}

@Composable
private fun scoreColor(score: Int): Color = when {
    score >= 80 -> MaterialTheme.colorScheme.primary
    score >= 50 -> Color(0xFFF59E0B)
    else -> MaterialTheme.colorScheme.error
}