package com.pramod.dailyword.framework.ui.common.exts

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.text.Spannable
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.AbsListView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.analytics.FirebaseAnalytics
import com.pramod.dailyword.R
import com.pramod.dailyword.databinding.BottomSheetDialogLayoutBinding
import com.pramod.dailyword.databinding.DialogWebviewLayoutBinding
import com.pramod.dailyword.framework.prefmanagers.PrefManager
import com.pramod.dailyword.framework.ui.aboutapp.AboutAppActivity
import com.pramod.dailyword.framework.ui.donate.DonateBottomDialogFragment
import timber.log.Timber


/*fun Context.showLottieDialog(fileName: String, title: String, body: String) {
    val binding: DialogLottieTitleBodyLayoutBinding = DataBindingUtil.inflate(
        LayoutInflater.from(this),
        R.layout.dialog_lottie_title_body_layout,
        null,
        false
    )
    val dialog = MaterialAlertDialogBuilder(this)
        .setView(binding.root)
        .setPositiveButton("Okay", null)
        .create()

    binding.lottieView.setAnimation(fileName)
    binding.titleTextView.text = title
    binding.bodyTextView.text = body

    dialog.show()

}*/

fun AboutAppActivity.showLib() {
    val builder = MaterialAlertDialogBuilder(this)
        .setTitle("Open source libraries")
        .setItems(R.array.libraries_name, null)

    val alertDialog = builder.create()
    alertDialog.applyStyleOnAlertDialog()
    alertDialog.setOnShowListener {

        alertDialog.listView.setOnScrollListener(object : AbsListView.OnScrollListener {
            override fun onScroll(
                view: AbsListView?,
                firstVisibleItem: Int,
                visibleItemCount: Int,
                totalItemCount: Int
            ) {
                for (i in 0 until visibleItemCount) {
                    val textView: TextView =
                        alertDialog.listView[i].findViewById(android.R.id.text1)
                    textView.linksClickable = true
                    textView.movementMethod = LinkMovementMethod.getInstance()
                    textView.background = null
                }
            }

            override fun onScrollStateChanged(view: AbsListView?, scrollState: Int) {

            }

        })
    }
    alertDialog.show()

}

fun Context.showBasicDialogWithSpannable(
    title: String,
    message: Spannable,
    positiveText: String? = null,
    positiveClickCallback: (() -> Unit)? = null,
    negativeText: String? = null,
    negativeClickCallback: (() -> Unit)? = null,
    neutralText: String? = null,
    neutralClickCallback: (() -> Unit)? = null
) {
    val builder = MaterialAlertDialogBuilder(this)
        .setTitle(title)
        .setMessage(message)
    positiveText?.let {
        builder.setPositiveButton(positiveText) { dialogInterface: DialogInterface, i: Int ->
            dialogInterface.dismiss()
            positiveClickCallback?.invoke()
        }
    }
    negativeText?.let {
        builder.setNegativeButton(negativeText) { dialogInterface: DialogInterface, i: Int ->
            dialogInterface.dismiss()
            negativeClickCallback?.invoke()
        }
    }

    neutralText?.let {
        builder.setNeutralButton(neutralText) { dialogInterface: DialogInterface, i: Int ->
            dialogInterface.dismiss()
            neutralClickCallback?.invoke()
        }
    }
    val alertDialog = builder.create()
    alertDialog.applyStyleOnAlertDialog()
    alertDialog.show()
}

fun Context.showBasicDialog(
    title: String,
    message: String,
    positiveText: String? = null,
    positiveClickCallback: (() -> Unit)? = null,
    negativeText: String? = null,
    negativeClickCallback: (() -> Unit)? = null,
    neutralText: String? = null,
    neutralClickCallback: (() -> Unit)? = null
) {
    showBasicDialogWithSpannable(
        title,
        SpannableString(message),
        positiveText,
        positiveClickCallback,
        negativeText,
        negativeClickCallback,
        neutralText,
        neutralClickCallback
    )
}


