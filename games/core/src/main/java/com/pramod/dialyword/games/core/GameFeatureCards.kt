package com.pramod.dialyword.games.core

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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

@Preview
@Composable
internal fun Preview() {
    FeatureCard(
        card = FeatureCard(
            id = TODO(),
            featureType = TODO(),
            status = TODO(),
            content = TODO(),
            visuals = TODO(),
            action = TODO()
        )
    ) { }
}

@Composable
fun GameFeatureCards(
    onPlayClick: (uri: String) -> Unit
) {

    val viewModel = hiltViewModel<FeatureCardViewModel>()

    val featureCard by viewModel.cards.collectAsState()

    Column {
        featureCard.forEach { card ->
            key(card.id) {
                FeatureCard(card = card, onPlayClick = onPlayClick)
            }
        }
    }
}


@Composable
fun FeatureCard(
    card: FeatureCard,
    onPlayClick: (uri: String) -> Unit
) {
    CrosswordTheme {

        val colorScheme = MaterialTheme.colorScheme

        val containerColor =
            when (card.visuals.colorTheme) {
                "PRIMARY" -> colorScheme.primaryContainer
                "SECONDARY" -> colorScheme.secondaryContainer
                else -> colorScheme.tertiaryContainer
            }

        val onContainerColor =
            when (card.visuals.colorTheme) {
                "PRIMARY" -> colorScheme.onPrimaryContainer
                "SECONDARY" -> colorScheme.onSecondaryContainer
                else -> colorScheme.onTertiaryContainer
            }

        val colorAccent = when (card.visuals.colorTheme) {
            "PRIMARY" -> colorScheme.primary
            "SECONDARY" -> colorScheme.secondary
            else -> colorScheme.tertiary
        }

        val onColorAccent = when (card.visuals.colorTheme) {
            "PRIMARY" -> colorScheme.onPrimary
            "SECONDARY" -> colorScheme.onSecondary
            else -> colorScheme.onTertiary
        }

        Card(
            onClick = {
                onPlayClick(card.action.routeUri)
            },
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

                val watermarkIcon = when (card.visuals.watermarkIcon) {
                    "GRID_ON" -> Icons.Outlined.GridOn
                    "HELP_OUTLINE" -> Icons.Outlined.Help
                    else -> null
                }

                watermarkIcon?.let {
                    Box(modifier = Modifier.matchParentSize()) {
                        Icon(
                            imageVector = watermarkIcon,
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

                    val icon = when (card.visuals.badgeIcon) {
                        "LIGHTBULB" -> Icons.Outlined.Lightbulb
                        "EXTENSION" -> Icons.Outlined.Extension
                        else -> null
                    }

                    icon?.let {

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = containerColor,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = onContainerColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))
                    }


                    // Center Text
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = card.content.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colorAccent
                        )
                        Text(
                            text = card.content.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurface.copy(alpha = 0.7f),
                            lineHeight = 16.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Right "Play" Button CTA
                    Surface(
                        shape = CircleShape,
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