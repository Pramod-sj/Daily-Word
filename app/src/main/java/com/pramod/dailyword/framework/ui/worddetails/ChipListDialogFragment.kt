package com.pramod.dailyword.framework.ui.worddetails

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import com.pramod.dailyword.R
import com.pramod.dailyword.databinding.DialogChipListBinding
import com.pramod.dailyword.framework.helper.openWebsite
import com.pramod.dailyword.framework.ui.common.ExpandingBottomSheetDialogFragment
import com.pramod.dailyword.framework.ui.common.bindingadapter.OnChipClickListener

class ChipListDialogFragment :
    ExpandingBottomSheetDialogFragment<DialogChipListBinding>(R.layout.dialog_chip_list) {


    override fun getBottomSheetBehaviorView(): View {
        return binding.cardBottomSheet
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.title = arguments?.getString(EXTRA_TITLE)
        binding.listData = arguments?.getStringArrayList(EXTRA_CHIP_ITEM_LIST)
        binding.onChipClickListener = object : OnChipClickListener {
            override fun onChipClick(text: String) {
                val url = resources.getString(R.string.google_search_url) + text
                requireContext().openWebsite(url)
            }
        }
        binding.executePendingBindings()
    }

    override fun onStateChanged(bottomSheet: View, newState: Int) {
        super.onStateChanged(bottomSheet, newState)
        bottomSheetBehavior.isDraggable = !binding.nestedScrollView.canScrollVertically(-1)
    }


    companion object {
        private const val EXTRA_TITLE = "title"
        private const val EXTRA_CHIP_ITEM_LIST = "chip_list"

        val TAG = ChipListDialogFragment::class.java.simpleName

        fun show(title: String, chipList: List<String>, fragmentManager: FragmentManager) {
            val dialog = ChipListDialogFragment()
            dialog.arguments = bundleOf(
                EXTRA_TITLE to title,
                EXTRA_CHIP_ITEM_LIST to chipList
            )
            dialog.show(fragmentManager, TAG)
        }
    }
}