package com.pramod.dailyword.framework.util

import android.content.Context
import com.pramod.dailyword.business.domain.util.ResourceProvider
import com.pramod.dailyword.framework.ui.common.exts.getContextCompatColor

class ResourceProviderImpl(
    private val context: Context
) : ResourceProvider {

    override fun getColor(id: Int): Int {
        return context.getContextCompatColor(id)
    }

}