package com.pramod.todaysword.helper

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.widget.ArrayAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pramod.todaysword.R
import java.util.*

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