package com.pramod.dialyword.games.featureCard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material.icons.outlined.Help
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Preview(showBackground = true, backgroundColor = 0xFFF5F5F5)
@Composable
internal fun PreviewFeatureCard() {
    MaterialTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. A brand new game without a result
            FeatureCard(
                card = FeatureCard(
                    id = "feat_cw_wk42",
                    gameId = "1",
                    gameType = "CROSSWORD",
                    status = "NEW",
                    content = CardContent(
                        title = "Weekly Crossword",
                        subtitle = "Review this week's words in a fun puzzle."
                    ),
                    visuals = CardVisuals(
                        badgeIcon = "EXTENSION",
                        watermarkIcon = "GRID_ON",
                        colorTheme = "PRIMARY"
                    ),
                    action = CardAction(
                        buttonText = "Play now",
                        routeUri = "app://dailyword.games/crossword/wk42"
                    )
                ),
                onPlayClick = {}
            )

            // 2. A completed game showing the PREMIUM score badge
            FeatureCard(
                card = FeatureCard(
                    id = "feat_qz_daily",
                    gameId = "1",
                    gameType = "QUIZ",
                    status = "COMPLETED",
                    content = CardContent(
                        title = "Daily Quick Quiz",
                        subtitle = "Lock this week’s vocabulary permanently into your long-term memory."
                    ),
                    visuals = CardVisuals(
                        badgeIcon = "LIGHTBULB",
                        watermarkIcon = "HELP_OUTLINE",
                        colorTheme = "TERTIARY"
                    ),
                    action = CardAction(
                        buttonText = "Review",
                        routeUri = "app://dailyword.games/quiz/daily"
                    )
                ),
                onPlayClick = {}
            )
        }
    }
}

@Composable
fun GameFeatureCards(
    onPlayClick: (uri: String) -> Unit
) {

    val viewModel = hiltViewModel<FeatureCardViewModel>()

    val featuresState by viewModel.featuresState.collectAsStateWithLifecycle()

    when (featuresState) {
        is FeatureUiState.Error -> Unit
        FeatureUiState.Loading -> Unit
        is FeatureUiState.Success -> {
            Column {
                (featuresState as FeatureUiState.Success).cards.forEach { card ->
                    key(card.id) {
                        FeatureCard(
                            card = card,
                            onPlayClick = onPlayClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FeatureCard(
    card: FeatureCard,
    onPlayClick: (uri: String) -> Unit
) {
    val resultText =
        remember(card.result) { card.result?.score?.let { score -> "You scored: $score" } }

    CrosswordTheme {
        val colorScheme = MaterialTheme.colorScheme
        val safeColorTheme = card.visuals?.colorTheme?.uppercase()

        val containerColor = when (safeColorTheme) {
            "PRIMARY" -> colorScheme.primaryContainer
            "SECONDARY" -> colorScheme.secondaryContainer
            else -> colorScheme.tertiaryContainer
        }

        val onContainerColor = when (safeColorTheme) {
            "PRIMARY" -> colorScheme.onPrimaryContainer
            "SECONDARY" -> colorScheme.onSecondaryContainer
            else -> colorScheme.onTertiaryContainer
        }

        val colorAccent = when (safeColorTheme) {
            "PRIMARY" -> colorScheme.primary
            "SECONDARY" -> colorScheme.secondary
            else -> colorScheme.tertiary
        }

        val onColorAccent = when (safeColorTheme) {
            "PRIMARY" -> colorScheme.onPrimary
            "SECONDARY" -> colorScheme.onSecondary
            else -> colorScheme.onTertiary
        }

        Card(
            onClick = { card.action?.routeUri?.let { onPlayClick(it) } },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = colorScheme.surfaceContainer,
            ),
            border = BorderStroke(
                width = 1.dp,
                color = containerColor
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clipToBounds()
            ) {

                // Watermark Icon
                val watermarkIcon = when (card.visuals?.watermarkIcon?.uppercase()) {
                    "GRID_ON" -> Icons.Outlined.GridOn
                    "HELP_OUTLINE" -> Icons.Outlined.Help
                    else -> null
                }

                watermarkIcon?.let {
                    Box(modifier = Modifier.matchParentSize()) {
                        Icon(
                            imageVector = it,
                            contentDescription = null,
                            tint = colorAccent.copy(alpha = 0.06f),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(90.dp)
                                .offset(x = 10.dp, y = 20.dp)
                                .graphicsLayer(rotationZ = -15f)
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Icon Badge
                    val badgeIcon = when (card.visuals?.badgeIcon?.uppercase()) {
                        "LIGHTBULB" -> Icons.Outlined.Lightbulb
                        "EXTENSION" -> Icons.Outlined.Extension
                        else -> null
                    }

                    badgeIcon?.let {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = containerColor,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = it,
                                    contentDescription = null,
                                    tint = onContainerColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                    }

                    // Center Text Column
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = card.content?.title.orEmpty(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colorAccent
                        )

                        card.content?.subtitle?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = colorScheme.onSurface.copy(alpha = 0.7f),
                                lineHeight = 16.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (resultText != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.EmojiEvents, // Trophy Icon!
                                    contentDescription = "Score",
                                    tint = colorAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = resultText,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = colorAccent
                                )
                            }
                        }
                    }

                    if (resultText == null) {

                        Spacer(modifier = Modifier.width(12.dp))

                        Surface(
                            shape = CircleShape,
                            // If completed, make the button a subtle grey/surface variant. If new, use full accent color!
                            color = colorAccent,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.PlayArrow,
                                    contentDescription = "Play",
                                    tint = onColorAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}