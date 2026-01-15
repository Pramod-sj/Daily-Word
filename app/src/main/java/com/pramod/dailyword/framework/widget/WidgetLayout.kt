package com.pramod.dailyword.framework.widget

/**
 * Improved Widget Layout Selection Logic
 *
 * Defines clear size categories based on grid cells occupied
 */

enum class WidgetLayout {
    SMALL,      // Minimal content, compact design
    MEDIUM,     // Moderate content
    LARGE,      // Full content with scrolling
}

/**
 * Determines appropriate widget layout based on dimensions in DP
 */
fun getWidgetLayout(
    widthDp: Int,
    heightDp: Int,
    isTablet: Boolean
): WidgetLayout {
    return if (isTablet) {
        getTabletLayout(widthDp, heightDp)
    } else {
        getPhoneLayout(widthDp, heightDp)
    }
}

/**
 * Phone layout logic:
 * - SMALL: Height < 100dp (approx 1 row)
 * - MEDIUM: Height < 180dp (approx 2 rows) OR Width < 260dp (Narrow)
 * - LARGE: Height >= 180dp AND Width >= 260dp (Scrollable)
 */
private fun getPhoneLayout(widthDp: Int, heightDp: Int): WidgetLayout {
    return when {
        // Height < 100dp -> 1 Row -> SMALL
        heightDp < 100 || widthDp < 200 -> WidgetLayout.SMALL

        // Height < 180dp -> 2 Rows -> MEDIUM
        heightDp < 180 -> WidgetLayout.MEDIUM

        // Tall but narrow (< 260dp width) -> MEDIUM
        widthDp < 260 -> WidgetLayout.MEDIUM

        // Tall and Wide -> LARGE
        else -> WidgetLayout.LARGE
    }
}


/**
 * Tablet layout logic:
 * - SMALL: Compact
 * - MEDIUM: Moderate
 * - LARGE: Spacious
 */
private fun getTabletLayout(widthDp: Int, heightDp: Int): WidgetLayout {
    return when {
        heightDp < 140 && widthDp < 210 -> WidgetLayout.SMALL
        heightDp < 280 && widthDp < 350 -> WidgetLayout.MEDIUM
        else -> WidgetLayout.LARGE
    }
}