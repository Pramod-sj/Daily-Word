package com.pramod.dailyword.helper

import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.AdapterView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.databinding.DataBindingUtil
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pramod.dailyword.R
import com.pramod.dailyword.databinding.DialogWebviewLayoutBinding
import com.pramod.dailyword.ui.about_app.AboutAppActivity
import java.util.*


fun AboutAppActivity.showLib() {
    val dialog = MaterialAlertDialogBuilder(this)
        .setTitle("Open source libraries")
        .setItems(R.array.libraries_name, null)
        .create()
    dialog.setOnShowListener {
        Log.i("DIALOG", "dialog")
        for (i in 0 until dialog.listView.count) {
            val textView: TextView = dialog.listView[i].findViewById(android.R.id.text1)
            textView.linksClickable = true
            textView.movementMethod = LinkMovementMethod.getInstance()
            textView.background = null
        }
    }
    dialog.show()

}

fun Context.showBasicDialog(
    title: String,
    message: String,
    positiveText: String?,
    positiveClickCallback: (() -> Unit)?,
    negativeText: String?,
    negativeClickCallback: (() -> Unit)?,
    neutralText: String?,
    neutralClickCallback: (() -> Unit)?
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
    builder.create().show()
}

fun Context.showWebViewDialog(url: String) {
    val dialogWebviewLayoutBinding: DialogWebviewLayoutBinding = DataBindingUtil.inflate(
        LayoutInflater.from(this),
        R.layout.dialog_webview_layout,
        null,
        false
    )
    Log.i("URL", url)
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
    builder.show()
}


class DailogHelper {
    companion object {
        fun showRadioDialog(
            context: Context,
            title: String,
            arrayResId: Int,
            selectedItem: String,
            itemSelectedCallback: (String) -> Unit
        ) {
            val items = context.resources.getStringArray(arrayResId)
            var selectedItemIndex = -1
            items.forEachIndexed { i: Int, s: String ->
                if (s.toLowerCase(Locale.getDefault())
                    == selectedItem.toLowerCase(Locale.getDefault())
                ) {
                    selectedItemIndex = i
                }
            }

            val dialog = MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setSingleChoiceItems(
                    items,
                    selectedItemIndex
                ) { dialogInterface: DialogInterface, i: Int ->
                    dialogInterface.dismiss()
                    itemSelectedCallback.invoke(items[i].toUpperCase())
                }
            dialog.show()
        }
    }
}

