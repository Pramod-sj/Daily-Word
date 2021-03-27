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

fun convertNumberRangeToAnotherRangeToFloat(
    oldValue: Float,
    oldRange: Pair<Int, Int>,
    newRange: Pair<Int, Int>
): Float {

    return (((oldValue - oldRange.first) / (oldRange.second - oldRange.first)) *
            (newRange.second - newRange.first) +
            newRange.first)
}

fun convertNumberRangeToAnotherRangeFromFloat(
    oldValue: Float,
    oldRange: Pair<Float, Float>,
    newRange: Pair<Float, Float>
): Int {

    return (((oldValue - oldRange.first) / (oldRange.second - oldRange.first)) *
            (newRange.second - newRange.first) +
            newRange.first).toInt()
}

fun convertNumberRangeToAnotherRangeFromFloatToFloat(
    oldValue: Float,
    oldRange: Pair<Float, Float>,
    newRange: Pair<Float, Float>
): Float {

    return (((oldValue - oldRange.first) / (oldRange.second - oldRange.first)) *
            (newRange.second - newRange.first) +
            newRange.first)
}