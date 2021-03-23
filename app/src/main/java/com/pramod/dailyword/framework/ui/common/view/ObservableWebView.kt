package com.pramod.dailyword.framework.ui.common.view

import android.content.Context
import android.util.AttributeSet
import android.webkit.WebView

class ObservableWebView : WebView {
    var onScrollChangedCallback: OnScrollChangeListener? = null

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    override fun onScrollChanged(currentHorizontalScroll: Int, currentVerticalScroll: Int, oldHorizontalScroll: Int, oldcurrentVerticalScroll: Int) {
        super.onScrollChanged(currentHorizontalScroll, currentVerticalScroll, oldHorizontalScroll, oldcurrentVerticalScroll)

        onScrollChangedCallback?.onScrollChanged(currentHorizontalScroll, currentVerticalScroll, oldHorizontalScroll, oldcurrentVerticalScroll)
    }

    interface OnScrollChangeListener {
        fun onScrollChanged(currentHorizontalScroll: Int, currentVerticalScroll: Int, oldHorizontalScroll: Int, oldcurrentVerticalScroll: Int)
    }
}