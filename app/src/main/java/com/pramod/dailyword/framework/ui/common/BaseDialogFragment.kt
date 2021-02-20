package com.pramod.dailyword.framework.ui.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment

abstract class BaseDialogFragment<T : ViewDataBinding, V : BaseViewModel> : DialogFragment() {
    lateinit var mBinding: T
    lateinit var mViewModel: V

    abstract fun getLayoutId(): Int
    abstract fun getViewModel(): V
    abstract fun getBindingVariable(): Int

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = DataBindingUtil.inflate(inflater, getLayoutId(), container, false)
        return mBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mViewModel = getViewModel()
        mBinding.setVariable(getBindingVariable(), mViewModel)
        mBinding.lifecycleOwner = viewLifecycleOwner
        mBinding.executePendingBindings()
    }
}