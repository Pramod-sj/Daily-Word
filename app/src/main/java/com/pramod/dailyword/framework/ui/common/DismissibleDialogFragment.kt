package com.pramod.dailyword.framework.ui.common

import android.content.DialogInterface
import androidx.annotation.LayoutRes
import androidx.fragment.app.DialogFragment

open class DismissibleDialogFragment : DialogFragment {

    constructor() : super()

    constructor(@LayoutRes layoutId: Int) : super(layoutId)


    private var onDismissListener: DialogInterface.OnDismissListener? = null

    fun setOnDismissListener(onDismissListener: DialogInterface.OnDismissListener?): DismissibleDialogFragment {
        this.onDismissListener = onDismissListener
        return this
    }


    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissListener?.onDismiss(dialog)
    }

}