package com.pramod.todaysword.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.google.android.material.snackbar.Snackbar
import com.pramod.todaysword.SnackbarMessage
import com.pramod.todaysword.helper.WindowPreferencesManager

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
    }


    fun showSnackBar(
        snackbarMessage: SnackbarMessage,
        onActionClickListener: View.OnClickListener? = null
    ) {
        val snackbar =
            Snackbar.make(mBinding!!.root, snackbarMessage.message, snackbarMessage.duration)
        snackbarMessage.actionText?.let {
            snackbar.setAction(snackbarMessage.actionText, onActionClickListener)
        }
    }

    fun shouldApplyEdgeToEdge() {
        val pref = WindowPreferencesManager.newInstance(this)
        pref.applyEdgeToEdgePreference(window)
    }

}