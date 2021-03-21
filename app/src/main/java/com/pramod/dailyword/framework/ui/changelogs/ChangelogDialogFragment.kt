package com.pramod.dailyword.framework.ui.changelogs

import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.DialogFragment
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
import com.pramod.dailyword.framework.ui.common.exts.configStatusBar
import com.pramod.dailyword.framework.ui.common.exts.doOnApplyWindowInsets
import com.pramod.dailyword.framework.ui.common.view.DividerItemDecoration
import com.pramod.dailyword.framework.util.CommonUtils
import com.pramod.dailyword.framework.util.convertNumberRangeToAnotherRange
import com.pramod.dailyword.framework.util.convertNumberRangeToAnotherRangeFloat

class ChangelogDialogFragment : DialogFragment() {
    lateinit var binding: DialogChangelogBinding

    lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    var statusBarHeight: Int = 0
    var navBarHeight: Int = 0
    var appBarHeight: Int = 0

    private val adapter: ChangelogAdapter by lazy {
        val type = TypeToken.getParameterized(List::class.java, Changes::class.java).type
        val changelogList =
            Gson().fromJson<List<Changes>>(
                CommonUtils.loadJsonFromAsset(requireContext(), "change_logs.json"),
                type
            )
        ChangelogAdapter(changelogList)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.AppTheme_FullScreenDialog)
    }

    override fun onStart() {
        super.onStart()
        EdgeToEdgePrefManager.newInstance(requireContext())
            .applyEdgeToEdgeIfEnabled(dialog?.window!!, true)
        dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        dialog?.setOnKeyListener { dialog, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            }
            return@setOnKeyListener true
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogChangelogBinding.inflate(
            LayoutInflater.from(requireContext()),
            container,
            false
        )
        binding.root.doOnApplyWindowInsets { view, windowInsets, initialPadding, initialMargin ->
            Log.i(TAG, "onCreateView: inset")
            statusBarHeight = windowInsets.systemWindowInsetTop
            navBarHeight = windowInsets.systemWindowInsetBottom
        }
        isViewsLoaded(binding.appBar) {
            appBarHeight = binding.appBar.height
        }


        bottomSheetBehavior = BottomSheetBehavior.from(binding.cardBottomSheet)
        bottomSheetBehavior.peekHeight =
            ((Resources.getSystem().displayMetrics.heightPixels) / 1.5f).toInt()
        bottomSheetBehavior.isHideable = true
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {

                bottomSheetBehavior.isDraggable = newState != BottomSheetBehavior.STATE_EXPANDED


                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    dismiss()
                }

                binding.recyclerviewChangeLogs.enableScroll(newState == BottomSheetBehavior.STATE_EXPANDED)



                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    if (!ThemeManager.isNightModeActive(requireContext())) {
                        dialog?.window?.configStatusBar(
                            makeLight = newState == BottomSheetBehavior.STATE_EXPANDED,
                            R.color.white,
                            true
                        )
                    }

                }


            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {

                applyBackgroundDim(slideOffset)

                applyRadiusBasedOnSlideOffset(slideOffset)

                applyPaddingTopAppbar(slideOffset)





                Log.i(TAG, "onSlide: recyclerView:" + binding.recyclerviewChangeLogs.paddingTop)


            }

        })
        return binding.root
    }

    private fun applyRadiusBasedOnSlideOffset(slideOffset: Float) {
        val radiusInPixel = 35f;
        val newCornerRadius = convertNumberRangeToAnotherRangeFloat(
            slideOffset,
            0.8f to 1f,
            radiusInPixel to 0f
        )

        Log.i(TAG, "onSlide: newCornerRadius:$newCornerRadius")

        val newShape: ShapeAppearanceModel = ShapeAppearanceModel.Builder()
            .setTopLeftCorner(
                CornerFamily.ROUNDED,
                if (newCornerRadius > radiusInPixel) radiusInPixel else newCornerRadius.toFloat()
            )
            .setTopRightCorner(
                CornerFamily.ROUNDED,
                if (newCornerRadius > radiusInPixel) radiusInPixel else newCornerRadius.toFloat()
            )
            .build()
        binding.cardBottomSheet.shapeAppearanceModel = newShape

    }

    private fun applyBackgroundDim(slideOffset: Float) {
        binding.coordinatorLayout.setBackgroundColor(
            ColorUtils.setAlphaComponent(
                Color.BLACK,
                convertNumberRangeToAnotherRange(
                    slideOffset,
                    -1 to 1,
                    100 to 255
                )
            )
        )
    }

    private fun applyPaddingTopAppbar(slideOffset: Float) {
        val topPadding = convertNumberRangeToAnotherRangeFloat(
            slideOffset,
            0.8f to 1f,
            0f to statusBarHeight.toFloat()
        )

        Log.i(TAG, "onSlide: padding: $topPadding: status:$statusBarHeight")

        /*binding.appBar.updatePadding(
            top = if (topPadding < 0) 0 else topPadding
        )*/


        (binding.textViewChangelogTitle.layoutParams as LinearLayout.LayoutParams).let {
            it.topMargin = if (topPadding < 0) 0 else topPadding
            binding.textViewChangelogTitle.layoutParams = it
        }

        /*binding.recyclerviewChangeLogs.updatePadding(
            top = if (topPadding < 0) appBarHeight else appBarHeight + topPadding
        )*/
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindingAdapter()
        enableBottomSheetDrag()
        dismissWhenClickOutside()
        binding.root.postDelayed({
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }, 300)
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

    private fun dismissWhenClickOutside() {
        binding.coordinatorLayout.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }

    companion object {
        val TAG = ChangelogDialogFragment::class.java.simpleName

        fun show(fragmentManager: FragmentManager) {
            val dialog = ChangelogDialogFragment()
            dialog.show(fragmentManager, TAG)
        }
    }

}