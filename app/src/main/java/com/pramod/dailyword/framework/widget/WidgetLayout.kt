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
 * Determines appropriate widget layout based on cell dimensions
 */
fun getWidgetLayout(
    rowCell: Int,
    colCell: Int,
    isTablet: Boolean
): WidgetLayout {
    return if (isTablet) {
        getTabletLayout(rowCell, colCell)
    } else {
        getPhoneLayout(rowCell, colCell)
    }
}

/**
 * Phone layout logic:
 * - SMALL: 1×1 to 2×2 (very compact)
 * - MEDIUM: 2×2 to 4×3 (moderate space)
 * - LARGE: 4×3+ (plenty of space for scrolling)
 */
private fun getPhoneLayout(rowCell: Int, colCell: Int): WidgetLayout {
    return when {
        rowCell <= 1 && colCell <= 5 -> WidgetLayout.SMALL
        rowCell <= 3 && colCell <= 4 -> WidgetLayout.MEDIUM
        else -> WidgetLayout.LARGE
    }
}

/**
 * Tablet layout logic:
 * - SMALL: 1×1 to 2×3 (compact for tablet)
 * - MEDIUM: 2×3 to 4×4 (moderate)
 * - LARGE: 4×4+ (lots of space)
 */
private fun getTabletLayout(rowCell: Int, colCell: Int): WidgetLayout {
    return when {
        rowCell <= 2 && colCell <= 3 -> WidgetLayout.SMALL
        rowCell <= 4 && colCell <= 5 -> WidgetLayout.MEDIUM
        else -> WidgetLayout.LARGE
    }
}