fun Context.showWebViewDialog(url: String) {
    val dialogWebviewLayoutBinding: DialogWebviewLayoutBinding = DataBindingUtil.inflate(
        LayoutInflater.from(this),
        R.layout.dialog_webview_layout,
        null,
        false
    )
    Timber.i("URL", url)

    if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
        WebSettingsCompat.setAlgorithmicDarkeningAllowed(
            dialogWebviewLayoutBinding.webView.settings,
            true
        )
    }

    dialogWebviewLayoutBinding.webView.webViewClient = object : WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            dialogWebviewLayoutBinding.webViewProgress.visibility = View.GONE
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            dialogWebviewLayoutBinding.webViewProgress.visibility = View.VISIBLE
        }
    }
    dialogWebviewLayoutBinding.webView.loadUrl(url)
    val builder = MaterialAlertDialogBuilder(this)
        .setView(dialogWebviewLayoutBinding.root)

    val alertDialog = builder.create()
    alertDialog.applyStyleOnAlertDialog()
    alertDialog.show()

    alertDialog.window?.setLayout(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT
    )


}

fun Activity.showBottomSheet(
    title: String,
    desc: Spannable,
    cancellable: Boolean = true,
    positiveText: String? = null,
    positiveClickCallback: (() -> Unit)? = null,
    negativeText: String? = null,
    negativeClickCallback: (() -> Unit)? = null,
    onDismissCallback: (() -> Unit)? = null
) {

    val bottomSheetDialog = BottomSheetDialog(this, R.style.AppTheme_BottomSheetDialog)
    val binding: BottomSheetDialogLayoutBinding = DataBindingUtil.inflate(
        LayoutInflater.from(this),
        R.layout.bottom_sheet_dialog_layout,
        null,
        false
    )
    bottomSheetDialog.setCancelable(cancellable)

    bottomSheetDialog.setContentView(binding.root)
    binding.bottomSheetTitle.text = title
    binding.bottomSheetBody.text = desc

    binding.bottomSheetBtnPositive.isVisible = positiveText != null
    binding.bottomSheetBtnNegative.isVisible = negativeText != null

    binding.bottomSheetBtnPositive.text = positiveText
    binding.bottomSheetBtnNegative.text = negativeText
    binding.bottomSheetBtnPositive.setOnClickListener {
        bottomSheetDialog.dismiss()
        positiveClickCallback?.invoke()
    }
    binding.bottomSheetBtnNegative.setOnClickListener {
        bottomSheetDialog.dismiss()
        negativeClickCallback?.invoke()
    }
    bottomSheetDialog.setOnDismissListener {
        onDismissCallback?.invoke()
    }
    bottomSheetDialog.show()

}

fun Activity.showBottomSheet(
    title: String,
    desc: String,
    cancellable: Boolean = true,
    positiveText: String? = null,
    positiveClickCallback: (() -> Unit)? = null,
    negativeText: String? = null,
    negativeClickCallback: (() -> Unit)? = null,
    onDismissCallback: (() -> Unit)? = null
) {
    showBottomSheet(
        title,
        SpannableString(desc),
        cancellable,
        positiveText,
        positiveClickCallback,
        negativeText,
        negativeClickCallback,
        onDismissCallback
    )
}


fun Context.showStaticPageDialog(
    layoutId: Int,
    positiveText: String? = null,
    positiveClickCallback: (() -> Unit)? = null,
    negativeText: String? = null,
    negativeClickCallback: (() -> Unit)? = null,
    neutralText: String? = null,
    neutralClickCallback: (() -> Unit)? = null
) {
    val builder = MaterialAlertDialogBuilder(this)
        .setView(layoutId)
    positiveText?.let {
        builder.setPositiveButton(positiveText) { dialogInterface: DialogInterface, i: Int ->
            dialogInterface.dismiss()
            positiveClickCallback?.invoke()
        }
    }
    negativeText?.let {
        builder.setNegativeButton(negativeText) { dialogInterface: DialogInterface, i: Int ->
            dialogInterface.dismiss()
            negativeClickCallback?.invoke()
        }
    }

    neutralText?.let {
        builder.setNeutralButton(neutralText) { dialogInterface: DialogInterface, i: Int ->
            dialogInterface.dismiss()
            neutralClickCallback?.invoke()
        }
    }
    val alertDialog = builder.create()
    alertDialog.applyStyleOnAlertDialog()
    alertDialog.show()
}


