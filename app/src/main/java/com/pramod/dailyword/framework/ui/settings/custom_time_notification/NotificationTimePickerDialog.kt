package com.pramod.dailyword.framework.ui.settings.custom_time_notification

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.pramod.dailyword.framework.prefmanagers.NotificationPrefManager
import com.pramod.dailyword.framework.ui.common.ComposeExpandingBottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotificationTimePickerDialog : ComposeExpandingBottomSheetDialogFragment() {


    companion object {

        const val EXTRA_NOTIFICATION_TRIGGER_TIME = "notificationTriggerTime"

        val TAG = NotificationTimePickerDialog::class.java.simpleName

        fun show(
            notificationTriggerTime: NotificationPrefManager.NotificationTriggerTime?,
            changeNotificationCallback: (NotificationPrefManager.NotificationTriggerTime?) -> Unit,
            fragmentManager: FragmentManager,
        ): NotificationTimePickerDialog {
            val dialog = NotificationTimePickerDialog()
            dialog.arguments = bundleOf(EXTRA_NOTIFICATION_TRIGGER_TIME to notificationTriggerTime)
            dialog.changeNotificationCallback = changeNotificationCallback
            dialog.show(fragmentManager, TAG)
            return dialog
        }
    }

    var changeNotificationCallback: (NotificationPrefManager.NotificationTriggerTime?) -> Unit = {}

    private var notificationTriggerTime: NotificationPrefManager.NotificationTriggerTime? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notificationTriggerTime =
            arguments?.getSerializable(EXTRA_NOTIFICATION_TRIGGER_TIME) as? NotificationPrefManager.NotificationTriggerTime
    }

    @Composable
    override fun GetComposable() {
        NotificationTimePicker(
            notificationTriggerTime = notificationTriggerTime,
            onSetNotificationTime = {
                changeNotificationCallback(it)
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            })
    }


}