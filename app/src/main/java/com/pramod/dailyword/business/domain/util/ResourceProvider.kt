package com.pramod.dailyword.business.domain.util

import androidx.annotation.ColorRes

interface ResourceProvider {

    fun getColor(@ColorRes id: Int): Int

}