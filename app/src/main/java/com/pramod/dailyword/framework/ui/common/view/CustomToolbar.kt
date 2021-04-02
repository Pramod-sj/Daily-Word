package com.pramod.dailyword.framework.ui.common.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.databinding.DataBindingUtil
import com.pramod.dailyword.R
import com.pramod.dailyword.databinding.LayoutCustomToolbarBinding

class CustomToolbar : FrameLayout {

    private var navIconResId: Int = R.drawable.ic_round_back_arrow
    private var title: String? = null
    private var subTitle: String? = null
    private var optionIconResId: Int = R.drawable.ic_more_vert_black_24dp

    private val binding: LayoutCustomToolbarBinding by lazy {
        return@lazy DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.layout_custom_toolbar,
            this,
            false
        )
    }

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {

    }

    fun applyTitlePosition() {

    }
}