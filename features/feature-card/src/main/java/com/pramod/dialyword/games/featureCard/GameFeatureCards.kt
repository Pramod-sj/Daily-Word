package com.pramod.dialyword.games.featureCard

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
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
                        overlineText = "1 JAN - 8 JAN' 25",
                        title = "Weekly Crossword - WEEK 1",
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
                        overlineText = "TODAY'S",
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

            FeatureNudge(
                card = FeatureCard(
                    id = "feat_cw_wk42",
                    gameId = "1",
                    gameType = "CROSSWORD",
                    status = "NEW",
                    content = CardContent(
                        overlineText = "LAST WEEK'S CROSSWORD",
                        title = "Weekly Crossword - 1",
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
                    ),
                ),
                onTap = {},
                onDismiss = {}
            )

        }
    }
}

@Composable
fun GameFeatureCards(
    screenName: String,
    onPlayClick: (uri: String) -> Unit
) {

    val viewModel = hiltViewModel<FeatureCardViewModel>()

    val featuresState by viewModel.featureCardsState.collectAsStateWithLifecycle()

    LaunchedEffect(screenName) { viewModel.getFeatureCard(screenName) }

    when (featuresState) {
        is FeatureUiState.Error -> Unit
        FeatureUiState.Loading -> Unit
        is FeatureUiState.Success -> {
            Column(modifier = Modifier.animateContentSize()) {
                (featuresState as FeatureUiState.Success).cards.forEach { card ->
                    key(card.id) {
                        when (card.placement?.cardTypeEnum) {
                            CardDispayType.CARD -> {
                                FeatureCard(
                                    card = card,
                                    onPlayClick = {
                                        if (card.dismissConfig
                                                .isDismissTrigger(DismissTrigger.ON_CARD_TAP)
                                        ) {
                                            viewModel.onCardDismissed(card)
                                        }
                                        onPlayClick(it)
                                    }
                                )
                            }

                            CardDispayType.NUDGE -> {
                                FeatureNudge(
                                    card = card,
                                    onTap = {
                                        if (card.dismissConfig
                                                .isDismissTrigger(DismissTrigger.ON_CARD_TAP)
                                        ) {
                                            viewModel.onCardDismissed(card)
                                        }
                                        onPlayClick(it)
                                    },
                                    onDismiss = { card ->
                                        viewModel.onCardDismissed(card)
                                    }
                                )
                            }

                            else -> Unit
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun FeatureCard(
    card: FeatureCard,
    onPlayClick: (uri: String) -> Unit
) {
    val hasResult = card.result != null
    val score = card.result?.score
    val timeTakenMillis = card.result?.completionTimeMillis

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
            onClick = {
                card.action?.routeUri?.let {
                    onPlayClick(it)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (card.animation != null && card.result == null) {
                        when (card.animation.typeEnum) {
                            CardAnimationType.BOUNCE -> {
                                Modifier.bounceAnimation(
                                    key = card.id.orEmpty(),
                                    playCount = card.animation.playCount ?: 1,
                                    startDelayMs = card.animation.startDelayMs ?: 0L
                                )
                            }

                            else -> Modifier
                        }
                    } else Modifier
                )
                .padding(horizontal = 16.dp, vertical = 6.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainer),
            border = BorderStroke(
                width = if (hasResult) 1.5.dp else 1.dp,
                color = containerColor
            )
        ) {
            Column(
                modifier = Modifier.then(
                    if (card.animation != null && card.result == null) {
                        when (card.animation.typeEnum) {
                            CardAnimationType.GLITTER_SHIMMER -> {
                                Modifier.glitterShimmer(
                                    key = card.id.orEmpty(),
                                    playCount = card.animation.playCount ?: 1,
                                    startDelayMs = card.animation.startDelayMs ?: 0L
                                )
                            }

                            else -> Modifier
                        }
                    } else Modifier
                )
            ) {

                // ── Top section ───────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clipToBounds()
                ) {

                    // Watermark Icon
                    val watermarkIcon = WatermarkIcon.from(card.visuals?.watermarkIcon?.uppercase())

                    watermarkIcon?.let {
                        Box(modifier = Modifier.matchParentSize()) {
                            Icon(
                                imageVector = it.toImageVector(),
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
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val badgeIcon = when (card.visuals?.badgeIcon?.uppercase()) {
                            "LIGHTBULB" -> Icons.Outlined.Lightbulb
                            "EXTENSION" -> Icons.Outlined.Extension
                            else -> null
                        }
                        badgeIcon?.let {
                            Surface(
                                shape = RoundedCornerShape(7.dp),
                                color = containerColor,
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = it,
                                        contentDescription = null,
                                        tint = onContainerColor,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                        }

                        // ── TEXT COLUMN ─────────────────────────────────────
                        Column(modifier = Modifier.weight(1f)) {

                            card.content?.overlineText?.let { overlineText ->
                                Text(
                                    text = overlineText,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = colorAccent.copy(alpha = 0.8f),
                                    modifier = Modifier.padding(bottom = 2.dp),
                                    letterSpacing = 0.5.sp // Adds a nice premium feel to small caps
                                )
                            }

                            Text(
                                text = card.content?.title.orEmpty(),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = colorAccent
                            )
                            card.content?.subtitle?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colorScheme.onSurface.copy(alpha = 0.6f),
                                    lineHeight = 15.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        if (!hasResult) {
                            Spacer(modifier = Modifier.width(10.dp))
                            Surface(
                                shape = CircleShape,
                                color = colorAccent,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.PlayArrow,
                                        contentDescription = "Play",
                                        tint = onColorAccent,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Result strip ──────────────────────────────────────────
                if (hasResult && score != null) {
                    HorizontalDivider(
                        color = containerColor,
                        thickness = 1.dp
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(containerColor.copy(alpha = 0.25f))
                            .padding(horizontal = 14.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Score
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.EmojiEvents,
                                contentDescription = null,
                                tint = colorAccent,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "Your Score",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                                Text(
                                    text = "$score / 100",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = colorAccent
                                )
                            }
                        }

                        // Separator
                        Box(
                            modifier = Modifier
                                .height(22.dp)
                                .width(1.dp)
                                .background(containerColor)
                        )

                        // Time
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Timer,
                                contentDescription = null,
                                tint = colorScheme.onSurface.copy(alpha = 0.5f),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "Time taken",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                                Text(
                                    text = timeTakenMillis?.formatAsTime() ?: "—",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.onSurface.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun FeatureNudge(
    card: FeatureCard,
    onTap: (uri: String) -> Unit,
    onDismiss: ((FeatureCard) -> Unit)? = null
) {
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

        Surface(
            onClick = { card.action?.routeUri?.let { onTap(it) } },
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (card.animation != null && card.result == null) {
                        when (card.animation.typeEnum) {
                            // glitter is perform in child view
                            CardAnimationType.BOUNCE -> {
                                Modifier.bounceAnimation(
                                    key = card.id.orEmpty(),
                                    playCount = card.animation.playCount ?: 1,
                                    startDelayMs = card.animation.startDelayMs ?: 0L
                                )
                            }

                            else -> Modifier
                        }
                    } else Modifier
                )
                .padding(horizontal = 16.dp, vertical = 4.dp),
            shape = RoundedCornerShape(10.dp),
            // Match FeatureCard background logic (surfaceContainer)
            color = colorScheme.surfaceContainer,
            // Match FeatureCard border logic
            border = BorderStroke(1.dp, containerColor)
        ) {
            Row(
                modifier = Modifier
                    .then(
                        if (card.animation != null && card.result == null) {
                            when (card.animation.typeEnum) {
                                CardAnimationType.GLITTER_SHIMMER -> {
                                    Modifier.glitterShimmer(
                                        key = card.id.orEmpty(),
                                        playCount = card.animation.playCount ?: 1,
                                        startDelayMs = card.animation.startDelayMs ?: 0L
                                    )
                                }

                                else -> Modifier
                            }
                        } else Modifier
                    )
                    .padding(
                        start = 10.dp,
                        end = if (onDismiss != null) 6.dp else 10.dp,
                        top = 8.dp,
                        bottom = 8.dp
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Badge icon
                val badgeIcon = when (card.visuals?.badgeIcon?.uppercase()) {
                    "LIGHTBULB" -> Icons.Outlined.Lightbulb
                    "EXTENSION" -> Icons.Outlined.Extension
                    else -> null
                }
                badgeIcon?.let {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        // Match FeatureCard badge background
                        color = containerColor,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = it,
                                contentDescription = null,
                                // Match FeatureCard badge icon tint
                                tint = onContainerColor,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                // Title + subtitle inline
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = card.content?.title.orEmpty(),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        // Match FeatureCard title color
                        color = colorAccent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    card.content?.subtitle?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.labelMedium,
                            // Match FeatureCard subtitle color
                            color = colorScheme.onSurface.copy(alpha = 0.6f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            softWrap = false
                        )
                    }
                }

                // Right slot — close button or chevron
                if (onDismiss != null) {
                    Box(modifier = Modifier.padding(end = 2.dp)) {
                        Surface(
                            onClick = { onDismiss(card) },
                            shape = CircleShape,
                            // Subtle background for the close button
                            color = containerColor.copy(alpha = 0.5f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Dismiss",
                                    // Match accent color for consistency
                                    tint = colorAccent,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                } else {
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        // Tint chevron to match accent color slightly muted
                        tint = colorAccent.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CardWatermark(
    key: String?,
    tint: Color,
    modifier: Modifier = Modifier
) {
    val icon = remember(key) { WatermarkIcon.from(key)?.toImageVector() } ?: return

    Box(modifier = modifier) {
        // Back icon — smaller, dimmer, offset to the left
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint.copy(alpha = 0.035f),
            modifier = Modifier
                .size(52.dp)
                .align(Alignment.BottomEnd)
                .offset(x = (-42).dp, y = 6.dp)
                .graphicsLayer(rotationZ = -15f)
        )
        // Front icon — larger, slightly stronger, bleeds off the edge
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint.copy(alpha = 0.07f),
            modifier = Modifier
                .size(80.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 10.dp, y = 18.dp)
                .graphicsLayer(rotationZ = -15f)
        )
    }
}

private fun Long.formatAsTime(): String {
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


internal enum class WatermarkIcon(val key: String) {
    GRID_ON("GRID_ON"),
    HELP_OUTLINE("HELP_OUTLINE"),
    EXTENSION("EXTENSION"),
    LIGHTBULB("LIGHTBULB"),
    STAR("STAR"),
    SCHOOL("SCHOOL");

    companion object {
        fun from(key: String?): WatermarkIcon? =
            entries.find { it.key == key?.uppercase() }
    }
}

internal fun WatermarkIcon.toImageVector(): ImageVector = when (this) {
    WatermarkIcon.GRID_ON -> Icons.Outlined.GridOn
    WatermarkIcon.HELP_OUTLINE -> Icons.Outlined.Help
    WatermarkIcon.EXTENSION -> Icons.Outlined.Extension
    WatermarkIcon.LIGHTBULB -> Icons.Outlined.Lightbulb
    WatermarkIcon.STAR -> Icons.Outlined.Star
    WatermarkIcon.SCHOOL -> Icons.Outlined.School
}