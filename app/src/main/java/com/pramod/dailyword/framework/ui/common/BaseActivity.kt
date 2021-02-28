package com.pramod.dailyword.framework.ui.common

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.google.android.material.snackbar.Snackbar

abstract class BaseActivity<T : ViewDataBinding, V : BaseViewModel> : ThemedActivity() {

    lateinit var binding: T
    lateinit var mViewModel: V

    abstract val layoutId: Int
    abstract val viewModel: V
    abstract val bindingVariable: Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.isEdgeToEdgeEnabled = egdeToEdgePrefManager.isEnabled()
        binding = DataBindingUtil.setContentView(this@BaseActivity, layoutId)
        binding.lifecycleOwner = this
        mViewModel = viewModel
        binding.setVariable(bindingVariable, mViewModel)
        binding.executePendingBindings()
        setMessageObserver()
    }

    companion object {
        val TAG = BaseActivity::class.java.simpleName
    }

    private fun safeViewFinding(parent: View, viewId: Int): View? {
        return try {
            parent.findViewById(viewId)
        } catch (e: Exception) {
            null
        }
    }

    private fun setMessageObserver() {
        mViewModel.message.observe(this) { message ->
            message?.let {
                when (it) {
                    is Message.SnackBarMessage -> {
                        handleSnackBarMessage(it)
                        Log.i(TAG, "setSnackBarObserver: snackbar message")
                    }
                    is Message.ToastMessage -> {
                        Log.i(TAG, "setSnackBarObserver: toast message")
                    }
                    is Message.DialogMessage -> {
                        Log.i(TAG, "setSnackBarObserver: dialog message")
                    }
                }
            }

        }
    }

    private fun handleSnackBarMessage(it: Message.SnackBarMessage) {
        val snackBar = Snackbar.make(
            if (it.parentViewId != null)
                safeViewFinding(
                    binding.root,
                    it.parentViewId
                ) ?: binding.root
            else binding.root,
            it.message,
            it.duration
        ).setAnimationMode(it.animation)
        it.action?.let { action ->
            snackBar.setAction(action.name) { v ->
                it.action.callback?.invoke()
            }
        }
        it.anchorId?.let { id ->
            snackBar.setAnchorView(id)
        }
        snackBar.addCallback(object : Snackbar.Callback() {
            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                super.onDismissed(transientBottomBar, event)
                mViewModel.setMessage(null)
                snackBar.removeCallback(this)
            }
        })
        snackBar.show()

    }

}