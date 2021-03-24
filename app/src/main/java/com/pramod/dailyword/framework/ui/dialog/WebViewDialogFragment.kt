package com.pramod.dailyword.framework.ui.dialog

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.pramod.dailyword.R
import com.pramod.dailyword.databinding.DialogWebviewLayoutBinding
import com.pramod.dailyword.framework.prefmanagers.ThemeManager
import com.pramod.dailyword.framework.ui.common.ExpandingBottomSheetDialogFragment
import com.pramod.dailyword.framework.ui.common.view.ObservableWebView

class WebViewDialogFragment :
    ExpandingBottomSheetDialogFragment<DialogWebviewLayoutBinding>(R.layout.dialog_webview_layout) {

    override fun getBottomSheetBehaviorView(): View {
        return binding.cardBottomSheet
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (ThemeManager.isNightModeActive(requireContext())) {
            try {
                if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                    WebSettingsCompat.setForceDark(
                        binding.webView.settings,
                        WebSettingsCompat.FORCE_DARK_ON
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        binding.txtViewAppBar.text = arguments?.getString(EXTRA_DIALOG_TITLE)
        binding.webView.loadUrl(arguments?.getString(EXTRA_WEB_PAGE_URL))
        detectWebViewScroll()
    }

    private fun detectWebViewScroll() {
        binding.webView.onScrollChangedCallback =
            object : ObservableWebView.OnScrollChangeListener {
                override fun onScrollChanged(
                    currentHorizontalScroll: Int,
                    currentVerticalScroll: Int,
                    oldHorizontalScroll: Int,
                    oldcurrentVerticalScroll: Int
                ) {
                    bottomSheetBehavior.isDraggable = !binding.webView.canScrollVertically(-1)
                }

            }
    }

    override fun onStateChanged(bottomSheet: View, newState: Int) {
        super.onStateChanged(bottomSheet, newState)
        bottomSheetBehavior.isDraggable = !binding.webView.canScrollVertically(-1)
    }

    companion object {
        val TAG = WebViewDialogFragment::class.java.simpleName
        private const val EXTRA_DIALOG_TITLE = "title"
        private const val EXTRA_WEB_PAGE_URL = "url";

        fun show(title: String, url: String, fragmentManager: FragmentManager) {
            val dialog = WebViewDialogFragment()
                .apply {
                    arguments = bundleOf(
                        EXTRA_DIALOG_TITLE to title,
                        EXTRA_WEB_PAGE_URL to url
                    )
                }
            dialog.show(fragmentManager, TAG)
        }
    }

}