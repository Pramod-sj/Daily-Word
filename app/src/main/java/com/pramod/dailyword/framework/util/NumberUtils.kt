package com.pramod.dailyword.framework.util

fun convertNumberRangeToAnotherRange(
    oldValue: Float,
    oldRange: Pair<Int, Int>,
    newRange: Pair<Int, Int>
): Int {

    return (((oldValue - oldRange.first) / (oldRange.second - oldRange.first)) *
            (newRange.second - newRange.first) +
            newRange.first).toInt()
}

fun convertNumberRangeToAnotherRangeFloat(
    oldValue: Float,
    oldRange: Pair<Float, Float>,
    newRange: Pair<Float, Float>
): Int {

    return (((oldValue - oldRange.first) / (oldRange.second - oldRange.first)) *
            (newRange.second - newRange.first) +
            newRange.first).toInt()
}