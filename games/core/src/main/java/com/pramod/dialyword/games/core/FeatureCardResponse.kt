package com.pramod.dialyword.games.core

import com.google.gson.annotations.SerializedName

data class FeatureCardResponse(
    @SerializedName("feature_cards")
    val featureCards: List<FeatureCard>
)

data class FeatureCard(
    val id: String,
    @SerializedName("feature_type")
    val featureType: String,
    val status: String,
    val content: CardContent,
    val visuals: CardVisuals,
    val action: CardAction
)

data class CardContent(
    val title: String,
    val subtitle: String
)

data class CardVisuals(
    @SerializedName("badge_icon") val badgeIcon: String,
    @SerializedName("watermark_icon") val watermarkIcon: String,
    @SerializedName("color_theme") val colorTheme: String
)

data class CardAction(
    @SerializedName("button_text") val buttonText: String,
    @SerializedName("route_uri") val routeUri: String
)