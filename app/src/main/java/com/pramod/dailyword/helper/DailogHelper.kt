package com.pramod.dailyword.helper

import android.content.Context
import android.content.DialogInterface
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pramod.dailyword.R
import java.util.*

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

