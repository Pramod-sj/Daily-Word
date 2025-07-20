package com.pramod.dailyword.framework.ui.dialog

import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.internal.NavigationMenuView
import com.pramod.dailyword.R
import com.pramod.dailyword.databinding.DialogBottomMenuLayoutBinding
import com.pramod.dailyword.framework.ui.common.ExpandingBottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BottomMenuDialog :
    ExpandingBottomSheetDialogFragment<DialogBottomMenuLayoutBinding>(R.layout.dialog_bottom_menu_layout) {

    var bottomMenuItemClickListener: BottomMenuItemClickListener? = null

    override fun getBottomSheetBehaviorView(): View {
        return binding.cardBottomSheet
    }

    override fun lockBottomSheetDragWhenExpanded(): Boolean {
        return false
    }

    override fun getInitialDelay(): Long {
        return 200
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleNavItemClick()
    }

    private fun handleNavItemClick() {
        (binding.navigationView.getChildAt(0) as NavigationMenuView)
            .layoutManager = object : LinearLayoutManager(requireContext()) {
            override fun canScrollVertically(): Boolean {
                return false
            }
        }
        binding.navigationView.setNavigationItemSelectedListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            Handler().postDelayed({
                bottomMenuItemClickListener?.onMenuItemClick(it)
            }, 200)
            false
        }
    }

    companion object {

        val TAG = BottomMenuDialog::class.java.simpleName

        fun show(fragmentManager: FragmentManager): BottomMenuDialog {
            val dialog = BottomMenuDialog()
            dialog.show(fragmentManager, TAG)
            return dialog
        }
    }

    interface BottomMenuItemClickListener {
        fun onMenuItemClick(menuItem: MenuItem)
    }
}