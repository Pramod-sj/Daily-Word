package com.pramod.dailyword.framework.ui.common.exts

import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.pramod.dailyword.R


fun AppCompatActivity.setUpToolbar(
    toolbar: Toolbar,
    title: String?,
    showNavAsBack: Boolean?,
    @DrawableRes
    navIcon: Int? = null,
    navIconClickListener: (() -> Unit)? = null
) {
    setSupportActionBar(toolbar)
    supportActionBar?.let {
        it.title = null
    }
    toolbar.title = title
    if (showNavAsBack == true) {
        toolbar.setNavigationIcon(R.drawable.ic_round_back_arrow)
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }
    navIcon?.let {
        toolbar.setNavigationIcon(it)
    }
    navIconClickListener?.let {
        toolbar.setNavigationOnClickListener {
            navIconClickListener.invoke()
        }
    }
}