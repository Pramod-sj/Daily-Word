package com.pramod.dailyword.framework.ui.changelogs

import android.os.Bundle
import android.view.*
import androidx.core.view.updatePadding
import androidx.fragment.app.FragmentManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pramod.dailyword.R
import com.pramod.dailyword.databinding.DialogChangelogBinding
import com.pramod.dailyword.framework.ui.common.ExpandingBottomSheetDialogFragment
import com.pramod.dailyword.framework.ui.common.exts.doOnApplyWindowInsets
import com.pramod.dailyword.framework.ui.common.view.DividerItemDecoration
import com.pramod.dailyword.framework.util.CommonUtils

class ChangelogDialogFragment :
    ExpandingBottomSheetDialogFragment<DialogChangelogBinding>(R.layout.dialog_changelog) {


    private val adapter: ChangelogAdapter by lazy {
        val type = TypeToken.getParameterized(List::class.java, Changes::class.java).type
        val changelogList =
            Gson().fromJson<List<Changes>>(
                CommonUtils.loadJsonFromAsset(requireContext(), "change_logs.json"),
                type
            )
        ChangelogAdapter(changelogList)
    }




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindingAdapter()
        applyBottomInsetToRecyclerView()
    }


    private fun applyBottomInsetToRecyclerView() {
        binding.recyclerviewChangeLogs.doOnApplyWindowInsets { view, windowInsets, initialPadding, initialMargin ->
            binding.recyclerviewChangeLogs
                .updatePadding(
                    bottom = windowInsets.systemWindowInsetBottom
                )
        }
    }

    private fun bindingAdapter() {
        binding.recyclerviewChangeLogs.adapter = adapter
        binding.recyclerviewChangeLogs.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL,
                false,
                CommonUtils.dpToPixel(requireContext(), 30f).toInt()
            )
        )
    }

    override fun onStateChanged(bottomSheet: View, newState: Int) {
        super.onStateChanged(bottomSheet, newState)
        bottomSheetBehavior.isDraggable = !binding.recyclerviewChangeLogs.canScrollVertically(-1)
    }

    companion object {
        val TAG = ChangelogDialogFragment::class.java.simpleName

        fun show(fragmentManager: FragmentManager) {
            val dialog = ChangelogDialogFragment()
            dialog.show(fragmentManager, TAG)
        }
    }

    override fun getBottomSheetBehaviorView(): View {
        return binding.cardBottomSheet
    }

}