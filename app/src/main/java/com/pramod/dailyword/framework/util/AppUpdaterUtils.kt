package com.pramod.dailyword.framework.util

import android.app.Activity
import android.text.SpannableString
import android.text.style.ClickableSpan
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.pramod.dailyword.framework.ui.changelogs.Release
import com.pramod.dailyword.framework.ui.common.exts.showBasicDialogWithSpannable
import timber.log.Timber


fun Activity.buildUpdateAvailableToDownloadSpannableString(releaseNote: Release): SpannableString {
    val message =
        "A new update v${releaseNote.versionName} is available to download, click to view changelog."
    return buildSpannableMessage(message, releaseNote)
}

fun Activity.buildUpdateAvailableToInstallSpannableString(releaseNote: Release): SpannableString {
    val message =
        "A new update v${releaseNote.versionName} is ready to install, click to view changelog."
    return buildSpannableMessage(message, releaseNote)
}

private fun Activity.buildSpannableMessage(
    message: String,
    releaseNote: Release
): SpannableString {
    return SpannableString(message).apply {
        setSpan(
            object : ClickableSpan() {
                override fun onClick(widget: View) {
                    Timber.i("HomeActivity.TAG", "onClick: ")
                    showBasicDialogWithSpannable(
                        title = "Changelog",
                        message = CommonUtils.formatListAsBulletList(releaseNote.changes),
                        positiveText = "Close"
                    )

                }
            },
            message.length - 10,
            message.length - 1,
            SpannableString.SPAN_INCLUSIVE_INCLUSIVE
        )
    }
}

fun AppUpdateManager.safeStartUpdateFlowForResult(
    appUpdateInfo: AppUpdateInfo,
    appUpdateType: Int,
    activity: Activity,
    requestCode: Int,
    errorCallback: (e: Exception) -> Unit
) {
    try {
        startUpdateFlowForResult(
            appUpdateInfo,
            appUpdateType,
            activity,
            requestCode
        )
    } catch (e: Exception) {
        errorCallback.invoke(e)
    }

}


fun AppUpdateManager.safeStartUpdateFlowForResult(
    appUpdateInfo: AppUpdateInfo,
    appUpdateType: Int,
    fragment: Fragment,
    requestCode: Int,
    errorCallback: (e: Exception) -> Unit
) {
    try {
        startUpdateFlowForResult(
            appUpdateInfo,
            appUpdateType,
            fragment::startIntentSenderForResult,
            requestCode,
        )
    } catch (e: Exception) {
        errorCallback.invoke(e)
    }

}
