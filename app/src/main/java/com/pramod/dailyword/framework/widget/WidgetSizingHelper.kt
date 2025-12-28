package com.pramod.dailyword.framework.widget

import android.os.Build
import kotlin.math.ceil

object ICSWidgetSizing {
    /**
     * Returns number of cells for given size (Android 4.0-11)
     */
    fun getCellsForSize(sizeDp: Int): Int {
        // Reverse formula: n = (size + 30) / 70
        return ceil(((sizeDp + 30) / 70).toDouble()).toInt().coerceAtLeast(1)
    }

    /**
     * Returns size in dp for given cells (Android 4.0-11)
     */
    fun getSizeForCells(cells: Int): Int {
        return (70 * cells) - 30
    }

    // Common sizes
    fun getCommonSizes() = mapOf(
        1 to 40,   // 1 cell: 40dp
        2 to 110,  // 2 cells: 110dp
        3 to 180,  // 3 cells: 180dp
        4 to 250,  // 4 cells: 250dp
        5 to 320   // 5 cells: 320dp
    )
}

object ModernWidgetSizing {
    /**
     * Portrait mode sizing (typical 5x4 grid)
     */
    /**
     * Portrait mode sizing (typical 5x4 grid)
     */
    object Portrait {
        fun getCellsForWidth(widthDp: Int): Int {
            // Formula: width = (73n - 16)
            return ((widthDp + 16) / 73.0).toInt().coerceAtLeast(1)
        }

        fun getCellsForHeight(heightDp: Int): Int {
            // Formula: height = (118m - 16)
            return ((heightDp + 16) / 118.0).toInt().coerceAtLeast(1)
        }
    }

    /**
     * Landscape mode sizing
     */
    object Landscape {
        fun getCellsForWidth(widthDp: Int): Int {
            // Formula: width = (142n - 15)
            return ((widthDp + 15) / 142.0).toInt().coerceAtLeast(1)
        }

        fun getCellsForHeight(heightDp: Int): Int {
            // Formula: height = (66m - 15)
            return ((heightDp + 15) / 66.0).toInt().coerceAtLeast(1)
        }

        fun getSizeForCells(widthCells: Int, heightCells: Int): Pair<Int, Int> {
            return Pair(142 * widthCells - 15, 66 * heightCells - 15)
        }
    }
}

/**
 * UNIVERSAL HELPER: Automatically uses correct formula based on API level
 */
object WidgetSizeHelper {

    /**
     * Returns NUMBER OF WIDTH CELLS occupied by given width in dp
     *
     * Example: getCellsForWidth(180) → 3 (means widget occupies 3 cells wide)
     */
    fun getCellsForWidth(widthDp: Int): Int {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                // Android 12+ - Use portrait width formula
                ModernWidgetSizing.Portrait.getCellsForWidth(widthDp)
            }

            else -> {
                // Android 4.0 to 11
                ICSWidgetSizing.getCellsForSize(widthDp)
            }
        }
    }

    /**
     * Returns NUMBER OF HEIGHT CELLS occupied by given height in dp
     *
     * Example: getCellsForHeight(220) → 2 (means widget occupies 2 cells tall)
     */
    fun getCellsForHeight(heightDp: Int): Int {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                // Android 12+ - Use portrait height formula
                ModernWidgetSizing.Portrait.getCellsForHeight(heightDp)
            }

            else -> {
                // Android 4.0 to 11 (same formula for both dimensions)
                ICSWidgetSizing.getCellsForSize(heightDp)
            }
        }
    }

}