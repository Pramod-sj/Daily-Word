package com.pramod.dailyword.ui

import android.R
import android.graphics.Insets
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import com.pramod.dailyword.SnackbarMessage
import com.pramod.dailyword.helper.ThemeManager
import com.pramod.dailyword.helper.WindowPrefManager
import com.pramod.dailyword.util.CommonUtils


abstract class BaseActivity<T : ViewDataBinding, V : BaseViewModel> :
    AppCompatActivity() {
    lateinit var mBinding: T
    lateinit var mViewModel: V

    abstract fun getLayoutId(): Int
    abstract fun getViewModel(): V
    abstract fun getBindingVariable(): Int

    private var forceEdgeToEdge = false
    fun forceEdgeToEdge(forceEdgeToEdge: Boolean) {
        this.forceEdgeToEdge = forceEdgeToEdge
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        shouldApplyEdgeToEdge()
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView<T>(this@BaseActivity, getLayoutId())
        mViewModel = getViewModel()
        mBinding.lifecycleOwner = this
        mBinding.setVariable(getBindingVariable(), mViewModel)
        mBinding.executePendingBindings()
        //adjusting views based onn inset
        if (WindowPrefManager.newInstance(this).isEdgeToEdgeEnabled()) {
            ViewCompat.setOnApplyWindowInsetsListener(
                mBinding.root
            ) { v, insets ->
                arrangeViewsForEdgeToEdge(v, insets)
                insets
            }
        }
        setSnackBarObserver()
    }


    private fun showSnackBar(
        snackbarMessage: SnackbarMessage
    ) {
        val snackbar =
            Snackbar.make(mBinding.root, snackbarMessage.message, snackbarMessage.duration)
        snackbar.animationMode = Snackbar.ANIMATION_MODE_SLIDE
        snackbar.show()
    }

    private fun shouldApplyEdgeToEdge() {
        val pref = WindowPrefManager.newInstance(this)
        pref.applyEdgeToEdgeIfEnabled(window, forceEdgeToEdge)
    }

    private fun setSnackBarObserver() {
        mViewModel.getMessage().observe(this, Observer {
            it.getContentIfNotHandled()?.let { snackbar ->
                showSnackBar(snackbar)
            }
        })
    }

    open fun lightStatusBar(
        makeLight: Boolean = !ThemeManager.isNightModeActive(this),
        matchingBackgroundColor: Boolean = true
    ) {
        configStatus(makeLight, -1, matchingBackgroundColor)
    }

    open fun lightStatusBar(makeLight: Boolean, statusBarColorResId: Int) {
        configStatus(makeLight, statusBarColorResId, false)
    }

    open fun lightStatusBar(makeLight: Boolean) {
        configStatus(makeLight, -1, false)
    }


    private fun configStatus(
        makeLight: Boolean,
        statusBarColorResId: Int,
        matchingBackgroundColor: Boolean
    ) {
        Log.i("BASE ACTIVITY", makeLight.toString())
        val oldFlags = window.decorView.systemUiVisibility
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            var flags = oldFlags
            flags = if (makeLight) {
                flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            }
            window.decorView.systemUiVisibility = flags
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            if (matchingBackgroundColor) {
                window.statusBarColor =
                    CommonUtils.resolveAttrToColor(this, android.R.attr.windowBackground)
            } else {
                window.statusBarColor = ContextCompat.getColor(
                    this,
                    if (statusBarColorResId != -1) statusBarColorResId
                    else (if (makeLight) R.color.white else R.color.black)
                )
            }
            Log.i("STATUS BAR COLOR", window.statusBarColor.toString())
        } else {
            window.statusBarColor = resources.getColor(com.pramod.dailyword.R.color.black)
        }
    }

    abstract fun arrangeViewsForEdgeToEdge(view: View, insets: WindowInsetsCompat)

}