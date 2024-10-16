package com.pramod.dailyword.framework.ui.common

import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.FloatRange
import androidx.appcompat.widget.AppCompatImageView
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.graphics.ColorUtils
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.card.MaterialCardView
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.ShapeAppearanceModel
import com.pramod.dailyword.R
import com.pramod.dailyword.databinding.DialogComposeLayoutBinding
import com.pramod.dailyword.framework.prefmanagers.EdgeToEdgePrefManager
import com.pramod.dailyword.framework.prefmanagers.ThemeManager
import com.pramod.dailyword.framework.ui.common.exts.configStatusBar
import com.pramod.dailyword.framework.ui.common.exts.doOnApplyWindowInsets
import com.pramod.dailyword.framework.ui.common.exts.getContextCompatColor
import com.pramod.dailyword.framework.util.convertNumberRangeToAnotherRange
import com.pramod.dailyword.framework.util.convertNumberRangeToAnotherRangeFromFloat
import com.pramod.dailyword.framework.util.convertNumberRangeToAnotherRangeToFloat
import timber.log.Timber

abstract class ComposeExpandingBottomSheetDialogFragment : DismissibleDialogFragment() {

    var topInset: Int = 0

    private var _binding: DialogComposeLayoutBinding? = null

    val binding get() = _binding!!

    lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    private fun getBottomSheetBehaviorView() = binding.cardBottomSheet


    @Composable
    abstract fun GetComposable()

    /**
     * override this method and return false when you don't want to stop bottom sheet dragging when
     * dialog is in expanded state
     * this method is returning true because to fix child view scrolling issue
     */
    open fun lockBottomSheetDragWhenExpanded(): Boolean {
        return true
    }

    /**
     * bottom sheet dialog opening delay (in millis)
     * default value is 300ms
     */
    open fun getInitialDelay(): Long {
        return 300
    }

    /**
     * close to 0 less than half expanded i.e. covering less screen
     * use = 0.5 half expanded i.e. covering half of the screen
     * close to 1 more than half expanded i.e. covering more screen
     */
    @FloatRange(from = 0.0, to = 1.0)
    open fun peekHeightFactor(): Float {
        return 0.7f
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
        dialog?.setOnKeyListener { _, keyCode, _ ->
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
        _binding = DataBindingUtil.inflate(
            LayoutInflater.from(requireContext()),
            R.layout.dialog_compose_layout,
            container,
            false
        )
        findBottomSheetBehavior()
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.root.doOnApplyWindowInsets { _, windowInsets, _, _ ->
            topInset = windowInsets.systemWindowInsetTop

        }



        dismissWhenClickOutside()
        binding.root.postDelayed({
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }, getInitialDelay())

        binding.composeView.setContent {
            GetComposable()
        }
        binding.composeView.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
    }

    private fun insertHorizontalRule() {
        val horizontalRule = AppCompatImageView(requireContext())
        horizontalRule.setImageResource(R.drawable.ic_round_horizontal_rule_24)
        horizontalRule.imageTintList =
            ColorStateList.valueOf(requireActivity().getContextCompatColor(R.color.app_icon_tint))
    }


    private fun findBottomSheetBehavior() {
        bottomSheetBehavior = BottomSheetBehavior.from(getBottomSheetBehaviorView())


        val factorInPercentage = convertNumberRangeToAnotherRangeToFloat(
            peekHeightFactor(),
            0 to 1,
            0 to 100
        )

        val peekHeightInPixel =
            (factorInPercentage * Resources.getSystem().displayMetrics.heightPixels) / 100f

        bottomSheetBehavior.peekHeight = peekHeightInPixel.toInt()

        bottomSheetBehavior.isHideable = true

        bottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback)

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }


    private val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss()
            }

            bottomSheetBehavior.isDraggable =
                !lockBottomSheetDragWhenExpanded() && newState != BottomSheetBehavior.STATE_EXPANDED

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                if (!ThemeManager.isNightModeActive(requireContext())) {
                    dialog?.window?.configStatusBar(
                        makeLight = newState == BottomSheetBehavior.STATE_EXPANDED,
                        R.color.white,
                        true
                    )
                }

            }

            //dispatch stateChange updates to extending fragments
            this@ComposeExpandingBottomSheetDialogFragment.onStateChanged(bottomSheet, newState)
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            applyBackgroundDim(slideOffset)

            applyTopInset(slideOffset)

            applyRadiusBasedOnSlideOffset(slideOffset)

            //dispatch onSlide updates to extending fragments
            this@ComposeExpandingBottomSheetDialogFragment.onSlide(bottomSheet, slideOffset)
        }
    }

    open fun onStateChanged(bottomSheet: View, newState: Int) {}

    open fun onSlide(bottomSheet: View, slideOffset: Float) {}


    private fun applyBackgroundDim(slideOffset: Float) {
        binding.root.setBackgroundColor(
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

    private fun applyTopInset(slideOffset: Float) {
        val topMargin = convertNumberRangeToAnotherRangeFromFloat(
            slideOffset,
            0.8f to 1f,
            0f to topInset.toFloat()
        )
        if (getBottomSheetBehaviorView() is MaterialCardView) {
            (getBottomSheetBehaviorView() as MaterialCardView).setContentPadding(
                0, if (topMargin < 0) 0 else topMargin, 0, 0
            )
        }
    }


    private fun applyRadiusBasedOnSlideOffset(slideOffset: Float) {
        if (getBottomSheetBehaviorView() is MaterialCardView) {

            val radiusInPixel = 35f
            val newCornerRadius = convertNumberRangeToAnotherRangeFromFloat(
                slideOffset,
                0.8f to 1f,
                radiusInPixel to 0f
            )

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
            (getBottomSheetBehaviorView() as MaterialCardView).shapeAppearanceModel = newShape
        } else {
            Timber.e(

                "applyRadiusBasedOnSlideOffset: not a card view, hence cannot apply radius manipulation"
            )
        }
    }


    private fun dismissWhenClickOutside() {
        binding.root.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }

    override fun onDestroyView() {
        bottomSheetBehavior.removeBottomSheetCallback(bottomSheetCallback)
        _binding = null
        super.onDestroyView()
    }


    companion object {
        val TAG = ExpandingBottomSheetDialogFragment::class.java.simpleName
    }

}