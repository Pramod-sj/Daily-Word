package com.pramod.dailyword.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import com.pramod.dailyword.SnackbarMessage
import com.pramod.dailyword.helper.WindowPreferencesManager

abstract class BaseActivity<T : ViewDataBinding, V : BaseViewModel> :
    AppCompatActivity() {
    lateinit var mBinding: T
    lateinit var mViewModel: V

    abstract fun getLayoutId(): Int
    abstract fun getViewModel(): V
    abstract fun getBindingVariable(): Int

    override fun onCreate(savedInstanceState: Bundle?) {
        shouldApplyEdgeToEdge()
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView<T>(this@BaseActivity, getLayoutId())
        mViewModel = getViewModel()
        mBinding.lifecycleOwner = this
        mBinding.setVariable(getBindingVariable(), mViewModel)
        mBinding.executePendingBindings()
        setSnackBarObserver()
    }


    fun showSnackBar(
        snackbarMessage: SnackbarMessage
    ) {
        val snackbar =
            Snackbar.make(mBinding.root, snackbarMessage.message, snackbarMessage.duration)
        snackbar.show()
    }

    fun shouldApplyEdgeToEdge() {
        val pref = WindowPreferencesManager.newInstance(this)
        pref.applyEdgeToEdgePreference(window)
    }

    private fun setSnackBarObserver() {
        mViewModel.getMessage().observe(this, Observer {
            it.getContentIfNotHandled()?.let { snackbar ->
                showSnackBar(snackbar)
            }
        })
    }

}