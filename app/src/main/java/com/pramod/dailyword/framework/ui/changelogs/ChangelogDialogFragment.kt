package com.pramod.dailyword.framework.ui.changelogs

import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import androidx.core.graphics.ColorUtils
import androidx.core.view.updatePadding
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pramod.dailyword.R
import com.pramod.dailyword.databinding.DialogChangelogBinding
import com.pramod.dailyword.framework.prefmanagers.EdgeToEdgePrefManager
import com.pramod.dailyword.framework.prefmanagers.ThemeManager
import com.pramod.dailyword.framework.transition.isViewsLoaded
import com.pramod.dailyword.framework.ui.common.ExpandingBottomSheetDialogFragment
import com.pramod.dailyword.framework.ui.common.exts.configStatusBar
import com.pramod.dailyword.framework.ui.common.exts.doOnApplyWindowInsets
import com.pramod.dailyword.framework.ui.common.view.DividerItemDecoration
import com.pramod.dailyword.framework.util.CommonUtils
import com.pramod.dailyword.framework.util.convertNumberRangeToAnotherRange
import com.pramod.dailyword.framework.util.convertNumberRangeToAnotherRangeFromFloat

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
        enableBottomSheetDrag()
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

    /**
     * This method is use to re-enable dragging feature when recycler can't be scroll up anymore
     */
    private fun enableBottomSheetDrag() {
        binding.recyclerviewChangeLogs.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                bottomSheetBehavior.isDraggable = !recyclerView.canScrollVertically(-1)
            }
        })
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