class DailogHelper {
    companion object {
        fun showRadioDialog(
            context: Context,
            title: String,
            arrayResId: Int,
            selectedItem: String,
            positionText: String?,
            negativeText: String?,
            positiveClickCallback: ((String) -> Unit)?
        ) {
            val items = context.resources.getStringArray(arrayResId)
            var selectedItemIndex = -1
            items.forEachIndexed { i: Int, s: String ->
                if (s.equals(selectedItem, ignoreCase = true)
                ) {
                    selectedItemIndex = i
                }
            }

            val builder: MaterialAlertDialogBuilder =
                MaterialAlertDialogBuilder(context)
                    .setTitle(title)
                    .setSingleChoiceItems(
                        items,
                        selectedItemIndex
                    )
                    { dialogInterface: DialogInterface, i: Int ->
                        selectedItemIndex = i
                    }
            positionText?.let {
                builder.setPositiveButton(it) { dialogInterface: DialogInterface, i: Int ->
                    dialogInterface.dismiss()
                    positiveClickCallback?.invoke(items[selectedItemIndex])
                }
            }
            negativeText?.let {
                builder.setNegativeButton(negativeText) { dialogInterface: DialogInterface, i: Int ->
                    dialogInterface.dismiss()
                }
            }
            val alertDialog = builder.create()
            alertDialog.applyStyleOnAlertDialog()
            alertDialog.show()
        }
    }
}

fun FragmentActivity.shouldShowSupportDevelopmentDialog() {
    val prefManager = PrefManager.getInstance(this)
    prefManager.incrementSupportUsDialogCalledCount()
    val frequency = if (prefManager.getAppLaunchCount() > 50) 10 else 20
    if ((prefManager.getSupportUsDialogCalledCount() % frequency == 0
                && prefManager.hasDonated() == false) && !prefManager.getNeverShowSupportUsDialog()
    ) {
        showSupportDevelopmentDialog {
            prefManager.setNeverShowSupportUsDialog(true)
        }
    } else {
        Timber.i(

            "shouldShowSupportDevelopmentDialog: not showing:${prefManager.getSupportUsDialogCalledCount() % 20} == 0"
        )
    }
}


fun FragmentActivity.showSupportDevelopmentDialog(neverCallback: () -> Unit) {
    FirebaseAnalytics.getInstance(baseContext).logEvent(
        FirebaseAnalytics.Event.SCREEN_VIEW,
        bundleOf(FirebaseAnalytics.Param.SCREEN_NAME to "support_development_dialog")
    )
    val builder = MaterialAlertDialogBuilder(this)
        .setBackgroundInsetBottom(10)
        .setBackgroundInsetTop(10)
        .setBackgroundInsetStart(50)
        .setBackgroundInsetEnd(50)
        .setView(R.layout.dialog_support_development)
        .setNeutralButton(
            "May be later"
        ) { dialog, which -> }
        .setNegativeButton("Never") { dialog, which ->
            neverCallback()
        }
        .setPositiveButton("Donate now") { dialog, which ->
            DonateBottomDialogFragment.show(supportFragmentManager)
        }
    val alertDialog = builder.create()
    alertDialog.applyStyleOnAlertDialog()
    alertDialog.show()
}


fun AlertDialog.applyStyleOnAlertDialog() {
    window?.let { window ->
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        window.setDimAmount(0.75f)
        window.setWindowAnimations(R.style.DialogWindowAnimation)
    }
}


fun Context.showCheckboxDialog(
    title: String,
    items: List<String>,
    selectedItems: List<String>,
    positiveText: String = "Okay",
    onPositiveClickCallback: (newSelectedItems: List<String>) -> Unit = {},
    negativeText: String = "Cancel",
    onNegativeClickCallback: () -> Unit = {},
) {
    val selected = selectedItems.toMutableSet()
    val builder: MaterialAlertDialogBuilder =
        MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setMultiChoiceItems(
                items.toTypedArray(),
                items.map { it in selectedItems }.toBooleanArray()
            ) { dialog, which, isChecked ->
                val item = items[which]
                if (isChecked) selected.add(item)
                else selected.remove(item)
            }.setPositiveButton(positiveText) { dialog, which ->
                onPositiveClickCallback(selected.toList())
            }.setNegativeButton(negativeText) { dialog, which ->
                onNegativeClickCallback()
            }

    val alertDialog = builder.create()
    alertDialog.applyStyleOnAlertDialog()
    alertDialog.show()
}




