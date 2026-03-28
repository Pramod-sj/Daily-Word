package com.pramod.dialyword.games.featureCard.dismissible

import com.pramod.dialyword.games.featureCard.DismissScope
import kotlinx.serialization.Serializable

@Serializable
internal data class DismissRecord(
    val cardId: String,
    val scope: DismissScope,
    val expiresAt: Long? = null
)