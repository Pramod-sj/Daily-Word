package com.pramod.dailyword.framework.ui.common

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.google.android.material.snackbar.Snackbar
import timber.log.Timber

abstract class BaseActivity<T : ViewDataBinding, V : BaseViewModel>(
    private val layoutId: Int
) : ThemedActivity() {

    lateinit var binding: T

    abstract val viewModel: V

    abstract val bindingVariable: Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this@BaseActivity, layoutId)
        binding.lifecycleOwner = this
        viewModel.isEdgeToEdgeEnabled = edgeToEdgePrefManager.isEnabled()
        binding.setVariable(bindingVariable, viewModel)
        binding.executePendingBindings()
        setMessageObserver()
    }

    private fun safeViewFinding(parent: View, viewId: Int): View? {
        return try {
            parent.findViewById(viewId)
        } catch (e: Exception) {
            null
        }
    }

    private fun setMessageObserver() {
        viewModel.message.observe(this) { message ->
            message?.let {
                when (it) {
                    is Message.SnackBarMessage -> {
                        handleSnackBarMessage(it)
                        Timber.i( "setSnackBarObserver: snackbar message")
                    }
                    is Message.ToastMessage -> {
                        Toast.makeText(this, it.message, it.duration).show()
                        Timber.i( "setSnackBarObserver: toast message")
                    }
                    is Message.DialogMessage -> {
                        Timber.i( "setSnackBarObserver: dialog message")
                    }
                }
            }

        }
    }

    private fun handleSnackBarMessage(it: Message.SnackBarMessage) {
        if (it.isShown) {
            return
        }
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
            override fun onShown(sb: Snackbar?) {
                super.onShown(sb)

                it.isShown = true

                viewModel.setMessage(null)
                snackBar.removeCallback(this)
            }

            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                super.onDismissed(transientBottomBar, event)
                viewModel.setMessage(null)
                snackBar.removeCallback(this)
            }
        })
        snackBar.show()

    }

    companion object {
        val TAG = BaseActivity::class.java.simpleName
    }

}