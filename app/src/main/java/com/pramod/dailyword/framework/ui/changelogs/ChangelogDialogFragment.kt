package com.pramod.dailyword.framework.ui.changelogs

import android.os.Bundle
import android.view.View
import androidx.core.view.updatePadding
import androidx.fragment.app.FragmentManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pramod.dailyword.BuildConfig
import com.pramod.dailyword.R
import com.pramod.dailyword.databinding.DialogChangelogBinding
import com.pramod.dailyword.framework.firebase.FBRemoteConfig
import com.pramod.dailyword.framework.ui.common.ExpandingBottomSheetDialogFragment
import com.pramod.dailyword.framework.ui.common.exts.doOnApplyWindowInsets
import com.pramod.dailyword.framework.ui.common.view.DividerItemDecoration
import com.pramod.dailyword.framework.util.CommonUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChangelogDialogFragment :
    ExpandingBottomSheetDialogFragment<DialogChangelogBinding>(R.layout.dialog_changelog) {

    @Inject
    lateinit var fbRemoteConfig: FBRemoteConfig

    private val adapter: ChangelogAdapter by lazy {
        if (fbRemoteConfig.getReleases()?.isNotEmpty() == true) {
            ChangelogAdapter(
                fbRemoteConfig.getReleases()?.filter { it.versionCode <= BuildConfig.VERSION_CODE })
        } else {
            val type = TypeToken.getParameterized(List::class.java, Release::class.java).type
            val changelogList = Gson().fromJson<List<Release>>(
                CommonUtils.loadJsonFromAsset(requireContext(), "change_logs.json"), type
            )
            ChangelogAdapter(changelogList)
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindingAdapter()
        applyBottomInsetToRecyclerView()
    }


    private fun applyBottomInsetToRecyclerView() {
        binding.recyclerviewChangeLogs.doOnApplyWindowInsets { _, windowInsets, _, _ ->
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