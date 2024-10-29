package com.pramod.dailyword.framework.helper

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.Fragment
import com.pramod.dailyword.R


fun Context.restartApp() {
    val intent = Intent(Intent.ACTION_MAIN)
    startActivity(intent)
    when (this) {
        is Activity -> {
            finish()
        }

        is Fragment -> {
            requireActivity().finish()
        }

        else -> {
            Toast.makeText(
                this,
                "Unable to finish previous activity or fragment activity",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}

fun Activity.restartActivity(smooth: Boolean = false) {
    startActivity(intent)
    if (smooth) {
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    } else {
        overridePendingTransition(0, 0)
    }
    finish()
}

fun Context.openWebsite(url: String) {
    val builder = CustomTabsIntent.Builder()
    builder.setShowTitle(false)
    builder.setUrlBarHidingEnabled(true)
    builder.setStartAnimations(
        this,
        R.anim.slide_in_right,
        R.anim.slide_out_left
    )
    builder.setExitAnimations(
        this,
        android.R.anim.slide_in_left,
        android.R.anim.slide_out_right
    )
    val customTabsIntent = builder.build()
    try {
        customTabsIntent.launchUrl(this, Uri.parse(url))
    } catch (e: Exception) {
        e.printStackTrace()
        val webpageUri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, webpageUri)
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(
                this,
                resources.getString(R.string.no_browser_app_message),
                Toast.LENGTH_SHORT
            ).show()
        }
    }


}


fun Context.openGmail(emails: Array<String>, subject: String, body: String) {
    val gmailPackageName = "com.google.android.gm"
    val isGmailInstalled = packageManager.getLaunchIntentForPackage(gmailPackageName) != null

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "message/rfc822"
        putExtra(Intent.EXTRA_EMAIL, emails)
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, body)
        if (isGmailInstalled) {
            `package` = gmailPackageName // Explicitly target Gmail
        }
    }

    if (isGmailInstalled) {
        startActivity(intent)
    } else {
        // Gmail is not installed, use Intent.createChooser or show an error message
        startActivity(Intent.createChooser(intent, "Choose an email client"))
        // or
        // Toast.makeText(this, resources.getString(R.string.no_gmail_app_message), Toast.LENGTH_SHORT).show()
    }
}

fun Context.openGoogleReviewPage() {
    val intent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("market://details?id=$packageName")
    )
    if (intent.resolveActivity(packageManager) != null) {
        startActivity(intent)
    } else {
        Toast.makeText(
            this,
            resources.getString(R.string.no_play_app_message),
            Toast.LENGTH_SHORT
        ).show()
    }
}

