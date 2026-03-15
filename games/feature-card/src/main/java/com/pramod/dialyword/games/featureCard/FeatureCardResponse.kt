package com.pramod.dialyword.games.featureCard

import com.google.gson.annotations.SerializedName
import com.pramod.dailyword.games.results.GameResultEntity


data class FeatureCardResponse(
    @SerializedName("feature_cards") val featureCards: List<FeatureCard>
)


data class FeatureCard(
    @SerializedName("id") val id: String?,
    @SerializedName("game_id") val gameId: String?,
    @SerializedName("game_type") val gameType: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("content") val content: CardContent?,
    @SerializedName("visuals") val visuals: CardVisuals?,
    @SerializedName("action") val action: CardAction?,
    @SerializedName("placement") val placement: CardPlacement? = null,
    @SerializedName("dismiss_config") val dismissConfig: DismissConfig? = null,
    @SerializedName("animation") val animation: CardAnimation? = null,

    val result: GameResultEntity? = null,
)

data class CardContent(
    @SerializedName("title") val title: String?, @SerializedName("subtitle") val subtitle: String?
)


data class CardVisuals(
    @SerializedName("badge_icon") val badgeIcon: String?,
    @SerializedName("watermark_icon") val watermarkIcon: String?,
    @SerializedName("color_theme") val colorTheme: String?
)


data class CardAction(
    @SerializedName("button_text") val buttonText: String?,
    @SerializedName("route_uri") val routeUri: String?
)


data class DismissConfig(
    @SerializedName("enabled") val enabled: Boolean?,
    @SerializedName("scope") val scope: String?,
    @SerializedName("ttl_hours") val ttlHours: Int?
) {
    val scopeEnum: DismissScope get() = DismissScope.from(scope)
}


data class CardPlacement(
    @SerializedName("promoted") val promoted: Boolean?,
    @SerializedName("screens") val screens: List<String>?,
    @SerializedName("display_style") val cardType: String?
) {
    val cardTypeEnum: CardDispayType get() = CardDispayType.from(cardType)
}


enum class DismissScope {
    SESSION, PERSISTENT, TIMED;

    companion object {
        fun from(value: String?): DismissScope =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: SESSION
    }
}


enum class CardDispayType {
    CARD, NUDGE;

    companion object {
        fun from(value: String?): CardDispayType =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: CARD
    }
}

data class CardAnimation(
    @SerializedName("type") val type: String?,
    @SerializedName("play_count") val playCount: Int?,
    @SerializedName("start_delay_ms") val startDelayMs: Long?
) {
    val typeEnum: CardAnimationType get() = CardAnimationType.from(type)
}


enum class CardAnimationType {
    NONE,
    GLITTER_SHIMMER,
    BOUNCE;

    companion object {
        fun from(value: String?): CardAnimationType =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: NONE
    }
}