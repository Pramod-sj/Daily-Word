package com.pramod.dailyword.framework.ui.common.exts

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.AbsListView
import android.widget.TextView
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pramod.dailyword.R
import com.pramod.dailyword.databinding.BottomSheetDialogLayoutBinding
import com.pramod.dailyword.databinding.DialogWebviewLayoutBinding
import com.pramod.dailyword.framework.ui.aboutapp.AboutAppActivity


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
    val dialog = MaterialAlertDialogBuilder(this)
        .setTitle("Open source libraries")
        .setItems(R.array.libraries_name, null)
        .create()
    dialog.setOnShowListener {

        dialog.listView.setOnScrollListener(object : AbsListView.OnScrollListener {
            override fun onScroll(
                view: AbsListView?,
                firstVisibleItem: Int,
                visibleItemCount: Int,
                totalItemCount: Int
            ) {
                for (i in 0 until visibleItemCount) {
                    val textView: TextView = dialog.listView[i].findViewById(android.R.id.text1)
                    textView.linksClickable = true
                    textView.movementMethod = LinkMovementMethod.getInstance()
                    textView.background = null
                }
            }

            override fun onScrollStateChanged(view: AbsListView?, scrollState: Int) {

            }

        })
    }
    dialog.show()

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
    val dialog = builder.create()
    dialog.show()
}

fun Context.showWebViewDialog(url: String) {
    val dialogWebviewLayoutBinding: DialogWebviewLayoutBinding = DataBindingUtil.inflate(
        LayoutInflater.from(this),
        R.layout.dialog_webview_layout,
        null,
        false
    )
    Log.i("URL", url)

    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
        dialogWebviewLayoutBinding.webView.settings
            .forceDark = WebSettings.FORCE_DARK_ON
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
    val dialog = MaterialAlertDialogBuilder(this)
        .setView(dialogWebviewLayoutBinding.root)
        .create()

    dialog.show()

    dialog.window?.setLayout(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT
    )


}

fun Activity.showBottomSheet(
    title: String,
    desc: String,
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
    val dialog = builder.create()
    dialog.show()
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

            val dialog: MaterialAlertDialogBuilder =
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
                dialog.setPositiveButton(it) { dialogInterface: DialogInterface, i: Int ->
                    dialogInterface.dismiss()
                    positiveClickCallback?.invoke(items[selectedItemIndex])
                }
            }
            negativeText?.let {
                dialog.setNegativeButton(negativeText) { dialogInterface: DialogInterface, i: Int ->
                    dialogInterface.dismiss()
                }
            }

            dialog.show()
        }
    }
}

